package org.jcvi.ometa.utils;

import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.helper.EventLoadHelper;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.validation.DPCCValidator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
//		input.add("2 days");
//        input.add("2 years");
//        input.add("40");
//		input.add("0.33 mL");
//        input.add(".1 mL");
//        input.add("0 mL");
//        input.add("mL");
//        input.add("NA");
//        input.add("U");
//        input.add("20");
//        input.add("20 days");
//        input.add("Unknown");
//        input.add("Unknown-<=>_) ( ");
//        input.add("Unknown-");
//        input.add("Unknown<>=_()-");
//        input.add("(HA)-(NA),");
//        input.add("P");
//        input.add("P,N ") ;
//        input.add("P, N ");
        //String p = "^(([0-9]*[\\s]*mL)|NA|U|Unknown)$";
        //String pattern = "^((\\d*\\.?\\d*[\\s]*mL))$";
       // ^(P|N|NT|NA|,|)*$"^[a-zA-Z0-9><=_\\-(),\\s ]*$";
        String pattern = "^(P|N|NT|NA|,\\s)*$";
        Boolean isNumber = false;
        String otherValue = "^(NA|U|Unknown)$";
        String message = "Value should be number mL, NA, U or Unknown";

        try{
            input.add("3 ml");
            input.add("3 mg");
            input.add(".5 mg");
            input.add("3.5 mg");
            input.add("ml");
            for (String ssn : input) {
                try{
                    Boolean ret = ValidationTest.validateNumberPattern(ssn,"^((\\d*\\.*\\d*[\\s]*(ml|mg)))$",Boolean.TRUE,"^(NA|U|Unknown)*$",message);
                    if (ret){
                       System.out.println("*Found "+ssn);
                    }else{
                        System.out.println("*Not Found "+ssn);
                    }
                }catch(Exception e){
                    System.out.println("Exception"+e.getMessage()+"  not found:"+ssn);
                }
          	}
        }catch(Exception e){
            System.out.println("Exception"+e.getMessage());
        }

