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

package org.jcvi.ometa.jndi.user_info;

import org.apache.log4j.Logger;
import org.jboss.ejb3.JndiUtil;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 8/26/11
 * Time: 1:58 PM
 *
 * Last modified by : hkim
 * date: 4/9/12
 *
 * Can get the user information, given user ID.
 */
public class JndiUserInfo {
    public static final String LDAP_URL = "JndiUserInfo.ldap_url";
    public static final String JNDI_CTX_FACTORY = "JndiUserInfo.jndi_ctx_factory";
    public static final String JNDI_BASE_DN = "JndiUserInfo.base_dn";
    public static final String GIVEN_NAME_ATTRIB = "JndiUserInfo.given_name_attrib";
    public static final String SURNAME_ATTRIB = "JndiUserInfo.surname_attrib";
    public static final String RELATIVE_DISTINGUISHED_NAME_ATTRIB = "JndiUserInfo.relative_distinguished_name_attrib";

    private Logger logger = Logger.getLogger(JndiUtil.class);

    private String ldapUrl;
    private String baseDn;
    private String givenNameAttrib;
    private String surnameAttrib;
    private String relativeDistinguishedNameAttrib;
    private String contextFactoryClassName;

    private ReadBeanPersister readPersister;
    /**
     * C'tor will find all info required to do searches.
     */
    public JndiUserInfo() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        ldapUrl = props.getProperty( LDAP_URL );
        baseDn = props.getProperty(JNDI_BASE_DN);
        givenNameAttrib = props.getProperty( GIVEN_NAME_ATTRIB );
        surnameAttrib = props.getProperty( SURNAME_ATTRIB );
        contextFactoryClassName = props.getProperty( JNDI_CTX_FACTORY );
        relativeDistinguishedNameAttrib = props.getProperty( RELATIVE_DISTINGUISHED_NAME_ATTRIB );

        readPersister = new ReadBeanPersister(props);
    }

    /**
     * Sets up a user info bean by doing a lookup in JNDI.
     *
     * @param userID whose info to find.
     * @return fully-populated bean.
     */
    public UserInfoBean getUserInfo( String userID ) {
        DirContext ctx = null;
        UserInfoBean bean = new UserInfoBean();
        try {
            ctx = getContext(ctx);

            String filter = String.format("uid=%s", userID);
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(baseDn, filter,constraints);

            if (results == null || !results.hasMore()) {
                throw new Exception();
            }

            SearchResult result = (SearchResult) results.next();
            Attributes attribs = result.getAttributes();

            bean.setUserFirstName( getAttribute( attribs, givenNameAttrib ) );
            bean.setUserLastName( getAttribute( attribs, surnameAttrib ) );
            bean.setRelativeDistinguishedName( getAttribute( attribs, relativeDistinguishedNameAttrib ) );
            bean.setUserID( userID );

            bean.setAdmin(readPersister.isUserAdmin(userID));

        } catch ( Exception ex ) {
            logger.error( "Failed to retrieve user info from JNDI server " + ldapUrl );
            // NOTE: we do not wish exceptions here to be propagated, just handled.
            ex.printStackTrace();  // FOR NOW

        } finally {
            if ( ctx != null ) {
                try {
                    ctx.close();
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        }

        return bean;

    }

    /** Gets the context for lookups. */
    private DirContext getContext(DirContext ctx) throws NamingException {
        Hashtable env = new Hashtable();
        env.put( Context.INITIAL_CONTEXT_FACTORY, contextFactoryClassName );
        env.put( Context.PROVIDER_URL, ldapUrl );
        ctx = new InitialDirContext(env);
        return ctx;
    }

    /** Handle the nitty-gritty of obtaining the named value from the attributes. */
    private String getAttribute( Attributes attributes, String attributeName ) throws NamingException {
        String rtnValue = "";
        if ( attributes != null  &&  attributes.get( attributeName ) != null ) {
            rtnValue = attributes.get( attributeName ).get().toString();
        }
        return rtnValue;
    }
}
