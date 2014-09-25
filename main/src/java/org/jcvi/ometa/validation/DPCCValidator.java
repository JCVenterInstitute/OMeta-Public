package org.jcvi.ometa.validation;

import org.jcvi.ometa.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * User: movence
 * Date: 9/24/14
 * Time: 12:26 PM
 * org.jcvi.ometa.validation
 * Data QC login for DPCC CEIRS data submission
 */
public class DPCCValidator {

    public static void validateDate(String value) throws Exception {
        boolean isValid = false;

        parseLoop:
        for(String format : Constants.DATE_ALL_POSSIBLE_FORMATS) {
            try {
                new SimpleDateFormat(format).parse(value);
                isValid = true;
                break parseLoop;
            } catch(ParseException e) { //ignore parse exception
            }
        }

        if(!isValid) {
            for(String noValue : Constants.DATE_NO_VALUES) {
                if(noValue.toLowerCase().equals(value.toLowerCase())) {
                    isValid = true;
                    break;
                }
            }
        }

        if(!isValid) {
            throw new Exception("date parse error: '" + value + "'");
        }
    }
}
