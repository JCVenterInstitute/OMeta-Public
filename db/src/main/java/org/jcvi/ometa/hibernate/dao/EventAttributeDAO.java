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

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.EventAttribute;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.ModelBean;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

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
        List<EventAttribute> eaList;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<EventAttribute> eaCriteriaQuery = builder.createQuery(EventAttribute.class);
            Root<EventAttribute> eventAttributeRoot = eaCriteriaQuery.from(EventAttribute.class);

            eaCriteriaQuery.select(eventAttributeRoot)
                    .where(builder.equal(eventAttributeRoot.get("eventId"), eventId));

            List<EventAttribute> results = session.createQuery(eaCriteriaQuery).getResultList();
            List<Long> nameLookupIds;
            if ( results != null && results.size() > 0 ) {
                nameLookupIds = results.stream()
                        .map(EventAttribute::getNameLookupValueId)
                        .collect(Collectors.toList());

                CriteriaQuery<EventMetaAttribute> emaCriteriaQuery = builder.createQuery(EventMetaAttribute.class);
                Root<EventMetaAttribute> emaRoot = emaCriteriaQuery.from(EventMetaAttribute.class);

                emaCriteriaQuery.select(emaRoot)
                        .where(builder.and(
                                builder.equal(emaRoot.get("projectId"), projectId),
                                emaRoot.get("nameLookupId").in(nameLookupIds)
                        ));

                List<EventMetaAttribute> metaResults = session.createQuery(emaCriteriaQuery).getResultList();
                if ( metaResults != null ) {
                    Map<Long,EventMetaAttribute> nameIdVsMetaAttribute = new HashMap<>(metaResults.size());
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
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eaList;
    }

    public List<EventAttribute> getEventAttributes( List<Long> eventIds, Long projectId, Session session ) throws DAOException {
        List<EventAttribute> eaList = new ArrayList<>(eventIds.size());
        try {
            if ( eventIds.size() > 0 ) {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<EventAttribute> eaCriteriaQuery = builder.createQuery(EventAttribute.class);
                Root<EventAttribute> eventAttributeRoot = eaCriteriaQuery.from(EventAttribute.class);

                eaCriteriaQuery.select(eventAttributeRoot)
                        .where(eventAttributeRoot.get("eventId").in(eventIds));

                List<EventAttribute> results = session.createQuery(eaCriteriaQuery).getResultList();

                CriteriaQuery<Event> eventCriteriaQuery = builder.createQuery(Event.class);
                Root<Event> eventRoot = eventCriteriaQuery.from(Event.class);

                eventCriteriaQuery.select(eventRoot)
                        .where(eventRoot.get("eventId").in(eventIds));

                List<Event> events = session.createQuery(eventCriteriaQuery).getResultList();
                Map<Long, Long> eventVsEventType = new HashMap<>(events.size());
                for(Event event : events) {
                    if(!eventVsEventType.containsKey(event.getEventId())) {
                        eventVsEventType.put(event.getEventId(), event.getEventType());
                    }
                }

                if(results != null && results.size() > 0) {
                    List<Long> nameLookupIds = results.stream()
                            .map(EventAttribute::getNameLookupValueId)
                            .collect(Collectors.toList());

                    CriteriaQuery<EventMetaAttribute> emaCriteriaQuery = builder.createQuery(EventMetaAttribute.class);
                    Root<EventMetaAttribute> emaRoot = emaCriteriaQuery.from(EventMetaAttribute.class);

                    emaCriteriaQuery.select(emaRoot)
                            .where(builder.and(
                                    builder.equal(emaRoot.get("projectId"), projectId),
                                    emaRoot.get("nameLookupId").in(nameLookupIds)
                            ));

                    List<EventMetaAttribute> metaResults = session.createQuery(emaCriteriaQuery).getResultList();

                    if(metaResults != null) {
                        Map<Long, Map<Long, EventMetaAttribute>> eventTypeToNameMap = new HashMap<>(metaResults.size());
                        for(EventMetaAttribute ema: metaResults) {
                            if(eventTypeToNameMap.containsKey(ema.getEventTypeLookupId())) {
                                eventTypeToNameMap.get(ema.getEventTypeLookupId()).put(ema.getNameLookupId(), ema);
                            } else {
                                Map<Long, EventMetaAttribute> nameVsEma = new TreeMap<>();
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
