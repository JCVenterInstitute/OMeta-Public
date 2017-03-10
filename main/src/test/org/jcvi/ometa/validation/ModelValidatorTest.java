package org.jcvi.ometa.validation;

import org.jcvi.ometa.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.jcvi.ometa.validation.ModelValidator.PST_ACCEPT_DATE_FORMAT;
import static org.junit.Assert.*;

/**
 * Created by mkuscuog on 3/10/2017.
 */
public class ModelValidatorTest {
    ModelValidator modelValidator;
    MessageFormat EXTRANEOUS_SAMPLE_ERR_MSG_FMT =
            new MessageFormat("Event {0} requires no sample, but sample was given.");
    MessageFormat NO_SAMPLE_ERR_MSG_FMT =
            new MessageFormat("Event {0} requires a sample for event attribute(s) {1} but no sample was given.");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        modelValidator = new ModelValidator();
    }

    @Test
    public void isValidDataType() throws Exception {
        assertTrue(modelValidator.isValidDataType(modelValidator.DATE_DATA_TYPE));
        assertTrue(modelValidator.isValidDataType(modelValidator.STRING_DATA_TYPE));
        assertTrue(modelValidator.isValidDataType(modelValidator.FLOAT_DATA_TYPE));
        assertTrue(modelValidator.isValidDataType(modelValidator.INT_DATA_TYPE));
        assertTrue(modelValidator.isValidDataType(modelValidator.URL_DATA_TYPE));
        assertTrue(modelValidator.isValidDataType(modelValidator.FILE_DATA_TYPE));
        assertFalse(modelValidator.isValidDataType("DOUBLE"));
    }

    @Test
    public void isValidLookupValueType() throws Exception {
        assertTrue(modelValidator.isValidLookupValueType(modelValidator.ATTRIBUTE_LV_TYPE_NAME));
        assertTrue(modelValidator.isValidLookupValueType(modelValidator.EVENT_STATUS_LV_TYPE_NAME));
        assertTrue(modelValidator.isValidLookupValueType(modelValidator.EVENT_TYPE_LV_TYPE_NAME));
        assertTrue(modelValidator.isValidLookupValueType(modelValidator.VIEW_GROUP_LV_TYPE_NAME));
        assertTrue(modelValidator.isValidLookupValueType(modelValidator.EDIT_GROUP_LV_TYPE_NAME));
        assertFalse(modelValidator.isValidLookupValueType("PROJECT_GROUP"));
    }

    @Test
    public void validateEventTemplateSanityException1() throws Exception {
        String eventName = "EventTest";
        String projectName = "ProjectTest";
        String sampleName = "SampleTest";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Error: there are no settable values for event " + eventName + " for project " + projectName +
                        ".  Please confirm the event may be applied to " + projectName + ".");

        modelValidator.validateEventTemplateSanity(new ArrayList<>(), projectName, sampleName, eventName);
    }

    @Test
    public void validateEventTemplateSanityException2() throws Exception {
        String eventName = "EventTest";
        String projectName = "ProjectTest";
        String sampleName = "SampleTest";

        List<EventMetaAttribute> eaList = new ArrayList<>();
        EventMetaAttribute ea = new EventMetaAttribute();

        eaList.addAll(Arrays.asList(ea));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(EXTRANEOUS_SAMPLE_ERR_MSG_FMT.format(new Object[]{eventName}));

        modelValidator.validateEventTemplateSanity(eaList, projectName, sampleName, eventName);
    }

    @Test
    public void validateEventTemplateSanityException3() throws Exception {
        String eventName = "EventTest";
        String projectName = "ProjectTest";
        String attributeName = "AttributeTest";

        List<EventMetaAttribute> eaList = new ArrayList<>();
        EventMetaAttribute ea = new EventMetaAttribute();
        ea.setSampleRequired(true);
        ea.setAttributeName(attributeName);

        eaList.addAll(Arrays.asList(ea));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage((NO_SAMPLE_ERR_MSG_FMT.format(new Object[]{eventName, attributeName})));

        modelValidator.validateEventTemplateSanity(eaList, projectName, null, eventName);
    }

    @Test
    public void validateSampleContents() throws Exception {
        Sample sample = new Sample();
        assertFalse(modelValidator.validateSampleContents(sample, new StringBuilder(), 0));

        sample.setSampleName("SampleTest");
        assertFalse(modelValidator.validateSampleContents(sample, new StringBuilder(), 0));

        sample.setProjectName("ProjectTest");
        assertTrue(modelValidator.validateSampleContents(sample, new StringBuilder(), 0));

        sample.setSampleName("");
        assertFalse(modelValidator.validateSampleContents(sample, new StringBuilder(), 0));
    }

    @Test
    public void validateProject() throws Exception {
        Project project = new Project();
        assertFalse(modelValidator.validateProject(project, null, new StringBuilder(), 0));

        project.setProjectName("ProjectTest");
        assertTrue(modelValidator.validateProject(project, null, new StringBuilder(), 0));

        project.setProjectLevel(2);
        assertFalse(modelValidator.validateProject(project, null, new StringBuilder(), 0));

        project.setParentProjectName("ProjectTest");
        assertTrue(modelValidator.validateProject(project, null, new StringBuilder(), 0));

        project.setParentProjectName("ParentTest");
        assertFalse(modelValidator.validateProject(project, null, new StringBuilder(), 0));
    }

    @Test
    public void setValueForAttributeException() throws Exception {
        String dataType = "DOUBLE";
        thrown.expect(Exception.class);
        thrown.expectMessage("invalid data type '" + dataType + "'");

        modelValidator.setValueForAttribute(null, null, null, null, dataType);
    }

    @Test
    public void getModelValue() throws Exception {
        String stringVal = "test";
        Integer intVal = 1;
        SimpleDateFormat chosenFormat = PST_ACCEPT_DATE_FORMAT;
        Date dateVal = chosenFormat.parse("2017-03-10");
        Double floatVal = 1.2;

        SampleAttribute sa = new SampleAttribute();
        LookupValue lv = new LookupValue();

        assertNull(modelValidator.getModelValue(null, null));

        lv.setDataType(modelValidator.DATE_DATA_TYPE);
        sa.setAttributeDateValue(dateVal);
        assertEquals(dateVal, modelValidator.getModelValue(lv, sa));

        lv.setDataType(modelValidator.FLOAT_DATA_TYPE);
        sa.setAttributeFloatValue(floatVal);
        assertEquals(floatVal, modelValidator.getModelValue(lv, sa));

        lv.setDataType(modelValidator.INT_DATA_TYPE);
        sa.setAttributeIntValue(intVal);
        assertEquals(intVal, modelValidator.getModelValue(lv, sa));

        sa.setAttributeStringValue(stringVal);
        lv.setDataType(modelValidator.STRING_DATA_TYPE);
        assertEquals(stringVal, modelValidator.getModelValue(lv, sa));

        lv.setDataType(modelValidator.URL_DATA_TYPE);
        assertEquals(stringVal, modelValidator.getModelValue(lv, sa));

        lv.setDataType(modelValidator.FILE_DATA_TYPE);
        assertEquals(stringVal, modelValidator.getModelValue(lv, sa));

    }

}