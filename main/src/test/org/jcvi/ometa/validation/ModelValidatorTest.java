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

package org.jcvi.ometa.validation;

import org.jcvi.ometa.model.ProjectAttribute;
import org.jcvi.ometa.model.ProjectMetaAttribute;
import org.jcvi.ometa.model.Sample;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/18/11
 * Time: 5:06 PM
 *
 * Testing facility for model validator.
 */
public class ModelValidatorTest {
    private ModelValidator validator;
    private Class validatorClass;

    @Before
    public void setUp() {
        validator = new ModelValidator();
        validatorClass = validator.getClass();
    }

    @Test
    public void testValidateAndSetString() {
        StringBuilder errors = new StringBuilder();
        try {
            ProjectAttribute pa = new ProjectAttribute();
            checkAcceptableString(pa, errors, "Song Kran Elephant Show");
            checkAcceptableString(pa, errors, ModelValidator.ACCEPTABLE_CHARACTERS.replace("\\", "x"));
            checkAcceptableString(pa, errors, "\"Stuff\" at the front.");
            checkAcceptableString(pa, errors, "Every Good Boy Does Fine.  See-it: that's how to memorize \"the scale\"!");
            checkAcceptableString(pa, errors, "Do you require the $-amount, the %, or the email@address.com?");
            checkAcceptableString(pa, errors, "Reach me at #6. Write the code in C#.  Hit the # key.");
            checkAcceptableString(pa, errors, "https://www.sitedoesnotexist.org/WhatsThatSmell.jsp?projid=101017");

            checkFlawedString(pa, errors, "\u0000\u0008\uEFFE\u0001");
            checkFlawedString(pa, errors, "^");

        } catch ( Exception ex ) {
            Assert.fail( ex.getMessage() );
        }
        if ( errors.length() > 0 ) {
            Assert.fail( errors.toString() );
        }
    }

    @Test
    public void testValidateAndSetDate() {
        StringBuilder errors = new StringBuilder();
        try {
            ProjectAttribute pa = new ProjectAttribute();
            checkAcceptableDate(pa, errors, "2013-10-31");
            checkFlawedDate(pa, errors, "2003/01/2003 12:12:12");
            //BEFORE lenient set false  out.println("Important Note: \"2003/01/2003 12:12:12\" not being rejected.");
            checkFlawedDate(pa, errors, "30/04/2003 12:12:12");
            //BEFORE lenient set false  out.println("Important Note: \"30/04/2003 12:12:12\" not being rejected.");
            checkFlawedDate(pa, errors, "2003-04-30 12:12:12");
            checkFlawedDate(pa, errors, "Apr 30 2003 12:12pm");
            checkFlawedDate(pa, errors, "Gar 15 2195 12:71pm");

        } catch ( Exception ex ) {
            Assert.fail( ex.getMessage() );
        }
        if ( errors.length() > 0 ) {
            Assert.fail( errors.toString() );
        }
    }

    @Test
    public void testValidateAndSetInt() {
        StringBuilder errors = new StringBuilder();
        try {
            ProjectAttribute pa = new ProjectAttribute();
            checkIntValue(pa, errors, "3371833");
            checkIntValue(pa, errors, "-58");
            checkIntValue(pa, errors, "10113");

            checkFlawedInt(pa, errors, "-2.3-");
            checkFlawedInt(pa, errors, "^");

        } catch ( Exception ex ) {
            Assert.fail( ex.getMessage() );
        }
        if ( errors.length() > 0 ) {
            Assert.fail( errors.toString() );
        }
    }

    @Test
    public void testValidateAndSetFloat() {
        StringBuilder errors = new StringBuilder();
        try {
            ProjectAttribute pa = new ProjectAttribute();
            checkFloatValue(pa, errors, "3.03");
            checkFloatValue(pa, errors, "3.03E15");
            checkFloatValue(pa, errors, "107");

            checkFlawedFloat(pa, errors, "-2.3-");
            checkFlawedFloat(pa, errors, "^");

        } catch ( Exception ex ) {
            Assert.fail( ex.getMessage() );
        }
        if ( errors.length() > 0 ) {
            Assert.fail( errors.toString() );
        }
    }

