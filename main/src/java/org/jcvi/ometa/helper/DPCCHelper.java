package org.jcvi.ometa.helper;

import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.DPCCValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: movence
 * Date: 10/14/14
 * Time: 3:17 PM
 * org.jcvi.ometa.helper
 */
public class DPCCHelper {
    public DPCCHelper() {
        /*Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.readPersister = new ReadBeanPersister(props);*/
    }


    public void validateDataForDPCC(List<FileReadAttributeBean> loadingList, int index,String eventName) throws Exception {
        Collection errorMessages = new ArrayList();

        try {
            for(FileReadAttributeBean fBean : loadingList) {
                String attributeName = fBean.getAttributeName();
                String lowerAttributeName = attributeName.toLowerCase();

                if(lowerAttributeName.contains("Collection_Date".toLowerCase())
                        || lowerAttributeName.contains("Receipt_Date".toLowerCase())
                        || lowerAttributeName.contains("Date_of_Illness_Onset".toLowerCase())
                        || lowerAttributeName.contains("Influenza_Vaccination_Date".toLowerCase())
                        ) {
                    try{
                        //DPCCValidator.validateDateDPCC(fBean.getAttributeValue());
                        DPCCValidator.validateDate(fBean.getAttributeValue());
                    } catch(Exception e){
                        errorMessages.add(attributeName+" is invalid. "+ e.getMessage());
                    }
                    continue;
                }

                if(attributeName.toLowerCase().contains("Embargo_End_Date".toLowerCase())) {
                    try{
                        DPCCValidator.validateFutureDate(fBean.getAttributeValue());
                    } catch(Exception e){
                        errorMessages.add(attributeName+" is invalid. "+ e.getMessage());
                    }
                    continue;
                }

                if(attributeName.toLowerCase().contains("Test_for_Influenza_Serology".toLowerCase())) {
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        DPCCValidator.validatePairs("Test_for_Influenza_Serology","^[" + Constants.ACCEPTABLE_CHARACTERS + "]*$","Text with space and comma","Serology_Test_Result","^[PNU,\\s]*$","P/N/U",loadingList);
                    } catch(Exception e){
                        errorMessages.add(e.getMessage());
                    }
                    continue;
                }

                if(attributeName.toLowerCase().contains("Influenza_Test_Type".toLowerCase())) {
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        DPCCValidator.validatePairs("Influenza_Test_Type","^([" + Constants.ACCEPTABLE_CHARACTERS + "]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Influenza_Test_Result","^(P|N|NT|NA|,|\\s)*$","P/N/NT/NA",loadingList);
                    } catch(Exception e){
                        errorMessages.add(e.getMessage());
                    }
                    continue;
                }

                if(attributeName.toLowerCase().contains("Other_Pathogens_Tested".toLowerCase())) {
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        DPCCValidator.validatePairs("Other_Pathogens_Tested","^([" + Constants.ACCEPTABLE_CHARACTERS + "]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Other_Pathogen_Test_Result","^(P|N|U|NT|NA|,|\\s)*$","P/N/U/NT/NA",loadingList);
                    } catch(Exception e){
                        errorMessages.add(e.getMessage());
                    }
                    continue;
                }
                if(attributeName.toLowerCase().contains("Duration_of_Poultry_Exposure".toLowerCase())) {
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        DPCCValidator.validatePairs("Other_Pathogens_Tested","^([" + Constants.ACCEPTABLE_CHARACTERS + "]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Other_Pathogen_Test_Result","^(P|N|U|NT|NA|,|\\s)*$","P/N/U/NT/NA",loadingList);
                    } catch(Exception e){
                        errorMessages.add(e.getMessage());
                    }
                    continue;
                }
                if (Constants.DURATION_ATTRIBUTES.contains(attributeName.toLowerCase())) {
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$","Number (2 days, 0.33 days) or NA or Unknown");
                    } catch(Exception e){
                        errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ e.getMessage());
                    }
                    continue;
                }
                if (eventName.contains("Human Surveillance")){
                    if (attributeName.toLowerCase().contains("Age".toLowerCase())) {
                        String message="Number (2 years, 0.33 years) or NA or U or Unknown";
                        try{
                            //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                            //DPCCValidator.validateNumberPattern(String value, String pattern,Boolean isNumber,String otherPattern, String message)
                            Boolean ret = DPCCValidator.validateNumberPattern(fBean.getAttributeValue(),"^((\\d*\\.?\\d*[\\s]*years))$",Boolean.TRUE,"^(NA|U|Unknown)*$","Number (2 years, 0.33 years) or NA or U or Unknown");
                            //DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[years])|NA|U|Unknown)*$","Number (2 years, 0.33 years) or NA or U or Unknown");
                            if (!ret){
                                errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                            }

                        } catch(Exception e){
                            errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                        }
                        continue;
                    }
                }
                if (attributeName.toLowerCase().contains("Vaccine_Dosage".toLowerCase())) {
                    String message="number (0.05 mL) or NA or Unknown";
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        Boolean ret = DPCCValidator.validateNumberPattern(fBean.getAttributeValue(),"^((\\d*\\.?\\d*[\\s]*mL))$",Boolean.TRUE,"^(NA|U|Unknown)*$",message);
                        //DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[mL])|NA|U|Unknown)*$","number (0.05 mL) or NA or Unknown");
                        if (!ret){
                            errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                        }
                    } catch(Exception e){
                        errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                    }
                    continue;
                }

                if (attributeName.toLowerCase().contains("Treatment_Dosage".toLowerCase())) {
                    String message="(0.05 mg or ml) or NA or Unknown";
                    try{
                        //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                        Boolean ret = DPCCValidator.validateNumberPattern(fBean.getAttributeValue(),"^((\\d*\\.*\\d*[\\s]*(ml|mg)))$",Boolean.TRUE,"^(NA|U|Unknown)*$",message);
                        //DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[mL])|NA|U|Unknown)*$","number (0.05 mL) or NA or Unknown");
                        if (!ret){
                            errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                        }
                    } catch(Exception e){
                        errorMessages.add(attributeName+": data is invalid. Allowed values are: "+ message);
                    }

                    continue;
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
    //@TODO Indresh can we change List to hash based on Attribute name so that lookup wil be easy


    public static FileReadAttributeBean findAttribute(String attributeValue, List<FileReadAttributeBean> loadingList) throws Exception {
        FileReadAttributeBean bean = null;

        loop:
        for(FileReadAttributeBean fBean : loadingList) {
            if(fBean.getAttributeName().toLowerCase().contains(attributeValue.toLowerCase())) {
                //index = loadingList.indexOf(fBean);
                bean = fBean;
                break loop;
            }
        }
        return bean;
    }


}

