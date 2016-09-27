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
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.EventAttribute;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.ModelBean;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 4:37 PM
 *
 * Database interchange for Event Meta Attributes - the information framing up what CAN be
 * given as an attribute.
 */
public class EventAttributeDAO extends HibernateDAO {

    /** Writeback model with standardized date, for whole transaction. */
    public void write( EventAttribute model, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareForWriteback( model, null, transactionDate, session );
            session.saveOrUpdate( model );

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    /** Keep this here as where-to-add other pre-write mods. */
    private void prepareForWriteback( ModelBean model, String actorName, Date transactionDate, Session session )
            throws Exception {
        handleCreationTracking( model, actorName, transactionDate, session );
    }

    public List<EventAttribute> getEventAttributes( Long eventId, Long projectId, Session session ) throws DAOException {
        List<EventAttribute> eaList = null;
        try {
            Criteria crit = session.createCriteria( EventAttribute.class );
            crit.add( Restrictions.eq("eventId", eventId) );
            List<EventAttribute> results = crit.list();
            List<Long> nameLookupIds = new ArrayList<Long>();
            if ( results != null && results.size() > 0 ) {
                for ( EventAttribute ea: results ) {
                    nameLookupIds.add( ea.getNameLookupValueId() );
                }
                crit = session.createCriteria(EventMetaAttribute.class);
                crit.add(Restrictions.in( "nameLookupId", nameLookupIds ) )
                        .add(Restrictions.eq( "projectId", projectId ) );

                List<EventMetaAttribute> metaResults = crit.list();
                if ( metaResults != null ) {
                    Map<Long,EventMetaAttribute> nameIdVsMetaAttribute = new HashMap<Long,EventMetaAttribute>();
                    for ( EventMetaAttribute ema: metaResults ) {
                        nameIdVsMetaAttribute.put( ema.getNameLookupId(), ema );
                    }
                    for ( EventAttribute ea: results ) {
                        EventMetaAttribute ema = nameIdVsMetaAttribute.get( ea.getNameLookupValueId() );
                        if ( ema != null )
                            ea.setMetaAttribute( ema );
                    }

                }
                eaList = results;
            }
            else {
                eaList = Collections.EMPTY_LIST;
            }

            /*Query query = session.createSQLQuery( "select ea.*, ema.*, lv1.*, lv2.* from " +
                    " from event_attribute as ea " +
                    " join event_meta_attribute as ema on ea.eventa_lkuvlu_attribute_id=ema.evenma_lkuvlu_attribute_id " +
                    " join lookup_value as lv1 on ema.eventTypeLookupId=lv1.lkuvlu_id " +
                    " join lookup_value as lv2 on ema.evenma_event_type_lkuvl_id=lv2.lkuvlu_id " +
                    " where ea.eventa_event_id = :eventId and ema.evenma_projet_id = :projectId "
            ).addEntity(EventAttribute.class);
            */

            /*Query hql = session.createQuery( "from EventAttribute ea join ea.metaAttribute ema with ema.projectId=:projectId " +
                    " join ema.lookupValue nlv join ema.eventTypeLookupValue tlv " +
                    " where ea.eventId=:eventId "
            );
            hql.setLong("eventId", eventId);
            hql.setLong("projectId", projectId);

            //eaList = (List<EventAttribute>)hql.list();

            List results = hql.list();
            if ( results != null ) {
                for ( Object result: results ) {
                    eaList.add( (EventAttribute) result);
                }
            }*/

        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eaList;
    }

    public List<EventAttribute> getEventAttributes( List<Long> eventIds, Long projectId, Session session ) throws DAOException {
        List<EventAttribute> eaList = new ArrayList<EventAttribute>();
        try {
            if ( eventIds.size() > 0 ) {

                Criteria crit = session.createCriteria( EventAttribute.class );
                crit.add( Restrictions.in("eventId", eventIds) );
                List<EventAttribute> results = crit.list();

                crit = session.createCriteria(Event.class);
                crit.add(Restrictions.in("eventId", eventIds));
                List<Event> events = crit.list();
                Map<Long, Long> eventVsEventType = new HashMap<Long, Long>();
                for(Event event : events) {
                    if(!eventVsEventType.containsKey(event.getEventId())) {
                        eventVsEventType.put(event.getEventId(), event.getEventType());
                    }
                }

                if(results != null && results.size() > 0) {

                    List<Long> nameLookupIds = new ArrayList<Long>();
                    for ( EventAttribute ea: results ) {
                        nameLookupIds.add( ea.getNameLookupValueId() );
                    }
                    crit = session.createCriteria(EventMetaAttribute.class);
                    crit.add(Restrictions.in("nameLookupId", nameLookupIds))
                            .add(Restrictions.eq("projectId", projectId));
                    List<EventMetaAttribute> metaResults = crit.list();

                    if(metaResults != null) {
                        Map<Long, Map<Long, EventMetaAttribute>> eventTypeToNameMap = new HashMap<Long, Map<Long, EventMetaAttribute>>();
                        for(EventMetaAttribute ema: metaResults) {
                            if(eventTypeToNameMap.containsKey(ema.getEventTypeLookupId())) {
                                eventTypeToNameMap.get(ema.getEventTypeLookupId()).put(ema.getNameLookupId(), ema);
                            } else {
                                Map<Long, EventMetaAttribute> nameVsEma = new TreeMap<Long, EventMetaAttribute>();
                                nameVsEma.put(ema.getNameLookupId(), ema);
                                eventTypeToNameMap.put(ema.getEventTypeLookupId(), nameVsEma);
                            }
                        }

                        for(EventAttribute ea: results) {
                            Long type = eventVsEventType.get(ea.getEventId());
                            Map<Long, EventMetaAttribute> name = type == null ? null : eventTypeToNameMap.get(type);
                            EventMetaAttribute ema = name == null ? null : name.get(ea.getNameLookupValueId());
                            if(ema != null) {
                                ea.setMetaAttribute(ema);
                            }
                        }

                    }
                    eaList.addAll( results );
                }
                else {
                    eaList = Collections.EMPTY_LIST;
                }

            }


        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eaList;
    }

}
