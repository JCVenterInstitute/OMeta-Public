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

package org.jcvi.ometa.configuration;

import org.jcvi.ometa.model.*;
import org.jcvi.ometa.validation.ModelValidator;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

import java.util.*;
import java.io.File;

import static java.lang.System.out;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 28, 2010
 * Time: 11:03:48 AM
 *
 * Load event-related data.  See if / when / where it fails.
 */
public class EventLoaderTest {
    private static final String EVENTS_FILE = "build/unit_test/test_werewolf/ProjectRegistration_EventAttributes.tsv";
    private static final String PROJECTS_FILE = "build/unit_test/test_werewolf/Projects.tsv";
    private static final String SAMPLE_FILE = "build/unit_test/test_werewolf/Sample.tsv";
    private static final String SAMPLE_META_ATTRIBUTES_FILE = "build/unit_test/test_werewolf/SampleMetaAttributes.tsv";
    private static final String PROJECT_META_ATTRIBUTES_FILE = "build/unit_test/test_werewolf/ProjectMetaAttributes.tsv";
    private static final String EVENT_META_ATTRIBUTES_FILE = "build/unit_test/test_werewolf/EventMetaAttributes.tsv";
    private static final String PROJECT_ATTRIBUTES_FILE = "build/unit_test/test_werewolf/ProjectAttributes.tsv";

    private EventLoader loader;
    private static final String SAMPLE_ATTRIBUTES_FILE = "build/unit_test/SampleAttributes.tsv";
    private final ModelValidator modelValidator = new ModelValidator();

    @Before
    public void setUp() {
        loader = new EventLoader();
    }

    @Test
    public void getProject() {

        try {
            boolean valid = true;
            File file = new File(PROJECTS_FILE);
            List<Project> beans = loader.getGenericModelBeans( file, Project.class );
            out.println( "Dumping Projects" );

            List<String> projectNames = new ArrayList<String>();

            StringBuilder combinedErrors = new StringBuilder();
            int rowNum = 0;

            for ( Project bean: beans ) {
                out.print( "Parent Project: '");
                out.print( bean.getParentProjectName() );
                out.print( "', Project: '");
                out.print( bean.getProjectName() );
                out.print( "', Project Level: '");
                out.print( bean.getProjectLevel() );
                out.print( "'");
                out.println();
                valid = modelValidator.validateProject(bean, projectNames, combinedErrors, rowNum);

            }

            if ( combinedErrors.length() > 0  ||  (! valid)) {
                Assert.fail( combinedErrors.toString() );
            }

        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( "Could not get projects: " + ex.getMessage() );
        }
    }

    @Test
    public void getSample() {
        try {
            File file = new File( SAMPLE_FILE );
            List<Sample> beans = loader.getGenericModelBeans( file, Sample.class );
            StringBuilder combinedErrors = new StringBuilder();
            int rowNum = 0;
            out.println("Dumping Samples");
            boolean valid = true;
            for ( Sample bean: beans ) {
                rowNum ++;

                // Look for required fields.
                boolean rowValid = modelValidator.validateSampleContents(bean, combinedErrors, rowNum);
                if ( rowValid ) {
                    out.print( "Sample Name: '" + bean.getSampleName() + "', ");
                    out.println( "Project Name: '" + bean.getProjectName() + "'" );                    
                }

                valid &= rowValid;

            }

            if ( combinedErrors.length() > 0 || (! valid) ) {
                Assert.fail( combinedErrors.toString() );
            }

        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( "Could not get samples: " + ex.getMessage() );
        }
    }

    @Test
    public void testSampleMetaAttributes() {
        try {

            Set<String> projectNames = getProjectNames();

            List<SampleMetaAttribute> beans = getSampleMetaAttributes();

            StringBuilder combinedErrors = new StringBuilder();
            int rowNum = 0;

            out.println("Dumping Sample Meta Attributes");
            for ( SampleMetaAttribute bean: beans ) {

                out.print( "Project: '");
                out.print( bean.getProjectName() );
                out.print( "', attribute name: '");
                out.print( bean.getAttributeName() );
                out.print( "', description: '" );
                out.print( bean.getDesc() );
                out.print( "', required: '");
                out.print( bean.isRequired() );
                out.print( "', possible values [if constrained]: '" );
                out.print( bean.getOptions() );
                out.print( "'" );
                out.println();

                modelValidator.validateSampleMetaAttributeContents(bean, projectNames, combinedErrors, rowNum);
                //if ( bean.getDesc() == null  ||  bean.getDesc().length() == 0 ) {
                //    combinedErrors.append( "Row number " + rowNum + " has no attribute description." );
                //}

            }

            if ( combinedErrors.length() > 0 ) {
                Assert.fail( combinedErrors.toString() );
            }

        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( "Could not get sample meta attributes: " + ex.getMessage() );
        }
    }

