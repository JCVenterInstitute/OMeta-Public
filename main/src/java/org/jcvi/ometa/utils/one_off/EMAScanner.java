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

package org.jcvi.ometa.utils.one_off;

import org.jcvi.ometa.configuration.EventLoader;
import org.jcvi.ometa.configuration.FileMappingSupport;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.db_interface.WritebackBeanPersister;
import org.jcvi.ometa.engine.FileCollector;
import org.jcvi.ometa.hibernate.dao.SessionAndTransactionManagerI;
import org.jcvi.ometa.hibernate.dao.StandaloneSessionAndTransactionManager;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CombinedFileSplitter;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/13/11
 * Time: 5:25 PM
 *
 * Scans to produce Event Meta Attributes for all project and sample Meta Attributes to ensure
 * that events can be properly processed, going forward.
 */
public class EMAScanner {
    private static final String USAGE = "Usage: java " + EMAScanner.class.getName() +
    " <directory-with-event-files> <new-event-meta-attribute-file> <db-environment:(" +
            Constants.DEVELOPMENT_DATABASE + "|" + Constants.PRODUCTION_DATABASE + ")>";
    private static final String KEY_SEPARATOR = "^";

    private String userBase;
    private EventLoader loader;

    public static void main( String[] args ) {
        if ( args.length < 3 ) {
            System.out.println( USAGE );
        }
        else {
            File baseDir = new File( args[ 0 ] );
            if (! baseDir.canRead()  ||  ! baseDir.isDirectory() ) {
                throw new IllegalArgumentException("Cannot read directory " + baseDir.getAbsolutePath() );
            }
            File outputFile = new File( args[ 1 ] );
            if ( ! outputFile.getParentFile().canWrite() ) {
                throw new IllegalArgumentException("Cannot write file " + outputFile.getAbsolutePath() );
            }
            if ( ! outputFile.getName().endsWith( FileMappingSupport.EVENT_META_ATTRIBUTES_FILE_SUFFIX ) ) {
                throw new IllegalArgumentException(
                        "File " + outputFile.getName() +
                        " will not be usable, since it does not end with " +
                        FileMappingSupport.EVENT_META_ATTRIBUTES_FILE_SUFFIX );
            }
            String environment = args[ 2 ];
            if ( ! (environment.equals( Constants.PRODUCTION_DATABASE )  ||
                    environment.equals( Constants.DEVELOPMENT_DATABASE ) ) ) {
                throw new IllegalArgumentException(
                        "Database environment " + environment + " illegal.  See usage: " + USAGE );
            }

            EMAScanner scanner = new EMAScanner();
            scanner.execute( baseDir, outputFile, environment );

        }
    }

    public EMAScanner() {
        userBase = System.getProperty("user.home");
        ScratchUtils.setScratchBaseLocation(userBase + "/" + Constants.SCRATCH_BASE_LOCATION);
        loader = new EventLoader();
    }

