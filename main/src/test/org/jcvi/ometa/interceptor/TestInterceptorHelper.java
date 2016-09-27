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
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.hibernate.dao.StandaloneSessionAndTransactionManager;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

import static java.lang.System.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/19/11
 * Time: 4:01 PM
 *
 * Test the main-line counterpart.
 */
public class TestInterceptorHelper {
    protected static final String TEST_USER = "lfoster";
    protected static final String OPEN_TEST_PROJECT = "MRSA";
    protected static final String BARRED_PROJECT = "Werewolf";
    private Logger logger = Logger.getLogger(TestInterceptorHelper.class);
    private InterceptorHelper helper;
    @Before
    public void setUp() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        helper = new InterceptorHelper(
                new StandaloneSessionAndTransactionManager( props, Constants.DEVELOPMENT_DATABASE ),
                logger
        );
    }

    @Test
    public void testHelper() {
        try {
            StringBuilder errors = new StringBuilder();

            List approved = null;

            Type simpleType = String.class;
            approved = helper.getListOfApproved( OPEN_TEST_PROJECT, TEST_USER, simpleType, QueryEntityType.Project );
            out.println( "Testing accept " + OPEN_TEST_PROJECT );
            if ( approved == null ) {
                errors.append("ERROR: Failed to approve open challenge single project.\n");
            }

            out.println( "Testing reject " + BARRED_PROJECT );
            approved = helper.getListOfApproved( BARRED_PROJECT, TEST_USER, simpleType, QueryEntityType.Project );
            if ( approved != null && approved.size() != 0 ) {
                errors.append("ERROR: Failed to reject barred challenge single project.\n");
            }

            if ( errors.length() > 0 ) {
                Assert.fail( errors.toString() );
            }

        } catch ( Exception ex ) {
            Assert.fail( ex.getMessage() );
        }
    }

    @After
    public void tearDown() {

    }
}
