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

package org.jcvi.ometa.db_interface;

import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.ProjectAttribute;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.model.SampleAttribute;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/14/11
 * Time: 3:01 PM
 *
 * Supports queries by presentation tier.
 */
public class MockWebDataBeanPersister { //implements WebDataFacadeI {

    public static final String PROJECT_NAME = "Werewolf";
    private static final long PROJECT_ID = 1;
    public static final String SAMPLE_NAME = "Werewolf Cuticle";
    private static final long SAMPLE_ID = 2;

    private static final String SAMPLE_ATT_NAME_1 = "Organism Name";
    private static final String SAMPLE_ATT_VAL_1 = "Common European Lycanthrope";
    private static final String SAMPLE_ATT_NAME_2 = "Estimated Genome Size";
    private static final String SAMPLE_ATT_VAL_2 = "3000000000";
    private static final String SAMPLE_ATT_NAME_3 = "Gene Forge Upload Date";

    private static final String PROJECT_ATT_NAME_1 = "Project Description";
    private static final String PROJECT_ATT_VAL_1 = "The chameleon factor...sometimes called lycanthropy.";
    private static final String PROJECT_ATT_NAME_2 = "Grant Name";
    private static final String PROJECT_ATT_VAL_2 = "Hammer Films Endowment";
    private static final String PROJECT_ATT_NAME_3 = "Grant Award Date";

    private static final Date TODAYS_DATE = new Date( new java.util.Date().getTime() );

    // Main to test, only.
    public static void main(String[] args) throws Exception {
        new MockWebDataBeanPersister().doTest();
    }

    // To test only.
    void doTest() throws Exception {
        Map<String,ProjectAttribute> mpa = getProjectAttributes(PROJECT_NAME);
        List<Sample> lsp = getSamplesForProject( PROJECT_NAME );
        Map<String,SampleAttribute> msa = getSampleAttributes(SAMPLE_NAME);
        List<Event> lep = getEventsForProject( PROJECT_NAME );

        // Try the dump.
        for ( String key: mpa.keySet() ) {
            ProjectAttribute projA = mpa.get(key);
            Object toPrint = null;
            if ( projA.getAttributeDateValue() != null ) {
                toPrint = projA.getAttributeDateValue();
            }
            else if ( projA.getAttributeFloatValue() != null ) {
                toPrint = projA.getAttributeFloatValue();
            }
            else if ( projA.getAttributeStringValue() != null ) {
                toPrint = projA.getAttributeStringValue();
            }
            out.println( toPrint );
        }
    }

    public Map<String,ProjectAttribute> getProjectAttributes(String projectName) throws Exception {
        Map<String,ProjectAttribute> rtnList = new HashMap<String,ProjectAttribute>();
        ProjectAttribute pa = new ProjectAttribute();
        pa.setProjectId(PROJECT_ID);
        pa.setAttributeStringValue(PROJECT_ATT_VAL_1);
        rtnList.put( PROJECT_ATT_NAME_1, pa );

        pa = new ProjectAttribute();
        pa.setProjectId( PROJECT_ID );
        pa.setAttributeStringValue(PROJECT_ATT_VAL_2);
        rtnList.put( PROJECT_ATT_NAME_2, pa );

        pa = new ProjectAttribute();
        pa.setProjectId(PROJECT_ID);
        pa.setAttributeDateValue(TODAYS_DATE);
        rtnList.put( PROJECT_ATT_NAME_3, pa );

        return rtnList;
    }

    public List<Sample> getSamplesForProject(String projectName) throws Exception {
        List<Sample> rtnList = new ArrayList<Sample>();
        for(int i=0;i<5; i++) {
            Sample s = new Sample();
            s.setProjectName(projectName);
            s.setSampleName(SAMPLE_NAME + i);
            s.setCreationDate(TODAYS_DATE);
            s.setSampleId(SAMPLE_ID);
            s.setProjectId( PROJECT_ID );
            rtnList.add( s );
        }
        return rtnList;
    }

    public Map<String,SampleAttribute> getSampleAttributes(String sampleName) throws Exception {
        Map<String,SampleAttribute> rtnList = new HashMap<String,SampleAttribute>();
        SampleAttribute sa = new SampleAttribute();
        sa.setProjectId(PROJECT_ID);
        sa.setSampleId(SAMPLE_ID);
        sa.setAttributeStringValue(SAMPLE_ATT_VAL_1);
        rtnList.put(SAMPLE_ATT_NAME_1, sa);

        sa = new SampleAttribute();
        sa.setProjectId( PROJECT_ID );
        sa.setSampleId( SAMPLE_ID );
        sa.setAttributeStringValue(SAMPLE_ATT_VAL_2);
        rtnList.put(SAMPLE_ATT_NAME_2, sa);

        sa = new SampleAttribute();
        sa.setProjectId( PROJECT_ID );
        sa.setSampleId( SAMPLE_ID );
        sa.setAttributeDateValue(TODAYS_DATE);
        rtnList.put(SAMPLE_ATT_NAME_3, sa);

        return rtnList;
    }

    public List<Event> getEventsForProject(String projectName) throws Exception {
        List<Event> rtnList = new ArrayList<Event>();
        Event pe = new Event();
        pe.setEventName("Project Kickoff");
        pe.setEventValue("" + TODAYS_DATE );
        return rtnList;
    }

}
