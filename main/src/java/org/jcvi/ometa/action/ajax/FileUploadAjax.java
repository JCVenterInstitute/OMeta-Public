package org.jcvi.ometa.action.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.util.*;

/**
 * User: movence
 * Date: 11/16/14
 * Time: 11:50 AM
 * org.jcvi.ometa.action.ajax
 */
public class FileUploadAjax extends ActionSupport implements IAjaxAction {

    private File upload;
    private String uploadFileName;
    private String uploadContentType;

    private File[] temporaryFiles;
    private String[] temporaryFilesFileName;
    private String[] temporaryFilesContentType;

    private String fileStoragePath;

    private Map<String, Object> result;

    private Logger logger = Logger.getLogger(FileUploadAjax.class);

    public FileUploadAjax() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.fileStoragePath = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH);
    }

    @Override
    public String runAjax() {
        String returnCode = SUCCESS;
        try {
            if(this.upload != null && this.upload.canRead() && this.upload.isFile() && this.uploadFileName != null) {
                String userName = ServletActionContext.getRequest().getRemoteUser();

                String storagePath = Constants.DIRECTORY_USER_BULK + File.separator + userName + File.separator + CommonTool.currentDateToDefaultFormat();

                File storingDirectory = new File(this.fileStoragePath + File.separator + storagePath);
                storingDirectory.mkdirs();

                Long guid = CommonTool.getGuid();

                String destFileName = guid + "_" + this.uploadFileName;
                File destFile = new File(storingDirectory.getAbsolutePath() + File.separator + destFileName);

                FileUtils.copyFile(this.upload, destFile);

                result = new HashMap<String, Object>(1);
                List<Map<String, String>> files = new ArrayList<Map<String, String>>(1);
                Map<String, String> fileInfo = new HashMap<String, String>();
                fileInfo.put("name", uploadFileName);
                fileInfo.put("size", Long.toString(this.upload.length()));
                fileInfo.put("path", storagePath);
                fileInfo.put("user", userName);
                fileInfo.put("mod_fileName", destFileName);
                files.add(fileInfo);

                result.put("files", files);
            }
        } catch (Exception ex) {

            returnCode = ERROR;
        }
        return returnCode;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String temporaryFileUpload() {
        String returnCode = SUCCESS;
        String currTime = CommonTool.currentDateToDefaultFormat();
        try {
            result = new HashMap<String, Object>(this.temporaryFiles.length);
            String userName = ServletActionContext.getRequest().getRemoteUser();
            String currWorkDir = System.getProperty("user.dir");

            for(int i = 0; i < this.temporaryFiles.length; i++) {
                File temporaryFile = this.temporaryFiles[i];
                String temporaryFileName = this.temporaryFilesFileName[i];
                String storagePath = Constants.DIRECTORY_USER_BULK + File.separator + userName + File.separator + currTime;

                if (temporaryFile != null && temporaryFile.canRead() && temporaryFile.isFile() && temporaryFileName != null) {

                    File storingDirectory = new File(currWorkDir + File.separator + "scratch" + File.separator + storagePath);
                    storingDirectory.mkdirs();

                    Long guid = CommonTool.getGuid();

                    String destFileName = guid + "_" + temporaryFileName;
                    File destFile = new File(storingDirectory.getAbsolutePath() + File.separator + destFileName);

                    FileUtils.copyFile(temporaryFile, destFile);

                    result.put(temporaryFileName, destFile.getAbsolutePath());
                }
            }
        } catch (Exception ex) {

            returnCode = ERROR;
        }
        return returnCode;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getUpload() {
        return upload;
    }

    public void setUpload(File upload) {
        this.upload = upload;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getUploadContentType() {
        return uploadContentType;
    }

    public void setUploadContentType(String uploadContentType) {
        this.uploadContentType = uploadContentType;
    }

    public Object getResult() {
        return result;
    }

    public File[] getTemporaryFiles() {
        return temporaryFiles;
    }

    public void setTemporaryFiles(File[] temporaryFiles) {
        this.temporaryFiles = temporaryFiles;
    }

    public String[] getTemporaryFilesFileName() {
        return temporaryFilesFileName;
    }

    public void setTemporaryFilesFileName(String[] temporaryFilesFileName) {
        this.temporaryFilesFileName = temporaryFilesFileName;
    }

    public String[] getTemporaryFilesContentType() {
        return temporaryFilesContentType;
    }

    public void setTemporaryFilesContentType(String[] temporaryFilesContentType) {
        this.temporaryFilesContentType = temporaryFilesContentType;
    }
}
