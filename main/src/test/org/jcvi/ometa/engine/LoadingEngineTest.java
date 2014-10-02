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

package org.jcvi.ometa.engine;

import org.jcvi.ometa.DeleteContainer;
import org.jcvi.ometa.PSTTestAbstract;
import org.jcvi.ometa.configuration.FileMappingSupport;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.ProjectMetaAttribute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/25/13
 * Time: 1:47 PM
 */
public class LoadingEngineTest extends PSTTestAbstract {
    LoadingEngineUsage usage;
    LoadingEngine engine;
    String testPath;
    ReadBeanPersister readBean;
    DeleteContainer deleter;
    String projectName;

    @Before
    public void init() throws Exception {
        this.getUserCrendential();
        usage = new LoadingEngineUsage();
        usage.setServerUrl("movence-pro.local:1399");
        usage.setUsername(this.userPassProvider.getUsername());
        usage.setPassword(this.userPassProvider.getPassword());

        readBean = this.getReadBean("dev");
        deleter = new DeleteContainer(getSessionManager("dev"));

        testPath = "/test";
        projectName = "TEST200";
    }

    @After
    public void clean() throws Exception {
        this.cleanup();
    }

    //building one big test to minimize user inputs
    @Test //(expected = FileNotFoundException.class)
    public void bigTest() throws Exception {
        this.testProject();
        this.testPMA();
    }

    private void testProject() throws Exception {
        String projectHeaders[] = {"ProjectName", "ParentProjectName", "ProjectLevel", "Public", "Secure"};
        String projectArgs[][] =  {
                {projectName, "", "1", "1", "0"}
        };

        //project load test
        //create project tsv file
        System.out.println("=== Creating project file");
        usage.setInputFilename(this.createFile(projectHeaders, projectArgs, FileMappingSupport.PROJECT_FILE_SUFFIX));
        assertTrue(usage.validate());

        System.out.println("=== Loading project file");
        engine = new LoadingEngine(usage);
        engine.dispatchByFilename();

        Project project = readBean.getProject(projectName);
        assertNotNull(project);
    }

    private void testPMA() throws Exception {
        String pmaHeaders[] = {"ProjectName", "DataType", "Required", "AttributeName", "AttributeDescription", "PossibleValues"};
        String pmaArgs[][] = {
                {projectName, "string", "T", "ProjectDescription"},
                {projectName, "string", "T", "GrantName"},
                {projectName, "string", "T", "ProjectGroup"}
                /*{projectName, "string", "T", "ProjectDescription", "desc"},
                {projectName, "string", "T", "GrantName", "grant"},
                {projectName, "string", "T", "ProjectGroup", "group"}*/
        };

        System.out.println("=== Creating PMA file");
        usage.setInputFilename(this.createFile(pmaHeaders, pmaArgs, FileMappingSupport.PROJECT_META_ATTRIBUTES_FILE_SUFFIX));
        assertTrue(usage.validate());

        System.out.println("=== Loading PMA file");
        engine = new LoadingEngine(usage);
        engine.dispatchByFilename();

        List<ProjectMetaAttribute> pmas = readBean.getProjectMetaAttributes(projectName);
        assertNotNull(pmas);
        assertTrue(pmas.size()>0 && pmas.size()==3);
        int i = 0;
        for(ProjectMetaAttribute pma : pmas) {
            assertEquals(pmaArgs[i++][3], pma.getAttributeName());
            assertTrue(pma.getRequiredDB() == 1);
        }
    }

    private String createFile(String[] headers, String[][] vals, String fileName) throws Exception {
        StringBuilder sb = new StringBuilder();
        for(String h : headers) {
            sb.append(h).append("\t");
        }
        sb.append("\n");
        for(String[] a : vals) {
            for(String b : a) {
                sb.append(b).append("\t");
            }
            sb.append("\n");
        }

        String filePath = testPath+fileName;
        FileWriter fw = new FileWriter(filePath);
        fw.append(sb.toString());
        fw.close();

        return filePath;
    }

    private void cleanup() throws Exception  {
        List<ProjectMetaAttribute> pmas = readBean.getProjectMetaAttributes(projectName);
        System.out.println("=== Deleting PMA");
        assertTrue(deleter.deletePMA(pmas));

        Project project = readBean.getProject(projectName);
        System.out.println("=== Deleting project");
        assertTrue(deleter.deleteProject(project));
    }
}
