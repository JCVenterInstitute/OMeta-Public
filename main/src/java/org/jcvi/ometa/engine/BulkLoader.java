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
                }
            }

        } catch (Exception ex) {

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

                            if(fileItem.exists() && fileItem.isFile() && !fileItem.isHidden()) {
                                fileList.add(fileItem.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }

        return fileList;
    }

    public void sendResultMail(String userName, String userMail, String fileName, int total, int success, int fail, String logPath) throws Exception {
        /*
        Dear <first> <last>,

        Your bulk data upload of <File Name> has been received and processed by the CEIRS DPCC.
        Please consult the attached log file which summarizes your data submission and highlights quality control errors we detected, if any.
        Samples for which data errors were detected could not be written to the DPCC database and will need to be corrected and re-submitted.

        Please review the log file to help you correct the rejected records and re-upload the corrected file.
        (using interactive tool or bulk dropbox upload using http://www.niaidceirs.org/)

        Total Submitted Records: <Count>
        Total Successfully Processed Records: <Count>
        Total Records With Errors: <Count>

        Thank you for your data contribution,
        The CEIRS DPCC Team
         */
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear ").append(userName).append(",\n\n");

        bodyBuilder.append("Your bulk data upload of '").append(fileName).append("' has been received and processed by the CEIRS DPCC. ")
                .append("Please consult the attached log file which summarizes your data submission and highlights quality control errors we detected, if any. ")
                .append("Samples for which data errors were detected could not be written to the DPCC database and will need to be corrected and re-submitted.\n\n")
                .append("Please review the log file to help you correct the rejected records and re-upload the corrected file.\n\n");

        bodyBuilder.append("Total Submitted Records: ").append(total).append("\n")
                .append("Total Successfully Processed Records: ").append(success).append("\n")
                .append("Total Records With Errors: ").append(fail).append("\n\n");

        bodyBuilder.append("Thank you for your data contribution,\nThe CEIRS DPCC Team");


        EmailSender mailer = new EmailSender();
        mailer.send(userMail, "CEIRS DPCC - bulk data upload", bodyBuilder.toString(), logPath);
    }
}
