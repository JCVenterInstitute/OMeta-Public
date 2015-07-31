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
import org.jcvi.ometa.hibernate.dao.*;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.GuidGetter;
import org.jcvi.ometa.validation.DataValidator;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/16/11
 * Time: 10:21 AM
 *
 * This delegate of the write-persister is meant to be a means of organizing certain information to cut down the
 * "surface area" of some intra-class method calls, as well as to take some burden off the code reader for
 * writebackBeanPersister.
 */
public class EventPersistenceHelper {

    protected static final MessageFormat UNKNOWN_META_ATTRIB_HUMAN_READABLE_MSG = new MessageFormat(
            "Meta Attribute for {0} unknown to {1} event type, in project {2}." );
    protected static final MessageFormat UNKNOWN_ATTRIB_TYPE_HUMAN_READABLE_MSG = new MessageFormat(
            "Unknown attribute type {0} " );
    protected static final MessageFormat CONTROLLED_VOCAB_VIOLATED_HUMAN_READABLE_MSG = new MessageFormat(
            "{0} {1} attribute may not be set to {2}.  Possible values: {3} " );
    protected static final MessageFormat NOT_VALID_ATTRIB_HUMAN_READABLE_MSG = new MessageFormat(
            "{0} {1} attribute may not be set to {2}.  Validation failed! " );
    protected static final MessageFormat EVENT_NOT_IN_PROJECT_HUMAN_READABLE_MSG = new MessageFormat(
            "Event attribute of {0} does not belong to project {1} " );
    protected static final MessageFormat INVALID_PROJECT_META_ATTRIB_HUMAN_READABLE_MSG = new MessageFormat(
            "Project attribute of {0} does not belong to project {1} " );
    protected static final MessageFormat INVALID_SAMPLE_META_ATTRIB_HUMAN_READABLE_MSG = new MessageFormat(
            "Sample attribute of {0} does not belong to project {1} and sample {2} " );
    protected static final MessageFormat NO_SUCH_EVENT_HUMAN_READABLE_MSG = new MessageFormat(
            "No such event type {0} " );
    protected static final MessageFormat NO_SUCH_SAMPLE_HUMAN_READABLE_MSG = new MessageFormat(
            "Failed to find sample by name of {0} " );
    protected static final MessageFormat UNEXPECTED_LOOKUP_EVENT_TYPE_HUMAN_READABLE_MSG = new MessageFormat(
            "Found lookup value for {0}, but it was not of type {1} " );
    protected static final MessageFormat UNKNOWN_PROJECT_HUMAN_READABLE_MSG = new MessageFormat(
            "Project {0} unknown." );
    protected static final MessageFormat SAMPLE_REQUIRED_HUMAN_READABLE_MSG = new MessageFormat(
            "Event attribute of {0} requires sample " );
    protected static final MessageFormat EXCEED_VALUE_LENGTH_HUMAN_READABLE_MSG = new MessageFormat(
            "Event attribute of {0} exceeds value length limit ( {1} character(s)) " );

    private DAOFactory daoFactory;
    private GuidGetter guidGetter;
    private String projectName;
    private Long projectId;
    private Long eventTypeLookupId;
    private String sampleName;
    private Long sampleId;
    private Long actorId;
    private Session session;
    private Date transactionStartDate;
    // These to enforce "required" attributes being given.
    private Map<String,Boolean> requiredPmaToSatisfied = null;
    private Map<String,Boolean> requiredSmaToSatisfied = null;
    private Map<String,Boolean> requiredEmaToSatisfied = null;

    private List<String> attributeNamesPermitted = null;

    // These to enforce "required" attribute values given.
    private Map<String,String> pmaNameToControls = null;
    private Map<String,String> smaNameToControls = null;
    private Map<String,String> emaNameToControls = null;

    private Map<String,String> dictParentValueMap = null;

    private boolean sampleAttributesEncountered = false;
    private boolean isSampleRequiredForEvent = false;
    private String eventType = null;

