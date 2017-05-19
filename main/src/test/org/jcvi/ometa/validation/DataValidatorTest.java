package org.jcvi.ometa.validation;

import org.jcvi.ometa.model.SampleAttribute;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by mkuscuog on 3/10/2017.
 */
public class DataValidatorTest {
    static DataValidator dataValidator;

    @BeforeClass
    public static void setUpBeforeAll() throws Exception {
        dataValidator = new DataValidator();
    }

    /* Allowed formats;
         * "yyyy",
         * "yyyy-MM-dd",
         * "yyyy-MMM-DD",
         * "yyyy-MMM"
         *  "NA"
         * @throws Exception
        */
    @Test
    public void validateDate() throws Exception {
        assertTrue(dataValidator.validateDate("2017"));
        assertTrue(dataValidator.validateDate("2017-03-10"));
        assertTrue(dataValidator.validateDate("2017-Mar-10"));
        assertTrue(dataValidator.validateDate("2017-Mar"));
        assertTrue(dataValidator.validateDate("NA"));

        assertFalse(dataValidator.validateDate("TEXT"));
        assertFalse(dataValidator.validateDate("03-12-2017"));
    }

    @Test
    public void validateMaxLength() throws Exception {
        String maxLength = "5";
        assertTrue(dataValidator.validateMaxLength("test", maxLength));
        assertFalse(dataValidator.validateMaxLength("validate test", maxLength));
    }

    @Test
    public void validateMinLength() throws Exception {
        String minLength = "5";
        assertTrue(dataValidator.validateMinLength("validate test", minLength));
        assertFalse(dataValidator.validateMinLength("test", minLength));
    }

    @Test
    public void validateAllNumbers() throws Exception {
        assertTrue(dataValidator.validateAllNumbers("2017"));
        assertFalse(dataValidator.validateAllNumbers("TEST"));
    }

    @Test
    public void validateAllLetters() throws Exception {
        assertTrue(dataValidator.validateAllLetters("TEST"));
        assertFalse(dataValidator.validateAllLetters("2017"));
    }

    @Test
    public void validateAllLowerCase() throws Exception {
        assertTrue(dataValidator.validateAllLowerCase("test"));
        assertFalse(dataValidator.validateAllLowerCase("TEST"));
    }

    @Test
    public void validateAllUpperCase() throws Exception {
        assertTrue(dataValidator.validateAllUpperCase("TEST"));
        assertFalse(dataValidator.validateAllUpperCase("test"));
    }

    @Test
    public void isEmpty() throws Exception {
        assertTrue(dataValidator.isEmpty(null));
        assertTrue(dataValidator.isEmpty(""));
        assertFalse(dataValidator.isEmpty("test"));
    }

    @Test
    public void checkFieldUniqueness() throws Exception {
        List<SampleAttribute> saList = new ArrayList<>();
        String stringDataType = "string", intDataType = "int", dateDataType = "date", floatDataType = "d";

        SampleAttribute stringSA = new SampleAttribute();
        stringSA.setSampleId(1L);
        stringSA.setAttributeStringValue("test");

        SampleAttribute intSA = new SampleAttribute();
        intSA.setSampleId(2L);
        intSA.setAttributeIntValue(2017);

        SampleAttribute dateSA = new SampleAttribute();
        dateSA.setSampleId(3L);
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateSA.setAttributeDateValue(sdf.parse("2017-03-10"));

        SampleAttribute floatAS = new SampleAttribute();
        floatAS.setSampleId(4L);
        floatAS.setAttributeFloatValue(2.017);

        saList.addAll(Arrays.asList(stringSA, intSA, dateSA, floatAS));

        assertTrue(dataValidator.checkFieldUniqueness("test1", stringDataType, 0L, saList));
        assertFalse(dataValidator.checkFieldUniqueness("test", stringDataType, 0L, saList));

        assertTrue(dataValidator.checkFieldUniqueness("2018", intDataType, 0L, saList));
        assertFalse(dataValidator.checkFieldUniqueness("2017", intDataType, 0L, saList));

        assertTrue(dataValidator.checkFieldUniqueness(sdf.parse("2018-03-10").toString(), dateDataType, 0L, saList));
        assertFalse(dataValidator.checkFieldUniqueness(sdf.parse("2017-03-10").toString(), dateDataType, 0L, saList));

        assertTrue(dataValidator.checkFieldUniqueness("2.018", floatDataType, 0L, saList));
        assertFalse(dataValidator.checkFieldUniqueness("2.017", floatDataType, 0L, saList));
    }

    @Test
    public void getMessage() throws Exception {
        dataValidator.validateAllUpperCase("test");

        assertEquals(dataValidator.UPPERCASE_ERROR_MSG, dataValidator.getMessage());
    }

}