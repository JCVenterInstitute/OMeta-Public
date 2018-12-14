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

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/19/11
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.action.ajax.IAjaxAction;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.util.*;

public class ProductionStatus extends ActionSupport implements IAjaxAction {
    private Logger logger = Logger.getLogger(ProductionStatus.class);


    private ReadBeanPersister readPersister;
    private static final String forbiddenAttributes[] = {"run date"};
    private final String defaultAttributes[] = {Constants.ATTR_PROJECT_NAME, Constants.ATTR_SAMPLE_NAME, Constants.ATTR_PARENT_SAMPLE_NAME};

    private String projectNames;
    private String attributes; //comma separated attributes
    private List<String> parameterizedAttributes;

    private String attributesOnScreen;

    private boolean iss = false;
    private boolean isExcel = false;

    private List<Map> pageElementList;

    //AJAX parameters
    private List aaData;
    private String type;
    //dataTable request parameters
    private int sEcho;
    private int iColumns;
    private int iDisplayStart;
    private int iDisplayLength;
    private int iTotalRecords;
    private int iTotalDisplayRecords;
    private String sSearch;
    private String iSortCol_0;
    private String sSortDir_0;

    //Column Filter Values
    Map<String, String> attributeTypeMap;
    private List<String> columnName;
    private List<String> columnSearchArguments;

    public ProductionStatus() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public ProductionStatus(ReadBeanPersister bean) {
        readPersister = bean;
    }

    public String statusPage() {
        String rtnVal = SUCCESS;
        try {

            List<String> projectNameList = new ArrayList<String>();
            if (projectNames.contains(",")) {
                projectNameList.addAll(Arrays.asList(projectNames.split(",")));
            } else {
                if (projectNames.equals("GSC") || projectNames.equals("HMP")) {
                    Project parentProject = readPersister.getProject(projectNames);
                    if(parentProject!=null)  {
                        projectNames="";
                        List<Project> childProjects = readPersister.getChildProjects(parentProject.getProjectId());
                        for (Project aProject : childProjects) {
                            projectNameList.add(aProject.getProjectName());
                            projectNames+=","+aProject.getProjectName();
                        }
                        projectNames = projectNames.substring(1);
                    }
                } else {
                    projectNameList.add(projectNames);
                }
            }

            // Obtain collections of data for all projects.
            List<Project> projects = readPersister.getProjects(projectNameList);
            List<Long> projectIds = new ArrayList<Long>();
            Map<String, Long> projectNameVsId = new HashMap<String, Long>();
            for (Project project : projects) {
                projectIds.add(project.getProjectId());
                projectNameVsId.put(project.getProjectName(), project.getProjectId());
            }

            //attributes
            List<String> availableAttributes = new ArrayList<String>();
            attributeTypeMap = new LinkedHashMap<String, String>(0);
            attributeTypeMap.put("Project Name", "string");
            attributeTypeMap.put("Sample Name", "string");
            attributeTypeMap.put("Parent Sample", "string");
            attributeTypeMap.put("Date", "date");
            attributeTypeMap.put("User", "string");
            availableAttributes.addAll(Arrays.asList(defaultAttributes));

            List<ProjectMetaAttribute> allProjectMetaAttributes = readPersister.getProjectMetaAttributes(projectIds);
            for (ProjectMetaAttribute pma : allProjectMetaAttributes) {
                String lookupValueName = pma.getLookupValue().getName();
                if (!availableAttributes.contains(lookupValueName)) {
                    availableAttributes.add(lookupValueName);
                    attributeTypeMap.put(lookupValueName, pma.getLookupValue().getDataType());
                }
            }
            List<SampleMetaAttribute> allSampleMetaAttributes = readPersister.getSampleMetaAttributes(projectIds);
            for (SampleMetaAttribute sma : allSampleMetaAttributes) {
                String lookupValueName = sma.getLookupValue().getName();
                if (!availableAttributes.contains(lookupValueName)) {
                    availableAttributes.add(lookupValueName);
                    attributeTypeMap.put(lookupValueName, sma.getLookupValue().getDataType());
                }
            }
            List<EventMetaAttribute> allEventMetaAttributes = readPersister.getEventMetaAttributes(projectIds);
            for (EventMetaAttribute ema : allEventMetaAttributes) {
                String lookupValueName = ema.getLookupValue().getName();
                if (!availableAttributes.contains(lookupValueName)) {
                    availableAttributes.add(lookupValueName);
                    attributeTypeMap.put(lookupValueName, ema.getLookupValue().getDataType());
                }
            }

            StringBuilder attributeBuilder = new StringBuilder();
            if (attributes == null || attributes.equals("") || "ALL".equals(attributes)) {
                for(String attribute:availableAttributes) {
                    attributeBuilder.append(attribute+",");
                }
                attributes = attributeBuilder.toString();
            }

            //escapes all single quotes
            attributes = attributes.replaceAll("'", "\\\\'");

        } catch (ForbiddenResourceException fre) {
            logger.error(Constants.DENIED_USER_VIEW_MESSAGE);
            addActionError(Constants.DENIED_USER_VIEW_MESSAGE);
        } catch (LoginRequiredException lre) {
            logger.error(Constants.LOGIN_REQUIRED_MESSAGE);
            rtnVal = LOGIN;
        } catch (Exception ex) {
            logger.error("Exception in Status Page Action : " + ex.toString());
            ex.printStackTrace();
            rtnVal = ERROR;
        }

        //shell action handler
        return rtnVal + (iss && !rtnVal.equals(LOGIN) ? "_s" : "");
    }

