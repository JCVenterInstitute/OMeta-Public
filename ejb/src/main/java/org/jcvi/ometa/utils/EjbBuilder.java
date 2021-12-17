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

package org.jcvi.ometa.utils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/10/11
 * Time: 4:29 PM
 *
 * Helper class for the upload/template actions.
 */
public class EjbBuilder {

    private static final int NUM_RETRIES = 5;

    /**
     * Get the ejb of the type provided, for a remote client.
     */
    public <T> T getEjb(
            String ejbName, String server, String username, String password, Logger logger) {
        T pseb = null;
        int i = 0;
        for ( i = 0; i < NUM_RETRIES; i++ ) {
            try {
                logger.info("Getting EJB " + ejbName + " off server " + server + " for user " + username);

                Context ctx = getContext( server, username, password, ejbName, logger );
                pseb = (T)ctx.lookup( ejbName );
                if ( pseb == null ) {
                    throw new Exception(
                            "Found null instead of EJB " + ejbName + " on server " + server + " after " + NUM_RETRIES );
                }
                else {
                    logger.info("Found EJB " + ejbName + " on server " + server + " after " + i + " retries." );
                }
                if ( pseb != null ) {
                    break;
                }
            } catch ( Exception ex ) {
                logger.error("Failed to pickup EJB dependency: " + ejbName );
                throw new RuntimeException(ex);
            }
        }
        return pseb;
    }
    public <T> T getEjb(
            Context ctx, String ejbName, String server, String username, String password, Logger logger) {
        T pseb = null;
        int i = 0;
        for ( i = 0; i < NUM_RETRIES; i++ ) {
            try {
                logger.info("Getting EJB " + ejbName + " off server " + server + " for user " + username);

                pseb = (T)ctx.lookup( ejbName );
                if ( pseb == null ) {
                    throw new Exception(
                            "Found null instead of EJB " + ejbName + " on server " + server + " after " + NUM_RETRIES );
                }
                else {
                    logger.info("Found EJB " + ejbName + " on server " + server + " after " + i + " retries." );
                }
                if ( pseb != null ) {
                    break;
                }
            } catch ( Exception ex ) {
                logger.error("Failed to pickup EJB dependency: " + ejbName );
                throw new RuntimeException(ex);
            }
        }
        return pseb;
    }

    /** Gets an initial context already setup to do server login. */
    public Context getContext(
            String ejbServerName, String username, String password, String ejbName, Logger logger )
            throws Exception {

        // Only login if principal and credential are available.
        if ( username != null  &&  password != null ) {
            jbossStyleLogin( username, password );
        }

        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProps.put("jboss.naming.client.ejb.context", true);
        jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
        jndiProps.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        jndiProps.put(Context.PROVIDER_URL, ejbServerName);

        if ( username != null ) {
            jndiProps.put( Context.SECURITY_PRINCIPAL, username );
        }
        if ( password != null ) {
            jndiProps.put( Context.SECURITY_CREDENTIALS, password );
        }

        Context ctx = new InitialContext(jndiProps);
        String ejbServerProp = ejbName + ".Server";
        try {
            if ( ejbServerName == null  ||  ejbServerName.trim().length() == 0 ) {
                Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
                ejbServerName = props.getProperty( ejbServerProp );
            }
            logger.info( "Context points to server " + ejbServerName );
        } catch (Exception ex) {
            logger.error(ex);
            throw new NamingException("Property "+ejbServerProp+" could not be found. "+ex.getMessage());
        }

        return ctx;

    }

    private void jbossStyleLogin( String username, String password ) throws Exception {
        SecurityClient securityClient = SecurityClientFactory.getSecurityClient();
        securityClient.setSimple(username, password);
        securityClient.login();
    }

}
