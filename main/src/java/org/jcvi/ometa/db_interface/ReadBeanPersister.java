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

package org.jcvi.ometa.db_interface;

import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.hibernate.dao.SessionAndTransactionManagerI;
import org.jcvi.ometa.intf.WebDataFacadeI;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.stateless_session_bean.ProjectSampleEventPresentationStateless;
import org.jcvi.ometa.utils.PresentationActionDelegate;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/7/11
 * Time: 12:28 PM
 *
 * Unified class to present a single interface for getting all presentation (or "read-only") information
 * regarding projects. Particularly useful for web-based clients.
 */
public class ReadBeanPersister implements WebDataFacadeI {
    private ProjectSampleEventPresentationBusiness pseb;

    public ReadBeanPersister( Properties props ) {
        //pseb = new ProjectSampleEventPresentationStateless();
        pseb = new PresentationActionDelegate().initializeEjb( Logger.getLogger( ReadBeanPersister.class ), null );
    }

    public ReadBeanPersister( SessionAndTransactionManagerI sessionAndTransactionManager ) {
        pseb = new ProjectSampleEventPresentationStateless( sessionAndTransactionManager );
    }

    public ReadBeanPersister(ProjectSampleEventPresentationBusiness pseb) {
        this.pseb = pseb;
    }

    public Actor getActor(Long loginId) throws Exception {
        return pseb.getActor( loginId );
    }

    public Actor getActorByUserName(String loginName) throws Exception {
        return pseb.getActorByUserName(loginName);
    }

    public String isUserAdmin(String loginName) throws Exception {
        return pseb.isUserAdmin(loginName);
    }

    public List<Actor> getAllActor() throws Exception {
        return pseb.getAllActor();
    }

    public List<ActorGroup> getActorGroup(Long actorId) throws Exception {
        return pseb.getActorGroup(actorId);
    }

    public Project getProject(String projectName) throws Exception {
        return pseb.getProject(projectName);
    }

    public Project getProject( long projectId ) throws Exception {
        return pseb.getProject(projectId);
    }

    public List<Project> getProjects(List<String> projectNames) throws Exception {
        return pseb.getProjects(projectNames);
    }

    public List<Project> getProjectsByPublicFlag(boolean isPublic) throws Exception {
        return pseb.getProjectsByPublicFlag(isPublic);
    }

    public Sample getSample(Long sampleId) throws Exception {
        return pseb.getSample(sampleId);
    }

    public Sample getSample(Long projectId, String sampleName) throws Exception {
        return pseb.getSample(projectId, sampleName);
    }

    public List<Sample> getChildSamples(Long sampleId) throws Exception {
        return pseb.getChildSamples(sampleId);
    }

    public Boolean isSampleRequired( String projectName, String eventName ) throws Exception {
        return pseb.isSampleRequired( projectName, eventName );
    }

    /**
     * Go and get an id for a project.  Most objects refer to a project by an ID.
     *
     * @throws Exception for called.
     */
    public Long getProjectId(String projectName) throws Exception {
        return pseb.getProjectId(projectName);
    }

    /**
     * Will pull in the list of project attributes used by a project.  Converts project name to id.
     *
     * @param projectName name of proj.
     */
    public List<ProjectMetaAttribute> getProjectMetaAttributes(String projectName) throws Exception {
        return pseb.getProjectMetaAttributes( projectName );
    }

    public List<ProjectMetaAttribute> getProjectMetaAttributes(Long projectId) throws Exception {
        return pseb.getProjectMetaAttributes(projectId);
    }

    public List<ProjectMetaAttribute> getProjectMetaAttributes(List<Long> projectIds) throws Exception {
        return pseb.getProjectMetaAttributes(projectIds);
    }

    public List<ProjectMetaAttribute> getUniqueProjectMetaAttributes() throws Exception {
        return pseb.getUniqueProjectMetaAttributes();
    }

    @Override
    public List<ProjectAttribute> getProjectAttributes(String projectName) throws Exception {
        return pseb.getProjectAttributes( projectName );
    }

    public List<ProjectAttribute> getProjectAttributes(Long projectId) throws Exception {
        return pseb.getProjectAttributes( projectId );
    }