    public String statusExcel() {
        String rtnVal = SUCCESS;

        try {
            isExcel = true;
            LookupValue tempLookupValue;

            List<String> tokenizedOnScreenAttribute = new ArrayList<String>(Arrays.asList(attributesOnScreen.trim().replaceAll(",\\s+", ",").split(",")));
            if (tokenizedOnScreenAttribute.contains("") || tokenizedOnScreenAttribute.contains(" ")) {
                List<String> emptyList = new ArrayList<String>();
                emptyList.add("");
                tokenizedOnScreenAttribute.removeAll(emptyList);
            }

            this.setParameterizedAttributes(tokenizedOnScreenAttribute);

            List<String> projectNameList = new ArrayList<String>();
            if (projectNames.contains(","))
                projectNameList.addAll(Arrays.asList(projectNames.split(",")));
            else
                projectNameList.add(projectNames);

            // Obtain collections of data for all projects.
            List<Project> projects = readPersister.getProjects(projectNameList);
            List<Long> projectIds = new ArrayList<Long>();
            for (Project project : projects) {
                projectIds.add(project.getProjectId());
            }

            List<ProjectAttribute> allProjectAttributes = readPersister.getProjectAttributes(projectIds);
            Map<Long, List<ProjectAttribute>> projIdVsAttributes = new HashMap<Long, List<ProjectAttribute>>();
            for (ProjectAttribute pa : allProjectAttributes) {
                List<ProjectAttribute> paList = projIdVsAttributes.get(pa.getProjectId());
                if (paList == null) {
                    paList = new ArrayList<ProjectAttribute>();
                    projIdVsAttributes.put(pa.getProjectId(), paList);
                }
                paList.add(pa);
            }

            List<Sample> allSamplesAllProjects = readPersister.getSamplesForProjects(projectIds);
            Map<Long, List<Sample>> projectIdVsSampleList = new HashMap<Long, List<Sample>>();
            for (Sample sample : allSamplesAllProjects) {
                List<Sample> thisProjectsSamples = projectIdVsSampleList.get(sample.getProjectId());
                if (thisProjectsSamples == null) {
                    thisProjectsSamples = new ArrayList<Sample>();
                    projectIdVsSampleList.put(sample.getProjectId(), thisProjectsSamples);
                }
                thisProjectsSamples.add(sample);
            }

            List<Map> tempSampleAttrList = new ArrayList<Map>();

            for (Project project : projects) {
                //project attributes
                List<ProjectAttribute> paList = projIdVsAttributes.get(project.getProjectId());
                Map<String, Object> projectAttrMap = new HashMap<String, Object>();
                if (paList != null) {
                    for (ProjectAttribute pa : paList) {
                        tempLookupValue = pa.getMetaAttribute().getLookupValue();
                        projectAttrMap.put(tempLookupValue.getName(), ModelValidator.getModelValue(tempLookupValue, pa));
                    }
                }

                if (!projectAttrMap.containsKey(Constants.ATTR_PROJECT_NAME)) {
                    projectAttrMap.put(Constants.ATTR_PROJECT_NAME, project.getProjectName());
                    if(!Constants.ATTR_PROJECT_NAME.equals("Project Name"))
                        projectAttrMap.put("Project Name", project.getProjectName());
                }

                List<Sample> samples = projectIdVsSampleList.get(project.getProjectId());
                if(samples!=null && samples.size()>0) {
                    List<Long> sampleIdList = new ArrayList<Long>();
                    for (Sample sample : samples) {
                        sampleIdList.add(sample.getSampleId());
                    }

                    Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = getSampleVsAttributeList(sampleIdList);
                    Map<Long, List<Event>> sampleIdVsEventList = getSampleIdVsEventList(sampleIdList);

                    for (Sample sample : samples) {
                        Map<String, Object> sampleAttrMap = new HashMap<String, Object>();
                        sampleAttrMap.putAll(projectAttrMap);
                        sampleAttrMap.put(Constants.ATTR_SAMPLE_NAME, sample.getSampleName());
                        if(!Constants.ATTR_SAMPLE_NAME.equals("Sample Name"))
                            sampleAttrMap.put("Sample Name", sample.getSampleName());
                        sampleAttrMap.put("sampleId", sample.getSampleId());
                        if (sample.getParentSampleId() != null) {
                            Sample parentSample = readPersister.getSample(sample.getParentSampleId());
                            sampleAttrMap.put("Parent Sample", parentSample.getSampleName());
                        }

                        List<SampleAttribute> sampleAttributes = sampleIdVsAttributeList.get(sample.getSampleId());
                        if (sampleAttributes != null && sampleAttributes.size() > 0) {
                            for (SampleAttribute sa : sampleAttributes) {
                                if (sa.getMetaAttribute() == null)
                                    continue;
                                tempLookupValue = sa.getMetaAttribute().getLookupValue();

                                sampleAttrMap.put(tempLookupValue.getName(), ModelValidator.getModelValue(tempLookupValue, sa));
                            }
                        }

                        List<Event> sampleEvents = sampleIdVsEventList.get(sample.getSampleId());
                        if (sampleEvents != null && sampleEvents.size() > 0) {
                            Map<Long, List<EventAttribute>> eventIdVsAttributes = getEventIdVsAttributeList(sampleEvents, project.getProjectId());

                            for (Event evt : sampleEvents) {
                                List<EventAttribute> eventAttributes = eventIdVsAttributes.get(evt.getEventId());
                                if (eventAttributes == null)
                                    continue;

                                sampleAttrMap.putAll(CommonTool.getAttributeValueMap(eventAttributes, sample.getProjectId(), sample.getSampleName(), false, new String[] {"Sample Status"}));
                            }
                        }
                        //tempSampleAttrList.add(CommonTool.decorateAttributeMap(sampleAttrMap, tokenizedOnScreenAttribute, project));
                        tempSampleAttrList.add(sampleAttrMap);
                    }
                }
            }
            this.setPageElementList(tempSampleAttrList);

            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in Status Page Action : " + ex.toString());
            ex.printStackTrace();
            return ERROR;
        }

        return rtnVal;
    }

