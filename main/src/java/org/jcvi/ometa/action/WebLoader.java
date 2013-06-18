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
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.configuration.EventLoader;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 4/27/11
 * Time: 1:44 PM
 * This action will allow upload of a file suitable for use by the Loading Engine.
 */
public class WebLoader extends ActionSupport {

    private static final String DEFAULT_USER_MESSAGE = "Not yet entered";

    private String message = DEFAULT_USER_MESSAGE;
    private File uploadFile;
    private String uploadContentType;
    private String uploadFileName;
    private String projectName;
    private String eventName;

    private Long projectId;
    private Long eventId;

    private List<Project> projectList;
    private String projectNames;
    private List<String> projectNameList;

    // Not working.  Leaving this in comment anticipating future releases of JBoss/Struts2. --LLF
    //@EJB(mappedName="PWS.ProjectSampleEventTracker")
    private ProjectSampleEventWritebackBusiness pset;

    private ReadBeanPersister readPersister;

    private Logger logger = Logger.getLogger(WebLoader.class);

    /**
     * This overload is for Unit testing.
     */
    public WebLoader(ProjectSampleEventWritebackBusiness pset) {
        this.pset = pset;
    }

    public WebLoader() {
        UploadActionDelegate delegate = new UploadActionDelegate();
        pset = delegate.initializeBusinessObject(logger, pset);

        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String execute() {
        String returnMessage = SUCCESS;

        try {

            projectNameList = new ArrayList<String>();
            if (projectNames == null || projectNames.equals(""))
                projectNameList.add("ALL");
            else if (projectNames.contains(","))
                projectNameList.addAll(Arrays.asList(projectNames.split(",")));
            else
                projectNameList.add(projectNames);

            String userName = ServletActionContext.getRequest().getRemoteUser();
            projectList = readPersister.getProjects(projectNameList);

            if (uploadFile != null) {
                if (!uploadFile.canRead()) {
                    addActionError("File could not be read.");
                    returnMessage = Constants.FILE_FAILURE_MSG;
                } else {
                    boolean successful = false;

                    // Need the internal "File" version because that's what is handed off after multipart content
                    // for upload.  Need absolute path because that's what web method requires.
                    try {
                        EventLoader loader = new EventLoader();
                        List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans(uploadFile);
                        int beanCount = pset.loadAttributes(attributeBeans, getEventName());
                        successful = (beanCount == attributeBeans.size());
                        if (!successful) {
                            setMessage(getUnknownErrorMessage());
                        } else {
                            setMessage("Event in file " + uploadFileName + " has been loaded.");
                        }
                    } catch (Throwable ex) {
                        // Message to be displayed should be coming through the EJB.
                        setMessage(ex.getMessage());
                        logger.error(message + "; " + ex.getMessage());
                        ex.printStackTrace();
                        successful = false;
                    }

                    if (!successful) {
                        if (getMessage() == null) {
                            setMessage(getUnknownErrorMessage());
                        }
                        addActionError(message);
                        returnMessage = Constants.FAILURE_MSG;
                    }

                    // Cleanup this resource.
                    uploadFile.delete();
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in Web Loader Action : " + ex.toString());
            ex.printStackTrace();
            if (ex.getClass() == ForbiddenResourceException.class)
                addActionError(Constants.DENIED_USER_EDIT_MESSAGE);
            else if (ex.getClass() == ParseException.class)
                addActionError(Constants.INVALID_DATE_MESSAGE);
            else
                addActionError(ex.toString());
            returnMessage = Constants.FAILURE_MSG;
        }

        return returnMessage;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setUploadFile(String uploadFileName) {
        uploadFile = new File(uploadFileName);
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public String getUploadFileContentType() {
        return uploadContentType;
    }

    public void setUploadFileContentType(String uploadContentType) {
        this.uploadContentType = uploadContentType;
    }

    public String getUploadFileFileName() {
        return uploadFileName;
    }

    public void setUploadFileFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    //-------------------------------------------HELPER METHODS
    private String getUnknownErrorMessage() {
        return "Unknown error uploading file " + getUploadFileFileName();
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
