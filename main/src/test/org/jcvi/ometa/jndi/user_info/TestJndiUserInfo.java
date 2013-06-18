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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.lang.System.out;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 8/26/11
 * Time: 2:05 PM
 *
 * Test for the production class of similar name.
 */
public class TestJndiUserInfo {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void gettingUserInfo() {
        try {
            JndiUserInfo infoGetter = new JndiUserInfo();
            UserInfoBean bean = infoGetter.getUserInfo( "janthony" );
            Assert.assertNotNull( "Null bean from JNDI/LDAP lookup.", bean );
            Assert.assertNotNull( "Null user first name ", bean.getUserFirstName() );
            Assert.assertNotNull( "Null user last name ", bean.getUserLastName() );

            out.println(
                    String.format(
                        "Found: firstname=%s   lastname=%s   fullname=%s    for userID=%s",
                            bean.getUserFirstName(),
                            bean.getUserLastName(),
                            bean.getRelativeDistinguishedName(),
                            bean.getUserID()
                    )
            );

        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }
}
