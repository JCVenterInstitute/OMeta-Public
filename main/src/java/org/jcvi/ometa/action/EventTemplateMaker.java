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
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.stateless_session_bean.LoginRequiredException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.TsvPreProcessingUtils;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/10/11
 * Time: 4:26 PM
 *
 * Struts2 action for dealing with creation of a user-fillable template defining an uploadable event file.
 */
public class EventTemplateMaker extends ActionSupport {
    protected static final String UNCHOSEN_MENU_ITEM_PREFIX = "--";
    private ReadBeanPersister readPersister;

    private String projectName;
    private String sampleName;
    private String eventName;
    private String message;
    private StringBuilder outputBuilder;

    private List<Project> projectList;
    private String projectNames;
    private List<String> projectNameList;

    private Long projectId;
    private Long sampleId;
    private Long eventId;

    private Logger logger = Logger.getLogger(EventTemplateMaker.class);

    /** Constructor preps for whole lifecycle. */
    public EventTemplateMaker() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister( props );
    }

    /** Constructor preps for unit testing. */
    public EventTemplateMaker( ReadBeanPersister readPersister ) {
        this.readPersister = readPersister;
    }

    /** Supports the file download. */
    public InputStream getInputStream() {
        if ( outputBuilder != null ) {
            return new ByteArrayInputStream( outputBuilder.toString().getBytes() );
        }
        else {
            return null;
        }
    }

    /** Setup a download filename to fully-indicate type of event.  See also: struts.xml */
    public String getDownloadFileName() {
        return eventName + "_EventAttributes.tsv";
    }

    public String execute() {
        message = null;

        String returnMessage = SUCCESS;
        // Here, create something for user to fill in.  Needs to be accurate to degree possible, and
        // have all info filled in that is possible.
        try {
            projectNameList = new ArrayList<String>();
            if( projectNames == null || projectNames.equals( "" ))
                projectNameList.add("ALL");
            else if( projectNames.contains(","))
                projectNameList.addAll( Arrays.asList(projectNames.split(",")) );
            else
                projectNameList.add( projectNames );

            String userName = ServletActionContext.getRequest().getRemoteUser();
            //projectList = readPersister.getAuthorizedProjects( userName, AccessLevel.View );
            projectList = readPersister.getProjects( projectNameList );

            if( projectName != null && eventName != null) {
                List<EventMetaAttribute> emaList = readPersister.getEventMetaAttributes( projectName, eventName );

                ModelValidator validator = new ModelValidator();
                validator.validateEventTemplateSanity(emaList, projectName, actualContent( sampleName ), eventName);
                outputBuilder = new StringBuilder();
                TsvPreProcessingUtils utils = new TsvPreProcessingUtils();
                utils.buildFileContent( emaList, projectName, sampleName, outputBuilder );

                // Cause the post-dispatcher to push the file stream back to user.
                returnMessage = Constants.FILE_DOWNLOAD_MSG;
            }
        } catch ( ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch( LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch ( Exception ex ) {
            logger.error( ex );
            message = "Failed to produce a template file." + ex.toString();
            addActionError( message );
            returnMessage = Constants.FAILURE_MSG;
        }
        return returnMessage;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    private String actualContent( String testStr ) {
        if ( testStr.startsWith( UNCHOSEN_MENU_ITEM_PREFIX ) ) {
            return "";
        }
        else {
            return testStr;
        }
    }
}
