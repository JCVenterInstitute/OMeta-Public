package org.jcvi.ometa.helper;

import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationRemote;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.model.GridBean;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.utils.*;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.*;

/**
 * User: movence
 * Date: 12/15/14
 * Time: 4:12 PM
 * org.jcvi.ometa.helper
 */
public class SequenceHelper {
    private static Logger logger = Logger.getLogger(SequenceHelper.class);

    private final String PROJECT_FILE_STORAGE;
    private final String USER_FILE_STORAGE;
    private final String DPCC_SUBMISSION_URL;
    private ProjectSampleEventPresentationBusiness readEjb;

    private final FileFilter notHiddenFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return !file.isHidden() && file.exists() && file.isFile();
        }
    };

    public SequenceHelper() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.PROJECT_FILE_STORAGE = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH) + File.separator + Constants.DIRECTORY_PROJECT;
        this.USER_FILE_STORAGE = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH) + File.separator + Constants.DIRECTORY_USER_BULK;
        this.DPCC_SUBMISSION_URL = props.getProperty(Constants.CONFIG_DATA_SUBMISSION_HOST);
    }

    public SequenceHelper(String server, String userName, String password) {
        this();
        PresentationActionDelegate readDelegate = new PresentationActionDelegate();
        this.readEjb = (ProjectSampleEventPresentationRemote)readDelegate.getEjb(PresentationActionDelegate.EJB_NAME, server, userName, password, logger);
    }

    public int processSequencePair(File dataFile, String submitterId) throws Exception {
        int success = 1;

        String userFullName = null;
        String userEmail = null;

        String sequenceFileName = null;
        String sequenceFilePath = null;

        try {
            // get actor information to send email
            Actor submitter = this.readEjb.getActorByUserName(submitterId);
            userFullName = submitter.getFirstName() + " " + submitter.getLastName();
            userEmail = submitter.getEmail();

            // parse data file
            TemplatePreProcessingUtils templateUtils = new TemplatePreProcessingUtils();
            List<GridBean> dataRows = templateUtils.parseEventFile(dataFile.getName(), dataFile, null, false, true);
            Map<String, GridBean> sampleIdToGridBeanMap = new HashMap<String, GridBean>(dataRows.size());

            String projectName = null;
            int gridLoopIndex = 1;
            int dataWithAccessionNumber = 0; // data row count with accession number data

            for(GridBean gridBean : dataRows) {
                if(gridLoopIndex++ == 1) {
                    projectName = gridBean.getProjectName();
                }

                Map<String, String> attributeKeyValueMap = new HashMap<String, String>(gridBean.getBeanList().size());

                for(FileReadAttributeBean bean : gridBean.getBeanList()) {
                    attributeKeyValueMap.put(bean.getAttributeName(), bean.getAttributeValue());
                }

                String sampleIdentifier = null;
                if(attributeKeyValueMap.containsKey(Constants.ATTR_SAMPLE_IDENTIFIER)) {
                    sampleIdentifier = attributeKeyValueMap.get(Constants.ATTR_SAMPLE_IDENTIFIER);
                }

                if(sampleIdentifier == null || sampleIdentifier.isEmpty()) {
                    throw new Exception(ErrorMessages.SEQUENCE_SAMPLE_IDENTIFIER_MISSING);
                } else {
                    boolean processSequenceFile = true;

                    // skip sequence file process if the data has Accession_Number already
                    if(attributeKeyValueMap.containsKey(Constants.ATTR_ACCESSION_NUMBER)) {
                        String accessionValue = attributeKeyValueMap.get(Constants.ATTR_ACCESSION_NUMBER);
                        if(accessionValue != null && !accessionValue.isEmpty() && !accessionValue.equals("NA")) {
                            dataWithAccessionNumber++;
                            gridBean.setHasAccessionNumber(1);
                            processSequenceFile = false;
                        }
                    }

                    if(processSequenceFile) {
                        sampleIdToGridBeanMap.put(sampleIdentifier, gridBean);
                    }
                }
            }

            // parse or unzip the sequence pair file
            File sequenceFileDirectory = this.processSequenceFile(dataFile);

            File[] sequenceFileArr = sequenceFileDirectory.listFiles(this.notHiddenFileFilter);

            if((dataRows.size() - dataWithAccessionNumber) != sampleIdToGridBeanMap.size() && sampleIdToGridBeanMap.size() != sequenceFileArr.length) {
                String ex = ErrorMessages.SEQUENCE_NUMBER_MISMATCH;

                if(sampleIdToGridBeanMap.size() < sequenceFileArr.length) { //extra sequence data in a fasta file
                    ex = ErrorMessages.SEQUENCE_EXTRA_SEQUENCE_DATA;
                }

                throw new Exception(ex);
            }

            int pairCount = 0;
            for(String sampleIdentifier : sampleIdToGridBeanMap.keySet()) {
                for(File sequenceFile : sequenceFileArr) {
                    if(sequenceFile.getName().toLowerCase().contains(sampleIdentifier.toLowerCase())) {
                        sampleIdToGridBeanMap.get(sampleIdentifier).setSequenceFileName(sequenceFile.getName());
                        pairCount++;
                        break;
                    }
                }
            }

            if(pairCount == (dataRows.size() - dataWithAccessionNumber)) {
                // get project to store the sequence file into a directory with project ID
                Project currProject = this.readEjb.getProject(projectName);
                if(currProject == null) {
                    throw new Exception(String.format(ErrorMessages.PROJECT_NOT_FOUND, projectName));
                }

                // move sequence file to a permanent location
                File newDataFile = new File(dataFile.getAbsolutePath() + ".temp");
                FileWriter newDataFileWriter = new FileWriter(newDataFile, false);

                // copy headers from original data file, then add sequence file path attribute field
                int dataLineCount = 1;
                LineIterator dataIterator = FileUtils.lineIterator(dataFile);
                while(dataLineCount < 3) {
                    String currLine = dataIterator.nextLine().trim();
                    if(dataLineCount == 1) { // event type
                        newDataFileWriter.write(currLine + "\n");
                    } else if(dataLineCount == 2) { // headers
                        newDataFileWriter.write(currLine + "," + Constants.ATTR_SEQUENCE_PATH + "\n");
                    }
                    dataLineCount++;
                }
                dataIterator.close();

                String projectDateStoragePath = currProject.getProjectId() + File.separator + CommonTool.currentDateToDefaultFormat();
                String relativeSequenceDirectory = sequenceFileDirectory.getAbsolutePath().replace(dataFile.getParent() + File.separator, "");
                // add sequence file path to each data row
                for(GridBean gridBean : dataRows) {
                    String dataRow = this.joinParsedCSVData(gridBean.getParsedRowData()) + ",";

                    if(gridBean.getHasAccessionNumber() == 0) {
                        /*File originalSequenceFile = new File(sequenceFileDirectory.getAbsolutePath() + File.separator + gridBean.getSequenceFileName());
                        String sequenceFileCopyPath = projectDateStoragePath + File.separator + CommonTool.getGuid() + "-" + gridBean.getSequenceFileName();
                        File sequenceFileCopy = new File(this.PROJECT_FILE_STORAGE + File.separator + sequenceFileCopyPath);

                        // move sequence file to a permanent location
                        sequenceFileCopy.getParentFile().mkdirs();
                        FileUtils.copyFile(originalSequenceFile, sequenceFileCopy);
                        FileUtils.forceDelete(originalSequenceFile);*/

                        String sequenceFileCopyPath = relativeSequenceDirectory + File.separator + gridBean.getSequenceFileName();

                        // add sequence file path to each data row
                        dataRow += sequenceFileCopyPath;
                    }

                    newDataFileWriter.write(dataRow + "\n");
                }

                newDataFileWriter.close();

                // replace data file
                FileUtils.copyFile(newDataFile, dataFile);
                // delete the temp
                FileUtils.forceDelete(newDataFile);

                //FileUtils.deleteDirectory(sequenceFileDirectory);
            }

        } catch(Exception ex) {
            ex.printStackTrace();

            this.sendResultMail(userFullName, userEmail, dataFile, sequenceFileName, sequenceFilePath, ex.getMessage());
            //FileUtils.forceDelete(dataFile);

            success = 0;
        }

        return success;
    }

    private File processSequenceFile(File dataFile) throws Exception {
        File sequenceFileDirectory = null;

        try {

            // find sequence file that pairs with the data file
            File sequencePair = null;

            String currentFileName = FilenameUtils.getBaseName(dataFile.getName());
            boolean isZip = false; // zip file flag

            for(File sibling : dataFile.getParentFile().listFiles(this.notHiddenFileFilter)) {
                String siblingName = sibling.getName().toLowerCase();
                if(siblingName.contains(currentFileName.toLowerCase())) {

                    if(FilenameUtils.isExtension(siblingName, Constants.SEQUENCE_EXTENSIONS)) { // .fasta | .fa
                        sequencePair = sibling;
                        break; //found pair
                    } else {

                        if(FilenameUtils.isExtension(siblingName, "zip")) {
                            isZip = true;
                            sequencePair = sibling;
                            break; //found pair
                        }
                    }
                }
            }

            sequenceFileDirectory = new File(dataFile.getParentFile() + File.separator + currentFileName); // store sequence files

            if(sequencePair == null || !sequencePair.exists() || !sequencePair.isFile()) { // sequence pair not found exception
                throw new Exception(String.format(ErrorMessages.SEQUENCE_FILE_MISSING, dataFile.getName()));

            } else {
                sequenceFileDirectory.mkdirs();

                if(isZip) { // unzip
                    ZipFile zipFile = new ZipFile(sequencePair);
                    zipFile.extractAll(sequenceFileDirectory.getAbsolutePath());


                    final FileFilter notHiddenDirectoryFilter = new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            String[] skip = { // name of directories to be skipped
                                    "__MACOSX"
                            };
                            List<String> skipList = Arrays.asList(skip);
                            return !file.isHidden() && file.exists() && file.isDirectory() && !skipList.contains(file.getName());
                        }
                    };
                    final FileFilter wildFilter = new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return !file.isHidden() && file.exists();
                        }
                    };

                    // move all files in a sub-directory to parent directory
                    for(File subItem : sequenceFileDirectory.listFiles(notHiddenDirectoryFilter)) {
                        for(File subSubItem : subItem.listFiles(wildFilter)) {
                            if(subSubItem.isDirectory()) {
                                throw new Exception(ErrorMessages.SEQUENCE_MULTILEVEL_ZIP);
                            } else {
                                if(FilenameUtils.isExtension(subSubItem.getName(), Constants.SEQUENCE_EXTENSIONS)) {
                                    FileUtils.copyFileToDirectory(subSubItem, sequenceFileDirectory);
                                }
                            }
                        }

                        FileUtils.deleteDirectory(subItem);
                    }

                } else { // parse big sequence file by Sample_Identifier
                    Map<String, List<String>> sequenceGroupedBySI = this.parseSequenceFileIntoMapBySI(sequencePair);

                    if(sequenceGroupedBySI.size() > 0) {
                        for(String sampleIdentifier : sequenceGroupedBySI.keySet()) { // create individual sequence files
                            File singleSequenceFile = new File(sequenceFileDirectory.getPath() + File.separator + sampleIdentifier + "." + Constants.SEQUENCE_EXTENSIONS[0]);

                            FileWriter fw = new FileWriter(singleSequenceFile);
                            IOUtils.writeLines(sequenceGroupedBySI.get(sampleIdentifier), "\n", fw);
                            IOUtils.closeQuietly(fw);
                        }
                    }
                }

                FileUtils.forceDelete(sequencePair);
            }

        } catch (Exception ex) {
            if(sequenceFileDirectory != null && sequenceFileDirectory.isDirectory()) {
                FileUtils.deleteDirectory(sequenceFileDirectory);
            }

            sequenceFileDirectory = null;

            throw ex;
        }

        return sequenceFileDirectory;
    }

    private String getSampleIdentifierFromAttributes(List<FileReadAttributeBean> beans) {
        String sampleIdentifier = null;

        Map<String, String> attributeKeyValueMap = new HashMap<String, String>(beans.size());

        for(FileReadAttributeBean bean : beans) {
            attributeKeyValueMap.put(bean.getAttributeName(), bean.getAttributeValue());
        }

        if(attributeKeyValueMap.containsKey(Constants.ATTR_SAMPLE_IDENTIFIER)) {
            sampleIdentifier = attributeKeyValueMap.get(Constants.ATTR_SAMPLE_IDENTIFIER);
        }

        return sampleIdentifier;
    }

    private String joinParsedCSVData(String[] data) {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < data.length; i++) {
            if(i > 0) {
                sb.append(",");
            }

            sb.append(data[i] == null ? "" : data[i]);
        }

        return sb.toString();
    }

    public Map<String, List<String>> parseSequenceFileIntoMapBySI(File sequenceFile) throws Exception {
        Map<String, List<String>> sequenceGroupedBySI = new HashMap<String, List<String>>();

        LineIterator sequenceIterator = FileUtils.lineIterator(sequenceFile);
        String currentSampleIdentifier = null;

        while(sequenceIterator.hasNext()) {
            String currLine = sequenceIterator.nextLine().trim();

            if(currLine.startsWith(">") && currLine.contains(Constants.ATTR_SAMPLE_IDENTIFIER)) { // metadata line
                boolean hasSegment = false;

                String[] metadataArr = currLine.split("\\|");
                for(String metadata : metadataArr) {
                    if(metadata.toLowerCase().contains(Constants.ATTR_SAMPLE_IDENTIFIER.toLowerCase())) {
                        String[] keyValue = metadata.split(":");
                        currentSampleIdentifier = keyValue[1];
                    } else if(metadata.toLowerCase().contains(Constants.ATTR_SEQUENCE_SEGMENT.toLowerCase())) {
                        hasSegment = true;
                    }
                }

                if(!hasSegment) {
                    throw new Exception(ErrorMessages.SEQUENCE_MISSING_SEGMENT);
                }

                if(sequenceGroupedBySI.containsKey(currentSampleIdentifier)) {
                    sequenceGroupedBySI.get(currentSampleIdentifier).add(currLine);
                    /*throw new Exception(
                            String.format(ErrorMessages.SEQUENCE_DUPLICATED_ATTRIBUTE, Constants.ATTR_SAMPLE_IDENTIFIER, currentSampleIdentifier, sequencePair.getName())
                    );*/
                } else {
                    List<String> sequenceLines = new ArrayList<String>();
                    sequenceLines.add(currLine);

                    sequenceGroupedBySI.put(currentSampleIdentifier, sequenceLines);
                }

            } else { // add to sequence lines
                sequenceGroupedBySI.get(currentSampleIdentifier).add(currLine);
            }
        }

        return sequenceGroupedBySI;
    }

    public void sendResultMail(String userName, String userMail, File dataFile, String sequenceFileName, String sequenceFilePath, String error) throws Exception {
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear ").append(userName).append(",<br/><br/>");

        bodyBuilder.append("Your bulk data upload of '").append(dataFile.getName());
        if(sequenceFileName != null) {
            String sequenceLink = "<a href=\"" + this.DPCC_SUBMISSION_URL + "/fileDownloader.action?filePath=" + sequenceFilePath + "\">" + sequenceFileName + "</a>";
            bodyBuilder.append(" and '").append(sequenceLink).append("' have");
        } else {
            bodyBuilder.append("' has");
        }
        bodyBuilder.append(" been received and processed by the CEIRS DPCC.<br/><br/>");

        bodyBuilder.append("Following error was found during the sequence submission process:<br/>");
        bodyBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp; '").append(error).append("'<br/><br/>");

        bodyBuilder.append("Samples for which quality control errors were detected could not be written to the DPCC database and will need to be corrected and re-submitted. ")
                .append("If this applies to your submission, please re-submit files to the DPCC.<br/><br/>");

        bodyBuilder.append("Thank you for your data contribution,<br/>The CEIRS DPCC Team<br/><br/>");
        bodyBuilder.append(Constants.DPCC_MAIL_SIGNATURE_HELP);

        List<String> files = new ArrayList<String>(1);
        files.add(dataFile.getAbsolutePath());

        EmailSender mailer = new EmailSender();
        mailer.send(userMail, "CEIRS DPCC - Sequence Submission bulk data upload", bodyBuilder.toString(), files);
    }
}
