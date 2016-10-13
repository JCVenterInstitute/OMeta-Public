package org.jcvi.ometa.validation;

import org.apache.log4j.Logger;
import org.jcvi.ometa.model.SampleAttribute;
import org.jcvi.ometa.utils.Constants;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mkuscuog on 7/24/2015.
 */
public class DataValidator{
    private Logger logger = Logger.getLogger(DataValidator.class);
    protected final MessageFormat DATE_NOT_VALID_MSG = new MessageFormat(
            "Date validation failed! Please use one of the format below: \n {0}" );
    protected final MessageFormat MAX_LENGTH_ERROR_MSG = new MessageFormat(
            "Max Length validation failed! Value's length should be less than{0}" );
    protected final MessageFormat MIN_LENGTH_ERROR_MSG = new MessageFormat(
            "Min Length validation failed! Value's length should be more than {0}" );
    protected final String NUMBER_ERROR_MSG = "Number validation failed! Value should contain only number!";
    protected final String LETTER_ERROR_MSG = "Letter validation failed! Value should contain only letter!";
    protected final String UPPERCASE_ERROR_MSG = "Uppercase validation failed! Value should contain only uppercase letter";
    protected final String LOWERCASE_ERROR_MSG = "Lowercase validation failed! Value should contain only lowercase letter";
    private String errorMessage;

    public boolean validateDate(String value) {
        if(value.equals("NA")) return true;

        boolean valid = false;
        for(String dateFormat : Constants.DATE_ALL_ALLOWED_FORMATS){
            if(dateFormat.length() == value.length()) {
                try {
                    DateFormat sdf = new SimpleDateFormat(dateFormat);
                    sdf.setLenient(false);
                    sdf.parse(value);
                    valid = true;
                    break;
                } catch (Exception e) {
                }
            }
        }

        if(!valid) this.errorMessage = DATE_NOT_VALID_MSG.format(
                new Object[]{Arrays.toString(Constants.DATE_ALL_ALLOWED_FORMATS)});

        return valid;
    }

    public boolean validateMaxLength(String value, String maxLength) {
        if (value.length() <= Integer.parseInt(maxLength)) {
            return true;
        } else {
            this.errorMessage = MAX_LENGTH_ERROR_MSG.format(
                    new Object[]{maxLength});
            return false;
        }
    }

    public boolean validateMinLength(String value, String minLength) {
        if (value.length() >= Integer.parseInt(minLength)){
            return true;
        } else {
            this.errorMessage = MIN_LENGTH_ERROR_MSG.format(
                    new Object[]{minLength});
            return false;
        }
    }

    public boolean validateAllNumbers(String value) {
        if (value == null) {
            return false;
        }
        int sz = value.length();

        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(value.charAt(i)) == false) {
                this.errorMessage = NUMBER_ERROR_MSG;
                return false;
            }
        }
        return true;
    }

    public boolean validateAllLetters(String value) {
        if (value == null) {
            return false;
        }

        int sz = value.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isLetter(value.charAt(i)) == false) {
                this.errorMessage = LETTER_ERROR_MSG;
                return false;
            }
        }
        return true;
    }

    public boolean validateAllLowerCase(String value) {
        if (value == null || isEmpty(value)) {
            return false;
        }

        int sz = value.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isLowerCase(value.charAt(i)) == false) {
                this.errorMessage = LOWERCASE_ERROR_MSG;
                return false;
            }
        }
        return true;
    }

    public boolean validateAllUpperCase(String value) {
        if (value == null || isEmpty(value)) {
            return false;
        }

        int sz = value.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isUpperCase(value.charAt(i)) == false) {
                this.errorMessage = UPPERCASE_ERROR_MSG;
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean checkFieldUniqueness(String value, String dataType, Long currentSampleId, List<SampleAttribute> sampleAttributeList) {
        for(SampleAttribute sampleAttribute : sampleAttributeList){
            if(sampleAttribute.getSampleId().compareTo(currentSampleId) != 0) {
                String attrValue = (dataType.equals("string")) ? sampleAttribute.getAttributeStringValue()
                        : (dataType.equals("int")) ? ((sampleAttribute.getAttributeIntValue() != null ) ? sampleAttribute.getAttributeIntValue().toString() : null)
                        : (dataType.equals("date")) ? ((sampleAttribute.getAttributeDateValue() != null) ? sampleAttribute.getAttributeDateValue().toString() : null)
                        : ((sampleAttribute.getAttributeFloatValue() != null) ? sampleAttribute.getAttributeFloatValue().toString() : null);

                if (attrValue != null && attrValue.equals(value)) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getMessage() {
        return this.errorMessage;
    }
}
