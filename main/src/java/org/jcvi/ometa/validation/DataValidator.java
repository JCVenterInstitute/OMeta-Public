package org.jcvi.ometa.validation;

import org.apache.log4j.Logger;
import org.jcvi.ometa.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by mkuscuog on 7/24/2015.
 */
public class DataValidator {
    private Logger logger = Logger.getLogger(DataValidator.class);

    public boolean validateDate(String value) {
        try {
            new SimpleDateFormat(Constants.DATE_USER_ENTER_FORMAT).parse(value);
            return true;
        } catch(ParseException e) {
            return false;
        }
    }

    public boolean validateMaxLength(String value, String maxLength) {
        return (value.length() < Integer.parseInt(maxLength));
    }

    public boolean validateMinLength(String value, String minLength) {
        return (value.length() > Integer.parseInt(minLength));
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
