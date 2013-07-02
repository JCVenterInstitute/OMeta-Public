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

package org.jcvi.ometa.action.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.action.Editor;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/26/11
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class SharedAjax extends ActionSupport implements IAjaxAction {
    private Logger logger = Logger.getLogger(SharedAjax.class);

    private ReadBeanPersister readPersister;

    private List aaData;

    private Long projectId;
    private Long sampleId;
    private Long eventId;
    private String type;
    private String subType;
    private String sampleLevel;
    private String projectName;
    private String eventName;
    private String err;

    private String userName;

    public SharedAjax() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public SharedAjax(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    public String runAjax() {
        String returnValue = ERROR;
        if(userName == null) {
            userName = ServletActionContext.getRequest().getRemoteUser();
        }

        try {
            if("Project".equals(type)) {
                aaData = new ArrayList<Map>();

                ProjectMap pMap = this.getProjectInformation(this.projectId, true);

                Map<String, Object> projectMap = new HashMap<String, Object>();
                projectMap.put("editable", pMap.isEditable()?1:0);

                projectMap.put("Project Name", pMap.getProject().getProjectName());
                projectMap.put("Project Registration", ModelValidator.PST_DEFAULT_DATE_FORMAT.format(pMap.getProject().getCreationDate()));

                List<ProjectAttribute> projectAttributes = readPersister.getProjectAttributes(this.projectId);
                for (ProjectAttribute projAttr : projectAttributes) {
                    LookupValue tempLookupValue = projAttr.getMetaAttribute().getLookupValue();
                    Object attrValue = ModelValidator.getModelValue(tempLookupValue, projAttr);
                    if(tempLookupValue!=null && tempLookupValue.getName()!=null) {
                        if(attrValue!=null) {
                            if (tempLookupValue.getName().toLowerCase().contains("status")
                                    && attrValue.getClass() == java.lang.Integer.class) {
                                attrValue = (Integer)attrValue==0?"Ongoing":"Completed";
                            } else if(attrValue.getClass() == Timestamp.class || attrValue.getClass() == Date.class) {
                                attrValue = CommonTool.convertTimestampToDate(attrValue);
                            }
                        }
                        projectMap.put(tempLookupValue.getName(), attrValue);
                    }
                }
                aaData.add(projectMap);
            } else if ("Sample".equals(type)) {
                aaData = new ArrayList<Map>();

                List<Sample> samples;
                if (sampleId != null && sampleId != 0) {
                    if(sampleLevel!=null && !sampleLevel.equals("1")) {
                        samples = readPersister.getChildSamples(sampleId);
                    } else {
                        samples = new ArrayList<Sample>();
                        samples.add(readPersister.getSample(sampleId));
                    }
                } else
                    samples = readPersister.getSamplesForProject(this.projectId);

                sampleLevel = sampleLevel == null ? "0" : sampleLevel;
                int intSampleLevel = Integer.parseInt(sampleLevel);
                for (Sample sample : samples) {
                    if (intSampleLevel == 0 || (sample.getSampleLevel() != null && sample.getSampleLevel() == intSampleLevel)) { //filter by sample level
                        Map<String, String> sampleMap = new HashMap<String, String>();
                        sampleMap.put("id", ""+sample.getSampleId());
                        sampleMap.put("name", sample.getSampleName());
                        aaData.add(sampleMap);
                    }
                }
            } else if ("Event".equals(type)) {
                aaData = readPersister.getEventTypesForProject(this.projectId);
            } else if ("MetaAttributes".equals(type)) { //get meta attributes for Event Report page
                aaData = new ArrayList<Map>();

                ProjectMap pMap = this.getProjectInformation(this.projectId, false);
                if(pMap.isViewable()) { //only if the user has view permission on a project
                    Map<String, List> containerMap = new HashMap<String, List>();
                    List<Long> projectIds = new ArrayList<Long>();
                    projectIds.add(this.projectId);

                    if (subType == null)
                        subType = "A";
                    if (subType.equals("P") || subType.equals("A")) {
                        List<ProjectMetaAttribute> allProjectMetaAttributes = readPersister.getProjectMetaAttributes(projectIds);
                        List<String> projectMetaList = new ArrayList<String>();
                        if (subType.equals("A"))
                            projectMetaList.add("Project Name");
                        for (ProjectMetaAttribute pma : allProjectMetaAttributes) {
                            if (!projectMetaList.contains(pma.getLookupValue().getName()))
                                projectMetaList.add(pma.getLookupValue().getName());
                        }
                        containerMap.put("project", projectMetaList);
                    }
                    if (subType.equals("S") || subType.equals("A")) {
                        List<SampleMetaAttribute> allSampleMetaAttributes = readPersister.getSampleMetaAttributes(projectIds);
                        List<String> sampleMetaList = new ArrayList<String>();
                        if (subType.equals("A")) {
                            sampleMetaList.add("Sample Name");
                            sampleMetaList.add("Parent Sample");
                        }
                        for (SampleMetaAttribute sma : allSampleMetaAttributes) {
                            if (!sampleMetaList.contains(sma.getLookupValue().getName()))
                                sampleMetaList.add(sma.getLookupValue().getName());
                        }
                        containerMap.put("sample", sampleMetaList);
                    }
                    if (subType.equals("E") || subType.equals("A")) {
                        List<EventMetaAttribute> allEventMetaAttributes = readPersister.getEventMetaAttributes(projectIds);
                        List<String> eventMetaList = new ArrayList<String>();
                        for (EventMetaAttribute ema : allEventMetaAttributes) {
                            //TODO hkim may need to remove "accession" constraint for more EMA choices
                            if (!eventMetaList.contains(ema.getLookupValue().getName())
                                    && ema.getLookupValue().getName().contains("accession"))
                                eventMetaList.add(ema.getLookupValue().getName());
                        }
                        containerMap.put("event", eventMetaList);
                    }
                    aaData.add(containerMap);
                } else {
                    Map<String, String> errorMap = new HashMap<String, String>(1);
                    errorMap.put("error", Constants.DENIED_USER_VIEW_MESSAGE);
                    aaData.add(errorMap);
                }
            } else if ("ea".equals(type)) { //attribute for an event
                aaData = readPersister.getEventMetaAttributes(projectName, eventName);
            } else if ("ces".equals(type)) { //Change Event Status
                Editor editor = new Editor();
                String resultVal = editor.eventEditProcess(eventId);

                aaData = new ArrayList<String>();
                aaData.add(resultVal);
            } else
                throw new Exception("undefined AJAX action for (" + type + ")");

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    private ProjectMap getProjectInformation(Long projectId, boolean includeEdit) throws Exception {
        ProjectMap pMap = null;
        Project project = readPersister.getProject(projectId);

        if(project!=null) {
            pMap = new ProjectMap();
            pMap.setProject(project);

            List<String> projectNamesList = new ArrayList<String>();
            projectNamesList.add(pMap.getProject().getProjectName());

            try { //check view permission
                readPersister.getAuthorizedProjectNames(
                        projectNamesList,
                        userName,
                        ResponseToFailedAuthorization.ThrowException,
                        AccessLevel.View,
                        QueryEntityType.Project);
                pMap.setViewable(true);
            } catch (IllegalAccessException iaex) {
                pMap.setViewable(false);
            }
            if(includeEdit) {
                try { //check edit permission
                    readPersister.getAuthorizedProjectNames(
                            projectNamesList,
                            userName,
                            ResponseToFailedAuthorization.ThrowException,
                            AccessLevel.Edit,
                            QueryEntityType.Project);
                    pMap.setEditable(true);
                } catch (IllegalAccessException iaex) {
                    pMap.setEditable(false);
                }
            }
        }
        return pMap;
    }

    private class ProjectMap {
        private Project project;
        private boolean viewable;
        private boolean editable;
        public Project getProject() { return project; }
        public void setProject(Project project) { this.project = project; }
        public boolean isViewable() { return viewable; }
        public void setViewable(boolean viewable) { this.viewable = viewable; }
        public boolean isEditable() { return editable; }
        public void setEditable(boolean editable) { this.editable = editable; }
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List getAaData() {
        return aaData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public String getSampleLevel() {
        return sampleLevel;
    }

    public void setSampleLevel(String sampleLevel) {
        this.sampleLevel = sampleLevel;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }
}
