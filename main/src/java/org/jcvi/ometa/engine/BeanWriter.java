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
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackRemote;
import org.jcvi.ometa.configuration.EventLoader;
import org.jcvi.ometa.configuration.FileMappingSupport;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.UploadActionDelegate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/2/11
 * Time: 4:18 PM
 *
 * Takes care of specifics to type of data.
 */
public class BeanWriter {
    private EventLoader loader;
    private ProjectSampleEventWritebackBusiness pseEjb;
    private Logger logger = Logger.getLogger( BeanWriter.class );

    /** Construct with all stuff needed for subsequent calls. */
    public BeanWriter( String server, String userName, String password, EventLoader loader ) {
        this.loader = loader;
        UploadActionDelegate delegate = new UploadActionDelegate();
        pseEjb = (ProjectSampleEventWritebackRemote)delegate.getEjb( UploadActionDelegate.EJB_NAME, server, userName, password, logger );
    }

    public void writePMAs( File... files ) throws Exception {
        for ( File file: files ) {
            List<ProjectMetaAttribute> pmaBeans = loader.getGenericModelBeans( file, ProjectMetaAttribute.class );
            pseEjb.loadProjectMetaAttributes(pmaBeans);

        }
    }

    public void writeEMAs( File... files ) throws Exception {
        for ( File file: files ) {
            List<EventMetaAttribute> emaBeans = loader.getGenericModelBeans( file, EventMetaAttribute.class );
            pseEjb.loadEventMetaAttributes(emaBeans);

        }
    }

    public void writeSMAs( File... files ) throws Exception {
        for ( File file: files ) {
            List<SampleMetaAttribute> smaBeans = loader.getGenericModelBeans( file, SampleMetaAttribute.class );
            pseEjb.loadSampleMetaAttributes(smaBeans);

        }
    }

    public void writeLookupValues( File... files ) throws Exception {
        for ( File file: files ) {
            List<LookupValue> lvBeans = loader.getGenericModelBeans( file, LookupValue.class );
            pseEjb.loadLookupValues(lvBeans);

        }
    }

    public void writeSamples( File... files ) throws Exception {
        for ( File file: files ) {
            List<Sample> sBeans = loader.getGenericModelBeans( file, Sample.class );
            pseEjb.loadSamples(sBeans);

        }
    }

    public void writeProjects( File... files ) throws Exception {
        for ( File file: files ) {
            List<Project> pBeans = loader.getGenericModelBeans(file, Project.class);
            pseEjb.loadProjects(pBeans);

        }
    }

    public void writeEvents( File... files) throws Exception {
        for ( File file: files ) {
            List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans( file );
            String eventName = getEventName(file.getName());
            pseEjb.loadAttributes(attributeBeans, eventName);

        }
    }

    public void writeEvent( File eventFile, String eventName ) throws Exception {
        List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans(eventFile);
        pseEjb.loadAttributes(attributeBeans, eventName);
    }

    /**
     * Writes back multiple objects of assorted type, rather than a single type of file.
     *
     * @param collector source for all the different types of files.
     * @throws Exception for called methods.
     */
    public void writeMultiType( FileCollector collector ) throws Exception {
        MultiLoadParameter parameterObject = createMultiLoadParameter(collector);
        List<String> projectsToSecure = getProjectsToSecure( parameterObject );
        pseEjb.loadAll( projectsToSecure, parameterObject );

    }

    /** Get the name of the event, from the input file name. */
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

