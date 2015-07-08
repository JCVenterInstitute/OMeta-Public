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

package org.jcvi.ometa.bean_interface;

import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.model.*;

import java.util.List;

/**
 * Copyright J. Craig Venter Institute, 2011
 * User: lfoster
 * Date: 4/25/11
 * Time: 11:12 AM
 *
 * Implement this to fulfill the contract for project/sample tracking (with Events).
 */
public interface ProjectSampleEventWritebackBusiness {
    void loadActor(Actor actor) throws Exception;
    void updateActor(Actor actor) throws Exception;

    void loadActorGroup(List<ActorGroup> actorGroups) throws Exception;
    void deleteActorGroup(List<ActorGroup> actorGroups) throws Exception;

    void loadGroups(List<Group> groups) throws Exception;
    /**
     * Implement this to load one+ files representing new projects.
     *
     * @param beans projects to be loaded.
     * @return number of files written.
     */
    int loadProjects( List<Project> beans ) throws Exception;

    /**
     * Implement this to load one+ files representing new samples.
     *
     * @param samples to be loaded
     * @return number of files written.
     */
    int loadSamples( List<Sample> samples ) throws Exception;

    /**
     * Implement this to load one+ files representing new lookup values.
     *
     * @param beans lookup values to be loaded.
     * @return number of files written.
     */
    int loadLookupValues( List<LookupValue> beans ) throws Exception;

    /**
     * Implement this to load one+ files representing new sample attribute descriptor rows.
     *
     * @param beans to be loaded.
     * @return number of files written.
     */
    int loadSampleMetaAttributes( List<SampleMetaAttribute> beans ) throws Exception;

    /**
     * Implement this to load one+ files representing new event attribute descriptor rows.
     *
     * @param beans to be loaded.
     * @return number of files written.
     */
    int loadEventMetaAttributes( List<EventMetaAttribute> beans ) throws Exception;

    /**
     * Implement this to load one+ files representing new project attribute descriptor rows.
     *
     * @param beans to be loaded.
     * @return number of files written.
     */
    int loadProjectMetaAttributes( List<ProjectMetaAttribute> beans ) throws Exception;

    /**
     * Implement this to load one+ files representing events.  Files must be named properly to
     * indicate the types of events being uploaded.
     *
     * @param attributes to be loaded.
     * @return number of files written.
     */
    int loadAttributes( List<FileReadAttributeBean> attributes, String eventName ) throws Exception;

    /**
     * Takes all the contained objects (all types of model beans) in the parameter object, and loads
     * them in the proper order to avoid database inconsistencies.
     *
     * @param projectNames all projects which _should_ be checked for access when this method runs.
     * @param parameterObject contains lookup values, events, samples, etc.
     * @throws Exception for called methods.
     */
    void loadAll( List<String> projectNames, MultiLoadParameter parameterObject ) throws Exception;

    /**
     * takes event object with its values are updated and loads it.
     *
     * @param event event object to update.
     * @throws Exception for called methods.
     */
    void updateEventStatus( Event event ) throws Exception;

    void updateProject(Project projects) throws Exception;

    void updateSample(Sample sample) throws Exception;

    void loadDictionaryWithDependency(String dictType, String dictValue, String dictCode, String parentDictTypeCode) throws Exception;

    void loadDictionary(String dictType, String dictValue, String dictCode) throws Exception;
}
