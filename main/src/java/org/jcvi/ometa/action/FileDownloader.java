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
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.ActorGroup;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/5/12
 * Time: 10:54 AM
 */
public class FileDownloader extends ActionSupport {
    private static Logger logger = Logger.getLogger(FileDownloader.class);

    private final String PROJECT_FILE_STORAGE;

    private String fp;

    private InputStream fileInputStream;
    private String fileType;
    private String fileName;

    private ReadBeanPersister readPersister;

    public FileDownloader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.PROJECT_FILE_STORAGE = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH) + File.separator + Constants.DIRECTORY_PROJECT;

        this.readPersister = new ReadBeanPersister(props);
    }

    public String download() {
        String rtnVal = ERROR;
        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();
            Actor actor = this.readPersister.getActorByUserName(userName);
            List<ActorGroup> actorGroups = this.readPersister.getActorGroup(actor.getLoginId());

            String currProjectId = this.fp.substring(0, this.fp.indexOf(File.separator));
            Project currProject = this.readPersister.getProject(Long.parseLong(currProjectId));
            Long projectViewGroup= currProject.getViewGroup();

            boolean hasAccess = false;

            for(ActorGroup actorGroup : actorGroups) {
                if(projectViewGroup.equals(actorGroup.getGroupId())) {
                    hasAccess = true;
                    break;
                }
            }

            if(hasAccess) {
                File file = new File(this.PROJECT_FILE_STORAGE + File.separator + this.fp);
                if(file.exists() && file.canRead()) {
                    this.fileInputStream = new FileInputStream(file);
                    this.fileName = file.getName();
                    this.fileType = "application/octet-stream";
                    rtnVal = SUCCESS;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rtnVal;
    }

    public String getFp() {
        return fp;
    }

    public void setFp(String fp) {
        this.fp = fp;
    }

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public void setFileInputStream(InputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
