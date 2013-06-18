/*
 * Copyright J. Craig Venter Institute, 2013
 *
 * The creation of this program was supported by J. Craig Venter Institute
 * and National Institute for Allergy and Infectious Diseases (NIAID),
 * Contract number HHSN272200900007C.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jcvi.ometa.hibernate.dao;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.*;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 3:00 PM
 * <p/>
 * Special base class to be extended by all true Hibernate Data Access Objects in this project.
 */
public abstract class HibernateDAO {

    // These are whatever specific names for events are cached here.
    public static final String EVENT_STATUS_ACTIVE = "Active";
    public static final String EVENT_STATUS_INACTIVE = "InActive";

    protected Logger logger = Logger.getLogger(HibernateDAO.class);

    /**
     * Grab any meta attribute by lookup value, given its final class.
     */
    protected <T extends MetaAttributeModelBean> T getMetaAttribute(
            Long lookupValueId, Long projectId, Session session, Class clazz) throws DAOException {
        T metaAttribute = null;
        try {
            Criteria crit = session.createCriteria(clazz);
            crit.add(Restrictions.eq("nameLookupId", lookupValueId));
            crit.add(Restrictions.eq("projectId", projectId));
            List results = crit.list();
            Date latestDate = null;
            for (Object nextResult : results) {
                T nextMetaAttribute = (T) nextResult;
                if (latestDate == null || nextMetaAttribute.getCreationDate().after(latestDate)) {
                    metaAttribute = nextMetaAttribute;
                }
            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return metaAttribute;
    }

    /**
     * Locate ID of project whose name is given.
     *
     * @param logName     for writing back sufficient log info to work out what went wrong.
     * @param session     for db.
     * @param projectName find by this.
     * @return ID for project.
     */
    protected Long findProjectId(String logName, Session session, String projectName) throws DAOException {
        if (projectName == null) {
            throw new DAOException("Do not attempt to find a null-named project object for  " + logName);
        }
        Criteria crit = session.createCriteria(Project.class);
        crit.add(Restrictions.eq("projectName", projectName));
        List results = crit.list();
        if (results.size() == 0) {
            throw new DAOException(
                    "Project " + projectName +
                            " associated with object " + logName +
                            " not found in database."
            );
        } else {
            Project project = (Project) results.get(0);
            return project.getProjectId();
        }
    }

    /**
     * Locate ID of project whose name is given.
     *
     * @param logName    for writing back sufficient log info to work out what went wrong.
     * @param session    for db.
     * @param sampleName find by this.
     * @return ID for project.
     */
    protected Long findSampleId(String logName, Session session, String sampleName) throws DAOException {
        if (sampleName == null) {
            throw new DAOException("Do not attempt to find a null-named project object for  " + logName);
        }
        Criteria crit = session.createCriteria(Sample.class);
        crit.add(Restrictions.eq("sampleName", sampleName));
        List results = crit.list();
        if (results.size() == 0) {
            throw new DAOException(
                    "Sample " + sampleName +
                            " associated with object " + logName +
                            " not found in database."
            );
        } else if (results.size() > 1) {
            throw new DAOException(
                    "Sample " + sampleName +
                            " associated with object " + logName +
                            " found multiple times in database."
            );
        } else {
            Sample sample = (Sample) results.get(0);
            return sample.getProjectId();
        }
    }

    /**
     * Need to find an ID for a lookup name.
     *
     * @param name    find by this.
     * @param logName for writeback.
     * @return lookup value id.
     * @throws DAOException thrown for invalid inputs or invalid cardinality of outputs.
     */
    protected LookupValue findLookupValue(String name, String logName, Session session) throws DAOException {
        if (name == null) {
            throw new DAOException("Do not attempt to find a lookup ID for a null name, for object " + logName);
        }

        LookupValue returnValue = null;
        Criteria crit = session.createCriteria(LookupValue.class);
        crit.add(Restrictions.eq("name", name));
        List results = crit.list();
        if (results.size() > 1) {
            throw new DAOException(
                    "Lookup value " + name + " for object " + logName + " found multiple times in db."
            );
        } else if (results.size() == 1) {
            returnValue = (LookupValue) results.get(0);
        }
        return returnValue;
    }

    /**
     * Sets up created-by and created-date in the model, using 'environmental' information.
     *
     * @param model    will be written to db.
     * @param username supplied as actor of action.
     * @throws DAOException thrown if impossible to establish actor id.
     */
    protected void handleCreationTracking(ModelBean model, String username, Date transactionDate, Session session)
            throws DAOException {
        if ((model.getCreatedBy() == null || model.getCreatedBy() == 0) && username != null) {
            // First resolve the actor name to an actor ID.
            Actor actor = new ActorDAO().getActorByLoginName(username, session);
            if (actor == null) {
                throw new DAOException("Actor " + username + " unknown.");
            } else {
                Long actorId = actor.getLoginId();
                model.setCreatedBy(actorId);
            }
        }

        // Now, today's date is creation date.
        if (model.getCreationDate() == null) {
            model.setCreationDate(transactionDate);
        } else {
            model.setModifiedDate(transactionDate);
        }
    }

    /**
     * Must ensure that the reference to the project, is satisfied by a real project already in database,
     * and get its ID into the new model object.
     */
    protected void handleProjectRelation(
            ProjectReferencingModelBean model, String projectName, String logName, Session session)
            throws DAOException {

        Long projectId = model.getProjectId();
        if (projectId == null || projectId == 0) {
            model.setProjectId(findProjectId(logName, session, projectName));

        }
    }

    /**
     * Must ensure that the reference to the sample, is satisfied by a real sample already in database,
     * and get its ID into the new model object.
     */
    protected void handleSampleRelation(
            SampleReferencingModelBean model, String sampleName, String logName, Session session)
            throws DAOException {

        Long projectId = model.getSampleId();
        if (projectId == null || projectId == 0) {
            model.setSampleId(findSampleId(logName, session, sampleName));

        }
    }

    /**
     * Ensure that string name is converted to its lookup value ID.
     */
    protected void locateAttribNameLookupId(
            MetaAttributeModelBean model, Session session, String lookupValueType)
            throws DAOException {

        Long lkuvluId = model.getNameLookupId();
        String attributeName = model.getAttributeName();
        lkuvluId = getLookupValueId(model, session, lookupValueType, lkuvluId, attributeName);

        if (lkuvluId == null) {
            throw new DAOException("Failed to obtain a lookup value for " + attributeName);
        } else {
            model.setNameLookupId(lkuvluId);
        }
    }

    /**
     * Ensure that lookup value ID is converted to its readable name.
     */
    protected void expandLookupValueId(
            MetaAttributeModelBean model, Session session)
            throws DAOException {

        Long lkuvluId = model.getNameLookupId();
        Criteria crit = session.createCriteria(LookupValue.class);
        crit.add(Restrictions.eq("lookupValueId", lkuvluId));
        List results = crit.list();
        if (results.size() == 1) {
            LookupValue lv = (LookupValue) results.get(0);
            model.setAttributeName(lv.getName());
        } else {
            throw new DAOException("Failed to find lookup value ID expansion for " + lkuvluId);
        }
    }

    /**
     * Ensure that all lookup value IDs from models are converted to its readable name.
     */
    protected <B extends MetaAttributeModelBean> void expandLookupValueIds(
            List<B> models, Session session)
            throws DAOException {

        if (models == null || models.size() == 0)
            return;

        // Get mapping of all lookup value ids for query.
        List<Long> lookupValueIds = new ArrayList<Long>();
        for (B model : models) {
            Long lkuvluId = model.getNameLookupId();
            lookupValueIds.add(lkuvluId);
        }

        // Query to get all the lookup values for the ids.
        Criteria crit = session.createCriteria(LookupValue.class);
        crit.add(Restrictions.in("lookupValueId", lookupValueIds));
        List<LookupValue> results = crit.list();
        Map<Long, LookupValue> idVsValue = new HashMap<Long, LookupValue>();
        for (LookupValue lv : results) {
            idVsValue.put(lv.getLookupValueId(), lv);
        }

        // Back-fill the attribute names based on the lookup values found in query.
        for (B model : models) {
            Long lvId = model.getNameLookupId();
            LookupValue lv = idVsValue.get(lvId);
            model.setAttributeName(lv.getName());
        }
    }

    /**
     * Expand a lookup value, if needed, from DB.  Then validate it for type, etc..
     */
    protected Long getLookupValueId(
            MetaAttributeModelBean model, Session session, String eventType, Long lkuvluId, String lookupValueName)
            throws DAOException {
        if (lkuvluId == null) {
            LookupValue lookupValue = findLookupValue(lookupValueName, model.getClass().getName(), session);
            if (lookupValue != null) {
                lkuvluId = getValidatedLookupValueId(model, eventType, lookupValue);
            }
        }
        return lkuvluId;
    }

    /**
     * Given a lookup ID and requested type, find out if it properly matches.
     */
    private Long getValidatedLookupValueId(
            MetaAttributeModelBean model, String lookupValueType, LookupValue lookupValue)
            throws DAOException {
        Long lkuvluId;
        if (!lookupValue.getType().equals(lookupValueType)) {
            throw new DAOException(
                    "Attempted to reference a meta attribute as type " +
                            lookupValueType +
                            " using attribute name " +
                            lookupValue.getName() +
                            " which exists and is for attributes of namespace " +
                            lookupValue.getType()
            );
        } else if ((model.getDataType() != null) &&
                (!lookupValue.getDataType().equals(model.getDataType()))) {
            throw new DAOException(
                    "Attempted to reference a meta attribute with data type " +
                            model.getDataType() +
                            " using attribute name " +
                            lookupValue.getName() +
                            " which exists and is for attributes of data type " +
                            lookupValue.getDataType()
            );
        } else {
            lkuvluId = lookupValue.getLookupValueId();
        }
        return lkuvluId;
    }
}