    public void execute( File baseDirectory, File outputFile, String environment ) {
        try {
            List<EventAttributeInfo> collection = new ArrayList<EventAttributeInfo>();
            recursiveCollect( baseDirectory, collection );

            writeFileOfEventMetaAttributes( collection, outputFile, environment );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    private void recursiveCollect( File f, Collection<EventAttributeInfo> collection ) throws Exception {
        if ( f.isDirectory() ) {
            File[] contents = f.listFiles(new FileFilter() {
                public boolean accept(File memberFile) {
                    String fileName = memberFile.getName();
                    return fileName.endsWith(".tsv") ||  memberFile.isDirectory();
                }
            });
            for ( File memberFile: contents ) {
                recursiveCollect( memberFile, collection );
            }
        }
        else if ( f.getName().endsWith( FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX ) ) {
            collectAttributes( f, collection );
        }
        else if ( f.getName().endsWith( ".tsv" ) ) {
            // Figure out: is this going to be multi or single-file TSV?  If multi, it could __contain__
            // some attributes to be handled.
            if ( isMulti( f ) ) {
                File scratchLoc = ScratchUtils.getScratchLocation( "EMAScanner__" + f.getName() );
                scratchLoc.deleteOnExit();
                CombinedFileSplitter splitter = new CombinedFileSplitter();
                splitter.process( f, scratchLoc );

                FileCollector collector = new FileCollector( scratchLoc );
                for ( File eventFile: collector.getEventFiles() ) {
                    collectAttributes( eventFile, collection );
                }
                for ( File eventFile: collector.getProjectRegistrationFiles() ) {
                    collectAttributes( eventFile, collection );
                }
                for ( File eventFile: collector.getSampleRegistrationFiles() ) {
                    collectAttributes( eventFile, collection );
                }
            }
        }
    }

    /** Write back a single file with all of the meta attributes. */
    private void writeFileOfEventMetaAttributes(
            Collection<EventAttributeInfo> collection, File outputFile, String environment )
        throws Exception {

        // Whittle down the collection to only those not already in db.
        // Need to get all the event types
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);

        WritebackBeanPersister writePersister = new WritebackBeanPersister(
                props, new StandaloneSessionAndTransactionManager(props, environment ));
        writePersister.open();

        // Make a map of all project/event vs associated event meta attributes.  Need this to tell if the EMA
        // has already been addeed to the database.
        Map<String,List<EventMetaAttribute>> projectEventVsEMAs = new HashMap<String,List<EventMetaAttribute>>();
        Set<String> allKeys = new HashSet<String>();
        Set<String> allProjectNames = new HashSet<String>();
        for ( EventAttributeInfo bean: collection ) {
            allProjectNames.add( bean.getProjectName() );
            allKeys.add( formProjectEventKey( bean ) );
        }
        for ( String key: allKeys ) {
            String[] projEvt = key.split( "\\" + KEY_SEPARATOR );
            if ( projEvt.length < 2 ) {
                System.out.println("Odd Key /" + key + "/");
            }
            projectEventVsEMAs.put(
                    key,
                    writePersister.getEventMetaAttributes(
                            projEvt[0],
                            projEvt[1]
                    )
            );

        }
        writePersister.close();

        // Now leveraging the read-persister.
        SessionAndTransactionManagerI readSessionAndTransactionManager =
                new StandaloneSessionAndTransactionManager(props, environment );
        ReadBeanPersister readPersister = new ReadBeanPersister(
                props);

        // Make a map of all project vs meta attributes.  Make a map of all project+sample vs meta attributes.
        // Need these to verify that the event attribute should affect a project attribute, or a sample attribute,
        // and to get the additional "dataType" information.
        List<String> allProjectNameList = new ArrayList<String>();
        allProjectNameList.addAll( allProjectNames );
        Map<Long,String> projectIdToProjectName = new HashMap<Long,String>();
        List<Project> allProjects = readPersister.getProjects( allProjectNameList );
        List<Long> allProjectIds = new ArrayList<Long>();
        for ( Project p: allProjects ) {
            projectIdToProjectName.put( p.getProjectId(), p.getProjectName() );
            allProjectIds.add( p.getProjectId() );
        }
        Map<String,List<ProjectMetaAttribute>> projectVsPMAs = new HashMap<String,List<ProjectMetaAttribute>>();
        Map<String,List<SampleMetaAttribute>> projectVsSMAs = new HashMap<String,List<SampleMetaAttribute>>();

        List<ProjectMetaAttribute> pmas = readPersister.getProjectMetaAttributes(allProjectIds);
        for ( ProjectMetaAttribute pma: pmas ) {
            String projectName = projectIdToProjectName.get( pma.getProjectId() );
            List<ProjectMetaAttribute> pmasForProject =  projectVsPMAs.get( projectName );
            if ( pmasForProject == null ) {
                pmasForProject = new ArrayList<ProjectMetaAttribute>();
                projectVsPMAs.put(projectName, pmasForProject);
            }
            pmasForProject.add(pma);

        }
        List<SampleMetaAttribute> smas = readPersister.getSampleMetaAttributes(allProjectIds);
        for ( SampleMetaAttribute sma: smas ) {
            String projectName = projectIdToProjectName.get( sma.getProjectId() );

            List<SampleMetaAttribute> smasForProject =  projectVsSMAs.get( projectName );
            if ( smasForProject == null ) {
                smasForProject = new ArrayList<SampleMetaAttribute>();
                projectVsSMAs.put(projectName, smasForProject);
            }
            smasForProject.add( sma );
        }

        PrintWriter pw = new PrintWriter( new FileWriter( outputFile ) );
        pw.println("ProjectName\tEventName\tRequired\tAttributeName\tDataType\tAttributeDescription\tPossibleValues");

        // Go through all the beans in the vast collection, finding those which have the same project and event name,
        // but which are not yet in the database.  Write each one to file only once.
        Set<EventAttributeInfo> beansToBeWritten = new HashSet<EventAttributeInfo>();
        for ( EventAttributeInfo bean: collection ) {
            // Bail if nothing to be done.
            // Only write each out once.
            boolean alreadyWrittenOut = beansToBeWritten.contains( bean );
            if ( alreadyWrittenOut ) {
                continue;
            }

            // Check: is this already represented in database?
            String eventName = bean.getEventName();
            String key = formProjectEventKey( bean );
            List<EventMetaAttribute> emas = projectEventVsEMAs.get( key );
            boolean inDbAlready = false;
            for ( EventMetaAttribute ema: emas ) {
                if ( ema.getEventName().equals( eventName ) ) {
                    inDbAlready = true;
                }
            }

            // Bail if it has already been done.
            if ( inDbAlready ) {
                continue;
            }

            // Ones to be written must be either Project or Sample meta attributes.  Need to get additional info
            // from the existing descriptions.

            String dataType = null;
            String description = null;
            String possibleValues = null;

            List<ProjectMetaAttribute> pmaList = projectVsPMAs.get( bean.getProjectName() );
            boolean isProjectMetaAttribute = false;

            if ( pmaList != null ) {
                for ( ProjectMetaAttribute pma: pmaList ) {
                    if ( pma.getAttributeName().equals( bean.getAttributeName() ) ) {
                        isProjectMetaAttribute = true;
                        dataType = pma.getDataType();
                        description = pma.getDesc();
                        possibleValues = pma.getOptions();
                    }
                }
            }

            List<SampleMetaAttribute> smaList = projectVsSMAs.get( bean.getProjectName() );
            boolean isSampleMetaAttribute = false;

            if ( smaList != null ) {
                for ( SampleMetaAttribute sma: smaList ) {
                    if ( sma.getAttributeName().equals( bean.getAttributeName() )) {
                        isSampleMetaAttribute = true;
                        dataType = sma.getDataType();
                        description = sma.getDesc();
                        possibleValues = sma.getOptions();
                    }
                }

            }

            if ( isProjectMetaAttribute  ||  isSampleMetaAttribute ) {
                if ( possibleValues == null ) {
                    possibleValues = "";
                }
                if ( description == null ) {
                    description = "";
                }
                if ( dataType == null ) {
                    throw new Exception(
                            "No data type found for attribute " +
                            bean.getAttributeName() +
                            " in project " + bean.getProjectName() );
                }

                pw.println(
                        bean.getProjectName() + "\t" +
                        bean.getEventName() + "\t" +
                        "T" + "\t" +                     // Setting all of these to required.  Assuming most will be.
                        bean.getAttributeName() + "\t" +
                        dataType + "\t" +
                        description + "\t" +
                        possibleValues
                );   // No description to write.
                beansToBeWritten.add( bean );

            }

        }

        pw.close();

    }

    private String formProjectEventKey( EventAttributeInfo info  ) {
        String projectName = info.getProjectName();
        if ( projectName == null  ||  projectName.length() == 0 ) {
            System.out.println("Empty project name " + info.getAttributeName() + " " + info.getEventName() );
        }
        String eventName = info.getEventName();
        if ( eventName == null  ||  eventName.length() == 0  ) {
            System.out.println("Empty event name " + info.getAttributeName() + " " + info.getProjectName() );
        }
        return formKey(projectName, eventName );
    }

    private String formKey(String... members ) {
        StringBuilder wholeKey = new StringBuilder();
        for ( String member: members ) {
            if ( wholeKey.length() > 0 )
                wholeKey.append( KEY_SEPARATOR );
            wholeKey.append( member );
        }
        return wholeKey.toString();
    }

    /** Get all the attributes out of the event file, to process as needed. */
    private void collectAttributes( File eventFile, Collection<EventAttributeInfo> collection ) throws Exception {
        List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans( eventFile );

        for ( FileReadAttributeBean bean: attributeBeans ) {
            String eventName = getEventName(eventFile.getName());
            String projectName = bean.getProjectName();
            String attributeName = bean.getAttributeName();
            String sampleName = bean.getSampleName();
            EventAttributeInfo info = new EventAttributeInfo( attributeName, projectName, sampleName, eventName );
            collection.add( info );
        }
    }

    /**
     * Get the name of the event, from the input file name.  Borrows from the BeanWriter implementation.
     */
    private String getEventName(String inputFilePathStr) throws Exception {
        int pos = inputFilePathStr.indexOf( FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX );
        String eventName = null;
        if ( pos <= 0  ||  inputFilePathStr.charAt( pos - 1 ) != '_' ) {
            throw new Exception(
                    inputFilePathStr + " ends with " +
                    FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX +
                    " but has no event name prefixing that.");
        }
        else {
            int pos2 = inputFilePathStr.lastIndexOf( "_" );
            int pos3 = pos2 - 1;
            while ( pos3 >= 0  &&  inputFilePathStr.charAt( pos3 ) != '_' ) {
                pos3--;
            }
            if ( pos3 < 0 ) pos3 = 0;
            else pos3 ++;

            eventName = inputFilePathStr.substring( pos3, pos2 );
        }
        return eventName;
    }

    /** Check: is this a multi-part file, or not? */
    private boolean isMulti( File f ) throws IOException {
        BufferedReader br = new BufferedReader( new FileReader( f ) );
        boolean settled = false;
        String inLine = null;
        boolean isMultiFlag = false;
        while ( (! settled)  &&  ( null != (inLine = br.readLine() ) ) ) {
            inLine = inLine.trim();
            if ( inLine.length() > 0 ) {
                settled = true;
                String[] headerFields = inLine.split( "\t" );
                // Single field -> name-of-file.
                if ( headerFields.length == 1 ) {
                    isMultiFlag = true;
                }
            }
        }
        br.close();

        return isMultiFlag;
    }

    /** All required info for any event attribute that is encountered. */
    public static class EventAttributeInfo {
        private String attributeName;
        private String projectName;
        private String eventName;
        private String sampleName;

        public EventAttributeInfo( String attributeName, String projectName, String sampleName, String eventName ) {
            this.attributeName = attributeName;
            this.projectName = projectName;
            this.sampleName = sampleName;
            this.eventName = eventName;
        }


        public String getAttributeName() {
            return attributeName;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getEventName() {
            return eventName;
        }

        public boolean equals( Object obj ) {
            boolean rtnVal = false;
            if ( obj != null  &&
                 obj instanceof EventAttributeInfo ) {
                EventAttributeInfo other = (EventAttributeInfo)obj;

                if ( other.comparatorString().equals( comparatorString() )) {
                    rtnVal = true;
                }
            }

            return rtnVal;
        }

        public int hashCode() {
            return comparatorString().hashCode();
        }

        private String comparatorString() {
            return attributeName + KEY_SEPARATOR + projectName + KEY_SEPARATOR + eventName;
        }
    }
}
