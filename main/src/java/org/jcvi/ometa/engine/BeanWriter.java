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
import org.jcvi.ometa.helper.EventLoadHelper;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.utils.*;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileWriter;
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
    private Logger logger = Logger.getLogger(BeanWriter.class);

    private Context ctx = null;
    private ProjectSampleEventWritebackBusiness writeEjb;
    private ProjectSampleEventPresentationBusiness readEjb;

    private Actor submitter;

    /** Construct with all stuff needed for subsequent calls. */
    public BeanWriter(String server, String userName, String password) {
        UploadActionDelegate writeDelegate = new UploadActionDelegate();
        writeEjb = (ProjectSampleEventWritebackRemote)writeDelegate.getEjb(ctx, UploadActionDelegate.EJB_NAME, server, userName, password, logger);

        PresentationActionDelegate readDelegate = new PresentationActionDelegate();
        readEjb = (ProjectSampleEventPresentationRemote)readDelegate.getEjb(ctx, PresentationActionDelegate.EJB_NAME, server, userName, password, logger);
    }

    public void closeContext() throws Exception {
        if(ctx != null) {
            try {
                ctx.close();
            } catch (NamingException ex) {
                throw new Exception("Ejb context cannot be closed : " + ex.getMessage());
            }
        }
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

    public String writeEvent(File eventFile, String eventName, String projectName, boolean processInput, String path, String submissionId, String submitterId) throws Exception {
        String lastSampleName = null;

        MultiLoadParameter loadParameter = new MultiLoadParameter();

        if(submitterId != null && !submitterId.isEmpty()) { //manually set createdBy for bulk load
            loadParameter.setSubmitterId(submitterId);
            this.submitter = this.readEjb.getActorByUserName(submitterId);
        }

        List<GridBean> gridList = this.getEventBeansFromFile(eventFile, eventName, processInput);

        EventLoadHelper loadHelper = new EventLoadHelper(this.readEjb);
        loadHelper.setOriginalPath(path); //add path to the helper for relative file paths
        loadHelper.setSubmissionId(submissionId);

        loadHelper.gridListToMultiLoadParameter(loadParameter, gridList, projectName, eventName, Constants.DPCC_STATUS_SUBMITTED_FORM, submitterId);

        writeEjb.loadAll(null, loadParameter);

        if(gridList != null && gridList.size() > 0 && gridList.get(0) != null) {
            lastSampleName =  gridList.get(0).getSampleName();
        }

        return lastSampleName;
    }

    public void setSubmitter(String submitterId) throws Exception {
        if(submitterId != null && !submitterId.isEmpty()) { //manually set createdBy for bulk load
            this.submitter = this.readEjb.getActorByUserName(submitterId);
        }
    }

    /**
     * Writes back multiple objects of assorted type, rather than a single type of file.
     *
     * @param collector source for all the different types of files.
     * @throws Exception for called methods.
     */
    public void writeMultiType(FileCollector collector) throws Exception {
        MultiLoadParameter parameterObject = this.createMultiLoadParameterWithCollector(collector);
        List<String> projectsToSecure = this.getProjectsToSecure(parameterObject);
        writeEjb.loadAll(projectsToSecure, parameterObject);
    }

    public void runJsonProducer() throws Exception {
        JsonProducer jsonProducerBean = new JsonProducer(readEjb);
        jsonProducerBean.generateJson();
    }

    public void readProjectData(String filePath, String projectName, String eventName) throws  Exception{
        FileWriter writer = new FileWriter(filePath);
        LookupValue tempLookupValue;

        Project project = this.readEjb.getProject(projectName);
        Long projectId = project.getProjectId();

        List<Sample> samples = this.readEjb.getSamplesForProject(projectId);

        if(samples!=null && samples.size()>0) {
            List<Long> sampleIdList = new ArrayList<Long>();
            for (Sample sample : samples) {
                sampleIdList.add(sample.getSampleId());
            }

            List<SampleAttribute> allSampleAttributes = this.readEjb.getSampleAttributes(sampleIdList);
            Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = new HashMap<Long, List<SampleAttribute>>();
            for (SampleAttribute att : allSampleAttributes) {
                List<SampleAttribute> atts = sampleIdVsAttributeList.get(att.getSampleId());
                if (atts == null) {
                    atts = new ArrayList<SampleAttribute>();
                    sampleIdVsAttributeList.put(att.getSampleId(), atts);
                }
                atts.add(att);
            }

            List<Event> allSampleEvents = this.readEjb.getEventsForSamples(sampleIdList);
            Map<Long, List<Event>> sampleIdVsEventList = new HashMap<Long, List<Event>>();
            for (Event att : allSampleEvents) {
                if(att.getEventTypeLookupValue().getName().equals(eventName)) {
                    List<Event> atts = sampleIdVsEventList.get(att.getSampleId());
                    if (atts == null) {
                        atts = new ArrayList<Event>();
                        sampleIdVsEventList.put(att.getSampleId(), atts);
                    }
                    atts.add(att);
                }
            }

            //Create Header
            List<EventMetaAttribute> eventMetaAttributeList = this.readEjb.getEventMetaAttributes(projectName, eventName);
            List<String> headerList = new ArrayList<String>(eventMetaAttributeList.size());
            writer.append("Sample Name");
            writer.append(",");
            for(EventMetaAttribute ema : eventMetaAttributeList){
                if(ema.isActive()) {
                    writer.append(ema.getAttributeName());
                    writer.append(',');

                    headerList.add(ema.getAttributeName());
                }
            }
            writer.append('\n');

            HashMap<String, String> sampleData;
            for (Sample sample : samples) {
                List<SampleAttribute> sampleAttributes = sampleIdVsAttributeList.get(sample.getSampleId());
                sampleData = new HashMap<String, String>(sampleAttributes.size());

                writer.append(sample.getSampleName());
                writer.append(',');

                if (sampleAttributes != null && sampleAttributes.size() > 0) {
                    for (SampleAttribute sa : sampleAttributes) {
                        if (sa.getMetaAttribute() == null)
                            continue;
                        tempLookupValue = sa.getMetaAttribute().getLookupValue();
                        String dataType = tempLookupValue.getDataType();

                        if(dataType.equals("date")) {
                            sampleData.put(tempLookupValue.getName(), '"' + sa.getAttributeDateValue().toString()+  '"');
                        } else if(dataType.equals("int")) {
                            sampleData.put(tempLookupValue.getName(), '"' + sa.getAttributeIntValue().toString()+  '"');
                        } else if(dataType.equals("string") || dataType.equals("file") || dataType.equals("url")) {
                            sampleData.put(tempLookupValue.getName(), '"' + sa.getAttributeStringValue().toString()+  '"');
                        } else {
                            sampleData.put(tempLookupValue.getName(), '"' + sa.getAttributeFloatValue().toString()+  '"');
                        }
                    }
                }

                for(String header : headerList){
                    String value = sampleData.get(header);
                    writer.append((value == null) ? "" : value);
                    writer.append(",");
                }

                writer.append('\n');
            }
        }

        writer.flush();
        writer.close();
    }

    /**
     * Builds a parameter object to be sent to EJB to load all files in the collection.
     *
     * @param collector has a collection of files that can be separated in order.
     * @return parameter that has the files' contents bundled and separated.
     * @throws Exception thrown by called methods.
     */
    private MultiLoadParameter createMultiLoadParameterWithCollector(FileCollector collector) throws Exception {
        List<File> files = null;

        MultiLoadParameter parameterObject = new MultiLoadParameter();
        files = collector.getLookupValueFiles();
        for (File file: files) {
            List<LookupValue> lvBeans = this.getGenericModelBeans(file, LookupValue.class);

            //load only new lookup values
            List<LookupValue> newLv = new ArrayList<LookupValue>();
            for(LookupValue lv : lvBeans) {
                LookupValue existingLV = this.readEjb.getLookupValue(lv.getName(), lv.getType());
                if(existingLV == null) {
                    newLv.add(lv);
                }
            }

            parameterObject.addLookupValues(newLv);
        }

        files = collector.getDictionaryFiles();
        for (File file: files) {
            List<Dictionary> dictBeans = this.getGenericModelBeans(file, Dictionary.class);

            //load only new dictionaries
            List<Dictionary> newDict = new ArrayList<Dictionary>();
            for(Dictionary dict : dictBeans){
                String code = dict.getDictionaryCode();
                if(code == null || code.equals("")) code = dict.getDictionaryValue();

                Dictionary existingDict = this.readEjb.getDictionaryByTypeAndCode(dict.getDictionaryType(), code);
                if(existingDict == null) {
                    newDict.add(dict);
                }
            }

            parameterObject.addDictionaries(newDict);
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

    public List<GridBean> getEventBeansFromFile(File inputFile, String eventName, boolean processInput) throws Exception {

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
                case dictionary:
                    bean = (B) new Dictionary();
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

    public Actor getSubmitter() {
        return submitter;
    }
}
