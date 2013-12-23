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
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.stateless_session_bean.LoginRequiredException;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.util.*;

public class ProductionStatus extends ActionSupport {
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
            availableAttributes.addAll(Arrays.asList(defaultAttributes));

            List<ProjectMetaAttribute> allProjectMetaAttributes = readPersister.getProjectMetaAttributes(projectIds);
            for (ProjectMetaAttribute pma : allProjectMetaAttributes) {
                if (!availableAttributes.contains(pma.getLookupValue().getName()))
                    availableAttributes.add(pma.getLookupValue().getName());
            }
            List<SampleMetaAttribute> allSampleMetaAttributes = readPersister.getSampleMetaAttributes(projectIds);
            for (SampleMetaAttribute sma : allSampleMetaAttributes) {
                if (!availableAttributes.contains(sma.getLookupValue().getName()))
                    availableAttributes.add(sma.getLookupValue().getName());
            }
            List<EventMetaAttribute> allEventMetaAttributes = readPersister.getEventMetaAttributes(projectIds);
            for (EventMetaAttribute ema : allEventMetaAttributes) {
                if (!availableAttributes.contains(ema.getLookupValue().getName()))
                    availableAttributes.add(ema.getLookupValue().getName());
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
        return rtnVal+(iss&&!rtnVal.equals(LOGIN)?"_s":"");
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

                                sampleAttrMap.putAll(CommonTool.getAttributeValueMap(eventAttributes, false, new String[] {"Sample Status"}));
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
}
