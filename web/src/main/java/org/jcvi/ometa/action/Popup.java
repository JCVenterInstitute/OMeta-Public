package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.model.ProjectAttribute;
import org.jcvi.ometa.model.ProjectMetaAttribute;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * User: hkim
 * Date: 7/9/13
 * Time: 8:30 AM
 * org.jcvi.ometa.action
 * This is a generic action for opening a popup. "t" parameter decides which popup this class should open.
 */
public class Popup extends ActionSupport {
    private Logger logger = LogManager.getLogger(Popup.class);

    private ReadBeanPersister readPersister;

    private String t; //type

    private String projectName;
    private String eventName;
    private String sampleName;

    private Long projectId;
    private Long eventId;
    private Long sampleId;

    private String ids;

    private List<LookupValue> eventTypeList;

    public Popup() {
        readPersister = new ReadBeanPersister();
    }

    public String run() {
        String rtnVal = SUCCESS;

        if(t.equals("sel_e")) {
            try {
                this.eventTypeList = readPersister.getEventTypesForProject(this.projectId);
            }  catch (Exception ex) {
                logger.error("Exception in POPUP : " + ex.toString());
                ex.printStackTrace();
            }

            rtnVal = "SELECT_EXPORT";
        } else if(t.startsWith("projectDetails")) {
            try {
                List<ProjectAttribute> projectAttributes = readPersister.getProjectAttributes(projectId);
                StringBuilder idsBuilder = new StringBuilder();
                String delim = "";
                for (ProjectAttribute pa : projectAttributes) {
                    LookupValue tempLookupValue = pa.getMetaAttribute().getLookupValue();
                    if (tempLookupValue != null && tempLookupValue.getName() != null) {
                        String tempLookupValueName = tempLookupValue.getName();
                        if(idsBuilder.indexOf(tempLookupValueName) < 0 && !tempLookupValueName.equals(Constants.ATTR_PROJECT_NAME)) {
                            idsBuilder.append(delim).append(tempLookupValueName.replaceAll("_", " "));
                            delim = ",";
                        }
                    }
                }
                this.ids = idsBuilder.toString();
                rtnVal = "PROJECT_DETAIL" + (t.endsWith("_pop") ? "_POP" : "");
            } catch (Exception ex) {
                logger.error("Exception in POPUP : " + ex.toString());
                ex.printStackTrace();
            }
        }
        return rtnVal;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public List<LookupValue> getEventTypeList() {
        return eventTypeList;
    }

    public void setEventTypeList(List<LookupValue> eventTypeList) {
        this.eventTypeList = eventTypeList;
    }
}
