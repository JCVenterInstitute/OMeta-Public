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

package org.jcvi.ometa.interceptor.struts2;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ValidationAware;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.struts2.interceptor.ServletConfigInterceptor;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.interceptor.InterceptorHelper;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 6/7/11
 * Time: 10:43 AM
 *
 * Intercepts contexts bound for actions which need to restrict access based on user
 * rights/roles, specifically regarding access by project. Here, will direct to an
 * error page if user proves to be disallowed for any project, and will test for
 * read (View) permissions.
 */
public class ReadOnlyAllOrNothingAuthInterceptor implements Interceptor {

    private boolean isInitialized = false;
    private Logger logger = LogManager.getLogger( ReadOnlyAllOrNothingAuthInterceptor.class );
    private InterceptorHelper interceptorHelper;

    /** Special resources requested. */
    public void init() {
    }

    public void destroy() {
    }

    /**
     * May forward to a login page, based on user, and their request.  May forward to forbidden landing page.
     *
     * @param invocation Standard parameters for interceptor.
     * @return either result of invocation, or a handoff to a login.
     * @throws Exception thrown by called methods.
     */
    public String intercept( ActionInvocation invocation ) throws Exception {
        lazyInit();

        String rtnVal = "error";

        ActionContext invocationContext = invocation.getInvocationContext();
        Map<String,Object> session = invocationContext.getSession();
        HttpServletRequest request = (HttpServletRequest) invocationContext.get(
                ServletConfigInterceptor.HTTP_REQUEST
        );
        String user = request.getRemoteUser();

        if ( user == null ) {
            // Check: any reason to make them log in?
            if ( interceptorHelper.isLoginRequired( user, session, invocationContext.getParameters() ) ) {
                // Starting entry path.  User has not yet logged into the service.  Forwarding to login.
                rtnVal = "login";
            }
            else {
                rtnVal = invocation.invoke();
            }
        }
        else {
            // NOTE: calling without any notion of modifying user's output.  Just prevent or don't.
            logger.debug( "Getting list of approved projects." );
            List<String> approvedProjects =
                    interceptorHelper.getApprovedProjects( user, invocationContext.getParameters() );

            if ( approvedProjects != null ) {
                logger.debug( "User may pass, unhindered." );
                // Past-the-login entry path, and the authorization.
                //  "These aren't the droids we're looking for.  Move along."
                rtnVal = invocation.invoke();
            }
            else {
                logger.debug( "Some project has been denied to user " + user );
                addActionError( invocation, Constants.DENIED_USER_VIEW_MESSAGE );
                rtnVal = Constants.FORBIDDEN_ACTION_RESPONSE;
            }
        }

        return rtnVal;
    }

    private void addActionError(ActionInvocation invocation, String message) {
		Object action = invocation.getAction();
		if(action instanceof ValidationAware) {
			((ValidationAware) action).addActionError(message);
		}
	}

    private void lazyInit() {
        if ( ! isInitialized ) {
            interceptorHelper = new InterceptorHelper( logger );
            interceptorHelper.setAccessLevel( AccessLevel.View );
            interceptorHelper.setMissingProjectsResponse( ResponseToFailedAuthorization.ThrowException );
            isInitialized = true;
        }
    }

}
