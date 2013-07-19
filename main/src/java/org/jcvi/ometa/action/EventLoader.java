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

import au.com.bytecode.opencsv.CSVReader;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.CsvPreProcessingUtils;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 6/30/11
 * Time: 9:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventLoader extends ActionSupport {
    private ReadBeanPersister readPersister;
    private ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;

    private String projectName;
    private String sampleName;
    private String eventName;
    private Long projectId;
    private Long eventId;

    private String jobType;
    private String label;

    private Project loadingProject;
    private Sample loadingSample;
    private List<FileReadAttributeBean> beanList;
    private List<GridBean> gridList;

    private InputStream downloadStream;
    private String downloadContentType;

    private File uploadFile;
    private String uploadFileName;

    private String fileStoragePath;
    private ArrayList<String> loadedFiles;

    private static final String DEFAULT_USER_MESSAGE = "Not yet entered";
    private final String MULTIPLE_SUBJECT_IN_FILE_MESSAGE = "Multiple projects/samples are found in the file";
    private final String UNSUPPORTED_UPLOAD_FILE_TYPE_MESSAGE = "File type is not supported. Supported file types are JPG, JPEG, GIF and BMP.";
    private String message = DEFAULT_USER_MESSAGE;

    private Logger logger = Logger.getLogger(EventLoader.class);

    public EventLoader(ReadBeanPersister persister, ProjectSampleEventWritebackBusiness writeBean) {
        this.readPersister = persister;
        this.psewt = writeBean;
    }

    public EventLoader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);

        fileStoragePath = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH); //file storage area

        UploadActionDelegate udelegate = new UploadActionDelegate();
        psewt = udelegate.initializeBusinessObject(logger, psewt);
    }

    /**
     * Setup a download filename to fully-indicate type of event.  See also: struts.xml
     */
    public String getDownloadFileName() {
        return eventName + "_EventAttributes." + (jobType.endsWith("c")?"csv":"xls");
    }

    public String execute() {
        String rtnVal = SUCCESS;
        UserTransaction tx = null;

        try {
            sampleName = sampleName!=null && sampleName.equals("0")?null:sampleName;

            if (jobType != null) {
                boolean isProjectRegistration = eventName.equals(Constants.EVENT_PROJECT_REGISTRATION);
                boolean isSampleRegistration = eventName.equals(Constants.EVENT_SAMPLE_REGISTRATION);

                if(projectName==null || projectName.equals("0") || eventName==null || eventName.equals("0"))
                    throw new Exception("Project or Event type is not selected.");

                if (jobType.equals("insert")) { //loads single event
                    tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();
                    psewt.loadAll(null, this.createMultiLoadParameter(projectName, loadingProject, loadingSample, beanList));
                    this.reset();
                } else if(jobType.equals("grid")) { //loads multiple events from grid view
                    tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();
                    for(GridBean gBean : gridList) {
                        if(gBean!=null) {
                            if(isProjectRegistration && gBean.getProjectName()!=null && gBean.getProjectPublic()!=null) {
                                loadingProject = new Project();
                                loadingProject.setProjectName(gBean.getProjectName());
                                loadingProject.setIsPublic(Integer.valueOf(gBean.getProjectPublic()));
                            } else if(isSampleRegistration && gBean.getSampleName()!=null && gBean.getSamplePublic()!=null) {
                                loadingSample = new Sample();
                                loadingSample.setSampleName(gBean.getSampleName());
                                loadingSample.setParentSampleName(gBean.getParentSampleName());
                                loadingSample.setIsPublic(Integer.valueOf(gBean.getSamplePublic()));
                            }
                            List<FileReadAttributeBean> fBeanList = gBean.getBeanList();
                            if(fBeanList!=null && fBeanList.size()>0) {
                                psewt.loadAll(null, this.createMultiLoadParameter(projectName, loadingProject,  loadingSample, fBeanList));
                            }
                        }
                    }
                    this.reset();

                } else if (jobType.equals("file")) { //loads data from a CSV file to grid view
                    if (!this.uploadFile.canRead()) {
                        throw new Exception("Error in reading the file.");
                    } else {
                        try {
                            CSVReader reader = new CSVReader(new FileReader(this.uploadFile));

                            int lineCount = 0;
                            List<String> columns = new ArrayList<String>();

                            String currProjectName = null;

                            gridList = new ArrayList<GridBean>();
                            boolean hasSampleName = false;
                            String[] line;
                            while ((line = reader.readNext()) != null) {
                                if (lineCount != 1) {
                                    if (lineCount == 0) {
                                        Collections.addAll(columns, line);
                                        hasSampleName = columns.indexOf("SampleName") >= 0;
                                    } else {
                                        int colIndex = 0;

                                        if (currProjectName == null || isProjectRegistration) {
                                            currProjectName = line[colIndex];
                                        }

                                        if (!isProjectRegistration && !currProjectName.equals(line[colIndex])) {
                                            throw new Exception(MULTIPLE_SUBJECT_IN_FILE_MESSAGE);
                                        }
                                        //move next column
                                        colIndex++;

                                        GridBean gBean = new GridBean();
                                        if (hasSampleName) {
                                            gBean.setSampleName(line[(colIndex++)]);
                                        }

                                        if (isProjectRegistration) {
                                            gBean.setProjectName(currProjectName);
                                            gBean.setProjectPublic(line[(colIndex++)]);
                                        } else if (isSampleRegistration) {
                                            gBean.setParentSampleName(line[(colIndex++)]);
                                            gBean.setSamplePublic(line[(colIndex++)]);
                                        }

                                        gBean.setBeanList(new ArrayList<FileReadAttributeBean>());
                                        for (; colIndex < columns.size(); colIndex++) {
                                            FileReadAttributeBean fBean = new FileReadAttributeBean();
                                            fBean.setProjectName(isProjectRegistration ? currProjectName : this.projectName);
                                            fBean.setAttributeName(columns.get(colIndex));
                                            fBean.setAttributeValue(line[colIndex]);
                                            gBean.getBeanList().add(fBean);
                                        }
                                        this.gridList.add(gBean);
                                    }
                                }
                                lineCount++;
                            }
                            jobType = "grid";
                        } catch (Exception ex) {
                            throw ex;
                        }
                    }
                } else if (jobType.equals("template")) { //download template
                    List<EventMetaAttribute> emaList = readPersister.getEventMetaAttributes(projectName, eventName);

                    /*
                     * removing the sanity check on sample requirement since multiple sample support is in action
                     * by hkim 5/2/13
                    ModelValidator validator = new ModelValidator();
                    validator.validateEventTemplateSanity(emaList, projectName, sampleName, eventName);
                    */

                    TemplatePreProcessingUtils cvsUtils = new TemplatePreProcessingUtils();
                    String templateType = jobType.substring(jobType.indexOf("_")+1);
                    downloadStream = cvsUtils.buildFileContent(templateType, emaList, projectName, sampleName, eventName);
                    downloadContentType = templateType.equals("c")?"csv":"vnd.ms-excel";
                    rtnVal = Constants.FILE_DOWNLOAD_MSG;
                }
            }

        } catch (Exception ex) {
            logger.error("Exception in EventLoader : " + ex.toString());
            ex.printStackTrace();
            if (ex.getClass() == ForbiddenResourceException.class) {
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
                return Constants.FORBIDDEN_ACTION_RESPONSE;
            } else if (ex.getClass() == ForbiddenResourceException.class) {
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
                return LOGIN;
            } else if (ex.getClass() == ParseException.class)
                addActionError(Constants.INVALID_DATE_MESSAGE);
            else {
                addActionError(ex.toString());
            }

            //deletes uploaded files in event of error
            if(loadedFiles!=null && loadedFiles.size()>0) {
                for(String filePath : loadedFiles) {
                    File tempFile = new File(fileStoragePath + filePath);
                    if(tempFile.exists())
                        tempFile.delete();
                }
            }

            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError(se.toString());
            }

            rtnVal = ERROR;
        } finally {
            try {
                //get project list for the drop down box
                List<String> projectNameList = new ArrayList<String>();
                if (projectNames == null || projectNames.equals("")) {
                    projectNameList.add("ALL");
                } else if (projectNames.contains(",")) {
                    projectNameList.addAll(Arrays.asList(projectNames.split(",")));
                } else {
                    projectNameList.add(projectNames);
                }
                projectList = readPersister.getProjects(projectNameList);

                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    tx.commit();
                }

                if(jobType!=null && jobType.equals("grid") && this.uploadFile!=null) {
                    this.uploadFile.delete();
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return rtnVal;
    }

    private MultiLoadParameter createMultiLoadParameter(String projectName, Project project, Sample sample, List<FileReadAttributeBean> frab) throws Exception {

        MultiLoadParameter loadParameter = new MultiLoadParameter();
        boolean isSampleRegistration = false;
        boolean isProjectRegistration = false;

        if (this.eventName.equals(Constants.EVENT_PROJECT_REGISTRATION) && project.getProjectName() != null && !project.getProjectName().isEmpty()) {
            isProjectRegistration = true;

            List<Project> projectList = new ArrayList<Project>();
            projectList.add(feedProjectData(project));
            loadParameter.addProjects(projectList);

            /*
             *   loads all event meta attributes from the parent
             *   by hkim 6/11/13
             */
            List<EventMetaAttribute> emas = this.readPersister.getEventMetaAttributes(projectName, null); //, Constants.EVENT_PROJECT_REGISTRATION);
            if (emas != null && emas.size() > 0) {
                List<EventMetaAttribute> newEmas = new ArrayList<EventMetaAttribute>();

                for (EventMetaAttribute ema : emas) {
                    EventMetaAttribute newEma = new EventMetaAttribute();
                    newEma.setProjectName(project.getProjectName());
                    newEma.setEventName(ema.getEventName());
                    newEma.setEventTypeLookupId(ema.getEventTypeLookupId());
                    newEma.setAttributeName(ema.getAttributeName());
                    newEma.setNameLookupId(ema.getNameLookupId());
                    newEma.setActive(ema.isActive());
                    newEma.setRequired(ema.isRequired());
                    newEma.setDesc(ema.getDesc());
                    newEma.setDataType(ema.getDataType());
                    newEma.setLabel(ema.getLabel());
                    newEma.setOntology(ema.getOntology());
                    newEma.setOptions(ema.getOptions());
                    newEma.setSampleRequired(ema.isSampleRequired());
                    newEmas.add(newEma);
                }
                loadParameter.addEventMetaAttributes(newEmas);
            } else {
                throw new Exception(
                        String.format("Event Metadata has not been set up for the parent project and the '%s' event type.", Constants.EVENT_PROJECT_REGISTRATION)
                );
            }

            List<ProjectMetaAttribute> pmas = this.readPersister.getProjectMetaAttributes(projectName);
            if (pmas != null && pmas.size() > 0) {
                List<ProjectMetaAttribute> newPmas = new ArrayList<ProjectMetaAttribute>();
                for (ProjectMetaAttribute pma : pmas) {
                    ProjectMetaAttribute newPma = new ProjectMetaAttribute();
                    newPma.setProjectName(project.getProjectName());
                    newPma.setAttributeName(pma.getAttributeName());
                    newPma.setDataType(pma.getDataType());
                    newPma.setDesc(pma.getDesc());
                    newPma.setLabel(pma.getLabel());
                    newPma.setNameLookupId(pma.getNameLookupId());
                    newPma.setOntology(pma.getOntology());
                    newPma.setOptions(pma.getOptions());
                    newPma.setRequired(pma.isRequired());
                    newPma.setActive(pma.isActive());
                    newPmas.add(newPma);
                }
                loadParameter.addProjectMetaAttributes(newPmas);
            }
        }
        else if (this.eventName.equals(Constants.EVENT_SAMPLE_REGISTRATION) && sample.getSampleName() != null && !sample.getSampleName().isEmpty()) {
            isSampleRegistration = true;

            List<Sample> sampleList = new ArrayList<Sample>();
            sampleList.add(feedSampleData(sample));
            loadParameter.addSamples(sampleList);
        }

        List<FileReadAttributeBean> loadingList = null;
        if (frab != null && frab.size() > 0) {
            loadingList = processFileReadBeans(
                    isProjectRegistration ? project.getProjectName() : projectName, isSampleRegistration ? sample.getSampleName() : this.sampleName, frab
            );
        }
        if (loadingList != null && loadingList.size() > 0) {
            if (isProjectRegistration) {
                loadParameter.addProjectRegistrations(Constants.EVENT_PROJECT_REGISTRATION, loadingList);
            } else if (isSampleRegistration) {
                loadParameter.addSampleRegistrations(Constants.EVENT_SAMPLE_REGISTRATION, loadingList);
            } else {
                loadParameter.addEvents(this.eventName, loadingList);
            }
        }
        return loadParameter;
    }

    private Project feedProjectData(Project project) throws Exception {
        project.setParentProjectName(projectName);

        Project parentProject = readPersister.getProject(projectName);
        project.setParentProjectId(parentProject.getProjectId());
        project.setProjectLevel(parentProject.getProjectLevel()+1);
        project.setEditGroup(parentProject.getEditGroup());
        project.setViewGroup(parentProject.getViewGroup());
        return project;
    }

    private Sample feedSampleData(Sample sample) throws Exception {
        sample.setProjectId(projectId);
        sample.setProjectName(projectName);

        //set project level by adding 1 to selected parent project's level
        if(sample.getParentSampleName()==null || sample.getParentSampleName().equals("0"))
            sample.setParentSampleName(null);

        String parentSampleName = sample.getParentSampleName();
        if (parentSampleName != null && !parentSampleName.isEmpty() && !parentSampleName.equals("0")) {
            Sample selectedParentSample = readPersister.getSample(projectId, parentSampleName);
            sample.setSampleLevel(selectedParentSample.getSampleLevel() + 1);
        } else {
            sample.setParentSampleId(null);
            sample.setSampleLevel(1);
        }

        return sample;
    }

    private List<FileReadAttributeBean> processFileReadBeans(String _projectName, String _sampleName, List<FileReadAttributeBean> loadingList) throws Exception {

        List<FileReadAttributeBean> processedList = new ArrayList<FileReadAttributeBean>();
        for(FileReadAttributeBean fBean:loadingList) {
            if(fBean.getProjectName()==null || eventName.equals(Constants.EVENT_PROJECT_REGISTRATION)) {
                fBean.setProjectName(_projectName);
            }
            if(fBean.getSampleName()==null) {
                fBean.setSampleName(_sampleName);
            }

            //handle file uploads
            if(fBean.getUpload()!=null && fBean.getUploadFileName()!=null && !fBean.getUploadFileName().isEmpty()) {
                fileStoragePath = fileStoragePath + (fileStoragePath.endsWith(File.separator)?"":File.separator);
                String originalFileName = fBean.getUploadFileName();

                String fileDirectoryPathProject = _projectName.replaceAll(" ", "_"); //project folder
                String fileDirectoryPathSample = fileDirectoryPathProject + File.separator +
                        (_sampleName!=null&&!_sampleName.isEmpty()?_sampleName.replaceAll(" ", "_"):"project"); //sample folder
                String fileDirectoryPath = fileDirectoryPathSample + File.separator + ModelValidator.PST_DEFAULT_DATE_FORMAT.format(new Date()); //date folder

                String fileName = originalFileName.substring(0,originalFileName.indexOf(".")) +
                        "_"+System.currentTimeMillis() +
                        originalFileName.substring(originalFileName.indexOf(".")); //append "_" + current time in milliseconds to file name

                File fileDirectory = new File(fileStoragePath + fileDirectoryPath);
                if(!fileDirectory.exists() || !fileDirectory.isDirectory()) {
                    fileDirectory.mkdirs();
                }

                File theFile = new File(fileDirectory.getPath() + File.separator + fileName);
                FileUtils.copyFile(fBean.getUpload(), theFile);

                if(theFile.exists() && theFile.isFile() && theFile.canRead()) {
                    fBean.getUpload().delete();

                    fBean.setAttributeValue(fileDirectoryPath + File.separator + fileName);
                    if(loadedFiles==null) {
                        loadedFiles = new ArrayList<String>();
                    }
                    loadedFiles.add(fBean.getAttributeValue());
                }
            }

            if (!fBean.getAttributeName().equals("0") && fBean.getAttributeValue()!=null && !fBean.getAttributeValue().isEmpty()) {
                processedList.add(fBean);
            }
        }
        return processedList;
    }

    private void reset() {
        projectId = null;
        eventId = null;
        beanList = null;
        gridList = null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    private String getUnknownErrorMessage() {
        return "Unknown error uploading file " + this.getUploadFileName();
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

    public InputStream getDownloadStream() {
        return downloadStream;
    }

    public void setDownloadStream(InputStream downloadStream) {
        this.downloadStream = downloadStream;
    }

    public String getDownloadContentType() {
        return downloadContentType;
    }

    public void setDownloadContentType(String downloadContentType) {
        this.downloadContentType = downloadContentType;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }
}
