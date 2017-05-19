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

import org.apache.activemq.artemis.utils.JNDIUtil;
import org.apache.log4j.Logger;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

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
    private Logger logger = Logger.getLogger(JNDIUtil.class);

    private ReadBeanPersister readPersister;
    /**
     * C'tor will find all info required to do searches.
     */
    public JndiUserInfo() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    /**
     * Sets up a user info bean by doing a lookup in JNDI.
     *
     * @param userID whose info to find.
     * @return fully-populated bean.
     */
    public UserInfoBean getUserInfo( String userID ) {
        UserInfoBean bean = new UserInfoBean();
        try {
            Actor actor = readPersister.getActorByUserName(userID);
            if(actor != null) {
                bean.setUserFirstName(actor.getFirstName());
                bean.setUserLastName(actor.getLastName());
                bean.setRelativeDistinguishedName(actor.getFirstName()+ " " + actor.getLastName());
            }

            bean.setAdmin(readPersister.isUserAdmin(userID));

        } catch ( Exception ex ) {
            logger.error(ex.getMessage());
            ex.printStackTrace();  // FOR NOW
        } finally {
        }

        return bean;

    }
}
