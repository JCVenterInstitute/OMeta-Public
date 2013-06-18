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

import com.opensymphony.xwork2.Action;
import org.jcvi.ometa.PSTTestAbstract;
import org.jcvi.ometa.model.Group;
import org.jcvi.ometa.model.Project;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/27/12
 * Time: 3:05 PM
 */
@Ignore
public class ProjectSetupTest extends PSTTestAbstract {
    @Test
    public void testInit() throws Exception {
        /*request.setParameter("jobType", "insert");
        ActionProxy proxy = getActionProxy("/projectSetup.action");
        ProjectSetup action = (ProjectSetup)proxy.getAction();
        String result = proxy.execute();*/

        ProjectSetup action = new ProjectSetup(this.getReadBean("dev"), null);
        String result = action.projectSetup();
        List<Project> projectList = action.getProjectList();
        List<Group> groupList = action.getGroupList();
        assertTrue(projectList != null && projectList.size() > 0);
        assertTrue(groupList != null && groupList.size() > 0);
        assertEquals(result, Action.SUCCESS);
    }

    @Test
    public void testRun() throws Exception {
        ProjectSetup action = new ProjectSetup(this.getReadBean("dev"), this.getWriteEjb("dev"));
        assertEquals(action.projectSetup(), Action.SUCCESS);

        action.setJobType("insert");

        Project loadingProject = new Project();
        loadingProject.setProjectName("TEST111");
        loadingProject.setIsPublic(1);
        loadingProject.setIsSecure(0);
        long vg = 0;
        long eg = 0;
        for(Group g : action.getGroupList()) {
            if(g.getGroupNameLookupValue().getName().equals(VIEW_GROUP_NAME))
                vg = g.getGroupId();
            else if(g.getGroupNameLookupValue().getName().equals(EDIT_GROUP_NAME))
                eg = g.getGroupId();
        }
        loadingProject.setViewGroup(vg);
        loadingProject.setEditGroup(eg);
        fail();
    }
}