    @Test
    public void testProjectMetaAttributes() {
        try {
            // Get the project list -- make sure the meta attributes apply to the project.
            Set<String> projectNames = getProjectNames();

            List<ProjectMetaAttribute> beans = getProjectMetaAttributes();

            StringBuilder combinedErrors = new StringBuilder();
            int rowNum = 0;

            // Dump result.
            out.println("Dumping Project Meta Attributes");
            boolean valid = true;
            for ( ProjectMetaAttribute bean: beans ) {
                rowNum ++;
                if ( ! projectNames.contains( bean.getProjectName() ) ) {
                    combinedErrors.append( "Project name unknown: " + bean.getProjectName() );
                }

                out.print( "Project: '");
                out.print( bean.getProjectName() );
                out.print( "', attribute name: '");
                out.print( bean.getAttributeName() );
                out.print( "', description: '");
                out.print( bean.getDesc() );
                out.print( "', required: '");
                out.print( bean.isRequired() );
                out.print( "', possible values [if constrained]: '" );
                out.print( bean.getOptions() );
                out.print( "'" );
                out.println();
                valid &= modelValidator.validateProjectMetaAttributeContents(bean, combinedErrors, rowNum);

            }

            if ( combinedErrors.length() > 0 || (! valid) ) {
                Assert.fail( combinedErrors.toString() );
            }

        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( "Could not get project meta attributes: " + ex.getMessage() );
        }
    }

    /**
     * Reads file of events.  Checks whether each event is in the namespace of project, study or (in turn)
     * event, as described by the meta-attributes for each attribute type.
     */
    @Test
    public void testEvents() {
        try {
            // Collect all meta attributes so that proper namespaces may be detected for dealing with
            // the events that follow.
            Map<String,ProjectMetaAttribute> pmaBeans = makeMap( getProjectMetaAttributes() );
            Map<String,SampleMetaAttribute> smaBeans = makeMap( getSampleMetaAttributes() );
            Map<String,EventMetaAttribute> emaBeans = makeMap( getEventMetaAttributes() );

            File file = new File(EVENTS_FILE);
            List<Event> beans = loader.getGenericModelBeans( file, Event.class );
            for ( Event event: beans ) {
                String evName = event.getEventName();
                out.println( "Event for project " + event.getProjectName() );
                if ( pmaBeans.containsKey( evName ) ) {
                    // Passed test for project attribute.
                    out.println("Project attribute " + event.getEventName() + "=" + event.getEventValue() );
                }
                else if ( smaBeans.containsKey( evName ) ) {
                    out.println( "Event for sample " + event.getSampleName() );
                    // Passed test for study attribute.
                    out.println("Sample attribute " + event.getEventName() + "=" + event.getEventValue() );

                }
                else if ( emaBeans.containsKey( evName ) ) {
                    // Passed test for event attribute.
                    out.println("Event attribute " + event.getEventName() + "=" + event.getEventValue() );

                }
                else {
                    out.println(" Unknown event target: " + evName );
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( "Could not get events: " + ex.getMessage() );
        }
    }

    /**
     * Takes any list of a type that extends MetaAttributeModelBean, and marshal it into
     * a map of the meta attribute's name versus the original bean value.
     *
     * @param collection original list.
     * @param <T> some type that extends MetaAttributeModelBean.
     * @return map instead of list.                                                   
     */
    public <T extends MetaAttributeModelBean> Map<String,T> makeMap( List<T> collection ) {
        Map<String,T> rtnMap = new HashMap<String,T>();
        for ( T tBean: collection ) {
            String attributeName = ((MetaAttributeModelBean)tBean).getAttributeName();
            rtnMap.put( attributeName, tBean );
        }

        return rtnMap;
    }

    private List<EventMetaAttribute> getEventMetaAttributes() throws Exception {
        File file = new File(EVENT_META_ATTRIBUTES_FILE);
        List<EventMetaAttribute> beans = loader.getGenericModelBeans( file, EventMetaAttribute.class );
        return beans;
    }

    private List<SampleMetaAttribute> getSampleMetaAttributes() throws Exception {
        File file = new File(SAMPLE_META_ATTRIBUTES_FILE);
        List<SampleMetaAttribute> beans = loader.getGenericModelBeans( file, SampleMetaAttribute.class );
        return beans;
    }

    private List<ProjectMetaAttribute> getProjectMetaAttributes() throws Exception {
        File file = new File(PROJECT_META_ATTRIBUTES_FILE);
        List<ProjectMetaAttribute> beans = loader.getGenericModelBeans( file, ProjectMetaAttribute.class );
        return beans;
    }

    private Set<String> getSampleNames() throws Exception {
        Set<String> sampleNames = new HashSet<String>();
        File file = new File( SAMPLE_FILE );
        List<Sample> samples = loader.getGenericModelBeans( file, Sample.class );
        for ( Sample sample: samples ) {
            sampleNames.add( sample.getSampleName() );
        }

        return sampleNames;
    }

    private Set<String> getProjectNames() throws Exception {
        File file = new File( PROJECTS_FILE );
        List<Project> pBeans = loader.getGenericModelBeans( file, Project.class );
        Set<String> projectNames = new HashSet<String>();
        for ( Project project: pBeans ) {
            projectNames.add( project.getProjectName() );
        }
        return projectNames;
    }

}
