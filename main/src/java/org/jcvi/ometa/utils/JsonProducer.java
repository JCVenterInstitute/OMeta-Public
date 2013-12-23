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

package org.jcvi.ometa.utils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.varia.scheduler.Schedulable;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 7/26/11
 * Time: 10:23 AM
 * To change this template use File | Settings | File Templates.
 */

public class JsonProducer implements Schedulable {
    private Logger logger = Logger.getLogger(JsonProducer.class);
    private ProjectSampleEventPresentationBusiness pseEjb;

    private final String forbiddenAttributes[] = {"run date"};
    private final String SAMPLE_STATUS = "Sample Status";
    private final String PROD_SERVER_ADDRESS = "https://projectsampletracking.jcvi.org";

    public JsonProducer() {
        pseEjb = new PresentationActionDelegate().initializeEjb(Logger.getLogger(ReadBeanPersister.class), null);
    }

    public JsonProducer(ProjectSampleEventPresentationBusiness ejb) {
        pseEjb = ejb;
    }

    public static void main(String[] args) {
        try {
            JsonProducer pro = new JsonProducer();
            pro.generateJson();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void generateJson() {
        try {
            logger.info("[JsonProducer-MBean] JsonProducer process is starting.");

            Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
            String jsonUrls = props.getProperty("JSON.Urls");
            String kingdomUrls = props.getProperty("KINGDOM.Urls");

            if (jsonUrls != null && !jsonUrls.isEmpty()) {
                String[] urls = jsonUrls.trim().split("\\$"); //create String array of sample tracking URLs
                if (urls.length > 0) {
                    for (String url : urls) {
                        String projectNames = null;
                        String attributes = null;
                        String screenAttributes = null;
                        String sorting = null;
                        String fileName = null;
                        String filePath = null;
                        String domain = null;

                        String[] urlAttributes = url.trim().split("\\&"); //attribute seperator
                        for (String urlAttribute : urlAttributes) {
                            String urlAttributeVal = (urlAttribute.substring(urlAttribute.indexOf("=") + 1, urlAttribute.length())).replaceAll("\\+", " ");
                            if (urlAttribute.contains("projectNames="))
                                projectNames = urlAttributeVal;
                            else if (urlAttribute.contains("attributes="))
                                attributes = urlAttributeVal;
                            else if (urlAttribute.contains("sorting="))
                                sorting = urlAttributeVal;
                            else if (urlAttribute.contains("fileName="))
                                fileName = urlAttributeVal;
                            else if (urlAttribute.contains("filePath="))
                                filePath = urlAttributeVal;
                            else if (urlAttribute.contains("screenAttributes="))
                                screenAttributes = urlAttributeVal;
                            else if (urlAttribute.contains("domain"))
                                domain = urlAttributeVal;
                        }

                        if (projectNames != null && attributes != null && fileName != null && filePath != null)
                            jsonHelper(projectNames, attributes, screenAttributes, sorting, fileName, filePath, domain);
                    }
                }
            }

            if(kingdomUrls!=null && !kingdomUrls.isEmpty()) {
                this.kingdomHelper(kingdomUrls);
            }

            logger.info("[JsonProducer-MBean] JsonProducer process is done.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void jsonHelper(String projectNames, String attributes, String screenAttributes, String sorting, String fileName, String filePath, String domain) {
        String PROJECT_STATUS = "Project Status";
        try {
            JSONObject json = new JSONObject();

            File directory = new File(filePath);
            if (!directory.exists() || !directory.isDirectory()) {
                if ((new File(directory.getParent())).canWrite())
                    directory.mkdir();
                else
                    throw new Exception();
            }
            //Json file Creation
            File tempFile = new File(filePath + File.separator + fileName + "_temp.json");
            FileWriter fileWriter = new FileWriter(tempFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            //Normal status data retrieval
            LookupValue tempLookupValue;

            List<String> projectNameList = new ArrayList<String>();
            if (projectNames.contains(","))
                projectNameList.addAll(Arrays.asList(projectNames.split(",")));
            else
                projectNameList.add(projectNames);

            List<String> availableAttributes = new ArrayList<String>();
            availableAttributes.add("Sample Name");

            List<Project> projects = pseEjb.getProjects(projectNameList);
            List<Long> projectIds = new ArrayList<Long>();
            Map<String, Long> projectNameVsId = new HashMap<String, Long>();
            for (Project project : projects) {
                projectIds.add(project.getProjectId());
                projectNameVsId.put(project.getProjectName(), project.getProjectId());
            }

            List<ProjectMetaAttribute> allProjectMetaAttributes = pseEjb.getProjectMetaAttributes(projectIds);
            for (ProjectMetaAttribute pma : allProjectMetaAttributes) {
                if (!availableAttributes.contains(pma.getLookupValue().getName()))
                    availableAttributes.add(pma.getLookupValue().getName());
            }
            List<SampleMetaAttribute> allSampleMetaAttributes = pseEjb.getSampleMetaAttributes(projectIds);
            for (SampleMetaAttribute sma : allSampleMetaAttributes) {
                if (!availableAttributes.contains(sma.getLookupValue().getName()))
                    availableAttributes.add(sma.getLookupValue().getName());
            }
            List<EventMetaAttribute> allEventMetaAttributes = pseEjb.getEventMetaAttributes(projectIds);
            for (EventMetaAttribute ema : allEventMetaAttributes) {
                if (!availableAttributes.contains(ema.getLookupValue().getName()))
                    availableAttributes.add(ema.getLookupValue().getName());
            }

            List<String> parameterizedAttributes = null;
            if (attributes == null || attributes.equals("") || "ALL".equals(attributes)) {
                parameterizedAttributes = availableAttributes;
            } else {
                parameterizedAttributes = new ArrayList<String>();

                ArrayList<String> tokenizedAttribute = new ArrayList<String>(Arrays.asList(attributes.split(",")));

                for (String tempAttribute : tokenizedAttribute) {
                    if (availableAttributes.contains(tempAttribute))
                        parameterizedAttributes.add(tempAttribute);
                }
            }
            parameterizedAttributes.removeAll(Arrays.asList(forbiddenAttributes));

            /*------------ XLS Part ------------*/
            //Excel file Creation
            Workbook workBook = new HSSFWorkbook();
            Sheet workSheet = workBook.createSheet();
            int cellIndex = 0, rowIndex = 0;
            Row singleRow = workSheet.createRow(rowIndex++);
            Cell headerCell = null;

            //Header row cell style
            CellStyle style = workBook.createCellStyle();
            style.setFillBackgroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            Font font = workBook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.WHITE.getIndex());
            style.setFont(font);
            /*------------ XLS Part END ------------*/

            List<String> attributeList = new ArrayList<String>();

            for (String tempAttribute : parameterizedAttributes) {
                attributeList.add(tempAttribute);
                headerCell = singleRow.createCell(cellIndex++);
                headerCell.setCellValue(tempAttribute);
                headerCell.setCellStyle(style);
            }

            if (screenAttributes == null || screenAttributes.equals("") || screenAttributes.equals("ALL")) {
                json.put("attributes", attributeList);
            } else {
                json.put("attributes", Arrays.asList(screenAttributes.split(",")));
            }

            json.put("sorting", (sorting == null || sorting.isEmpty() || sorting.equals("-") ? null : sorting));
            json.put("projectNames", projectNames);

            List<ProjectAttribute> allProjectAttributes = pseEjb.getProjectAttributes(projectIds);
            Map<Long, List<ProjectAttribute>> projIdVsAttributes = new HashMap<Long, List<ProjectAttribute>>();
            for (ProjectAttribute pa : allProjectAttributes) {
                List<ProjectAttribute> paList = projIdVsAttributes.get(pa.getProjectId());
                if (paList == null) {
                    paList = new ArrayList<ProjectAttribute>();
                    projIdVsAttributes.put(pa.getProjectId(), paList);
                }
                paList.add(pa);
            }

            List<Sample> allSamplesAllProjects = pseEjb.getSamplesForProjects(projectIds);
            Map<Long, List<Sample>> projectIdVsSampleList = new HashMap<Long, List<Sample>>();
            for (Sample sample : allSamplesAllProjects) {
                List<Sample> thisProjectsSamples = projectIdVsSampleList.get(sample.getProjectId());
                if (thisProjectsSamples == null) {
                    thisProjectsSamples = new ArrayList<Sample>();
                    projectIdVsSampleList.put(sample.getProjectId(), thisProjectsSamples);
                }
                thisProjectsSamples.add(sample);
            }


            /************* Main LOOP starts *****************/
            List<JSONObject> sampleList = new ArrayList<JSONObject>();
            List<String> statusList = new ArrayList<String>();
            List<JSONObject> sumList = new ArrayList<JSONObject>();

            for (Project project : projects) {
                JSONObject currSum = new JSONObject();

                if (project.getIsPublic() == 0)
                    continue;

                Long tempProjectId = project.getProjectId();
                List<ProjectAttribute> paList = projIdVsAttributes.get(tempProjectId);
                Map<String, Object> projectAttrMap = new HashMap<String, Object>();
                if (paList != null) {
                    for (ProjectAttribute pa : paList) {
                        tempLookupValue = pa.getMetaAttribute().getLookupValue();
                        projectAttrMap.put(tempLookupValue.getName(), ModelValidator.getModelValue(tempLookupValue, pa));
                    }
                }

                if (!projectAttrMap.containsKey(Constants.ATTR_PROJECT_NAME))
                    projectAttrMap.put(Constants.ATTR_PROJECT_NAME, project.getProjectName());

                currSum.put("p_n", project.getProjectName());
                currSum.put("p_s", projectAttrMap.get(PROJECT_STATUS));
                currSum.put("p_g", projectAttrMap.get("Project Group"));

                List<Long> sampleIdList = getSampleIdList(getSamplesFromList(projectIdVsSampleList, tempProjectId));
                Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = getSampleVsAttributeList(sampleIdList);
                Map<Long, List<Event>> sampleIdVsEventList = getSampleIdVsEventList(sampleIdList);

                List<Sample> samplesForProject = getSamplesFromList(projectIdVsSampleList, tempProjectId);
                currSum.put("tot", samplesForProject.size());

                for (Sample sample : samplesForProject) {
                    Map<String, Object> sampleAttrMap = new HashMap<String, Object>();
                    sampleAttrMap.putAll(projectAttrMap);
                    sampleAttrMap.put(Constants.ATTR_SAMPLE_NAME, sample.getSampleName());
                    sampleAttrMap.put("sampleId", sample.getSampleId());

                    List<SampleAttribute> sampleAttributes = sampleIdVsAttributeList.get(sample.getSampleId());
                    if (sampleAttributes != null && sampleAttributes.size() > 0) {
                        for (SampleAttribute sa : sampleAttributes) {
                            if(sa.getMetaAttribute()==null)
                                continue;
                            tempLookupValue = sa.getMetaAttribute().getLookupValue();
                            Object sav = ModelValidator.getModelValue(tempLookupValue, sa);
                            sampleAttrMap.put(tempLookupValue.getName(), sav);

                            if(SAMPLE_STATUS.equals(tempLookupValue.getName())) {
                                String currStatus = (String)sav;
                                if(!statusList.contains(currStatus)) //add new status value
                                    statusList.add(currStatus);
                                currSum.put(currStatus, currSum.has(currStatus)?currSum.getInt(currStatus)+1:1); //count
                            }

                        }
                    }

                    List<Event> sampleEvents = sampleIdVsEventList.get(sample.getSampleId());
                    if (sampleEvents != null && sampleEvents.size() > 0) {
                        Map<Long, List<EventAttribute>> eventIdVsAttributes = getEventIdVsAttributeList(sampleEvents, tempProjectId);
                        //skip sample status value in event attributes
                        String[] skipArrForEventAttribute = {"Sample Status"};

                        for (Event evt : sampleEvents) {
                            List<EventAttribute> eventAttributes = eventIdVsAttributes.get(evt.getEventId());
                            if (eventAttributes == null)
                                continue;

                            sampleAttrMap.putAll(CommonTool.getAttributeValueMap(eventAttributes, false, skipArrForEventAttribute));
                        }
                    }

                    JSONObject sampleJsonObj = new JSONObject();
                    for (String key : sampleAttrMap.keySet()) {
                        //this is custom decorating process for json data file only
                        //in status.shtml page, link on an organism should land to the project page rather than sample detail page
                        if(key.equals("Organism")) {
                            String organismVal = (String)sampleAttrMap.get(key);

                            sampleJsonObj.put("OrganismUrl",
                                    (PROD_SERVER_ADDRESS + Constants.SAMPLE_DETAIL_URL +
                                            "iss=true" +
                                            "&projectName=" + project.getProjectName() +
                                            "&projectId=" + project.getProjectId() +
                                            "&sampleName=" + sampleAttrMap.get("Sample Name") +
                                            "&sampleId=" + sampleAttrMap.get("sampleId")).replaceAll("\\\"", "\\\\\"")
                            );
                            if(domain!=null && !"none".equals(domain)) {
                                organismVal = convertIntoATag(
                                        String.format(Constants.INTERNAL_PROJECT_PAGE,
                                                domain, //hostName != null && hostName.contains("spike") ? fileName + "-dev" : fileName,
                                                ((String) sampleAttrMap.get("Project Group")).toLowerCase(),
                                                project.getProjectName().replaceAll(" ", "_")
                                        ), (String)sampleAttrMap.get(key)
                                );
                            }
                            sampleJsonObj.put(key, organismVal);
                        } else {
                            sampleJsonObj.put(key, CommonTool.decorateAttribute(sampleAttrMap, key, project));
                        }
                    }
                    sampleList.add(sampleJsonObj);

                    cellIndex = 0;
                    singleRow = workSheet.createRow(rowIndex++);
                    for (String tempAttribute : parameterizedAttributes) {
                        singleRow.createCell(cellIndex++)
                                .setCellValue(sampleAttrMap.get(tempAttribute) != null ? "" + sampleAttrMap.get(tempAttribute) : "");
                    }
                }
                sumList.add(currSum);
            }

            JSONObject sumMap = new JSONObject();
            sumMap.put("s_l", statusList);
            sumMap.put("data", sumList);
            json.put("sums", sumMap);

            json.put("samples", sampleList);
            //bufferedWriter.write("]");
            bufferedWriter.write(json.toString());
            bufferedWriter.close();

            if (tempFile.exists() && tempFile.length() > 0) {
                File dataFile = new File(filePath + File.separator + fileName + ".json");
                tempFile.renameTo(dataFile);

                FileOutputStream fileOut = new FileOutputStream(filePath + File.separator + fileName + ".xls");
                workBook.write(fileOut);
                fileOut.close();
            } else
                throw new Exception("Failure in retrieving data for " + fileName + ". File does not exist or file size is zero.");

            logger.info("[JsonProducer-MBean] JsonProducer process succeeded for " + projectNames);
        } catch (Exception ex) {
            logger.info("[JsonProducer-MBean] JsonProducer failed for " + projectNames);
            ex.printStackTrace();

            /*if( hostName.contains( "dmzweb" ) ) { //Send error notification for DMZs only
                new EmailSender().send(
                        "json",
                        "[PST]Failure in generating Json Data file on : " + hostName,
                        ex.toString()
                );
            }*/
        }
    }

    private void kingdomHelper(String kingdomUrl) {
        try {
            String filePath = null;
            Map<String, List<String>> kingdomProjectMap = null;

            String[] urls = kingdomUrl.trim().split("\\$");
            if (urls.length > 0) {
                kingdomProjectMap = new HashMap<String, List<String>>();
                for (String url : urls) {
                    String kingdom = null;
                    String[] urlAttributes = url.trim().split("\\&"); //attribute seperator
                    for (String urlAttribute : urlAttributes) {
                        String urlAttributeVal = (urlAttribute.substring(urlAttribute.indexOf("=") + 1, urlAttribute.length())).replaceAll("\\+", " ");
                        if(urlAttribute.contains("filePath="))
                            filePath=urlAttributeVal;
                        else if(urlAttribute.contains("kingdom="))
                            kingdom=urlAttributeVal;
                        else if(urlAttribute.contains("projectNames="))
                            kingdomProjectMap.put(kingdom, Arrays.asList(urlAttributeVal.split(",")));
                    }
                }
            }

            //Normal status data retrieval
            LookupValue tempLookupValue;

            Map<String, Species> speciesMap = new HashMap<String, Species>();
            for(Map.Entry<String, List<String>> entry : kingdomProjectMap.entrySet()) {
                List<String> projectNameList = entry.getValue();
                List<Project> projects = pseEjb.getProjects(projectNameList);
                List<Long> projectIds = new ArrayList<Long>();
                for (Project project : projects) {
                    projectIds.add(project.getProjectId());
                }

                List<ProjectAttribute> allProjectAttributes = pseEjb.getProjectAttributes(projectIds);
                Map<Long, List<ProjectAttribute>> projIdVsAttributes = new HashMap<Long, List<ProjectAttribute>>();
                for (ProjectAttribute pa : allProjectAttributes) {
                    List<ProjectAttribute> paList = projIdVsAttributes.get(pa.getProjectId());
                    if (paList == null) {
                        paList = new ArrayList<ProjectAttribute>();
                        projIdVsAttributes.put(pa.getProjectId(), paList);
                    }
                    paList.add(pa);
                }

                List<Sample> allSamplesAllProjects = pseEjb.getSamplesForProjects(projectIds);
                Map<Long, List<Sample>> projectIdVsSampleList = new HashMap<Long, List<Sample>>();
                for (Sample sample : allSamplesAllProjects) {
                    List<Sample> thisProjectsSamples = projectIdVsSampleList.get(sample.getProjectId());
                    if (thisProjectsSamples == null) {
                        thisProjectsSamples = new ArrayList<Sample>();
                        projectIdVsSampleList.put(sample.getProjectId(), thisProjectsSamples);
                    }
                    thisProjectsSamples.add(sample);
                }
                for(Project project : projects) {
                    Long tempProjectId = project.getProjectId();
                    String projectUrl = null;

                    List<ProjectAttribute> paList = projIdVsAttributes.get(tempProjectId);
                    if (paList != null) {
                        for (ProjectAttribute pa : paList) {
                            tempLookupValue = pa.getMetaAttribute().getLookupValue();
                            if(tempLookupValue.getName().equals("ProjectURL")) {
                                projectUrl = pa.getAttributeStringValue();
                            }
                        }
                    }

                    List<Long> sampleIdList = getSampleIdList(getSamplesFromList(projectIdVsSampleList, tempProjectId));
                    Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = getSampleVsAttributeList(sampleIdList);

                    List<Sample> samplesForProject = getSamplesFromList(projectIdVsSampleList, tempProjectId);
                    for (Sample sample : samplesForProject) {
                        String sampleKingdom = null;
                        String sampleGenus = null;
                        String sampleSpecies = null;
                        int completedCount = 0;
                        int ongoingCount = 0;

                        List<SampleAttribute> sampleAttributes = sampleIdVsAttributeList.get(sample.getSampleId());
                        if (sampleAttributes != null && sampleAttributes.size() > 0) {
                            for (SampleAttribute sa : sampleAttributes) {
                                if(sa.getMetaAttribute()==null)
                                    continue;
                                tempLookupValue = sa.getMetaAttribute().getLookupValue();
                                Object sav = ModelValidator.getModelValue(tempLookupValue, sa);

                                String lookupValueName = tempLookupValue.getName();
                                if(SAMPLE_STATUS.equals(lookupValueName)) {
                                    String currStatus = ((String)sav).toLowerCase();
                                    if(currStatus.equals("completed")) {
                                        completedCount++;
                                    } else {
                                        if(!currStatus.equals("deprecated")) {
                                            ongoingCount++;
                                        }
                                    }
                                }
                                //for Viral project, use int attribute value
                                else if("In Progress Samples".equals(lookupValueName)) {
                                    completedCount = (Integer)sav;
                                } else if("Published Samples".equals(lookupValueName)) {
                                    ongoingCount = (Integer)sav;
                                }

                                else if("Kingdom".equals(lookupValueName)) {
                                    sampleKingdom = (String)sav;
                                } else if("Genus".equals(lookupValueName) || "Family".equals(lookupValueName)) {
                                    sampleGenus = (String)sav;
                                } else if("Species".equals(lookupValueName)) {
                                    sampleSpecies = (String)sav;
                                }
                            }

                            if(sampleKingdom!=null && sampleGenus!=null && sampleSpecies!=null) {
                                String concatKGS = sampleKingdom+";"+sampleGenus+";"+sampleSpecies;
                                Species currSpecies = speciesMap.get(concatKGS);
                                if(currSpecies==null) {
                                    currSpecies = new Species();
                                    currSpecies.setKingdom(sampleKingdom);
                                    currSpecies.setGenus(sampleGenus);
                                    currSpecies.setSpecies(sampleSpecies);
                                    currSpecies.setUrl(projectUrl);
                                    speciesMap.put(concatKGS, currSpecies);
                                }
                                currSpecies.ongoing+=ongoingCount;
                                currSpecies.completed+=completedCount;
                            }
                        }
                    }
                }
            }

            Map<String, Species> kingdoms = new HashMap<String, Species>();

            List<JSONObject> speciesList = new ArrayList<JSONObject>();

            //TODO: return object with parent-child information rather than kingdom-genus relationship
            for(Species currSpecies : speciesMap.values()) {
                JSONObject currSpeciesJson = new JSONObject();
                currSpeciesJson.put("s_k", currSpecies.getKingdom());
                currSpeciesJson.put("s_g", currSpecies.getGenus());
                currSpeciesJson.put("s_s", currSpecies.getSpecies());
                currSpeciesJson.put("s_cp", currSpecies.getCompleted());
                currSpeciesJson.put("s_og", currSpecies.getOngoing());
                currSpeciesJson.put("s_u", currSpecies.getUrl());

                Species kingdomSum = kingdoms.get(currSpecies.getKingdom());
                if(kingdomSum==null) {
                    kingdomSum = new Species();
                    kingdomSum.setKingdom(currSpecies.getKingdom());
                    kingdoms.put(kingdomSum.getKingdom(), kingdomSum);
                }
                kingdomSum.ongoing += currSpecies.getOngoing();
                kingdomSum.completed += currSpecies.getCompleted();
                speciesList.add(currSpeciesJson);
            }

            //summary data
            JSONObject summary = new JSONObject();
            for(Species species : kingdoms.values()) {
                JSONObject kingdomSum = new JSONObject();
                kingdomSum.put("ongoing", species.getOngoing());
                kingdomSum.put("completed", species.getCompleted());
                summary.put(species.getKingdom(), kingdomSum);
            }

            JSONObject json = new JSONObject();
            json.put("sum", summary);
            json.put("p", speciesList);

            File directory = new File(filePath);
            if (!directory.exists() || !directory.isDirectory()) {
                if ((new File(directory.getParent())).canWrite())
                    directory.mkdir();
                else
                    throw new Exception();
            }
            //Json file Creation
            File tempFile = new File(filePath + File.separator + "kingdom_temp.json");
            FileWriter fileWriter = new FileWriter(tempFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(json.toString());
            bufferedWriter.close();
            fileWriter.close();

            if (tempFile.exists() && tempFile.length() > 0) {
                File dataFile = new File(filePath + File.separator + "kingdom.json");
                tempFile.renameTo(dataFile);
            }
            logger.info("[JsonProducer-MBean] kingdomHelper process finished");
        } catch(Exception ex) {
            logger.info("[JsonProducer-MBean] kingdomHelper failed.");
            ex.printStackTrace();
        }
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
        List<EventAttribute> allEventAttributes = pseEjb.getEventAttributes(allEventIds, projectId);
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

    private Map<Long, List<Event>> getSampleIdVsEventList(List<Long> sampleIdList) throws Exception {
        // Get all events for all samples, and remarshal them into a map of sample vs event.
        List<Event> allSampleEvents = pseEjb.getEventsForSamples(sampleIdList);
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

    private List<Long> getSampleIdList(List<Sample> samples) {
        List<Long> sampleIdList = new ArrayList<Long>();
        for (Sample sample : samples) {
            sampleIdList.add(sample.getSampleId());
        }
        return sampleIdList;
    }

    private Map<Long, List<SampleAttribute>> getSampleVsAttributeList(List<Long> sampleIdList) throws Exception {
        // Get all sample attributes for all samples, and remarshal them into a map of sample vs attributes.
        List<SampleAttribute> allSampleAttributes = pseEjb.getSampleAttributes(sampleIdList);
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

    private List<Sample> getSamplesFromList(Map<Long, List<Sample>> projectIdVsSampleList, Long projectId) {
        List<Sample> samplesForCurrentProject = projectIdVsSampleList.get(projectId);
        if (samplesForCurrentProject == null) {
            samplesForCurrentProject = Collections.EMPTY_LIST;
        }
        return samplesForCurrentProject;
    }

    @Override
    public void perform(Date date, long l) {

        try {
            JsonProducer jsonProducer = new JsonProducer();
            jsonProducer.generateJson();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private String convertIntoATag(String url, String displayValue) {
        return (
                String.format(Constants.A_TAG_HTML, "#",
                        String.format(Constants.NEW_WINDOW_LINK_HTML, url)
                )
                        + displayValue
                        + Constants.A_TAG_CLOSING_HTML
        ).replaceAll("\\\"", "\\\\\"");
    }

    private String convertIntoNewWindowTag(String url) {
        return String.format(Constants.NEW_WINDOW_LINK_HTML, url);
    }

    /**
     * Displays "LINK" for published data only. "Submitted" for event that has been submitted, empty otherwise.
     *
     * @param status "published"/"submitted"/unknown
     * @param url    event specific external link url
     * @return String value according to status and given url
     */
    private String displayLinkOnlyForPublished(String status, String url) {
        String rtnVal = null;
        if(status.equals("")) {
            rtnVal = status;
        } else {
            String loweredStatus = status.trim().toLowerCase();

            if ("submitted".equals(loweredStatus))
                rtnVal = "submitted";
            else if ("published".equals(loweredStatus))
                rtnVal = convertIntoATag(url, "LINK");
            else
                rtnVal = status;
        }

        return rtnVal;
    }

    class Species {
        private String kingdom;
        private String genus;
        private String species;
        private int ongoing;
        private int completed;
        private String url;

        public String getKingdom() { return kingdom; }
        public void setKingdom(String kingdom) { this.kingdom = kingdom; }
        public String getGenus() { return genus; }
        public void setGenus(String genus) { this.genus = genus; }
        public String getSpecies() { return species; }
        public void setSpecies(String species) { this.species = species; }
        public int getOngoing() { return ongoing; }
        public void setOngoing(int ongoing) { this.ongoing = ongoing; }
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
        public void addOngoing() { this.ongoing++; }
        public void addCompleted() { this.completed++; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
