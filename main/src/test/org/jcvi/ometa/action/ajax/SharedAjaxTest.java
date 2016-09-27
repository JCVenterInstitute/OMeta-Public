package org.jcvi.ometa.action.ajax;

import org.jcvi.ometa.PSTTestAbstract;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * User: movence
 * Date: 10/10/14
 * Time: 2:10 PM
 * org.jcvi.ometa.action.sharedAjax
 */
public class SharedAjaxTest extends PSTTestAbstract {
    SharedAjax sharedAjax;

    @Before
    public void init() {
        this.getUserCrendential();
        sharedAjax = new SharedAjax(this.getReadBean("dev"));
        sharedAjax.setUserName("hkim");
    }

    @Test
    public void main() throws Exception {
        this.sa();
    }

    private void project() {
        sharedAjax.setType("Project");
        sharedAjax.runAjax();
        List<Map> data = sharedAjax.getAaData();
        for(Map<String, Object> map : data) {
            for (Map.Entry entry : map.entrySet()) {
                System.err.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    private void ea() {
        sharedAjax.setType("ea");
        sharedAjax.setEventId(Long.parseLong("1128491829520"));
        sharedAjax.setProjectName("TEST100");
        sharedAjax.setEventName("SampleRegistration");
        sharedAjax.runAjax();
        System.err.println(sharedAjax.getAaData());
    }

    private void sa() {
        sharedAjax.setType("sa");
        sharedAjax.setProjectId(9132678944004L);
        sharedAjax.setIds("9132678944004");
        sharedAjax.setEventId(9132575825719L);
        sharedAjax.setProjectName("TEST100-P3");
        sharedAjax.setEventName("ProjectUpdate");
        sharedAjax.setSubType("p");
        sharedAjax.runAjax();
        List aaData = sharedAjax.getAaData();

    }
}
