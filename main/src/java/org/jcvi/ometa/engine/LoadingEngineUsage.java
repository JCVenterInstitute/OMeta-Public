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

import org.jcvi.ometa.configuration.FileMappingSupport;
import org.jtc.common.util.command_line.*;

import java.io.Console;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/9/11
 * Time: 12:47 AM
 *
 * Sets up the command line usage for the loading engine. Complete with the usage strings.
 */
public class LoadingEngineUsage {

    protected static final String USERNAME_PARAM_NAME = "username";
    protected static final String PASSWORD_PARAM_NAME = "password";
    protected static final String INPUTFILE_PARAM_NAME = "i";
    protected static final String MULTIPART_INPUTFILE_PARAM_NAME = "mf";
    protected static final String MULTIPART_DIRECTORY_PARAM_NAME = "md";
    protected static final String PROJECT_NAME_PARAM_NAME = "p";
    protected static final String SAMPLE_NAME_PARAM_NAME = "s";
    protected static final String EVENT_NAME_PARAM_NAME = "e";
    protected static final String OUTPUTLOC_PARAM_NAME = "o";
    protected static final String BATCH_SIZE_PARAM_NAME = "b";
    protected static final String MAKE_EVENT_PARAM_NAME = "template";
    protected static final String BATCH_PARAM_NAME = "batch";
    protected static final String JSON_PARAM_NAME = "json";
    protected static final String DOWNLOAD_DATA_PARAM_NAME = "dd";
    protected static final String DATABASE_ENVIRONMENT_PARAM_NAME = "server";

    private OptionParameter usernameParam;
    private OptionParameter passwordParam;
    private OptionParameter inputFileNameParam;
    private OptionParameter multiInputFileParam;
    private OptionParameter multiDirectoryParam;
    private OptionParameter projectNameParam;
    private OptionParameter sampleNameParam;
    private OptionParameter eventNameParam;
    private OptionParameter outputLocationParam;
    private OptionParameter batchSizeParam;
    private FlagParameter makeTemplateFlag;
    private FlagParameter batchFlag;
    private FlagParameter jsonFlag;
    private FlagParameter downloadDataFlag;
    private OptionParameter serverUrlParam;
    private StringBuilder errors;

    //bulk load variables
    private String submitter;
    private String submitterName;
    private String submitterEmail;
    private int totalCount;
    private int successCount;
    private int failCount;
    private String logFileName;
    private String failFileName;
    private String outputDirectory;


    /**
     * Established this one to allow post-construction set-up.  Should call validate after all
     * parameters have been set.
     */
    public LoadingEngineUsage() throws Exception {
        initCommandLineHandler();
    }

    /**
     * Construct with all information given at a main function command line.
     *
     * @param commandLineArguments what user asked for
     * @throws Exception thrown by any called methods.
     */
    public LoadingEngineUsage(String[] commandLineArguments) throws Exception {
        init(commandLineArguments);
        if (!validate()) {
            System.out.println(getUsage());
            System.out.println();
            System.out.println(getErrors());
            System.exit(0);
        }
    }

    /**
     * Initialize the command line handler.
     *
     * @param commandLineArguments from main method
     * @throws Exception for any called.
     */
    private void init(String[] commandLineArguments) throws Exception {
        CommandLineHandler commandLineHandler = initCommandLineHandler();
        commandLineHandler.acceptCommandLine(commandLineArguments);
    }

