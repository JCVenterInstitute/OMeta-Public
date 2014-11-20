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

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.ProjectMetaAttribute;
import org.jcvi.ometa.validation.ModelValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 5:46 PM
 * <p/>
 * Data access operations for project attribute meta data (defining proper project attributes) are done here.
 */
public class ProjectMetaAttributeDAO extends HibernateDAO {

    /**
     * Use session to write the meta-attribute.
     */
    public void write(ProjectMetaAttribute model, Date transactionDate, Session session) throws DAOException {
        try {
            prepareForWriteback(model, null, transactionDate, session);
            session.saveOrUpdate(model);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    public void update(ProjectMetaAttribute model, Date transactionDate, Session session) throws DAOException {
        try {
            prepareForWriteback(model, null, transactionDate, session);
            session.merge(model);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    public ProjectMetaAttribute getProjectMetaAttribute(Long lookupValueId, Long projectId, Session session)
            throws DAOException {
        return getMetaAttribute(lookupValueId, projectId, session, ProjectMetaAttribute.class);
    }

    /**
     * get all meta-attributes associated with project.
     */
    public List<ProjectMetaAttribute> readAll(Long projectId, Session session) throws DAOException {
        List<ProjectMetaAttribute> attributeList = new ArrayList<ProjectMetaAttribute>();
        try {
            Criteria crit = session.createCriteria(ProjectMetaAttribute.class);
            crit.add(Restrictions.eq("projectId", projectId));
            List<ProjectMetaAttribute> results = crit.list();
            attributeList.addAll(results);
            logger.debug("Got " + results.size() + " meta attributes for project " + projectId);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    /**
     * get all meta-attributes associated with project.
     */
    public List<ProjectMetaAttribute> readAll(List<Long> projectIds, Session session) throws DAOException {
        List<ProjectMetaAttribute> attributeList = new ArrayList<ProjectMetaAttribute>();
        try {
            if (projectIds.size() > 0) {
                Criteria crit = session.createCriteria(ProjectMetaAttribute.class);
                crit.add(Restrictions.in("projectId", projectIds));
                List<ProjectMetaAttribute> results = crit.list();
                attributeList.addAll(results);
                logger.debug(
                        "Got " + results.size() + " meta attributes for project list of size " + projectIds.size());
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    /**
     * get all unique meta-attributes
     */
    public List<ProjectMetaAttribute> readAllUnique(Session session) throws DAOException {
        List<ProjectMetaAttribute> attributeList = new ArrayList<ProjectMetaAttribute>();
        try {
            String sql =
                    " select PMA.* from project_meta_attribute PMA, " +
                            " (select projma_id, max(projma_create_date) " +
                            "     from project_meta_attribute group by projma_lkuvlu_attribute_id) PMAU " +
                            " where PMA.projma_id = PMAU.projma_id ";
            SQLQuery query = session.createSQLQuery(sql);
            query.addEntity("PMA", ProjectMetaAttribute.class);

            attributeList = query.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    private void prepareForWriteback(ProjectMetaAttribute model, String actorName, Date transactionDate, Session session) throws DAOException {
        handleProjectRelation(model, model.getProjectName(), model.getAttributeName(), session);
        handleCreationTracking(model, actorName, transactionDate, session);
        locateAttribNameLookupId(model, session, ModelValidator.ATTRIBUTE_LV_TYPE_NAME);
    }

}
