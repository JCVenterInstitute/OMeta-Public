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

package org.jcvi.ometa.production_etl.email;

import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.EmailSender;
import org.jtc.common.util.property.PropertyHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 6/14/11
 * Time: 12:28 AM
 *
 * Test of like-named class.
 */
public class EmailSenderTest {
    private String notificationListStr;
    private EmailSender en;

    @Before
    public void setUp() {
        try {
            Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
            notificationListStr = props.getProperty( "PST.mail_help_to_address", null );
            System.err.println(notificationListStr);
            en = new EmailSender();
        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( ex.getMessage() );
        }
    }

    @Test
    public void notification() {
        try {
            RuntimeException re = new RuntimeException("ONLY A TEST:  Does the notification work.");
            //en.send( "help", re.toString(), re.toString() );

            System.out.println("Expect an email to be sent to " + notificationListStr);
        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }
}