    /** Answers the question: are the inputs good? */
    public boolean validate() {
        boolean rtnVal = true;
        errors = new StringBuilder();

        try {
            if(isMakeEventTemplate() || isBatchLoad() || isDownloadData()) { //validate on an output location
                validateOutputLocation(outputLocationParam.getValue(), rtnVal);
                if(isMakeEventTemplate() && (isEmpty(projectNameParam) || isEmpty(eventNameParam))) {
                    errors.append("need values for project and event.\n");
                    rtnVal = false;
                }
                if(isBatchLoad()) { //check input file for sequential load
                    rtnVal = validateInputfile(inputFileNameParam.getValue(), rtnVal);
                }
            } else {
                int count = 0;
                if(!isEmpty(inputFileNameParam)) {
                    ++count;
                }
                if(!isEmpty(multiInputFileParam)) {
                    ++count;
                }
                if(!isEmpty(multiDirectoryParam)) {
                    ++count;
                }
                if(isJson()){
                    ++count;
                }
                if(count > 1) { //only one
                    errors.append("Only one of " + inputFileNameParam.getName() + "," + multiInputFileParam.getName() + " or " + multiDirectoryParam.getName() + "is allowed.\n");
                    rtnVal = false;
                } else if(count == 0) { //at least one
                    errors.append("No value provided for " + inputFileNameParam.getName() + "," + multiInputFileParam.getName() + " or " + multiDirectoryParam.getName() + "\n");
                    rtnVal = false;
                } else { //one given
                    if(!isEmpty(multiDirectoryParam)) {
                        File directory = new File(multiDirectoryParam.getValue());
                        rtnVal = (directory.isDirectory() && directory.canRead());
                    } else if(!isEmpty(multiInputFileParam)) {
                        rtnVal = validateInputfile(multiInputFileParam.getValue(), rtnVal);
                    } else {
                        rtnVal = validateInputfile(inputFileNameParam.getValue(), rtnVal);
                    }
                }
            }

            if(isEmpty(serverUrlParam)) {
                errors.append(serverUrlParam.getName()).append("is required.\n");
                rtnVal = false;
            }

            // Under some circumstances, will need to login.  But no need if other params were wrong.
            if(rtnVal) {
                if(isEmpty(usernameParam)) {
                    Console console = System.console();
                    if(console == null) {
                        errors.append(usernameParam.getName()).append(" required, and no console available.\n");
                    } else {
                        usernameParam.setValue(console.readLine("Enter your USERNAME, please: "));
                    }
                }
                if(isEmpty(passwordParam)) {
                    Console console = System.console();
                    if(console == null) {
                        errors.append(passwordParam.getName()).append(" required, and no console available.\n");
                    } else {
                        char[] passwordArr = console.readPassword("Enter your PASSWORD, please: ");
                        passwordParam.setValue(new String(passwordArr));
                    }
                }
                if(isEmpty(usernameParam) || isEmpty(passwordParam)) {
                    errors.append("Both ").append(usernameParam.getName()).append(" and ").append(passwordParam.getName()).append(" are required or you must respond with them when prompted.\n");
                    rtnVal = false;
                }
            }
        } catch (Exception ex) {
            errors.append(ex.getMessage()).append("\n");
        }

        return rtnVal;
    }

    /**
     * Shows a usage string for figuring out how to run the program.
     *
     * @return usage string.
     * @throws Exception for any called method.
     */
    private String getUsage() throws Exception {
        CommandLineParameter[] allParams = new CommandLineParameter[] {
                usernameParam,
                passwordParam,
                inputFileNameParam,
                multiInputFileParam,
                multiDirectoryParam,
                projectNameParam,
                sampleNameParam,
                eventNameParam,
                outputLocationParam,
                batchSizeParam,
                makeTemplateFlag,
                batchFlag,
                downloadDataFlag,
                serverUrlParam
        };

        String blurb = "Loading engine: loads files of events and settings, to be written to the events database.  " +
                " To load events, and get more detailed information, get a template with " +
                "-" + MAKE_EVENT_PARAM_NAME + ",  -" + PROJECT_NAME_PARAM_NAME  + ", -" + EVENT_NAME_PARAM_NAME + "(, -" + SAMPLE_NAME_PARAM_NAME + ").";
        return CommandLineHandler.getGeneralJavaUsage(LoadingEngine.class, "", allParams, blurb, false);

    }

    /**
     * Setup parameters.
     *
     * @return command line handler suitable to accept input parameters.
     * @throws Exception thrown by called methods.
     */
    private CommandLineHandler initCommandLineHandler() throws Exception {
        CommandLineHandler commandLineHandler = new CommandLineHandler();
        usernameParam = commandLineHandler.addOptionParameter(USERNAME_PARAM_NAME);
        usernameParam.setUsageInfo("username not given here, you will be prompted.");
        passwordParam = commandLineHandler.addOptionParameter(PASSWORD_PARAM_NAME);
        passwordParam.setUsageInfo("password not given here, you will be prompted.");
        inputFileNameParam = commandLineHandler.addOptionParameter(INPUTFILE_PARAM_NAME);
        inputFileNameParam.setUsageInfo("input file to be loaded, which applies to only one Project, Sample or Event.");
        multiInputFileParam = commandLineHandler.addOptionParameter(MULTIPART_INPUTFILE_PARAM_NAME);
        multiInputFileParam.setUsageInfo("input file to be loaded, which applies multiple types of settings.");
        multiDirectoryParam = commandLineHandler.addOptionParameter(MULTIPART_DIRECTORY_PARAM_NAME);
        multiDirectoryParam.setUsageInfo("directory that has files to be loaded, which applies multiple types of settings.");
        projectNameParam = commandLineHandler.addOptionParameter(PROJECT_NAME_PARAM_NAME);
        projectNameParam.setUsageInfo("project name for loading or creating a template.");
        sampleNameParam = commandLineHandler.addOptionParameter(SAMPLE_NAME_PARAM_NAME);
        sampleNameParam.setUsageInfo("Optional sample name for events that require a sample.");
        eventNameParam = commandLineHandler.addOptionParameter(EVENT_NAME_PARAM_NAME);
        eventNameParam.setUsageInfo("event name for loading or creating a template.");
        outputLocationParam = commandLineHandler.addOptionParameter(OUTPUTLOC_PARAM_NAME);
        outputLocationParam.setUsageInfo("Output directory for a template and logs.");
        batchSizeParam = commandLineHandler.addOptionParameter(BATCH_SIZE_PARAM_NAME);
        batchSizeParam.setUsageInfo("user configurable batch size per transaction. default is 1.");
        makeTemplateFlag = commandLineHandler.addFlagParameter(MAKE_EVENT_PARAM_NAME);
        makeTemplateFlag.setUsageInfo("flag for creating an event template.");
        batchFlag = commandLineHandler.addFlagParameter(BATCH_PARAM_NAME);
        batchFlag.setUsageInfo("flag for batch load.");
        jsonFlag = commandLineHandler.addFlagParameter(JSON_PARAM_NAME);
        jsonFlag.setUsageInfo("flag for json producer.");
        downloadDataFlag = commandLineHandler.addFlagParameter(DOWNLOAD_DATA_PARAM_NAME);
        downloadDataFlag.setUsageInfo("flag for download project data");
        serverUrlParam = commandLineHandler.addOptionParameter(DATABASE_ENVIRONMENT_PARAM_NAME);
        serverUrlParam.setUsageInfo("server host/port(hostname.domain:port).");

        return commandLineHandler;
    }

