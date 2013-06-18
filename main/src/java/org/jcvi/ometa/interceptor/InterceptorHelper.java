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

package org.jcvi.ometa.interceptor;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jcvi.ometa.configuration.*;
import org.jcvi.ometa.db_interface.DAOFactory;
import org.jcvi.ometa.hibernate.dao.ContainerizedSessionAndTransactionManager;
import org.jcvi.ometa.hibernate.dao.ProjectDAO;
import org.jcvi.ometa.hibernate.dao.SecurityDAO;
import org.jcvi.ometa.hibernate.dao.SessionAndTransactionManagerI;
import org.jcvi.ometa.model.ProjectNamerOnFileRead;

import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.*;

public class InterceptorHelper implements Serializable {
    // Note: had tried using ReadOnlyAllOrNothingAuthInterceptor.class.getName(), but triggered some sort
    // of opensymphony exception regarding one of ITS interceptor classes/interfaces.  LLF, 6/27/2011
    public static final String PARAMETERS_MAP_SESSION_KEY =
            "PWSSecurityValidation__parameters-map";
    public static final String NOT_LOGGED_IN_MSG = "You must login to use this service.";

    private Logger logger;
    private AccessLevel accessLevel;
    private ResponseToFailedAuthorization missingProjectsResponse;
    private SecurityDAO securityDAO;
    private ProjectDAO projectDAO;
    private SessionAndTransactionManagerI sessionAndTransactionManager;

    public InterceptorHelper( Properties props, Logger logger ) {
        DAOFactory daoFactory = new DAOFactory();
        securityDAO = daoFactory.getSecurityDAO();
        projectDAO = daoFactory.getProjectDAO();
        sessionAndTransactionManager = new ContainerizedSessionAndTransactionManager( props );
        this.logger = logger;
    }

    /** Alternative constructor to support unit testing. */
    public InterceptorHelper( SessionAndTransactionManagerI sessionAndTransactionManager, Logger logger ) {
        DAOFactory daoFactory = new DAOFactory();
        securityDAO = daoFactory.getSecurityDAO();
        projectDAO = daoFactory.getProjectDAO();
        this.sessionAndTransactionManager = sessionAndTransactionManager;
        this.logger = logger;
    }

    public void setAccessLevel( AccessLevel accessLevel ) {
        this.accessLevel = accessLevel;
    }

    public void setMissingProjectsResponse( ResponseToFailedAuthorization missingProjectsResponse ) {
        this.missingProjectsResponse = missingProjectsResponse;
    }

    /**
     * Checks whether a real user is logged into the service.
     *
     * @param principal must be nonnull, and have nonnull username.
     * @return username if found.
     * @throws IllegalAccessException if null encountered in tests.
     */
    public String getAndTestLoggedInUser(Principal principal) throws IllegalAccessException {
        if ( principal == null   ||   principal.equals( SecurityDAO.UNLOGGED_IN_USER ) ) {
            logger.error(
                    "No authenticated caller principal found."
            );
            throw new IllegalAccessException( InterceptorHelper.NOT_LOGGED_IN_MSG );
        }

        return principal.getName();
    }

    /**
     * Given a username and call context, get list of all things from the requested parameter,
     * which user is allowed to actually see.  May return null.
     *
     * @param invocationContext from interceptor.  For pulling parameters.
     * @param user who wants to fetch or write?
     * @return list of allowed, or null.
     */
    public List<? extends Object> checkAnnotatedMethodPermissions(InvocationContext invocationContext, String user) {
        List<? extends Object> approved = Collections.EMPTY_LIST;

        Method targetMethod = invocationContext.getMethod();
        Annotation[][] allAnnotations = targetMethod.getParameterAnnotations();
        boolean encounteredRelevantAnnotation = false;

        // Look for relevant params.  Note: i index is for method.  j index is for annotations.
        for ( int i = 0; !encounteredRelevantAnnotation  &&  i < allAnnotations.length; i++ ) {

            for ( int j = 0; !encounteredRelevantAnnotation  &&  j < allAnnotations[ i ].length; j++ ) {

                Type[] paramTypes = targetMethod.getGenericParameterTypes();
                QueryEntityType queryEntityType = resolveQueryType(allAnnotations[i][j]);
                if ( queryEntityType != null ) {

                    logger.debug( "Getting list of approved " + queryEntityType );
                    approved = getListOfApproved(
                            invocationContext.getParameters()[i],
                            user,
                            paramTypes[i],
                            queryEntityType
                    );

                    encounteredRelevantAnnotation = true;
                }

            }
        }

        if ( ! encounteredRelevantAnnotation ) {
            // Nothing annotated -> nothing to be checked against permissions list.
            approved = Collections.EMPTY_LIST;
        }

        return approved;
    }

