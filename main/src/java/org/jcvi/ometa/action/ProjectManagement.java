package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.Group;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.List;
import java.util.Properties;

/**
 * Created by mkuscuog on 2/20/2015.
 */
public class ProjectManagement extends ActionSupport {
    private Logger logger = Logger.getLogger(EditProject.class);

    private ReadBeanPersister readPersister;

    private List<Project> projectList;
    private String projectNames;
    private List<Group> groupList;

    private String errorMsg;

    public ProjectManagement() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String manageProject() {
        String returnValue = NONE;

        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();
            this.projectList = readPersister.getAuthorizedProjects( userName, AccessLevel.View );
            this.groupList = readPersister.getAllGroup();
        } catch (ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch(LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch(Exception ex) {
            logger.error("Exception in Event Detail Action : " + ex.toString());
            ex.printStackTrace();
        }

        if(errorMsg != null) addActionError(errorMsg);

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

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
