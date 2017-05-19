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

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.helper.AttributeHelper;
import org.jcvi.ometa.helper.AttributePair;
import org.jcvi.ometa.helper.EventLoadHelper;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.TemplatePreProcessingUtils;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 6/30/11
 * Time: 9:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventLoader extends ActionSupport implements Preparable {
    private ReadBeanPersister readPersister;
    private ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;

    // form values
    private String projectName;
    private String sampleName;
    private String eventName;
    private Long projectId;
    private Long eventId;

    private String status; // sample status value
    private String jobType; // form action type : insert, grid, file, template
    private String label;
    private String filter;

    private Project loadingProject; // single project loading
    private Sample loadingSample; // single sample loading
    private List<FileReadAttributeBean> beanList; // form
    private List<GridBean> gridList; // grid

    // template files
    private File dataTemplate;
    private InputStream dataTemplateStream;
    private String dataTemplateFileName;
    private String dataTemplateContentType;

    private String fileStoragePath;
    private ArrayList<String> loadedFiles;

    private String ids;
    private String sampleArrIndex;
    /*private Long defaultProjectId;*/

    /* final string values from the form*/
    private final String SUBMISSION_TYPE_GRID = "grid";
    private final String SUBMISSION_TYPE_FILE = "file";
    private final String SUBMISSION_TYPE_FORM = "form";
    private final String TEMPLATE_DOWNLOAD = "template";
    private final String EXPORT_SAMPLE = "export";

    private final String SUBMISSION_STATUS_SAVE = "save";
    private final String SUBMISSION_STATUS_VALIDATE = "validate";
    private final String SUBMISSION_STATUS_SUBMIT = "submit";

    private Logger logger = Logger.getLogger(EventLoader.class);

    public EventLoader(ReadBeanPersister persister, ProjectSampleEventWritebackBusiness writeEjb) {
        this.readPersister = persister;
        this.psewt = writeEjb;
    }

    public EventLoader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);

        fileStoragePath = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH); //file storage area
        //defaultProjectId = Long.parseLong(props.getProperty(Constants.CONFIG_DEFAULT_PROJECT_ID));

        UploadActionDelegate udelegate = new UploadActionDelegate();
        psewt = udelegate.initializeBusinessObject(logger, psewt);
    }

    @Override
    public void prepare() throws Exception {
        //get project list for the drop down box
        List<String> projectNameList = new ArrayList<String>();
        if (projectNames == null || projectNames.equals("")) {
            projectNameList.add("ALL");
        } else if (projectNames.contains(",")) {
            projectNameList.addAll(Arrays.asList(projectNames.split(",")));
        } else {
            projectNameList.add(projectNames);
        }

        String userName = ServletActionContext.getRequest().getRemoteUser();
        projectList = readPersister.getAuthorizedProjects(userName, AccessLevel.View);
        // projectList = readPersister.getProjects(projectNameList);
    }

    /**
     * Setup a download filename to fully-indicate type of event.  See also: struts.xml
     */
    public String getDataTemplateFileName() {
        return this.dataTemplateFileName;
    }

    public String execute() {
        String rtnVal = SUCCESS;
        UserTransaction tx = null;

        try {
            this.sampleName = (this.sampleName == null || this.sampleName.isEmpty() || this.sampleName.equals("0") ? null : this.sampleName);

            if(this.filter != null && this.filter.equals("pr")) {
                //this.projectId = this.defaultProjectId;
                //this.eventName = Constants.EVENT_PROJECT_REGISTRATION;
            }

            if (jobType != null) {
                boolean isProjectRegistration = eventName.contains(Constants.EVENT_PROJECT_REGISTRATION);
                boolean isSampleRegistration = eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION);

                String userName = ServletActionContext.getRequest().getRemoteUser();

                if(this.projectName==null || this.projectName.equals("0") || eventName==null || eventName.equals("0"))
                    throw new Exception(ErrorMessages.PROJECT_OR_EVENT_NOT_SELECTED);

                if (jobType.equals(SUBMISSION_TYPE_FORM)) { //loads single event
                    tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();

                    this.gridList = null; // force grid list to be empty
                    MultiLoadParameter loadParameter = new MultiLoadParameter();
                    EventLoadHelper loadHelper = new EventLoadHelper(this.readPersister);
                    loadHelper.setSubmissionId(Long.toString(CommonTool.getGuid()));

                    // manually create grid list then delegate it to the helper
                    List<GridBean> singleGridList = new ArrayList<GridBean>(1);
                    GridBean singleGridBean = new GridBean();

                    singleGridBean.setProjectName(isProjectRegistration ? this.loadingProject.getProjectName() : this.projectName);
                    if(isProjectRegistration) {
                        singleGridBean.setProjectPublic(Integer.toString(this.loadingProject.getIsPublic()));
                    }
                    singleGridBean.setSampleName(isSampleRegistration ? this.loadingSample.getSampleName() : this.sampleName);
                    if(isSampleRegistration) {
                        singleGridBean.setSamplePublic(Integer.toString(this.loadingSample.getIsPublic()));
                        singleGridBean.setParentSampleName(this.loadingSample.getParentSampleName());
                    }
                    singleGridBean.setBeanList(this.beanList);
                    singleGridList.add(singleGridBean);

                    loadHelper.gridListToMultiLoadParameter(loadParameter, singleGridList, this.projectName, this.eventName, this.status, userName);
                    psewt.loadAll(null, loadParameter);


                    this.pageDataReset(isProjectRegistration, isSampleRegistration, this.status);

                    if(isSampleRegistration) { // set sample name for sample update form load with newly loaded sample
                        this.sampleName = singleGridBean.getSampleName();
                    }

                    addActionMessage(this.getResultMessage());

                } else if(jobType.equals(SUBMISSION_TYPE_GRID)) { //loads multiple events from grid view
                    tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();

                    //delegate populating multiload parameter to the helper
                    MultiLoadParameter loadParameter = new MultiLoadParameter();
                    EventLoadHelper loadHelper = new EventLoadHelper(this.readPersister);

                    loadHelper.gridListToMultiLoadParameter(loadParameter, this.gridList, this.projectName, this.eventName, this.status, userName);
                    psewt.loadAll(null, loadParameter);

                    if(!filter.equals("su"))this.pageDataReset(isProjectRegistration, isSampleRegistration, this.status);

                    addActionMessage(this.getResultMessage());

                } else if (jobType.equals(SUBMISSION_TYPE_FILE)) { //loads data from a CSV file to grid view
                    if(!this.dataTemplate.canRead()) {
                        throw new Exception(ErrorMessages.EVENT_LOADER_FILE_READ);
                    } else {
                        try {
                            TemplatePreProcessingUtils templateUtil = new TemplatePreProcessingUtils();
                            gridList = templateUtil.parseEventFile(
                                    this.dataTemplateFileName, this.dataTemplate,
                                    this.projectName, isProjectRegistration, isSampleRegistration
                            );
                            jobType = SUBMISSION_TYPE_GRID;
                        } catch(Exception ex) {
                            throw ex;
                        }
                    }

                } else if(jobType.startsWith(TEMPLATE_DOWNLOAD)) { //download template
                    List<EventMetaAttribute> emaList = this.readPersister.getEventMetaAttributes(this.projectName, this.eventName);
                    emaList = CommonTool.filterEventMetaAttribute(emaList, "template");
                    //CommonTool.sortEventMetaAttributeByOrder(emaList);

                    /*
                     * removing the sanity check on sample requirement since multiple sample support is in action
                     * by hkim 5/2/13
                    ModelValidator validator = new ModelValidator();
                    validator.validateEventTemplateSanity(emaList, projectName, sampleName, eventName);
                    */

                    TemplatePreProcessingUtils templateUtil = new TemplatePreProcessingUtils();
                    String templateType = this.jobType.substring(jobType.indexOf("_")+1);
                    this.dataTemplateStream = templateUtil.buildFileContent(templateType, emaList, this.projectName, this.sampleName, this.eventName);
                    this.dataTemplateContentType = "application/octet-stream"; //templateType.equals("e") ? "application/vnd.ms-excel" : "text/csv";
                    this.dataTemplateFileName = eventName.replaceAll(" ", "_") + "_template." + (jobType.endsWith("e") ? "xls" : "csv");

                    rtnVal = Constants.STRUTS_FILE_DOWNLOAD;
                } else if(jobType.startsWith(EXPORT_SAMPLE)) { //export sample
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ZipOutputStream zos = new ZipOutputStream(baos);
                    List<EventMetaAttribute> emaList = this.readPersister.getEventMetaAttributes(this.projectName, this.eventName);
                    emaList = CommonTool.filterEventMetaAttribute(emaList, "template");

                    TemplatePreProcessingUtils templateUtil = new TemplatePreProcessingUtils();
                    List<String> templateLines = IOUtils.readLines(templateUtil.buildFileContent("c", emaList, this.projectName, this.sampleName, this.eventName));
                    StringBuffer newTemplateBuffer = new StringBuffer();

                    for(int i = 0;i < 2;i++) { //only writes column headers and descriptions
                        newTemplateBuffer.append(templateLines.get(i)).append("\n");
                    }

                    this.dataTemplateContentType = "application/zip";

                    if(ids != null && !ids.isEmpty()) { //project or sample edit from EventDetail
                        StringBuffer dataBuffer = new StringBuffer();

                        AttributeHelper attributeHelper = new AttributeHelper(this.readPersister);
                        List<AttributePair> pairList = attributeHelper.getAllAttributeByIDs(this.projectId, this.eventId, this.ids, "s");
                        if(pairList != null) {
                            for(AttributePair pair : pairList) {
                                dataBuffer.append(pair.getProjectName() + ",");
                                Sample currSample = pair.getSample();
                                dataBuffer.append(currSample.getSampleName() + ",");
                                if(isSampleRegistration) {
                                    dataBuffer.append((currSample.getParentSampleName() == null) ? "," : currSample.getParentSampleName() + ",");
                                    dataBuffer.append(currSample.getIsPublic() + ",");
                                }
                                List<FileReadAttributeBean> attributeList = pair.getAttributeList();
                                Map<String, String> attributeMap = AttributeHelper.attributeListToMap(attributeList);

                                boolean sampleFolderCreated = false;

                                for(EventMetaAttribute ema : emaList) {
                                    String attributeName = ema.getLookupValue().getName();
                                    if(ema.getLookupValue().getDataType().equals("file")){
                                        if(attributeMap.containsKey(attributeName)) {
                                            String[] paths = attributeMap.get(attributeName).split(",");

                                            if(!paths[0].equals("")) {
                                                File file = null;
                                                byte[] bytes;
                                                FileInputStream fis;

                                                if (!sampleFolderCreated) {
                                                    zos.putNextEntry(new ZipEntry(currSample.getSampleName() + "/"));
                                                    sampleFolderCreated = true;
                                                }

                                                zos.putNextEntry(new ZipEntry(currSample.getSampleName() + "/" + attributeName + "/"));

                                                StringBuilder dataBufferFilePath = new StringBuilder();
                                                String delim = "";

                                                for (String path : paths) {
                                                    String absoluteFilePath = this.fileStoragePath + File.separator + Constants.DIRECTORY_PROJECT + File.separator + File.separator + path;
                                                    String name = path.substring(path.lastIndexOf(File.separator) + 1);

                                                    file = new File(absoluteFilePath);

                                                    if (file.exists()) {
                                                        String zipPath = currSample.getSampleName() + "/" + attributeName + "/" + name;
                                                        fis = new FileInputStream(file);
                                                        bytes = IOUtils.toByteArray(fis);
                                                        zos.putNextEntry(new ZipEntry(zipPath));
                                                        zos.write(bytes);

                                                        dataBufferFilePath.append(delim).append("/" + zipPath);
                                                        delim = ",";
                                                    }
                                                }

                                                zos.closeEntry();

                                                dataBuffer.append("\"" + dataBufferFilePath.toString() + "\"");
                                            }
                                        } else{
                                            dataBuffer.append("");
                                        }
                                    } else {
                                        dataBuffer.append(attributeMap.containsKey(attributeName) ? "\"" + attributeMap.get(attributeName) + "\"" : "");
                                    }
                                    dataBuffer.append(",");
                                }
                                dataBuffer.append("\n");
                            }
                        }

                        newTemplateBuffer.append(dataBuffer);
                    }

                    zos.putNextEntry(new ZipEntry(this.eventName + "_samples.csv"));
                    zos.write(newTemplateBuffer.toString().getBytes());
                    zos.closeEntry();
                    zos.flush();
                    zos.close();
                    this.dataTemplateStream = new ByteArrayInputStream(baos.toByteArray());
                    this.dataTemplateFileName = eventName.replaceAll(" ", "_") + "_export.zip";

                    rtnVal = Constants.STRUTS_FILE_DOWNLOAD;

                } else if(jobType.equals("projectedit")) {
                    AttributeHelper attributeHelper = new AttributeHelper(this.readPersister);
                    if(this.eventId == null && this.eventName != null) {
                        LookupValue eventLV = this.readPersister.getLookupValue(this.eventName, Constants.LOOKUP_VALUE_TYPE_EVENT_TYPE);
                        if(eventLV != null) {
                            this.eventId = eventLV.getLookupValueId();
                        }
                    }
                    List<AttributePair> projectPairList = attributeHelper.getAllAttributeByIDs(this.projectId, this.eventId, "" + this.projectId, "p");
                    if(projectPairList.size() > 0) {
                        AttributePair projectPair = projectPairList.get(0);
                        this.beanList = projectPair.getAttributeList();
                    }
                    jobType = SUBMISSION_TYPE_FORM;
                }
            }

            if(ids != null && ids.length() > 0) {
                jobType = SUBMISSION_TYPE_GRID;
            }
        } catch (Exception ex) {

            if(loadedFiles!=null && loadedFiles.size()>0) { //deletes uploaded files in event of error
                for(String filePath : loadedFiles) {
                    File tempFile = new File(fileStoragePath + filePath);
                    if(tempFile.exists())
                        tempFile.delete();
                }
            }

            try { //transaction rollback
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError(se.toString());
            }

            //<Date>:<Project>:<Sample>: <Type>:<User ID>:<Row Number>: <Data attribute Name>:<Error message>
            StringBuilder error = new StringBuilder(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + ":");
            error.append(this.projectName + ":");
            error.append((this.sampleName == null ? "" : this.sampleName) + ":");
            error.append(this.eventName + ":");

            String errorMsg = "";
            if (ex.getClass() == ForbiddenResourceException.class) {
                errorMsg = Constants.DENIED_USER_EDIT_MESSAGE;
                rtnVal = Constants.FORBIDDEN_ACTION_RESPONSE;
            } else {
                errorMsg = (ex.getClass() == DetailedException.class ? ((DetailedException)ex).getRowIndex() + ":" : "") +
                        (ex.getCause() == null ? ex.getMessage() : ex.getCause());
                rtnVal = ERROR;
            }

            addActionError(errorMsg.replaceAll("\\\n", "<br/>"));
            error.append(errorMsg);
            logger.error(error.toString());
        } finally {
            try {
                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    tx.commit();
                }

                if(jobType != null && jobType.equals(SUBMISSION_TYPE_GRID) && this.dataTemplate != null) {
                    this.dataTemplate.delete();
                    this.dataTemplate = null;
                    this.dataTemplateContentType = null;
                    this.dataTemplateFileName = null;
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return rtnVal;
    }

    private void pageDataReset(boolean isProjectRegistration, boolean isSampleRegistration, String status) {
        boolean resetIdsAndNames = true;
        boolean resetLists = true;

        if(status.equals(SUBMISSION_STATUS_SAVE) || status.equals(SUBMISSION_STATUS_VALIDATE)) {
            resetIdsAndNames = false;
            resetLists = false;
            if(isSampleRegistration) { //update registration event to update on save requests
                this.eventName = this.eventName.replaceAll(Constants.EVENT_SAMPLE_REGISTRATION, Constants.EVENT_SAMPLE_UPDATE);
                this.filter = "su";
            }
        } else {
            if(status.equals(SUBMISSION_STATUS_SUBMIT) && isProjectRegistration) { // do not reset project and event for project registration
                resetIdsAndNames = false;
            }
        }

        if(resetIdsAndNames) {
            /* Filter is not necessary for internal usage
            if((isSampleRegistration || this.eventName.contains(Constants.EVENT_SAMPLE_UPDATE)) && status.equals(SUBMISSION_STATUS_SUBMIT)) {
                this.filter = "sr";
            }*/

            projectId = null;
            projectName = null;
            eventId = null;
            eventName = null;
            sampleName = null;
        }
        if(resetLists) {
            beanList = null;
            gridList = null;
        }
    }

    private String getResultMessage() {
        String resultMessage;
        if(this.status.equals(SUBMISSION_STATUS_SUBMIT)) {
            resultMessage = "Data successfully submitted to the OMETA.";
        } else if(this.status.equals(SUBMISSION_STATUS_VALIDATE)) {
            resultMessage = "Data submission is validated.";
        } else {
            resultMessage = "Data submission is saved.";
        }
        return resultMessage;
    }

    private String getUnknownErrorMessage() {
        return "Unknown error uploading file " + this.dataTemplateFileName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public String getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(String projectNames) {
        this.projectNames = projectNames;
    }

    public List<FileReadAttributeBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<FileReadAttributeBean> beanList) {
        this.beanList = beanList;
    }

    public Project getLoadingProject() {
        return loadingProject;
    }

    public void setLoadingProject(Project loadingProject) {
        this.loadingProject = loadingProject;
    }

    public Sample getLoadingSample() {
        return loadingSample;
    }

    public void setLoadingSample(Sample loadingSample) {
        this.loadingSample = loadingSample;
    }

    public List<GridBean> getGridList() {
        return gridList;
    }

    public void setGridList(List<GridBean> gridList) {
        this.gridList = gridList;
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

    public void setDataTemplateContentType(String dataTemplateContentType) {
        this.dataTemplateContentType = dataTemplateContentType;
    }

    public String getDataTemplateContentType() {
        return dataTemplateContentType;
    }

    public void setDataTemplateFileName(String dataTemplateFileName) {
        this.dataTemplateFileName = dataTemplateFileName;
    }

    public void setDataTemplate(File dataTemplate) {
        this.dataTemplate = dataTemplate;
    }

    public InputStream getDataTemplateStream() {
        return dataTemplateStream;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSampleArrIndex() {
        return sampleArrIndex;
    }

    public void setSampleArrIndex(String sampleArrIndex) {
        this.sampleArrIndex = sampleArrIndex;
    }
}
