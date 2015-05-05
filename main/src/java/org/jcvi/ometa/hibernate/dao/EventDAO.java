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

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.EventAttribute;
import org.jcvi.ometa.model.LookupValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 3:07 PM
 *
 * Data Access Object for encapsulating database access to project table.
 */
public class EventDAO extends HibernateDAO {

    private static final String RETURN_VAL_PARAM = "attributeName";
    private static final String EVENT_NAME_PARAM = "eventName";
    private static final String PROJECT_NAME_PARAM = "projectName";
    private static final String SAMPLE_REQUIRED_QUERY =
            "select LA.lkuvlu_name AS " + RETURN_VAL_PARAM + "\n" +
                    "from event_meta_attribute EMA, project P, lookup_value LE, lookup_value LA,\n" +
                    "     sample_meta_attribute SMA\n" +
                    "where LE.lkuvlu_name = :" + EVENT_NAME_PARAM +
                    "  and P.projet_name = :" + PROJECT_NAME_PARAM + "\n" +
                    "  and EMA.evenma_event_type_lkuvl_id=LE.lkuvlu_id\n" +
                    "  and P.projet_id=EMA.evenma_projet_id\n" +
                    "  and P.projet_id=SMA.sampma_projet_id\n" +
                    "  and SMA.sampma_lkuvlu_attribute_id=EMA.evenma_lkuvlu_attribute_id\n" +
                    "  and LA.lkuvlu_id=SMA.sampma_lkuvlu_attribute_id";