    public List<ProjectAttribute> getProjectAttributes(List<Long> projectIds) throws Exception {
        return pseb.getProjectAttributes( projectIds );
    }

    public List<SampleMetaAttribute> getSampleMetaAttributes(Long projectId) throws Exception {
        return pseb.getSampleMetaAttributes(projectId);
    }

    public List<SampleMetaAttribute> getSampleMetaAttributes(List<Long> projectIds) throws Exception {
        return pseb.getSampleMetaAttributes(projectIds);
    }

    public List<SampleMetaAttribute> getUniqueSampleMetaAttributes() throws Exception {
        return pseb.getUniqueSampleMetaAttributes();
    }

    @Override
    public List<Sample> getSamplesForProject(String projectName) throws Exception {
        return pseb.getSamplesForProject(projectName);
    }

    public List<Sample> getSamplesForProject(Long projectId) throws Exception {
        return pseb.getSamplesForProject(projectId);
    }

    public List<Sample> getSamplesForProjectBySearch(Long projectId, String sampleVal,int firstResult, int maxResult) throws Exception {
        return pseb.getSamplesForProjectBySearch(projectId, sampleVal, firstResult, maxResult);
    }

    public Integer getSampleCountForProjectBySearch(Long projectId, String sampleVal) throws Exception {
        return pseb.getSampleCountForProjectBySearch(projectId, sampleVal);
    }

    public List<Sample> getSamplesForProjectByPublicFlag(Long projectId, boolean isPublic) throws Exception {
        return pseb.getSamplesForProjectByPublicFlag(projectId, isPublic);
    }

    public List<Sample> getSamplesForProjects(List<Long> projectIds) throws Exception {
        return pseb.getSamplesForProjects(projectIds);
    }

    public List<Sample> getAllSamples(Long flexId, String type, String sSearch, String sortCol, String sortDir, List<String> columnName, List<String> columnSearchArguments) throws Exception {
        return pseb.getAllSamples(flexId, type, sSearch, sortCol, sortDir, columnName, columnSearchArguments);
    }

    public List<Sample> getAllSamples(String projectIds, String attributeNames, String sSearch, String sortType, String sortCol, String sortDir,
                                      List<String> columnName, List<String> columnSearchArguments) throws Exception {
        return pseb.getAllSamplesBySearch(projectIds, attributeNames, sSearch, sortType, sortCol, sortDir, columnName, columnSearchArguments);
    }

    @Override
    public List<SampleAttribute> getSampleAttributes(String sampleName) throws Exception {
        return pseb.getSampleAttributes( sampleName );
    }

    public List<SampleAttribute> getSampleAttributes(Long sampleId) throws Exception {
        return pseb.getSampleAttributes(sampleId);
    }

    /** Get all attributes associated with a list of samples, by ID. */
    public List<SampleAttribute> getSampleAttributes(List<Long> sampleIds) throws Exception {
        return pseb.getSampleAttributes(sampleIds);
    }

    /** Get a specific sample attribute **/
    public SampleAttribute getSampleAttribute(String projectName, String sampleName, String attributeName) throws Exception {
        return pseb.getSampleAttribute(projectName, sampleName, attributeName);
    }

    @Override
    public List<Event> getEventsForProject(String projectName) throws Exception {
        return pseb.getEventsForProject(projectName);
    }

    public List<Event> getEventsForProject(Long projectId) throws Exception {
        return pseb.getEventsForProject(projectId);
    }

    public List<Event> getEventsForProjects(List<Long> projectIds) throws Exception {
        return pseb.getEventsForProjects(projectIds);
    }

    public List<Event> getAllEvents(Long flexId, String type, String sSearch, String sortCol, String sortDir, int start, int count,
                                    String fromd, String tod, List<String> columnName, List<String> columnSearchArguments) throws Exception {
        return pseb.getAllEvents(flexId, type, sSearch, sortCol, sortDir, start, count, fromd, tod, columnName, columnSearchArguments);
    }

    public List<Event> getEventsForSample(Long sampleId) throws Exception {
        return pseb.getEventsForSample(sampleId);
    }

    public List<Event> getEventsForSamples(List<Long> sampleIds) throws Exception {
        return pseb.getEventsForSamples(sampleIds);
    }

