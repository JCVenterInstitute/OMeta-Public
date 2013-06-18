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
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 6/2/11
 * Time: 4:16 PM
 *
 * Data access object to handle auth/auth problems.
 */
public class SecurityDAO extends HibernateDAO {
    public static final String UNLOGGED_IN_USER = "guest";

    private static final String PROJ_GRP_SUBST_STR = "{project_group_field}";
    private static final String USERNAME_PARAM = "userName";

    private static final String VIEW_PROJECT_GROUP_FIELD = "P.projet_view_group_id";
    private static final String EDIT_PROJECT_GROUP_FIELD = "P.projet_edit_group_id";

    private static final String RTN_PROJECT_VAR_NAME = "projectReturn";
    private static final String OPEN_PROJECTS_PARAM_LIST_NAME = "openProject";
    private static final String SECURE_PROJECTS_PARAM_LIST_NAME = "securedProject";

    private static final String RTN_SAMPLE_VAR_NAME = "sampleReturn";
    private static final String OPEN_SAMPLES_PARAM_LIST_NAME = "openSample";
    private static final String SECURE_SAMPLES_PARAM_LIST_NAME = "securedSample";

    //-------------------------------PROJECT SECTION
    private static final String SECURED_PROJECTS_SQL_QUERY =
            "select P.projet_name as " + RTN_PROJECT_VAR_NAME +
                    " from actor A, actor_group AG, groups G, project P " +
                    "where  A.actor_id=AG.actgrp_actor_id and AG.actgrp_group_id=G.group_id and G.group_id=" +
                    PROJ_GRP_SUBST_STR + " and A.actor_username=:" + USERNAME_PARAM + " and P.projet_name in (:securedProject)";

    private static final String SECURED_PROJECT_IDS_SQL_QUERY =
            "select P.projet_id as " + RTN_PROJECT_VAR_NAME +
                    " from actor A, actor_group AG, groups G, project P " +
                    "where  A.actor_id=AG.actgrp_actor_id and AG.actgrp_group_id=G.group_id and G.group_id=" +
                    PROJ_GRP_SUBST_STR + " and A.actor_username=:" + USERNAME_PARAM + " and P.projet_id in (:securedProject)";

    private static final String OPEN_AND_SECURED_PROJECTS_SQL_QUERY =
            "select P.projet_name as " + RTN_PROJECT_VAR_NAME +
                    " from project P where P.projet_is_secure=0 " +
                    "and P.projet_name in (:openProject) " +
                    "union " + SECURED_PROJECTS_SQL_QUERY;

    private static final String OPEN_AND_SECURED_PROJECT_IDS_SQL_QUERY =
            "select P.projet_id as " + RTN_PROJECT_VAR_NAME +
                    " from project P where P.projet_is_secure=0 " +
                    "and P.projet_id in (:openProject) " +
                    "union " + SECURED_PROJECT_IDS_SQL_QUERY;


    private static final String AUTHORIZED_FOR_USER_SQL_QUERY =
            "select P.* from project P" +
                    " where " + PROJ_GRP_SUBST_STR + " is null" +
                    " or " + PROJ_GRP_SUBST_STR + " in ("+
                    "   select actgrp_group_id from actor a, actor_group ag where a.actor_username =:"+ USERNAME_PARAM +
                    "   and a.actor_id = ag.actgrp_actor_id)" +
                    " order by P.projet_name";

    //-------------------------------SAMPLE SECTION
    private static final String SECURED_SAMPLES_SQL_QUERY =
            "select S.sample_name as " + RTN_SAMPLE_VAR_NAME +
                    " from actor A, actor_group AG, groups G, sample S, " +
                    "project P " +
                    "where  A.actor_id=AG.actgrp_actor_id and AG.actgrp_group_id=G.group_id and G.group_id=" +
                    PROJ_GRP_SUBST_STR + " and A.actor_username=:" + USERNAME_PARAM + " and " +
                    "S.sample_projet_id=S.sample_id and " +
                    "S.sample_name in (:securedSample)";

    private static final String SECURED_SAMPLE_IDS_SQL_QUERY =
            "select S.sample_id as " + RTN_SAMPLE_VAR_NAME +
                    " from actor A, actor_group AG, groups G, project P, " +
                    "sample S " +
                    "where  A.actor_id=AG.actgrp_actor_id and AG.actgrp_group_id=G.group_id and G.group_id=" +
                    PROJ_GRP_SUBST_STR + " and A.actor_username=:" + USERNAME_PARAM +
                    " and S.sample_projet_id=P.projet_id and S.sample_id in (:securedSample)";

    private static final String OPEN_AND_SECURED_SAMPLES_SQL_QUERY =
            "select S.sample_name as " + RTN_SAMPLE_VAR_NAME +
                    " from project P, sample S where P.projet_is_secure=0 " +
                    "and S.sample_projet_id=P.projet_id " +
                    "and S.sample_name in (:openSample) " +
                    "union " + SECURED_SAMPLES_SQL_QUERY;

