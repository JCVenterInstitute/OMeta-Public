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

package org.jcvi.ometa.upload_action;

import com.opensymphony.xwork2.Action;
import org.apache.log4j.Logger;
import org.jcvi.ometa.action.Downloader;
import org.jcvi.ometa.action.EventTemplateMaker;
import org.jcvi.ometa.action.WebLoader;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.hibernate.dao.StandaloneSessionAndTransactionManager;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.EjbBuilder;
import org.jcvi.ometa.utils.LoginDialog;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;
import org.junit.*;

import java.io.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static java.lang.System.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/19/11
 * Time: 5:58 PM
 * Tests the action classes in the same package as this.
 */
public class UploadActionsTest {
    protected static final String INNOCUOUS_FILE = "build/initial_db_load/event_def/README.txt";
    protected static final String WGS_EVENT_NAME = "WGS";
    //"build/hibernate/dev_hibernate.cfg.xml";

    @Before
    public void setUp() {

    }

    /**
     * Tests that the file can be read off the downloader.
     */
    @Test
    public void testDownloader() {
        Downloader downloader = new Downloader();
        File file = new File( INNOCUOUS_FILE );
        downloader.setFilename( file.getAbsolutePath() );
        String result = downloader.execute();
        if ( result != Downloader.SUCCESS ) {
            Assert.fail("ERROR: unsuccessful execution " + result + " " + file.getAbsolutePath() );
        }
        InputStream fis = downloader.getFileInputStream();
        BufferedReader br = new BufferedReader( new InputStreamReader( fis ) );
        StringBuilder sb = new StringBuilder();
        String inline = null;
        try {
            while ( null != ( inline = br.readLine( ) ) ) {
                sb.append( inline );
            }

            br.close();
        } catch ( Exception ex ) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * This will test the template maker action.
     */
    @Test
    public void testEventTemplateMaker() {
        Properties props = PropertyHelper.getHostnameProperties( Constants.PROPERTIES_FILE_NAME );
        StandaloneSessionAndTransactionManager sessionManager =
                new StandaloneSessionAndTransactionManager( props, Constants.DEVELOPMENT_DATABASE  );
        ReadBeanPersister readPersister = new ReadBeanPersister( sessionManager );
        EventTemplateMaker maker = new EventTemplateMaker( readPersister );
        String populateResult = maker.execute();
        if ( populateResult != Action.SUCCESS ) {
            Assert.fail("ERROR: failed to run initial 'populate' step of EventTemplateMaker.  Got " + populateResult);
        }
        List<String> projectNames = maker.getProjectNameList();

        maker.setProjectName( projectNames.get( 0 ) );
        String result = maker.execute();
        if ( result != Action.SUCCESS ) {
            Assert.fail("ERROR: failed to push template maker past event setter.  Got " + result);
        }
        createTemplate( maker, WGS_EVENT_NAME, Boolean.TRUE );
        createTemplate( maker, "NoSuchEvent", Boolean.FALSE );
    }

    @Test
    public void testWebLoader() {
        try {
            // Step one: create an uploadable file.
            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime( now );
            DateFormat dfmt = DateFormat.getDateTimeInstance();
            dfmt.setCalendar( cal );
            String valueDate = ModelValidator.US_SLASHED_DATE_TIME_FMT.format( now );
            String fileContent =
                    "ProjectName\tSampleName\tAttributeName\tAttributeValue\n" +
                            "MRSA\tgsa21172\twgs accession\tACC_" + now.getTime() + "\n" +
                            "MRSA\tgsa21172\twgs date\t" + valueDate +"\n" +
                            "MRSA\tgsa21172\twgs status\tTESTING";

            out.println("About to load this event:");
            out.println( fileContent );
            File f = File.createTempFile(this.getClass().getName(), ".tsv");
            f.deleteOnExit();
            PrintWriter pw = new PrintWriter( new FileWriter( f ) );
            pw.print(fileContent);
            pw.close();

            LoginDialog userPassProvider = new LoginDialog();
            userPassProvider.promptForLoginPassword( "Test Web Loader" );
            String username = userPassProvider.getUsername();
            String password = userPassProvider.getPassword();

            if ( username == null  ||  password == null ) {
                Assert.fail("ERROR: You must enter a username and a password.");
            }

            // Set two: submit to loader.
            ProjectSampleEventWritebackBusiness pseb = (new EjbBuilder()).getEjb(
                    UploadActionDelegate.EJB_NAME,
                    "jnp://localhost:1399",
                    username,
                    password,
                    Logger.getLogger(this.getClass().getName())
            );
            WebLoader webLoader = new WebLoader( pseb );
            webLoader.setUploadFile( f.getAbsolutePath() );
            webLoader.setEventName( WGS_EVENT_NAME );
            String result = webLoader.execute();
            if ( result != Action.SUCCESS ) {
                Assert.fail(String.format(
                        "ERROR: Failed to upload the temporary file: %s and create event %s. Got %s.\n",
                        f.getAbsolutePath(), WGS_EVENT_NAME, result));
            }
            else {
                out.println( String.format( "Successfully loaded %s.  Please check database.", f.getAbsolutePath()) );
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

    /** Template-creational method.  Will be capable of testing should-pass or should-fail scenarios. */
    private void createTemplate(EventTemplateMaker maker, String eventName, Boolean expectSuccess) {
        maker.setProjectName("MRSA");
        maker.setEventName( eventName );
        maker.setSampleName( "gsa21172" );
        eventName = maker.getEventName();
        maker.setEventName( eventName );
        String finalResult = maker.execute();

        // Looking at template creations that expect to pass.
        if ( expectSuccess  &&  finalResult != Constants.FILE_DOWNLOAD_MSG ) {
            Assert.fail(
                    "ERROR: failed to get final successful result of template creation for event " +
                            eventName +
                            "  Got " +
                            finalResult);
        }
        else if (finalResult == Constants.FILE_DOWNLOAD_MSG) {
            String fn = maker.getDownloadFileName();
            out.println( "Would produce this file: " + fn );
            try {
                BufferedReader br = new BufferedReader( new InputStreamReader( maker.getInputStream() ) );
                String inline = null;
                out.println("----------------------File content---------------------");
                while ( null != ( inline = br.readLine() ) ) {
                    out.println( inline );
                }
                br.close();
            } catch ( Exception ex ) {
                ex.printStackTrace();
                Assert.fail( ex.getMessage() );
            }
        }

        // Looking at template creations that should not possibly pass.
        if ( ! expectSuccess  &&  finalResult != Constants.FAILURE_MSG ) {
            Assert.fail(
                    "ERROR: failed to get final failure result of template creation for event " +
                            eventName +
                            ".  Got " +
                            finalResult);

        }
    }

    @After
    public void tearDown() {
    }
}
