package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class Dashboard extends ActionSupport {
    private Logger logger = Logger.getLogger(Dashboard.class);

    private ReadBeanPersister readPersister;
    private int totalCount = 8;
    private List<Map<String, Object>> projectMapList;

    public Dashboard() {
        readPersister = new ReadBeanPersister();
    }

    public String process() {
        String returnValue = ERROR;

        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();
            List<Project> projectList = readPersister.getLastUpdatedProjects( null, totalCount, AccessLevel.View );

            List<ProjectAttribute> projectAttributeList = readPersister.getProjectAttributes(
                    projectList.stream()
                            .map(Project::getProjectId)
                            .collect(Collectors.toList())
            );

            projectMapList = new ArrayList<>(projectList.size());
            for(Project project : projectList) {
                Map<String, Object> projectMap = new LinkedHashMap<>(projectList.size());
                projectMap.put(Constants.ATTR_PROJECT_NAME, project.getProjectName());
                projectMap.put("ProjectId", project.getProjectId());

                List<String[]> sampleStatus = readPersister.getSampleStatusForProject(project.getProjectId());
                projectMap.put("sampleInfo", sampleStatus);

                if(sampleStatus != null && sampleStatus.size() > 0)
                    projectMapList.add(projectMap);
            }
            returnValue = SUCCESS;
        } catch ( ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch( LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch(Exception ex) {
            logger.error("Exception in Event Detail Action : " + ex.toString());
            ex.printStackTrace();
        }

        return returnValue;
    }

    public List<Map<String, Object>> getProjectMapList() {
        return projectMapList;
    }

    public void setProjectMapList(List<Map<String, Object>> projectMapList) {
        this.projectMapList = projectMapList;
    }
}
