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
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.utils.*;
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
            LoadingEngineUsage usage = new LoadingEngineUsage(args);

            LoadingEngine engine = new LoadingEngine(usage);
            if(usage.isCmdLineNamedEvent()) {
                if(usage.isSequentialLoad()) {
                    engine.sequentialLoad();
                } else {
                    engine.loadEventFile();
                }
            } else if(usage.isMultiFile()) {
                engine.digestMultipart();
            } else if(usage.isDirectory()) {
                engine.digestMultiDirectory();
            } else if (usage.isMakeEventTemplate()) {
                engine.createEventTemplate();
            } else {
                engine.dispatchByFilename();
            }
            System.out.println("Loading process done!");
        } catch (DAOException daoex) {
            System.out.println("Database Access Error: " + daoex.getMessage());
            logger.error(LogTraceFormatter.formatStackTrace(daoex));
            logger.fatal(daoex);
        } catch (Throwable ex) {
            System.out.println("Error: " + ex.getMessage());
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
            ProjectSampleEventPresentationBusiness ejb = delegate.getEjb(
                    PresentationActionDelegate.EJB_NAME, server, userName, passWord, logger);

            Project project = ejb.getProject(projectName);
            List<EventMetaAttribute> emaList = ejb.getEventMetaAttributes(project.getProjectName(), eventName);
            ModelValidator validator = new ModelValidator();
            validator.validateEventTemplateSanity(emaList, projectName, sampleName, eventName);
            if (sampleName == null  ||  sampleName.trim().length() == 0) {
                // No sample name provided.
                // Need a sample?
                if (ejb.isSampleRequired(projectName, eventName)) {
                    throw new IllegalArgumentException(
                            "Sample is required for this template. Therefore, please provide a sample name."
                    );
                }
            }
            else {
                // User provided a sample name.
                // Does the sample go with the project?
                List<Sample> samples = ejb.getSamplesForProject(project.getProjectId());
                boolean found = false;
                for (Sample sample: samples) {
                    if (sample.getSampleName().equals(sampleName)) {
                        found = true;
                    }
                }
                if (! found) {
                    throw new IllegalArgumentException(
                            "Sample name " + sampleName + " given does not belong to project " + projectName
                    );
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

            File eventFile = new File(eventFileName);
            writer.writeEvent(eventFile, eventType, projectName, true);

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Given a file with multiple parts, applying to Projects, Samples, and events, handle all.
     */
    public void digestMultipart() throws Exception {
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
                    writer.writeEvent(file, null, null, true);
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

    public void sequentialLoad() throws Exception {
        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String serverUrl = usage.getServerUrl();

        String eventFileName = usage.getInputFilename();
        String eventName = usage.getEventName();
        String projectName = usage.getProjectName();
        String logPath = usage.getOutputLocation();

        File eventFile = new File(eventFileName);

        Long timeStamp = new Date().getTime();
        File logFile = new File(logPath + File.separator + "result_" + timeStamp + ".log");
        FileWriter logWriter = new FileWriter(logFile,true);

        // Must break this file up, and deposit it into a temporary output directory.
        String userBase = System.getProperty("user.home");
        ScratchUtils.setScratchBaseLocation(userBase + "/" + Constants.SCRATCH_BASE_LOCATION);
        File scratchLoc = ScratchUtils.getScratchLocation(timeStamp, "LoadingEngine__" + eventFile.getName());

        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord);

            LineIterator lineIterator = FileUtils.lineIterator(eventFile);
            int lineCount = 0;
            int processedLineCount = 0;

            String headerLine = null;
            while(lineIterator.hasNext()) {
                ++lineCount;
                String currLine = lineIterator.nextLine();

                if(lineCount == 1) {
                    headerLine = currLine;
                    continue;
                } else if(lineCount == 2 && (currLine.startsWith("#") || currLine.startsWith("\"#"))) {
                    continue;
                } else {
                    File singleEventFile = new File(scratchLoc.getAbsoluteFile() + File.separator + "temp.csv");
                    List<String> lines = new ArrayList<String>(2);
                    lines.add(headerLine);
                    lines.add(currLine);
                    FileUtils.writeLines(singleEventFile, lines);

                    String eventTarget = writer.writeEvent(singleEventFile, eventName, projectName, true);

                    logWriter.write(String.format("[line# %d] loaded event for %s\n", lineCount, eventTarget));

                    System.out.println(++processedLineCount + " event(s) loaded.");
                }
            }
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        } catch (Exception ex) {
            logWriter.write(ex.toString());
            throw ex;
        } finally {
            if(scratchLoc != null  &&  scratchLoc.exists()) {
                File cleanupDir = scratchLoc;
                String parentDirName = cleanupDir.getParentFile().getName();
                try {
                    Long.parseLong(parentDirName);
                    cleanupDir = cleanupDir.getParentFile();
                } catch (NumberFormatException nf) {}
                FileUtils.forceDelete(cleanupDir);
            }

            logWriter.close();
            System.out.println("log file: " + logFile.getAbsolutePath());
        }
    }
}
