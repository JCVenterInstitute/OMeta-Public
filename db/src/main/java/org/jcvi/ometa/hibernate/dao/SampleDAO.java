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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.utils.Constants;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 3:07 PM
 *
 * Data Access Object for encapsulating database access to sample table.
 */
public class SampleDAO extends HibernateDAO {

    /**
     * Find all info on the sample, whose name is given.
     * @throws DAOException wrapping any thrown by called.
     */
    public Sample getSample( String sampleName, Session session ) throws DAOException {
        Sample retVal = null;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.equal(sampleRoot.get("sampleName"), sampleName));

            List results = session.createQuery(criteriaQuery).getResultList();

            if ( results != null ) {
                if ( results.size() == 1 ) {
                    retVal = (Sample)results.get( 0 );
                }
                else if ( results.size() > 1 ) {
                    throw new DAOException( "Found " + results.size() + " samples for sample name " +
                            sampleName + " but expected only 1." );
                }
            }

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return retVal;
    }

    public Sample getSample( Long sampleId, Session session ) throws DAOException {
        Sample retVal = null;

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.equal(sampleRoot.get("sampleId"), sampleId));

            List results = session.createQuery(criteriaQuery).getResultList();

            if ( results != null ) {
                if ( results.size() == 1 ) {
                    retVal = (Sample)results.get( 0 );
                }
                else if ( results.size() > 1 ) {
                    throw new DAOException( "Found " + results.size() + " samples for sample name " +
                            sampleId + " but expected only 1." );
                }
            }

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return retVal;
    }

    public Sample getSample( Long projectId, Long sampleId, Session session ) throws DAOException {
        Sample retVal = null;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.and(
                            builder.equal(sampleRoot.get("sampleId"), sampleId),
                            builder.equal(sampleRoot.get("projectId"), projectId)
                    ));

            List results = session.createQuery(criteriaQuery).getResultList();
            if ( results != null ) {
                if ( results.size() == 1 ) {
                    retVal = (Sample)results.get( 0 );
                }
                else if ( results.size() > 1 ) {
                    throw new DAOException( "Found " + results.size() + " samples for sample name " +
                            sampleId + " but expected only 1." );
                }
            }
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
        return retVal;
    }

    public Sample getSample( Long projectId, String sampleName, Session session ) throws DAOException {
        Sample retVal = null;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.and(
                            builder.equal(sampleRoot.get("sampleName"), sampleName),
                            builder.equal(sampleRoot.get("projectId"), projectId)
                    ));

            List results = session.createQuery(criteriaQuery).getResultList();
            if ( results != null ) {
                if ( results.size() == 1 ) {
                    retVal = (Sample)results.get( 0 );
                }
                else if ( results.size() > 1 ) {
                    throw new DAOException( "Found " + results.size() + " samples for sample name " + sampleName + " but expected only 1." );
                }
            }
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
        return retVal;
    }

    /** Return a list of all Samples. */
    public List<Sample> getAllSamples( Session session ) throws DAOException {
        List<Sample> returnVal;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot);
            returnVal = new ArrayList<>(session.createQuery(criteriaQuery).getResultList());
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public List<Sample> getChildSamples(Long sampleId, Session session) throws DAOException {
        List<Sample> returnVal;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.equal(sampleRoot.get("parentSampleId"), sampleId));

            returnVal = new ArrayList<>(session.createQuery(criteriaQuery).getResultList());
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    /**
     * Given a sample model, write its relevant data to the database.
     *
     * @throws org.jcvi.ometa.hibernate.dao.DAOException thrown if state of database not as required.
     */
    public void write( Sample sample, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareSampleForWriteback(sample, null, transactionDate, session);
            session.saveOrUpdate( sample );
        } catch ( Exception ex ) {
            throw (ex.getClass() == DAOException.class ? (DAOException)ex : new DAOException(ex));
        }

    }

    public void update( Sample sample, Date transactionDate, Session session ) throws DAOException {
        try {
            if ( sample.getModifiedDate() == null ) {
                sample.setModifiedDate( transactionDate );
            }
            session.merge( sample );
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    /**
     * Finds any missing information from the sample object, and fills it in so that DB
     * writeback will be complete.
     *
     * @param sample may or may not have all its data.
     * @throws Exception by called methods.
     */
    private void prepareSampleForWriteback(Sample sample, String actorName, Date transactionDate, Session session)
            throws DAOException {

        handleNonNewSample(sample, session);
        handleCreationTracking(sample, actorName, transactionDate, session);
        handleProjectRelation(sample, sample.getProjectName(), sample.getSampleName(), session);
    }

    private void handleNonNewSample(Sample sample, Session session) throws DAOException {
        // See: any old sample by same name, and in same project?  Then throw exception.
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
        Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

        String sampleName = sample.getSampleName();
        criteriaQuery.select(sampleRoot)
                .where(builder.and(
                        builder.equal(sampleRoot.get("sampleName"), sampleName),
                        builder.equal(sampleRoot.get("projectId"), sample.getProjectId())
                ));

        List results = session.createQuery(criteriaQuery).getResultList();
        if(results != null  &&  results.size() > 0) {
            throw new DAOException("Sample '" + sampleName + "' already exists.");
        }
    }

    public List<Sample> getAllSamples(Long projectId, Session session) throws DAOException {
        List<Sample> sampleList;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.equal(sampleRoot.get("projectId"), projectId));

            sampleList = new ArrayList<>(session.createQuery(criteriaQuery).getResultList());
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public List<String[]> getSampleStatusForProject(Long projectId, Session session) throws DAOException {
        List<String[]> sampleStatusList;
        try {
            String sql = "select distinct count(s.sample_name), sa.sampla_attribute_str from dod_ometa.sample s" +
                    " join dod_ometa.sample_attribute sa on sa.sampla_sample_id = s.sample_id" +
                    " join dod_ometa.lookup_value lv on lv.lkuvlu_id = sa.sampla_lkuvlu_attribute_id" +
                    " where s.sample_projet_id = :projectId" +
                    " and (lv.lkuvlu_name = 'Sample Status' or  lv.lkuvlu_name = 'Sample_Status')" +
                    " group by sa.sampla_attribute_str";

            NativeQuery query = session.createNativeQuery( sql );
            query.setParameter("projectId", projectId);
            sampleStatusList = new ArrayList<>(query.list());
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleStatusList;
    }

    public List<Sample> getAllSamplesBySearch(Long projectId, String parentEventName, String sampleVal, int firstResult, int maxResult, Session session) throws DAOException {
        List<Sample> sampleList;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);
            List<Predicate> criteriaList = new ArrayList<>();
            criteriaList.add(builder.equal(sampleRoot.get("projectId"), projectId));
            criteriaList.add(builder.like(builder.upper(sampleRoot.get("sampleName")), '%' +sampleVal.toUpperCase()+ '%'));

            if(parentEventName != null && !parentEventName.equals("")) {
                Root<Event> eventRoot = criteriaQuery.from(Event.class);
                Join<Event, LookupValue> eventLookupValueJoin = eventRoot.join("eventTypeLookupValue");

                criteriaList.add(builder.equal(eventLookupValueJoin.get("name"), parentEventName));
                criteriaList.add(builder.equal(eventRoot.get("sampleId"), sampleRoot.get("sampleId")));
            }

            criteriaQuery.select(sampleRoot)
                    .where(builder.and(criteriaList.toArray(new Predicate[0])))
                    .orderBy(builder.asc(sampleRoot.get("sampleName")));

            sampleList = new ArrayList<>(session.createQuery(criteriaQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResult)
                    .getResultList());
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public Integer getSampleCountForProjectBySearch(Long projectId, String sampleVal, Session session) throws DAOException {
        Integer totalSampleCount;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(builder.count(sampleRoot))
                    .where(builder.and(
                            builder.equal(sampleRoot.get("projectId"), projectId),
                            builder.like(builder.upper(sampleRoot.get("sampleName")), '%' +sampleVal.toUpperCase()+ '%')
                    ));

            totalSampleCount = session.createQuery(criteriaQuery).getSingleResult().intValue();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return totalSampleCount;
    }

    public List<Sample> getAllSamples(List<Long> projectIds, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<>();
        try {
            if ( projectIds.size() > 0 ) {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
                Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

                criteriaQuery.select(sampleRoot)
                        .where(sampleRoot.get("projectId").in(projectIds));

                sampleList.addAll( session.createQuery(criteriaQuery).getResultList() );
            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public List<Sample> getSamplesByPublicFlag(Long projectId, boolean isPublic, Session session) throws Exception {
        List<Sample> sampleList;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Sample> criteriaQuery = builder.createQuery(Sample.class);
            Root<Sample> sampleRoot = criteriaQuery.from(Sample.class);

            criteriaQuery.select(sampleRoot)
                    .where(builder.and(
                            builder.equal(sampleRoot.get("projectId"), projectId),
                            builder.equal(sampleRoot.get("isPublic"), isPublic?1:0)
                    ))
                    .orderBy(builder.asc(sampleRoot.get("sampleName")));

            sampleList = new ArrayList<>(session.createQuery(criteriaQuery).getResultList());
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return sampleList;
    }

    public List<Sample> getAllSamples(Long flexId, String type, String sSearch, String sortCol, String sortDir, List<String> columnName, List<String> columnSearchArguments, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<>();
        try {
            List results;

            StringBuilder sql = new StringBuilder(" select S1.*, S2.sample_name parent, CONCAT(A.actor_last_name,',',A.actor_first_name) user," +
                    " SA2.sampla_attribute_date attribute_value_date, SA2.sampla_attribute_float attribute_value_float, SA2.sampla_attribute_str attribute_value_str, SA2.sampla_attribute_int attribute_value_int " +
                    " from sample S1 " +
                    " left join sample S2 on S1.sample_sample_parent_id=S2.sample_id " +
                    " left join actor A on S1.sample_created_by=A.actor_id " +
                    " left join (select SA1.* from sample_attribute SA1, lookup_value LV " +
                    " where SA1.sampla_lkuvlu_attribute_id = LV.lkuvlu_id and LV.lkuvlu_name = '" + sortCol +
                    "') SA2 on S1.sample_id = SA2.sampla_sample_id where ");

            if("sample".equals(type))
                sql.append("S1.sample_id=");
            else
                sql.append("S1.sample_projet_id=");
            sql.append(flexId);

            if(sSearch!=null && !sSearch.isEmpty()) {
                sSearch = '%' +sSearch+ '%';
                sql.append(" and (LOWER(S1.sample_name) like '").append(sSearch).append("' or S1.sample_create_date like '").append(sSearch).append("' ").append(" or (S1.sample_id in (select SA.sampla_sample_id from sample_attribute SA, lookup_value LV ").append("   where COALESCE(SA.sampla_attribute_date,LOWER(SA.sampla_attribute_float),LOWER(SA.sampla_attribute_str),LOWER(SA.sampla_attribute_int)) like '").append(sSearch).append('\'').append(" or (SA.sampla_lkuvlu_attribute_id=LV.lkuvlu_id and LOWER(LV.lkuvlu_name) like '").append(sSearch).append("')) ").append(" or LOWER(S2.sample_name) like '").append(sSearch).append("' or ((LOWER(A.actor_first_name) like '").append(sSearch).append("' or LOWER(A.actor_last_name) like '").append(sSearch).append("'))))");
            }

            if(columnName!=null && !columnName.isEmpty()){
                sql.append(" and (");

                for(int i = 0; i<columnName.size(); i++){
                    String key = columnName.get(i);
                    String[] valueArr = columnSearchArguments.get(i).split(";");
                    String searchVal = valueArr[0];
                    String operation = valueArr[1];
                    String logicGate = i == 0 ? "" : valueArr[2].equals("not") ? "and not" : valueArr[2];

                    if (key.equals("Sample Name")) {
                        sql.append(operation.equals("like") ? ' ' + logicGate + " LOWER(S1.sample_name) like '%" + searchVal + "%' "
                                : operation.equals("in") ? ' ' + logicGate + " LOWER(S1.sample_name) in ('" + searchVal.replaceAll(",", "','") + "') "
                                : ' ' + logicGate + " LOWER(S1.sample_name) = '" + searchVal + "' ");
                    } else if (key.equals("Parent")) {
                        sql.append(operation.equals("like") ? ' ' + logicGate + " LOWER(S2.sample_name) like '%" + searchVal + "%' "
                                : operation.equals("in") ? ' ' + logicGate + " LOWER(S2.sample_name) in ('" + searchVal.replaceAll(",", "','") + "') "
                                : ' ' + logicGate + " LOWER(S2.sample_name) = '" + searchVal + "' ");
                    } else if (key.equals("User")) {
                        String flNameSeparator = ", ";
                        if(searchVal.contains(flNameSeparator)){
                            String[] searchValArr = searchVal.split(flNameSeparator);
                            String firstName = searchValArr[1];
                            String lastName = searchValArr[0];

                            sql.append(operation.equals("like") ? ' ' + logicGate + " (LOWER(A.actor_first_name) like '%" + firstName + "%' or LOWER(A.actor_last_name) like '%" + lastName + "%') "
                                    : operation.equals("in") ? ' ' + logicGate + " (LOWER(A.actor_first_name) in ('" + firstName.replaceAll(",", "','") + "') or LOWER(A.actor_last_name) in ('" + lastName.replaceAll(",", "','") + "')) "
                                    : ' ' + logicGate + " (LOWER(A.actor_first_name) = '" + firstName + "' or LOWER(A.actor_last_name) = '" + lastName + "') ");
                        } else {
                            sql.append(operation.equals("like") ? ' ' + logicGate + " (LOWER(A.actor_first_name) like '%" + searchVal + "%' or LOWER(A.actor_last_name) like '%" + searchVal + "%') "
                                    : operation.equals("in") ? ' ' + logicGate + " (LOWER(A.actor_first_name) in ('" + searchVal.replaceAll(",", "','") + "') or LOWER(A.actor_last_name) in ('" + searchVal.replaceAll(",", "','") + "')) "
                                    : ' ' + logicGate + " (LOWER(A.actor_first_name) = '" + searchVal + "' or LOWER(A.actor_last_name) = '" + searchVal + "') ");
                        }
                    } else if (key.equals("Date")) {
                        sql.append(operation.equals("like") ? ' ' + logicGate + " S1.sample_create_date like '%" + searchVal + "%' "
                                : operation.equals("in") ? ' ' + logicGate + " S1.sample_create_date in ('" + searchVal.replaceAll(",", "','") + "') "
                                : operation.equals("equals") ? ' ' + logicGate + " S1.sample_create_date = '" + searchVal + "' "
                                : ' ' + logicGate + " S1.sample_create_date " + (operation.equals("less") ? "<" : ">") + " '" + searchVal + "' ");
                    } else {
                        String columnSearchSql = ' ' +logicGate+" (S1.sample_id in (select SA.sampla_sample_id from sample_attribute SA, lookup_value LV where SA.sampla_lkuvlu_attribute_id = LV.lkuvlu_id and " +
                                "LV.lkuvlu_name = '"+key+"' and COALESCE(SA.sampla_attribute_date,LOWER(SA.sampla_attribute_float),LOWER(SA.sampla_attribute_str),LOWER(SA.sampla_attribute_int)) #columnSearch#))";

                        sql.append(operation.equals("like") ? columnSearchSql.replace("#columnSearch#", "like '%" + searchVal + "%'")
                                : operation.equals("in") ? columnSearchSql.replace("#columnSearch#", "in ('" + searchVal.replaceAll(",", "','") + "')")
                                : operation.equals("equals") ? columnSearchSql.replace("#columnSearch#", "= '" + searchVal + '\'')
                                : columnSearchSql.replace("#columnSearch#", (operation.equals("less") ? "<" : ">") + " '" + searchVal + '\''));
                    }
                }

                sql.append(')');
            }

            if(sortCol!=null && !sortCol.isEmpty() && sortDir!=null && !sortDir.isEmpty()) {
                sql.append(" order by");
                boolean isDateSort = false;
                if(sortCol.equals("sample"))
                    sql.append(" sample_name ");
                else if(sortCol.equals("parent"))
                    sql.append(" parent ");
                else if(sortCol.equals("user"))
                    sql.append(" user ");
                else if(sortCol.equals("date")) {
                    sql.append(" sample_create_date ");
                    isDateSort = true;
                }else
                    sql.append(" COALESCE(attribute_value_date, attribute_value_float, attribute_value_str, attribute_value_int) ");
                sql.append(sortDir);

                if(isDateSort) sql.append(", sample_name asc");
            }

            NativeQuery query = session.createNativeQuery(sql.toString());
            query.addEntity("S", Sample.class);
            results = query.list();

            if (results != null) {
                for(Object result: results) {
                    sampleList.add((Sample)result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public List<Sample> getAllSamples(String projectIds, String attributeNames, String sSearch, String sortType,
                                      String sortCol, String sortDir, List<String> columnName, List<String> columnSearchArguments, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<>();
        String defaultAttributes[] = {Constants.ATTR_PROJECT_NAME, Constants.ATTR_SAMPLE_NAME, Constants.ATTR_PARENT_SAMPLE_NAME};

        try {
            List results;
            boolean isInt = (sSearch!=null && Pattern.compile("\\d+").matcher(sSearch).matches());
            boolean isSearch = (sSearch!=null && !sSearch.isEmpty());
            boolean isSort = (sortCol!=null && !sortCol.isEmpty() && sortDir!=null && !sortDir.isEmpty());
            boolean isColumnSearch = (columnName!=null && !columnName.isEmpty());

            String sql = null;
            String sub_sql = null;
            StringBuilder col_s_sql = null;
            String sql_s_default =
                    "select distinct #selector# "+
                            "  from project p left join project p_1 on p.projet_projet_parent_id=p_1.projet_id " +
                            "    left join sample s on p.projet_id=s.sample_projet_id left join sample s_1 on s.sample_id=s_1.sample_id"+
                            "  where p.projet_id in (#projectIds#) #opt# ";

            String sql_p =
                    "select distinct #selector# #p_attr#"+
                            "  from sample s left join project p on s.sample_projet_id=p.projet_id " +
                            "    left join project p_1 on p.projet_projet_parent_id=p_1.projet_id "+
                            "    left join project_attribute pa on p.projet_id=pa.projea_projet_id " +
                            "    #lookup# "+
                            "  where p.projet_id in (#projectIds#) #col_s# and #p_opt# ";

            String sql_p_wsearch =
                    " ( "+
                            "    p.projet_name like #sSearch# or p_1.projet_name like #sSearch# or ( "+
                            "      (pa.projea_attribute_str like #sSearch# " +
                            "        or date(pa.projea_attribute_date) like #sSearch# " +
                            (isInt?" or pa.projea_attribute_int=#i_sSearch# ":"") + ") and lv.lkuvlu_name in (#attributes#) "+
                            "    ) "+
                            "  ) ";

            String sql_s =
                    "select distinct #selector# #s_attr# "+
                            "  from sample s left join sample s_1 on s.sample_sample_parent_id=s_1.sample_id "+
                            "    left join project p on s.sample_projet_id=p.projet_id "+
                            "    left join sample_attribute sa on s.sample_id=sa.sampla_sample_id "+
                            "    #lookup# "+
                            "  where p.projet_id in (#projectIds#) #col_s# and #s_opt# ";
            String sql_s_wsearch =
                    " ( "+
                            "   s.sample_name like #sSearch# or s_1.sample_name like #sSearch# or ( "+
                            "     (sa.sampla_attribute_str like #sSearch# " +
                            "       or date(sa.sampla_attribute_date) like #sSearch# " +
                            (isInt?"or sa.sampla_attribute_int=#i_sSearch# ":"") + ')' +
                            "     and lv.lkuvlu_name in (#attributes#) "+
                            "   ) "+
                            " ) ";

            String sql_e =
                    "select distinct #selector# #e_attr# "+
                            "  from "+
                            "    (select e1.* from event e1 "+
                            "       where e1.event_projet_id in (#projectIds#) "+
                            "         and e1.event_create_date=( "+
                            "           select max(e2.event_create_date) from event e2 "+
                            "             where e1.event_projet_id=e2.event_projet_id and e1.event_sampl_id=e2.event_sampl_id and e1.event_type_lkuvl_id=e2.event_type_lkuvl_id "+
                            "             group by e2.event_type_lkuvl_id) "+
                            "    ) as e left join sample s on e.event_sampl_id=s.sample_id "+
                            "    left join event_attribute ea on e.event_id=ea.eventa_event_id "+
                            "    #lookup# "+
                            "  where #e_opt# ";

            String sql_e_wsearch = " (ea.eventa_attribute_str like #sSearch# or date(ea.eventa_attribute_date) like #sSearch# " +
                    (isInt ? " or ea.eventa_attribute_int=#i_sSearch# " : "") + ") and lv.lkuvlu_name in (#attributes#) ";

            String sql_wsort = " #sortOpt# order by attr #sortDir# ";
            String sql_wsort_s = " s.sample_id in (#sampleIds#) ";
            String sql_wsort_p = " s.sample_projet_id in (#projectIds#)";
            String project_field = "pa.projea";
            String sample_field = "sa.sampla";
            String event_field = "ea.eventa";

            if(isColumnSearch){
                col_s_sql = new StringBuilder(" and (");

                for(int i = 0; i<columnName.size(); i++){
                    String key = columnName.get(i);
                    String[] valueArr = columnSearchArguments.get(i).split(";");
                    String searchVal = valueArr[0];
                    String operation = valueArr[1];
                    String logicGate = i == 0 ? "" : valueArr[2].equals("not") ? "and not" : valueArr[2];

                    if (key.equals("Project Name")) {
                        col_s_sql.append(operation.equals("like") ? ' ' + logicGate + " LOWER(p.projet_name) like '%" + searchVal + "%' "
                                : operation.equals("in") ? ' ' + logicGate + " LOWER(p.projet_name) in ('" + searchVal.replaceAll(",", "','") + "') "
                                : ' ' + logicGate + " LOWER(p.projet_name) = '" + searchVal + "' ");
                    } else if (key.equals("Sample Name")) {
                        col_s_sql.append(operation.equals("like") ? ' ' + logicGate + " LOWER(s.sample_name) like '%" + searchVal + "%' "
                                : operation.equals("in") ? ' ' + logicGate + " LOWER(s.sample_name) in ('" + searchVal.replaceAll(",", "','") + "') "
                                : ' ' + logicGate + " LOWER(s.sample_name) = '" + searchVal + "' ");
                    } else {
                        String columnSearchSql = ' ' +logicGate+" (s.sample_id in (select SA.sampla_sample_id from sample_attribute SA, lookup_value LV where SA.sampla_lkuvlu_attribute_id = LV.lkuvlu_id and " +
                                "LV.lkuvlu_name = '"+key+"' and COALESCE(SA.sampla_attribute_date,LOWER(SA.sampla_attribute_float),LOWER(SA.sampla_attribute_str),LOWER(SA.sampla_attribute_int)) #columnSearch#)";

                        col_s_sql.append(operation.equals("like") ? columnSearchSql.replace("#columnSearch#", "like '%" + searchVal + "%'")
                                : operation.equals("in") ? columnSearchSql.replace("#columnSearch#", "in ('" + searchVal.replaceAll(",", "','") + "')")
                                : operation.equals("equals") ? columnSearchSql.replace("#columnSearch#", "= '" + searchVal + '\'')
                                : columnSearchSql.replace("#columnSearch#", (operation.equals("less") ? "<" : ">") + " '" + searchVal + '\''));

                        logicGate = "or";
                        String columnSearchSqlProject = ' ' +logicGate+" p.projet_id in (select PA.projea_projet_id from project_attribute PA, lookup_value LV where PA.projea_lkuvlu_attribute_id = LV.lkuvlu_id and " +
                                "LV.lkuvlu_name = '"+key+"' and COALESCE(PA.projea_attribute_date,LOWER(PA.projea_attribute_float),LOWER(PA.projea_attribute_str),LOWER(PA.projea_attribute_int)) #columnSearch#))";

                        col_s_sql.append(operation.equals("like") ? columnSearchSqlProject.replace("#columnSearch#", "like '%" + searchVal + "%'")
                                : operation.equals("in") ? columnSearchSqlProject.replace("#columnSearch#", "in ('" + searchVal.replaceAll(",", "','") + "')")
                                : operation.equals("equals") ? columnSearchSqlProject.replace("#columnSearch#", "= '" + searchVal + '\'')
                                : columnSearchSqlProject.replace("#columnSearch#", (operation.equals("less") ? "<" : ">") + " '" + searchVal + '\''));


                    }
                }

                col_s_sql.append(')');
            }

            if(isSearch) {
                String lookup = " left join lookup_value lv on #field#_lkuvlu_attribute_id=lv.lkuvlu_id ";
                sub_sql = sql_p.replaceFirst("#p_attr#", "").replaceFirst("#p_opt#", sql_p_wsearch).replaceFirst("#lookup#", lookup.replaceAll("#field#", project_field));

                sub_sql += " union " + sql_s.replaceFirst("#s_attr#", "").replaceFirst("#s_opt#", sql_s_wsearch).replaceFirst("#lookup#", lookup.replaceAll("#field#", sample_field));
                sub_sql += " union " + sql_e.replaceFirst("#e_attr#", "").replaceFirst("#e_opt#", sql_e_wsearch).replaceFirst("#lookup#", lookup.replaceAll("#field#", event_field));
                sub_sql = sub_sql.replaceAll("#sSearch#", "'%"+sSearch.toLowerCase().replaceAll("'", "''")+"%'")
                        .replaceAll("#i_sSearch#", sSearch)
                        .replaceAll("#attributes#", '\'' +attributeNames.replaceAll("'", "''").replaceAll(",", "','")+ '\'');

                sub_sql = sub_sql.replaceAll("#col_s#", "");
            }

            if(isSort) {
                String optSelector = "";
                List<String> defaults = Arrays.asList(defaultAttributes);
                if(defaults.contains(sortCol) || sortCol.equals("Sample Name") || sortCol.equals("Project Name") || sortCol.equals("Parent Sample")) {
                    String temp_sql = "";
                    if(isColumnSearch)
                        temp_sql += col_s_sql;
                    if(isSearch)
                        temp_sql += " and "+sql_wsort_s.replaceFirst("#sampleIds#", sub_sql.replaceAll("#selector#", "s.sample_id"));
                    temp_sql += " order by ";
                    if(sortCol.equals(Constants.ATTR_PROJECT_NAME) || sortCol.equals("Project Name"))
                        temp_sql += "p.projet_name ";
                    else if(sortCol.equals(Constants.ATTR_SAMPLE_NAME) || sortCol.equals("Sample Name"))
                        temp_sql += "s.sample_name ";
                    else if(sortCol.equals("Parent Project"))
                        temp_sql += "p_1.project_name ";
                    else if(sortCol.equals(Constants.ATTR_PARENT_SAMPLE_NAME) || sortCol.equals("Parent Sample"))
                        temp_sql += "s_1.sample_name ";
                    sql = sql_s_default.replace("#opt#", temp_sql + " #sortDir# ");
                } else {
                    if(sortType!=null) {
                        // ea.eventa, sa.sampla, pa.projea
                        String sql_attr = " ,CONCAT(IFNULL(#field#_attribute_str, ''),',',IFNULL(#field#_attribute_date, ''),',',IFNULL(#field#_attribute_int, '')) attr ";
                        String lookup = "and #field#_lkuvlu_attribute_id = (select lv.lkuvlu_id from lookup_value lv where lv.lkuvlu_name in ('"+sortCol+"'))";
                        sql = sortType.equals("p") ? sql_p.replaceFirst("#p_attr#", sql_attr.replaceAll("#field#", project_field)).replaceAll("#lookup#", lookup.replaceAll("#field#", project_field))
                                : sortType.equals("s") ? sql_s.replaceFirst("#s_attr#", sql_attr.replaceAll("#field#", sample_field)).replaceAll("#lookup#", lookup.replaceAll("#field#", sample_field))
                                : sql_e.replaceFirst("#e_attr#", sql_attr.replaceAll("#field#", event_field)).replaceAll("#lookup#", lookup.replaceAll("#field#", event_field));
                        optSelector = '#' + sortType + "_opt#";
                    }

                    String sortOptionSql = (isSearch ? sql_wsort_s.replaceFirst("#sampleIds#", sub_sql.replaceAll("#selector#", "s.sample_id")) : sql_wsort_p);
                    String sortWhereSql = sql_wsort.replaceFirst("#sortOpt#", sortOptionSql);
                    sql = sql.replaceAll(optSelector, sortWhereSql);

                    sql = isColumnSearch ? sql.replaceAll("#col_s#", col_s_sql.toString()) : sql.replaceAll("#col_s#", "");
                }

                sql = sql.replaceFirst("#sortDir#", sortDir);
            }

            sql = (!isSearch && !isSort) ? sql_s_default.replace("#opt#", col_s_sql.toString())
                    : sql == null && isColumnSearch ? sql_s_default.replace("#opt#", col_s_sql.toString()) + " and  s.sample_id in (" + sub_sql.replaceAll("#selector#", "s.sample_id") +" )"
                    : sql == null ? sub_sql
                    : sql;
            sql = sql.replaceAll("#projectIds#", projectIds).replaceAll("#selector#", "s.*");

            NativeQuery query = session.createNativeQuery(sql);
            query.addEntity(Sample.class);
            results = query.list();

            if (results != null) {
                for(Object result: results) {
                    sampleList.add((Sample)result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

}
