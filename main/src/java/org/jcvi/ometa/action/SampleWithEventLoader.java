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
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/4/12
 * Time: 8:43 AM
 */
public class SampleWithEventLoader  extends ActionSupport {
    private ReadBeanPersister readPersister;
    private ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;
    private List<String> projectNameList;
    private List<FileReadAttributeBean> beanList;

    private String label;
    private String jobType;
    private Sample loadingSample;

    private String eventName;

    private Long projectId;
    private Long sampleId;
    private Long eventId;

    private Logger logger = Logger.getLogger(EventLoader.class);

    public SampleWithEventLoader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister( props );
    }

    public String execute() {
        String returnMessage = ERROR;

        UserTransaction tx = null;
        try {
            projectNameList = new ArrayList<String>();
            projectNameList.add( "ALL" );
            projectList = readPersister.getProjects( projectNameList );

            if ( jobType != null && jobType.equals( "insert" ) ) { //load sample
                tx=(UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                UploadActionDelegate udelegate = new UploadActionDelegate();
                psewt = udelegate.initializeBusinessObject( logger, psewt );

                String projectName = readPersister.getProject(loadingSample.getProjectId()).getProjectName();
                loadingSample.setProjectName(projectName);

                //set project level by adding 1 to selected parent project's level
                Long parentSampleId = loadingSample.getParentSampleId();
                if( parentSampleId != null && parentSampleId != 0 ) {
                    Sample selectedParentSample = readPersister.getSample(parentSampleId);
                    loadingSample.setSampleLevel(selectedParentSample.getSampleLevel() + 1);
                } else {
                    loadingSample.setParentSampleId(null);
                    loadingSample.setSampleLevel(1);
                }

                List<Sample> sampleList = new ArrayList<Sample>();
                sampleList.add(loadingSample);

                String sampleName = loadingSample.getSampleName();
                if( beanList != null && beanList.size() > 0) {
                    for(FileReadAttributeBean readBean : beanList) {
                        if( readBean.getAttributeName().toLowerCase().contains("date") )
                            ModelValidator.PST_DEFAULT_DATE_FORMAT.parse(readBean.getAttributeValue());
                        readBean.setSampleName(sampleName);
                    }
                }

                MultiLoadParameter loadParameter = new MultiLoadParameter();
                loadParameter.addSamples(sampleList);
                if(beanList.size()>0)
                    loadParameter.addEvents(eventName, beanList);
                psewt.loadAll( null, loadParameter );

                beanList = null;
                projectId = null;
                sampleId = null;
                eventId = null;
            }

            beanList=null;
            loadingSample=null;
            jobType=null;

            returnMessage = SUCCESS;
        } catch(Exception ex) {
            logger.error("Exception in SampleWithEventLoader : " + ex.toString());
            ex.printStackTrace();
            if( ex.getClass() == ForbiddenResourceException.class ) {
                addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
                return Constants.FORBIDDEN_ACTION_RESPONSE;
            } else if( ex.getClass() == ForbiddenResourceException.class ) {
                addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
                return LOGIN;
            } else if( ex.getClass() == ParseException.class )
                addActionError( Constants.INVALID_DATE_MESSAGE );
            else
                addActionError( ex.toString() );

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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
