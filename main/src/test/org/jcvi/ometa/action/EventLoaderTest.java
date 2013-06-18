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
import org.jcvi.ometa.model.GridBean;
import org.jcvi.ometa.utils.Constants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 5/9/13
 * Time: 10:00 AM
 */
public class EventLoaderTest extends PSTTestAbstract {
    EventLoader eventLoader;
    String filePath;

    @Before
    public void before() {
        eventLoader = new EventLoader(this.getReadBean("dev"), null);
        eventLoader.setEventName(Constants.EVENT_SAMPLE_REGISTRATION);
        eventLoader.setProjectName("TEST100");
    }

    @Test
    public void ordered() throws Exception {
        template();
        file();
    }

    public void template() throws Exception {
        eventLoader.setJobType("template");
        assertEquals(eventLoader.execute(), Constants.FILE_DOWNLOAD_MSG);

        StringWriter writer = eventLoader.getOutputWriter();
        assertNotNull(writer);

        filePath = "/Users/hkim/Downloads/OEMTA/"+eventLoader.getDownloadFileName();
        assertNotNull(filePath);
        System.out.println(filePath);
        //FileUtils.writeStringToFile(new File(filePath), writer.toString());
    }

    public void file() throws Exception {
        eventLoader.setJobType("file");
        eventLoader.setCsvUploadFile(new File(filePath));
        eventLoader.setCsvUploadFileName(filePath.substring(filePath.lastIndexOf("/")));
        assertEquals(eventLoader.execute(), eventLoader.SUCCESS);
        assertEquals(eventLoader.getJobType(), "grid");

        List<GridBean> gridList = eventLoader.getGridList();
        assertTrue(gridList!=null && gridList.size()>0);
        for(GridBean bean : gridList) {
            System.out.printf("%s, %s, %s", bean.getSampleName(), bean.getParentSampleName(), bean.getSamplePublic());
        }

    }
}
