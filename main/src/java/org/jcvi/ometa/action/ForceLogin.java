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

package org.jcvi.ometa.action;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/19/11
 * Time: 1:54 PM
 *
 * Brief redirector.  An anchor point for securing entry.
 */

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.jcvi.ometa.interceptor.InterceptorHelper;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.*;

public class ForceLogin extends ActionSupport implements SessionAware {
    private Logger logger = Logger.getLogger(ForceLogin.class);
    private Map<String,Object> session;
    private String projectNames;
    private String attributes;

    public ForceLogin() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
    }

    public String silentRedirect() {
        getForwardingParameters();
        return SUCCESS;
    }

    public void setSession( Map<String,Object> session ) {
        this.session = session;
    }

    /** Provide parameters in a URL-friendly manner. */
    public void setProjectNames( String projectNames ) {
        this.projectNames = projectNames;
    }
    public String getProjectNames() {
        return projectNames;
    }
    public void setAttributes( String attributes ) {
        this.attributes = attributes;
    }
    public String getAttributes() {
        return attributes;
    }

    /** Need to provide params as a pass-along to next stage. */
    private void getForwardingParameters() {
        if ( session.containsKey( InterceptorHelper.PARAMETERS_MAP_SESSION_KEY ) ) {
            // Alternate entry path: user has been forwarded to the target Action, after being forced
            // to login already.  Restoring the parameters from the previous attempt.
            Map<String,Object> sessionVersionOfParameters =
                    (Map<String,Object>)session.get( InterceptorHelper.PARAMETERS_MAP_SESSION_KEY );
            session.remove( InterceptorHelper.PARAMETERS_MAP_SESSION_KEY );

            // Build up parameters.
            if ( projectNames == null || projectNames.trim().length() == 0 ) {
                logger.info("Using session values.");
                projectNames = ((String[])sessionVersionOfParameters.get( "projectNames" ))[0];
            }
            if ( attributes == null || attributes.trim().length() == 0 )
                attributes = ((String[])sessionVersionOfParameters.get( "attributes" ))[0];
            logger.info( "Projects and attributes are " + projectNames + ":" + attributes );

        }
        else {
            logger.info("Key not in session");
        }

    }

}