    public List<Event> getEventByType(Long projectId, Long eventTypeId) throws Exception {
        return pseb.getEventByType(projectId, eventTypeId);
    }

    public List<Event> getEventByTypeAndSample(Long sampleId, Long eventTypeId) throws Exception {
        return pseb.getEventByTypeAndSample(sampleId, eventTypeId);
    }

    public List<Event> getEventByLookupValue(Long lookupValueId, String lookupValueStr) throws Exception {
        return pseb.getEventByLookupValue(lookupValueId, lookupValueStr);
    }

    public Event getLatestEventForSample(Long projectId, Long sampleId, Long eventTypeId) throws Exception {
        return pseb.getLatestEventForSample(projectId, sampleId, eventTypeId);
    }

    public List<Event> getUniqueEventTypes() throws Exception {
        return pseb.getUniqueEventTypes();
    }

    public List<LookupValue> getEventTypesForProject(Long projectId) throws Exception {
        return pseb.getEventTypesForProject(projectId);
    }

    public List<EventAttribute> getEventAttributes(Long eventId, Long projectId) throws Exception {
        return pseb.getEventAttributes(eventId, projectId);
    }

    public List<EventAttribute> getEventAttributes(List<Long> eventIds, Long projectId) throws Exception {
        return pseb.getEventAttributes(eventIds, projectId);
    }

    public List<EventMetaAttribute> getEventMetaAttributes(Long projectId) throws Exception {
        return pseb.getEventMetaAttributes(projectId);
    }

    public List<EventMetaAttribute> getEventMetaAttributes(Long projectId, Long eventTypeId) throws Exception {
        return pseb.getEventMetaAttributes(projectId, eventTypeId);
    }

    public List<EventMetaAttribute> getEventMetaAttributes(List<Long> projectIds) throws Exception {
        return pseb.getEventMetaAttributes(projectIds);
    }

    public List<EventMetaAttribute> getUniqueEventMetaAttributes() throws Exception {
        return pseb.getUniqueEventMetaAttributes();
    }

    public LookupValue getLookupValue(String name, String type) throws Exception {
        return pseb.getLookupValue( name, type );
    }

    public Event getEvent( Long eventId ) throws Exception {
        return pseb.getEvent( eventId );
    }

    public List<EventMetaAttribute> getEventMetaAttributes( String projectName, String eventTypeName ) throws Exception {
        return pseb.getEventMetaAttributes( projectName, eventTypeName );
    }

    public List<String> getAuthorizedProjectNames( List<String> names,
                                                   String username,
                                                   ResponseToFailedAuthorization failureResponse,
                                                   AccessLevel accessLevel,
                                                   QueryEntityType queryEntityType ) throws Exception {
        return pseb.getAuthorizedProjectNames( names, username, failureResponse, accessLevel, queryEntityType);
    }

    public List<Project> getAuthorizedProjects( String username, AccessLevel accessLevel ) throws Exception {
        return pseb.getAuthorizedProjects( username, accessLevel );
    }

    public List<Group> getAllGroup() throws Exception {
        return pseb.getAllGroup();
    }

    public List<Project> getChildProjects( Long projectId ) throws Exception, IllegalAccessException {
        return pseb.getChildProjects( projectId );
    }

    public List<LookupValue> getLookupValueByType(String type) throws Exception {
        return pseb.getLookupValueByType(type);
    }

    public List<Dictionary> getDictionaries(boolean includeInactive) throws Exception {
        return pseb.getDictionaries(includeInactive);
    }

    public List<DictionaryDependency> getDictionaryDependencies() throws Exception {
        return pseb.getDictionaryDependencies();
    }

    public List<Dictionary> getDictionaryByType(String dictType) throws Exception {
        return pseb.getDictionaryByType(dictType);
    }

    public List<Dictionary> getDictionaryDependenciesByType(String dictType, String dictCode) throws Exception {
        return pseb.getDictionaryDependenciesByType(dictType, dictCode);
    }

    public void updateDictionary(Long dictionaryId, boolean active) throws Exception{
        pseb.updateDictionary(dictionaryId, active);
    }
}
