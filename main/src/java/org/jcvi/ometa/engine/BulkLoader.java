package org.jcvi.ometa.engine;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.EmailSender;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * User: movence
 * Date: 11/25/14
 * Time: 2:51 PM
 * org.jcvi.ometa.engine
 */
public class BulkLoader {
    private static Logger logger = Logger.getLogger(BulkLoader.class);

    private final String BULK_FILE_DIRECTORY;
    private final String BULK_SYSTEM_USER;
    private final String BULK_SYSTEM_USERPASS;
    private final String BULK_SERVER;

    public BulkLoader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.BULK_FILE_DIRECTORY = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH) + File.separator + Constants.DIRECTORY_USER_BULK;
        this.BULK_SERVER = props.getProperty(Constants.CONFIG_BULK_LOAD_SERVER);
        this.BULK_SYSTEM_USER = props.getProperty(Constants.CONFIG_SYSTEM_USER);
        this.BULK_SYSTEM_USERPASS = props.getProperty(Constants.CONFIG_SYSTEM_USER_PASS);
    }

    public static void main(String[] args) throws Exception {
        BulkLoader bulkLoader = new BulkLoader();
        bulkLoader.processBulkLoads();
    }

    private void processBulkLoads() throws Exception {
        LoadingEngine loadingEngine = new LoadingEngine();
        int iterationResult = 0;

        try {
            LoadingEngineUsage usage = new LoadingEngineUsage();
            usage.setUsername(this.BULK_SYSTEM_USER);
            usage.setPassword(this.BULK_SYSTEM_USERPASS);
            usage.setServerUrl(this.BULK_SERVER);

            List<String> fileQueue = this.findNewDataFiles();

            if(fileQueue.size() > 0) {
                for(String filePath : fileQueue) {
                    File currentFile = new File(filePath);
                    String currentUserPath = currentFile.getParentFile().getParent();
                    String currentUserId = currentUserPath.substring(currentUserPath.lastIndexOf(File.separator) + 1);
                    usage.setSubmitter(currentUserId);

                    String currentFileName = currentFile.getName();
                    String onlyFileName = currentFileName.substring(0, currentFileName.lastIndexOf("."));

                    File outputDirectory = new File( // {storage path}/{userName}/{date}/processed/{fileName}
                            currentFile.getParent() + File.separator + Constants.DIRECTORY_PROCESSED_BULK + File.separator + onlyFileName
                    );
                    outputDirectory.mkdirs();

                    // move file to processed directory
                    FileUtils.copyFileToDirectory(currentFile, outputDirectory);
                    currentFile.delete();

                    usage.setInputFilename(outputDirectory + File.separator + currentFileName);
                    usage.setOutputLocation(outputDirectory.getAbsolutePath());
                    loadingEngine.setUsage(usage);

                    iterationResult = loadingEngine.batchLoad();

                    this.sendResultMail(
                            usage.getSubmitterName(), usage.getSubmitterEmail(),
                            currentFileName,
                            usage.getTotalCount(), usage.getSuccessCount(), usage.getFailCount(),
                            usage.getLogFilePath(), usage.getFailFilePath());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<String> findNewDataFiles() throws Exception {
        List<String> fileList = new ArrayList<String>();

        File storageDirectory = new File(this.BULK_FILE_DIRECTORY);
        for(File directoryItem : storageDirectory.listFiles()) { // get list of user directories

            if(directoryItem.isDirectory() && !directoryItem.isHidden()) {
                for(File dateItem : directoryItem.listFiles()) { // date directories

                    if(dateItem.isDirectory() && !dateItem.isHidden()) {
                        for(File fileItem : dateItem.listFiles()) {

                            if(fileItem.exists() && fileItem.isFile() && !fileItem.isHidden() && fileItem.getName().endsWith(".csv")) {
                                fileList.add(fileItem.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }

        return fileList;
    }

    public void sendResultMail(String userName, String userMail, String fileName, int total, int success, int fail, String logPath, String failPath) throws Exception {
        /*
        Dear <first> <last>,

        Your bulk data upload of “<submission-file-name>.csv" has been received and processed by the CEIRS DPCC.
        Please consult the attached “<submission-file-name>_summary.txt” file which confirms your data submission and identifies quality control errors we detected, if any.

             Total Submitted Records: 3
             Total Successfully Processed Records: 1
             Total Records With Quality Control Errors: 2

        Samples for which quality control errors were detected could not be written to the DPCC database and will need to be corrected and re-submitted.
        If this applies to your submission, please use the “<submission-file-name>_summary.txt” as a guide to correct the rejected records directly on the attached “<submission-file-name>_errors.csv" file and re-submit only that file to the DPCC.

        Thank you for your data contribution,
        The CEIRS DPCC Team

        For technical support, submit a support request online
        Or contact us:
        	By Phone: 1-855-846-2697
        	By Email: support@niaidceirs.org
         */

        String failedFileName = failPath == null ? "unknown" : failPath.substring(failPath.lastIndexOf(File.separator) + 1);
        String logFileName = logPath == null ? "unknown" : logPath.substring(logPath.lastIndexOf(File.separator) + 1);

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear ").append(userName).append(",<br/><br/>");

        bodyBuilder.append("Your bulk data upload of '").append(fileName).append("' has been received and processed by the CEIRS DPCC. ")
                .append("Please consult the attached '").append(logFileName)
                .append("' file which confirms your data submission and identifies quality control errors we detected, if any.<br/><br/>");

        bodyBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp; Total Submitted Records: ").append(total).append("<br/>")
                   .append("&nbsp;&nbsp;&nbsp;&nbsp; Total Successfully Processed Records: ").append(success).append("<br/>")
                   .append("&nbsp;&nbsp;&nbsp;&nbsp; Total Records With Errors: ").append(fail).append("<br/><br/>");

        bodyBuilder.append("Samples for which quality control errors were detected could not be written to the DPCC database and will need to be corrected and re-submitted. ")
                .append("If this applies to your submission, please use the '").append(logFileName)
                .append("' as a guide to correct the rejected records directly on the attached '")
                .append(failedFileName).append("' file and re-submit only that file to the DPCC.<br/><br/>");

        bodyBuilder.append("Thank you for your data contribution,<br/>The CEIRS DPCC Team<br/><br/>");

        bodyBuilder.append("For technical support, submit a support request <a href=\"https://dpcc.niaidceirs.org/ometa/support.action\">online</a><br/>")
                .append("Or contact us:<br/>&nbsp;&nbsp;&nbsp;&nbsp;By Phone: 1-855-846-2697<br/>&nbsp;&nbsp;&nbsp;&nbsp;By Email: support@niaidceirs.org<br/><br/>");


        EmailSender mailer = new EmailSender();
        List<String> files = new ArrayList<String>(2);
        files.add(logPath);
        files.add(failPath);
        mailer.send(userMail, "CEIRS DPCC - bulk data upload", bodyBuilder.toString(), files);
    }
}
