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
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 10/15/12
 * Time: 3:07 PM
 */
public class ProductionStatusAjax extends ActionSupport implements IAjaxAction {
    private ReadBeanPersister readPersister;

    private static final String forbiddenAttributes[] = {"run date"};
    private final String defaultAttributes[] = {"Project Name", "Sample Name", "Parent Sample"};

    private List aaData;
    private String type;
    private String projectNames;
    private String attributes;

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

    public ProductionStatusAjax() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public ProductionStatusAjax(ReadBeanPersister bean) {
        readPersister = bean;
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
            if(iSortCol_0!=null && !iSortCol_0.isEmpty()) {
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
                    if (attributeList.contains(tempAttribute))
                        existingAttributes.add(tempAttribute);
                }
                attributeList=existingAttributes;
            }
            attributeList.addAll(Arrays.asList(defaultAttributes));
            attributeList.removeAll(Arrays.asList(forbiddenAttributes));

            if(!attributesGiven) {
                attributes = "";
                for(String attribute:attributeList) {
                    attributes+=","+attribute;
                }
                attributes = attributes.substring(1);
            }

            List<Sample> samples = null;
            if((sSearch!=null && !sSearch.isEmpty()) || (iSortCol_0!=null && !iSortCol_0.isEmpty())) {
                samples = readPersister.getAllSamples(projectIds_str, attributes, sSearch, attributeType, sortCol, sSortDir_0);
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
            LookupValue tempLookupValue = null;

            Map<Long, Project> projects = new HashMap<Long, Project>(); //for caching projects
            Map<Long, Map<String, Object>> projectAttributes = new HashMap<Long, Map<String, Object>>(); // for caching project's attribute value map
            Map<Long, Sample> parentSamples = new HashMap<Long, Sample>(); //for caching retrieved parent samples

            for (Sample sample : samples) {
                Map<String, Object> sampleAttrMap = new HashMap<String, Object>();
                if(!projects.containsKey(sample.getProjectId())) {
                    currProject = readPersister.getProject(sample.getProjectId());
                    projects.put(currProject.getProjectId(), currProject);

                    List<ProjectAttribute> projectAttributesList = readPersister.getProjectAttributes(currProject.getProjectId());
                    sampleAttrMap.putAll(CommonTool.getAttributeValueMap(projectAttributesList, true, null));
                    sampleAttrMap.put("Project Name", currProject.getProjectName());
                    projectAttributes.put(currProject.getProjectId(), sampleAttrMap);
                } else {
                    currProject = projects.get(sample.getProjectId());
                    sampleAttrMap = projectAttributes.get(currProject.getProjectId());
                }

                sampleAttrMap.put("Sample Name", sample.getSampleName());
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
                        sampleAttrMap.putAll(CommonTool.getAttributeValueMap(sampleAttributes, true, null));
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

                            sampleAttrMap.putAll(CommonTool.getAttributeValueMap(eventAttributes, true, new String[] {"Sample Status"}));
                        }
                    }
                }
                aaData.add(CommonTool.decorateAttributeMap(sampleAttrMap, attributeList, currProject));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return rtnVal;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Map<Long, List<SampleAttribute>> getSampleVsAttributeList(List<Long> sampleIdList) throws Exception {
        List<SampleAttribute> allSampleAttributes = readPersister.getSampleAttributes(sampleIdList);
        Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = new HashMap<Long, List<SampleAttribute>>();
        for (SampleAttribute att : allSampleAttributes) {
            List<SampleAttribute> sas = sampleIdVsAttributeList.get(att.getSampleId());
            if (sas == null) {
                sas = new ArrayList<SampleAttribute>();
                sampleIdVsAttributeList.put(att.getSampleId(), sas);
            }
            sas.add(att);
        }

        return sampleIdVsAttributeList;
    }
    private Map<Long, List<Event>> getSampleIdVsEventList(List<Long> sampleIdList) throws Exception {
        List<Event> allSampleEvents = readPersister.getEventsForSamples(sampleIdList);
        Map<Long, List<Event>> sampleIdVsEventList = new HashMap<Long, List<Event>>();
        for (Event att : allSampleEvents) {
            List<Event> eas = sampleIdVsEventList.get(att.getSampleId());
            if (eas == null) {
                eas = new ArrayList<Event>();
                sampleIdVsEventList.put(att.getSampleId(), eas);
            }
            eas.add(att);
        }

        return sampleIdVsEventList;
    }
    private Map<Long, List<EventAttribute>> getEventIdVsAttributeList(List<Event> sampleEvents, Long projectId) throws Exception {
        List<Long> allEventIds = new ArrayList<Long>();
        for (Event evt : sampleEvents) {
            allEventIds.add(evt.getEventId());
        }
        List<EventAttribute> allEventAttributes = readPersister.getEventAttributes(allEventIds, projectId);
        Map<Long, List<EventAttribute>> eventIdVsAttributes = new HashMap<Long, List<EventAttribute>>();
        for (EventAttribute ea : allEventAttributes) {
            List<EventAttribute> lea = eventIdVsAttributes.get(ea.getEventId());
            if (lea == null) {
                lea = new ArrayList<EventAttribute>();
                eventIdVsAttributes.put(ea.getEventId(), lea);
            }
            lea.add(ea);
        }
        return eventIdVsAttributes;
    }

    public List getAaData() {
        return aaData;
    }

    public void setAaData(List aaData) {
        this.aaData = aaData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getsEcho() {
        return sEcho;
    }

    public void setSEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public int getiColumns() {
        return iColumns;
    }

    public void setIColumns(int iColumns) {
        this.iColumns = iColumns;
    }

    public int getiDisplayStart() {
        return iDisplayStart;
    }

    public void setIDisplayStart(int iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public int getiDisplayLength() {
        return iDisplayLength;
    }

    public void setIDisplayLength(int iDisplayLength) {
        this.iDisplayLength = iDisplayLength;
    }

    public int getiTotalRecords() {
        return iTotalRecords;
    }

    public void setITotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setITotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }

    public String getsSearch() {
        return sSearch;
    }

    public void setSSearch(String sSearch) {
        this.sSearch = sSearch;
    }

    public String getiSortCol_0() {
        return iSortCol_0;
    }

    public void setISortCol_0(String iSortCol_0) {
        this.iSortCol_0 = iSortCol_0;
    }

    public String getSortDir_0() {
        return sSortDir_0;
    }

    public void setSSortDir_0(String sSortDir_0) {
        this.sSortDir_0 = sSortDir_0;
    }
}
