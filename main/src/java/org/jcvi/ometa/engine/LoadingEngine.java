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

package org.jcvi.ometa.engine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.configuration.FileMappingSupport;
import org.jcvi.ometa.configuration.InputBeanType;
import org.jcvi.ometa.hibernate.dao.DAOException;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.utils.*;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/9/11
 * Time: 12:40 AM
 *
 * Picks up the incoming events, and dispatches them appropriately.
 */
public class LoadingEngine {
    private LoadingEngineUsage usage;
    private static Logger logger = Logger.getLogger(LoadingEngine.class);

    //------------------------------------------MAIN
    /**
     * This method allows this POJO to be run as a standalone application.  It may also be run differently,
     * as long as the usage arguments are passed in as needed.
     *
     * @param args command line arguments conforming to the expectations set down in the LoadingEngineUsage class.
     */
    public static void main(String[] args) {
        try {
            int success = 1;
            LoadingEngineUsage usage = new LoadingEngineUsage(args);

            LoadingEngine engine = new LoadingEngine(usage);
            if(usage.isMakeEventTemplate() && usage.isCmdLineNamedEvent()) {
                engine.createEventTemplate();
            } else if(usage.isBatchLoad()) {
                success = engine.batchLoad();
            } else if(usage.isMultiFile()) {
                success = engine.digestMultipart();
            } else if(usage.isDirectory()) {
                engine.digestMultiDirectory();
            } else if(usage.isDownloadData()){
                engine.downloadData();
            } else {
                engine.loadEventFile();
                //engine.dispatchByFilename();
            }

            String finalMessage = null;
            if(success == 1) {
                finalMessage = "Loading process done!";
            } else {
                finalMessage = "Error occurred. check the log.";
            }
            System.out.println(finalMessage);

        } catch (DAOException daoex) {
            System.out.println("Database Access Error: " + daoex.getMessage());
            logger.error(LogTraceFormatter.formatStackTrace(daoex));
            logger.fatal(daoex);
        } catch (Throwable ex) {
            Throwable cause = ex.getCause();
            while(cause != null && cause.getCause() != null) { //drill down to the actual exception
                cause = cause.getCause();
            }
            System.out.println("Error: " + (cause == null ? ex.getMessage() : cause.getMessage()));
            logger.error(LogTraceFormatter.formatStackTrace(ex));
            logger.fatal(ex);
        }
    }

    //------------------------------------------CONSTRUCTORS
    /**
     * Constructor takes a "usage argument", a big parameter object which can (but need not) be fed
     * from command-line arguments.
     *
     * @param usage full configuration object.
     */
    public LoadingEngine(LoadingEngineUsage usage) {
        this.usage = usage;

    }

    public LoadingEngine() {}

    /**
     * Given the project and event, will create an incomplete "template/form" event TSV file.
     *
     * @throws Exception thrown for bad inputs, and called methods' errors.
     */
    public void createEventTemplate() throws Exception {

        String projectName = usage.getProjectName();
        String sampleName = usage.getSampleName();
        String eventName = usage.getEventName();
        String outputPath = usage.getOutputLocation();
        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String server = usage.getServerUrl();

        try {
            PresentationActionDelegate delegate = new PresentationActionDelegate();
            ProjectSampleEventPresentationBusiness ejb = delegate.getEjb(PresentationActionDelegate.EJB_NAME, server, userName, passWord, logger);

            Project project = ejb.getProject(projectName);
            List<EventMetaAttribute> emaList = ejb.getEventMetaAttributes(project.getProjectName(), eventName);
            ModelValidator validator = new ModelValidator();
            validator.validateEventTemplateSanity(emaList, projectName, sampleName, eventName);
            if (sampleName == null  ||  sampleName.trim().length() == 0) {
                // No sample name provided.
                // Need a sample?
                if (ejb.isSampleRequired(projectName, eventName)) {
                    throw new IllegalArgumentException("Sample is required for the event.");
                }
            }
            else {
                // User provided a sample name.
                // Does the sample go with the project?
                Sample sample = ejb.getSample(project.getProjectId(), sampleName);
                if(sample == null) {
                    throw new IllegalArgumentException("Sample '" + sampleName + "' not found with project '" + projectName + "'");
                }
            }

            TemplatePreProcessingUtils templateUtils = new TemplatePreProcessingUtils();
            InputStream templateInputStream = templateUtils.buildFileContent("c", emaList, projectName, sampleName, eventName);

            String outputFileName = eventName + "_" + new Date().getTime() + ".csv";
            File templateFile = new File(outputPath, outputFileName);
            FileOutputStream outputStream = new FileOutputStream(templateFile);
            IOUtils.copy(templateInputStream, outputStream);
            IOUtils.closeQuietly(outputStream);

            System.out.println("template file is at: " + templateFile.getAbsolutePath());

        } catch (Exception ex) {
            throw ex;
        }

    }

