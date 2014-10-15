package org.jcvi.ometa.helper;

import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.stateless_session_bean.DetailedException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.DPCCValidator;
import org.jtc.common.util.property.PropertyHelper;

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

        try {
            for(FileReadAttributeBean fBean : loadingList) {
                if(fBean.getAttributeName().toLowerCase().contains("date")) {
                    attributeName = fBean.getAttributeName();
                    DPCCValidator.validateDate(fBean.getAttributeValue());
                }
            }
        } catch(Exception ex) {
            DetailedException dex = new DetailedException(index, "date parse error: '" + attributeName + "'");
            throw dex;
        }
    }
}
