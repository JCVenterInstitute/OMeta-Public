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

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.configuration.BeanPopulator;
import org.jcvi.ometa.configuration.EventLoader;
import org.jcvi.ometa.configuration.InputBeanType;
import org.jcvi.ometa.hibernate.dao.DAOException;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.*;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
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
    private static Logger logger = Logger.getLogger( LoadingEngine.class );

    //------------------------------------------MAIN
    /**
     * This method allows this POJO to be run as a standalone application.  It may also be run differently,
     * as long as the usage arguments are passed in as needed.
     *
     * @param args command line arguments conforming to the expectations set down in the LoadingEngineUsage class.
     */
    public static void main(String[] args) {
        try {
            LoadingEngineUsage usage = new LoadingEngineUsage( args );
            boolean isValid = usage.validate();
            if ( !isValid ) {
                System.out.println( usage.getErrors() );
                throw new IllegalArgumentException("Invalid Usage");
            }
            LoadingEngine engine = new LoadingEngine( usage );
            if ( usage.isCreateStubb() ) {
                engine.createStubbFile();
            }
            else if ( usage.isCmdLineNamedEvent() ) {
                engine.loadEventFile();
            }
            else if ( usage.isMultipart() ) {
                engine.digestMultipart();
            }
            else if ( usage.isMakeEventTemplate() ) {
                engine.createEventTemplate();
            }
            else {
                engine.dispatchByFilename();

            }
            System.out.println("Loading process done!");
        } catch ( DAOException daoex ) {
            System.out.println("Database Access Error: " + daoex.getMessage());
            logger.error(LogTraceFormatter.formatStackTrace( daoex ));
            logger.fatal( daoex );
        } catch ( Throwable ex ) {
            System.out.println("Error: " + ex.getMessage());
            logger.error(LogTraceFormatter.formatStackTrace( ex ));
            logger.fatal( ex );
        }
    }

    //------------------------------------------CONSTRUCTORS
    /**
     * Constructor takes a "usage argument", a big parameter object which can (but need not) be fed
     * from command-line arguments.
     *
     * @param usage full configuration object.
     */
    public LoadingEngine( LoadingEngineUsage usage ) {
        this.usage = usage;

    }

    //------------------------------------------PUBLIC INTERFACE
    /** Makes a file with all the headers expected in a normal input file of type given. */
    public void createStubbFile() throws Exception {
        String inputFile = usage.getInputFilename();
        InputBeanType inputBeanType = InputBeanType.getInputBeanType(inputFile);
        File file = new File( inputFile );
        Class beanClass = decodeBeanClass(inputBeanType);

        BeanPopulator populator = new BeanPopulator( beanClass );
        List<String> headerList = populator.getHeaderNames();

        PrintWriter pw = new PrintWriter( new FileWriter( file ) );
        int colNum = 0;
        for ( String header: headerList ) {
            if ( colNum > 0 ) {
                pw.print("\t");
            }
            pw.print( header );
            colNum ++;
        }
        pw.println();
        pw.close();
    }

    /**
     * Given the project and event, will create an incomplete "template/form" event TSV file.
     *
     * @throws Exception thrown for bad inputs, and called methods' errors.
     */
    public void createEventTemplate() throws Exception {

        String projectName = usage.getProjectName();
        String sampleName = usage.getSampleName();
        String eventName = usage.getTemplateEventName();
        String outputPath = usage.getOutputLocation();
        String userName = usage.getUsername();
        String passWord = usage.getPassword();
        String server = usage.getServerUrl();

        // Must fetch the list of attributes applying to the event type.
        FileWriter fw = null;
        try {
            PresentationActionDelegate delegate = new PresentationActionDelegate();
            ProjectSampleEventPresentationBusiness ejb = delegate.getEjb(
                    PresentationActionDelegate.EJB_NAME, server, userName, passWord, logger );

            Project project = ejb.getProject( projectName );
            List<EventMetaAttribute> emaList = ejb.getEventMetaAttributes( project.getProjectName(), eventName );
            ModelValidator validator = new ModelValidator();
            validator.validateEventTemplateSanity( emaList, projectName, sampleName, eventName );
            if ( sampleName == null  ||  sampleName.trim().length() == 0 ) {
                // No sample name provided.
                // Need a sample?
                if ( ejb.isSampleRequired( projectName, eventName ) ) {
                    throw new IllegalArgumentException(
                            "Sample is required for this template. Therefore, please provide a sample name."
                    );
                }
            }
            else {
                // User provided a sample name.
                // Does the sample go with the project?
                List<Sample> samples = ejb.getSamplesForProject( project.getProjectId() );
                boolean found = false;
                for ( Sample sample: samples ) {
                    if ( sample.getSampleName().equals( sampleName ) ) {
                        found = true;
                    }
                }
                if ( ! found ) {
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

            System.out.println("Your template file has been created, and is located at " + templateFile.getAbsolutePath() );

        } catch ( Exception ex ) {
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

        EventLoader loader = new EventLoader();
        String eventFileName = usage.getInputFilename();
        String eventType = usage.getEventType();
        try {
            BeanWriter writer = new BeanWriter( serverUrl, userName, passWord, loader );

            File eventFile = new File( eventFileName );
            writer.writeEvent( eventFile, eventType );

        } catch ( Exception ex ) {
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
        EventLoader loader = new EventLoader();
        String multipartFileName = usage.getMultipartInputfileName();
        File file = new File( multipartFileName );

        // Must break this file up, and deposit it into a temporary output directory.
        String userBase = System.getProperty("user.home");
        ScratchUtils.setScratchBaseLocation( userBase + "/" + Constants.SCRATCH_BASE_LOCATION );
        Long timeStamp = new Date().getTime();
        File scratchLoc = ScratchUtils.getScratchLocation( timeStamp, "LoadingEngine__" + file.getName() );
        CombinedFileSplitter splitter = new CombinedFileSplitter();
        splitter.process( file, scratchLoc );

        // Now, will look for files in the directory of the types needed.
        FileCollector collector = new FileCollector( scratchLoc );
        try {
            BeanWriter writer = new BeanWriter( serverUrl, userName, passWord, loader );
            writer.writeMultiType( collector );

        } catch ( Exception ex ) {
            throw ex;
        } finally {
            if ( scratchLoc != null  &&  scratchLoc.exists() ) {
                // The scratch directory may be within a numeric "unique id" directory.  That should
                // go away, too.
                File cleanupDir = scratchLoc;
                String parentDirName = cleanupDir.getParentFile().getName();
                try {
                    Long.parseLong( parentDirName );
                    cleanupDir = cleanupDir.getParentFile();
                } catch ( NumberFormatException nf ) {
                    // Do nothing. Just use the original.
                }
                splitter.removeDirectory( cleanupDir );
            }
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
        EventLoader loader = new EventLoader();
        File file = new File( inputFilePathStr );

        try {
            BeanWriter writer = new BeanWriter(serverUrl, userName, passWord, loader);
            InputBeanType inputBeanType = InputBeanType.getInputBeanType( inputFilePathStr );
            switch ( inputBeanType ) {
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
                    writer.writeEMAs( file );
                    break;
                case projectMetaAttributes:
                    writer.writePMAs( file );
                    break;
                case eventAttributes:
                    writer.writeEvents( file );
                    break;
                default:
                    throw new IllegalArgumentException(
                            inputFilePathStr + " is not named suitably for this application.  No data loaded."
                    );
            }
        } catch ( Exception ex ) {
            throw ex;
        }


    }

    /**
     * Find the class that should be used to read data of the type of bean given.
     */
    private Class decodeBeanClass(InputBeanType inputBeanType) {
        Class beanClass = null;
        switch( inputBeanType ) {
            case project:
                beanClass = Project.class;
                break;
            case sample:
                beanClass = Sample.class;
                break;
            case lookupValue:
                beanClass = LookupValue.class;
                break;
            case sampleMetaAttributes:
                beanClass = SampleMetaAttribute.class;
                break;
            case projectMetaAttributes:
                beanClass = ProjectMetaAttribute.class;
                break;
            case eventMetaAttribute:
                beanClass = EventMetaAttribute.class;
                break;
            case eventAttributes:
                beanClass = EventAttribute.class;
                break;
            default:
                break;
        }
        return beanClass;
    }

}
