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
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.PresentationActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/26/11
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class SharedAjax extends ActionSupport implements IAjaxAction {
    private Logger logger = Logger.getLogger(SharedAjax.class);

    private ProjectSampleEventPresentationBusiness psept;

    private List aaData;

    private Long projectId;
    private Long sampleId;
    private Long eventId;
    private String type;
    private String subType;
    private String sampleLevel;
    private String projectName;
    private String eventName;

    public SharedAjax() {
        PresentationActionDelegate pdeledate = new PresentationActionDelegate();
        psept = pdeledate.initializeEjb(logger, psept);
    }


    public String runAjax() {
        String returnValue = ERROR;
        String userName = ServletActionContext.getRequest().getRemoteUser();
        int isEdit = 0;

        try {
            if("Project".equals(type)) {
                aaData = new ArrayList<Map>();

                Project currProject = psept.getProject(projectId);
                projectName = currProject.getProjectName();

                List<String> projectNamesList = new ArrayList<String>();
                projectNamesList.add(projectName);

                try { //check view permission
                    psept.getAuthorizedProjectNames(projectNamesList, userName, ResponseToFailedAuthorization.ThrowException, AccessLevel.View, QueryEntityType.Project);
                } catch (IllegalAccessException iaex) {
                    aaData = null;
                    return SUCCESS;
                }

                Map<String, Object> projectMap = new HashMap<String, Object>();
                int editable = 0;
                try { //check edit permission
                    psept.getAuthorizedProjectNames(projectNamesList, userName,ResponseToFailedAuthorization.ThrowException, AccessLevel.Edit, QueryEntityType.Project);
                    editable = 1;
                } catch (IllegalAccessException iaex) {
                    editable = 0;
                }
                projectMap.put("editable", editable);

                projectMap.put("Project Name", projectName);
                projectMap.put("Project Registration", ModelValidator.PST_DEFAULT_DATE_FORMAT.format(currProject.getCreationDate()));

                List<ProjectAttribute> projectAttributes = psept.getProjectAttributes(projectId);
                for (ProjectAttribute projAttr : projectAttributes) {
                    LookupValue tempLookupValue = projAttr.getMetaAttribute().getLookupValue();
                    Object attrValue = ModelValidator.getModelValue(tempLookupValue, projAttr);
                    if(tempLookupValue!=null && tempLookupValue.getName()!=null) {
                        if (tempLookupValue.getName().toLowerCase().contains("status") && attrValue!=null && attrValue.getClass() == java.lang.Integer.class)
                            attrValue = (Integer)attrValue==0?"Ongoing":"Completed";
                        projectMap.put(tempLookupValue.getName(), attrValue);
                    }
                }
                aaData.add(projectMap);
            } else if ("Sample".equals(type)) {
                aaData = new ArrayList<Map>();

                List<Sample> samples;
                if (sampleId != null && sampleId != 0) {
                    if(sampleLevel!=null && !sampleLevel.equals("1")) {
                        samples = psept.getChildSamples(sampleId);
                    } else {
                        samples = new ArrayList<Sample>();
                        samples.add(psept.getSample(sampleId));
                    }
                } else
                    samples = psept.getSamplesForProject(projectId);

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
                aaData = psept.getEventTypesForProject(projectId);
            } else if ("MetaAttributes".equals(type)) {
                aaData = new ArrayList<Map>();
                Map<String, List> containerMap = new HashMap<String, List>();

                List<Long> projectIds = new ArrayList<Long>();
                projectIds.add(projectId);

                if (subType == null)
                    subType = "A";
                if (subType.equals("P") || subType.equals("A")) {
                    List<ProjectMetaAttribute> allProjectMetaAttributes = psept.getProjectMetaAttributes(projectIds);
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
                    List<SampleMetaAttribute> allSampleMetaAttributes = psept.getSampleMetaAttributes(projectIds);
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
                    List<EventMetaAttribute> allEventMetaAttributes = psept.getEventMetaAttributes(projectIds);
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
            } else if ("EventSpecificAttrs".equals(type)) {
                aaData = psept.getEventMetaAttributes(projectName, eventName);
            } else if ("changeEventStatus".equals(type)) {
                Editor editor = new Editor();
                String resultVal = editor.eventEditProcess(eventId);

                aaData = new ArrayList<String>();
                aaData.add(resultVal);
            } else
                throw new Exception("undefined AJAX action for (" + type + ")");

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            ex.printStackTrace();
        }

        return returnValue;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<ModelBean> getAaData() {
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
}
