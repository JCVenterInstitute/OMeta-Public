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

import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.model.*;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 6/19/11
 * Time: 1:07 AM
 *
 * EJB will implement this for all readable information, for subsequent presentation.
 */
public interface ProjectSampleEventPresentationBusiness {
    public Actor getActor(Long loginId) throws Exception;

    public Actor getActorByUserName(String loginName) throws Exception;

    public String isUserAdmin(String loginName) throws Exception;

    public List<Actor> getAllActor() throws Exception;

    public List<ActorGroup> getActorGroup(Long actorId) throws Exception;

    public Project getProject(String projectName) throws Exception;

    public Project getProject(Long projectId) throws Exception;

    public List<Project> getProjects(List<String> projectNames) throws Exception;

    public List<Project> getProjectsByPublicFlag(boolean isPublic) throws Exception;

    public Boolean isSampleRequired( String projectName, String eventName ) throws Exception;

    public Sample getSample(Long sampleId) throws Exception;

    public Sample getSample(Long projectId, String sampleName) throws Exception;

    public List<Sample> getChildSamples(Long sampleId) throws Exception;

    /**
     * Go and get an id for a project.  Most objects refer to a project by an ID.
     *
     * @throws Exception for called.
     */
    public Long getProjectId(String projectName) throws Exception;

    /**
     * Will pull in the list of project attributes used by a project.  Converts project name to id.
     *
     * @param projectName name of proj.
     */
    public List<ProjectMetaAttribute> getProjectMetaAttributes(String projectName) throws Exception;

    public List<ProjectMetaAttribute> getProjectMetaAttributes(Long projectId) throws Exception;

    public List<ProjectMetaAttribute> getProjectMetaAttributes(List<Long> projectIds) throws Exception;

    public List<ProjectMetaAttribute> getUniqueProjectMetaAttributes() throws Exception;

    public List<ProjectAttribute> getProjectAttributes(String projectName) throws Exception;

    public List<ProjectAttribute> getProjectAttributes(Long projectId) throws Exception;

    public List<ProjectAttribute> getProjectAttributes(List<Long> projectIds) throws Exception;

    public List<SampleMetaAttribute> getSampleMetaAttributes(Long projectId) throws Exception;

    public List<SampleMetaAttribute> getSampleMetaAttributes(List<Long> projectIds) throws Exception;

    public List<SampleMetaAttribute> getUniqueSampleMetaAttributes() throws Exception;

    public List<Sample> getSamplesForProject(String projectName) throws Exception;

    public List<Sample> getSamplesForProject(Long projectId) throws Exception;

    public List<Sample> getSamplesForProjectBySearch(Long projectId, String sampleVal,int firstResult, int maxResult) throws Exception;

    public Integer getSampleCountForProjectBySearch(Long projectId, String sampleVal) throws Exception;

    public List<Sample> getSamplesForProjects(List<Long> projectIds) throws Exception;

    public List<Sample> getSamplesForProjectByPublicFlag(Long projectId, boolean isPublic) throws Exception;

    public List<Sample> getAllSamples(Long flexId, String type, String sSearch, String sortCol, String sortDir, List<String> columnName, List<String> columnSearchArguments) throws Exception;

    public List<Sample> getAllSamplesBySearch(String projectIds, String attributeNames, String sSearch, String sortType,
                                              String sortCol, String sortDir, List<String> columnName, List<String> columnSearchArguments) throws Exception;

    public List<SampleAttribute> getSampleAttributes(String sampleName) throws Exception;

    public List<SampleAttribute> getSampleAttributes(Long sampleId) throws Exception;

    /** Get all attributes associated with a list of samples, by ID. */
    public List<SampleAttribute> getSampleAttributes(List<Long> sampleIds) throws Exception;

    public List<Event> getEventsForProject(String projectName) throws Exception;

    public List<Event> getEventsForProject(Long projectId) throws Exception;

    public List<Event> getEventsForProjects(List<Long> projectIds) throws Exception;

    public List<Event> getAllEvents(Long flexId, String type, String sSearch, String sortCol, String sortDir, int start, int count,
                                    String fromd, String tod, List<String> columnName, List<String> columnSearchArguments) throws Exception;

    public List<Event> getEventsForSample(Long sampleId) throws Exception;

    public List<Event> getEventsForSamples(List<Long> sampleIds) throws Exception;

    public List<Event> getEventByType(Long projectId, Long eventTypeId) throws Exception;

    public List<Event> getEventByTypeAndSample(Long sampleId, Long eventTypeId) throws Exception;

    public List<Event> getEventByLookupValue(Long lookupValueId, String lookupValueStr) throws Exception;

    public Event getLatestEventForSample(Long projectId, Long sampleId, Long eventTypeId) throws Exception;

    public List<Event> getUniqueEventTypes() throws Exception;

    public List<LookupValue> getEventTypesForProject(Long projectId) throws Exception;

    public List<EventAttribute> getEventAttributes(Long eventId, Long projectId) throws Exception;

    public List<EventAttribute> getEventAttributes(List<Long> eventIds, Long projectId) throws Exception;

    public List<EventMetaAttribute> getEventMetaAttributes(Long projectId) throws Exception;

    public List<EventMetaAttribute> getEventMetaAttributes(Long projectId, Long evnetTypeId) throws Exception;

    public List<EventMetaAttribute> getEventMetaAttributes(List<Long> projectIds) throws Exception;

    public List<EventMetaAttribute> getUniqueEventMetaAttributes() throws Exception;

    public LookupValue getLookupValue(String name, String type) throws Exception;

    public Event getEvent(Long eventId) throws Exception;

    public List<EventMetaAttribute> getEventMetaAttributes( String projectName, String eventTypeName ) throws Exception;

    public List<String> getAuthorizedProjectNames( List<String> names, String username, ResponseToFailedAuthorization failureResponse, AccessLevel accessLevel, QueryEntityType queryEntityType ) throws Exception;

    public List<Project> getAuthorizedProjects( String username, AccessLevel accessLevel ) throws Exception;

    public List<Project> getChildProjects( Long projectId ) throws Exception;

    public List<Group> getAllGroup() throws Exception;

    public List<LookupValue> getLookupValueByType(String type) throws Exception;
}