    @Override
    public String runAjax() {
        String rtnVal = ERROR;

        aaData = new ArrayList<Map>();

        try {
            boolean attributesGiven = (attributes!=null && !attributes.equals("") && !"ALL".equals(attributes));

            List<String> givenAttributeList = null;
            if(attributesGiven) {
                if(attributes.endsWith(","))
                    attributes = attributes.substring(0, attributes.length()-1);
                givenAttributeList = Arrays.asList(attributes.split(","));
            }
            String sortCol = null;
            if(iSortCol_0 != null && !iSortCol_0.isEmpty()) {
                sortCol = givenAttributeList.get(Integer.parseInt(iSortCol_0));
            }

            //get all project for given project names
            List<Project> projectList = readPersister.getProjects(Arrays.asList(projectNames.split(",")));
            List<Long> projectIds = new ArrayList<Long>();
            String projectIds_str = "";
            for(Project project:projectList) {
                projectIds.add(project.getProjectId());
                projectIds_str += ","+project.getProjectId();
            }
            projectIds_str=projectIds_str.substring(1);

            //process attributes
            String attributeType = null;
            boolean hasProjectAttribute = false;
            boolean hasEventAttribute = false;
            boolean hasSampleAttribute = false;

            String tempMetaName = null;
            List<String> attributeList = new ArrayList<String>();
            List<ProjectMetaAttribute> allProjectMetaAttributes = readPersister.getProjectMetaAttributes(projectIds);
            for (ProjectMetaAttribute pma : allProjectMetaAttributes) {
                tempMetaName = pma.getLookupValue().getName();
                if (!attributeList.contains(tempMetaName))
                    attributeList.add(tempMetaName);
                if(!hasProjectAttribute && attributes.contains(tempMetaName)) {
                    hasProjectAttribute = true;
                }
                if(sortCol!=null && sortCol.equals(tempMetaName))
                    attributeType = "p";
            }
            List<SampleMetaAttribute> allSampleMetaAttributes = readPersister.getSampleMetaAttributes(projectIds);
            for (SampleMetaAttribute sma : allSampleMetaAttributes) {
                tempMetaName = sma.getLookupValue().getName();
                if (!attributeList.contains(tempMetaName)) {
                    attributeList.add(tempMetaName);
                    if(!hasSampleAttribute && attributes.contains(tempMetaName)) {
                        hasSampleAttribute = true;
                    }
                    if(sortCol!=null && sortCol.equals(tempMetaName))
                        attributeType = "s";
                }
            }
            List<EventMetaAttribute> allEventMetaAttributes = readPersister.getEventMetaAttributes(projectIds);
            for (EventMetaAttribute ema : allEventMetaAttributes) {
                tempMetaName = ema.getLookupValue().getName();
                if (!attributeList.contains(tempMetaName)) {
                    attributeList.add(tempMetaName);
                    if(!hasEventAttribute && attributes.contains(tempMetaName)) {
                        hasEventAttribute = true;
                    }
                    if(sortCol!=null && sortCol.equals(tempMetaName))
                        attributeType = "e";
                }
            }
            if (attributesGiven) {
                List<String> tokenizedAttribute = new ArrayList<String>(Arrays.asList(attributes.split(",")));
                List<String> existingAttributes = new ArrayList<String>();
                for (String tempAttribute : tokenizedAttribute) {
                    if (attributeList.contains(tempAttribute) || tempAttribute.equals("Project Name"))
                        existingAttributes.add(tempAttribute);
                }
                attributeList=existingAttributes;
            }
            attributeList.addAll(Arrays.asList(defaultAttributes));
            attributeList.removeAll(Arrays.asList(forbiddenAttributes));

            //Add Sample Name as an attribute if constants one is SampleName
            if(!Constants.ATTR_SAMPLE_NAME.equals("Sample Name")) attributeList.add("Sample Name");
            if(!Constants.ATTR_PARENT_SAMPLE_NAME.equals("Parent Sample")) attributeList.add("Parent Sample");

            if(!attributesGiven) {
                attributes = "";
                for(String attribute:attributeList) {
                    attributes+=","+attribute;
                }
                attributes = attributes.substring(1);
            }

            List<Sample> samples;
            if((sSearch!=null && !sSearch.isEmpty()) || (iSortCol_0!=null && !iSortCol_0.isEmpty()) || (columnName != null && !columnName.isEmpty())) {
                samples = readPersister.getAllSamples(projectIds_str, attributes, sSearch, attributeType, sortCol, sSortDir_0, columnName, columnSearchArguments);
            } else {
                //get all samples for given project IDs
                samples = readPersister.getSamplesForProjects(projectIds);
            }

            //paginate samples before main loop
            iTotalDisplayRecords=iTotalRecords=samples.size();
            samples = samples.subList(iDisplayStart, iDisplayStart+iDisplayLength>samples.size()?samples.size():iDisplayLength+iDisplayStart);

            List<Long> sampleIdList = new ArrayList<Long>();
            for (Sample sample : samples) {
                sampleIdList.add(sample.getSampleId());
            }
            Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = getSampleVsAttributeList(sampleIdList);
            Map<Long, List<Event>> sampleIdVsEventList = getSampleIdVsEventList(sampleIdList);

            Project currProject = null;

            Map<Long, Project> projects = new HashMap<Long, Project>(); //for caching projects
            Map<Long, Map<String, Object>> projectAttributes = new HashMap<Long, Map<String, Object>>(); // for caching project's attribute value map
            Map<Long, Sample> parentSamples = new HashMap<Long, Sample>(); //for caching retrieved parent samples

            for (Sample sample : samples) {
                Map<String, Object> sampleAttrMap = new HashMap<String, Object>();
                if(!projects.containsKey(sample.getProjectId())) { //recycle or get project data with attributes
                    currProject = readPersister.getProject(sample.getProjectId());
                    projects.put(currProject.getProjectId(), currProject);

                    List<ProjectAttribute> projectAttributesList = readPersister.getProjectAttributes(currProject.getProjectId());
                    Map<String, Object> projectAttrMap = new HashMap<String, Object>();
                    projectAttrMap.putAll(CommonTool.getAttributeValueMap(projectAttributesList, sample.getProjectId(), sample.getSampleName(), true, null));

                    if(!projectAttrMap.containsKey(Constants.ATTR_PROJECT_NAME)) { //if there is no project name attribute, use project name from project object
                        projectAttrMap.put(Constants.ATTR_PROJECT_NAME, currProject.getProjectName());
                    }
                    projectAttributes.put(currProject.getProjectId(), projectAttrMap);
                }

                currProject = projects.get(sample.getProjectId());
                sampleAttrMap.putAll(projectAttributes.get(currProject.getProjectId()));

                sampleAttrMap.put(Constants.ATTR_SAMPLE_NAME, sample.getSampleName());
                if(!sampleAttrMap.containsKey("Sample Name")){ // Constants value may be 'SampleName'
                    sampleAttrMap.put("Sample Name", sample.getSampleName());
                }
                sampleAttrMap.put("sampleId", sample.getSampleId());

                if (sample.getParentSampleId() != null) { //get parent sample information and cache it
                    Sample parentSample = null;
                    if(parentSamples.containsKey(sample.getParentSampleId())) {
                        parentSample = parentSamples.get(sample.getParentSampleId());
                    } else {
                        parentSample = readPersister.getSample(sample.getParentSampleId());
                        parentSamples.put(parentSample.getSampleId(), parentSample);
                    }
                    sampleAttrMap.put("Parent Sample", parentSample.getSampleName());
                }

                if(hasSampleAttribute) {
                    List<SampleAttribute> sampleAttributes = sampleIdVsAttributeList.get(sample.getSampleId());
                    if (sampleAttributes != null && sampleAttributes.size() > 0) {
                        sampleAttrMap.putAll(CommonTool.getAttributeValueMap(sampleAttributes, sample.getProjectId(), sample.getSampleName(), true, null));
                    }
                }

                if(hasEventAttribute) {
                    List<Event> sampleEvents = sampleIdVsEventList.get(sample.getSampleId());
                    if (sampleEvents != null && sampleEvents.size() > 0) {
                        Map<Long, List<EventAttribute>> eventIdVsAttributes = getEventIdVsAttributeList(sampleEvents, currProject.getProjectId());

                        for (Event evt : sampleEvents) {
                            List<EventAttribute> eventAttributes = eventIdVsAttributes.get(evt.getEventId());
                            if (eventAttributes == null)
                                continue;

                            sampleAttrMap.putAll(CommonTool.getAttributeValueMap(eventAttributes, sample.getProjectId(), sample.getSampleName(), true, new String[] {"Sample Status"}));
                        }
                    }
                }
                aaData.add(CommonTool.decorateAttributeMap(sampleAttrMap, attributeList, currProject));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return rtnVal;
    }

    private Map<Long, List<SampleAttribute>> getSampleVsAttributeList(List<Long> sampleIdList) throws Exception {
        // Get all sample attributes for all samples, and remarshal them into a map of sample vs attributes.
        List<SampleAttribute> allSampleAttributes = readPersister.getSampleAttributes(sampleIdList);
        Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = new HashMap<Long, List<SampleAttribute>>();
        for (SampleAttribute att : allSampleAttributes) {
            List<SampleAttribute> atts = sampleIdVsAttributeList.get(att.getSampleId());
            if (atts == null) {
                atts = new ArrayList<SampleAttribute>();
                sampleIdVsAttributeList.put(att.getSampleId(), atts);
            }
            atts.add(att);
        }

        return sampleIdVsAttributeList;
    }
    private Map<Long, List<Event>> getSampleIdVsEventList(List<Long> sampleIdList) throws Exception {
        // Get all events for all samples, and remarshal them into a map of sample vs event.
        List<Event> allSampleEvents = readPersister.getEventsForSamples(sampleIdList);
        Map<Long, List<Event>> sampleIdVsEventList = new HashMap<Long, List<Event>>();
        for (Event att : allSampleEvents) {
            List<Event> atts = sampleIdVsEventList.get(att.getSampleId());
            if (atts == null) {
                atts = new ArrayList<Event>();
                sampleIdVsEventList.put(att.getSampleId(), atts);
            }
            atts.add(att);
        }

        return sampleIdVsEventList;
    }
    private Map<Long, List<EventAttribute>> getEventIdVsAttributeList(List<Event> sampleEvents, Long projectId) throws Exception {
        // Corral the ids of the events from the list of events.
        List<Long> allEventIds = new ArrayList<Long>();
        for (Event evt : sampleEvents) {
            allEventIds.add(evt.getEventId());
        }

        // Remarshal the event attributes into a map keyed off the event id.
        if (allEventIds == null || allEventIds.size() == 0) {
            logger.debug("Invoking getEventIdVsAttributeList with empty or null list.");
        }
        List<EventAttribute> allEventAttributes = readPersister.getEventAttributes(allEventIds, projectId);
        logger.debug("Got " + allEventAttributes.size() + " event attributes in getEventIdVsAttributeList ");
        Map<Long, List<EventAttribute>> eventIdVsAttributes = new HashMap<Long, List<EventAttribute>>();
        for (EventAttribute ea : allEventAttributes) {
            List<EventAttribute> lea = eventIdVsAttributes.get(ea.getEventId());
            if (lea == null) {
                lea = new ArrayList<EventAttribute>();
                eventIdVsAttributes.put(ea.getEventId(), lea);
            }
            lea.add(ea);
        }
        if (eventIdVsAttributes.size() == 0) {
            logger.debug("Returning empty results from getEventIdVsAttributeList  for input list of size " + allEventIds.size());
        }
        return eventIdVsAttributes;
    }

    public String getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(String projectNames) {
        this.projectNames = projectNames;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public List<String> getParameterizedAttributes() {
        return parameterizedAttributes;
    }

    public void setParameterizedAttributes(List<String> parameterizedAttributes) {
        this.parameterizedAttributes = parameterizedAttributes;
    }

    public String getAttributesOnScreen() {
        return attributesOnScreen;
    }

    public void setAttributesOnScreen(String attributesOnScreen) {
        this.attributesOnScreen = attributesOnScreen;
    }

    public boolean isExcel() {
        return isExcel;
    }

    public void setExcel(boolean excel) {
        isExcel = excel;
    }

    public boolean isIss() {
        return iss;
    }

    public void setIss(boolean iss) {
        this.iss = iss;
    }

    public List<Map> getPageElementList() {
        return pageElementList;
    }

    public void setPageElementList(List<Map> pageElementList) {
        this.pageElementList = pageElementList;
    }

    public List getaaData() {
        return aaData;
    }

    public void setaaData(List aaData) {
        this.aaData = aaData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getsEcho() {
        return sEcho;
    }

    public void setsEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public int getiColumns() {
        return iColumns;
    }

    public void setiColumns(int iColumns) {
        this.iColumns = iColumns;
    }

    public int getiDisplayStart() {
        return iDisplayStart;
    }

    public void setiDisplayStart(int iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public int getiDisplayLength() {
        return iDisplayLength;
    }

    public void setiDisplayLength(int iDisplayLength) {
        this.iDisplayLength = iDisplayLength;
    }

    public int getiTotalRecords() {
        return iTotalRecords;
    }

    public void setiTotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }

    public String getsSearch() {
        return sSearch;
    }

    public void setsSearch(String sSearch) {
        this.sSearch = sSearch;
    }

    public String getiSortCol_0() {
        return iSortCol_0;
    }

    public void setiSortCol_0(String iSortCol_0) {
        this.iSortCol_0 = iSortCol_0;
    }

    public String getsSortDir_0() {
        return sSortDir_0;
    }

    public void setsSortDir_0(String sSortDir_0) {
        this.sSortDir_0 = sSortDir_0;
    }

    public List<String> getColumnSearchArguments() {
        return columnSearchArguments;
    }

    public void setColumnSearchArguments(List<String> columnSearchArguments) {
        this.columnSearchArguments = columnSearchArguments;
    }

    public List<String> getColumnName() {
        return columnName;
    }

    public void setColumnName(List<String> columnName) {
        this.columnName = columnName;
    }

    public Map<String, String> getAttributeTypeMap() {
        return attributeTypeMap;
    }

    public void setAttributeTypeMap(Map<String, String> attributeTypeMap) {
        this.attributeTypeMap = attributeTypeMap;
    }
}
