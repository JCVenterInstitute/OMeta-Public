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
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Group;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.utils.Constants;

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
public class ProjectDAO extends HibernateDAO {
    private static final String RTN_PROJECT_NAME = "projectName";
    private static final String RTN_PROJECT_ID = "projectId";
    private static final String SECURED_PROJECTS_SQL_QUERY = "select P.projet_name as projectName from project P where P.projet_is_secure=1";
    private static final String CHILD_PROJECTS_SQL_QUERY = "select * from project P where P.projet_projet_parent_id=:parantId";


    /**
     * Writes back to database, but assumes session and possible transaction already established.
     *
     * @param project to be saved.
     * @param session for hibernate action.
     * @throws DAOException thrown in place of any received exception.
     */
    public void write( Project project, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareProjectForWriteback( project, null, transactionDate, session );
            session.saveOrUpdate( project );
            session.flush();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    /**
     * update to database, but assumes session and possible transaction already established.
     *
     * @param project to be saved.
     * @param session for hibernate action.
     * @throws DAOException thrown in place of any received exception.
     */
    public void update( Project project, Date transactionDate, Session session ) throws DAOException {
        try {
            if ( project.getModifiedDate() == null ) {
                project.setModifiedDate( transactionDate );
            }
            session.merge(project);
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    /**
     * Given name of project, return actual project "row" object.
     *
     * @param projectName name, as known to user world.
     * @param session for dealing with db.
     * @return the object found.
     * @throws DAOException in response to other exceptions.
     */
    public Project getProject( String projectName, Session session ) throws DAOException {
        Project returnVal = null;
        try {
            Criteria crit = session.createCriteria( Project.class );
            crit.add( Restrictions.eq( "projectName", projectName ) );
            List results = crit.list();
            if ( results == null  ||  results.size() == 0  ||  results.size() > 1 ) {
                if ( results.size() > 1 ) {
                    throw new DAOException("Multiple project appear by the same name, of " + projectName );
                }
            }
            else {
                returnVal = (Project)results.get( 0 );
                logger.debug( "Found one project called " + returnVal.getProjectName() + " " +
                        returnVal.getProjectId() );
            }
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public Project getProject( Long projectId, Session session ) throws DAOException {
        Project returnVal = null;
        try {
            Criteria crit = session.createCriteria( Project.class );
            crit.add( Restrictions.eq( "projectId", projectId ) );
            List results = crit.list();
            returnVal = (Project)results.get( 0 );
            logger.debug( "Found one project called " + returnVal.getProjectName() + " " +
                    returnVal.getProjectId() );
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    /**
     * Given names of projects, return actual project "row" objects.
     *
     * @param projectNames names of projects, as known to user world.
     * @param session for dealing with db.
     * @return the object found.
     * @throws DAOException in response to other exceptions.
     */
    public List<Project> getProjects( List<String> projectNames, Session session ) throws DAOException {
        List<Project> returnVal = new ArrayList<Project>();
        try {
            if ( projectNames.size() > 0 ) {
                Criteria crit = session.createCriteria( Project.class );
                if( !"ALL".equals( projectNames.get(0) ) )
                    crit.add( Restrictions.in( "projectName", projectNames ) );
                crit.addOrder(Order.asc("projectName"));
                List results = crit.list();
                returnVal.addAll( results );
            }
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public List<Project> getProjectsByPublicFlag(boolean isPublic, Session session) throws Exception {
        List<Project> rtnVal = new ArrayList<Project>();
        try {
            Criteria crit = session.createCriteria( Project.class );
            crit.add( Restrictions.eq( "isPublic", isPublic?1:0 ) );
            crit.addOrder(Order.asc("projectName"));
            List results = crit.list();
            rtnVal.addAll( results );
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }
        return rtnVal;
    }

    /** Return a list of all projects. */
    public List<Project> getAllProjects( Session session ) throws DAOException {
        List<Project> returnVal = new ArrayList<Project>();
        try {
            Criteria crit = session.createCriteria( Project.class );
            returnVal.addAll( crit.list() );
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    /** Return a list of all projects. */
    public List<String> getSecuredProjectNames( Session session ) throws DAOException {
        List<String> returnVal = new ArrayList<String>();
        try {
            SQLQuery query = session.createSQLQuery(SECURED_PROJECTS_SQL_QUERY);
            query.addScalar(RTN_PROJECT_NAME, Hibernate.STRING);
            returnVal.addAll( query.list() );

        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public List<Project> getChildProjects( Long projectId, Session session ) throws DAOException {
        List<Project> returnVal = new ArrayList<Project>();
        try {
            SQLQuery query = session.createSQLQuery( CHILD_PROJECTS_SQL_QUERY );
            /*query.addScalar(RTN_PROJECT_NAME, Hibernate.STRING );
            query.addScalar(RTN_PROJECT_ID, Hibernate.LONG );*/
            query.setLong("parantId", projectId); //ParentProjectId
            query.addEntity("P", Project.class);
            returnVal = query.list();

        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    /**
     * Finds any missing information from the project object, and fills it in so that DB
     * writeback will be complete.
     *
     * @param project may or may not have all its data.
     * @throws Exception by called methods.
     */
    private void prepareProjectForWriteback( Project project, String actorName, Date transactionDate, Session session ) throws Exception {

        handleNonNewProject(project, session);
        handleCreationTracking(project, actorName, transactionDate, session);
        resolveParentProjectId(project, session);
        applyDefaultSecurity(project, session);
    }

    private void handleNonNewProject(Project project, Session session) {
        // See: any old version of project?  If so, blow out.
        Criteria crit = session.createCriteria( Project.class );
        String projectName = project.getProjectName();
        crit.add( Restrictions.eq("projectName", projectName) );
        List results = crit.list();
        if ( results != null  &&  results.size() > 0 ) {
            throw new IllegalStateException("Do not call writeback loop with an existing project.  Project " + projectName + " is in the database.");
        }
    }

    private void resolveParentProjectId(Project project, Session session) {
        Criteria crit;List results;
        if ( project.getParentProjectId() == null ) {
            // Need to resolve the parent project ID from the parent project name.
            String parentProjectName = project.getParentProjectName();
            Integer projectLevel = project.getProjectLevel();
            if ( parentProjectName == null   &&  projectLevel > 1 ) {
                throw new IllegalStateException(
                        "Cannot resolve parent project name for project at level " + projectLevel);
            }
            else if ( parentProjectName != null ) {
                // Need to read yet another project from database, by name, to get its ID.
                crit = session.createCriteria( Project.class );
                crit.add( Restrictions.eq("projectName", parentProjectName) );
                results = crit.list();
                if ( results.size() > 0 ) {
                    Project parent = (Project)results.get( 0 );
                    project.setParentProjectId(parent.getProjectId());
                }
            }
        }
    }


    //-------------------------------------------------DEFAULTING
    /**
     * Under certain circumstances, especially when a new project is created, it is necessary to place
     * its write-back security into a default mode.  This mode allows the general user (anyone logged
     * in) to write things in that project.  When projects are newly-being created (load time), this
     * is desirable.  Otherwise, with security in place, the user creating the project is barred from
     * completing its data.
     *
     * @param project object that should already be in the hibernate session.
     * @param session hibernate session that knows the project.
     * @throws Exception thrown by called methods, or if the general edit group lookup value has wrong cardinality.
     */
    public void applyDefaultSecurity(Project project, Session session) throws Exception {
        logger.info( "Setting default security on project " + project.getProjectName() );

        if(project.getEditGroup() == null) {
            Group editGroup = this.getGroup(Constants.GROUP_GENERAL_EDIT, Constants.LOOKUP_VALUE_TYPE_EDIT_GROUP, project.getProjectName(), session);
            project.setEditGroup(editGroup.getGroupId());
        }

        if(project.getViewGroup() == null) {
            Group viewGroup = this.getGroup(Constants.GROUP_GENERAL_VIEW, Constants.LOOKUP_VALUE_TYPE_ACCESS_GROUP, project.getProjectName(), session);
            project.setViewGroup(viewGroup.getGroupId());
        }
    }

    private Group getGroup(String groupName, String roleType, String projectName, Session session) throws Exception {
        LookupValueDAO lvDAO = new LookupValueDAO();
        LookupValue groupLv = lvDAO.getLookupValue(groupName, roleType, session);
        if(groupLv == null) {
            throw new DAOException( "Failed to find lookup value for " + groupName );
        }

        GroupDAO groupDAO = new GroupDAO();
        Group group = groupDAO.getGroupByLookupId(groupLv. getLookupValueId(), session);
        if(group == null) {
            throw new DAOException("Cannot find group " + groupName + " to secure " + projectName);
        }

        return group;
    }
}
