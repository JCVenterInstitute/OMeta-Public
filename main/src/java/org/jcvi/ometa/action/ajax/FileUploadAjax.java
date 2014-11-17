package org.jcvi.ometa.action.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.text.SimpleDateFormat;
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

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String storagePathDate = sdf.format(date);

                String storagePath = "users" + File.separator + userName + File.separator + storagePathDate;

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
}
