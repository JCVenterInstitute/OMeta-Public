package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.DetailedException;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.TemplatePreProcessingUtils;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Calendar;
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

    // template files
    private File dataTemplate;
    private InputStream dataTemplateStream;
    private String dataTemplateFileName;
    private String dataTemplateContentType;
    private String projectName;
    private String eventName;

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

    public String downloadProjectSetup() {
        try {
            Project currProject = readPersister.getProject(this.projectName);
            Long projectId = currProject.getProjectId();
            List<ProjectMetaAttribute> pmaList = readPersister.getProjectMetaAttributes(this.projectName);
            List<EventMetaAttribute> emaList = readPersister.getEventMetaAttributes(projectId);
            List<SampleMetaAttribute> smaList = readPersister.getSampleMetaAttributes(projectId);
            List<LookupValue> eventNameList = readPersister.getEventTypesForProject(projectId);

            TemplatePreProcessingUtils templateUtil = new TemplatePreProcessingUtils();

            this.dataTemplateStream = templateUtil.buildProjectSetupContent(currProject, pmaList, emaList, eventNameList, smaList);
            this.dataTemplateContentType = "application/octet-stream";
            this.dataTemplateFileName = this.projectName + "_Setup.csv";

            return Constants.STRUTS_FILE_DOWNLOAD;
        }  catch (Exception ex) {
            String rtnVal;

            StringBuilder error = new StringBuilder(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + ":");
            error.append(this.projectName + ":");
            error.append(this.eventName + ":");

            String errorMsg = "";
            if (ex.getClass() == ForbiddenResourceException.class) {
                errorMsg = Constants.DENIED_USER_EDIT_MESSAGE;
                rtnVal = Constants.FORBIDDEN_ACTION_RESPONSE;
            } else {
                errorMsg = (ex.getClass() == DetailedException.class ? ((DetailedException)ex).getRowIndex() + ":" : "") +
                        (ex.getCause() == null ? ex.getMessage() : ex.getCause());
                rtnVal = ERROR;
            }

            addActionError(errorMsg.replaceAll("\\\n", "<br/>"));
            error.append(errorMsg);
            logger.error(error.toString());

            return rtnVal;
        }
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

    public String getDataTemplateContentType() {
        return dataTemplateContentType;
    }

    public void setDataTemplateContentType(String dataTemplateContentType) {
        this.dataTemplateContentType = dataTemplateContentType;
    }

    public File getDataTemplate() {
        return dataTemplate;
    }

    public void setDataTemplate(File dataTemplate) {
        this.dataTemplate = dataTemplate;
    }

    public InputStream getDataTemplateStream() {
        return dataTemplateStream;
    }

    public void setDataTemplateStream(InputStream dataTemplateStream) {
        this.dataTemplateStream = dataTemplateStream;
    }

    public String getDataTemplateFileName() {
        return dataTemplateFileName;
    }

    public void setDataTemplateFileName(String dataTemplateFileName) {
        this.dataTemplateFileName = dataTemplateFileName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