    private ProcessingPhase buildPhase;

    /** Construct with tools for dealing with session, DAOs and GUIDs. */
    public EventPersistenceHelper(
            DAOFactory daoFactory, Date transactionStartDate, GuidGetter guidGetter ) {
        attributeNamesPermitted = new ArrayList<String>();
        this.daoFactory = daoFactory;
        this.transactionStartDate = transactionStartDate;
        this.guidGetter = guidGetter;
        buildPhase = ProcessingPhase.preIterate;
    }

    /** Progress the builder aspects of this class through checkable phases. */
    public void setPhase( ProcessingPhase phase ) {
        this.buildPhase = phase;
    }

    public Long setProjectInfo( String beanProjectName ) throws Exception {
        boolean beanProjectNameGiven = beanProjectName != null  &&  beanProjectName.trim().length() > 0;

        if ( beanProjectNameGiven ) {
            projectName = beanProjectName;
            projectId = getProjectId(beanProjectName);

            getAttributeInclusionAndValueRequirements(beanProjectName, AttributeType.project);
            getAttributeInclusionAndValueRequirements(beanProjectName, AttributeType.sample);
        }

        // NOTE: if not newly-set by this operation, will return old one.
        return projectId;
    }

    /** Make sure the sample name and ID are contained herein. */
    public Long setSampleInfo(String beanSampleName, Long projectId) throws Exception {
        sampleId = null;

        boolean beanSampleNameGiven = beanSampleName != null  &&  beanSampleName.trim().length() > 0;
        if ( beanSampleNameGiven ) {
            sampleId = getSampleId(beanSampleName, projectId);
            sampleName = beanSampleName;
        }

        // Note: if not newly-set by this operation, will return old one.
        return sampleId;
    }

    public String getUniqueSampleName(List<FileReadAttributeBean> aBeans) throws Exception {
        String sameSampleName = null;
        for ( FileReadAttributeBean bean: aBeans ) {
            String beanSampleName = bean.getSampleName();
            if ( sameSampleName == null ) {
                sameSampleName = beanSampleName;
            }
            else {
                if ( ! beanSampleName.equals( sameSampleName ) ) {
                    throw new Exception( "all sample names within one event must be the same." +
                     " Sample name " + beanSampleName + " differs from pre-established one of " +
                     sameSampleName );
                }
            }
        }
        return sameSampleName;
    }

    public String getUniqueProjectName(List<FileReadAttributeBean> aBeans) throws Exception {
        String sameProjectName = null;
        for ( FileReadAttributeBean bean: aBeans ) {
            String beanProjectName = bean.getProjectName();
            if ( sameProjectName == null ) {
                sameProjectName = beanProjectName;
            }
            else {
                if ( ! beanProjectName.equals( sameProjectName ) ) {
                    throw new Exception( "all project names within one event must be the same." +
                        " Project name " + beanProjectName + " differs from pre-established one of " +
                        sameProjectName );
                }
            }
        }
        return sameProjectName;
    }

    /** Write back an event object to database.  Track required event attributes.  Cache event info. */
    public Event createEvent(String eventType) throws Exception {
        checkPreIteratePhase();
        this.eventType = eventType;

        // If not first set this time, return old ID.
        if ( requiredEmaToSatisfied == null ) {
            if ( projectName == null ) {
                throw new IllegalStateException( "Must first set project info before calling this." );
            }

            getAttributeInclusionAndValueRequirements( projectName, eventType );
            eventTypeLookupId = getEventTypeLookupValueId( eventType, daoFactory.getLookupValueDAO() );
        }

        Event event = new Event();
        event.setCreatedBy( actorId );
        event.setEventId( guidGetter.getGuid() );
        event.setProjectId( projectId );
        event.setSampleId( sampleId );
        event.setEventType( eventTypeLookupId );

        EventDAO eDAO = daoFactory.getEventDAO();
        event = eDAO.write( event, null, eventType, new Date(), session );

        return event;
    }

