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

package org.jcvi.ometa.db_interface;

import org.hibernate.Session;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.exception.InvalidInputException;
import org.jcvi.ometa.hibernate.dao.*;
import org.jcvi.ometa.intf.BeanPersistenceFacadeI;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.GuidGetter;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jcvi.ometa.validation.ModelValidator;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/10/11
 * Time: 11:12 AM
 * <p/>
 * Serves as facade for the writeback of beans to database.  This way, the writeback can be behind another
 * tier (or n tiers), and the caller would not have to know.
 */
public class WritebackBeanPersister implements BeanPersistenceFacadeI {

    protected static final String NO_SUCH_ATTRIBUTE_MSG = "attribute %s not found.";
    protected static final String BAD_LOOKUP_TYPE_MSG = "'%s' is not an attribute.";
    protected static final String INVALID_LOOKUP_VALUE_DATA_TYPE_MSG = "invalid data type '%s'.";
    protected static final String INCOMPATIBLE_LOOKUP_VALUE_MSG = "Lookup value %s already exists. (%s, %s)";//, and is not compatible.";
    protected static final String INCOMPATIBLE_DICTIONARY_MSG = "Dictionary already exists. (%s, %s, %s)";//, and is not compatible.";
    protected static final String UNKNOWN_SAMPLE_FOR_PROJECT_MSG = "Project '%s' does not have sample '%s'.";;
    protected static final String VALIDATION_PREFIX = "validate:";

    private SessionAndTransactionManagerI sessionAndTransactionManager;
    private Session session;
    private DAOFactory daoFactory;

    private GuidGetter guidGetter;
    private ModelValidator modelValidator;

    public WritebackBeanPersister(Properties props, SessionAndTransactionManagerI sessionAndTransactionManager) {
        String sessionFactoryName = null;

        if (props != null) {
            // Gather dependencies for creating DAO's.
            sessionFactoryName = props.getProperty(SESSION_FACTORY_NAME_PROP);
        } else {
            sessionFactoryName = "java:/hibernate/OMETASessionFactory";
        }

        this.sessionAndTransactionManager = sessionAndTransactionManager;
        this.sessionAndTransactionManager.setSessionFactoryName(sessionFactoryName);
        this.daoFactory = new DAOFactory();
        this.modelValidator = new ModelValidator();
        this.guidGetter = new GuidGetter();
    }

    /**
     * Begin the session.
     */
    public void open() throws Exception {
        session = sessionAndTransactionManager.getSession();
        sessionAndTransactionManager.startTransaction();
    }

    private boolean managerHasBeenClosed = false;

    /**
     * Always close this after operations complete.
     */
    public void close() {
        if (managerHasBeenClosed)
            return;

        try {
            managerHasBeenClosed = true;
            sessionAndTransactionManager.commitTransaction();
        } catch (DAOException daoe) {
            throw new RuntimeException(daoe);
        }
        sessionAndTransactionManager.closeSession();
    }

    /**
     * Call this from external source, whenever an error will prevent proper writeback of completed data.
     */
    public void error() {
        sessionAndTransactionManager.rollBackTransaction();
    }