    /**
     * Take the input file to be a file representing a single event, of the type given in usage, and load that
     * event.
     *
     * @throws Exception for called methods.
     */
    public void loadEventFile() throws Exception {
        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String serverUrl = usage.getServerUrl();

        String eventFileName = usage.getInputFilename();
        String eventType = usage.getEventName();
        String projectName = usage.getProjectName();

        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord);

            String submissionId = Long.toString(CommonTool.getGuid()); //submission Id

            File eventFile = new File(eventFileName);
            writer.writeEvent(eventFile, eventType, projectName, true, eventFile.getParent(), submissionId, null);

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Given a file with multiple parts, applying to Projects, Samples, and events, handle all.
     */
    public int digestMultipart() throws Exception {
        int success = 1;

        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String serverUrl = usage.getServerUrl();
        String multipartFileName = usage.getMultipartInputfileName();
        File file = new File(multipartFileName);

        // Must break this file up, and deposit it into a temporary output directory.
        String userBase = System.getProperty("user.home");
        ScratchUtils.setScratchBaseLocation(userBase + "/" + Constants.SCRATCH_BASE_LOCATION);
        Long timeStamp = new Date().getTime();
        File scratchLoc = ScratchUtils.getScratchLocation(timeStamp, "LoadingEngine__" + file.getName());

        CombinedFileSplitter splitter = new CombinedFileSplitter();
        splitter.process(file, scratchLoc);

        // Now, will look for files in the directory of the types needed.
        FileCollector collector = new FileCollector(scratchLoc);
        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord);
            writer.writeMultiType(collector);

        } catch (Exception ex) {
            success = 0;
            throw ex;
        } finally {
            if (scratchLoc != null  &&  scratchLoc.exists()) {
                // The scratch directory may be within a numeric "unique id" directory.  That should
                // go away, too.
                File cleanupDir = scratchLoc;
                String parentDirName = cleanupDir.getParentFile().getName();
                try {
                    Long.parseLong(parentDirName);
                    cleanupDir = cleanupDir.getParentFile();
                } catch (NumberFormatException nf) {
                    // Do nothing. Just use the original.
                }
                splitter.removeDirectory(cleanupDir);
            }
        }

        return success;
    }

    /**
     * Given a directory with multiple files, applying to Projects, Samples, and events, handle all.
     * This function basically calls digestMultipart function recursively with files in a directory
     */
    public void digestMultiDirectory() throws Exception {
        String currentFileName = "";

        String multiDirectoryName = usage.getMultipartDirectoryParamName();
        File path = new File(multiDirectoryName);

        try {
            if(path.isDirectory() && path.canRead()) {
                File[] files = path.listFiles();
                List<File> filesToProcess = new ArrayList<File>(files.length);

                for(File file : files) {
                    //make sure project files get processed first before samples
                    if(file.canRead() && file.getName().toLowerCase().endsWith("_project" + FileMappingSupport.INPUT_FILE_EXTENSION)) {
                        filesToProcess.add(0, file);
                    } else {
                        filesToProcess.add(file);
                    }
                }

                for(File file: filesToProcess) {
                    if(file.isFile() && file.canRead() && file.getName().endsWith(FileMappingSupport.INPUT_FILE_EXTENSION)) {
                        currentFileName = file.getName();
                        usage.setMultipartInputfileName(file.getAbsolutePath());
                        this.digestMultipart();
                    }
                }

                System.out.println("Loading process done!");
            }
            else {
                throw new IllegalArgumentException("given path ' " + path + "' is not a directory or cannot be read.");
            }
        } catch(Exception ex) {
            throw new Exception("directory load failed (" + currentFileName + ") - " + ex.getMessage());
        }
    }

    /**
     * Here, a single file with a single purpose is being loaded.  All its contents apply to
     * objects of one type.
     *
     * @throws Exception thrown by called methods.
     */
    public void dispatchByFilename() throws Exception {
        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String serverUrl = usage.getServerUrl();
        String inputFilePathStr = usage.getInputFilename();
        File file = new File(inputFilePathStr);

        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord);
            InputBeanType inputBeanType = InputBeanType.getInputBeanType(inputFilePathStr);
            switch (inputBeanType) {
                case project:
                    writer.writeProjects(file);
                    break;
                case sample:
                    writer.writeSamples(file);
                    break;
                case lookupValue:
                    writer.writeLookupValues(file);
                    break;
                case sampleMetaAttributes:
                    writer.writeSMAs(file);
                    break;
                case eventMetaAttribute:
                    writer.writeEMAs(file);
                    break;
                case projectMetaAttributes:
                    writer.writePMAs(file);
                    break;
                case eventAttributes:
                    writer.writeEvent(file, null, null, true, null, null, null);
                    break;
                default:
                    throw new IllegalArgumentException(
                            inputFilePathStr + " is not named suitably for this application.  No data loaded."
                    );
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public int batchLoad() throws Exception {
        int success = 1;

        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String serverUrl = usage.getServerUrl();

        String eventFileName = usage.getInputFilename();
        String outputPath = usage.getOutputLocation();

        int batchSizeInt = 1;
        String batchSize = usage.getBatchSize();
        if(batchSize != null && !batchSize.isEmpty()) {
            batchSizeInt = Integer.parseInt(batchSize);
        }

        String submissionId = Long.toString(CommonTool.getGuid()); //submission Id

        File eventFile = new File(eventFileName);

        File logFile = new File(outputPath + File.separator + submissionId /*LoadingEngine.getNameWoExt(eventFile.getName())*/ + "_summary.txt");
        FileWriter logWriter = new FileWriter(logFile, false);

        if(!eventFile.canRead() || !eventFile.isFile()) {
            throw new Exception(ErrorMessages.CLI_BATCH_INPUT_FILE_MISSING);
        }
        if(!eventFileName.endsWith(".csv")) { // temporary?
            throw new Exception(ErrorMessages.CLI_BATCH_CSV_ONLY);
        }

        int successCount = 0;
        File processedFile = new File(outputPath + File.separator + submissionId /*LoadingEngine.getNameWoExt(eventFile.getName())*/ + "-success.csv");
        FileWriter processedWriter = new FileWriter(processedFile, false);

        int failedCount = 0;
        File failedFile = new File(outputPath + File.separator + submissionId /*LoadingEngine.getNameWoExt(eventFile.getName())*/  + "-errors.csv");
        FileWriter failedWriter = new FileWriter(failedFile, false);

        // Must break this file up, and deposit it into a temporary output directory.
        String userBase = System.getProperty("user.home");
        ScratchUtils.setScratchBaseLocation(userBase + "/" + Constants.SCRATCH_BASE_LOCATION);
        File scratchLoc = ScratchUtils.getScratchLocation(new Date().getTime(), "LoadingEngine__" + eventFile.getName());

        int processedLineCount = 0;
        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord);

            LineIterator lineIterator = FileUtils.lineIterator(eventFile);
            int lineCount = 0;

            String eventNameLine = null;
            String eventName = null;
            String headerLine = null;
            while(lineIterator.hasNext()) {
                ++lineCount;
                String currLine = lineIterator.nextLine();

                if(lineCount == 1) {
                    if(!currLine.startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) && currLine.contains(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER)) {
                        throw new Exception("event type is missing in the data file.");
                    }
                    eventNameLine = currLine;
                    String[] eventTypeTokens = eventNameLine.split(":");
                    if(eventTypeTokens.length != 2 || eventTypeTokens[1].isEmpty()) {
                        throw new Exception(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER + " must be '" + Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER + ":<eventName>'");
                    }
                    eventName = eventTypeTokens[1].trim().replaceAll("(,)*$", "");

                    processedWriter.write(eventNameLine + "\n");
                    failedWriter.write(eventNameLine + "\n");
                } else if(lineCount == 2) {
                    headerLine = currLine;
                    processedWriter.write(currLine + "\n");
                    failedWriter.write(currLine + "\n");
                    continue;
                } else {
                    if(currLine.startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) || currLine.startsWith("\"" + Constants.TEMPLATE_COMMENT_INDICATOR)) { //skip comment line
                        continue;
                    } else {
                        File singleEventFile = new File(scratchLoc.getAbsoluteFile() + File.separator + "temp.csv");
                        List<String> lines = new ArrayList<String>(2);
                        lines.add(eventNameLine);
                        lines.add(headerLine);
                        lines.add(currLine);
                        FileUtils.writeLines(singleEventFile, lines);

                        try {
                            String eventTarget = writer.writeEvent(singleEventFile, eventName, null, false, eventFile.getParent(), submissionId, (usage.getSubmitter() != null ? usage.getSubmitter() : userName));
                            //logWriter.write(String.format("[%d] loaded event%s.\n", lineCount, (eventTarget == null ? "" : " for '" + eventTarget + "'")));
                            processedWriter.write(currLine + "\n");
                            successCount++;
                        } catch(java.lang.IllegalAccessError iae) {
                            String exceptionString = iae.getCause() == null ? iae.getMessage() : iae.getCause().getMessage();
                            logWriter.write(exceptionString + "\n");
                            success = 0;
                            throw iae;
                        }catch (Exception ex) {
                            failedWriter.write(currLine + "\n");
                            logWriter.write(String.format("[%d] failed : ", ++failedCount));


                            if(ex.getClass() == javax.ejb.EJBException.class) {
                                String accessError = ex.getMessage();
                                if(accessError != null && accessError.contains("java.lang.IllegalAccessError")) {
                                    logWriter.write(ErrorMessages.DENIED_USER_EDIT_MESSAGE + "\n");
                                }
                            } else {
                                logWriter.write((ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()) + "\n");
                            }
                        }
                        processedLineCount++;
                    }
                }
            }
            usage.setTotalCount(processedLineCount);
            usage.setSuccessCount(successCount);
            usage.setFailCount(failedCount);
            usage.setLogFileName(logFile.getName());
            usage.setFailFileName(failedFile.getName());
            if(writer.getSubmitter() != null) {
                Actor submitter = writer.getSubmitter();
                usage.setSubmitterEmail(submitter.getEmail());
                usage.setSubmitterName(submitter.getFirstName() + " " + submitter.getLastName());
            }

        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
            success = 0;
        } catch (Exception ex) {
            String exceptionString = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
            logWriter.write(exceptionString + "\n");
            success = 0;
            throw ex;
        }  finally {
            if(scratchLoc != null  &&  scratchLoc.exists()) {
                File cleanupDir = scratchLoc;
                String parentDirName = cleanupDir.getParentFile().getName();
                try {
                    Long.parseLong(parentDirName);
                    cleanupDir = cleanupDir.getParentFile();
                } catch (NumberFormatException nf) {}
                FileUtils.forceDelete(cleanupDir);
            }

            if(failedCount == 0 && processedLineCount == successCount) {
                logWriter.write("All data has been loaded successfully for Submission ID: [" + submissionId + "]. Submission to the OMETA complete!");
            }

            logWriter.close();
            processedWriter.close();
            failedWriter.close();
            System.out.printf("total: %d, processed: %d, failed: %d\n", processedLineCount, successCount, failedCount);
            System.out.println("log file: " + logFile.getAbsolutePath());
        }

        return success;
    }

    public void downloadData() throws Exception {
        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String serverUrl = usage.getServerUrl();

        String projectName = usage.getProjectName();
        String eventName = usage.getEventName();
        String outputPath = usage.getOutputLocation();

        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord);
            String filePath = outputPath + File.separator + projectName + "_Data.csv";
            writer.readProjectData(filePath, projectName, eventName);

        } catch (Exception ex) {
            throw ex;
        }
    }


    public static String getNameWoExt(String name) {
        int index = name.lastIndexOf(".");
        if (index < 1) return name;
        return name.substring(0,index);
    }

    public void setUsage(LoadingEngineUsage usage) {
        this.usage = usage;
    }
}
