package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.action.ajax.IAjaxAction;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.Group;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.List;
import java.util.Properties;

/**
 * Created by mkuscuog on 2/18/2015.
 */
public class EditProject extends ActionSupport{
    private Logger logger = Logger.getLogger(EditProject.class);

    private ReadBeanPersister readPersister;
    ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;
    private List<Group> groupList;
    private Project project;

    private Long projectId;

    private String action;
    private String errorMsg;

    public EditProject() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String editProject() {
        String returnValue = NONE;
        UserTransaction tx = null;

        if(projectId != null){
            if(action != null && action.equals("update")) {
                try {
                    tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();

                    this.groupList = readPersister.getAllGroup();

                    if (psewt == null) {
                        UploadActionDelegate udelegate = new UploadActionDelegate();
                        psewt = udelegate.initializeBusinessObject(logger, psewt);
                    }

                    Project currProject = readPersister.getProject(projectId);

                    currProject.setEditGroup(this.project.getEditGroup());
                    currProject.setViewGroup(this.project.getViewGroup());
                    currProject.setIsPublic(this.project.getIsPublic());
                    currProject.setIsSecure(this.project.getIsSecure());

                    this.psewt.updateProject(currProject);

                    returnValue = SUCCESS;
                    this.setProject(currProject);
                    addActionMessage("Project has been updated.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                            tx.commit();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (action != null && action.equals("lookup")){
                try {
                    this.project = readPersister.getProject(projectId);
                    this.groupList = readPersister.getAllGroup();

                    returnValue = INPUT;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMsg = "You do not have permission to access the project, or the project does not exist.";
                } finally {
                    try {
                        if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                            tx.commit();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        return returnValue;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
