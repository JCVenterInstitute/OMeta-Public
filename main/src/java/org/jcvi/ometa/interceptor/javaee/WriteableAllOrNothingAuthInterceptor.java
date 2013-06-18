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

package org.jcvi.ometa.interceptor.javaee;

import org.apache.log4j.Logger;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.interceptor.InterceptorHelper;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/5/11
 * Time: 10:43 AM
 *
 * Intercepts contexts bound for actions which need to restrict access based on user
 * rights/roles, specifically regarding access by project. Here, will direct to an
 * error page if user proves to be disallowed for any project, and will test for
 * write (Edit) permissions.
 */
public class WriteableAllOrNothingAuthInterceptor {

    private boolean isInitialized = false;
    private Logger logger = Logger.getLogger( WriteableAllOrNothingAuthInterceptor.class );
    private InterceptorHelper interceptorHelper;

    @Resource
    private EJBContext context;

    /** Special resources requested. */
    public void init() {
    }

    /**
     * May throw exception if user is not logged in, and has rights to all they requested.
     *
     * @param invocationContext Standard parameters for interceptor.
     * @return result of proceeding with invocation.
     * @throws Exception thrown by called methods, or in event of security blockade.
     */
    @AroundInvoke
    public Object intercept( InvocationContext invocationContext ) throws Exception {
        lazyInit();

        Object rtnVal = null;

        String user = interceptorHelper.getAndTestLoggedInUser(context.getCallerPrincipal());
        List<? extends Object> approved = null;
        approved = interceptorHelper.checkAnnotatedMethodPermissions(invocationContext, user);

        if ( approved != null ) {
            logger.debug( "User may pass, unhindered." );
            // Past-the-login entry path, and the authorization.
            //  "These aren't the droids we're looking for.  Move along."
            rtnVal = invocationContext.proceed();
        }
        else {
            String message = "One or more projects have been denied to user " + user;
            logger.debug( message );
            throw new IllegalAccessError( message );
        }

        return rtnVal;
    }

    private void lazyInit() {
        if ( ! isInitialized ) {
            Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
            interceptorHelper = new InterceptorHelper( props, logger );
            interceptorHelper.setAccessLevel( AccessLevel.Edit );
            interceptorHelper.setMissingProjectsResponse( ResponseToFailedAuthorization.ThrowException );
            isInitialized = true;
        }
    }

}
