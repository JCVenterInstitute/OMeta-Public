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

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.jcvi.ometa.model.SampleMetaAttribute;
import org.jcvi.ometa.utils.Constants;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 4:37 PM
 * <p/>
 * Database interchange for Event Meta Attributes - the information framing up what CAN be
 * given as an attribute.
 */
public class SampleMetaAttributeDAO extends HibernateDAO {

    public void write(SampleMetaAttribute model, Date transactionDate, Session session) throws DAOException {
        try {
            prepareForWriteback(model, null, transactionDate, session);
            session.saveOrUpdate(model);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

    }

    public void update(SampleMetaAttribute model, Date transactionDate, Session session) throws DAOException {
        try {
            prepareForWriteback(model, null, transactionDate, session);
            session.merge(model);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

    }

    /**
     * get all meta-attributes associated with project.
     */
    public List<SampleMetaAttribute> readAll(Long projectId, Session session)
            throws DAOException {
        List<SampleMetaAttribute> attributeList = new ArrayList<>();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<SampleMetaAttribute> criteriaQuery = builder.createQuery(SampleMetaAttribute.class);
            Root<SampleMetaAttribute> smaRoot = criteriaQuery.from(SampleMetaAttribute.class);

            criteriaQuery.select(smaRoot)
                    .where(builder.equal(smaRoot.get("projectId"), projectId));

            List<SampleMetaAttribute> results = session.createQuery(criteriaQuery).getResultList();

            if (results != null) {
                for (SampleMetaAttribute result : results) {
                    SampleMetaAttribute attribute = result;
                    expandLookupValueId(attribute, session);
                    attributeList.add(attribute);
                }
                logger.debug("Got " + results.size() + " sample meta attributes for project " + projectId);
            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    /**
     * get all meta-attributes associated with all projects given.
     */
    public List<SampleMetaAttribute> readAll(List<Long> projectIds, Session session)
            throws DAOException {
        List<SampleMetaAttribute> attributeList = new ArrayList<>();
        try {
            if (projectIds.size() > 0) {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<SampleMetaAttribute> criteriaQuery = builder.createQuery(SampleMetaAttribute.class);
                Root<SampleMetaAttribute> smaRoot = criteriaQuery.from(SampleMetaAttribute.class);

                criteriaQuery.select(smaRoot)
                        .where(smaRoot.get("projectId").in(projectIds));

                List<SampleMetaAttribute> results = session.createQuery(criteriaQuery).getResultList();
                if (results != null) {
                    expandLookupValueIds(results, session);
                    logger.debug(
                            "Got " + results.size() + " sample meta attributes for project list of size " +
                                    projectIds.size());

                    attributeList.addAll(results);
                }
            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    public SampleMetaAttribute getSampleMetaAttribute(
            Long lookupValueId, Long projectId, Session session)
            throws DAOException {
        return getMetaAttribute(lookupValueId, projectId, session, SampleMetaAttribute.class);
    }

    /**
     * get all unique meta-attributes
     */
    public List<SampleMetaAttribute> readAllUnique(Session session) throws DAOException {
        List<SampleMetaAttribute> attributeList;
        try {
            String sql =
                    " select SMA.* from sample_meta_attribute SMA, " +
                            "  (select sampma_id, max(sampma_create_date) " +
                            "  from sample_meta_attribute group by sampma_lkuvlu_attribute_id) SMAU " +
                            "  where SMA.sampma_id = SMAU.sampma_id ";
            NativeQuery query = session.createNativeQuery(sql);
            query.addEntity("SMA", SampleMetaAttribute.class);

            attributeList = query.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    private void prepareForWriteback(SampleMetaAttribute model, String actorName, Date transactionDate, Session session) throws DAOException {
        handleProjectRelation(model, model.getProjectName(), model.getAttributeName(), session);
        handleCreationTracking(model, actorName, transactionDate, session);
        locateAttribNameLookupId(model, session, Constants.ATTRIBUTE_LV_TYPE_NAME);
    }

}
