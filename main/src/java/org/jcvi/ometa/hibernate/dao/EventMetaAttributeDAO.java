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
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.validation.ModelValidator;

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
            Criteria crit = session.createCriteria( EventMetaAttribute.class );
            crit.add( Restrictions.eq("nameLookupId", lookupValueId) );
            crit.add( Restrictions.eq("projectId", projectId) );
            crit.add( Restrictions.eq("eventTypeLookupId", eventTypeLookupId) );

            List results = crit.list();
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
        List<EventMetaAttribute> attributeList = new ArrayList<EventMetaAttribute>();
        try {
            Criteria crit = session.createCriteria( EventMetaAttribute.class );
            crit.add( Restrictions.eq( "projectId", projectId ) );
            if(eventTypeLookupId != null)
                crit.add( Restrictions.eq( "eventTypeLookupId", eventTypeLookupId ) );
            List results = crit.list();

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
        List<EventMetaAttribute> attributeList = new ArrayList<EventMetaAttribute>();
        try {
            if ( projectIds.size() > 0 ) {
                Criteria crit = session.createCriteria( EventMetaAttribute.class );
                crit.add( Restrictions.in("projectId", projectIds) );
                if(eventTypeLookupId != null)
                    crit.add( Restrictions.eq( "eventTypeLookupId", eventTypeLookupId ) );
                List<EventMetaAttribute> results = crit.list();

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
        List<EventMetaAttribute> attributeList = new ArrayList<EventMetaAttribute>();
        try {
            String sql = " select EMA.*,LV.lkuvlu_name " +
                    " from event_meta_attribute EMA, " +
                    " (select evenma_id, max(evenma_create_date) from event_meta_attribute group by evenma_lkuvlu_attribute_id) EMAU, " +
                    " lookup_value LV " +
                    " where EMA.evenma_id = EMAU.evenma_id and EMA.evenma_lkuvlu_attribute_id=LV.lkuvlu_id " +
                    " order by LV.lkuvlu_name ";
            SQLQuery query = session.createSQLQuery( sql );
            query.addEntity( "EMA", EventMetaAttribute.class );

            attributeList = query.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    private void prepareForWriteback(EventMetaAttribute model, String actorName, Date transactionDate, Session session ) throws Exception {
        /*
         * Only apply active flag to newly added meta attribute
         * by hkim 9/7/12
         */
        if (model.getCreationDate() == null)
            model.setActive(true);
        handleProjectRelation( model, model.getProjectName(), model.getAttributeName(), session );
        handleCreationTracking( model, actorName, transactionDate, session );
        locateAttribNameLookupId( model, session, ModelValidator.ATTRIBUTE_LV_TYPE_NAME);
    }

}