    private boolean validateInputfile(String filename, boolean rtnVal) {
        File testFile = new File(filename);
        if(!testFile.exists()) {
            errors.append("File " + filename + " does not exist.\n");
        } else if(!testFile.canRead()) {
            errors.append("File " + filename + " cannot be read.\n");
        } else if(!filename.toLowerCase().endsWith(FileMappingSupport.INPUT_FILE_EXTENSION)) {
            errors.append("Only CSV format is supported.\n");
        } else {
            rtnVal = true;
        }
        return rtnVal;
    }

    private boolean validateOutputLocation(String location, boolean rtnVal) {
        if(location == null || location.trim().length() == 0) {
            return false;
        }

        File testFile = new File(location);
        if(makeTemplateFlag.getValue()) {
            if(!testFile.exists()) {
                errors.append("Directory " + location + " does not exist.\n");
            } else if(!testFile.canWrite()) {
                errors.append("Directory " + location + " cannot be written.\n");
            } else if(!testFile.isDirectory()) {
                errors.append(location + " is not a directory.\n");
            } else {
                rtnVal = true;
            }

        } else {
            errors.append("Do not request an output location for any options except to make a template.\n");
            rtnVal = false;
        }
        return rtnVal;
    }

    // helper for determining if things are empty or not.
    private boolean isEmpty(OptionParameter param) {
        return param == null || param.getValue() == null || param.getValue().trim().length() == 0;
    }

    //getters & setters
    public String getUsername() {
        return usernameParam.getValue();
    }
    public void setUsername(String username) {
        usernameParam.setValue(username);
    }

    public String getPassword() {
        return passwordParam.getValue();
    }
    public void setPassword(String password) {
        passwordParam.setValue(password);
    }

    public String getInputFilename() {
        return inputFileNameParam.getValue();
    }
    public void setInputFilename(String filename) {
        inputFileNameParam.setValue(filename);
    }

    public String getServerUrl() {
        return serverUrlParam.getValue();
    }
    public void setServerUrl(String url) {
        serverUrlParam.setValue(url);
    }

    public String getMultipartInputfileName() {
        return multiInputFileParam.getValue();
    }
    public void setMultipartInputfileName(String filename) {
        multiInputFileParam.setValue(filename);
    }
    public boolean isMultiFile() {
        return !isEmpty(multiInputFileParam);
    }

    public String getMultipartDirectoryParamName() {
        return multiDirectoryParam.getValue();
    }
    public void setMultipartDirectoryParamName(String dirname) {
        multiDirectoryParam.setValue(dirname);
    }
    public boolean isDirectory() {
        return !isEmpty(multiDirectoryParam);
    }

    public void setEventName(String eventType) {
        eventNameParam.setValue(eventType);
    }
    public String getEventName() {
        return eventNameParam.getValue();
    }

    public void setProjectName(String projectName) {
        projectNameParam.setValue(projectName);
    }
    public String getProjectName() {
        return projectNameParam.getValue();
    }

    public void setSampleName(String sampleName) {
        sampleNameParam.setValue(sampleName);
    }
    public String getSampleName() {
        return sampleNameParam.getValue();
    }

    public boolean isMakeEventTemplate() {
        return makeTemplateFlag.getValue();
    }

    public String getOutputLocation() {
        return outputLocationParam.getValue();
    }
    public void setOutputLocation(String location) {
        outputLocationParam.setValue(location);
    }

    public boolean isCmdLineNamedEvent() {
        return !isEmpty(eventNameParam);
    }

    public String getErrors() {
        return errors.toString();
    }

    public boolean isBatchLoad() {
        return batchFlag.getValue();
    }

    public boolean isJson() { return jsonFlag.getValue(); }

    public boolean isDownloadData() {
        return downloadDataFlag.getValue();
    }

    public String getBatchSize() {
        return batchSizeParam.getValue();
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getFailFileName() {
        return failFileName;
    }

    public void setFailFileName(String failFileName) {
        this.failFileName = failFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }
}
