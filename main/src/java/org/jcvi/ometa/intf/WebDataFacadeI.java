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

package org.jcvi.ometa.intf;

import org.jcvi.ometa.model.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/14/11
 * Time: 2:45 PM
 *
 * Supports presentation data fetch.  Note that Jave EE is meant to support authentication, and no "roles" are
 * anticipated for accessing specific rows of data, so no authorization is needed, and indeed no user identity
 * need be represented at this layer.
 */
public interface WebDataFacadeI {

    String SESSION_FACTORY_NAME_PROP = "BeanPersistenceFacade.session_factory_name";
    String PROPERTIES_FILE_NAME = "resource/LoadingEngine";

    /**
     * Given a project name known to the user, grab all its latest-date attributes.
     *
     * @param projectName name of project.
     * @return all attributes with latest date per name.
     * @throws Exception
     */
    List<ProjectAttribute> getProjectAttributes( String projectName ) throws Exception;

    List<ProjectMetaAttribute> getProjectMetaAttributes( Long projectId ) throws Exception;

    /**
     * Given a sample name known to the user, grab all its latest-date attributes.
     *
     * @param projectName tell all samples used by the ID.
     * @return all attributes at latest date by name.
     */
    List<Sample> getSamplesForProject( String projectName ) throws Exception;

    /**
     * Gives attributes of the chosen sample.
     *
     * @param projectName get attributes of this.
     * @return latest attribute by name.
     * @throws Exception
     */
    List<SampleAttribute> getSampleAttributes( String projectName ) throws Exception;

    List<SampleMetaAttribute> getSampleMetaAttributes( Long projectId ) throws Exception;

    /**
     * Tells event objects against project--latest data for name.
     *
     * @param projectName which project to get events on.
     * @return list of events--latest by name.
     * @throws Exception
     */
    List<Event> getEventsForProject( String projectName ) throws Exception;

}
