package org.jcvi.ometa.validation;

import org.jcvi.ometa.helper.DPCCHelper;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.utils.Constants;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        for(String format : Constants.DPCC_DATE_ALL_POSSIBLE_FORMATS) {
            try {
                new SimpleDateFormat(format).parse(value);
                isValid = true;
                break parseLoop;
            } catch(ParseException e) { //ignore parse exception
            }
        }

        if(!isValid) {
            for(String noValue : Constants.DPCC_DATE_NO_VALUES) {
                if(noValue.toLowerCase().equals(value.toLowerCase())) {
                    isValid = true;
                    break;
                }
            }
        }

        if(!isValid) {
            throw new Exception("Invalid value '" + value + "'");
        }
    }

    //validation for Serology:Test_for_Influenza_Serology
    public static void validateSerologyTestAndResult(FileReadAttributeBean serologyTestBean,List<FileReadAttributeBean> loadingList) throws Exception {
        boolean isValid = false;

        String[] result = serologyTestBean.getAttributeValue().split(",");

        int testCount =  result.length;
        FileReadAttributeBean beanPair = DPCCHelper.findAttribute( "Serology_Test_Result",  loadingList);

        if (beanPair == null){
            throw new Exception("Attribute Serology_Test_Result is required.");
        }

        String[] resultPair = beanPair.getAttributeValue().split(",");
        int testCountPair = resultPair.length;

        if (testCountPair != testCount) {
            throw new Exception("Number of tests for "+serologyTestBean.getAttributeName()+" and test results for "+beanPair.getAttributeName()+" should be same.");
        }

        parseLoop:
        for(String testResult : resultPair) {
            try {
                Constants.serologyTestResult.valueOf(testResult);
            } catch(IllegalArgumentException  e) {
                throw new Exception("Invalid value '"+ testResult+"' . Allowed values are "+ Arrays.toString(Constants.serologyTestResult.values()));
            }
        }
    }

    public static void validatePairs(String attribute1, String attribute1Values, String attribute1Message
                                     ,String attribute2, String attribute2Values, String attribute2Message
                                     ,List<FileReadAttributeBean> loadingList
                                     )
    throws Exception{

        boolean isValid = false;

        FileReadAttributeBean beanAttribute1 = DPCCHelper.findAttribute( attribute1,  loadingList);
        if (beanAttribute1 == null){
           throw new Exception("Attribute "+attribute1+" is required.");
        }

        FileReadAttributeBean beanAttribute2 = DPCCHelper.findAttribute( attribute2,  loadingList);
        if (beanAttribute2 == null){
           throw new Exception("Attribute "+attribute2+" is required.");
        }

        String[] attributeResult1 = beanAttribute1.getAttributeValue().split(",");
        String[] attributeResult2 = beanAttribute2.getAttributeValue().split(",");

        if (attributeResult1.length != attributeResult2.length) {
            throw new Exception("Number of tests for "+beanAttribute1.getAttributeName()+" and test results for "+beanAttribute2.getAttributeName()+" should be same.");
        }

        if (!beanAttribute1.getAttributeValue().matches(attribute1Values)){
            throw new Exception("Invalid value '"+beanAttribute1.getAttributeValue()+"'. Allowed values are "+ attribute1Message);
        }
        if (!beanAttribute2.getAttributeValue().matches(attribute2Values)){
            throw new Exception("Invalid value '"+beanAttribute2.getAttributeValue()+"'. Allowed values are "+ attribute2Message);
        }

    }

    public static void validateRegEx(String attributeName, String attributeValue, String attribute1RegEx, String attribute1Message)
    throws Exception{

        if (!attributeValue.matches(attribute1RegEx)){
            throw new Exception( " Invalid data '"+attributeValue+"'. Allowed values are "+ attribute1Message);
        }

    }

    //@TODO isingh remove or move to unit test
    public static void main (String[] args){

        DPCCValidator validator = new DPCCValidator();
        List<FileReadAttributeBean> loadingList = new ArrayList();
        FileReadAttributeBean b1 = new FileReadAttributeBean();
        b1.setAttributeName("Serology_Test_Result");
        b1.setAttributeValue("U,P");
        loadingList.add(b1);

        FileReadAttributeBean b2 = new FileReadAttributeBean();
        b2.setAttributeName("Test_for_Influenza_Serology");
        b2.setAttributeValue("ESL,TSL");
        loadingList.add(b2);

        int i = 1;
        System.out.println("Test"+i);
        try{
            validator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9,]*$","Text/NA","Serology_Test_Result","^[PSU,]*$","P/S/U",loadingList);
        }catch(Exception e){
             System.out.println(e.getMessage());
        }


        //test 2

        validator = new DPCCValidator();
        loadingList = new ArrayList();
        b1 = new FileReadAttributeBean();
        b1.setAttributeName("Serology_Test_Result");
        b1.setAttributeValue(" ");
        loadingList.add(b1);

        b2 = new FileReadAttributeBean();
        b2.setAttributeName("Test_for_Influenza_Serology");
        b2.setAttributeValue("ESL");

        loadingList.add(b2);

        System.out.println("Test"+i++);
        try{
            validator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9,]*$","Text/NA","Serology_Test_Result","^[PSU,]*$","P/S/U",loadingList);
        }catch(Exception e){
             System.out.println(e.getMessage());
        }

        //test 2
        loadingList = new ArrayList();
//        FileReadAttributeBean b1 = new FileReadAttributeBean();
//        b1.setAttributeName("Serology_Test_Result");
//        b1.setAttributeValue(" ");
//        loadingList.add(b1);

        b2 = new FileReadAttributeBean();
        b2.setAttributeName("Test_for_Influenza_Serology");
        b2.setAttributeValue("ESL");

        loadingList.add(b2);
        System.out.println("Test"+i++);
        try{
            validator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9,]*$","Text/NA","Serology_Test_Result","^[PSU,]*$","P/S/U",loadingList);
        }catch(Exception e){
             System.out.println(e.getMessage());
        }

        //test 3
        loadingList = new ArrayList();
        b1 = new FileReadAttributeBean();
        b1.setAttributeName("Serology_Test_Result");
        b1.setAttributeValue("U,U");
        loadingList.add(b1);

        b2 = new FileReadAttributeBean();
        b2.setAttributeName("Test_for_Influenza_Serology");
        b2.setAttributeValue("ESL");

        loadingList.add(b2);
        System.out.println("Test"+i++);
        try{
            validator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9,]*$","Text/NA","Serology_Test_Result","^[PSU,]*$","P/S/U",loadingList);
        }catch(Exception e){
             System.out.println(e.getMessage());
        }

        //test 4
        loadingList = new ArrayList();
        b1 = new FileReadAttributeBean();
        b1.setAttributeName("Serology_Test_Result");
        b1.setAttributeValue("U,C");
        loadingList.add(b1);

        b2 = new FileReadAttributeBean();
        b2.setAttributeName("Test_for_Influenza_Serology");
        b2.setAttributeValue("ESL,T");

        loadingList.add(b2);
        System.out.println("Test"+i++);
        try{
            validator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9,]*$","Text/NA","Serology_Test_Result","^[PSU,]*$","P/S/U",loadingList);
        }catch(Exception e){
             System.out.println(e.getMessage());
        }
    }
}
