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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.utils.Constants;

import javax.jws.WebMethod;
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

            Criteria crit = session.createCriteria( Sample.class );
            crit.add( Restrictions.eq( "sampleName", sampleName ) );
            List results = crit.list();

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

            Criteria crit = session.createCriteria( Sample.class );
            crit.add( Restrictions.eq( "sampleId", sampleId ) );
            List results = crit.list();

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
            Criteria crit = session.createCriteria( Sample.class );
            crit.add( Restrictions.and(
                    Restrictions.eq( "sampleId", sampleId ),
                    Restrictions.eq( "projectId", projectId ) ) );
            List results = crit.list();
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
            Criteria crit = session.createCriteria( Sample.class );
            crit.add( Restrictions.and(
                    Restrictions.eq( "sampleName", sampleName ),
                    Restrictions.eq( "projectId", projectId ) ) );
            List results = crit.list();
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
        List<Sample> returnVal = new ArrayList<Sample>();
        try {
            Criteria crit = session.createCriteria( Sample.class );
            returnVal.addAll( crit.list() );
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public List<Sample> getChildSamples(Long sampleId, Session session) throws DAOException {
        List<Sample> returnVal = new ArrayList<Sample>();
        try {
            Criteria crit = session.createCriteria( Sample.class );
            crit.add(Restrictions.eq("parentSampleId", sampleId));
            returnVal.addAll( crit.list() );
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
        Criteria crit = session.createCriteria( Sample.class );
        String sampleName = sample.getSampleName();
        crit.add( Restrictions.eq("sampleName", sampleName) );
        crit.add( Restrictions.eq("projectId", sample.getProjectId()) );
        List results = crit.list();
        if(results != null  &&  results.size() > 0) {
            throw new DAOException("Sample '" + sampleName + "' already exists.");
        }
    }

    public List<Sample> getAllSamples(Long projectId, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<Sample>();
        try {
            Criteria crit = session.createCriteria( Sample.class );
            crit.add( Restrictions.eq( "projectId", projectId ) );
            List<Sample> results = crit.list();
            sampleList.addAll( results );
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public List<Sample> getAllSamplesBySearch(Long projectId, String sampleVal, int firstResult, int maxResult, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<Sample>();
        try {
            Criteria crit = session.createCriteria( Sample.class );
            crit.add( Restrictions.eq( "projectId", projectId ) );
            crit.add( Restrictions.ilike("sampleName", sampleVal, MatchMode.ANYWHERE));
            crit.addOrder(Order.asc("sampleName"));
            crit.setFirstResult(firstResult);
            crit.setMaxResults(maxResult);
            List<Sample> results = crit.list();
            sampleList.addAll( results );
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public Integer getSampleCountForProjectBySearch(Long projectId, String sampleVal, Session session) throws DAOException {
        Integer totalSampleCount = 0;
        try {
            Criteria crit = session.createCriteria( Sample.class );
            crit.add(Restrictions.eq("projectId", projectId));
            crit.add( Restrictions.ilike("sampleName", sampleVal, MatchMode.ANYWHERE));
            crit.setProjection(Projections.rowCount());
            totalSampleCount = (Integer) crit.uniqueResult();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return totalSampleCount;
    }

    public List<Sample> getAllSamples(List<Long> projectIds, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<Sample>();
        try {
            if ( projectIds.size() > 0 ) {
                Criteria crit = session.createCriteria( Sample.class );
                crit.add( Restrictions.in( "projectId", projectIds ) );
                List<Sample> results = crit.list();
                sampleList.addAll( results );

            }

        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return sampleList;
    }

    public List<Sample> getSamplesByPublicFlag(Long projectId, boolean isPublic, Session session) throws Exception {
        List<Sample> sampleList = new ArrayList<Sample>();
        try {
            Criteria crit = session.createCriteria( Sample.class );
            crit.add(
                    Restrictions.and(
                            Restrictions.eq( "projectId", projectId ),
                            Restrictions.eq( "isPublic", isPublic?1:0 )
                    )
            );
            List<Sample> results = crit.list();
            sampleList.addAll( results );
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return sampleList;
    }

    public List<Sample> getAllSamples(Long flexId, String type, String sSearch, String sortCol, String sortDir, Map<String, String> columnSearchMap, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<Sample>();
        try {
            List results = null;

            String sql = " select S1.*, S2.sample_name parent, CONCAT(A.actor_last_name,',',A.actor_first_name) user," +
                    " SA2.sampla_attribute_float attribute_value_float, SA2.sampla_attribute_str attribute_value_str, SA2.sampla_attribute_int attribute_value_int " +
                    " from sample S1 " +
                    " left join sample S2 on S1.sample_sample_parent_id=S2.sample_id " +
                    " left join actor A on S1.sample_created_by=A.actor_id " +
                    " left join (select SA1.* from sample_attribute SA1, lookup_value LV " +
                    " where SA1.sampla_lkuvlu_attribute_id = LV.lkuvlu_id and LV.lkuvlu_name = '"+ sortCol +
                    "') SA2 on S1.sample_id = SA2.sampla_sample_id where ";

            if("sample".equals(type))
                sql += "S1.sample_id=";
            else
                sql += "S1.sample_projet_id=";
            sql+=flexId;

            if(sSearch!=null && !sSearch.isEmpty()) {
                sSearch = "%"+sSearch+"%";
                sql+=" and (LOWER(S1.sample_name) like '"+sSearch+"' " +
                        " or (S1.sample_id in (select SA.sampla_sample_id from sample_attribute SA, lookup_value LV " +
                        "   where LOWER(SA.sampla_attribute_str) like '"+sSearch+"' or (SA.sampla_lkuvlu_attribute_id=LV.lkuvlu_id and LOWER(LV.lkuvlu_name) like '"+sSearch+"')) " +
                        " or LOWER(S2.sample_name) like '"+sSearch+"' or ((LOWER(A.actor_first_name) like '"+sSearch+"' or LOWER(A.actor_last_name) like '"+sSearch+"'))))";
            }

            if(columnSearchMap!=null && !columnSearchMap.isEmpty()){
                String columnSearchSql = " and (S1.sample_id in (select SA.sampla_sample_id from sample_attribute SA, lookup_value LV where SA.sampla_lkuvlu_attribute_id = LV.lkuvlu_id and " +
                        "LV.lkuvlu_name = '#columnName#' and COALESCE(LOWER(SA.sampla_attribute_float),LOWER(SA.sampla_attribute_str),LOWER(SA.sampla_attribute_int)) like '%#columnSearchVal#%'))";

                for(Map.Entry<String, String> entry : columnSearchMap.entrySet()){
                    String key = entry.getKey();
                    String value = entry.getValue();

                    if(key.equals("sample")){
                        sql += " and LOWER(S1.sample_name) like '%"+value+"%' ";
                    } else if(key.equals("parent")){
                        sql += " and LOWER(S2.sample_name) like '%"+value+"%' ";
                    } else if(key.equals("user")){
                        sql += " and (LOWER(A.actor_first_name) like '%"+value+"%' or LOWER(A.actor_last_name) like '%"+value+"%') ";
                    } else if(key.equals("date")){
                       //No filter for date yet
                    } else {
                        sql += columnSearchSql.replace("#columnName#", key).replace("#columnSearchVal#", entry.getValue());
                    }
                }
            }

            if(sortCol!=null && !sortCol.isEmpty() && sortDir!=null && !sortDir.isEmpty()) {
                sql += " order by";
                boolean isDateSort = false;
                if(sortCol.equals("sample"))
                    sql += " sample_name ";
                else if(sortCol.equals("parent"))
                    sql += " parent ";
                else if(sortCol.equals("user"))
                    sql += " user ";
                else if(sortCol.equals("date")) {
                    sql += " sample_create_date ";
                    isDateSort = true;
                }else
                    sql += " COALESCE(attribute_value_float, attribute_value_str, attribute_value_int) ";
                sql += sortDir;

                if(isDateSort) sql += ", sample_name asc";
            }

            SQLQuery query = session.createSQLQuery( sql );
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

    public List<Sample> getAllSamples(String projectIds, String attributeNames, String sSearch, String sortType, String sortCol, String sortDir, Session session) throws DAOException {
        List<Sample> sampleList = new ArrayList<Sample>();
        String defaultAttributes[] = {Constants.ATTR_PROJECT_NAME, Constants.ATTR_SAMPLE_NAME, Constants.ATTR_PARENT_SAMPLE_NAME};

        try {
            List results = null;
            boolean isInt = (sSearch!=null && Pattern.compile("\\d+").matcher(sSearch).matches());
            boolean isSearch = (sSearch!=null && !sSearch.isEmpty());
            boolean isSort = (sortCol!=null && !sortCol.isEmpty() && sortDir!=null && !sortDir.isEmpty());

            String sql = null;
            String sub_sql = null;
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
                            "  where p.projet_id in (#projectIds#) and #p_opt# ";

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
                            "  where p.projet_id in (#projectIds#) and #s_opt# ";
            String sql_s_wsearch =
                    " ( "+
                            "   s.sample_name like #sSearch# or s_1.sample_name like #sSearch# or ( "+
                            "     (sa.sampla_attribute_str like #sSearch# " +
                            "       or date(sa.sampla_attribute_date) like #sSearch# " +
                            (isInt?"or sa.sampla_attribute_int=#i_sSearch# ":"") + ")" +
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

            if(isSearch) {
                String lookup = " left join lookup_value lv on #field#_lkuvlu_attribute_id=lv.lkuvlu_id ";
                sub_sql = sql_p.replaceFirst("#p_attr#", "").replaceFirst("#p_opt#", sql_p_wsearch).replaceFirst("#lookup#", lookup.replaceAll("#field#", project_field));
                sub_sql += " union " + sql_s.replaceFirst("#s_attr#", "").replaceFirst("#s_opt#", sql_s_wsearch).replaceFirst("#lookup#", lookup.replaceAll("#field#", sample_field));
                sub_sql += " union " + sql_e.replaceFirst("#e_attr#", "").replaceFirst("#e_opt#", sql_e_wsearch).replaceFirst("#lookup#", lookup.replaceAll("#field#", event_field));
                sub_sql = sub_sql.replaceAll("#sSearch#", "'%"+sSearch.toLowerCase().replaceAll("'", "''")+"%'")
                        .replaceAll("#i_sSearch#", sSearch)
                        .replaceAll("#attributes#", "'"+attributeNames.replaceAll("'", "''").replaceAll(",", "','")+"'");
            }
            if(isSort) {
                String optSelector = "";
                List<String> defaults = Arrays.asList(defaultAttributes);
                if(defaults.contains(sortCol) || sortCol.equals("Sample Name") || sortCol.equals("Project Name") || sortCol.equals("Parent Sample")) {
                    String temp_sql = "";
                    if(isSearch)
                        temp_sql = " and "+sql_wsort_s.replaceFirst("#sampleIds#", sub_sql.replaceAll("#selector#", "s.sample_id"));
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
                        optSelector = "#" + sortType + "_opt#";
                    }

                    String sortOptionSql = (isSearch ? sql_wsort_s.replaceFirst("#sampleIds#", sub_sql.replaceAll("#selector#", "s.sample_id")) : sql_wsort_p);
                    String sortWhereSql = sql_wsort.replaceFirst("#sortOpt#", sortOptionSql);
                    sql = sql.replaceAll(optSelector, sortWhereSql);
                }

                sql = sql.replaceFirst("#sortDir#", sortDir);
            }
            sql = (sql == null ? sub_sql : sql);
            sql = sql.replaceAll("#projectIds#", projectIds).replaceAll("#selector#", "s.*");

            SQLQuery query = session.createSQLQuery(sql);
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
