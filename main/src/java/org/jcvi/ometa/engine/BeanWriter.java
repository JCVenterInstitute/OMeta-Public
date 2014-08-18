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

package org.jcvi.ometa.engine;

import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationRemote;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackRemote;
import org.jcvi.ometa.configuration.BeanPopulator;
import org.jcvi.ometa.configuration.InputBeanType;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.PresentationActionDelegate;
import org.jcvi.ometa.utils.TemplatePreProcessingUtils;
import org.jcvi.ometa.utils.UploadActionDelegate;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/2/11
 * Time: 4:18 PM
 *
 * Takes care of specifics to type of data.
 */
public class BeanWriter {
    private ProjectSampleEventWritebackBusiness writeEjb;
    private ProjectSampleEventPresentationBusiness readEjb;
    private Logger logger = Logger.getLogger(BeanWriter.class);

    /** Construct with all stuff needed for subsequent calls. */
    public BeanWriter(String server, String userName, String password) {
        UploadActionDelegate writeDelegate = new UploadActionDelegate();
        writeEjb = (ProjectSampleEventWritebackRemote)writeDelegate.getEjb(UploadActionDelegate.EJB_NAME, server, userName, password, logger);

        PresentationActionDelegate readDelegate = new PresentationActionDelegate();
        readEjb = (ProjectSampleEventPresentationRemote)readDelegate.getEjb(PresentationActionDelegate.EJB_NAME, server, userName, password, logger);
    }

    public void writePMAs(File... files) throws Exception {
        for (File file: files) {
            List<ProjectMetaAttribute> pmaBeans = this.getGenericModelBeans(file, ProjectMetaAttribute.class);
            writeEjb.loadProjectMetaAttributes(pmaBeans);

        }
    }

    public void writeEMAs(File... files) throws Exception {
        for (File file: files) {
            List<EventMetaAttribute> emaBeans = this.getGenericModelBeans(file, EventMetaAttribute.class);
            writeEjb.loadEventMetaAttributes(emaBeans);

        }
    }

    public void writeSMAs(File... files) throws Exception {
        for (File file: files) {
            List<SampleMetaAttribute> smaBeans = this.getGenericModelBeans(file, SampleMetaAttribute.class);
            writeEjb.loadSampleMetaAttributes(smaBeans);

        }
    }

    public void writeLookupValues(File... files) throws Exception {
        for (File file: files) {
            List<LookupValue> lvBeans = this.getGenericModelBeans(file, LookupValue.class);
            writeEjb.loadLookupValues(lvBeans);

        }
    }

    public void writeSamples(File... files) throws Exception {
        for (File file: files) {
            List<Sample> sBeans = this.getGenericModelBeans(file, Sample.class);
            writeEjb.loadSamples(sBeans);

        }
    }

    public void writeProjects(File... files) throws Exception {
        for (File file: files) {
            List<Project> pBeans = this.getGenericModelBeans(file, Project.class);
            writeEjb.loadProjects(pBeans);

        }
    }

    public String writeEvent(File eventFile, String eventName, String projectName, boolean processInput) throws Exception {
        String lastSampleName = null;

        boolean isProjectRegistration = eventName.contains(Constants.EVENT_PROJECT_REGISTRATION);
        boolean isSampleRegistration = eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION);

        List<GridBean> gridList = this.getEventBeans(eventFile, eventName, processInput);

        MultiLoadParameter loadParameter = new MultiLoadParameter();

        for(GridBean gBean : gridList) {
            if(gBean!=null) {
                Project loadingProject = null;
                Sample loadingSample = null;

                if(isProjectRegistration && gBean.getProjectName()!=null && gBean.getProjectPublic()!=null) {
                    loadingProject = new Project();
                    loadingProject.setProjectName(gBean.getProjectName());
                    loadingProject.setIsPublic(Integer.valueOf(gBean.getProjectPublic()));
                } else {
                    projectName = gBean.getProjectName();
                    loadingProject = readEjb.getProject(projectName);

                    Sample existingSample = readEjb.getSample(loadingProject.getProjectId(), gBean.getSampleName());
                    if(existingSample == null && isSampleRegistration) {
                        loadingSample = new Sample();
                        loadingSample.setSampleName(gBean.getSampleName());
                        loadingSample.setParentSampleName(gBean.getParentSampleName());
                        loadingSample.setIsPublic(Integer.valueOf(gBean.getSamplePublic()));
                    } else {
                        loadingSample = existingSample;
                    }
                }

                List<FileReadAttributeBean> fBeanList = gBean.getBeanList();
                if(fBeanList!=null && fBeanList.size()>0) {
                    this.createMultiLoadParameter(loadParameter, eventName, projectName, loadingProject,  loadingSample, fBeanList);
                    lastSampleName = gBean.getSampleName();
                }
            }
        }

