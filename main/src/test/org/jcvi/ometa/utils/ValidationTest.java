package org.jcvi.ometa.utils;

import org.jcvi.ometa.validation.DPCCValidator;

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
		input.add("0.33 mL");
        input.add(".1 mL");
        input.add("0 mL");
        input.add("mL");
        input.add("NA");
        input.add("U");
        input.add("20");
        input.add("20 days");
        input.add("Unknown");
        //String p = "^(([-+]?[0-9]*.?[0-9]*[ ]+[mL])|NA|U|Unknown)*$";
        String p = "^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$";
        //number (0.05 mL) or NA or Unknown
		for (String ssn : input) {
			if (ssn.matches(p)) {
				System.out.println("Found " + ssn);
			}else{
               String [] g = ssn.split(" ");
               for(String t:g){

               }
//

                System.out.println("Not found " + ssn);
            }

		}
        String attributeName  = "duration_of_human_exposure";
        String attributeValue = "3 days";
        if (Constants.DURATION_ATTRIBUTES.contains(attributeName)) {
           try{
               //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
               DPCCValidator.validateRegEx(attributeName, attributeValue, "^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$", "Number (2 days, 0.33 days) or NA or Unknown");
               System.out.println("Validation is good");
           }catch(Exception e){
               System.out.println(attributeName+" data is invalid "+ e.getMessage());
           }
        }
	}
}