    @Test
    public void testStrangeDataType() {
        if ( validator.isValidDataType( "RecurvedLizardTeeth" ) ) {
            Assert.fail( "Failed to reject really strange data type." );
        }
    }

    @Test
    public void testStrangeLVType() {
        if ( validator.isValidLookupValueType( "RecurvedLizardTeeth" ) ) {
            Assert.fail( "Failed to reject really strange lookup value type." );
        }
    }

    @Test
    public void testSampleShakedown() {
        Sample s = new Sample();
        if ( validator.validateSampleContents( s, new StringBuilder(), 1 ) ) {
            Assert.fail( "Failed to reject completely empty sample object." );
        }
    }

    @Test
    public void testShakedownMetaAttrib() {
        ProjectMetaAttribute pma = new ProjectMetaAttribute();
        if ( validator.validateProjectMetaAttributeContents( pma, new StringBuilder(), 1 ) ) {
            Assert.fail( "Failed to reject completely empty Project Meta Attribute" );
        }
    }

    private void checkAcceptableString(ProjectAttribute pa, StringBuilder errors, String value) throws Exception {
        out.println("TEST: checking " + value );
        Method method = this.getPrivateMethod("validateAndSetString");
        method.invoke(errors, "JUnit Test", pa, value);
    }

    private void checkFlawedString(ProjectAttribute pa, StringBuilder errors, String flawedValue) throws Exception {
        out.println("Attempting to reject " + flawedValue);
        Method method = this.getPrivateMethod("validateAndSetString");
        method.invoke(errors, "JUnit Test", pa, flawedValue);
    }

    private void checkAcceptableDate(ProjectAttribute pa, StringBuilder errors, String value) throws Exception {
        out.println("TEST: checking as date " + value );
        Method method = this.getPrivateMethod("validateAndSetDate");
        Date parsedDate = (Date)method.invoke(errors, "JUnit Test", pa, value);
        if ( errors.length() > 0 ) {
            err.println(value + " failed to parse.");
        }
        else {
            out.println(value + " parsed as " + parsedDate.toString());
        }
    }

    private void checkFlawedDate(ProjectAttribute pa, StringBuilder errors, String flawedValue) throws Exception {
        out.println("Attempting to reject " + flawedValue);
        Method method = this.getPrivateMethod("validateAndSetDate");
        method.invoke(errors, "JUnit Test", pa, flawedValue);
    }

    private void checkIntValue(ProjectAttribute pa, StringBuilder errors, String value) throws Exception {
        out.println("TEST: checking as integer " + value );
        Method method = this.getPrivateMethod("validateAndSetInteger");
        method.invoke(errors, "JUnit Test", pa, value);
    }

    private void checkFlawedInt(ProjectAttribute pa, StringBuilder errors, String flawedValue) throws Exception {
        out.println("Attempting to reject " + flawedValue);
        Method method = this.getPrivateMethod("validateAndSetInteger");
        method.invoke(errors, "JUnit Test", pa, flawedValue);
    }

    private void checkFloatValue(ProjectAttribute pa, StringBuilder errors, String value) throws Exception {
        out.println("TEST: checking as float " + value );
        Method method = this.getPrivateMethod("validateAndSetFloat");
        method.invoke(errors, "JUnit Test", pa, value);
    }

    private void checkFlawedFloat(ProjectAttribute pa, StringBuilder errors, String flawedValue) throws Exception {
        out.println("Attempting to reject " + flawedValue);
        Method method = this.getPrivateMethod("validateAndSetFloat");
        method.invoke(errors, "JUnit Test", pa, flawedValue);
    }

    private Method getPrivateMethod(String name) throws NoSuchMethodException {
        Method method = validatorClass.getDeclaredMethod(name);
        method.setAccessible(true);
        return method;
    }
}