        writeEjb.loadAll(null, loadParameter);

        return lastSampleName;
    }

    /**
     * Writes back multiple objects of assorted type, rather than a single type of file.
     *
     * @param collector source for all the different types of files.
     * @throws Exception for called methods.
     */
    public void writeMultiType(FileCollector collector) throws Exception {
        MultiLoadParameter parameterObject = createMultiLoadParameter(collector);
        List<String> projectsToSecure = getProjectsToSecure(parameterObject);
        writeEjb.loadAll(projectsToSecure, parameterObject);
    }

    /**
     * Builds a parameter object to be sent to EJB to load all files in the collection.
     *
     * @param collector has a collection of files that can be separated in order.
     * @return parameter that has the files' contents bundled and separated.
     * @throws Exception thrown by called methods.
     */
    private MultiLoadParameter createMultiLoadParameter(FileCollector collector) throws Exception {
        List<File> files = null;

        MultiLoadParameter parameterObject = new MultiLoadParameter();
        files = collector.getLookupValueFiles();
        for (File file: files) {
            List<LookupValue> lvBeans = this.getGenericModelBeans(file, LookupValue.class);
            parameterObject.addLookupValues(lvBeans);
        }

        files = collector.getProjectFiles();
        for (File file: files) {
            List<Project> pBeans = this.getGenericModelBeans(file, Project.class);
            parameterObject.addProjects(pBeans);
        }

        files = collector.getSampleFiles();
        for (File file: files) {
            List<Sample> sBeans = this.getGenericModelBeans(file, Sample.class);
            parameterObject.addSamples(sBeans);
        }

        files = collector.getProjectMetaAttributeFiles();
        for (File file: files) {
            List<ProjectMetaAttribute> pmaBeans = this.getGenericModelBeans(file, ProjectMetaAttribute.class);
            parameterObject.addProjectMetaAttributes(pmaBeans);
        }

        files = collector.getSampleMetaAttributeFiles();
        for (File file: files) {
            List<SampleMetaAttribute> smaBeans = this.getGenericModelBeans(file, SampleMetaAttribute.class);
            parameterObject.addSampleMetaAttributes(smaBeans);
        }

        files = collector.getEventMetaAttributeFiles();
        for (File file: files) {
            List<EventMetaAttribute> emaBeans = this.getGenericModelBeans(file, EventMetaAttribute.class);
            parameterObject.addEventMetaAttributes(emaBeans);
        }

        /*
        * no events allowed in a multi file. event loads should go through [template generation -> load]
        * by hkim 3/25/14
        *
        // Finally, the events.
        files = collector.getProjectRegistrationFiles();
        for (File file: files) {
            String eventName = getEventName(file.getName());
            List<FileReadAttributeBean> attributeBeans = this.getGenericAttributeBeans(file);
            parameterObject.addProjectRegistrations(eventName, attributeBeans);
        }

        files = collector.getSampleRegistrationFiles();
        for (File file: files) {
            String eventName = getEventName(file.getName());
            List<FileReadAttributeBean> attributeBeans = this.getGenericAttributeBeans(file);
            parameterObject.addSampleRegistrations(eventName, attributeBeans);
        }

        files = collector.getEventFiles();
        for (File file: files) {
            String eventName = getEventName(file.getName());
            List<FileReadAttributeBean> attributeBeans = this.getGenericAttributeBeans(file);
            parameterObject.addEvents(eventName, attributeBeans);
        }
        */

        return parameterObject;
    }

    /** Get all project names of projects encountered in this multi-file.  Exclude any that are newly-creating. */
    private List<String> getProjectsToSecure(MultiLoadParameter parameter) {

        Set<String> projectsToSecure = new HashSet<String>();
        //Do not bother with projects newly-created.
        Set<String> exclusionSet = new HashSet<String>();
        if (parameter.getProjects() != null) {
            for (List<Project> projects: parameter.getProjects()) {
                for (Project project: projects) {
                    exclusionSet.add(project.getProjectName().intern());
                }
            }
        }

        //Do bother with everything NOT on that list.
        if (parameter.getSamples() != null) {
            for (List<Sample> samples: parameter.getSamples()) {
                for (Sample sample: samples) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, sample);
                }
            }
        }

        if (parameter.getPmas() != null) {
            for (List<ProjectMetaAttribute> pmas: parameter.getPmas()) {
                for (ProjectMetaAttribute pma: pmas) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, pma);
                }
            }
        }

        if (parameter.getSmas() != null) {
            for (List<SampleMetaAttribute> smas: parameter.getSmas()) {
                for (SampleMetaAttribute sma: smas) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, sma);
                }
            }
        }

        if (parameter.getEmas() != null) {
            for (List<EventMetaAttribute> emas: parameter.getEmas()) {
                for (EventMetaAttribute ema: emas) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, ema);
                }
            }
        }

        if (parameter.getSampleRegistrationEventAttributes() != null) {
            for (List<FileReadAttributeBean> eas: parameter.getSampleRegistrationEventAttributes()) {
                for (FileReadAttributeBean ea: eas) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, ea);
                }
            }
        }

        if (parameter.getOtherEvents() != null) {
            for (MultiLoadParameter.LoadableEventBean eventBean: parameter.getOtherEvents()) {
                for (FileReadAttributeBean attribute: eventBean.getAttributes()) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, attribute);
                }
            }
        }

        List<String> rtnList = new ArrayList<String>();
        rtnList.addAll(projectsToSecure);
        return rtnList;
    }

    private void addNonExcludedProjects(
            Set<String> projectsToSecure, Set<String> exclusionSet, ProjectNamerOnFileRead pnamer) {
        String projectName = pnamer.getProjectName().intern();
        if (! exclusionSet.contains(projectName)) {
            projectsToSecure.add(projectName);
        }
    }


    /**
     * Reads any kind of bean, given file for input, and class.
     *
     * @param file      to read information.
     * @param beanClass read into this.
     * @param <B>       type of bean.
     * @return list of beans of the type given.
     * @throws Exception thrown if exception during get phase.
     */
    public <B extends ModelBean> List<B> getGenericModelBeans(File file, Class<B> beanClass) throws Exception {
        List<B> beans = null;

        try {
            beans = new ArrayList<B>();
            BeanPopulator beanPopulator = new BeanPopulator(beanClass);

            String inputFileName = file.getName();
            BeanFactory factory = new BeanFactory(InputBeanType.getInputBeanType(inputFileName));

            TemplatePreProcessingUtils templateUtil = new TemplatePreProcessingUtils();
            File processedFile = templateUtil.preProcessTemplateFile(file);

            List<Map<String, String>> dataList = templateUtil.parseNonEventFile(processedFile);
            for(Map<String, String> data : dataList) {
                B nextBean = factory.getInstance();
                // NOTE: all of the beans in the file are required to be the same type.
                beanPopulator.populateBean(data, nextBean);
                beans.add(nextBean);
            }

            templateUtil.deletePreProcessedFile(processedFile);

        } catch (Throwable ex) {
            throw new Exception("failed parsing file - " + file.getAbsolutePath() + " : " + ex.getMessage());
        }

        return beans;

    }

    public List<GridBean> getEventBeans(File inputFile, String eventName, boolean processInput) throws Exception {

        // Assume the file contains right kind of data for this tye of bean.
        TemplatePreProcessingUtils templateUtils = new TemplatePreProcessingUtils();

        File processedFile = inputFile;
        if(processInput) {
            processedFile = templateUtils.preProcessTemplateFile(inputFile);
        }

        boolean isProjectRegistration = eventName.equals(Constants.EVENT_PROJECT_REGISTRATION);
        boolean isSampleRegistration = eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION);

        List<GridBean> parsedList = templateUtils.parseEventFile(
                processedFile.getName(),
                processedFile,
                null,
                isProjectRegistration,
                isSampleRegistration
        );

        templateUtils.deletePreProcessedFile(processedFile);
        return parsedList;
    }


    /**
     * inner class BeanFactory
     * Type-parameterized factory method, to build out model beans.
     */
    public static class BeanFactory {
        private InputBeanType inputBeanType;

        /**
         * Construct with info for criteria to chose type of object to create.
         */
        public BeanFactory(InputBeanType inputBeanType) {
            this.inputBeanType = inputBeanType;
        }

        /**
         * Create a bean of the type dictated by configured criteria.
         */
        public <B extends ModelBean> B getInstance() {
            B bean = null;

            switch (inputBeanType) {
                case eventMetaAttribute:
                    bean = (B) new EventMetaAttribute();
                    break;
                case projectMetaAttributes:
                    bean = (B) new ProjectMetaAttribute();
                    break;
                case sampleMetaAttributes:
                    bean = (B) new SampleMetaAttribute();
                    break;
                case project:
                    bean = (B) new Project();
                    break;
                case sample:
                    bean = (B) new Sample();
                    break;
                case lookupValue:
                    bean = (B) new LookupValue();
                    break;
                default:
                    break;
            }
            return bean;
        }
    }



    /*
    * Get the name of the event, from the input file name.
    private String getEventName(String inputFilePathStr) throws Exception {
        int pos = inputFilePathStr.indexOf(FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX);
        String eventName = null;
        if (pos <= 0 || inputFilePathStr.charAt(pos - 1) != '_') {
            throw new Exception(inputFilePathStr + " ends with " + FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX + " but has no event name prefixing that.");
        } else {
            int pos2 = inputFilePathStr.lastIndexOf("_");
            int pos3 = pos2 - 1;
            while (pos3 >= 0  &&  inputFilePathStr.charAt(pos3) != '_') {
                pos3--;
            }
            if (pos3 < 0) pos3 = 0;
            else pos3 ++;

            eventName = inputFilePathStr.substring(pos3, pos2);
        }
        return eventName;
    }
    */

    private MultiLoadParameter createMultiLoadParameter(MultiLoadParameter loadParameter, String eventName, String projectName, Project project, Sample sample, List<FileReadAttributeBean> frab)
            throws Exception {
        boolean isSampleRegistration = false;
        boolean isProjectRegistration = false;

        if (eventName.contains(Constants.EVENT_PROJECT_REGISTRATION) && project.getProjectName() != null && !project.getProjectName().isEmpty()) {
            isProjectRegistration = true;

            List<Project> projectList = new ArrayList<Project>();
            projectList.add(feedProjectData(project, projectName));
            loadParameter.addProjects(projectList);

            /*
            *   loads all event meta attributes from the parent
            *   by hkim 6/11/13
            */
            List<EventMetaAttribute> emas = readEjb.getEventMetaAttributes(projectName, null); //, Constants.EVENT_PROJECT_REGISTRATION);
            if (emas != null && emas.size() > 0) {
                List<EventMetaAttribute> newEmas = new ArrayList<EventMetaAttribute>(emas.size());

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

            List<SampleMetaAttribute> smas = readEjb.getSampleMetaAttributes(project.getProjectId());
            if(smas != null && smas.size() > 0) {
                List<SampleMetaAttribute> newSmas = new ArrayList<SampleMetaAttribute>(smas.size());
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
                loadParameter.addSampleMetaAttributes(newSmas);
            }

            List<ProjectMetaAttribute> pmas = readEjb.getProjectMetaAttributes(projectName);
            if (pmas != null && pmas.size() > 0) {
                List<ProjectMetaAttribute> newPmas = new ArrayList<ProjectMetaAttribute>(pmas.size());
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
        else if (eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION) && sample.getSampleName() != null && !sample.getSampleName().isEmpty()) {
            isSampleRegistration = true;

            List<Sample> sampleList = new ArrayList<Sample>();
            sampleList.add(feedSampleData(sample, project));
            loadParameter.addSamples(sampleList);
        }

        List<FileReadAttributeBean> loadingList = null;
        if (frab != null && frab.size() > 0) {
            loadingList = processFileReadBeans(eventName,
                    isProjectRegistration ? project.getProjectName() : projectName,
                    sample.getSampleName(),
                    frab
            );
        }

        if (isProjectRegistration) {
            loadParameter.addProjectRegistrations(eventName, loadingList);
        } else if (isSampleRegistration) {
            loadParameter.addSampleRegistrations(eventName, loadingList);
        } else {
            if (loadingList != null && loadingList.size() > 0) {
                loadParameter.addEvents(eventName, loadingList);
            }
        }
        return loadParameter;
    }

    private Project feedProjectData(Project project, String projectName) throws Exception {
        project.setParentProjectName(projectName);

        Project parentProject = readEjb.getProject(projectName);
        project.setParentProjectId(parentProject.getProjectId());
        project.setProjectLevel(parentProject.getProjectLevel()+1);
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
                Sample selectedParentSample = readEjb.getSample(project.getProjectId(), parentSampleName);
                if(selectedParentSample != null && selectedParentSample.getSampleId() != null) {
                    sample.setSampleLevel(selectedParentSample.getSampleLevel() + 1);
                    sample.setParentSampleId(selectedParentSample.getSampleId());
                }
            }
        }
        return sample;
    }

    private List<FileReadAttributeBean> processFileReadBeans(String eventName, String projectName, String sampleName, List<FileReadAttributeBean> loadingList) throws Exception {

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

}