    /**
     * Builds a parameter object to be sent to EJB to load all files in the collection.
     *
     * @param collector has a collection of files that can be separated in order.
     * @return parameter that has the files' contents bundled and separated.
     * @throws Exception thrown by called methods.
     */
    private MultiLoadParameter createMultiLoadParameter( FileCollector collector ) throws Exception {
        List<File> files = null;

        MultiLoadParameter parameterObject = new MultiLoadParameter();
        files = collector.getLookupValueFiles();
        for ( File file: files ) {
            List<LookupValue> lvBeans = loader.getGenericModelBeans( file, LookupValue.class );
            parameterObject.addLookupValues( lvBeans );
        }

        files = collector.getProjectFiles();
        for ( File file: files ) {
            List<Project> pBeans = loader.getGenericModelBeans( file, Project.class );
            parameterObject.addProjects( pBeans );
        }

        files = collector.getSampleFiles();
        for ( File file: files ) {
            List<Sample> sBeans = loader.getGenericModelBeans( file, Sample.class );
            parameterObject.addSamples( sBeans );
        }

        files = collector.getProjectMetaAttributeFiles();
        for ( File file: files ) {
            List<ProjectMetaAttribute> pmaBeans = loader.getGenericModelBeans( file, ProjectMetaAttribute.class );
            parameterObject.addProjectMetaAttributes( pmaBeans );
        }

        files = collector.getSampleMetaAttributeFiles();
        for ( File file: files ) {
            List<SampleMetaAttribute> smaBeans = loader.getGenericModelBeans( file, SampleMetaAttribute.class );
            parameterObject.addSampleMetaAttributes( smaBeans );
        }

        files = collector.getEventMetaAttributeFiles();
        for ( File file: files ) {
            List<EventMetaAttribute> emaBeans = loader.getGenericModelBeans( file, EventMetaAttribute.class );
            parameterObject.addEventMetaAttributes( emaBeans );
        }

        // Finally, the events.
        files = collector.getProjectRegistrationFiles();
        for ( File file: files ) {
            List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans( file );
            String eventName = getEventName( file.getName() );
            parameterObject.addProjectRegistrations( eventName, attributeBeans );
        }

        files = collector.getSampleRegistrationFiles();
        for ( File file: files ) {
            List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans( file );
            String eventName = getEventName( file.getName() );
            parameterObject.addSampleRegistrations( eventName, attributeBeans );
        }

        files = collector.getEventFiles();
        for ( File file: files ) {
            List<FileReadAttributeBean> attributeBeans = loader.getGenericAttributeBeans( file );
            String eventName = getEventName( file.getName() );
            parameterObject.addEvents( eventName, attributeBeans );
        }

        return parameterObject;
    }

    /** Get all project names of projects encountered in this multi-file.  Exclude any that are newly-creating. */
    private List<String> getProjectsToSecure( MultiLoadParameter parameter ) {

        Set<String> projectsToSecure = new HashSet<String>();
        //Do not bother with projects newly-created.
        Set<String> exclusionSet = new HashSet<String>();
        if ( parameter.getProjects() != null ) {
            for ( List<Project> projects: parameter.getProjects() ) {
                for ( Project project: projects ) {
                    exclusionSet.add( project.getProjectName().intern() );
                }
            }
        }

        //Do bother with everything NOT on that list.
        if ( parameter.getSamples() != null ) {
            for ( List<Sample> samples: parameter.getSamples() ) {
                for ( Sample sample: samples ) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, sample);
                }
            }
        }

        if ( parameter.getPmas() != null ) {
            for ( List<ProjectMetaAttribute> pmas: parameter.getPmas() ) {
                for ( ProjectMetaAttribute pma: pmas ) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, pma);
                }
            }
        }

        if ( parameter.getSmas() != null ) {
            for ( List<SampleMetaAttribute> smas: parameter.getSmas() ) {
                for ( SampleMetaAttribute sma: smas ) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, sma);
                }
            }
        }

        if ( parameter.getEmas() != null ) {
            for ( List<EventMetaAttribute> emas: parameter.getEmas() ) {
                for ( EventMetaAttribute ema: emas ) {
                    addNonExcludedProjects( projectsToSecure, exclusionSet, ema );
                }
            }
        }

        if ( parameter.getSampleRegistrationEventAttributes() != null ) {
            for ( List<FileReadAttributeBean> eas: parameter.getSampleRegistrationEventAttributes() ) {
                for ( FileReadAttributeBean ea: eas ) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, ea);
                }
            }
        }

        if ( parameter.getOtherEvents() != null ) {
            for ( MultiLoadParameter.LoadableEventBean eventBean: parameter.getOtherEvents() ) {
                for ( FileReadAttributeBean attribute: eventBean.getAttributes() ) {
                    addNonExcludedProjects(projectsToSecure, exclusionSet, attribute );
                }
            }
        }

        List<String> rtnList = new ArrayList<String>();
        rtnList.addAll( projectsToSecure );
        return rtnList;
    }

    private void addNonExcludedProjects(
            Set<String> projectsToSecure, Set<String> exclusionSet, ProjectNamerOnFileRead pnamer ) {
        String projectName = pnamer.getProjectName().intern();
        if ( ! exclusionSet.contains( projectName ) ) {
            projectsToSecure.add(projectName);
        }
    }

}
