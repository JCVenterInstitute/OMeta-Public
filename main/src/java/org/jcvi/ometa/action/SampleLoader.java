/*
 * Copyright J. Craig Venter Institute, 2013
 *
 * The creation of this program was supported by J. Craig Venter Institute
 * and National Institute for Allergy and Infectious Diseases (NIAID),
 * Contract number HHSN272200900007C.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.model.SampleMetaAttribute;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/10/12
 * Time: 8:43 AM
 */
public class SampleLoader extends ActionSupport {
    private ReadBeanPersister readPersister;
    private ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;
    private List<String> projectNameList;
    private List<FileReadAttributeBean> beanList;

    private String label;
    private String jobType;
    private Sample loadingSample;

    private Logger logger = Logger.getLogger(EventLoader.class);

    public SampleLoader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String execute() {
        String returnMessage = ERROR;
        UserTransaction tx = null;

        try {
            projectNameList = new ArrayList<String>();
            projectNameList.add("ALL");
            projectList = readPersister.getProjects(projectNameList);

            if (jobType != null && jobType.equals("insert")) { //load sample
                tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                UploadActionDelegate udelegate = new UploadActionDelegate();
                psewt = udelegate.initializeBusinessObject(logger, psewt);

                String projectName = readPersister.getProject(loadingSample.getProjectId()).getProjectName();
                loadingSample.setProjectName(projectName);

                //set project level by adding 1 to selected parent project's level
                Long parentSampleId = loadingSample.getParentSampleId();
                if (parentSampleId != null && parentSampleId != 0) {
                    Sample selectedParentSample = readPersister.getSample(parentSampleId);
                    loadingSample.setSampleLevel(selectedParentSample.getSampleLevel() + 1);
                } else {
                    loadingSample.setParentSampleId(null);
                    loadingSample.setSampleLevel(1);
                }

                List<Sample> sampleList = new ArrayList<Sample>();
                sampleList.add(loadingSample);

                List<SampleMetaAttribute> smaList = readPersister.getSampleMetaAttributes(loadingSample.getProjectId());
                //create projectMetaAttribute hashmap for quick lookup
                HashMap<String, SampleMetaAttribute> nameToMetaAttributeMap = new HashMap<String, SampleMetaAttribute>();
                for (SampleMetaAttribute sma : smaList) {
                    nameToMetaAttributeMap.put(sma.getAttributeName(), sma);
                }

                List<FileReadAttributeBean> loadedBeanList = new ArrayList<FileReadAttributeBean>();
                if (beanList != null && beanList.size() > 0) {
                    for (FileReadAttributeBean sBean : beanList) {
                        if (!sBean.getAttributeName().equals("0") && sBean.getAttributeValue() != null && !sBean.getAttributeValue().isEmpty()
                                && nameToMetaAttributeMap.containsKey(sBean.getAttributeName())) {
                            sBean.setProjectName(projectName);
                            sBean.setSampleName(loadingSample.getSampleName());
                            loadedBeanList.add(sBean);
                        }
                    }
                }

                MultiLoadParameter loadParameter = new MultiLoadParameter();
                loadParameter.addSamples(sampleList);
                if (loadedBeanList.size() > 0)
                    loadParameter.addSampleRegistrations(Constants.EVENT_SAMPLE_REGISTRATION, loadedBeanList);
                psewt.loadAll(null, loadParameter);
            }

            beanList = null;
            loadingSample = null;
            jobType = null;

            returnMessage = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in SampleLoader : " + ex.toString());
            ex.printStackTrace();
            if (ex.getClass() == ForbiddenResourceException.class) {
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
                return Constants.FORBIDDEN_ACTION_RESPONSE;
            } else if (ex.getClass() == ForbiddenResourceException.class) {
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
                return LOGIN;
            } else if (ex.getClass() == ParseException.class)
                addActionError(Constants.INVALID_DATE_MESSAGE);
            else
                addActionError(ex.getMessage());

            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError(se.toString());
            }
        } finally {
            try {
                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                    tx.commit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return returnMessage;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public String getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(String projectNames) {
        this.projectNames = projectNames;
    }

    public List<String> getProjectNameList() {
        return projectNameList;
    }

    public void setProjectNameList(List<String> projectNameList) {
        this.projectNameList = projectNameList;
    }

    public List<FileReadAttributeBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<FileReadAttributeBean> beanList) {
        this.beanList = beanList;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Sample getLoadingSample() {
        return loadingSample;
    }

    public void setLoadingSample(Sample loadingSample) {
        this.loadingSample = loadingSample;
    }
}
