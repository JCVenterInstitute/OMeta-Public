package org.jcvi.ometa.engine;

import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.jcvi.ometa.helper.SequenceHelper;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.EmailSender;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.io.FileFilter;
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
    private final Integer FILE_QUITE_TIME;

    private final String ZIP_FILE_DIRECTORY_APPENDER = "___zip";

    public BulkLoader() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.BULK_FILE_DIRECTORY = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH) + File.separator + Constants.DIRECTORY_USER_BULK;
        this.BULK_SERVER = props.getProperty(Constants.CONFIG_BULK_LOAD_SERVER);
        this.BULK_SYSTEM_USER = props.getProperty(Constants.CONFIG_SYSTEM_USER);
        this.BULK_SYSTEM_USERPASS = props.getProperty(Constants.CONFIG_SYSTEM_USER_PASS);

        this.FILE_QUITE_TIME = Integer.parseInt(props.getProperty(Constants.CONFIG_FILE_QUITETIME));
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
                SequenceHelper sequenceHelper = new SequenceHelper(this.BULK_SERVER, this.BULK_SYSTEM_USER, this.BULK_SYSTEM_USERPASS);

                for(String filePath : fileQueue) {
                    File currentFile = new File(filePath);
                    String currentUserPath = filePath.substring(filePath.indexOf(Constants.DIRECTORY_USER_BULK) + Constants.DIRECTORY_USER_BULK.length() + 1);
                    String currentUserId = currentUserPath.substring(0, currentUserPath.indexOf(File.separator));
                    usage.setSubmitter(currentUserId);

                    boolean isFromZip = currentUserPath.indexOf(this.ZIP_FILE_DIRECTORY_APPENDER) > 0;

                    String currentFileName = currentFile.getName();

                    // process sequence file
                    String eventName = this.getEventNameFromFile(currentFile);
                    if(eventName != null && eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION) && eventName.contains(Constants.EVENT_SEQUENCE_SUBMISSION)) {
                        if(sequenceHelper.processSequencePair(currentFile, currentUserId) != 1) {
                            continue;
                        }
                    }

                    File dateDirectory = isFromZip ? currentFile.getParentFile().getParentFile() : currentFile.getParentFile(); // one level more for zip contents
                    String processingDirectoryPath = dateDirectory.getAbsolutePath() + File.separator + Constants.DIRECTORY_PROCESSING_BULK + File.separator;
                    processingDirectoryPath += FilenameUtils.getBaseName(isFromZip ? currentFile.getParentFile().getName() : currentFileName);
                    String outputDirectoryPath = dateDirectory.getAbsolutePath() + File.separator + Constants.DIRECTORY_PROCESSED_BULK + File.separator;
                    outputDirectoryPath += FilenameUtils.getBaseName(isFromZip ? currentFile.getParentFile().getName() : currentFileName);

                    File processingDirectory = new File(processingDirectoryPath);
                    processingDirectory.mkdirs();

                    if(isFromZip) {
                        FileUtils.copyDirectory(currentFile.getParentFile(), processingDirectory);
                        FileUtils.deleteDirectory(currentFile.getParentFile());
                    } else {
                        FileUtils.copyFileToDirectory(currentFile, processingDirectory);
                        FileUtils.forceDelete(currentFile);
                    }
                    /*// move file to processed directory
                    FileUtils.copyFileToDirectory(currentFile, outputDirectory);
                    currentFile.delete();
                    if(isFromZip) {
                        FileUtils.deleteDirectory(currentFile.getParentFile());
                    }*/

                    usage.setInputFilename(processingDirectory + File.separator + currentFileName);
                    usage.setOutputLocation(processingDirectory.getAbsolutePath());
                    loadingEngine.setUsage(usage);

                    iterationResult = loadingEngine.batchLoad();

                    //Move csv file under error folder if it is not successfully processed
                    if (iterationResult == 0) outputDirectoryPath += File.separator + Constants.DIRECTORY_ERROR_BULK;

                    File outputDirectory = new File(outputDirectoryPath); // {storage path}/{userName}/{date}/processed/{fileName}
                    outputDirectory.mkdirs();
                    FileUtils.copyDirectory(processingDirectory, outputDirectory);
                    FileUtils.forceDeleteOnExit(processingDirectory);

                    this.sendResultMail(
                            usage.getSubmitterName(), usage.getSubmitterEmail(),
                            currentFileName,
                            usage.getTotalCount(), usage.getSuccessCount(), usage.getFailCount(),
                            outputDirectoryPath + File.separator + usage.getLogFileName(),
                            outputDirectoryPath + File.separator + usage.getFailFileName());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<String> findNewDataFiles() throws Exception {
        List<String> fileList = new ArrayList<String>();
        String[] acceptedFileExtensions = { "csv", "zip" };

        File storageDirectory = new File(this.BULK_FILE_DIRECTORY);

        File[] userDirectories = storageDirectory.listFiles();
        if(userDirectories != null) {
            for(File userItem : userDirectories) { // get list of user directories

                if(userItem.isDirectory() && !userItem.isHidden()) {

                    File[] dateDirectories = userItem.listFiles();
                    if(dateDirectories != null) {
                        for(File dateItem : dateDirectories) { // date directories

                            if(dateItem.isDirectory() && !dateItem.isHidden()) {

                                File[] files = dateItem.listFiles();
                                if(files != null) {
                                    for(File fileItem : files) {

                                        if(fileItem.exists() && fileItem.isFile() && !fileItem.isHidden()) {
                                            String fileName = fileItem.getName();
                                            if(FilenameUtils.isExtension(fileName, acceptedFileExtensions)) { // .csv or zip only

                                                if(FilenameUtils.isExtension(fileName, acceptedFileExtensions[1])) {
                                                    ZipFile zipFile = new ZipFile(fileItem);
                                                    File zipDirectory = new File(fileItem.getParent() + File.separator + FilenameUtils.getBaseName(fileItem.getName()) + this.ZIP_FILE_DIRECTORY_APPENDER);
                                                    zipDirectory.mkdirs();
                                                    zipFile.extractAll(zipDirectory.getAbsolutePath());
                                                    FileUtils.forceDeleteOnExit(fileItem);

                                                    File[] filesInZip = zipDirectory.listFiles(new FileFilter() {
                                                        @Override
                                                        public boolean accept(File file) {
                                                            return FilenameUtils.isExtension(file.getName(), "csv");
                                                        }
                                                    });

                                                    // missing or multiple csv data file error
                                                    if(filesInZip.length == 0) {
                                                        throw new Exception(ErrorMessages.BULK_CSV_FILE_MISSING);
                                                    } else if(filesInZip.length > 1) {
                                                        throw new Exception(ErrorMessages.BULK_MULTIPLE_CSV_FILE);
                                                    } else {
                                                        fileItem = filesInZip[0];
                                                    }
                                                }

                                                if((System.currentTimeMillis() - fileItem.lastModified()) / (1000 * 60) >= this.FILE_QUITE_TIME) { // quite time for 10 min
                                                    fileList.add(fileItem.getAbsolutePath());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return fileList;
    }

    private String getEventNameFromFile(File eventFile) throws Exception {

        LineIterator lineIterator = FileUtils.lineIterator(eventFile);
        String eventName = null;

        if(lineIterator.hasNext()) {
            String currLine = lineIterator.nextLine();

            if(currLine.startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) && currLine.contains(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER)) {
                String eventNameLine = currLine;
                String[] eventTypeTokens = eventNameLine.split(":");

                if (eventTypeTokens.length == 2 && !eventTypeTokens[1].isEmpty()) {
                    eventName = eventTypeTokens[1].trim().replaceAll("(,)*$", "");
                }
            }
        }

        return eventName;
    }

    public void sendResultMail(String userName, String userMail, String fileName,
                               int total, int success, int fail,
                               String logPath, String failPath) throws Exception {

        String logFileName = null;
        List<String> files = new ArrayList<String>(2);
        if(logPath != null) {
            files.add(logPath);
            logFileName = logPath.substring(logPath.lastIndexOf(File.separator) + 1);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear ").append(userName).append(",<br/><br/>");

        bodyBuilder.append("Your bulk data upload of '").append(fileName).append("' has been received and processed by the OMETA. ")
                .append("Please consult the attached '").append(logFileName)
                .append("' file which confirms your data submission and identifies quality control errors we detected, if any.<br/><br/>");

        bodyBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp; Total Submitted Records: ").append(total).append("<br/>")
                .append("&nbsp;&nbsp;&nbsp;&nbsp; Total Successfully Processed Records: ").append(success).append("<br/>")
                .append("&nbsp;&nbsp;&nbsp;&nbsp; Total Records With Errors: ").append(fail).append("<br/><br/>");

        if(fail > 0) {
            String failedFileName = null;
            if(failPath != null) {
                files.add(failPath);
                failedFileName = failPath.substring(failPath.lastIndexOf(File.separator) + 1);
            }

            bodyBuilder.append("Samples for which quality control errors were detected could not be written to the OMETA database and will need to be corrected and re-submitted. ")
                    .append("If this applies to your submission, please use the '").append(logFileName)
                    .append("' as a guide to correct the rejected records directly on the attached '")
                    .append(failedFileName).append("' file and re-submit only that file to the OMETA.<br/><br/>");

        }

        bodyBuilder.append("Thank you for your data contribution,<br/>The OMETA Team<br/><br/>");
        bodyBuilder.append(Constants.DPCC_MAIL_SIGNATURE_HELP);


        EmailSender mailer = new EmailSender();
        mailer.send(userMail, "OMETA - bulk data upload", bodyBuilder.toString(), files);
    }
}
