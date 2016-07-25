package org.jcvi.ometa.validation;

import org.apache.log4j.Logger;
import org.jcvi.ometa.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by mkuscuog on 7/24/2015.
 */
public class DataValidator {
    private Logger logger = Logger.getLogger(DataValidator.class);

    public boolean validateDate(String value) {
        if(value.equals("NA")) return true;

        boolean result = false;
        for(String dateFormat : Constants.DATE_ALL_ALLOWED_FORMATS){
            if(dateFormat.length() == value.length()) {
                try {
                    DateFormat sdf = new SimpleDateFormat(dateFormat);
                    sdf.setLenient(false);
                    sdf.parse(value);
                    result = true;
                    break;
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    public boolean validateMaxLength(String value, String maxLength) {
        return (value.length() <= Integer.parseInt(maxLength));
    }

    public boolean validateMinLength(String value, String minLength) {
        return (value.length() >= Integer.parseInt(minLength));
    }

    public boolean validateAllNumbers(String value) {
        if (value == null) {
            return false;
        }
        int sz = value.length();

        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(value.charAt(i)) == false) {
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
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}
