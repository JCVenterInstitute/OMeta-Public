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
import org.jcvi.ometa.helper.AttributeHelper;
import org.jcvi.ometa.helper.AttributePair;
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
    private String ids;
    private String type;
    private String subType;
    private String sampleLevel;
    private String projectName;
    private String eventName;
    private String filter;
    private String err;
    private String sampleVal;
    private int totalSampleCount;
    private int firstResult;
    private int maxResult;

    private String userName;

    private String projectPopupAttributes;

    public SharedAjax() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);

        this.projectPopupAttributes = props.getProperty(Constants.CONFIG_PROJECT_POPUP_ATTRS);
    }

    public SharedAjax(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    public String singleProject() {
        String returnValue = ERROR;

        try {
            aaData = new ArrayList<Map>();

            ProjectMap pMap = this.getProjectInformation(this.projectId, true);

            Map<String, Object> projectMap = new LinkedHashMap<String, Object>();

            projectMap.put(Constants.ATTR_PROJECT_NAME, pMap.getProject().getProjectName());

            List<LookupValue> allLV = readPersister.getEventTypesForProject(projectId);
            List<String> attributeList = new ArrayList<String>(0);
            Map<String, String> attributeType = new HashMap<String, String>(0);
            final List<String> sortedMetaAttributeNames = new ArrayList<String>(0);

            attributeType.put("Sample Name", "string");
            attributeType.put("Parent", "string");
            attributeType.put("User", "string");
            attributeType.put("Date", "date");

            for(LookupValue lv : allLV){
                String lv_name = lv.getName();

                if(lv_name.equals(Constants.EVENT_PROJECT_REGISTRATION)){
                    List<EventMetaAttribute> registrationEMAList = readPersister.getEventMetaAttributes(this.projectId, lv.getLookupValueId());
                    for(EventMetaAttribute ema : registrationEMAList) {
                        sortedMetaAttributeNames.add(ema.getLookupValue().getName());
                    }
                } else if(!lv_name.toLowerCase().contains("project")){
                    List<EventMetaAttribute> EMAList = readPersister.getEventMetaAttributes(this.projectId, lv.getLookupValueId());

                    for(EventMetaAttribute ema : EMAList) {
                        String tempMetaName = ema.getLookupValue().getName();
                        if (!attributeList.contains(tempMetaName)) {
                            attributeList.add(tempMetaName);
                            attributeType.put(tempMetaName, ema.getLookupValue().getDataType());
                        }
                    }
                }
            }
            //CommonTool.sortEventMetaAttributeByOrder(registrationEMAList);

            List<ProjectAttribute> projectAttributes = readPersister.getProjectAttributes(this.projectId);

            Collections.sort(projectAttributes, new Comparator<ProjectAttribute>() {
                @Override
                public int compare(ProjectAttribute pa1, ProjectAttribute pa2) {
                    Integer pa1Index = sortedMetaAttributeNames.indexOf(pa1.getMetaAttribute().getLookupValue().getName());
                    Integer pa2Index = sortedMetaAttributeNames.indexOf(pa2.getMetaAttribute().getLookupValue().getName());
                    return pa1Index.compareTo(pa2Index);
                }
            });

            for (ProjectAttribute projAttr : projectAttributes) {
                ProjectMetaAttribute pma = projAttr.getMetaAttribute();
                if(!pma.isActive()) { //skip inactive attribute
                    continue;
                }
                LookupValue tempLookupValue = projAttr.getMetaAttribute().getLookupValue();
                Object attrValue = ModelValidator.getModelValue(tempLookupValue, projAttr);
                if(tempLookupValue != null && tempLookupValue.getName() != null) {
                    if(attrValue!=null) {
                        if (tempLookupValue.getName().toLowerCase().contains("status")
                                && attrValue.getClass() == java.lang.Integer.class) {
                            attrValue = (Integer)attrValue == 0 ? "Ongoing" : "Completed";
                        } else if(attrValue.getClass() == Timestamp.class || attrValue.getClass() == Date.class) {
                            attrValue = CommonTool.convertTimestampToDate(attrValue);
                        }
                    }
                    projectMap.put(tempLookupValue.getName(), attrValue);
                }
            }

            projectMap.put("Project Registration", CommonTool.convertTimestampToDate(pMap.getProject().getCreationDate()));
            projectMap.put("editable", pMap.isEditable()?1:0);

            aaData.add(projectMap);
            aaData.add(attributeList);
            aaData.add(attributeType);

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    public String projectInfoByUser() {
        String returnValue = ERROR;

        try {
            aaData = new ArrayList<Map>();

            this.getCurrentUserName();

            Map<String, String> generalInfo = new HashMap<String, String>(2);
            generalInfo.put("actor", this.userName);
            generalInfo.put("attributes", this.projectPopupAttributes);
            aaData.add(generalInfo);

            List<String> displayFields = Arrays.asList(this.projectPopupAttributes.split(","));

            List<Project> projects = null;
            if(this.projectName != null && !this.projectName.isEmpty()) {
                Project requestedProject = this.readPersister.getProject(this.projectName);
                projects = new ArrayList<Project>(1);
                projects.add(requestedProject);
            } else {
                projects = this.readPersister.getAuthorizedProjects(this.userName, AccessLevel.View);
            }
            for(Project project : projects) {
                Map<String, Object> projectMap = new HashMap<String, Object>();
                projectMap.put("project", project);

                String projectPIName = "";
                String projectCoPIName = "";

                List<ProjectAttribute> projectAttributes = readPersister.getProjectAttributes(project.getProjectId());
                Map<String, Object> attributeMap = new HashMap<String, Object>(projectAttributes.size());

                for (ProjectAttribute pa : projectAttributes) {
                    ProjectMetaAttribute pma = pa.getMetaAttribute();
                    if(!pma.isActive()) { //skip inactive attribute
                        continue;
                    }
                    LookupValue tempLookupValue = pa.getMetaAttribute().getLookupValue();
                    Object attrValue = ModelValidator.getModelValue(tempLookupValue, pa);
                    if(tempLookupValue != null && tempLookupValue.getName() != null) {
                        String attributeName = tempLookupValue.getName();

                        if(displayFields.contains(attributeName)) {
                            if(attrValue != null) {
                                if(attributeName.toLowerCase().contains("status") && attrValue.getClass() == java.lang.Integer.class) {
                                    attrValue = (Integer) attrValue == 0 ? "Ongoing" : "Completed";
                                } else if(attrValue.getClass() == Timestamp.class || attrValue.getClass() == Date.class) {
                                    attrValue = CommonTool.convertTimestampToDate(attrValue);
                                } else if(attributeName.equals("Project_PI_1 First_Name")) {
                                    projectPIName = attrValue + " ";
                                } else if(attributeName.equals("Project_PI_1 Last_Name")) {
                                    projectPIName += attrValue;
                                } else if(attributeName.equals("Project_PI_2 First_Name")) {
                                    projectCoPIName = attrValue + " ";
                                } else if(attributeName.equals("Project_PI_2 Last_Name")) {
                                    projectCoPIName += attrValue;
                                }
                            }
                        }
                        attributeMap.put(tempLookupValue.getName().replaceAll("_", " "), attrValue);
                    }
                }

                attributeMap.put("Project PI", projectPIName.isEmpty() ? "N/A" : projectPIName);
                attributeMap.put("Project Co-PI", projectCoPIName.isEmpty() ? "N/A" : projectCoPIName);

                projectMap.put("attributes", attributeMap);
                aaData.add(projectMap);
            }

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    public String sample() {
        String returnValue = ERROR;

        try {
            aaData = new ArrayList<Map>();

            if(type != null && type.equals("single")){
                List<SampleAttribute> sampleAttributes = null;

                Sample sample = readPersister.getSample(this.projectId, this.sampleVal);
                sampleAttributes = readPersister.getSampleAttributes(sample.getSampleId());

                Map<String, Object> attributeMap = new HashMap<String, Object>(sampleAttributes.size());
                if(sampleAttributes != null) {
                    for (SampleAttribute sa : sampleAttributes) {
                        SampleMetaAttribute sma = sa.getMetaAttribute();
                        if (!sma.isActive()) { //skip inactive attribute
                            continue;
                        }
                        LookupValue tempLookupValue = sma.getLookupValue();
                        Object attrValue = ModelValidator.getModelValue(tempLookupValue, sa);
                        if (tempLookupValue != null && tempLookupValue.getName() != null) {
                            attributeMap.put(tempLookupValue.getName(), attrValue);
                        }
                    }
                }

                aaData.add(attributeMap);
            } else {
                List<Sample> samples;
                if (sampleId != null && sampleId != 0) {
                    if (sampleLevel != null && !sampleLevel.equals("1")) {
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
                        sampleMap.put("id", "" + sample.getSampleId());
                        sampleMap.put("name", sample.getSampleName());
                        aaData.add(sampleMap);
                    }
                }
            }

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    public String eventTypeForProject() {
        String returnValue = ERROR;

        try {
            List<LookupValue> eventNameList = readPersister.getEventTypesForProject(this.projectId);

            if(filter != null && !filter.isEmpty()) {
                boolean isSampleRegistration = filter.equals("sr");
                boolean isProjectRegistration = filter.equals("pr");

                List<LookupValue> filteredList = new ArrayList<LookupValue>();
                for(LookupValue lv : eventNameList) {
                    String eventName = lv.getName();
                    if(isProjectRegistration) {
                        if(eventName.contains(Constants.EVENT_PROJECT_REGISTRATION)) {
                            filteredList.add(lv);
                        }
                    } else if(isSampleRegistration){
                        if(eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION)) {
                            filteredList.add(lv);
                        }
                    } else {
                        if(!eventName.contains(Constants.EVENT_PROJECT_REGISTRATION)
                                && !eventName.contains(Constants.EVENT_PROJECT_UPDATE)
                                && !eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION)) {
                            filteredList.add(lv);
                        }
                    }
                }

                aaData = filteredList;
            } else {
                aaData = eventNameList;
            }

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    public String metadataForProject() {
        String returnValue = ERROR;

        try {
            aaData = new ArrayList<Map>();

            ProjectMap pMap = this.getProjectInformation(this.projectId, false);
            if(pMap.isViewable()) { //only if the user has view permission on a project
                Map<String, Object> containerMap = new HashMap<String, Object>();
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
                        if (!projectMetaList.contains(pma.getLookupValue().getName()) && pma.isActive()) {
                            projectMetaList.add(pma.getLookupValue().getName());
                        }
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
                        if (!sampleMetaList.contains(sma.getLookupValue().getName()) && sma.isActive()) {
                            sampleMetaList.add(sma.getLookupValue().getName());
                        }
                    }
                    containerMap.put("sample", sampleMetaList);
                }
                if (subType.equals("E") || subType.equals("A")) {
                    List<EventMetaAttribute> allEventMetaAttributes = readPersister.getEventMetaAttributes(projectIds);
                    Map<String, List<String>> groupedEMA = new HashMap<String, List<String>>();
                    for (EventMetaAttribute ema : allEventMetaAttributes) {
                        if(ema.getEventTypeLookupValue() != null && ema.getLookupValue() != null && ema.isActive()) {
                            String et = ema.getEventTypeLookupValue().getName();
                            String name = ema.getLookupValue().getName();
                            if(groupedEMA.containsKey(et)) {
                                groupedEMA.get(et).add(name);
                            } else {
                                List<String> newList = new ArrayList<String>();
                                newList.add(name);
                                groupedEMA.put(et, newList);
                            }
                        }
                    }
                    containerMap.put("event", groupedEMA);
                }
                aaData.add(containerMap);
            } else {
                Map<String, String> errorMap = new HashMap<String, String>(1);
                errorMap.put("error", Constants.DENIED_USER_VIEW_MESSAGE);
                aaData.add(errorMap);
            }

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    public String runAjax() {
        String returnValue = ERROR;
        if(userName == null) {
            userName = ServletActionContext.getRequest().getRemoteUser();
        }

        try {
            if("project".equals(type)) {
                aaData = new ArrayList<Map>();

                ProjectMap pMap = this.getProjectInformation(this.projectId, true);

                Map<String, Object> projectMap = new HashMap<String, Object>();
                projectMap.put("editable", pMap.isEditable()?1:0);

                projectMap.put(Constants.ATTR_PROJECT_NAME, pMap.getProject().getProjectName());
                projectMap.put("Project Registration", CommonTool.convertTimestampToDate(pMap.getProject().getCreationDate()));

                List<EventMetaAttribute> registrationEMAList = readPersister.getEventMetaAttributes(this.projectId);
                Map<String, String> attributeType = new LinkedHashMap<String, String>(0);

                attributeType.put("Event Type", "string");
                attributeType.put("Sample Name", "string");
                attributeType.put("Date", "date");
                attributeType.put("User", "string");
                for (EventMetaAttribute ema : registrationEMAList) {
                    String tempMetaName = ema.getLookupValue().getName();
                    if (!attributeType.containsKey(tempMetaName)) {
                        attributeType.put(tempMetaName, ema.getLookupValue().getDataType());
                    }
                }

                List<ProjectAttribute> projectAttributes = readPersister.getProjectAttributes(this.projectId);
                for (ProjectAttribute projAttr : projectAttributes) {
                    ProjectMetaAttribute pma = projAttr.getMetaAttribute();
                    if(!pma.isActive()) { //skip inactive attribute
                        continue;
                    }
                    LookupValue tempLookupValue = projAttr.getMetaAttribute().getLookupValue();
                    Object attrValue = ModelValidator.getModelValue(tempLookupValue, projAttr);
                    if(tempLookupValue != null && tempLookupValue.getName() != null) {
                        if(attrValue!=null) {
                            if (tempLookupValue.getName().toLowerCase().contains("status")
                                    && attrValue.getClass() == java.lang.Integer.class) {
                                attrValue = (Integer)attrValue == 0 ? "Ongoing" : "Completed";
                            } else if(attrValue.getClass() == Timestamp.class || attrValue.getClass() == Date.class) {
                                attrValue = CommonTool.convertTimestampToDate(attrValue);
                            }
                        }
                        projectMap.put(tempLookupValue.getName(), attrValue);
                    }
                }
                aaData.add(projectMap);
                aaData.add(attributeType);
            } else if ("sample".equals(type)) { //get samples for a project
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
                    //samples = readPersister.getSamplesForProject(this.projectId);
                    samples = readPersister.getSamplesForProjectBySearch(this.projectId, (this.sampleVal != null ? this.sampleVal : ""), firstResult, maxResult);

                if(firstResult == 0) totalSampleCount = readPersister.getSampleCountForProjectBySearch(this.projectId, (this.sampleVal != null ? this.sampleVal : ""));

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
            } else if ("event".equals(type)) { //get event types for a project
                List<LookupValue> eventNameList = readPersister.getEventTypesForProject(this.projectId);

                if(filter != null && !filter.isEmpty()) {
                    boolean isSampleRegistration = filter.equals("sr");
                    boolean isProjectRegistration = filter.equals("pr");

                    List<LookupValue> filteredList = new ArrayList<LookupValue>();
                    for(LookupValue lv : eventNameList) {
                        String eventName = lv.getName();
                        if(isProjectRegistration) {
                            if(eventName.contains(Constants.EVENT_PROJECT_REGISTRATION)) {
                                filteredList.add(lv);
                            }
                        } else if(isSampleRegistration){
                            if(eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION)) {
                                filteredList.add(lv);
                            }
                        } else {
                            if(!eventName.contains(Constants.EVENT_PROJECT_REGISTRATION)
                                    && !eventName.contains(Constants.EVENT_PROJECT_UPDATE)
                                    /*&& !eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION)*/) {
                                filteredList.add(lv);
                            }
                        }
                    }

                    aaData = filteredList;
                } else {
                    aaData = eventNameList;
                }

            } else if ("ma".equals(type)) { //get meta attributes for Event Report page
                aaData = new ArrayList<Map>();

                ProjectMap pMap = this.getProjectInformation(this.projectId, false);
                if(pMap.isViewable()) { //only if the user has view permission on a project
                    Map<String, Object> containerMap = new HashMap<String, Object>();
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
                            if (!projectMetaList.contains(pma.getLookupValue().getName()) && pma.isActive()) {
                                projectMetaList.add(pma.getLookupValue().getName());
                            }
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
                            if (!sampleMetaList.contains(sma.getLookupValue().getName()) && sma.isActive()) {
                                sampleMetaList.add(sma.getLookupValue().getName());
                            }
                        }
                        containerMap.put("sample", sampleMetaList);
                    }
                    if (subType.equals("E") || subType.equals("A")) {
                        List<EventMetaAttribute> allEventMetaAttributes = readPersister.getEventMetaAttributes(projectIds);
                        Map<String, List<String>> groupedEMA = new HashMap<String, List<String>>();
                        for (EventMetaAttribute ema : allEventMetaAttributes) {
                            if(ema.getEventTypeLookupValue() != null && ema.getLookupValue() != null && ema.isActive()) {
                                String et = ema.getEventTypeLookupValue().getName();
                                String name = ema.getLookupValue().getName();
                                if(groupedEMA.containsKey(et)) {
                                    groupedEMA.get(et).add(name);
                                } else {
                                    List<String> newList = new ArrayList<String>();
                                    newList.add(name);
                                    groupedEMA.put(et, newList);
                                }
                            }
                        }
                        containerMap.put("event", groupedEMA);
                    }
                    aaData.add(containerMap);
                } else {
                    Map<String, String> errorMap = new HashMap<String, String>(1);
                    errorMap.put("error", Constants.DENIED_USER_VIEW_MESSAGE);
                    aaData.add(errorMap);
                }
            } else if ("ea".equals(type)) { //attribute for an event
                List<EventMetaAttribute> emaList = readPersister.getEventMetaAttributes(projectName, eventName);
                emaList = CommonTool.filterEventMetaAttribute(emaList);
                //CommonTool.sortEventMetaAttributeByOrder(emaList);
                aaData = emaList;
            } else if ("ces".equals(type)) { //Change Event Status
                Editor editor = new Editor();
                String resultVal = editor.eventEditProcess(eventId);

                aaData = new ArrayList<String>();
                aaData.add(resultVal);
            } else if("sa".equals(type)) {
                aaData = new ArrayList<Map>();
                AttributeHelper attributeHelper = new AttributeHelper(this.readPersister);
                List<AttributePair> pairList = attributeHelper.getAllAttributeByIDs(this.projectId, this.eventId, this.ids, subType);
                if(pairList != null) {
                    for(AttributePair pair : pairList) {
                        Map<String, Object> jsonPair = new HashMap<String, Object>(3);
                        jsonPair.put("type", pair.getSample() == null ? "project" : "sample");
                        jsonPair.put(Constants.ATTR_PROJECT_NAME, pair.getProject().getProjectName());
                        jsonPair.put("object", pair.getSample() == null ? pair.getProject() : pair.getSample());
                        jsonPair.put("attributes", pair.getAttributeList());
                        aaData.add(jsonPair);
                    }
                }
            } else {
                throw new Exception("undefined AJAX action for (" + type + ")");
            }

            returnValue = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Shared AJAX : " + ex.toString());
            this.err = ex.toString();
            ex.printStackTrace();
        }

        return returnValue;
    }

    private void getCurrentUserName() {
        if(this.userName == null) {
            this.userName = ServletActionContext.getRequest().getRemoteUser();
        }
    }

    private ProjectMap getProjectInformation(Long projectId, boolean includeEdit) throws Exception {
        ProjectMap pMap = null;
        Project project = this.readPersister.getProject(projectId);

        this.getCurrentUserName();

        if(project != null) {
            pMap = new ProjectMap();
            pMap.setProject(project);

            List<String> projectNamesList = new ArrayList<String>();
            projectNamesList.add(pMap.getProject().getProjectName());

            try { //check view permission
                this.readPersister.getAuthorizedProjectNames(
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
                    this.readPersister.getAuthorizedProjectNames(
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

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSampleVal() {
        return sampleVal;
    }

    public void setSampleVal(String sampleVal) {
        this.sampleVal = sampleVal;
    }

    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    public int getTotalSampleCount() {
        return totalSampleCount;
    }

    public void setTotalSampleCount(int totalSampleCount) {
        this.totalSampleCount = totalSampleCount;
    }
}