    public void writeBackActor(Actor actor) throws Exception {
        try {
            ActorDAO actorDAO = daoFactory.getActorDAO();

            //check if another user with same user name exists
            Actor existingUser = actorDAO.getActorByLoginName(actor.getUsername(), session);
            if(existingUser!=null) {
                throw new Exception("user id already exists!");
            }

            actor.setLoginId(guidGetter.getGuid());
            actor.setCreationDate(sessionAndTransactionManager.getTransactionStartDate());
            actorDAO.write(actor, session);

            LookupValueDAO lookupValueDAO = daoFactory.getLookupValueDAO();
            GroupDAO groupDAO = daoFactory.getGroupDAO();

            LookupValue viewLV = lookupValueDAO.getLookupValue("General-View", session);
            Group viewGroup = groupDAO.getGroupByLookupId(viewLV.getLookupValueId(), session);

            LookupValue editLV = lookupValueDAO.getLookupValue("General-Edit", session);
            Group editGroup = groupDAO.getGroupByLookupId(editLV.getLookupValueId(), session);

            List<ActorGroup> groups = new ArrayList<ActorGroup>(2);
            //add view group
            ActorGroup actgrp = new ActorGroup();
            actgrp.setActorGroupId(guidGetter.getGuid());
            actgrp.setActorId(actor.getLoginId());
            actgrp.setGroupId(viewGroup.getGroupId());
            actgrp.setCreationDate(sessionAndTransactionManager.getTransactionStartDate());
            groups.add(actgrp);
            //add edit group
            actgrp = new ActorGroup();
            actgrp.setActorGroupId(guidGetter.getGuid());
            actgrp.setActorId(actor.getLoginId());
            actgrp.setGroupId(editGroup.getGroupId());
            actgrp.setCreationDate(sessionAndTransactionManager.getTransactionStartDate());
            groups.add(actgrp);
            actorDAO.writeActorGroup(groups, session);

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void writeBackGroups(List<Group> groups) throws Exception {
        try {
            GroupDAO groupDAO = daoFactory.getGroupDAO();
            for(Group group : groups) {
                group.setGroupId(guidGetter.getGuid());
                groupDAO.addGroup(group, session);
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void writeBackActorGroup(List<ActorGroup> actorGroups) throws Exception {
        try {
            ActorDAO actorDAO = daoFactory.getActorDAO();
            for(ActorGroup actorGroup : actorGroups) {
                actorGroup.setActorGroupId(guidGetter.getGuid());
                actorGroup.setCreationDate(sessionAndTransactionManager.getTransactionStartDate());
                actorDAO.writeActorGroup(actorGroup, session);
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void deleteActorGroup(List<ActorGroup> actorGroups) throws Exception {
        try {
            ActorDAO actorDAO = daoFactory.getActorDAO();
            actorDAO.deleteActorGroup(actorGroups, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }


    /**
     * Save things to be referenced by attributes later.
     */
    public void writeBackLookupValues(List<LookupValue> lookupValueList) throws Exception {
        try {
            StringBuilder errors = new StringBuilder();

            LookupValueDAO lookupValueDAO = daoFactory.getLookupValueDAO();
            List<LookupValue> loadingList = new ArrayList<LookupValue>(lookupValueList.size()); //load all or nothing list

            for (LookupValue lookupValue : lookupValueList) {
                String lvName = lookupValue.getName();
                String lvType = lookupValue.getType();
                if (!modelValidator.isValidLookupValueType(lvType)) {
                    errors.append(lvName + ":invalid lookup value type - " + lvType);
                }
                String lvDataType = lookupValue.getDataType();
                if (!modelValidator.isValidDataType(lvDataType)) {
                    errors.append(lvName + ":invalid data type - " + lvDataType);
                }
                LookupValue oldValue = lookupValueDAO.getLookupValue(lvName, session);
                if (oldValue == null) {
                    loadingList.add(lookupValue);
                    //lookupValueDAO.createLookupValue(lookupValue, sessionAndTransactionManager.getTransactionStartDate(), session);
                } else {
                    String message = String.format(INCOMPATIBLE_LOOKUP_VALUE_MSG, lvName, oldValue.getType(), oldValue.getDataType());
                    errors.append(lvName + ":lookup value already exists");
                    /*
                    * commented out since it creates more confusions for user
                    * 8/7/12 by hkim
                    *
                    // Must look at user's expectations, and let them know if this LV is taken.
                    String oldType = oldValue.getType();
                    String oldDataType = oldValue.getDataType();

                    String newDataType = lookupValue.getDataType();
                    String newType = lookupValue.getType();

                    // Looking through all values to see if "expectation" is compatible with existing reality.
                    boolean acceptable = true;
                    if (!newDataType.equals(oldDataType)) {
                        String message = String.format(MISMATCH_DATATYPE_MSG, lvName, oldDataType, newDataType);
                        errors.append(message);
                        errors.append("\n");
                        acceptable = false;
                    }

                    if (newType != null && newType.endsWith(" " + ModelValidator.ATTRIBUTE_LV_TYPE_NAME)) {
                        newType = ModelValidator.ATTRIBUTE_LV_TYPE_NAME;
                    }
                    if (!newType.equals(oldType)) {
                        String message = String.format(MISMATCH_TYPE_MSG, lvName, oldType, newType);
                        errors.append(message);
                        errors.append("\n");
                        acceptable = false;
                    }

                    if (acceptable) {
                        String message = String.format(INCOMPATIBLE_LOOKUP_VALUE_MSG, lvName);
                    }
                    */
                }
            }

            if(loadingList.size() > 0 && loadingList.size() == lookupValueList.size()) { //only load if there is no error
                for(LookupValue lv : loadingList) {
                    lookupValueDAO.createLookupValue(lv, sessionAndTransactionManager.getTransactionStartDate(), session);
                }
            }

            // Explain what went wrong.
            if (errors.length() > 0) {
                throw new Exception(errors.toString());
            }

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void writeBackDictionaries(List<Dictionary> dictList) throws Exception {
        try {
            StringBuilder errors = new StringBuilder();

            DictionaryDAO dictionaryDAO = daoFactory.getDictionaryDAO();
            List<Dictionary> loadingList = new ArrayList<Dictionary>(dictList.size()); //load all or nothing list

            for (Dictionary dict : dictList) {
                String dictType = dict.getDictionaryType();
                String dictCode = dict.getDictionaryCode();
                String dictValue = dict.getDictionaryValue();
                if(dictCode == null || dictCode.equals("")) dictCode = dictValue;

                Dictionary oldDict = dictionaryDAO.getDictionaryByTypeAndCode(dictType, dictCode, session);
                if (oldDict == null) {
                    boolean hasParentError = false;
                    String parentDictionary = dict.getParentDependency();
                    if(parentDictionary != null && !parentDictionary.equals("")){
                        String[] parentDictTypeCode = parentDictionary.split(" - ");
                        Dictionary parentDict = dictionaryDAO.getDictionaryByTypeAndCode(parentDictTypeCode[0], parentDictTypeCode[1], session);

                        if(parentDict == null) {
                            hasParentError = true;
                            errors.append(dictType + ", " + dictCode + ", " + dictValue + ":parent dictionary does not exist!\n");
                        }
                    }

                    if(!hasParentError)
                        loadingList.add(dict);
                } else {
                    String message = String.format(INCOMPATIBLE_DICTIONARY_MSG,
                            oldDict.getDictionaryType(), oldDict.getDictionaryCode(), oldDict.getDictionaryValue());
                    errors.append(dictType + ", " + dictCode + ", " + dictValue + ":dictionary already exists\n");
                }
            }

            if(loadingList.size() > 0 && loadingList.size() == dictList.size()) { //only load if there is no error
                for(Dictionary dict : loadingList) {
                    String dictCode = dict.getDictionaryCode();
                    String dictValue = dict.getDictionaryValue();
                    if(dictCode == null || dictCode.equals("")) dictCode = dictValue;

                    String parentDictionary = dict.getParentDependency();
                    if(parentDictionary != null && !parentDictionary.equals("")) {
                        dictionaryDAO.loadDictionaryWithDependency(dict.getDictionaryType(), dictValue, dictCode, parentDictionary,
                                sessionAndTransactionManager.getTransactionStartDate(), session);
                    } else {
                        dictionaryDAO.loadDictionary(dict.getDictionaryType(), dictValue, dictCode,
                                sessionAndTransactionManager.getTransactionStartDate(), session);
                    }
                }
            }

            // Explain what went wrong.
            if (errors.length() > 0) {
                throw new Exception(errors.toString());
            }

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    /**
     * Call this when you have a project read up to the spec given for filesystem, or non-db-origin data.
     *
     * @param pBeans        list of all projects to save.
     * @param actorUserName from environment: user's name to store as the actor of creation.
     * @throws Exception thrown by called methods.
     */
    public void writeBackProjects(List<Project> pBeans, String actorUserName) throws Exception {
        try {
            for (Project project : pBeans) {
                validateProjectInput(project);
                Long actorId = getActorId(actorUserName, session);
                ProjectDAO projectDAO = daoFactory.getProjectDAO();
                project.setProjectId(guidGetter.getGuid());
                project.setCreatedBy(actorId);
                projectDAO.write(project, sessionAndTransactionManager.getTransactionStartDate(), session);
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    /**
     * Call this when you have an actor to update.
     *
     * @param actor       actor to update.
     * @throws Exception thrown by called methods.
     */
    public void updateActor(Actor actor) throws  Exception {
        try {
            ActorDAO actorDAO = daoFactory.getActorDAO();
            actorDAO.update(actor, session);
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    /**
     * Call this when you have a project to update.
     *
     * @param project       project to update.
     * @param actorUserName from environment: user's name to store as the actor of creation.
     * @throws Exception thrown by called methods.
     */
    public void updateProject(Project project, String actorUserName) throws Exception {
        try {
            Long actorId = getActorId(actorUserName, session);
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            project.setModifiedBy(actorId);
            projectDAO.update(project, sessionAndTransactionManager.getTransactionStartDate(), session);

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    /**
     * Call this when you have a sample read up to the spec given for filesystem, or non-db-origin data.
     *
     * @param sBeans        list of samples to write out.
     * @param actorUserName username of person attempting to write this back.
     * @throws Exception thrown by called methods.
     */
    public void writeBackSamples(List<Sample> sBeans, String actorUserName) throws Exception {
        Long actorId = getActorId(actorUserName, session);
        try {
            Map<String, Long> projNameVsId = new HashMap<String, Long>();
            for (Sample sample : sBeans) {
                if (sample.getProjectId() == null) {
                    String projectName = sample.getProjectName();
                    Long projectId = getAndCacheProjectId(session, projNameVsId, projectName);
                    sample.setProjectId(projectId);
                }
                if (sample.getParentSampleId() == null && sample.getParentSampleName() != null && !isEmpty(sample.getParentSampleName())) {
                    Sample parentSample = getSample(sample.getProjectId(), sample.getParentSampleName(), session);
                    sample.setParentSampleId(parentSample.getSampleId());
                    sample.setSampleLevel(parentSample.getSampleLevel() + 1);
                }

                if(sample.getSampleLevel() == null || sample.getSampleLevel() == 0) {
                    sample.setSampleLevel(1);
                }
                validateSampleInput(sample);
                sample.setCreatedBy(actorId);
                sample.setSampleId(guidGetter.getGuid());
                SampleDAO sampleDAO = daoFactory.getSampleDAO();
                sampleDAO.write(sample, sessionAndTransactionManager.getTransactionStartDate(), session);
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void updateSample(Sample sample, String actorUserName) throws Exception {
        Long actorId = getActorId(actorUserName, session);
        try {
            validateSampleInput(sample);
            SampleDAO sampleDAO = daoFactory.getSampleDAO();
            sample.setModifiedBy(actorId);
            sampleDAO.update(sample, sessionAndTransactionManager.getTransactionStartDate(), session);
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void writeBackProjectMetaAttributes(List<ProjectMetaAttribute> pmaBeans, String actorUserName)
            throws Exception {
        Long actorId = getActorId(actorUserName, session);

        try {
            ProjectMetaAttributeDAO pmaDAO = daoFactory.getProjectMetaAttributeDAO();
            Map<String, Long> projNameVsId = new HashMap<String, Long>();
            for (ProjectMetaAttribute attribute : pmaBeans) {
                if (attribute.getProjectId() == null) {
                    String projectName = attribute.getProjectName();
                    Long projectId = getAndCacheProjectId(session, projNameVsId, projectName);
                    attribute.setProjectId(projectId);
                }
                validateProjectMetaAttributeInput(attribute);
                if(attribute.getCreatedBy()==null)
                    attribute.setCreatedBy(actorId);
                else
                    attribute.setModifiedBy(actorId);

                if(attribute.getId()==null)
                    attribute.setId(guidGetter.getGuid());

                if(attribute.isActive() == null) {
                    attribute.setActiveDB(1);
                }

                if(pmaDAO.getProjectMetaAttribute(attribute.getNameLookupId(), attribute.getProjectId(), session) != null) {
                    continue;
                }

                if(attribute.getCreationDate()==null)
                    pmaDAO.write(attribute, sessionAndTransactionManager.getTransactionStartDate(), session);
                else
                    pmaDAO.update(attribute, sessionAndTransactionManager.getTransactionStartDate(), session);
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void writeBackSampleMetaAttributes(List<SampleMetaAttribute> smaBeans, String actorUserName)
            throws Exception {
        Long actorId = getActorId(actorUserName, session);

        try {
            SampleMetaAttributeDAO smaDAO = daoFactory.getSampleMetaAttributeDAO();
            Map<String, Long> projNameVsId = new HashMap<String, Long>();
            for (SampleMetaAttribute attribute : smaBeans) {
                validateSampleMetaAttributeInput(attribute);

                if (attribute.getProjectId() == null) {
                    String projectName = attribute.getProjectName();
                    Long projectId = getAndCacheProjectId(session, projNameVsId, projectName);
                    attribute.setProjectId(projectId);
                }
                if(attribute.getCreatedBy()==null)
                    attribute.setCreatedBy(actorId);
                else
                    attribute.setModifiedBy(actorId);
                if(attribute.getId()==null)
                    attribute.setId(guidGetter.getGuid());

                if(attribute.isActive() == null) {
                    attribute.setActiveDB(1);
                }

                if(smaDAO.getSampleMetaAttribute(attribute.getNameLookupId(), attribute.getProjectId(), session) != null) {
                    continue; //skip for any existing SMA
                }

                if(attribute.getCreationDate() == null)
                    smaDAO.write(attribute, sessionAndTransactionManager.getTransactionStartDate(), session);
                else
                    smaDAO.update(attribute, sessionAndTransactionManager.getTransactionStartDate(), session);
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void writeBackEventMetaAttributes(List<EventMetaAttribute> emaBeans, String actorUserName)
            throws Exception {
        Long actorId = getActorId(actorUserName, session);
        LookupValueDAO lvDAO = daoFactory.getLookupValueDAO();

        try {
            EventMetaAttributeDAO emaDAO = daoFactory.getEventMetaAttributeDAO();
            ProjectMetaAttributeDAO pmaDAO = daoFactory.getProjectMetaAttributeDAO();
            SampleMetaAttributeDAO smaDAO = daoFactory.getSampleMetaAttributeDAO();

            ProjectMetaAttribute pma = null;
            SampleMetaAttribute sma = null;

            Map<String, Long> projNameVsId = new HashMap<String, Long>();
            for (EventMetaAttribute attribute : emaBeans) {
                LookupValue attributeNameLookup = lvDAO.getLookupValue(attribute.getAttributeName(), session);
                if(attributeNameLookup == null) {
                    throw new Exception("attribute name '" + attribute.getAttributeName() + "' does not exist.");
                }

                if (attribute.getEventTypeLookupId() == null || attribute.getEventTypeLookupId() == 0) { //free event type id
                    String eventName = attribute.getEventName();
                    LookupValue eventNameLV = lvDAO.getLookupValue(eventName, ModelValidator.EVENT_TYPE_LV_TYPE_NAME, session);
                    if (eventNameLV == null) {
                        throw new Exception("'" + eventName + "' not found as an event type.");
                    }
                    attribute.setEventTypeLookupId(eventNameLV.getLookupValueId());
                }

                if (attribute.getProjectId() == null) { //feed project id
                    String projectName = attribute.getProjectName();
                    Long projectId = getAndCacheProjectId(session, projNameVsId, projectName);
                    attribute.setProjectId(projectId);
                }

                /*EventMetaAttribute duplicate = emaDAO.getEventMetaAttribute(attributeNameLookup.getLookupValueId(), attribute.getProjectId(), attribute.getEventTypeLookupId(), session);
                if(duplicate != null) {
                    throw new Exception("attribute '" + attribute.getAttributeName() + "' already exists for '" + attribute.getEventName() + "' event.");
                }*/

                if (attribute.isSampleRequired() == null) {
                    throw new Exception("SampleRequired field needs value for '" + attribute.getAttributeName() + "'");
                }
                validateEventMetaAttributeInput(attribute);

                if(attribute.getCreatedBy() == null) {
                    attribute.setCreatedBy(actorId);
                } else {
                    attribute.setModifiedBy(actorId);
                }

                if(attribute.getEventMetaAttributeId() == null) {
                    attribute.setEventMetaAttributeId(guidGetter.getGuid());
                }

                if(attribute.getCreationDate() == null) {
                    emaDAO.write(attribute, sessionAndTransactionManager.getTransactionStartDate(), session);
                } else { //update corresponding PMA or SMA
                    emaDAO.update(attribute, sessionAndTransactionManager.getTransactionStartDate(), session);
                    if((pma = getProjectMetaAttribute(attribute.getProjectId(), attributeNameLookup.getLookupValueId())) != null) {
                        pma.setActiveDB(attribute.getActiveDB());
                        pma.setRequiredDB(attribute.getRequiredDB());
                        pma.setOptions(attribute.getOptions());
                        pma.setDesc(attribute.getDesc());
                        pmaDAO.update(pma, sessionAndTransactionManager.getTransactionStartDate(), session);
                    }
                    if((sma = getSampleMeatAttribute(attribute.getProjectId(), attributeNameLookup.getLookupValueId())) != null) {
                        sma.setActiveDB(attribute.getActiveDB());
                        sma.setRequiredDB(attribute.getRequiredDB());
                        sma.setDesc(attribute.getDesc());
                        sma.setOptions(attribute.getOptions());
                        smaDAO.update(sma, sessionAndTransactionManager.getTransactionStartDate(), session);
                    }
                }
            }
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    /**
     * Have a collection of attribute beans of all possible types.  Need to discern the types based on
     * the names of the attributes.  Once established, will use that to write each type of attribute as
     * needed and create an event based on the attributes.
     */
    @Override
    public void writeBackAttributes(List<FileReadAttributeBean> aBeans, String eventName, String actorUserName)
            throws Exception {

        // Pre-emptive bail.
        if (aBeans == null || aBeans.size() == 0) {
            if(!eventName.contains(Constants.EVENT_PROJECT_REGISTRATION) && !eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION)) { //still record project or sample registration events
                throw new Exception("event cannot be created with empty attributes");
            }
        }

        // Check in with Security.  Do this early to avoid wasting cycles on other things.  This process will
        // except out of this method, if user does not have access to ALL projects in the list.
        SecurityDAO securityDAO = daoFactory.getSecurityDAO();
        List<String> projectList = new ArrayList<String>();
        for (FileReadAttributeBean forSecurityBean : aBeans) {
            String forSecurityBeanProjectName = forSecurityBean.getProjectName();
            if (!projectList.contains(forSecurityBeanProjectName))
                projectList.add(forSecurityBeanProjectName);
        }
        projectList = securityDAO.getListOfAuthorizedByName(
                projectList,
                actorUserName,
                ResponseToFailedAuthorization.ThrowException,     // Can throw an exception!
                AccessLevel.Edit,
                session,
                QueryEntityType.Project);

        // Prepare the helper.
        Long actorId = getActorId(actorUserName, session);

        EventPersistenceHelper helper = new EventPersistenceHelper(daoFactory, sessionAndTransactionManager.getTransactionStartDate(), guidGetter);

        helper.setActorInfo(actorId);
        helper.setSession(session);

        try {
            LookupValueDAO lvDAO = daoFactory.getLookupValueDAO();

            String sameProjectName = helper.getUniqueProjectName(aBeans);
            String sameSampleName = helper.getUniqueSampleName(aBeans);

            Long projectId = helper.setProjectInfo(sameProjectName);
            Long sampleId = helper.setSampleInfo(sameSampleName, projectId);

            if (sameSampleName != null && sampleId != null
                    && sameProjectName != null && projectId != null
                    && !helper.isSampleBelongToProject(projectId, sampleId)) {
                String message = UNKNOWN_SAMPLE_FOR_PROJECT_MSG.format(sameProjectName, sameSampleName);
                throw new Exception(message);
            }

            Event event = helper.createEvent(eventName);
            Long eventTypeLookupId = event.getEventType();
            Long eventId = event.getEventId();

            helper.setPhase(EventPersistenceHelper.ProcessingPhase.iterate);
            helper.setDictionaryParentValue(aBeans, EventPersistenceHelper.AttributeType.event);
            for (FileReadAttributeBean bean : aBeans) {
                String beanProjectName = bean.getProjectName();
                String beanSampleName = bean.getSampleName();

                // Need to know if these values were given for this particular attribute.
                boolean projectNameGiven = !isEmpty(beanProjectName);
                boolean sampleNameGiven = !isEmpty(beanSampleName);

                // Associate the attribute with the lookup-value for its attribute name.
                String attribName = bean.getAttributeName();
                if(attribName == null) {
                    // skips bean without attribute name
                    // it is necessary to record empty project/sample registration events without any attributes
                    continue;
                }

                LookupValue attributeNameLookupValue = lvDAO.getLookupValue(attribName, session);
                if (attributeNameLookupValue == null) {
                    throw new Exception(NO_SUCH_ATTRIBUTE_MSG.format(attribName));
                }
                checkName(projectNameGiven, "project", attribName);

                String dataTypeExpected = attributeNameLookupValue.getDataType();
                String lvType = attributeNameLookupValue.getType();
                // Fix/workaround for earlier flawed database load assumption.
                if (lvType.endsWith(ModelValidator.ATTRIBUTE_LV_TYPE_NAME)) {
                    lvType = ModelValidator.ATTRIBUTE_LV_TYPE_NAME;
                }

                String value = bean.getAttributeValue();

                if (lvType.equals(ModelValidator.ATTRIBUTE_LV_TYPE_NAME)) {

                    Long attributeNameLookupValueId = attributeNameLookupValue.getLookupValueId();
                    EventAttribute attribute = helper.createEventAttribute(eventId, attributeNameLookupValueId);

                    String valueResult = setValue(attribute, attribName, dataTypeExpected, value);
                    if (valueResult.length() > 0) {
                        throw new Exception(valueResult);
                    }

                    helper.satisfyEmaRequirement(attribName, eventName, beanProjectName);
                    helper.checkControlledValue(attribName, value, EventPersistenceHelper.AttributeType.event);

                    //Verify session is still open after value check
                    if(!session.isOpen()) {
                        this.open();
                        helper.setSession(session);
                    }
                    helper.writeBackAttribute(eventTypeLookupId, attribName, attributeNameLookupValueId, attribute);

                } else {
                    String message = BAD_LOOKUP_TYPE_MSG.format(attribName, lvType);
                    throw new Exception(message);
                }

                Long attributeNameLookupValueId = attributeNameLookupValue.getLookupValueId();
                // Need to check whether the attribute is also meant to be tracked as project attribute.
                if (isProjectAttribute(projectId, attributeNameLookupValueId)) {
                    // Either existing (update) or new (create).
                    ProjectAttribute attribute = helper.getExistingProjectAttribute(attributeNameLookupValueId, projectId);
                    if (attribute == null) {
                        attribute = helper.createProjectAttribute(attributeNameLookupValue);
                    } else {
                        attribute.setModifiedDate(new Date());
                        attribute.setModifiedBy(actorId);
                    }

                    String valueResult = setValue(attribute, attribName, dataTypeExpected, value);
                    if (valueResult.length() > 0) {
                        throw new Exception(valueResult);
                    }

                    helper.checkControlledValue(attribName, value, EventPersistenceHelper.AttributeType.project);
                    helper.writeBackAttribute(attribName, attributeNameLookupValueId, attribute);
                }

                // Need to check whether the attribute is also meant to be tracked as sample attribute.
                if (isSampleAttribute(projectId, attributeNameLookupValueId) && sampleId!=null) {
                    checkName(sampleNameGiven, "sample", attribName);
                    // Either existing (update) or new (create).
                    SampleAttribute attribute = helper.getExistingSampleAttribute(attributeNameLookupValueId, projectId, sampleId);
                    if (attribute == null) {
                        attribute = helper.createSampleAttribute(sampleId, attributeNameLookupValue);
                    } else {
                        attribute.setModifiedDate(new Date());
                        attribute.setModifiedBy(actorId);
                    }

                    String valueResult = setValue(attribute, attribName, dataTypeExpected, value);
                    if (valueResult.length() > 0) {
                        throw new Exception(valueResult);
                    }

                    helper.checkControlledValue(attribName, value, EventPersistenceHelper.AttributeType.sample);
                    helper.writeBackAttribute(attribName, attributeNameLookupValueId, attribute);

                }

            }
            helper.setPhase(EventPersistenceHelper.ProcessingPhase.postIterate);

            helper.checkRequiredEventAttribsAndSample();

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void loadDictionary(String dictType, String dictValue, String dictCode) throws Exception {
        try {
            DictionaryDAO dictionaryDAO = daoFactory.getDictionaryDAO();

            dictionaryDAO.loadDictionary(dictType, dictValue, dictCode, new Date(), session);

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void loadDictionaryWithDependency(String dictType, String dictValue, String dictCode, String parentDictTypeCode) throws Exception {
        try {
            DictionaryDAO dictionaryDAO = daoFactory.getDictionaryDAO();

            dictionaryDAO.loadDictionaryWithDependency(dictType, dictValue, dictCode, parentDictTypeCode,
                    sessionAndTransactionManager.getTransactionStartDate(), session);

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    public void updateEventStatus(Event event, String actorUserName) throws Exception {
        try {
            EventDAO eventDao = daoFactory.getEventDAO();
            Long actorId = getActorId(actorUserName, session);
            event.setModifiedBy(actorId);

            eventDao.updateEventStatus(event, sessionAndTransactionManager.getTransactionStartDate(), session);

        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        }
    }

    //-----------------------------------------COMMONALITY
    private Long getActorId(String actorUserName, Session session) throws Exception {
        // One actor will be doing all of this.
        ActorDAO actorDAO = daoFactory.getActorDAO();
        Actor actor = actorDAO.getActorByLoginName(actorUserName, session);
        if (actor == null) {
            throw new IllegalArgumentException("Actor " + actorUserName + " unknown to system.");
        }
        return actor.getLoginId();
    }

    private Long getAndCacheProjectId(Session session, Map<String, Long> projNameVsId, String projectName) throws Exception {
        Long projectId;
        projectId = projNameVsId.get(projectName);
        if (projectId == null) {
            projectId = getProjectId(projectName, session);
            projNameVsId.put(projectName, projectId);
        }
        return projectId;
    }

    /**
     * Go and get an id for a project.  Most objects refer to a project by an ID.
     *
     * @throws Exception for called.
     */
    private Long getProjectId(String projectName, Session session) throws Exception {
        ProjectDAO projectDAO = daoFactory.getProjectDAO();
        Project project = projectDAO.getProject(projectName, session);

        if(project == null) {
            throw new Exception("project '" + projectName + "' does not exist.");
        }
        return project.getProjectId();
    }

    private Sample getSample(Long projectId, String sampleName, Session session) throws Exception {
        SampleDAO sampleDAO = daoFactory.getSampleDAO();
        Sample sample = sampleDAO.getSample(projectId, sampleName, session);

        if(sample == null) {
            throw new Exception("sample '" + sampleName + "' does not exist under the project.");
        }
        return sample;
    }

    /**
     * Was the value given or not?
     */
    private boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

    /**
     * Sets any type of value in any type of attribute bean.
     */
    private String setValue(AttributeModelBean bean, String attributeName, String dataType, String value) throws Exception {
        StringBuilder errors = new StringBuilder();
        modelValidator.setValueForAttribute(errors, attributeName, bean, value, dataType);
        return errors.toString();
    }

    private void checkName(boolean nameGiven, String nameType, String attribName) throws Exception {
        if (!nameGiven) {
            throw new Exception(
                    "no " + nameType + " given with which to associate attribute " + attribName);
        }
    }

    private ProjectMetaAttribute getProjectMetaAttribute(Long projectId, Long lookupValueId) throws Exception {
        ProjectMetaAttributeDAO pmaDAO = daoFactory.getProjectMetaAttributeDAO();
        ProjectMetaAttribute pma = null;
        pma = pmaDAO.getProjectMetaAttribute(lookupValueId, projectId, session);
        return pma;
    }
    private boolean isProjectAttribute(Long projectId, Long lookupValueId) throws Exception {
        return getProjectMetaAttribute(projectId, lookupValueId) != null;
    }

    private SampleMetaAttribute getSampleMeatAttribute(Long projectId, Long lookupValueId) throws Exception {
        SampleMetaAttributeDAO smaDAO = daoFactory.getSampleMetaAttributeDAO();
        SampleMetaAttribute sma = null;
        sma = smaDAO.getSampleMetaAttribute(lookupValueId, projectId, session);
        return sma;
    }
    private boolean isSampleAttribute(Long projectId, Long lookupValueId) throws Exception {
        return getSampleMeatAttribute(projectId, lookupValueId) != null;
    }

    //-----------------------------------------VALIDATION
    private void validateProjectInput(Project project) throws InvalidInputException {
        // Do some validation.
        if (isEmpty(project.getProjectName())) {
            throw new InvalidInputException("Invalid Project Definition: no project name.");
        }
        int projectLevel = project.getProjectLevel();
        String parentProjectName = project.getParentProjectName();
        if (projectLevel > 1 && isEmpty(parentProjectName) && project.getParentProjectId() == null) {
            throw new InvalidInputException(
                    "Invalid Project Definition: Project Level not at base, but parent project name is not empty.");
        }
    }

    private void validateSampleInput(Sample sample) throws InvalidInputException {
        if (isEmpty(sample.getSampleName()) || isEmpty(sample.getProjectName())) {
            throw new InvalidInputException("Invalid Sample Definition: Must have both project and sample name.");
        }
    }

    private void validateProjectMetaAttributeInput(ProjectMetaAttribute attribute)
            throws InvalidInputException, ClassNotFoundException, NoSuchMethodException {
        /*if (isEmpty(attribute.getAttributeName())) {
            throw new InvalidInputException("Invalid project meta attribute: must have attribute name.");
        }
        String dataType = attribute.getDataType();
        if (isEmpty(attribute.getProjectName()) || isEmpty(dataType)) {
            throw new InvalidInputException("Invalid project meta attribute: must have project name and data type.");
        }

        // Check: valid data type?
        modelValidator.isValidDataType(dataType);*/
        if (isEmpty(attribute.getAttributeName()) || isEmpty(attribute.getProjectName())) {
            throw new InvalidInputException("Invalid project meta attribute: must have attribute name and project name.");
        } else if(attribute.getOptions() != null && attribute.getOptions().contains(VALIDATION_PREFIX)){
            checkValidationClassAndMethod(attribute.getOptions());
        }
    }

    private void validateSampleMetaAttributeInput(SampleMetaAttribute attribute)
            throws InvalidInputException, ClassNotFoundException, NoSuchMethodException {
        /*if (isEmpty(attribute.getAttributeName())) {
            throw new InvalidInputException("Invalid project meta attribute: must have attribute name.");
        }
        String dataType = attribute.getDataType();
        if (isEmpty(attribute.getProjectName()) || isEmpty(dataType)) {
            throw new InvalidInputException(
                    "Invalid sample meta attribute: must have project name, sample name, and data type.");
        }

        // Check: valid data type?
        modelValidator.isValidDataType(dataType);*/
        if (isEmpty(attribute.getAttributeName()) || isEmpty(attribute.getProjectName())) {
            throw new InvalidInputException("project name and attribute name are required.");
        } else if(attribute.getOptions() != null && attribute.getOptions().contains(VALIDATION_PREFIX)){
            checkValidationClassAndMethod(attribute.getOptions());
        }
    }

    private void validateEventMetaAttributeInput(EventMetaAttribute attribute)
            throws InvalidInputException, ClassNotFoundException, NoSuchMethodException {
        if (isEmpty(attribute.getAttributeName())) {
            throw new InvalidInputException("attribute name is empty or null.");
        } else if(attribute.getOptions() != null && attribute.getOptions().contains(VALIDATION_PREFIX)){
            checkValidationClassAndMethod(attribute.getOptions());
        }

        if (isEmpty(attribute.getProjectName()) || isEmpty(attribute.getEventName())) {
            throw new InvalidInputException("project name and event name are required.");
        }

        /*
        * There is no need to check for data type since lookup_value has data typ with new schema
        * 8/3/12 by hkim
        *
        String dataType = attribute.getDataType();
        if (isEmpty(attribute.getProjectName()) || isEmpty(dataType)) {
            throw new InvalidInputException(
                    "Invalid event meta attribute: must have project name, and data type.");
        }

        // Check: valid data type?
        modelValidator.isValidDataType(dataType);
        */
    }

    public void checkValidationClassAndMethod(String options) throws ClassNotFoundException, NoSuchMethodException{
        int indexOfValidate = options.indexOf(VALIDATION_PREFIX);

        String valStr = options.substring(indexOfValidate, options.length());

        valStr = valStr.substring(VALIDATION_PREFIX.length(), valStr.length());
        String[] validationRequests = valStr.split(",");

        for(String valReq : validationRequests) {
            String[] classMethodVal = valReq.split("\\.");

            try {
                Class validatorClass = Class.forName("org.jcvi.ometa.validation." + classMethodVal[0]);

                if (classMethodVal[1].contains("(") && classMethodVal[1].contains(")")) {
                    int indexOfArg = classMethodVal[1].indexOf("(");
                    classMethodVal[1] = classMethodVal[1].substring(0, indexOfArg);

                    validatorClass.getDeclaredMethod(classMethodVal[1], String.class, String.class);
                } else {
                    classMethodVal[1] = classMethodVal[1].replaceAll("\\}", "");
                    if(!classMethodVal[1].equals(Constants.VAL_UNIQUE_METHOD_NAME)) {
                        validatorClass.getDeclaredMethod(classMethodVal[1], String.class);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException(String.format(ErrorMessages.VALIDATION_CLASS_NOT_FOUND, classMethodVal[0]));
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(String.format(ErrorMessages.VALIDATION_METHOD_NOT_FOUND, classMethodVal[1]));
            }
        }
    }

}
