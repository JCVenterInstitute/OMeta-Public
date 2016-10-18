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

package org.jcvi.ometa.action;

import org.jcvi.ometa.PSTTestAbstract;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.Project;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 10/11/12
 * Time: 3:59 PM
 */
public class ProductionStatusTest extends PSTTestAbstract {
    ProductionStatus action = new ProductionStatus(this.getReadBean("dev"));

    @Test
    public void main() {
        //this.doStatus();
        //this.doExcel();
        this.ajax();
    }

    private void ajax() {
        action.setProjectNames("DPCC_1000");
        action.setAttributes("attributes:Project Name,Collecting_Institution,Project_Code,Project_ID,Project_Title,Project_Description,Project_PI_1 First_Name,Project_PI_1 Last_Name,Project_PI_2 First_Name,Project_PI_2 Last_Name,");
        action.setiSortCol_0("2");
        action.setsSortDir_0("asc");
        action.runAjax();
    }

    private void doStatus() {
        ProductionStatus action = new ProductionStatus(this.getReadBean("dev"));
        action.setProjectNames("GATES-test");
        action.setAttributes("ALL");
        action.statusPage();
        assertTrue(1==1);
    }

    private void doExcel() throws Exception {
        String projectName = "GSC";
        ReadBeanPersister readBeanPersister = this.getReadBean("prod");

        Project parentProject = readBeanPersister.getProject(projectName);
        assertNotNull(parentProject);
        assertEquals(projectName, parentProject.getProjectName());

        List<Project> projects = readBeanPersister.getChildProjects(parentProject.getProjectId());
        assertNotNull(projects);
        assertTrue(projects.size()>0);

        List<Project> childProjects = readBeanPersister.getChildProjects(parentProject.getProjectId());
        String projectNames = "";

        for (Project aProject : childProjects) {
            projectNames+=","+aProject.getProjectName();
        }
        projectNames = projectNames.substring(1);

        ProductionStatus action = new ProductionStatus(readBeanPersister);
        action.setProjectNames(projectNames);
        action.statusExcel();
    }
}