    /**
     * Given a event model, write its relevant data to the database.
     *
     * @throws org.jcvi.ometa.hibernate.dao.DAOException thrown if state of database not as required.
     */
    public Event write(
            Event event, String actorName, String eventName, Date transactionDate, Session session)
            throws DAOException {
        try {
            prepareEventForWriteback(event, actorName, eventName, transactionDate, session);
            session.saveOrUpdate(event);

        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return event; // Fully-populated.
    }

    /**
     * Finds any missing information from the event object, and fills it in so that DB
     * writeback will be complete.
     *
     * @param event may or may not have all its data.
     * @throws Exception by called methods.
     */
    private void prepareEventForWriteback(
            Event event, String actorName, String eventName, Date transactionDate, Session session)
            throws Exception {

        handleEventName(event, eventName, session);
        handleEventStatus(event, session);
        handleCreationTracking(event, actorName, transactionDate, session);
        handleProjectRelation(event, event.getProjectName(), eventName, session);
        if (event.getSampleName() != null   &&   event.getSampleName().length() > 0   &&  event.getSampleId() != null)
            handleSampleRelation(event, event.getSampleName(), eventName, session);

    }

    public void updateEventStatus(Event event, Date transactionDate, Session session) throws Exception {
        try {
            LookupValueDAO lvDAO = new LookupValueDAO();
            LookupValue lv = lvDAO.getEventStatusLookupValue(true, session);

            if(event.getEventStatus().compareTo(lv.getLookupValueId()) == 0)
                event.setEventStatus(lvDAO.getEventStatusLookupValue(false, session).getLookupValueId());
            else
                event.setEventStatus(lv.getLookupValueId());

            if (event.getModifiedDate() == null) {
                event.setModifiedDate(transactionDate);
            }

            session.merge(event);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    /** Fills in the event type using the event name, if that is needed.  */
    private void handleEventName(Event event, String eventName, Session session) throws Exception {
        Long eventType = event.getEventType();
        if (eventType == null  ||  eventType == 0) {
            LookupValueDAO lvDAO = new LookupValueDAO();
            LookupValue lv = lvDAO.getLookupValue(eventName, session);
            if (lv == null) {
                throw new Exception("No such event type: " + eventName);
            }
            else {
                event.setEventType(lv.getLookupValueId());
            }
        }
    }

    /** Fills in the status of the event.  Should be active on creation. */
    private void handleEventStatus(Event event, Session session) throws Exception {
        LookupValueDAO lvDAO = new LookupValueDAO();
        LookupValue lv = lvDAO.getEventStatusLookupValue(true, session);
        if (lv == null) {
            throw new IllegalArgumentException("No event status found for event status of active.");
        }
        event.setEventStatus(lv.getLookupValueId());
    }

    public List<Event> getAllEvents(
            Long flexId, String identifier, String sSearch,
            String sortCol, String sortDir, int start, int count,
            String fromd, String tod, List<String> columnName, List<String> columnSearchArguments, Session session) throws DAOException {
        List<Event> eventList = new ArrayList<Event>();
        try {
            List results = null;

            String sql = " select E.*, S.sample_name sample, CONCAT(A.actor_last_name,',',A.actor_first_name) user, LV.lkuvlu_name eventType  " +
                    " from event E " +
                    "   left join sample S on E.event_sampl_id=S.sample_id " +
                    "   left join lookup_value LV on E.event_type_lkuvl_id=LV.lkuvlu_id " +
                    "   left join actor A on E.event_actor_created_by=A.actor_id where ";

            if("Sample".equals(identifier))
                sql += "E.event_sampl_id=";
            else if("Eventlist".equals(identifier))
                sql += "E.event_projet_id=";
            else if("Event".equals(identifier))
                sql += "E.event_id=";
            else
                sql += "E.event_sampl_id is null and E.event_projet_id=";
            sql+=flexId;

            if(sSearch!=null && !sSearch.isEmpty()) {
                sSearch = "%"+sSearch+"%";
                sql+=" and (LOWER(LV.lkuvlu_name) like '"+sSearch+"' or LOWER(S.sample_name) like '"+sSearch+"' " +
                        " or ((LOWER(A.actor_first_name) like '"+sSearch+"' or LOWER(A.actor_last_name) like '"+sSearch+"')))";
            }

            if(columnName!=null && !columnName.isEmpty()){
                String columnSearchSql = " #logicGate# (E.event_id in (select EA.eventa_event_id from event_attribute EA, lookup_value LV1 where EA.eventa_lkuvlu_attribute_id = LV1.lkuvlu_id and " +
                        "LV1.lkuvlu_name = '#columnName#' and COALESCE(EA.eventa_attribute_date,LOWER(EA.eventa_attribute_float),LOWER(EA.eventa_attribute_str),LOWER(EA.eventa_attribute_int)) #columnSearch#))";
                sql += " and (";

                for(int i = 0; i<columnName.size(); i++) {
                    String key = columnName.get(i);
                    String[] valueArr = columnSearchArguments.get(i).split(";");
                    String searchVal = valueArr[0];
                    String operation = valueArr[1];
                    String logicGate = i == 0 ? "" : valueArr[2].equals("not") ? "and not" : valueArr[2];

                    if (key.equals("Sample Name")) {
                        sql += operation.equals("like") ? " " + logicGate + " LOWER(S.sample_name) like '%" + searchVal + "%' "
                                : operation.equals("in") ? " " + logicGate + " LOWER(S.sample_name) in ('" + searchVal.replaceAll(",", "','") + "') "
                                : " " + logicGate + " LOWER(S.sample_name) = '" + searchVal + "' ";
                    } else if (key.equals("Event Type")) {
                        sql += operation.equals("like") ? " " + logicGate + " LOWER(LV.lkuvlu_name) like '%" + searchVal + "%' "
                                : operation.equals("in") ? " " + logicGate + " LOWER(LV.lkuvlu_name) in ('" + searchVal.replaceAll(",", "','") + "') "
                                : " " + logicGate + " LOWER(LV.lkuvlu_name) = '" + searchVal + "' ";
                    } else if (key.equals("User")) {
                        sql += operation.equals("like") ? " " + logicGate + " (LOWER(A.actor_first_name) like '%" + searchVal + "%' or LOWER(A.actor_last_name) like '%" + searchVal + "%') "
                                : operation.equals("in") ? " " + logicGate + " (LOWER(A.actor_first_name) in ('" + searchVal.replaceAll(",", "','") + "') or LOWER(A.actor_last_name) in ('" + searchVal.replaceAll(",", "','") + "')) "
                                : " " + logicGate + " (LOWER(A.actor_first_name) = '" + searchVal + "' or LOWER(A.actor_last_name) = '" + searchVal + "') ";
                    } else if (key.equals("Date")) {
                        sql += operation.equals("like") ? " " + logicGate + " E.event_create_date like '%" + searchVal + "%' "
                                : operation.equals("in") ? " " + logicGate + " E.event_create_date in ('" + searchVal.replaceAll(",", "','") + "') "
                                : operation.equals("equals") ? " " + logicGate + " E.event_create_date = '" + searchVal + "' "
                                : " " + logicGate + " E.event_create_date " + (operation.equals("less")?"<":">") + " '" + searchVal + "' ";
                    }  else {
                        sql += operation.equals("like") ? columnSearchSql.replace("#logicGate#", logicGate).replace("#columnName#", key).replace("#columnSearch#", "like '%" + searchVal + "%'")
                                : operation.equals("in") ? columnSearchSql.replace("#logicGate#", logicGate).replace("#columnName#", key).replace("#columnSearch#", "in ('" + searchVal.replaceAll(",", "','") + "')")
                                : operation.equals("equals") ? columnSearchSql.replace("#logicGate#", logicGate).replace("#columnName#", key).replace("#columnSearch#", "= '" + searchVal + "'")
                                : columnSearchSql.replace("#logicGate#", logicGate).replace("#columnName#", key).replace("#columnSearch#", (operation.equals("less")?"<":">") + " '" + searchVal + "'");
                    }
                }

                sql += ")";
            }

            if(fromd!=null && !fromd.isEmpty())
                sql+=" and date(E.event_create_date)>='"+fromd+"'";
            if(tod!=null && !tod.isEmpty())
                sql+=" and date(E.event_create_date)<='"+tod+"'";

            if(sortCol!=null && !sortCol.isEmpty() && sortDir!=null && !sortDir.isEmpty()) {
                sql += " order by";
                if(sortCol.equals("event"))
                    sql += " eventType ";
                else if(sortCol.equals("user"))
                    sql += " user ";
                else if(sortCol.equals("sample"))
                    sql += " sample ";
                else if(sortCol.equals("date"))
                    sql += " event_create_date ";
                sql += sortDir;
            }

            SQLQuery query = session.createSQLQuery(sql);
            query.addEntity("E", Event.class);
            if(start>=0 && count>=0) {
                query.setFirstResult(start);
                query.setMaxResults(count);
            }
            results = query.list();

            if (results != null) {
                for (Object result: results) {
                    eventList.add((Event) result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eventList;
    }

    public List<Event> getAllEvents(List<Long> flexIds, String identifier, Session session) throws DAOException {
        List<Event> eventList = new ArrayList<Event>();
        if (flexIds == null  ||  flexIds.size() == 0)
            return eventList;

        try {
            Criteria crit = session.createCriteria(Event.class);
            if("Sample".equals(identifier))
                crit.add(Restrictions.in("sampleId", flexIds));
            else
                crit.add(Restrictions.and(
                        Restrictions.in("projectId", flexIds) ,
                        Restrictions.isNull("sampleId")
                ));

            List results = crit.list();

            if (results != null) {
                for (Object result: results) {
                    eventList.add((Event) result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eventList;
    }

    public List<Event> getEventByType( Long projectId, Long eventTypeId, Session session) throws DAOException {
        List<Event> eventList = new ArrayList<Event>();

        try {
            Criteria crit = session.createCriteria(Event.class);
            crit.add(Restrictions.and(
                    Restrictions.eq("projectId", projectId),
                    Restrictions.eq("eventType", eventTypeId)
            ));

            List results = crit.list();

            if (results != null) {
                for (Object result: results) {
                    eventList.add((Event) result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eventList;
    }

    public List<Event> getEventByTypeAndSample(Long sampleId, Long eventTypeId, Session session) throws DAOException {
        List<Event> eventList = new ArrayList<Event>();

        try {
            Criteria crit = session.createCriteria(Event.class);
            crit.add(Restrictions.and(
                    Restrictions.eq("sampleId", sampleId),
                    Restrictions.eq("eventType", eventTypeId)
            ));

            List results = crit.list();

            if (results != null) {
                for (Object result: results) {
                    eventList.add((Event) result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eventList;
    }

    public List<Event> getEventByLookupValue(Long lookupValueId, String lookupValueStr, Session session) throws DAOException {
        List<Event> eventList;

        try {
            String sql = " select E.* from event E" +
                    " left join event_attribute EA on E.event_id = EA.eventa_event_id" +
                    " where EA.eventa_lkuvlu_attribute_id = :lookupValueId " +
                    " and EA.eventa_attribute_str = :lookupValueStr ";

            SQLQuery query = session.createSQLQuery(sql);
            query.addEntity("E", Event.class);
            query.setLong("lookupValueId", lookupValueId);
            query.setString("lookupValueStr", lookupValueStr);


            eventList = query.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eventList;

    }

    public Event getLatestEventForSample(Long projectId, Long sampleId, Long eventTypeId, Session session) throws DAOException {
        Event latestEvent = null;

        try {
            String where = "where eventType = :eventTypeId and projectId = :projectId";
            if(sampleId != null) {
                where += " and sampleId = :sampleId";
            }
            Query query = session.createQuery("from Event " + where + " order by creationDate DESC");
            query.setLong("projectId", projectId);
            if(sampleId != null) {
                query.setLong("sampleId", sampleId);
            }
            query.setLong("eventTypeId", eventTypeId);
            query.setMaxResults(1);
            latestEvent = (Event)query.uniqueResult();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return latestEvent;
    }

    public List<Event> getUniqueEventTypes(Session session) throws DAOException {
        List<Event> eventList;

        try {
            String sql = " select E.* from event E, lookup_value LV " +
                    " where E.event_type_lkuvl_id=LV.lkuvlu_id " +
                    " group by E.event_type_lkuvl_id order by LV.lkuvlu_name asc ";
            SQLQuery query = session.createSQLQuery(sql);
            query.addEntity("E", Event.class);

            eventList = query.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return eventList;
    }

    /** Tells whether a sample name is needed, for event-oriented operations. */
    public Boolean isSampleRequired(String projectName, String eventName, Session session) throws DAOException {
        Boolean rtnVal = true;      // Burdened until told otherwise.
        try {
            List<String> attributeNames = getSampleMetaAttributeNames(projectName, eventName, session);
            if (attributeNames == null  ||  attributeNames.size() == 0) {
                rtnVal = false;
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return rtnVal;
    }

    /** Broad method for simply getting a list of meta attrib names.  Used above as T/F feed. */
    private List<String> getSampleMetaAttributeNames(String projectName, String eventName, Session session) {
        SQLQuery query = session.createSQLQuery(SAMPLE_REQUIRED_QUERY);
        query.setParameter(PROJECT_NAME_PARAM, projectName);
        query.setParameter(EVENT_NAME_PARAM, eventName);
        query.addScalar(RETURN_VAL_PARAM, Hibernate.STRING);
        if (logger.isDebugEnabled()) {
            logger.debug("Query is " + query.toString());
        }
        return query.list();
    }

}
