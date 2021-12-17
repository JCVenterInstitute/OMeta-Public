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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.model.web.MetadataSetupReadBean;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.utils.CommonTool;
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
 * Date: 10/31/11
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectSetup extends ActionSupport {
    private Logger logger = LogManager.getLogger(ProjectSetup.class);

    private ReadBeanPersister readPersister;
    private ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;
    private List<Group> groupList;

    private String jobType;
    private List<MetadataSetupReadBean> beanList;
    private Project loadingProject;

    public ProjectSetup() {
        readPersister = new ReadBeanPersister();
    }

    public ProjectSetup(ReadBeanPersister persister, ProjectSampleEventWritebackBusiness writeBean) {
        this.readPersister = persister;
        this.psewt = writeBean;
    }

    public String projectSetup() {
        List<String> projectNameList;
        String returnValue = ERROR;
        UserTransaction tx = null;

        try {
            projectNameList = new ArrayList<String>();
            projectNameList.add("ALL");
            projectList = readPersister.getProjects(projectNameList);

            groupList = readPersister.getAllGroup();

            if (jobType != null && jobType.equals("insert")) {
                tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                if(psewt==null) {
                    UploadActionDelegate udelegate = new UploadActionDelegate();
                    psewt = udelegate.initializeBusinessObject(logger, psewt);
                }

                //set project level by adding 1 to selected parent project's level
                Long parentProjectId = loadingProject.getParentProjectId();
                if (parentProjectId != null && parentProjectId != 0) {
                    Project selectedParentProject = readPersister.getProject(parentProjectId);
                    loadingProject.setProjectLevel(selectedParentProject.getProjectLevel() + 1);
                } else {
                    loadingProject.setParentProjectId(null);
                    loadingProject.setProjectLevel(1);
                }

                List<Project> projectList = new ArrayList<Project>();
                projectList.add(loadingProject);

                List<ProjectMetaAttribute> pmaList = null;
                List<EventMetaAttribute> emaList = null;
                List<FileReadAttributeBean> fbList = null;

                if (beanList != null && beanList.size() > 0) {
                    pmaList = new ArrayList<ProjectMetaAttribute>();
                    emaList = new ArrayList<EventMetaAttribute>();
                    fbList = new ArrayList<FileReadAttributeBean>();

                    for (MetadataSetupReadBean bean : beanList) {
                        if (bean.getName() != null && bean.getValue()!=null) {
                            LookupValue lv = readPersister.getLookupValue(bean.getName(), Constants.ATTRIBUTE_LV_TYPE_NAME);

                            ProjectMetaAttribute pma = new ProjectMetaAttribute();
                            pma.setProjectName(loadingProject.getProjectName());
                            pma.setDataType(lv.getDataType());
                            pma.setActiveDB(bean.getActiveDB());
                            pma.setDesc(bean.getDesc());
                            pma.setAttributeName(bean.getName());
                            pma.setNameLookupId(lv.getLookupValueId());
                            pma.setOptions(bean.getOptions());
                            pma.setRequiredDB(bean.getRequiredDB());
                            pmaList.add(pma);

                            EventMetaAttribute ema = CommonTool.createEMA(
                                    null, loadingProject.getProjectName(), Constants.EVENT_PROJECT_REGISTRATION, pma.getAttributeName(),
                                    pma.isRequired(), pma.isActive(), pma.getDataType(), pma.getDesc(),
                                    pma.getOntology(), pma.getLabel(), pma.getOptions(), false, 0);
                            emaList.add(ema);

                            if(bean.getValue()!=null && !bean.getValue().isEmpty()) {
                                FileReadAttributeBean fbean = new FileReadAttributeBean();
                                fbean.setAttributeName(pma.getAttributeName());
                                fbean.setAttributeValue(bean.getValue());
                                fbean.setProjectName(pma.getProjectName());
                                fbList.add(fbean);
                            } else {
                                if(bean.getRequiredDB().equals(1)) {
                                    throw new Exception(String.format("'%s' is a required field, but value is not provided.", bean.getName()));
                                }
                            }
                        }
                    }
                }

                MultiLoadParameter loadParameter = new MultiLoadParameter();
                loadParameter.setEventName(Constants.EVENT_PROJECT_REGISTRATION);
                loadParameter.addProjectPair(loadingProject, fbList, pmaList, null, emaList, 1);
                psewt.loadAll(null, loadParameter);

                addActionMessage("Project '" + loadingProject.getProjectName() + "' has been loaded successfully.");
            }
            returnValue = SUCCESS;

        } catch (Exception ex) {
            logger.error("Exception in ProjectSetup : " + ex.toString());
            ex.printStackTrace();
            if (ex.getClass() == ForbiddenResourceException.class) {
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
                return Constants.FORBIDDEN_ACTION_RESPONSE;
            /*} else if (ex.getClass() == ForbiddenResourceException.class) {
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
                return LOGIN;*/
            } else if (ex.getClass() == ParseException.class)
                addActionError(Constants.INVALID_DATE_MESSAGE);
            else
                addActionError(ex.toString());

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

        return returnValue;
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

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Project getLoadingProject() {
        return loadingProject;
    }

    public void setLoadingProject(Project loadingProject) {
        this.loadingProject = loadingProject;
    }

    public List<MetadataSetupReadBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<MetadataSetupReadBean> beanList) {
        this.beanList = beanList;
    }
}
