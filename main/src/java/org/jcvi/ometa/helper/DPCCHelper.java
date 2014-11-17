package org.jcvi.ometa.helper;

import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.DPCCValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * User: movence
 * Date: 10/14/14
 * Time: 3:17 PM
 * org.jcvi.ometa.helper
 */
public class DPCCHelper {
    private ReadBeanPersister readPersister;

    public DPCCHelper() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public DPCCHelper(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    public void updateSampleStatus(List<FileReadAttributeBean> loadingList, String projectName, String sampleName, String status, int index) throws Exception {
        boolean foundSampleStatus = false;
        String strStatus = status.equals("submit") ? Constants.DPCC_STATUS_SUBMITTED : status.equals("validate") ? Constants.DPCC_STATUS_VALIDATED : Constants.DPCC_STATUS_EDITING;

        try {
            Project project = this.readPersister.getProject(projectName);
            if(project == null) {
                throw new Exception("project does not exist.");
            }

            if(sampleName == null || sampleName.isEmpty()) {
                throw new Exception("sample is required.");
            }

            Sample sample = this.readPersister.getSample(project.getProjectId(), sampleName);
            if(sample != null) { //it could be sample registration event
                List<SampleAttribute> saList = this.readPersister.getSampleAttributes(sample.getSampleId());
                for(SampleAttribute sa : saList) {
                    if(sa.getMetaAttribute().getLookupValue().getName().equals(Constants.ATTR_SAMPLE_STATUS)) {
                        if(sa.getAttributeStringValue() != null && sa.getAttributeStringValue().equals("Data submitted to DPCC")) {
                            throw new Exception("You cannot load any events for a sample that has been submitted to DPCC.");
                        }
                    }
                }
            }

            for(FileReadAttributeBean fBean : loadingList) {
                if(fBean.getAttributeName().equals(Constants.ATTR_SAMPLE_STATUS)) {
                    fBean.setAttributeValue(strStatus);
                    foundSampleStatus = true;
                }
            }
            if(!foundSampleStatus) { //if sample status attribute is not in the list
                List<SampleMetaAttribute> smaList = this.readPersister.getSampleMetaAttributes(project.getProjectId());
                for(SampleMetaAttribute sma : smaList) { //check if sample status is in sample meta attributes
                    if(sma.getLookupValue().getName().equals(Constants.ATTR_SAMPLE_STATUS)) {
                        foundSampleStatus = true;
                    }
                }

                if(foundSampleStatus) { //manually add sample status with the status value
                    FileReadAttributeBean statusBean = new FileReadAttributeBean();
                    statusBean.setAttributeName(Constants.ATTR_SAMPLE_STATUS);
                    statusBean.setAttributeValue(strStatus);
                    statusBean.setProjectName(projectName);
                    statusBean.setSampleName(sampleName);
                    loadingList.add(statusBean);
                } else {
                    throw new Exception("'" + Constants.ATTR_SAMPLE_STATUS + "' attribute not found.");
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            DetailedException dex = new DetailedException(index, ex.getMessage());
            throw dex;
        }
    }

    public void validateDataForDPCC(List<FileReadAttributeBean> loadingList, int index) throws Exception {
        String attributeName = null;
        Collection errorMessages = new ArrayList();

        try {
            for(FileReadAttributeBean fBean : loadingList) {
                if(fBean.getAttributeName().toLowerCase().contains("Collection_Date")) {
                    attributeName = fBean.getAttributeName();
                    try{
                        DPCCValidator.validateDate(fBean.getAttributeValue());
                    }catch(Exception e){
                        errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                    }
                }
                if(fBean.getAttributeName().toLowerCase().contains("Receipt_Date")) {
                   attributeName = fBean.getAttributeName();
                   try{
                       DPCCValidator.validateDate(fBean.getAttributeValue());
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }

                if(fBean.getAttributeName().toLowerCase().contains("Influenza_Vaccination_Date")) {
                   attributeName = fBean.getAttributeName();

                   FileReadAttributeBean influenzaVaccinationType = DPCCHelper.findAttribute( "Influenza_Vaccination_Type",  loadingList);
                   if (influenzaVaccinationType == null){
                      throw new Exception("Attribute "+influenzaVaccinationType+" is required.");
                   }
                   if (influenzaVaccinationType.getAttributeValue().equalsIgnoreCase("none")) {
                       if (attributeName.equalsIgnoreCase("na")){
                           //it's ok'
                       }else{
                           attributeName = fBean.getAttributeName();
                           try{
                               DPCCValidator.validateDate(fBean.getAttributeValue());
                           }catch(Exception e){
                               errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                           }
                       }
                   }
                }
                if(fBean.getAttributeName().toLowerCase().contains("Test_for_Influenza_Serology")) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Test_for_Influenza_Serology","^[a-zA-Z0-9,]*$","Text","Serology_Test_Result","^[PNU,]*$","P/N/U",loadingList);
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }

                if(fBean.getAttributeName().toLowerCase().contains("Influenza_Test_Type")) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Influenza_Test_Type","^([a-zA-Z0-9]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Influenza_Test_Result","^(P|N|NT|NA|,)*$","P/N/NT/NA",loadingList);
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }

                if(fBean.getAttributeName().toLowerCase().contains("Other_Pathogens_Tested")) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validatePairs("Other_Pathogens_Tested","^([a-zA-Z0-9]*|NT|NA|,)*$","Text/NT(Not tested)/NA","Other_Pathogen_Test_Result","^(P|N|U|NT|NA|,)*$","P/N/U/NT/NA",loadingList);
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }
                if (Constants.DURATION_ATTRIBUTES.contains(fBean.getAttributeName().toLowerCase())) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[days|month|years])|NA|U|Unknown)*$","Number (2 days, 0.33 days) or NA or Unknown");
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }
                if (fBean.getAttributeName().toLowerCase().contains("Age")) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[years])|NA|U|Unknown)*$","Number (2 years, 0.33 years) or NA or Unknown");
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }
                if (fBean.getAttributeName().toLowerCase().contains("Vaccine_Dosage")) {
                   attributeName = fBean.getAttributeName();
                   try{
                       //DPCCValidator.validateSerologyTestAndResult(fBean, loadingList);
                       DPCCValidator.validateRegEx(attributeName,fBean.getAttributeValue(),"^(([0-9.]*[ ]*[mL])|NA|U|Unknown)*$","number (0.05 mL) or NA or Unknown");
                   }catch(Exception e){
                       errorMessages.add(attributeName+" data is invalid "+ e.getMessage());
                   }
                }

                //throw DetailedException with all error messages
                if (!errorMessages.isEmpty()) {
                    DetailedException dex = new DetailedException(index,errorMessages.toString());
                    throw dex;
                }
            }
        } catch(Exception ex) {
            DetailedException dex = new DetailedException(index, "DPCC validation Failed." + ex.getMessage()+ "'");
            throw dex;
        }
    }
    //@TODO Indresh can we change List to hash based on Attribute name so that lookup wil be easy
    public static FileReadAttributeBean findAttribute(String attributeValue,List<FileReadAttributeBean> loadingList) throws Exception {
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

