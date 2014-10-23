package org.jcvi.ometa.helper;

import org.apache.commons.io.FileUtils;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.DPCCValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * User: movence
 * Date: 10/14/14
 * Time: 3:05 PM
 * org.jcvi.ometa.helper
 */
public class EventLoadHelper {
    private ReadBeanPersister readPersister;
    private String fileStoragePath;

    public EventLoadHelper(ReadBeanPersister readPersister) {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        if(readPersister == null) {
            this.readPersister = new ReadBeanPersister(props);
        } else {
            this.readPersister = readPersister;
        }
        this.fileStoragePath = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH);
    }

    public EventLoadHelper(ProjectSampleEventPresentationBusiness pseb) {
        this(new ReadBeanPersister(pseb));
    }

    public void gridListToMultiLoadParameter(MultiLoadParameter loadParameter, List<GridBean> gridList, String projectName, String eventName, Project loadingProject, Sample loadingSample) throws Exception {
        if(eventName == null || eventName.isEmpty()) {
            throw new Exception("event name is missing.");
        }

        boolean isProjectRegistration = eventName.contains(Constants.EVENT_PROJECT_REGISTRATION);
        boolean isProjectUpdate = eventName.contains(Constants.EVENT_PROJECT_UPDATE);
        boolean isSampleRegistration = eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION);

        int gridRowIndex = 0;
        for(GridBean gBean : gridList) {
            if(gBean!=null) {
                if(isProjectRegistration && gBean.getProjectName() != null) {
                    loadingProject = new Project();
                    loadingProject.setProjectName(gBean.getProjectName());
                    loadingProject.setIsPublic(gBean.getProjectPublic() == null ? 0 : Integer.valueOf(gBean.getProjectPublic()));
                } else {
                    if(projectName == null || projectName.isEmpty()) {
                        projectName = gBean.getProjectName();
                    }
                    loadingProject = this.readPersister.getProject(projectName);

                    if(isProjectUpdate) {
                        loadingSample = null;
                    } else {
                        if(gBean.getSampleName() == null || gBean.getSampleName().isEmpty()) {
                            continue;
                        }
                        Sample existingSample = this.readPersister.getSample(loadingProject.getProjectId(), gBean.getSampleName());
                        if(existingSample == null) {
                            if(isSampleRegistration) {
                                loadingSample = new Sample();
                                loadingSample.setSampleName(gBean.getSampleName());
                                loadingSample.setParentSampleName(gBean.getParentSampleName());
                                loadingSample.setIsPublic(Integer.valueOf(gBean.getSamplePublic()));
                                loadingSample.setSampleLevel(1);
                            }
                        } else {
                            loadingSample = existingSample;
                        }
                    }
                }

                List<FileReadAttributeBean> fBeanList = gBean.getBeanList();
                // process empty attribute lists events for project/sample registrations
                if((fBeanList != null && fBeanList.size() > 0) || isProjectRegistration || isSampleRegistration) {
                    this.createMultiLoadParameter(loadParameter, projectName, eventName, loadingProject,  loadingSample, fBeanList, null, ++gridRowIndex);
                }
            }
        }
    }

    public void createMultiLoadParameter(
            MultiLoadParameter loadParameter, String projectName, String eventName,
            Project project, Sample sample, List<FileReadAttributeBean> frab, String status, int rowIndex) throws Exception {

        loadParameter.setEventName(eventName);

        boolean isProjectRegistration = (eventName.contains(Constants.EVENT_PROJECT_REGISTRATION) && project.getProjectName() != null && !project.getProjectName().isEmpty());
        boolean isSampleRegistration = (eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION) && sample.getSampleName() != null && !sample.getSampleName().isEmpty());

        List<FileReadAttributeBean> loadingList = null;
        if (frab != null && frab.size() > 0) {
            loadingList = this.feedAndFilterFileReadBeans(eventName, isProjectRegistration ? project.getProjectName() : projectName, sample.getSampleName(), frab);
        }

        if (isProjectRegistration) {
            /*
            *   loads all meta attributes from the parent
            *   by hkim 6/11/13
            */
            Project parentProject = this.readPersister.getProject(projectName);

            List<EventMetaAttribute> emas = readPersister.getEventMetaAttributes(parentProject.getProjectId()); //, Constants.EVENT_PROJECT_REGISTRATION);
            List<EventMetaAttribute> newEmas = null;
            if (emas != null && emas.size() > 0) {
                newEmas = new ArrayList<EventMetaAttribute>(emas.size());
                for (EventMetaAttribute ema : emas) {
                    EventMetaAttribute newEma = CommonTool.createEMA(
                            null, project.getProjectName(), ema.getEventName(), ema.getAttributeName(),
                            ema.isRequired(), ema.isActive(), ema.getDataType(), ema.getDesc(),
                            ema.getOntology(), ema.getLabel(), ema.getOptions(), ema.isSampleRequired());
                    newEma.setEventTypeLookupId(ema.getEventTypeLookupId());
                    newEma.setNameLookupId(ema.getNameLookupId());
                    newEmas.add(newEma);
                }
            }
            List<SampleMetaAttribute> smas = readPersister.getSampleMetaAttributes(parentProject.getProjectId());
            List<SampleMetaAttribute> newSmas = null;
            if(smas != null && smas.size() > 0) {
                newSmas = new ArrayList<SampleMetaAttribute>(smas.size());
                for(SampleMetaAttribute sma : smas) {
                    SampleMetaAttribute newSma = new SampleMetaAttribute();
                    newSma.setProjectName(project.getProjectName());
                    newSma.setAttributeName(sma.getAttributeName());
                    newSma.setNameLookupId(sma.getNameLookupId());
                    newSma.setDataType(sma.getDataType());
                    newSma.setDesc(sma.getDesc());
                    newSma.setLabel(sma.getLabel());
                    newSma.setOntology(sma.getOntology());
                    newSma.setOptions(sma.getOptions());
                    newSma.setRequired(sma.isRequired());
                    newSma.setActive(sma.isActive());
                    newSmas.add(newSma);
                }
            }
            List<ProjectMetaAttribute> pmas = readPersister.getProjectMetaAttributes(parentProject.getProjectId());
            List<ProjectMetaAttribute> newPmas = null;
            if (pmas != null && pmas.size() > 0) {
                newPmas = new ArrayList<ProjectMetaAttribute>(pmas.size());
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
            }

            loadParameter.addProjectPair(feedProjectData(project, parentProject), loadingList, newPmas, newSmas, newEmas, rowIndex);
        } else {
            boolean listHasData = loadingList != null && loadingList.size() > 0;
            boolean isProjectLevel = eventName.toLowerCase().contains("project");
            boolean isStatusGiven = status != null && !status.isEmpty();

            if(isStatusGiven && (status.equals("submit") || status.equals("validate")) && listHasData && !isProjectLevel) { //DPCC data validation
                this.validateDataForDPCC(loadingList, rowIndex);
                if(isSampleRegistration || eventName.contains(Constants.EVENT_SAMPLE_UPDATE)) {
                    this.updateSampleStatus(loadingList, projectName, sample.getSampleName(), status, rowIndex);
                }
            }

            if(isSampleRegistration) {
                if(project == null || project.getProjectName() == null || project.getProjectName().isEmpty()) {
                    project = this.readPersister.getProject(projectName);
                }
                loadParameter.addSamplePair(feedSampleData(sample, project), loadingList, rowIndex);
            } else {
                if(listHasData)  {
                    loadParameter.addEvents(eventName, loadingList, rowIndex);
                }
            }
        }
    }

    private Project feedProjectData(Project project, Project parentProject) throws Exception {
        if(parentProject != null) {
            project.setParentProjectName(parentProject.getProjectName());
            project.setParentProjectId(parentProject.getProjectId());
            project.setProjectLevel(parentProject.getProjectLevel() + 1);
        } else {
            project.setProjectLevel(1);
        }
        return project;
    }

    private Sample feedSampleData(Sample sample, Project project) throws Exception {
        sample.setProjectId(project.getProjectId());
        sample.setProjectName(project.getProjectName());

        //set project level by adding 1 to selected parent project's level
        if(sample.getParentSampleName()==null || sample.getParentSampleName().equals("0")) {
            sample.setParentSampleName(null);
            sample.setParentSampleId(null);
            sample.setSampleLevel(1);
        } else {
            String parentSampleName = sample.getParentSampleName();
            if (parentSampleName != null && !parentSampleName.isEmpty() && !parentSampleName.equals("0")) {
                Sample selectedParentSample = readPersister.getSample(project.getProjectId(), parentSampleName);

                if(selectedParentSample != null && selectedParentSample.getSampleId() != null) {
                    sample.setSampleLevel(selectedParentSample.getSampleLevel() + 1);
                    sample.setParentSampleId(selectedParentSample.getSampleId());
                }
            }
        }
        return sample;
    }

    private List<FileReadAttributeBean> feedAndFilterFileReadBeans(String eventName, String projectName, String sampleName, List<FileReadAttributeBean> loadingList) throws Exception {
        List<FileReadAttributeBean> processedList = new ArrayList<FileReadAttributeBean>();

        for(FileReadAttributeBean fBean : loadingList) { //skip invalid attribute bean
            if (fBean.getAttributeName().equals("0") || fBean.getAttributeValue() == null
                    || fBean.getAttributeValue().isEmpty() || fBean.getAttributeValue().toLowerCase().equals("null")) { //&& !fBean.getAttributeValue().equals("0")
                continue;
            }

            if(fBean.getProjectName() == null || eventName.contains(Constants.EVENT_PROJECT_REGISTRATION)) {
                fBean.setProjectName(projectName);
            }
            if(fBean.getSampleName() == null) {
                if(sampleName == null || sampleName.isEmpty()) {
                    throw new Exception("sample does not exist or sample name is empty.");
                }
                fBean.setSampleName(sampleName);
            }

            //handle file uploads
            if(fBean.getUpload()!=null && fBean.getUploadFileName()!=null && !fBean.getUploadFileName().isEmpty()) {
                fileStoragePath = fileStoragePath + (fileStoragePath.endsWith(File.separator)?"":File.separator);
                String originalFileName = fBean.getUploadFileName();

                String fileDirectoryPathProject = projectName.replaceAll(" ", "_"); //project folder
                String fileDirectoryPathSample = fileDirectoryPathProject + File.separator +
                        (sampleName != null && !sampleName.isEmpty() ? sampleName.replaceAll(" ", "_") : "project"); //sample folder
                String fileDirectoryPath = fileDirectoryPathSample + File.separator + CommonTool.convertTimestampToDate(new Date()); //date folder

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
                }
            }

            processedList.add(fBean);
        }

        return processedList;
    }

    private void validateDataForDPCC(List<FileReadAttributeBean> loadingList, int index) throws Exception {
        String attributeName = null;
        try {
            for(FileReadAttributeBean fBean : loadingList) {
                if(fBean.getAttributeName().toLowerCase().contains("date")) {
                    attributeName = fBean.getAttributeName();
                    DPCCValidator.validateDate(fBean.getAttributeValue());
                }
            }
        } catch(Exception ex) {
            DetailedException dex = new DetailedException(index, "date parse error: '" + attributeName + "'");
            throw dex;
        }
    }

    private void updateSampleStatus(List<FileReadAttributeBean> loadingList, String projectName, String sampleName, String status, int index) throws Exception {
        boolean foundSampleStatus = false;
        String strStatus = status.equals("submit") ? Constants.DPCC_STATUS_SUBMITTED : status.equals("validate") ? Constants.DPCC_STATUS_VALIDATED : Constants.DPCC_STATUS_EDITING;

        try {
            Project project = this.readPersister.getProject(projectName);

            if(sampleName == null || sampleName.isEmpty()) {
                throw new Exception("Sample is required.");
            }

            Sample sample = this.readPersister.getSample(project.getProjectId(), sampleName);
            if(sample != null) { //it could be sample registration event
                List<SampleAttribute> saList = this.readPersister.getSampleAttributes(sample.getSampleId());
                for(SampleAttribute sa : saList) {
                    if(sa.getMetaAttribute().getLookupValue().getName().equals(Constants.ATTR_SAMPLE_STATUS)) {
                        if(sa.getAttributeStringValue() != null && sa.getAttributeStringValue().equals("Data submitted to DPCC")) {
                            throw new Exception("You cannot load any events for samples that has been submitted to DPCC.");
                        }
                    }
                }
            }

            for(FileReadAttributeBean fBean : loadingList) {
                if(fBean.getAttributeName().equals(Constants.ATTR_SAMPLE_STATUS)) {
                    fBean.setAttributeValue(strStatus);
                    foundSampleStatus = true;
                }
            }
            if(!foundSampleStatus) { //if sample status attribute is not in the list
                List<SampleMetaAttribute> smaList = this.readPersister.getSampleMetaAttributes(project.getProjectId());
                for(SampleMetaAttribute sma : smaList) { //check if sample status is in sample meta attributes
                    if(sma.getLookupValue().getName().equals(Constants.ATTR_SAMPLE_STATUS)) {
                        foundSampleStatus = true;
                    }
                }

                if(foundSampleStatus) { //manually add sample status with the status value
                    FileReadAttributeBean statusBean = new FileReadAttributeBean();
                    statusBean.setAttributeName(Constants.ATTR_SAMPLE_STATUS);
                    statusBean.setAttributeValue(strStatus);
                    statusBean.setProjectName(projectName);
                    statusBean.setSampleName(sampleName);
                    loadingList.add(statusBean);
                } else {
                    throw new Exception("'" + Constants.ATTR_SAMPLE_STATUS + "' attribute not found.");
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            DetailedException dex = new DetailedException(index, ex.getMessage());
            throw dex;
        }
    }
}
