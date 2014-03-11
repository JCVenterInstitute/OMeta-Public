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
    protected static final String INPUTFILE_PARAM_NAME = "inputfile";
    protected static final String MULTIPART_INPUTFILE_PARAM_NAME = "multipartInputfile";
    protected static final String DATABASE_ENVIRONMENT_PARAM_NAME = "serverUrl";
    protected static final String EVENT_TYPE_PARAM_NAME = "eventType";
    protected static final String OUTPUTLOC_PARAM_NAME = "outputLocation";
    protected static final String MAKE_EVENT_PARAM_NAME = "makeEvent";
    protected static final String PROJECT_NAME_PARAM_NAME = "projectName";
    protected static final String SAMPLE_NAME_PARAM_NAME = "sampleName";
    protected static final String TEMPLATE_EVENT_NAME = "templateEventName";

    private static final String INPUT_FILE_EXTENSION = ".tsv";

    private static final String APPLICATION_BLURB =
        "Loading engine: loads files of events and settings, to be written to the events database as table rows.  " +
        "  To make events, and get more detailed information, give flags   -" + MAKE_EVENT_PARAM_NAME + ",  -" +
        PROJECT_NAME_PARAM_NAME + ", -" + SAMPLE_NAME_PARAM_NAME + ", and -" + EVENT_TYPE_PARAM_NAME + ".";

    private OptionParameter usernameParam;
    private OptionParameter passwordParam;
    private OptionParameter inputFileNameParam;
    private OptionParameter multiDefinitionFileParam;
    private OptionParameter serverUrlParam;
    private OptionParameter eventTypeParam;
    private FlagParameter makeTemplateFlag;
    private OptionParameter projectNameParam;
    private OptionParameter sampleNameParam;
    private OptionParameter outputLocationParam;
    private OptionParameter templateEventNameParam;
    private StringBuilder errors;

    /**
     * Construct with all information given at a main function command line.
     *
     * @param commandLineArguments what user asked for
     * @throws Exception thrown by any called methods.
     */
    public LoadingEngineUsage(String[] commandLineArguments) throws Exception {
        init(commandLineArguments);
        if (! validate()) {
            System.out.println(getUsage());
            System.out.println();
            System.out.println(getErrors());
            System.exit(0);
        }
    }

    /**
     * Established this one to allow post-construction set-up.  Should call validate after all
     * parameters have been set.
     */
    public LoadingEngineUsage() throws Exception {
        initCommandLineHandler();
    }

    public String getUsername() { return usernameParam.getValue(); }
    public void setUsername( String username) { usernameParam.setValue( username ); }
    public String getPassword() { return passwordParam.getValue(); }
    public void setPassword( String password) { passwordParam.setValue( password ); }

    public String getInputFilename() { return inputFileNameParam.getValue(); }
    public void setInputFilename(String filename) { inputFileNameParam.setValue(filename); }
    public boolean isSingleFile() { return !isEmpty(inputFileNameParam); }

    public String getServerUrl() { return serverUrlParam.getValue(); }
    public void setServerUrl(String url) { serverUrlParam.setValue( url ); }
    public String getMultipartInputfileName() { return multiDefinitionFileParam.getValue(); }
    public void setMultipartInputfileName( String filename) { multiDefinitionFileParam.setValue( filename ); }
    public boolean isMultipart() { return !isEmpty( multiDefinitionFileParam); }

    public void setEventType( String eventType) { eventTypeParam.setValue( eventType ); }
    public String getEventType() { return eventTypeParam.getValue(); }

    // All related to creating a template for event.
    public void setProjectName( String projectName) { projectNameParam.setValue( projectName ); }
    public String getProjectName() { return projectNameParam.getValue(); }
    public void setSampleName( String sampleName) { sampleNameParam.setValue( sampleName ); }
    public String getSampleName() { return sampleNameParam.getValue(); }
    public boolean isMakeEventTemplate() { return makeTemplateFlag.getValue(); }
    public void setMakeEventTemplate(boolean flag) { makeTemplateFlag.setValue( flag ); }
    public String getTemplateEventName() { return templateEventNameParam.getValue(); }
    public void setTemplateEventName( String templateEventName) {  templateEventNameParam.setValue( templateEventName ); }
    public String getOutputLocation() { return outputLocationParam.getValue(); }
    public void setOutputLocation( String location) { outputLocationParam.setValue( location ); }
    public boolean isCmdLineNamedEvent() { return ! isEmpty( eventTypeParam ); }

    /** Answers the question: are the inputs good? */
    public boolean validate() {
        boolean rtnVal = true;
        errors = new StringBuilder();

        try {
            if(isMakeEventTemplate()) {
                testOutputLocation(outputLocationParam.getValue(), rtnVal);
                if(isEmpty(projectNameParam) || isEmpty(templateEventNameParam)) {
                    errors.append("No value provided for projectName or event template name, for template generation.\n");
                    rtnVal = false;
                }
            }
            else if(isEmpty(inputFileNameParam)  &&  isEmpty( multiDefinitionFileParam )) {
                errors.append("No value provided for parameters " + inputFileNameParam.getName() )
                      .append( " or " + multiDefinitionFileParam.getName() )
                      .append("\n");
                rtnVal = false;
            }
            else if(! isEmpty(inputFileNameParam)  &&  ! isEmpty( multiDefinitionFileParam )) {
                errors.append("Values provided for both parameters ")
                      .append(inputFileNameParam.getName())
                      .append(" and ")
                      .append(multiDefinitionFileParam.getName())
                      .append(".  Only one is allowed.")
                      .append("\n");
                rtnVal = false;
            }
            else {
                if(! isEmpty(inputFileNameParam)) {
                    rtnVal &= testInputFile(inputFileNameParam.getValue(), rtnVal);
                }
                else if(! isEmpty( multiDefinitionFileParam )) {
                    rtnVal &= testInputFile(multiDefinitionFileParam.getValue(), rtnVal);
                }
            }

            if(isEmpty(serverUrlParam)) {
                errors.append("Value must be provided for ")
                      .append(serverUrlParam.getName())
                      .append("\n");
                rtnVal = false;
            }

            // Under some circumstances, will need to login.  But no need if other params were wrong.
            if(rtnVal) {
                if(isEmpty( usernameParam )) {
                    Console console = System.console();
                    if(console == null) {
                        errors.append( "No " + usernameParam.getName() + " given, and no console available.\n" );
                    }
                    else {
                        usernameParam.setValue(console.readLine("Enter your USERNAME, please: "));
                    }
                }
                if(isEmpty( passwordParam )) {
                    Console console = System.console();
                    if(console == null) {
                        errors.append( "No " + passwordParam.getName() + " given, and no console available.\n" );
                    }
                    else {
                        char[] passwordArr = console.readPassword("Enter your PASSWORD, please: ");
                        passwordParam.setValue( new String( passwordArr ) );
                    }

                }
                if(isEmpty( usernameParam )  || isEmpty( passwordParam )) {
                    errors.append( "For the combination of parameters given, you must either provide both a " +
                            usernameParam.getName() + " value and a " + passwordParam.getName() +
                            " value, or you must respond with them when prompted.\n" );
                    rtnVal = false;
                }

            }

            // NOTE:  Cannot test that any event type exists, from within this usage object.  That
            //  must be done after database contact has been made.

        } catch ( Exception ex) {
            errors.append(ex.getMessage()).append("\n");
        }

        return rtnVal;
    }

    public String getErrors() { return errors.toString(); }

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
                eventTypeParam,
                multiDefinitionFileParam,
                projectNameParam,
                sampleNameParam,
                templateEventNameParam,
                makeTemplateFlag,
                outputLocationParam,
                serverUrlParam,
        };
        return CommandLineHandler.getGeneralJavaUsage(LoadingEngine.class, "", allParams, APPLICATION_BLURB, false);

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

    /**
     * Setup parameters.
     *
     * @return command line handler suitable to accept input parameters.
     * @throws Exception thrown by called methods.
     */
    private CommandLineHandler initCommandLineHandler() throws Exception {
        CommandLineHandler commandLineHandler = new CommandLineHandler();
        usernameParam = commandLineHandler.addOptionParameter(USERNAME_PARAM_NAME);
        usernameParam.setUsageInfo("Username for Projects database." +
                "\n      If required, and not given here, you will be prompted." +
                "\n      If prompt is not answered, the application could freeze."
        );
        passwordParam = commandLineHandler.addOptionParameter(PASSWORD_PARAM_NAME);
        passwordParam.setUsageInfo("Password for Projects database." +
                "\n      If required, and not given here, you will be prompted."
        );
        inputFileNameParam = commandLineHandler.addOptionParameter(INPUTFILE_PARAM_NAME);
        inputFileNameParam.setUsageInfo("Input file to be loaded, which applies to only one" +
                "\n      Project, Sample or Event."
        );
        multiDefinitionFileParam = commandLineHandler.addOptionParameter(MULTIPART_INPUTFILE_PARAM_NAME);
        multiDefinitionFileParam.setUsageInfo("Input file to be loaded, which applies multiple" +
                "\n      types of settings."
        );
        serverUrlParam = commandLineHandler.addOptionParameter(DATABASE_ENVIRONMENT_PARAM_NAME);
        serverUrlParam.setUsageInfo("Tells which application server host/port is to be used." +
                "\n      hostname.domain:PPPP please check with Project Websites team for values," +
                "\n      where PPPP is port number, usually something like 1099, 1299. 1399"
        );
        eventTypeParam = commandLineHandler.addOptionParameter(EVENT_TYPE_PARAM_NAME);
        eventTypeParam.setUsageInfo("If the file is an input file, states that the file represents" +
                "\n      an event with all its attributes, " +
                "\n      and tells what type of event is to be created."
        );
        makeTemplateFlag = commandLineHandler.addFlagParameter(MAKE_EVENT_PARAM_NAME);
        makeTemplateFlag.setUsageInfo("Output file template of the event name given will be created," +
                "\n       with headers and prompts to help fill it in."
        );
        projectNameParam = commandLineHandler.addOptionParameter(PROJECT_NAME_PARAM_NAME);
        projectNameParam.setUsageInfo("Project name to be used when generating template." +
                "\n       Use with -" + MAKE_EVENT_PARAM_NAME
        );
        sampleNameParam = commandLineHandler.addOptionParameter(SAMPLE_NAME_PARAM_NAME);
        sampleNameParam.setUsageInfo("Optional sample name to be used when generating template." +
                "\n       Use with -" + MAKE_EVENT_PARAM_NAME
        );
        outputLocationParam = commandLineHandler.addOptionParameter(OUTPUTLOC_PARAM_NAME);
        outputLocationParam.setUsageInfo("Output directory for use with -" + MAKE_EVENT_PARAM_NAME + " only.");
        templateEventNameParam = commandLineHandler.addOptionParameter(TEMPLATE_EVENT_NAME);
        templateEventNameParam.setUsageInfo("Type of event to be templated.  The required attribute names will be shown" +
                "\n      as rows in the output, with handy prompts."
        );

        return commandLineHandler;
    }

    /** Input files all follow these rules. */
    private boolean testInputFile(String filename, boolean rtnVal) {
        File testFile = new File( filename );
        if(!testFile.exists()) {
            errors.append("File " + filename + " does not exist.\n");
        } else if(!testFile.canRead()) {
            errors.append("File " + filename + " cannot be read.\n");
        } else if(!filename.toLowerCase().endsWith(INPUT_FILE_EXTENSION)) {
            errors.append("File " + filename + " does not end with proper extension.\n");
        } else {
            rtnVal = true;
        }
        return rtnVal;
    }

    /** Input files all follow these rules. */
    private boolean testOutputLocation(String location, boolean rtnVal) {
        if(location == null  ||  location.trim().length() == 0) {
            return false;
        }

        File testFile = new File( location );
        if(makeTemplateFlag.getValue()) {
            if(!testFile.exists()) {
                errors.append("Directory " + location + " does not exist.\n");
            } else if(!testFile.canWrite()) {
                errors.append("Directory " + location + " cannot be written.\n");
            } else if(!testFile.isDirectory()) {
                errors.append("File " + location + " is not a directory.\n");
            } else {
                rtnVal = true;
            }

        } else {
            errors.append( "Do not request an output location for any options except to make a template.\n");
            rtnVal = false;
        }
        return rtnVal;
    }

    // helper for determining if things are empty or not.
    private boolean isEmpty(OptionParameter param) {
        return param == null || param.getValue() == null || param.getValue().trim().length() == 0;
    }

}
