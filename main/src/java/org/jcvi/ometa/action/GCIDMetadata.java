package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.tools.jxc.apt.Const;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.*;
import java.util.*;

/**
 * Created by mkuscuog on 3/9/2015.
 */
public class GCIDMetadata extends ActionSupport {
    private Logger logger = Logger.getLogger(GCIDMetadata.class);

    private ReadBeanPersister readPersister;
    private final Long bioSampleLookupValueId = 9132686064399L;
    private final Long bioProjectLookupValueId = 9132686064394L;
    private String attrFilePath;
    private String attrMappingFilePath;
    private String bioProjectFilePath;
    private String fileStoragePath;

    private String format;
    private String bioSampleId;
    private String bioProjectId;
    private List<Map> pageElementList;
    private String jObject;
    private InputStream dataTemplateStream;

    private List<String> outputAttributes;
    private Map<String, String> attrNameMapping;

    private final String genbankExcelName = Constants.ATTR_GENBANK_ASSEMBLY_ACESSION;
    private final String[] genbankDatabaseNames = {Constants.ATTR_GENBANK_XREF_CHROMOSOMES,
            Constants.ATTR_GENBANK_XREF_PLASMIDS,Constants.ATTR_GENBANK_XREF_WGS};

    public GCIDMetadata() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
        this.fileStoragePath = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH); //file storage area
        this.attrFilePath = props.getProperty(Constants.CONFIG_GCIDMETADATA_OUTPUTATTR_FILEPATH); //csv file area
        this.bioProjectFilePath = props.getProperty(Constants.CONFIG_GCIDMETADATA_BIOPROJECTFILE_FILEPATH); // bioproject id file area
        this.attrMappingFilePath = props.getProperty(Constants.CONFIG_GCIDMETADATA_ATTRMAPPING_FILEPATH); //attribute mapping file area
    }

    public String getMetadata(){
        String rtnVal = SUCCESS;
        boolean bioSample = false;

        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();

            if(userName == null) throw new LoginRequiredException(Constants.LOGIN_REQUIRED_MESSAGE);
            if(bioSampleId != null && !bioSampleId.isEmpty() && !bioSampleId.equals("")) bioSample = true;

            List<Event> eventList ;
            if(bioSample) {
                eventList = readPersister.getEventByLookupValue(bioSampleLookupValueId, bioSampleId);
            } else {
                eventList = readPersister.getEventByLookupValue(bioProjectLookupValueId, bioProjectId);
                this.generateAttrMapping();
            }

            List<Long> projectIds = new ArrayList<Long>();
            List<Project> projects = new ArrayList<Project>();
            this.getOutputAttributes();
            LookupValue tempLookupValue;

            for(Event event : eventList){
                Long projectId = event.getProjectId();
                if(!projectIds.contains(projectId)) {
                    projectIds.add(projectId);
                    projects.add(readPersister.getProject(projectId));
                }
            }

            List<ProjectAttribute> allProjectAttributes = readPersister.getProjectAttributes(projectIds);
            Map<Long, List<ProjectAttribute>> projIdVsAttributes = new LinkedHashMap<Long, List<ProjectAttribute>>();
            for (ProjectAttribute pa : allProjectAttributes) {
                List<ProjectAttribute> paList = projIdVsAttributes.get(pa.getProjectId());
                if (paList == null) {
                    paList = new ArrayList<ProjectAttribute>();
                    projIdVsAttributes.put(pa.getProjectId(), paList);
                }
                paList.add(pa);
            }

            List<Sample> allSamplesAllProjects = readPersister.getSamplesForProjects(projectIds);
            Map<Long, List<Sample>> projectIdVsSampleList = new LinkedHashMap<Long, List<Sample>>();
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
                Map<String, Object> projectAttrMap = new LinkedHashMap<String, Object>();
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
                        Map<String, Object> sampleAttrMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
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

                                sampleAttrMap.putAll(CommonTool.getAttributeValueMap(eventAttributes, sample.getProjectId(), sample.getSampleName(), false, new String[]{"Sample Status"}));
                            }
                        }
                        if(bioSample && sampleAttrMap.containsKey(Constants.ATTR_BIOSAMPLE_ID)){
                            //Only keep requested attributes
                            if(!this.outputAttributes.isEmpty())
                                sampleAttrMap.keySet().retainAll(this.outputAttributes);
                            tempSampleAttrList.add(sampleAttrMap);
                        } else if(!bioSample && sampleAttrMap.containsKey(Constants.ATTR_BIOPROJECT_ID)){
                            tempSampleAttrList.add(sampleAttrMap);
                        }
                    }
                }
            }

            if(tempSampleAttrList.isEmpty()){
                Map<String, Object> noDataMap = new LinkedHashMap<String, Object>();
                noDataMap.put("NO DATA!", "NO DATA");
                tempSampleAttrList.add(noDataMap);
            }

            this.setPageElementList(tempSampleAttrList);

            if(!bioSample){
                this.generateExcelForBioProject();

                rtnVal = "bioproject-excel";
            } else if(format != null && format.toLowerCase().equals(Constants.EXCEL)) {
                rtnVal = "excel";
            } else if(format != null && format.toLowerCase().equals(Constants.CSV)){
                this.generateCSVForBioSample();

                rtnVal = "csv";
            } else {
                rtnVal = "json";
            }
        } catch (ForbiddenResourceException fre) {
            logger.error(Constants.DENIED_USER_VIEW_MESSAGE);
            addActionError(Constants.DENIED_USER_VIEW_MESSAGE);
        } catch (LoginRequiredException lre) {
            logger.error(Constants.LOGIN_REQUIRED_MESSAGE);
            rtnVal = LOGIN;
        } catch (Exception ex){
            logger.error("Exception in GCID Metadata : " + ex.toString());
            ex.printStackTrace();
        }

        return rtnVal;
    }

    public void getOutputAttributes(){
        this.outputAttributes = new ArrayList<String>(0);
        BufferedReader br = null;

        try {
            br =  new BufferedReader(new FileReader(fileStoragePath + attrFilePath));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                String[] attributes = inputLine.split(",");

                for(String attr : attributes)
                    this.outputAttributes.add(attr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e.toString());
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void generateCSVForBioSample(){
        StringBuffer dataBuffer = new StringBuffer();

        //writes column headers
        if(this.outputAttributes.isEmpty()){
            Map<String, Object> map = this.pageElementList.get(0);
            for(String attr : map.keySet()){
                dataBuffer.append(attr + ",");
            }
        } else{
            for(String attr : this.outputAttributes){
                dataBuffer.append(attr + ",");
            }
        }
        dataBuffer.append("\n");

        //write requested attributes
        for(Map map : this.pageElementList) {
            for(String attr : this.outputAttributes){
                dataBuffer.append(map.get(attr) + ",");
            }
            dataBuffer.append("\n");
        }
        this.dataTemplateStream = IOUtils.toInputStream(dataBuffer.toString());
    }

    public void generateExcelForBioProject(){
        ByteArrayOutputStream bos = null;
        FileInputStream file = null;

        try {
            file = new FileInputStream(new File(fileStoragePath + bioProjectFilePath));

            //Get the workbook instance for XLSM file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get sheets for Project and Sample from the workbook
            XSSFSheet projectSheet = workbook.getSheetAt(0);
            XSSFSheet sampleSheet = workbook.getSheetAt(1);

            //Get the project row includes attribute names
            Row projectAttrRow = projectSheet.getRow(2);

            //Get the sample row includes attribute names
            Row sampleAttrRow = sampleSheet.getRow(3);

            int rowIndex = 3;
            //For each row, iterate through each columns
            for (Map map : this.pageElementList) {
                Row projectValRow = projectSheet.getRow(rowIndex);
                Row sampleValRow = sampleSheet.getRow(rowIndex + 1);

                if (projectValRow == null)
                    projectValRow = projectSheet.createRow(rowIndex);

                if (sampleValRow == null)
                    sampleValRow = sampleSheet.createRow(rowIndex + 1);

                createAttrValRow(map, projectAttrRow, projectValRow);
                createAttrValRow(map, sampleAttrRow, sampleValRow);

                //Increase row if there are more data to add
                rowIndex++;
            }

            bos = new ByteArrayOutputStream();
            workbook.write(bos);
            this.dataTemplateStream = new ByteArrayInputStream(bos.toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
            try {
                if(bos != null) bos.close();
                if(file != null) file.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.toString());
            }
        }
    }

    private void generateAttrMapping(){
        this.attrNameMapping = new LinkedHashMap<String, String>();
        BufferedReader br = null;

        try {
            br =  new BufferedReader(new FileReader(fileStoragePath + attrMappingFilePath));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                String[] attributes = inputLine.split(",");

                this.attrNameMapping.put(attributes[0], attributes[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e.toString());
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createAttrValRow(Map map, Row attrRow, Row valRow){
        //Write attribute value to related cell
        for(int index = 0; index < attrRow.getLastCellNum(); index++){
            Cell attrCell = attrRow.getCell(index);
            Cell valCell = valRow.getCell(index);

            if(valCell == null){
                valCell = valRow.createCell(index);
                valCell.setCellType(Cell.CELL_TYPE_STRING);
            }

            String attrName = attrCell.getStringCellValue();


            if (map.containsKey(attrName)) {
                valCell.setCellValue(
                        (String) map.get(attrName));
            } else if (attrName.equals(this.genbankExcelName)) { //Check if cell is related to genbank
                StringBuilder sb = new StringBuilder();
                String delim = "";

                for (int i = 0; i < this.genbankDatabaseNames.length; i++) {
                    String genbankDatabaseName = this.genbankDatabaseNames[i];

                    if (map.containsKey(genbankDatabaseName)) {
                        sb.append(delim).append(
                                (String) map.get(genbankDatabaseName));
                        delim = ",";
                    }
                }

                valCell.setCellValue(sb.toString());
            } else {
                //Check if column name is mapped
                if (this.attrNameMapping.containsKey(attrName)) {
                    String attrMapVal = this.attrNameMapping.get(attrName);

                    attrCell.setCellValue(attrMapVal);
                    if (map.containsKey(attrMapVal)) {
                        valCell.setCellValue(
                                (String) map.get(attrMapVal));
                    }
                }
            }
        }
    }

    private Map<Long, List<SampleAttribute>> getSampleVsAttributeList(List<Long> sampleIdList) throws Exception {
        // Get all sample attributes for all samples, and remarshal them into a map of sample vs attributes.
        List<SampleAttribute> allSampleAttributes = readPersister.getSampleAttributes(sampleIdList);
        Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = new LinkedHashMap<Long, List<SampleAttribute>>();
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
        Map<Long, List<Event>> sampleIdVsEventList = new LinkedHashMap<Long, List<Event>>();
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
        Map<Long, List<EventAttribute>> eventIdVsAttributes = new LinkedHashMap<Long, List<EventAttribute>>();
        for (EventAttribute ea : allEventAttributes) {
            List<EventAttribute> lea = eventIdVsAttributes.get(ea.getEventId());
            if (lea == null) {
                lea = new ArrayList<EventAttribute>();
                eventIdVsAttributes.put(ea.getEventId(), lea);
            }
            lea.add(ea);
        }
        if (eventIdVsAttributes.size() == 0) {
            logger.debug("Returning empty results from getEventIdVsAttributeList for input list of size " + allEventIds.size());
        }
        return eventIdVsAttributes;
    }

    public String getBioSampleId() {
        return bioSampleId;
    }

    public void setBioSampleId(String bioSampleId) {
        this.bioSampleId = bioSampleId;
    }

    public List<Map> getPageElementList() {
        return pageElementList;
    }

    public void setPageElementList(List<Map> pageElementList) {
        this.pageElementList = pageElementList;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getjObject() {
        return jObject;
    }

    public void setjObject(String jObject) {
        this.jObject = jObject;
    }

    public InputStream getDataTemplateStream() {
        return dataTemplateStream;
    }

    public void setDataTemplateStream(InputStream dataTemplateStream) {
        this.dataTemplateStream = dataTemplateStream;
    }

    public String getBioProjectId() {
        return bioProjectId;
    }

    public void setBioProjectId(String bioProjectId) {
        this.bioProjectId = bioProjectId;
    }
}
