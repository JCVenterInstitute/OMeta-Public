package org.jcvi.ometa.helper;

import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.DPCCValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.util.ArrayList;
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

    public EventLoadHelper() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public EventLoadHelper(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    public EventLoadHelper(ProjectSampleEventPresentationBusiness pseb) {
        this.readPersister = new ReadBeanPersister(pseb);
    }

    public MultiLoadParameter createMultiLoadParameter(
            MultiLoadParameter loadParameter, String eventName, String projectName,
            Project project, Sample sample, List<FileReadAttributeBean> frab, int index) throws Exception {

        boolean isSampleRegistration = false;
        boolean isProjectRegistration = false;

        if (eventName.contains(Constants.EVENT_PROJECT_REGISTRATION) && project.getProjectName() != null && !project.getProjectName().isEmpty()) {
            isProjectRegistration = true;
        } else if (eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION) && sample.getSampleName() != null && !sample.getSampleName().isEmpty()) {
            isSampleRegistration = true;
        }

        List<FileReadAttributeBean> loadingList = null;
        if (frab != null && frab.size() > 0) {
            loadingList = this.feedAndFilterFileReadBeans(eventName, isProjectRegistration ? project.getProjectName() : projectName, sample.getSampleName(), frab);
        }

        if (isProjectRegistration) {
            /*
            *   loads all meta attributes from the parent
            *   by hkim 6/11/13
            */
            List<EventMetaAttribute> emas = readPersister.getEventMetaAttributes(projectName, null); //, Constants.EVENT_PROJECT_REGISTRATION);
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

            List<SampleMetaAttribute> smas = readPersister.getSampleMetaAttributes(project.getProjectId());
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

            List<ProjectMetaAttribute> pmas = readPersister.getProjectMetaAttributes(projectName);
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
            loadParameter.addProjectPair(feedProjectData(project, projectName), loadingList, newPmas, newSmas, newEmas, index);
        } else if (isSampleRegistration) {
            loadParameter.addSamplePair(feedSampleData(sample, project), loadingList, index);
        } else {
            loadParameter.addEvents(eventName, loadingList);
        }
        return loadParameter;
    }

    private Project feedProjectData(Project project, String projectName) throws Exception {
        project.setParentProjectName(projectName);

        Project parentProject = readPersister.getProject(projectName);
        project.setParentProjectId(parentProject.getProjectId());
        project.setProjectLevel(parentProject.getProjectLevel() + 1);
        project.setEditGroup(parentProject.getEditGroup());
        project.setViewGroup(parentProject.getViewGroup());
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
        for(FileReadAttributeBean fBean:loadingList) {
            if(fBean.getProjectName()==null || eventName.equals(Constants.EVENT_PROJECT_REGISTRATION)) {
                fBean.setProjectName(projectName);
            }
            if(fBean.getSampleName()==null) {
                fBean.setSampleName(sampleName);
            }

            if (!fBean.getAttributeName().equals("0") && fBean.getAttributeValue()!=null && !fBean.getAttributeValue().isEmpty()) { //&& !fBean.getAttributeValue().equals("0")
                processedList.add(fBean);
            }
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
}
