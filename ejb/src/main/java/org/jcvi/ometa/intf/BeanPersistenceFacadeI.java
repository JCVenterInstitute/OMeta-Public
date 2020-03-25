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
 * Time: 2:42 PM
 *
 * Implement this to support persisting of data for project websites.
 */
public interface BeanPersistenceFacadeI {
    String SESSION_FACTORY_NAME_PROP = "BeanPersistenceFacade.session_factory_name";

    void open() throws Exception;
    void close();
    void error();

    void writeBackActor(Actor actor) throws Exception;

    void writeBackLookupValues(List<LookupValue> lBeans ) throws Exception;

    void writeBackDictionaries(List<Dictionary> dictBeans ) throws Exception;

    void writeBackProjects(List<Project> pBeans, String actorUserName) throws Exception;

    void writeBackSamples(List<Sample> sBeans, String actorUserName) throws Exception;

    void writeBackProjectMetaAttributes(List<ProjectMetaAttribute> pmaBeans, String actorUserName) throws Exception;

    void writeBackEventMetaAttributes(List<EventMetaAttribute> emaBeans, String actorUserName) throws Exception;

    void writeBackSampleMetaAttributes(List<SampleMetaAttribute> smaBeans, String actorUserName) throws Exception;

    void writeBackAttributes(List<FileReadAttributeBean> aBeans, String eventName, String actorUserName) throws Exception;

    void updateActor(Actor actor) throws Exception;

    void updateEventStatus(Event event, String actorUserName) throws Exception;

    void updateProject(Project project, String actorUserName) throws Exception;

    void updateSample(Sample sample, String actorUserName) throws Exception;

    void writeBackActorGroup(List<ActorGroup> actorGroups) throws Exception;

    void deleteActorGroup(List<ActorGroup> actorgroups) throws Exception;

    void deleteSMA(SampleMetaAttribute sma) throws Exception;

    void deletePMA(ProjectMetaAttribute pma) throws Exception;

    void writeBackGroups(List<Group> groups) throws Exception;

    void loadDictionaryWithDependency(String dictType, String dictValue, String dictCode, String parentDictTypeCode) throws Exception;

    void loadDictionary(String dictType, String dictValue, String dictCode) throws Exception;
}
