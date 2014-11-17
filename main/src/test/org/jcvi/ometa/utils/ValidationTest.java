package org.jcvi.ometa.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: isingh
 * Date: 11/16/14
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class ValidationTest {
	public static void main(String[] args) {
		List<String> input = new ArrayList<String>();
		input.add("2 days");
		input.add("0.33 days");
        input.add("NA");
        input.add("U");
        input.add("Unknown");
        String p = "^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$";

		for (String ssn : input) {
			if (ssn.matches(p)) {
				System.out.println("Found " + ssn);
			}else{
                System.out.println("Not found " + ssn);
            }

		}
	}
}