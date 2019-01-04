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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.utils.Constants;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 4:37 PM
 *
 * Database interchange for Event Meta Attributes - the information framing up what CAN be
 * given as an attribute.
 */
public class EventMetaAttributeDAO extends HibernateDAO {

    public void write( EventMetaAttribute model, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareForWriteback( model, null, transactionDate, session );
            session.saveOrUpdate( model );
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    public void update( EventMetaAttribute model, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareForWriteback( model, null, transactionDate, session );
            session.merge( model );
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    /** Find event MA by name, for event type, and for project. */
    public EventMetaAttribute getEventMetaAttribute(
            Long lookupValueId, Long projectId, Long eventTypeLookupId, Session session) throws DAOException {

        EventMetaAttribute metaAttribute = null;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<EventMetaAttribute> criteriaQuery = builder.createQuery(EventMetaAttribute.class);
            Root<EventMetaAttribute> emaRoot = criteriaQuery.from(EventMetaAttribute.class);

            criteriaQuery.select(emaRoot)
                    .where(builder.and(
                            builder.equal(emaRoot.get("nameLookupId"), lookupValueId),
                            builder.equal(emaRoot.get("projectId"), projectId),
                            builder.equal(emaRoot.get("eventTypeLookupId"), eventTypeLookupId)
                    ));

            List results = session.createQuery(criteriaQuery).getResultList();
            Date latestDate = null;
            for ( Object nextResult: results ) {
                EventMetaAttribute nextMetaAttribute = (EventMetaAttribute)nextResult;
                if ( latestDate == null  ||  nextMetaAttribute.getCreationDate().after( latestDate ) ) {
                    metaAttribute = nextMetaAttribute;
                }
            }

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
        return metaAttribute;
    }

    /** Find all the required event attributes (by meta attribute) for the event type/project combination. */
    public List<EventMetaAttribute> readAll( Long projectId, Long eventTypeLookupId, Session session )
            throws DAOException {
        List<EventMetaAttribute> attributeList = new ArrayList<>();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<EventMetaAttribute> criteriaQuery = builder.createQuery(EventMetaAttribute.class);
            Root<EventMetaAttribute> emaRoot = criteriaQuery.from(EventMetaAttribute.class);

            List<Predicate> predicates = new ArrayList<>(2);
            predicates.add(builder.equal(emaRoot.get("projectId"), projectId));
            if(eventTypeLookupId != null)
                predicates.add(builder.equal(emaRoot.get("eventTypeLookupId"), eventTypeLookupId ));

            criteriaQuery.select(emaRoot)
                    .where(predicates.toArray(new Predicate[]{}))
                    .orderBy(builder.asc(emaRoot.get("order"))); //order by position value

            List results = session.createQuery(criteriaQuery).getResultList();

            if ( results != null ) {
                for ( Object result: results ) {
                    EventMetaAttribute attribute = (EventMetaAttribute) result;
                    expandLookupValueId( attribute, session );
                    attributeList.add( attribute );
                }
                logger.debug( "Got " + results.size() + " event meta attributes for project " + projectId );
            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;

    }

    /** Find all the required event attributes (by meta attribute) for each event type/project combination. */
    public List<EventMetaAttribute> readAll( List<Long> projectIds, Long eventTypeLookupId, Session session )
            throws DAOException {
        List<EventMetaAttribute> attributeList = new ArrayList<>();
        try {
            if(projectIds.size() > 0) {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<EventMetaAttribute> criteriaQuery = builder.createQuery(EventMetaAttribute.class);
                Root<EventMetaAttribute> emaRoot = criteriaQuery.from(EventMetaAttribute.class);

                List<Predicate> predicates = new ArrayList<>(2);
                predicates.add(emaRoot.get("projectId").in(projectIds));
                if(eventTypeLookupId != null)
                    predicates.add(builder.equal(emaRoot.get("eventTypeLookupId"), eventTypeLookupId ));

                criteriaQuery.select(emaRoot)
                        .where(predicates.toArray(new Predicate[]{}))
                        .orderBy(builder.asc(emaRoot.get("order"))); //order by position value

                List<EventMetaAttribute> results = session.createQuery(criteriaQuery).getResultList();

                if ( results != null   &&   results.size() > 0 ) {
                    expandLookupValueIds( results, session );
                    logger.debug("Got " + results.size() + " event meta attributes for project list of size " + projectIds.size());

                    attributeList.addAll( results );
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;

    }

    /** get all unique meta-attributes */
    public List<EventMetaAttribute> readAllUnique( Session session ) throws DAOException {
        List<EventMetaAttribute> attributeList;
        try {
            String sql = " select EMA.*,LV.lkuvlu_name " +
                    " from event_meta_attribute EMA, " +
                    " (select evenma_id, max(evenma_create_date) from event_meta_attribute group by evenma_lkuvlu_attribute_id) EMAU, " +
                    " lookup_value LV " +
                    " where EMA.evenma_id = EMAU.evenma_id and EMA.evenma_lkuvlu_attribute_id=LV.lkuvlu_id " +
                    " order by LV.lkuvlu_name ";
            NativeQuery query = session.createNativeQuery( sql );
            query.addEntity( "EMA", EventMetaAttribute.class );

            attributeList = query.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    private void prepareForWriteback(EventMetaAttribute model, String actorName, Date transactionDate, Session session ) throws Exception {
        handleProjectRelation( model, model.getProjectName(), model.getAttributeName(), session );
        handleCreationTracking( model, actorName, transactionDate, session );
        locateAttribNameLookupId( model, session, Constants.ATTRIBUTE_LV_TYPE_NAME);
    }

}