    /**
     * Find out what is approved for this user, compared to the request.
     *
     * @param o list or individual project or study to be examined for access.
     * @param user principal which may/may not have access.
     * @param paramType type of parameter, from reflection scan.
     * @param queryEntityType either project or sample.
     * @return list of approved.  Null -> tried to go out-of-bounds.
     */
    public List<? extends Object> getListOfApproved(
            Object o,
            String user,
            Type paramType,
            QueryEntityType queryEntityType
    ) {
        List<? extends Object> approved = null;

        // i-index for parameter itself.
        Type nextType = paramType;
        if ( nextType instanceof ParameterizedType) {

            ParameterizedType type = (ParameterizedType) nextType;
            Type[] typeArguments = type.getActualTypeArguments();
            for ( Type typeArgument : typeArguments ) {

                Class typeArgClass = (Class) typeArgument;
                if ( typeArgClass == String.class ) {
                    // We have a collection of Strings.
                    approved = this.getApprovedByName(
                            queryEntityType, o, user
                    );
                }
                else if ( typeArgClass == Long.class ) {
                    // We have a collection of Longs.
                    approved = this.getApprovedById(
                            queryEntityType, o, user
                    );
                }
                else if ( Arrays.asList( typeArgClass.getInterfaces() ).contains( ProjectNamerOnFileRead.class ) ) {
                    // Rather obscure error message: meant for programmers.
                    if ( queryEntityType != QueryEntityType.Project ) {
                        throw new IllegalStateException( "Project names given, but marked up as samples." );
                    }
                    // Not attempting to cast to specific type at generic level. That would force
                    // parameters to be typed <ProjectNamerOnFileRead> or <? extends ProjectNamerOnFileRead>
                    List<String> projectNames = new ArrayList<String>();
                    List namers = (List)o;
                    for ( Object nextO: namers ) {
                        String projectName = ((ProjectNamerOnFileRead)nextO).getProjectName();
                        if ( ! projectNames.contains( projectName ) )
                            projectNames.add( projectName );
                    }
                    approved = this.getApprovedByName(
                            queryEntityType, projectNames, user
                    );
                }


            }

        }
        if ( nextType == String.class ) {
            // We have one string.
            approved = this.getApprovedSingleByName(queryEntityType, o, user);
        }
        else if ( nextType == Long.class ) {
            // We have one Long.
            approved = this.getApprovedSingleById(queryEntityType, o, user);
        }
        return approved;
    }

    public List<Long> getApprovedSingleById( QueryEntityType queryEntityType, Object o, String user ) {
        List<Long> approved = new ArrayList<Long>();
        List<Long> requested = new ArrayList<Long>();
        requested.add((Long) o);
        return this.getApprovedById(
                user, approved, requested, queryEntityType);
    }

    public List<String> getApprovedSingleByName( QueryEntityType queryEntityType, Object o, String user ) {
        List<String> requested = new ArrayList<String>();
        requested.add((String) o);
        List<String> approved = new ArrayList<String>();
        return this.getApprovedByName(
                user, approved, requested, queryEntityType);
    }

    public List<Long> getApprovedById(QueryEntityType queryEntityType, Object o, String user) {
        List<Long> approved = new ArrayList<Long>();
        List<Long> requested =
                (List<Long>) o;
        return this.getApprovedById(
                user, approved, requested, queryEntityType);
    }

    public List<String> getApprovedByName(QueryEntityType queryEntityType, Object o, String user) {
        List<String> approved = new ArrayList<String>();
        List<String> requested =
                (List<String>) o;
        return this.getApprovedByName(
                user, approved, requested, queryEntityType);
    }

    /** Which type of query, found in annotation? */
    public QueryEntityType resolveQueryType(Annotation annotation) {
        if ( annotation instanceof JCVI_Project) {
            return QueryEntityType.Project;
        }
        else if ( annotation instanceof JCVI_Sample) {
            return QueryEntityType.Sample;
        }
        else {
            return null;
        }
    }

    /**
     * This code looks at the projects that have been requested, and tells whether they are allowed or not.
     *
     * @param user       who to lookup.
     * @param parameters what they are asking for.
     * @return list of approved project names.
     */
    public List<String> getApprovedProjects(String user, Map<String, Object> parameters) {
        List<String> approvedProjects = new ArrayList<String>();
        String[] requestedProjectsArray = (String[]) parameters.get("projectNames");
        if ( requestedProjectsArray == null || requestedProjectsArray.length == 0 ) {
            requestedProjectsArray = (String[]) parameters.get("projectName");
            // Be careful to set this to empty, instead of null.  Null symbolizes something different.
            if ( requestedProjectsArray == null ) {
                requestedProjectsArray = new String[] {};
            }
        }
        List<String> requestedProjectsList = makeListOfMultivaluedParameters( requestedProjectsArray );

        approvedProjects = getApprovedByName(user, approvedProjects, requestedProjectsList, QueryEntityType.Project);
        return approvedProjects;

    }