    private static final String OPEN_AND_SECURED_SAMPLE_IDS_SQL_QUERY =
            "select S.sample_id as " + RTN_SAMPLE_VAR_NAME +
                    " from project P, sample S " +
                    "where S.sample_projet_id=P.projet_id and P.projet_is_secure=0 " +
                    "and S.sample_id in (:openSample) " +
                    "union " + SECURED_SAMPLE_IDS_SQL_QUERY;

    private Logger logger = Logger.getLogger( SecurityDAO.class );

    //-------------------------------------------------NAME SECTION
    /**
     * Helper to enforce authorization by name, to users.
     */
    public List<String> getListOfAuthorizedByName(
            List<String> names,
            String username,
            ResponseToFailedAuthorization failureResponse,
            AccessLevel accessLevel,
            Session session,
            QueryEntityType queryEntityType ) throws Exception {

        if ( queryEntityType == QueryEntityType.Project ) {
            logger.debug( "Getting list of authorized projects for user " + username );
            return getListOfAuthorizedByName(
                    names, username, failureResponse, accessLevel, session,
                    SECURED_PROJECTS_SQL_QUERY, OPEN_AND_SECURED_PROJECTS_SQL_QUERY, RTN_PROJECT_VAR_NAME, SECURE_PROJECTS_PARAM_LIST_NAME, OPEN_PROJECTS_PARAM_LIST_NAME
            );
        }
        else {
            logger.debug( "Getting list of authorized samples for user " + username );
            return getListOfAuthorizedByName(
                    names, username, failureResponse, accessLevel, session,
                    SECURED_SAMPLES_SQL_QUERY, OPEN_AND_SECURED_SAMPLES_SQL_QUERY, RTN_SAMPLE_VAR_NAME, SECURE_SAMPLES_PARAM_LIST_NAME, OPEN_SAMPLES_PARAM_LIST_NAME
            );
        }
    }

    //-------------------------------------------------ID SECTION
    /**
     * Helper to enforce authorization of values known by their IDs, to users.
     */
    public List<Long> getListOfAuthorizedById(
            List<Long> ids,
            String username,
            ResponseToFailedAuthorization failureResponse,
            AccessLevel accessLevel,
            Session session,
            QueryEntityType queryEntityType
    ) throws Exception {

        if ( queryEntityType == QueryEntityType.Project ) {
            logger.debug( "Getting list of authorized projects for user " + username );
            return getListOfAuthorizedById(
                    ids, username, failureResponse, accessLevel, session,
                    SECURED_PROJECT_IDS_SQL_QUERY,
                    OPEN_AND_SECURED_PROJECT_IDS_SQL_QUERY,
                    SECURE_PROJECTS_PARAM_LIST_NAME,
                    OPEN_PROJECTS_PARAM_LIST_NAME,
                    RTN_PROJECT_VAR_NAME
            );

        }
        else {
            logger.debug( "Getting list of authorized samples for user " + username );
            return getListOfAuthorizedById(
                    ids, username, failureResponse, accessLevel, session,
                    SECURED_SAMPLE_IDS_SQL_QUERY,
                    OPEN_AND_SECURED_SAMPLE_IDS_SQL_QUERY,
                    SECURE_SAMPLES_PARAM_LIST_NAME,
                    OPEN_SAMPLES_PARAM_LIST_NAME,
                    RTN_SAMPLE_VAR_NAME
            );

        }
    }

    //---------------------------------------------HELPERS

    /**
     * Common code for both project and sample, to get data by list-of-identifiers.
     * @throws Exception if anything requested is left out, iff failureResponse == throw ex.
     */
    private List<Long> getListOfAuthorizedById(
            List<Long> ids,
            String username,
            ResponseToFailedAuthorization failureResponse,
            AccessLevel accessLevel,
            Session session,
            String securedIdsQuery,
            String openAndSecuredIdsQuery,
            String securedParamListName,
            String openParamListName,
            String returnVarName
    ) throws Exception {

        ids = uniquifyIds(ids);

        String queryStr = null;
        if ( accessLevel == AccessLevel.View  ) {
            queryStr = openAndSecuredIdsQuery.replace( PROJ_GRP_SUBST_STR, VIEW_PROJECT_GROUP_FIELD );
        }
        else {
            queryStr = securedIdsQuery.replace( PROJ_GRP_SUBST_STR, EDIT_PROJECT_GROUP_FIELD );
        }
        SQLQuery query = session.createSQLQuery( queryStr );
        query.addScalar( returnVarName, Hibernate.STRING );
        if ( accessLevel == AccessLevel.View  ) {
            query.setParameterList( openParamListName, ids );
        }
        query.setParameterList( securedParamListName, ids );
        String queryUsername = username == null ? UNLOGGED_IN_USER : username;
        query.setParameter( USERNAME_PARAM, queryUsername );

        logger.debug(query.getQueryString());
        List<Long> rtnVal = query.list();
        if ( failureResponse == ResponseToFailedAuthorization.ThrowException  &&
                rtnVal.size() < ids.size() ) {
            String idStr = joinIdList( ids );
            String message = makeUserReadableMessage( username, idStr );
            logger.error( message );
            throw new ForbiddenResourceException( message );
        }

        return rtnVal;
    }

