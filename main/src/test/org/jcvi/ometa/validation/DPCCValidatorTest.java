package org.jcvi.ometa.validation;

import org.jcvi.ometa.utils.Constants;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * User: movence
 * Date: 1/14/15
 * Time: 3:04 PM
 * org.jcvi.ometa.validation
 */
public class DPCCValidatorTest {
    DPCCValidator dpccValidator = new DPCCValidator();

    @Test
    public void main() throws Exception {
        //this.date();


        String value = "abc\\dcs";

        String testableValue = value.trim().replace("\\\\", "\\\\\\\\");
        String acceptableRegexp = "[" + Constants.ACCEPTABLE_CHARACTERS + "]*";
        //if(!Pattern.matches("[A-Za-z0-9 _\\-=,:;<>()\\[\\]/\\\\]*", testableValue)) {


        if (!testableValue.matches(acceptableRegexp)) {
            fail();
        } else {
            System.out.println("invalid character(s) in '" + testableValue + "', use " + Constants.ACCEPTABLE_CHARACTERS.replace("\\", "") + "\n");
        }
    }

    private void date() throws Exception {
        String[] dates = {
                "30-Nov-2011",
                "Nov-2014",
                "2014",
                "Unknown",
                "Missing"
        };

        for(String date : dates) {
            DPCCValidator.validateDateDPCC(date);
            DPCCValidator.validateDate(date);

            System.out.println(date);
        }

        DPCCValidator.validateFutureDate("16-Jan-2015");
    }
}