    /**
     * This code looks at the projects that have been requested, and tells whether they are allowed or not.
     * NOTE: cannot contact the EJB that is being intercepted, from the interceptor!
     *
     * @param user                who to lookup.
     * @param requestedList       what they are asking for.
     * @param approved            default list.  Can add within here.
     * @param queryEntityType     which type are we concerned with?
     * @return list of approved.
     */
    public synchronized List<String> getApprovedByName(
            String user, List<String> approved, List<String> requestedList,
            QueryEntityType queryEntityType
    ) {
        // Avoid telling user they are not allowed to request nothing.
        if ( requestedList == null  ||  requestedList.size() == 0 ) {
            return Collections.EMPTY_LIST;
        }

        try {
            Session session = sessionAndTransactionManager.getSession();
            sessionAndTransactionManager.startTransaction();
            approved.addAll(
                securityDAO.getListOfAuthorizedByName(
                        requestedList,
                        user,
                        missingProjectsResponse,
                        accessLevel,
                        session,
                        queryEntityType
                )
            );
            sessionAndTransactionManager.commitTransaction();

        } catch (Exception ex) {
            logger.error( ex.getMessage() );
            sessionAndTransactionManager.rollBackTransaction();
            ex.printStackTrace();
            approved = null;
        }
        return approved;
    }

    /**
     * This code looks at the items that have been requested, and tells whether they are allowed or not.
     * NOTE: cannot contact the EJB that is being intercepted, from the interceptor!
     *
     * @param user               who to lookup.
     * @param requestedList      what they are asking for.
     * @param approved           default list.  Can add within here.
     * @param queryEntityType    type of entity to check.
     * @return list of approved.
     */
    public synchronized List<Long> getApprovedById(
            String user,
            List<Long> approved,
            List<Long> requestedList,
            QueryEntityType queryEntityType) {

        // Avoid telling user they are not allowed to request nothing.
        if ( requestedList == null  ||  requestedList.size() == 0 ) {
            return Collections.EMPTY_LIST;
        }

        try {
            // NOTE: ignoring the session-and-tx-mgr's own transactions, because not expecting to run this
            // under any other transaction.
            Session session = sessionAndTransactionManager.getSession();
            sessionAndTransactionManager.startTransaction();

            approved.addAll(
                securityDAO.getListOfAuthorizedById(
                        requestedList,
                        user,
                        missingProjectsResponse,
                        accessLevel,
                        session,
                        queryEntityType
                )
            );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            logger.error( ex.getMessage() );
            sessionAndTransactionManager.rollBackTransaction();
            ex.printStackTrace();
            approved = null;
        }
        return approved;
    }

    /**
     * Tests for conditions under which user must login.  User checked for not already logged in.  If logged in,
     * no problem. Otherwise, check requested projects against secured projects.  Must not use the EJB for this.
     *
     * @param user checked for non-guest.
     * @param session an output: may need to put things there.
     * @param parameters from request from user.
     * @return T=need to login
     */
    public boolean isLoginRequired(
            String user,
            Map<String, Object> session,
            Map<String, Object> parameters) {

        boolean rtnVal = false;

        logger.debug("Remote user is " + (user == null ? "undefined" : user));
        if ( user == null ) {
            try {
                // Get the list of secured projects currently in the database.
                List<String> securedProjectNameList = getSecuredProjectNames();
                if ( securedProjectNameList != null  &&  securedProjectNameList.size() > 0 ) {

                    // There is an array of values for any HTTP parameter. However, within each projectNames
                    // array member, may be (probably is) a set of project names that are comma-separated,
                    // for URL brevity, rather than using a repeat of the parameter name.  Both multi-value
                    // mechanisms are taken into account here.
                    String[] requestedProjectsArray = (String[])parameters.get( "projectNames" );
                    if ( requestedProjectsArray != null ) {

                        for ( String projectsRequestParam: requestedProjectsArray ) {
                            String[] projectNamesPerRequestParm = projectsRequestParam.split(",");
                            for ( String requestedProjectName: projectNamesPerRequestParm ) {
                                if ( securedProjectNameList.contains( requestedProjectName ) ) {
                                    logger.debug("Forcing login");
                                    session.put( PARAMETERS_MAP_SESSION_KEY, parameters );

                                    rtnVal = true;
                                    break;  // Only one match is all that is needed to trigger a login.
                                }
                            }
                        }

                    }

                }

            } catch ( Exception ex ) {
                // Do not allow bypass in event of failure to retrieve list of projects.
                logger.error(
                        "Failed to do proper check of list of controlled projects.  Therefore forcing login. " +
                        ex.getMessage() );
                ex.printStackTrace();
                rtnVal = true;
            }
        }

        return rtnVal;
    }

    /** Helper to turn parameter/multivalued into a list. */
    private List<String> makeListOfMultivaluedParameters(String[] requestedParameterArray) {
        List<String> rtnVal = new ArrayList<String>();
        // Can both have multiple entries in the array, and multiple, comma-separated entries in each array member.
        for ( String commaSepParamList: requestedParameterArray ) {
            String[] projectNamesPerRequestParm = commaSepParamList.split(",");
            rtnVal.addAll( Arrays.asList(projectNamesPerRequestParm) );
        }

        return rtnVal;
    }

    private synchronized List<String> getSecuredProjectNames() throws Exception {
        List<String> projectNames;
        try {
            Session session = sessionAndTransactionManager.getSession();
            sessionAndTransactionManager.startTransaction();
            projectNames = projectDAO.getSecuredProjectNames(session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            ex.printStackTrace();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return projectNames;
    }
}