    /**
     * Common code for both project and sample, to get data by list-of-names.
     * @throws Exception if anything requested is left out, iff failureResponse == throw ex.
     */
    private List<String> getListOfAuthorizedByName(
            List<String> names,
            String username,
            ResponseToFailedAuthorization failureResponse,
            AccessLevel accessLevel,
            Session session,
            String securedQuery,
            String openAndSecuredQuery,
            String returnVarName,
            String securedParamListName,
            String openParamListName)
            throws Exception {

        // Need to avoid sending same name multiple times.
        names = uniquifyNames(names);

        String queryStr = null;
        if ( accessLevel == AccessLevel.View  ) {
            queryStr = openAndSecuredQuery.replace( PROJ_GRP_SUBST_STR, VIEW_PROJECT_GROUP_FIELD );
        }
        else {
            queryStr = securedQuery.replace( PROJ_GRP_SUBST_STR, EDIT_PROJECT_GROUP_FIELD );
        }
        SQLQuery query = session.createSQLQuery(queryStr);
        query.addScalar(returnVarName, Hibernate.STRING );

        query.setParameterList( securedParamListName, names );
        if ( accessLevel == AccessLevel.View  ) {
            query.setParameterList( openParamListName, names );
        }
        String queryUsername = username == null ? UNLOGGED_IN_USER : username;
        query.setParameter( USERNAME_PARAM, queryUsername );

        List<String> rtnVal = query.list();
        if ( failureResponse == ResponseToFailedAuthorization.ThrowException  &&
                rtnVal.size() < names.size() ) {

            String nameStr = joinNameList(names);
            String message = makeUserReadableMessage( username, nameStr );
            logger.error( message );
            throw new ForbiddenResourceException( message );
        }

        return rtnVal;
    }

    //-------------------------------------------------PROJECT SECTION
    /**
     * Helper to enforce authorization by name, to users.
     */
    public List<Project> getListOfAuthorizedProjects(
            String username,
            AccessLevel accessLevel,
            Session session ) throws Exception {

        String queryStr = AUTHORIZED_FOR_USER_SQL_QUERY;

        if ( accessLevel == AccessLevel.View  ) {
            queryStr = queryStr.replace( PROJ_GRP_SUBST_STR, VIEW_PROJECT_GROUP_FIELD );
        }
        else {
            queryStr = queryStr.replace( PROJ_GRP_SUBST_STR, EDIT_PROJECT_GROUP_FIELD );
        }

        SQLQuery query = session.createSQLQuery(queryStr);
        String queryUsername = username == null ? UNLOGGED_IN_USER : username;
        query.setParameter( USERNAME_PARAM, queryUsername );
        query.addEntity("P", Project.class);
        List<Project> rtnVal = query.list();
        return rtnVal;

    }

    /** Messages generated here can wind up before the user.  Take care to make them readable! */
    private String makeUserReadableMessage(String username, String projectStr) {
        //return "User " + username + " requested projects '" + projectStr + "' but may not access one or more of them.";
        return "You do not have permission to view or edit the project.";
    }

    private List<String> uniquifyNames(List<String> names) {
        Set<String> uniqueNames = new HashSet<String>();
        uniqueNames.addAll( names );
        List<String> rtnList = new ArrayList<String>();
        rtnList.addAll( uniqueNames );
        return rtnList;
    }

    private List<Long> uniquifyIds( List<Long> names ) {
        Set<Long> uniqueNames = new HashSet<Long>();
        uniqueNames.addAll( names );
        List<Long> rtnList = new ArrayList<Long>();
        rtnList.addAll( uniqueNames );
        return rtnList;
    }

    /** Roll a list of strings into a comma-separated single string. */
    private String joinNameList(List<String> names) {
        StringBuilder bldr = new StringBuilder();

        if ( names != null ) {
            for ( String nextName: uniquifyNames(names) ) {
                if ( bldr.length() > 0 ) {
                    bldr.append( "','" );
                }
                bldr.append( nextName );
            }
        }
        else {
            throw new IllegalArgumentException( "Null name list not allowed." );
        }

        return bldr.toString();
    }

    /** Roll a list of numeric IDs into a comma-separated single string. */
    private String joinIdList(List<Long> projects) {
        StringBuilder bldr = new StringBuilder();
        if ( projects != null ) {
            for ( Long nextProject: projects ) {
                if ( bldr.length() > 0 ) {
                    bldr.append( "," );
                }
                bldr.append( nextProject );
            }
        }
        else {
            throw new IllegalArgumentException( "Null id list not allowed." );
        }

        return bldr.toString();
    }

}
