package org.jcvi.ometa.utils;

import org.junit.Test;

import java.text.SimpleDateFormat;

/**
 * User: movence
 * Date: 9/25/14
 * Time: 1:01 PM
 * org.jcvi.ometa.utils
 */
public class CommonToolTest {
    @Test
    public void testParsingDate() throws Exception {
        String defaultFormat = "2014-09-25";
        String slash = "2014/09/25";
        String slash2 = "09/25/2014";
        String ddmmmyyyy = "25-Sep-2014";
        String mmmyyyy = "Sep-2014";
        String yyyy = "2014";
        String whole = "2014-09-25T12:23:20";

        //Assert.assertNotNull(CommonTool.convertTimestampToDate(yyyy));

        SimpleDateFormat dateFormatYYYY = new SimpleDateFormat("yyyy");
        System.out.println(dateFormatYYYY.parse(yyyy));
        SimpleDateFormat dateFormatMMMYYYY = new SimpleDateFormat("MMM-yyyy");
        System.out.println(dateFormatMMMYYYY.parse(mmmyyyy));
        SimpleDateFormat dateFormatDDMMMYYYY = new SimpleDateFormat("dd-MMM-yyyy");
        System.out.println(dateFormatDDMMMYYYY.parse(ddmmmyyyy));
        SimpleDateFormat dateFormatSlashYYYY = new SimpleDateFormat("MM/dd/yyyy");
        System.out.println(dateFormatSlashYYYY.parse(slash2));
        SimpleDateFormat dateFormatSlash = new SimpleDateFormat("yyyy/MM/dd");
        System.out.println(dateFormatSlash.parse(slash));
        SimpleDateFormat dateFormatWhole = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        System.out.println(dateFormatWhole.parse(whole));

    }
}