    public void satisfyEmaRequirement( String attribName, String eventName, String projectName ) throws Exception {
        checkIteratePhase();
        if ( requiredEmaToSatisfied.containsKey( attribName ) ) {
            requiredEmaToSatisfied.put( attribName, Boolean.TRUE );
        }
        else if ( ! this.attributeNamesPermitted.contains( attribName ) ) {
            String message = UNKNOWN_META_ATTRIB_HUMAN_READABLE_MSG.format( new Object[] { attribName, eventName, projectName } );
            throw new Exception( message );
        }
    }

    public void setDictionaryParentValue(List<FileReadAttributeBean> aBeans, AttributeType aType) throws Exception {
        Map<String,String> controlMap = null;
        switch( aType ) {
            case project:
                controlMap = pmaNameToControls;
                break;
            case sample:
                controlMap = smaNameToControls;
                break;
            case event:
                controlMap = emaNameToControls;
                break;
            default: {
                String message = UNKNOWN_ATTRIB_TYPE_HUMAN_READABLE_MSG.format( new Object[] { aType.toString() } );
                throw new Exception( message );
            }
        }

        dictParentValueMap = new HashMap<String, String>(0);
        String parentDef = "Parent:";
        for( Map.Entry<String, String> entry : controlMap.entrySet()){
            String value = entry.getValue();
            if(value.contains(parentDef)){
                dictParentValueMap.put(value.substring(value.lastIndexOf(parentDef) + 7), "");
            }
        }

        for(FileReadAttributeBean bean : aBeans) {
            String parentFieldName = bean.getAttributeName();

            if(dictParentValueMap.get(parentFieldName) != null){
                dictParentValueMap.put(parentFieldName, bean.getAttributeValue());
            }
        }
    }

