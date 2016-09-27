package org.jcvi.ometa.action;

import org.apache.commons.io.IOUtils;
import org.jcvi.ometa.PSTTestAbstract;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * User: movence
 * Date: 10/7/14
 * Time: 12:14 PM
 * org.jcvi.ometa.action
 */
public class EventLoaderTest extends PSTTestAbstract {
    EventLoader eventLoader;
    String testPath;

    @Before
    public void before() {
        this.getUserCrendential();
        eventLoader = new EventLoader(this.getReadBean("dev"), this.getWriteEjb("dev"));
    }

    @Test
    public void main() throws Exception {
        this.template();
    }

    private void template() throws Exception {
        eventLoader.setProjectId(9132678944004L);
        eventLoader.setProjectName("TEST100-P3");
        eventLoader.setEventId(1129032704287L);
        eventLoader.setEventName("SampleUpdate");
        eventLoader.setSampleName("TEST100-P3-S1");
        eventLoader.setIds("9132749459159,9132756459139");
        eventLoader.setJobType("template_c");
        eventLoader.execute();

        List<String> lines = IOUtils.readLines(eventLoader.getDataTemplateStream());
        for(String line : lines) {
            System.out.println(line + "\n");
        }
    }
}
