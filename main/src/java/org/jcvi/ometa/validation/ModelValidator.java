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

import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A nexus of validation methods, to test all manner of inputs for validity.  Here, the rules are enforced, and
 * this class exists to allow them to be done consistently.
 */
public class ModelValidator {

    public static final String ACCEPTABLE_CHARACTERS = "A-Za-z0-9=.,;:!?@$%#+()\\[\\]/\\-_'\" ";
    public static final String ACCEPTABLE_CHARACTERS_REGEXP = "[" + ACCEPTABLE_CHARACTERS + "]*";

    public static final String DATE_DATA_TYPE = "date";
    public static final String STRING_DATA_TYPE = "string";
    public static final String FLOAT_DATA_TYPE = "float";
    public static final String INT_DATA_TYPE = "int";
    public static final String URL_DATA_TYPE = "url";
    public static final String FILE_DATA_TYPE = "file";

    public static final String ATTRIBUTE_LV_TYPE_NAME = "Attribute";
    public static final String EVENT_STATUS_LV_TYPE_NAME = "Event Status";
    public static final String EVENT_TYPE_LV_TYPE_NAME = "Event Type";
    public static final String VIEW_GROUP_LV_TYPE_NAME = "Access Group";
    public static final String EDIT_GROUP_LV_TYPE_NAME = "Edit Group";