    /** Check that the value given is within the control set, if a control set was given. */
    public void checkControlledValue( String attributeName, String attributeValue, AttributeType aType )
            throws Exception {
        Map<String,String> controlMap = null;
        switch( aType ) {
            case project:
                controlMap = pmaNameToControls;
                break;
            case sample:
                controlMap = smaNameToControls;
                break;
            case event:
                controlMap = emaNameToControls;
                break;
            default: {
                String message = UNKNOWN_ATTRIB_TYPE_HUMAN_READABLE_MSG.format( new Object[] { aType.toString() } );
                throw new Exception( message );
            }
        }

        String controlValues = controlMap.get( attributeName );

        // Not all values are controlled.  Some may have empty control values.
        if ( controlValues != null  &&  controlValues.trim().length() > 0 ) {
            String multiplePrefix = "multi(";
            String radioPrefix = "radio(";
            String dictionaryPrefix = "Dictionary:";
            String validationPrefix = "validate:";
            boolean valid = true;

            if(controlValues.contains(validationPrefix)){
                int indexOfValidate = controlValues.indexOf(validationPrefix);

                String valStr = controlValues.substring(indexOfValidate, controlValues.length());
                controlValues = controlValues.replace(valStr, "");

                valStr = valStr.substring(validationPrefix.length(), valStr.length());
                String[] validationRequests = valStr.split(",");

                for(String valReq : validationRequests){
                    String[] classMethodVal = valReq.split("\\.");
                    boolean hasArgument = false;
                    String argVal = null;

                    if(classMethodVal[1].contains("(") && classMethodVal[1].contains(")")){
                        int indexOfArg = classMethodVal[1].indexOf("(");
                        argVal = classMethodVal[1].substring(indexOfArg+1, classMethodVal[1].length() - 1);

                        classMethodVal[1] = classMethodVal[1].substring(0, indexOfArg);
                        hasArgument = true;
                    }

                    Class validatorClass = Class.forName("org.jcvi.ometa.validation."+classMethodVal[0]);
                    Method validatorMethod;
                    if(hasArgument){
                        validatorMethod = validatorClass.getDeclaredMethod(classMethodVal[1], String.class, String.class);
                        valid = (Boolean) validatorMethod.invoke(validatorClass.newInstance(), attributeValue, argVal);
                    } else {
                        validatorMethod = validatorClass.getDeclaredMethod(classMethodVal[1], String.class);
                        valid = (Boolean) validatorMethod.invoke(validatorClass.newInstance(), attributeValue);
                    }

                    if(!valid) break;
                }
            }

            if(valid) {
                if(!controlValues.equals("")) {
                    //trim multiple select for validation
                    if (controlValues.startsWith(multiplePrefix) && controlValues.endsWith(")")) {
                        controlValues = controlValues.substring(multiplePrefix.length(), controlValues.length() - 1);
                    } else if (controlValues.startsWith(radioPrefix) && controlValues.endsWith(")")) {
                        controlValues = controlValues.substring(radioPrefix.length(), controlValues.length() - 1);
                    } else if (controlValues.startsWith(dictionaryPrefix)) {
                        String dictType = controlValues.replace(dictionaryPrefix, "");
                        boolean hasParent = dictType.contains("Parent:");

                        try {
                            Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
                            ReadBeanPersister readPersister = new ReadBeanPersister(props);
                            List<Dictionary> dictList = null;

                            if (hasParent) {
                                String[] dictOpts = dictType.split(",Parent:");
                                String parentAttrName = dictOpts[1];
                                String parentAttrValue = dictParentValueMap.get(parentAttrName);

                                if (parentAttrValue != null && !parentAttrValue.equals("")) {
                                    dictList = readPersister.getDictionaryDependenciesByType(
                                            controlMap.get(parentAttrName).replace(dictionaryPrefix, ""), parentAttrValue.split(" - ")[0]);
                                }
                            } else {
                                dictList = readPersister.getDictionaryByType(dictType);
                            }

                            StringBuilder sb = new StringBuilder();
                            String delim = "";

                            for (Dictionary dictionary : dictList) {
                                sb.append(delim).append(dictionary.getDictionaryCode());

                                delim = ";";
                            }

                            controlValues = sb.toString();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    List<String> controlValueList = Arrays.asList(controlValues.split(";"));
                    boolean found = true;
                    if (attributeValue.contains(",")) {
                        for (String currentAttributeValue : attributeValue.split(",")) {
                            if (controlValueList.indexOf(currentAttributeValue.trim()) < 0) {
                                found = false;
                                break;
                            }
                        }
                    } else {
                        found = controlValueList.indexOf(attributeValue) >= 0;
                    }
                /*for ( String controlValue: controlValueArray ) {
                    if ( attributeValue.equals( controlValue ) ) {
                        found = true;
                        break;
                    }
                }*/

                    if (!found) {
                        String message = CONTROLLED_VOCAB_VIOLATED_HUMAN_READABLE_MSG.format(
                                new Object[]{aType.toString(), attributeName, attributeValue, controlValues});
                        throw new Exception(message);

                    }
                }
            } else {
                String message = NOT_VALID_ATTRIB_HUMAN_READABLE_MSG.format(
                        new Object[]{aType.toString(), attributeName, attributeValue});
                throw new Exception(message);
            }
        }
    }

    public void setActorInfo( Long actorId ) {
        checkPreIteratePhase();
        this.actorId = actorId;
    }

    public void setSession( Session session ) {
        this.session = session;
    }

    public void writeBackAttribute(
            Long eventTypeLookupId,
            String attribName,
            Long attributeNameLookupValueId,
            EventAttribute attribute) throws DAOException {
        checkIteratePhase();  // Attributes during iteration.  Later - missed oppt'y

        EventMetaAttributeDAO metaAttributeDAO = daoFactory.getEventMetaAttributeDAO();
        EventMetaAttribute ema = metaAttributeDAO.getEventMetaAttribute(attributeNameLookupValueId, projectId, eventTypeLookupId, session);

        checkSampleGivenForEventAttribute(attribName, ema);
        String dataType = ema.getLookupValue().getDataType();
        int attrValueLength = (dataType.equals("string")) ? attribute.getAttributeStringValue().length()
                : (dataType.equals("int")) ? attribute.getAttributeIntValue().toString().length()
                : (dataType.equals("date")) ? attribute.getAttributeDateValue() .toString().length()
                : attribute.getAttributeFloatValue().toString().length() ;

        if ( ema == null   &&  ! attributeNamesPermitted.contains( attribName ) ) {
            String message = EVENT_NOT_IN_PROJECT_HUMAN_READABLE_MSG.format( new Object[] { attribName, projectName } );
            throw new DAOException( message );
        }
        else if ( ema.isSampleRequired() && ( sampleId == null || sampleId == 0 ) ) {
            String message = SAMPLE_REQUIRED_HUMAN_READABLE_MSG.format(new Object[]{attribName } );
            throw new DAOException( message );
        }
        else if ( ema.getValueLength() != null && attrValueLength > ema.getValueLength() ) {
            String message = EXCEED_VALUE_LENGTH_HUMAN_READABLE_MSG.format(new Object[]{attribName, ema.getValueLength() } );
            throw new DAOException( message );
        }
        else {
            EventAttributeDAO attribDAO = daoFactory.getEventAttributeDAO();
            attribDAO.write( attribute, transactionStartDate, session );

        }
    }

    public void writeBackAttribute(
            String attribName,
            Long attributeNameLookupValueId,
            ProjectAttribute attribute) throws DAOException {
        checkIteratePhase();  // Attributes during iteration.  Later - missed oppt'y

        ProjectMetaAttributeDAO metaAttributeDAO = daoFactory.getProjectMetaAttributeDAO();
        ProjectMetaAttribute pma = metaAttributeDAO.getProjectMetaAttribute(
                attributeNameLookupValueId, projectId, session);
        if ( pma == null ) {
            String msg = INVALID_PROJECT_META_ATTRIB_HUMAN_READABLE_MSG.format( new Object[] { attribName, projectName } );
            throw new DAOException( msg );

        }
        else {
            ProjectAttributeDAO attributeDAO = daoFactory.getProjectAttributeDAO();
            attributeDAO.write( attribute, transactionStartDate, session );
        }
    }

    public ProjectAttribute getExistingProjectAttribute( Long attributeNameLookupValueId, Long projectId )
        throws DAOException {
        ProjectAttributeDAO attributeDAO = daoFactory.getProjectAttributeDAO();
        return attributeDAO.getProjectAttribute( projectId, attributeNameLookupValueId, session );
    }

    public SampleAttribute getExistingSampleAttribute( Long attributeNameLookupValueId, Long projectId, Long sampleId )
        throws DAOException {
        SampleAttributeDAO attributeDAO = daoFactory.getSampleAttributeDAO();
        return attributeDAO.getSampleAttribute(projectId, sampleId, attributeNameLookupValueId, session);
    }

    public void writeBackAttribute(
            String attribName,
            Long attributeNameLookupValueId,
            SampleAttribute attribute) throws DAOException {
        checkIteratePhase();

        SampleMetaAttributeDAO metaAttributeDAO = daoFactory.getSampleMetaAttributeDAO();
        SampleMetaAttribute pma = metaAttributeDAO.getSampleMetaAttribute(
                attributeNameLookupValueId, projectId, session);
        if ( pma == null ) {
            String message = INVALID_SAMPLE_META_ATTRIB_HUMAN_READABLE_MSG.format( new Object[] { attribName, projectName, sampleName } );
            throw new DAOException( message );

        }
        else {
            SampleAttributeDAO attributeDAO = daoFactory.getSampleAttributeDAO();
            attributeDAO.write( attribute, transactionStartDate, session );
        }
    }

    public EventAttribute createEventAttribute(Long eventId, Long attributeNameLookupValueId) throws Exception {
        checkIteratePhase();
        EventAttribute attribute = new EventAttribute();
        attribute.setId( guidGetter.getGuid() );
        attribute.setEventId(eventId);
        attribute.setProjectId(projectId);
        attribute.setCreatedBy( actorId );
        attribute.setCreationDate( null );

        attribute.setNameLookupValueId( attributeNameLookupValueId);
        return attribute;
    }

    public SampleAttribute createSampleAttribute(Long sampleId, LookupValue attributeNameLookupValue) throws Exception {
        checkIteratePhase();
        sampleAttributesEncountered = true;
        SampleAttribute attribute = new SampleAttribute();
        attribute.setId( guidGetter.getGuid() );
        attribute.setProjectId(projectId);
        attribute.setSampleId(sampleId);
        attribute.setCreatedBy( actorId );
        attribute.setCreationDate( null );

        attribute.setNameLookupValueId( attributeNameLookupValue.getLookupValueId() );
        return attribute;
    }

    public ProjectAttribute createProjectAttribute(LookupValue attributeNameLookupValue) throws Exception {
        checkIteratePhase();
        //ProjectName	SampleName	AttributeName	AttributeValue
        ProjectAttribute attribute = new ProjectAttribute();
        attribute.setId(guidGetter.getGuid());
        attribute.setProjectId(projectId);
        attribute.setCreatedBy( actorId );
        attribute.setCreationDate( null );

        attribute.setNameLookupValueId( attributeNameLookupValue.getLookupValueId() );
        return attribute;
    }

    public Long getEventTypeLookupValueId(String eventName, LookupValueDAO lvDAO) throws Exception {
        Long eventTypeLookupId;LookupValue eventTypeLV =
                lvDAO.getLookupValue( eventName, ModelValidator.EVENT_TYPE_LV_TYPE_NAME, session );
        if ( eventTypeLV == null ) {
            String message = NO_SUCH_EVENT_HUMAN_READABLE_MSG.format( new Object[] { eventName } );
            throw new Exception(
                    message
            );
        }
        eventTypeLookupId = eventTypeLV.getLookupValueId();
        return eventTypeLookupId;
    }

    public void checkRequiredEventAttribsAndSample() throws Exception {
        checkPostIteratePhase();
        // hkim removed requirement check for only sample attribute
        //if ( sampleAttributesEncountered )
            checkRequiredAttribs( requiredEmaToSatisfied, "Event"  );

        // Event meta attributes are what decide whether the sample is required for an event, so this check
        // is being done here.
        checkSampleForEvent();
    }

    /**
     * Go and get an id for a project.  Most objects refer to a project by an ID.
     *
     * @throws Exception for called.
     */
    public Long getProjectId(String projectName) throws Exception {
        ProjectDAO projectDAO = daoFactory.getProjectDAO();
        Project project = projectDAO.getProject(
                projectName, session);
        return project.getProjectId();
    }

    public Long getSampleId(String beanSampleName, Long projectId) throws DAOException {
        Long sampleId;
        SampleDAO sDAO = daoFactory.getSampleDAO();
        Sample sample = sDAO.getSample(projectId, beanSampleName, session);
        if ( sample == null ) {
            String message = NO_SUCH_SAMPLE_HUMAN_READABLE_MSG.format( new Object[] { beanSampleName } );
            throw new DAOException( message );
        }
        sampleId = sample.getSampleId();
        return sampleId;
    }

    //----------------------------------------------NON-INTERFACE METHODS
    private void checkRequiredAttribs( Map<String,Boolean> requiredToSatisfied, String logStr )
        throws Exception {

        StringBuilder builder = new StringBuilder();
        for ( String attributeName: requiredToSatisfied.keySet() ) {
            Boolean satisfied = requiredToSatisfied.get( attributeName );
            if ( ! satisfied )
                builder.append( "\n" )
                       .append( logStr + " attribute '" + attributeName + "' required.");
        }

        if ( builder.length() > 0 )
            throw new Exception( builder.toString() );

    }

    /**
     * Checks that, if an event attribute requires a sample, that sample is given.  Has the side-effect, that a
     * flag for whole-event requires a sample, is set true if this event attribute needs a sample.
     *
     * @throws DAOException if the sample is required and not given.
     */
    private void checkSampleGivenForEventAttribute(String attribName, EventMetaAttribute ema) throws DAOException {
        if (ema.isSampleRequired() || this.eventType.contains(Constants.EVENT_SAMPLE_REGISTRATION)) {
            isSampleRequiredForEvent = true;
            if ( sampleId == null ) {
                throw new DAOException("'" + attribName + "' for '" + eventType + "' requires a sample");
            }
        }
    }

    /**
     * Checks that the sample has NOT been set for an event, if it is not expected.
     *
     * @throws DAOException if sample was set and no ema called for one.
     */
    private void checkSampleForEvent() throws DAOException {
        if(!isSampleRequiredForEvent  &&  sampleId != null)
            throw new DAOException("'" + eventType + " should not have a sample associated with it." );
    }

    /** Will return the required meta attributes for project or sample, depending on boolean switch. */
    private void getAttributeInclusionAndValueRequirements(
            String projectName, AttributeType attributeType)
        throws DAOException {

        Map<String, Boolean> rtnMap = null;
        if ( attributeType == AttributeType.project ) {

            requiredPmaToSatisfied = new HashMap<String,Boolean>();
            pmaNameToControls = new HashMap<String,String>();
            rtnMap = requiredPmaToSatisfied;
            List<ProjectMetaAttribute> pmaList = getProjectMetaAttributes( projectName );
            for ( ProjectMetaAttribute pma: pmaList ) {
                String pmaName = pma.getAttributeName();
                if ( pmaName == null ) {
                    System.out.println("NULL attribute found for Project Meta Attribute in project " +
                                       projectName + " " + pma.getDesc() );
                }
                if ( pma.isRequired() ) {
                    rtnMap.put( pmaName, Boolean.FALSE );
                }

                if ( pma.getOptions() != null ) {
                    pmaNameToControls.put( pmaName, pma.getOptions() );
                }
            }
        }
        else if ( attributeType == AttributeType.sample ) {

            requiredSmaToSatisfied = new HashMap<String,Boolean>();
            smaNameToControls = new HashMap<String,String>();

            rtnMap = requiredSmaToSatisfied;
            List<SampleMetaAttribute> smaList = getSampleMetaAttributes( projectName );
            for ( SampleMetaAttribute sma: smaList ) {
                if ( sma.isRequired() ) {
                    rtnMap.put( sma.getAttributeName(), Boolean.FALSE );
                }

                if ( sma.getOptions() != null ) {
                    smaNameToControls.put( sma.getAttributeName(), sma.getOptions() );
                }
            }
        }

    }

    /** Will return the required meta attributes for event, by type. */
    private void getAttributeInclusionAndValueRequirements(String projectName, String eventType)
        throws DAOException {

        requiredEmaToSatisfied = new HashMap<String,Boolean>();
        emaNameToControls = new HashMap<String,String>();

        List<EventMetaAttribute> emaList = getEventMetaAttributes( projectName, eventType );
        for ( EventMetaAttribute ema: emaList ) {
            attributeNamesPermitted.add( ema.getAttributeName() );
            if ( ema.isRequired() ) {
                requiredEmaToSatisfied.put( ema.getAttributeName(), Boolean.FALSE );
            }

            if ( ema.getOptions() != null ) {
                emaNameToControls.put( ema.getAttributeName(), ema.getOptions() );
            }
        }

    }

    private List<ProjectMetaAttribute> getProjectMetaAttributes( String projectName ) throws DAOException {
        List<ProjectMetaAttribute> pmaBeans;
        ProjectDAO projectDAO = daoFactory.getProjectDAO();
        Project project = projectDAO.getProject( projectName, session );
        if ( project == null ) {
            throw exceptOnUnknownProject( projectName );
        }

        ProjectMetaAttributeDAO pmaDao = daoFactory.getProjectMetaAttributeDAO();
        pmaBeans = pmaDao.readAll( project.getProjectId(), session );
        return pmaBeans;
    }

    private List<SampleMetaAttribute> getSampleMetaAttributes( String projectName ) throws DAOException {
        List<SampleMetaAttribute> smaBeans;
        ProjectDAO projectDAO = daoFactory.getProjectDAO();
        Project project = projectDAO.getProject( projectName, session );
        if ( project == null ) {
            throw exceptOnUnknownProject( projectName );
        }

        SampleMetaAttributeDAO smaDao = daoFactory.getSampleMetaAttributeDAO();
        smaBeans = smaDao.readAll( project.getProjectId(), session );
        return smaBeans;
    }

    private List<EventMetaAttribute> getEventMetaAttributes( String projectName, String eventType )
            throws DAOException {

        List<EventMetaAttribute> emaBeans;
        ProjectDAO projectDAO = daoFactory.getProjectDAO();
        Project project = projectDAO.getProject( projectName, session );
        if ( project == null ) {
            throw exceptOnUnknownProject( projectName );
        }

        LookupValueDAO lvDao = daoFactory.getLookupValueDAO();
        LookupValue lv = lvDao.getLookupValue( eventType, session );
        if ( lv == null ) {
            return Collections.emptyList();
        }
        else if ( ! lv.getType().equals( ModelValidator.EVENT_TYPE_LV_TYPE_NAME) ) {
            String message = UNEXPECTED_LOOKUP_EVENT_TYPE_HUMAN_READABLE_MSG.format(
                    eventType, ModelValidator.EVENT_TYPE_LV_TYPE_NAME );
            throw new DAOException( message );
        }

        EventMetaAttributeDAO emaDao = daoFactory.getEventMetaAttributeDAO();
        emaBeans = emaDao.readAll(project.getProjectId(), lv.getLookupValueId(), session);
        return emaBeans;
    }

    /** Convenience for excepting in a specific circumstance. */
    private DAOException exceptOnUnknownProject( String projectName ) {
        String message = UNKNOWN_PROJECT_HUMAN_READABLE_MSG.format( projectName );
        return new DAOException( message );
    }

    public boolean isSampleBelongToProject( Long projectId, Long sampleId ) throws DAOException {
        boolean isBelong = false;

        SampleDAO sDAO = daoFactory.getSampleDAO();
        Sample sample = sDAO.getSample( projectId, sampleId, session);
        if ( sample != null ) {
            isBelong = true;
        }

        return isBelong;
    }

    private void checkPostIteratePhase() {
        if ( ! ( buildPhase == ProcessingPhase.postIterate ) ) {
            throw new IllegalStateException("Calling out-of-order: please call this method in the post-iterate phase.");
        }
    }

    private void checkIteratePhase() {
        if ( ! ( buildPhase == ProcessingPhase.iterate ) ) {
            throw new IllegalStateException("Calling out-of-order: please call this method in the iterate phase.");
        }
    }

    private void checkPreIteratePhase() {
        if ( ! ( buildPhase == ProcessingPhase.preIterate ) ) {
            throw new IllegalStateException("Calling out-of-order: please call this method in the pre-iterate phase.");
        }
    }

    public static enum AttributeType {
        project, sample, event
    }

    /** This is for dictating "state" for the "builder" aspects of this class. */
    public static enum ProcessingPhase {
        preIterate, iterate, postIterate
    }

    public void setEmaNameToControls(Map<String, String> emaNameToControls) {
        this.emaNameToControls = emaNameToControls;
    }
}