//        try{
//            List<FileReadAttributeBean> loadingList = new ArrayList<FileReadAttributeBean>();
//            FileReadAttributeBean a = new FileReadAttributeBean();
//            a.setAttributeName("Date_of_Illness_Onset");
//            a.setAttributeValue("11-111-1972");
//            loadingList.add(a);
//            int i=1;
//            Boolean ret=false;
//            //ValidationTest.validateDataForDPCC(loadingList,i,"SampleUpdate for Human Surveillance");
//            try{      ValidationTest.validateDate("11-111-1972");  }catch(Exception e){  System.out.println("Exception"+e.getMessage());   }
//            try{ ValidationTest.validateDate("111-1972");   }catch(Exception e){  System.out.println("Exception"+e.getMessage());   }
//            try{ ValidationTest.validateDate("1972");    }catch(Exception e){  System.out.println("Exception"+e.getMessage());   }
//            try{ ValidationTest.validateDate("Jan-1972");  }catch(Exception e){  System.out.println("Exception"+e.getMessage());   }
//            try{ ValidationTest.validateDate("01-Jan-1972");  }catch(Exception e){  System.out.println("Exception"+e.getMessage());   }
//            try{ ValidationTest.validateDate("0-Jan-1972");  }catch(Exception e){  System.out.println("Exception"+e.getMessage());   }
//           System.out.println("ret"+ret);
//        }catch(Exception e){
//           System.out.println("Exception"+e.getMessage());
//        }
//
//        for (String ssn : input) {
//            try{
//               Boolean b = ValidationTest.validateNumberPattern(ssn,pattern,isNumber,otherValue,message);
//               if (b == true){
//                   System.out.println("*Found "+ssn);
//                }else{
//                   System.out.println("*Not Found "+ssn);
//               }
//            }catch(Exception e){
//                System.out.println("Exception"+e.getMessage());
//            }
//
//
//      	}
//
//        //number (0.05 mL) or NA or Unknown
//        String attributeName  = "duration_of_human_exposure";
//        String attributeValue = "3 days";
//        if (Constants.DURATION_ATTRIBUTES.contains(attributeName)) {
//           try{
//               //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
//               DPCCValidator.validateRegEx(attributeName, attributeValue, "^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$", "Number (2 days, 0.33 days) or NA or Unknown");
//               System.out.println("Validation is good");
//           }catch(Exception e){
//               System.out.println(attributeName+" data is invalid "+ e.getMessage());
//         }
//        }
	}
   public static Boolean validateNumberPattern(String value, String pattern,Boolean isNumber,String otherPattern, String message) throws Exception{

      Boolean isValid = false;
      try{
          if (value.matches(pattern)) {
            System.out.println("Found " + value);
            isValid=true;
            if (isNumber) {
                 String [] g = value.split(" ");
                 if (g.length > 0){
                   try{
                      float f = Float.parseFloat(g[0].trim());
                      if (f == 0 ){
                         //System.out.println("   Invalid data");
                         isValid=false;
                      }
                   }catch(Exception e){
                      //System.out.println("   Invalid data");
                      isValid=false;
                   }
                 }else{
                    isValid=false;
                 }
            }
          }else if (value.matches(otherPattern)) {
             //System.out.println("Found " + ssn);
             isValid=true;
          }else{
             //System.out.println("Not found " + ssn);
             isValid=false;
          }
      }catch(Exception e){
        isValid=false;
      }
      return isValid;
   }

    private static void validateDataForDPCC(List<FileReadAttributeBean> loadingList, int index,String eventName) throws Exception {
        String attributeName = null;
        Collection errorMessages = new ArrayList();
        try {
            for(FileReadAttributeBean fBean : loadingList) {
                if(fBean.getAttributeName().toLowerCase().contains("Collection_Date".toLowerCase())) {
                    attributeName = fBean.getAttributeName();
                    try{
                        DPCCValidator.validateDate(fBean.getAttributeValue());
                    }catch(Exception e){
                        errorMessages.add(attributeName+" is invalid. "+ e.getMessage());
                    }
                }
                if(fBean.getAttributeName().toLowerCase().contains("Receipt_Date".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       DPCCValidator.validateDate(fBean.getAttributeValue());
                   }catch(Exception e){
                       errorMessages.add(attributeName+" is invalid. "+ e.getMessage());
                   }
                }
                if(fBean.getAttributeName().toLowerCase().contains("Influenza_Vaccination_Date".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       DPCCValidator.validateDate(fBean.getAttributeValue());
                   }catch(Exception e){
                       errorMessages.add(attributeName+" is invalid. "+ e.getMessage());
                   }
                }
                if(fBean.getAttributeName().toLowerCase().contains("Test_for_Influenza_Serology".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9><=_\\-(),\\s ]*$","Text with space and comma","Serology_Test_Result","^[PNU,\\s]*$","P/N/U",loadingList);
                   }catch(Exception e){
                       errorMessages.add(e.getMessage());
                   }
                }

                if(fBean.getAttributeName().toLowerCase().contains("Influenza_Test_Type".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Influenza_Test_Type","^([a-zA-Z0-9><=_\\-(),\\s ]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Influenza_Test_Result","^(P|N|NT|NA|,|\\s)*$","P/N/NT/NA",loadingList);
                   }catch(Exception e){
                       errorMessages.add(e.getMessage());
                   }
                }

                if(fBean.getAttributeName().toLowerCase().contains("Other_Pathogens_Tested".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Other_Pathogens_Tested","^([a-zA-Z0-9><=_\\-(),\\s ]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Other_Pathogen_Test_Result","^(P|N|U|NT|NA|,|\\s)*$","P/N/U/NT/NA",loadingList);
                   }catch(Exception e){
                       errorMessages.add(e.getMessage());
                   }
                }
                if(fBean.getAttributeName().toLowerCase().contains("Duration_of_Poultry_Exposure".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Other_Pathogens_Tested","^([a-zA-Z0-9><=_\\-(),\\s ]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Other_Pathogen_Test_Result","^(P|N|U|NT|NA|,|\\s)*$","P/N/U/NT/NA",loadingList);
                   }catch(Exception e){
                       errorMessages.add(e.getMessage());
                   }
                }
                if (Constants.DURATION_ATTRIBUTES.contains(fBean.getAttributeName().toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$","Number (2 days, 0.33 days) or NA or Unknown");
                   }catch(Exception e){
                       errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ e.getMessage());
                   }
                }
                if (eventName.contains("Human Surveillance")){
                    if (fBean.getAttributeName().toLowerCase().contains("Age".toLowerCase())) {
                       attributeName = fBean.getAttributeName();
                       String message="Number (2 years, 0.33 years) or NA or U or Unknown";
                       try{
                           //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                           //DPCCValidator.validateNumberPattern(String value, String pattern,Boolean isNumber,String otherPattern, String message)
                           Boolean ret = DPCCValidator.validateNumberPattern(fBean.getAttributeValue(),"^((\\d*\\.?\\d*[\\s]*years))$",Boolean.TRUE,"^(NA|U|Unknown)*$","Number (2 years, 0.33 years) or NA or U or Unknown");
                           //DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[years])|NA|U|Unknown)*$","Number (2 years, 0.33 years) or NA or U or Unknown");
                           if (!ret){
                             errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                           }

                       }catch(Exception e){
                           errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                       }
                    }
                }
                if (fBean.getAttributeName().toLowerCase().contains("Vaccine_Dosage".toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   String message="Number (2 years, 0.33 years) or NA or U or Unknown";
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       Boolean ret = DPCCValidator.validateNumberPattern(attributeName,"^((\\d*\\.?\\d*[\\s]*years))$",Boolean.TRUE,"^(NA|U|Unknown)*$",message);
                       //DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[mL])|NA|U|Unknown)*$","number (0.05 mL) or NA or Unknown");
                       if (!ret){
                         errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                       }
                   }catch(Exception e){
                       errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                   }
                }

                //"^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$"
                //throw DetailedException with all error messages
                if (!errorMessages.isEmpty()) {
                    DetailedException dex = new DetailedException(index,errorMessages.toString().replaceAll("^(\\[)|(\\])$", ""));
                    throw dex;
                }
            }
        }
        catch (DetailedException e){
            throw e;
        }
        catch(Exception ex) {
            DetailedException dex = new DetailedException(index, "DPCC validation Failed." + ex.getMessage()+ "'");
            throw dex;
        }
    }
    public static void validateDate(String value) throws Exception {
        boolean isValid = false;

        if ( value.matches("^([0-9]{1,2})-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}-([0-9]{4})|Unknown|Not applicable|Missing$")) {
            isValid=true;
        }
        if ( value.matches("^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec){1}-([0-9]{4})|Unknown|Not applicable|Missing$")) {
            isValid=true;
        }
        if ( value.matches("^([0-9]{4})|Unknown|Not applicable|Missing$")) {
            isValid=true;
        }
        if(!isValid) {
            throw new Exception("Invalid value '" + value + "'");
        }

//        parseLoop:
//        for(String format : Constants.DPCC_DATE_ALL_POSSIBLE_FORMATS) {
//            try {
//                Object date = new SimpleDateFormat(format).parse(value);
//                isValid = true;
//                break parseLoop;
//            } catch(ParseException e) { //ignore parse exception
//            }
//        }
//
//        if(!isValid) {
//            for(String noValue : Constants.DPCC_DATE_NO_VALUES) {
//                if(noValue.toLowerCase().equals(value.toLowerCase())) {
//                    isValid = true;
//                    break;
//                }
//            }
//        }
//
//        if(!isValid) {
//            throw new Exception("Invalid value '" + value + "'");
//        }
    }

}