    public static final SimpleDateFormat PST_DEFAULT_DATE_FORMAT = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
    public static final SimpleDateFormat PST_DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT + " H:mm");

    private static final MessageFormat NO_SAMPLE_ERR_MSG_FMT =
            new MessageFormat("Event {0} requires a sample for event attribute(s) {1} but no sample was given.");
    private static final MessageFormat EXTRANEOUS_SAMPLE_ERR_MSG_FMT =
            new MessageFormat("Event {0} requires no sample, but sample was given.");

    private Set<String> validDataTypes;
    private Set<String> validLookupValueTypes;

    public Set<String> getValidDataTypes() { return validDataTypes; }
    public Set<String> getValidLookupValueTypes() { return validLookupValueTypes; }

    public ModelValidator() {
        validDataTypes = new HashSet<String>();
        validDataTypes.add(DATE_DATA_TYPE);
        validDataTypes.add(STRING_DATA_TYPE);
        validDataTypes.add(FLOAT_DATA_TYPE);
        validDataTypes.add(INT_DATA_TYPE);
        validDataTypes.add(URL_DATA_TYPE);
        validDataTypes.add(FILE_DATA_TYPE);

        validLookupValueTypes = new HashSet<String>();
        validLookupValueTypes.add(ATTRIBUTE_LV_TYPE_NAME);
        validLookupValueTypes.add(EVENT_STATUS_LV_TYPE_NAME);
        validLookupValueTypes.add(EVENT_TYPE_LV_TYPE_NAME);
        validLookupValueTypes.add(VIEW_GROUP_LV_TYPE_NAME);
        validLookupValueTypes.add(EDIT_GROUP_LV_TYPE_NAME);
    }

    /**
     * Check data type against known constants.
     */
    public boolean isValidDataType(String dataType) {
        return validDataTypes.contains(dataType);
    }

    public boolean isValidLookupValueType(String lookupValueType) {
        //  Bypass the rejection of old-style attribute types.
        if (lookupValueType.endsWith(" " + ATTRIBUTE_LV_TYPE_NAME)) {
            lookupValueType = ATTRIBUTE_LV_TYPE_NAME;
        }
        return validLookupValueTypes.contains(lookupValueType);
    }

    /**
     * Check: are these file-read inputs reasonable.
     *
     * @param projectNames   list of all sample names.  Add this one.
     * @param combinedErrors all errors encountered.
     * @param rowNum         for error messages.
     * @param bean           what to look at.
     */
    public void validateSampleMetaAttributeContents(
            SampleMetaAttribute bean, Set<String> projectNames, StringBuilder combinedErrors, int rowNum) {

        if (!projectNames.contains(bean.getProjectName())) {
            combinedErrors.append("Row number "+rowNum+" has a project name not in known project name list.\n");
        }

        // Check: all required to establish this metadata is given.
        if (bean.getAttributeName() == null || bean.getAttributeName().length() == 0) {
            combinedErrors.append("Row number " + rowNum + " has no attribute name.");
        }
    }

    /**
     * Ensures that it is reasonable to create an event of this type for the project given.
     *
     * @param emaList     meta attributes applying to project/event.
     * @param projectName which project
     * @param eventName   which event.
     */
    public void validateEventTemplateSanity(
            List<EventMetaAttribute> emaList,
            String projectName,
            String sampleName,
            String eventName) {

        if (emaList == null || emaList.size() == 0) {
            throw new IllegalArgumentException(
                    "Error: there are no settable values for event " + eventName + " for project " + projectName +
                            ".  Please confirm the event may be applied to " + projectName + ".");
        }

        StringBuilder listBuilder = new StringBuilder();
        boolean sampleRequired = false;
        for (EventMetaAttribute ema : emaList) {
            sampleRequired |= ema.isSampleRequired();
            if (ema.isSampleRequired()) {
                if (listBuilder.length() > 0)
                    listBuilder.append(",");
                listBuilder.append(ema.getAttributeName());
            }
        }

        boolean sampleGiven = sampleName != null && !sampleName.equals("0") && sampleName.trim().length() > 0;
        if (sampleRequired && (!sampleGiven) && !eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION)) {
            String msg = NO_SAMPLE_ERR_MSG_FMT.format(new Object[]{eventName, listBuilder.toString()});
            throw new IllegalArgumentException(msg);
        }
        if ((!sampleRequired) && sampleGiven) {
            String msg = EXTRANEOUS_SAMPLE_ERR_MSG_FMT.format(new Object[]{eventName});
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Validates a sample model object.
     *
     * @param bean           what to examine.
     * @param combinedErrors add errors to this.
     * @param rowNum         for error report.
     */
    public boolean validateSampleContents(Sample bean, StringBuilder combinedErrors, int rowNum) {
        String sampleName = bean.getSampleName();
        String projectName = bean.getProjectName();

        boolean rtnVal = true;

        if (sampleName == null || sampleName.trim().length() == 0) {
            combinedErrors.append("Sample name required but not given at row " + rowNum + "\n");
            rtnVal = false;
        }

        if (projectName == null || projectName.trim().length() == 0) {
            combinedErrors.append("Project name required but not given at row " + rowNum + "\n");
            rtnVal = false;
        }

        return rtnVal;
    }

    /**
     * Is this project reasonable?  Expects any parent project to have been previously added to list of names.
     *
     * @param projectNames   all project names: add this one.
     * @param combinedErrors all errors found.
     * @param rowNum         for error messages.
     * @param bean           Looking at this.
     */
    public boolean validateProject(Project bean, List<String> projectNames, StringBuilder combinedErrors, int rowNum) {
        boolean passed = true;
        // Check: required things are in?
        String projectName = bean.getProjectName();
        if (projectName == null || projectName.trim().length() == 0) {
            combinedErrors.append(
                    "Project name required but not given, at row ").append(rowNum).append("\n");
            passed = false;
        } else {
            projectNames.add(projectName);
        }

        // Check: if project Parent exits, is it already in the collection?
        String parentProject = bean.getParentProjectName();
        if (parentProject != null && parentProject.trim().length() > 0) {
            if (!projectNames.contains(parentProject)) {
                combinedErrors
                        .append("Project Parent Name " + parentProject + " not in known project list, at row ")
                        .append(rowNum).append("\n");
                passed = false;
            }
        } else {
            if (bean.getProjectLevel() > 1) {
                combinedErrors
                        .append("Project ")
                        .append(projectName)
                        .append(" has project level of ")
                        .append(bean.getProjectLevel())
                        .append(" but it has no parent project.")
                        .append("\n");
                passed = false;

            }
        }

        return passed;
    }

    /**
     * Look at meta-attribs for project: was the information read okay.
     *
     * @param bean           what was read
     * @param combinedErrors populate this with what we find.
     * @param rowNum         for error messages.
     */
    public boolean validateProjectMetaAttributeContents(
            ProjectMetaAttribute bean, StringBuilder combinedErrors, int rowNum) {

        boolean passed = true;

        // Check: all required to establish this metadata is given.
        if (bean.getAttributeName() == null || bean.getAttributeName().trim().length() == 0) {
            combinedErrors.append("Row number " + rowNum + " has no attribute name.");
            passed = false;
        }

        return passed;
    }

    /**
     * Attributes have types which are governed by meta attributes. Method determines the type and sets the value.
     */
    public void setValueForAttribute(
            StringBuilder errors,
            String colHeader,
            AttributeModelBean attribute,
            String value,
            String dataType)
            throws Exception {

        // This test should never fail, or it means loader has a flaw elsewhere, or someone is loading
        // project meta attributes outside this code.
        if (!isValidDataType(dataType)) {
            throw new Exception("invalid data type '" + dataType + "'");
        }

        if (dataType.equals(ModelValidator.FLOAT_DATA_TYPE)) {
            validateAndSetFloat(errors, colHeader, attribute, value);
        } else if (dataType.equals(ModelValidator.INT_DATA_TYPE)) {
            validateAndSetInteger(errors, colHeader, attribute, value);
        } else if (dataType.equals(ModelValidator.STRING_DATA_TYPE) || dataType.equals(ModelValidator.FILE_DATA_TYPE)) {
            validateAndSetString(errors, colHeader, attribute, value);
        } else if (dataType.equals(ModelValidator.URL_DATA_TYPE)) {
            validateAndSetUrl(errors, colHeader, attribute, value);
        } else if (dataType.equals(ModelValidator.DATE_DATA_TYPE)) {
            validateAndSetDate(errors, colHeader, attribute, value);
        }
    }

    private Date validateAndSetDate(StringBuilder errors, String sourceName, AttributeModelBean attribute, String value) {
        // Two tries to get the date/time right.
        SimpleDateFormat chosenFormat = PST_DEFAULT_DATE_FORMAT;//US_SLASHED_DATE_TIME_FMT;
        chosenFormat.setLenient(false);
        Date rtnDate = null;
        try {
            String trimmedValue = CommonTool.convertTimestampToDate(value.trim());
            rtnDate = chosenFormat.parse(trimmedValue);
            attribute.setAttributeDateValue(new java.sql.Date(rtnDate.getTime()));
        } catch (NullPointerException npe) {
            errors.append(sourceName + ":cannot be empty");
        } catch (ParseException pe) {
            errors.append(sourceName + ":date parse error:'" + value + "', use '" + chosenFormat.toPattern());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rtnDate;
    }

    private void validateAndSetFloat(StringBuilder errors, String sourceName, AttributeModelBean attribute, String value) {
        try {
            Double fValue = Double.parseDouble(value.trim());
            attribute.setAttributeFloatValue(fValue);
        } catch (NullPointerException npe) {
            errors.append(sourceName + ":cannot be empty");
        } catch (NumberFormatException nfe) {
            errors.append(sourceName + ":float parse error:'" + value + "'");
        }
    }

    private void validateAndSetString(StringBuilder errors, String sourceName, AttributeModelBean attribute, String value) {
        try {
            String testableValue = value.trim();
            if (testableValue.matches(ACCEPTABLE_CHARACTERS_REGEXP)) {
                attribute.setAttributeStringValue(value.trim());
            } else {
                errors.append("invalid character(s) in '" + testableValue + "', use " + ACCEPTABLE_CHARACTERS + "\n");
            }
        } catch (NullPointerException npe) {
            errors.append(sourceName + ":cannot be empty");
        }
    }

    private void validateAndSetUrl(StringBuilder errors, String sourceName, AttributeModelBean attribute, String value) {
        try {
            String trimmedValue = value.trim();
            URL url = new URL(trimmedValue);
            attribute.setAttributeStringValue(trimmedValue);
        } catch (NullPointerException npe) {
            errors.append(sourceName + ":cannot be empty");
        } catch (MalformedURLException npe) {
            errors.append(sourceName + ":invalid url:'" + value + "'");
        }
    }

    private void validateAndSetInteger(StringBuilder errors, String sourceName, AttributeModelBean attribute, String value) {
        try {
            Integer iValue = Integer.parseInt(value.isEmpty()||value.trim().isEmpty()?"0":value.trim());
            attribute.setAttributeIntValue(iValue);
        } catch (NullPointerException npe) {
            errors.append(sourceName + ":cannot be empty");
        } catch (NumberFormatException nfe) {
            errors.append(sourceName + ":int parse error: '" + value + "'");
        }
    }

    public static Object getModelValue(LookupValue lookupValue, AttributeModelBean model) {
        Object rtnValue = null;
        String dataType = lookupValue.getDataType();
        if(dataType.equals(DATE_DATA_TYPE)) {
            rtnValue = model.getAttributeDateValue();
        } else if(dataType.equals(INT_DATA_TYPE)) {
            rtnValue = model.getAttributeIntValue();
        } else if(dataType.equals(STRING_DATA_TYPE) || dataType.equals(FILE_DATA_TYPE)) {
            rtnValue = model.getAttributeStringValue();
        } else if (dataType.equals(URL_DATA_TYPE)) {
            rtnValue = model.getAttributeStringValue();
        } else {
            rtnValue = model.getAttributeFloatValue();
        }
        return rtnValue;
    }
}