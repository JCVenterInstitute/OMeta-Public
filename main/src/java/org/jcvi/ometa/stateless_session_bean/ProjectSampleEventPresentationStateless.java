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

package org.jcvi.ometa.stateless_session_bean;

import org.hibernate.Session;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.configuration.*;
import org.jcvi.ometa.db_interface.DAOFactory;
import org.jcvi.ometa.hibernate.dao.*;
import org.jcvi.ometa.interceptor.javaee.ReadOnlyAllOrNothingAuthInterceptor;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 6/19/11
 * Time: 1:03 AM
 *
 * Read-side EJB to fetch information for web and other presentation.
 *
 * NOTE on annotations below.  Web services annotations do not support overloaded methods. Marking
 * multiple same-named methods with @WebService will cause an error at JBoss deployment time.  Those
 * errors can be dispelled by adding operationName= attributes to the WebMethod annotation, but then
 * when the WSDL is generated, it uses the same type for all of the overloads, defeating the purpose.
 *
 * Therefore, am annotating only the multi-cardinality overloads of the methods.  If no multi-cardinal,
 * then will annotate the by-name (string) version instead of the ID version.
 */
@Interceptors( { ReadOnlyAllOrNothingAuthInterceptor.class } )
@WebService(
        name = "PSEData",
        serviceName = "PSEDataService",
        targetNamespace = "PSEDataNS"
)
@Stateless(name="OMETA.Presentation")
@Remote(org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationRemote.class)
@Local(org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationLocal.class)
@SecurityDomain("jcvi")
public class ProjectSampleEventPresentationStateless implements ProjectSampleEventPresentationBusiness {
    private DAOFactory daoFactory;
    private SessionAndTransactionManagerI sessionAndTransactionManager;
    public ProjectSampleEventPresentationStateless() {
        daoFactory = new DAOFactory();
        Properties props = PropertyHelper.getHostnameProperties( Constants.PROPERTIES_FILE_NAME );
        sessionAndTransactionManager = new ContainerizedSessionAndTransactionManager( props );
    }

    /** This constructor to support unit testing.  */
    public ProjectSampleEventPresentationStateless(SessionAndTransactionManagerI sessionAndTransactionManager ) {
        this.sessionAndTransactionManager = sessionAndTransactionManager;
        daoFactory = new DAOFactory();
    }

    //-------------------------------------------------------------------ACTOR QUERIES
    @WebMethod
    public Actor getActor(Long loginId) throws Exception {
        Actor actor;
        try {
            ActorDAO actorDao = daoFactory.getActorDAO();
            Session session = this.startTransactedSession();
            actor = actorDao.getActorById(loginId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return actor;
    }

    @ExcludeClassInterceptors
    public String isUserAdmin(String loginName) throws Exception {
        boolean isAdmin = false;
        try {
            ActorDAO actorDao = daoFactory.getActorDAO();
            Session session = this.startTransactedSession();
            isAdmin = actorDao.isActorAdmin(loginName, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return Boolean.toString(isAdmin);
    }

    //-------------------------------------------------------------------PROJECT QUERIES
    @WebMethod
    public Project getProject( @JCVI_Project String projectName ) throws Exception {
        Project project;
        try {
            // Would throw NPE down in innards. System.out.println( "User is: " + context.getCallerPrincipal() );
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            Session session = this.startTransactedSession();
            project = projectDAO.getProject(projectName, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return project;
    }
    public Project getProject( @JCVI_Project Long projectId ) throws Exception {
        Project project;
        try {
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            Session session = this.startTransactedSession();
            project = projectDAO.getProject(projectId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return project;
    }

    /**
     * This method returns all projects, but none of that information is considered secured.
     *
     * @param projectNames by-name list of projects.
     * @return full project objects.
     * @throws Exception not thrown.  Used only for consistency.
     */
    @ExcludeClassInterceptors
    @WebMethod
    public List<Project> getProjects( @JCVI_Project List<String> projectNames ) throws Exception {
        List<Project> projects;
        try {
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            Session session = this.startTransactedSession();
            projects = projectDAO.getProjects(projectNames, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return projects;
    }

    @ExcludeClassInterceptors
    @WebMethod
    public List<Project> getProjectsByPublicFlag( boolean isPublic ) throws Exception {
        List<Project> projects;
        try {
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            Session session = this.startTransactedSession();
            projects = projectDAO.getProjectsByPublicFlag(isPublic, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return projects;
    }

    @WebMethod
    public Long getProjectId( @JCVI_Project String projectName ) throws Exception {
        return this.getProject(projectName).getProjectId();
    }

    @WebMethod
    public List<ProjectMetaAttribute> getProjectMetaAttributes( @JCVI_Project List<Long> projectIds ) throws Exception {

        List<ProjectMetaAttribute> pmaBeans = new ArrayList<ProjectMetaAttribute>();
        try {
            ProjectMetaAttributeDAO pmaDao = daoFactory.getProjectMetaAttributeDAO();
            Session session = this.startTransactedSession();
            pmaBeans.addAll( pmaDao.readAll(projectIds, session) );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return pmaBeans;
    }

    /**
     * Will pull in the list of project attributes used by a project.  Converts project name to id.
     *
     * @param projectName name of proj.
     */
    public List<ProjectMetaAttribute> getProjectMetaAttributes( @JCVI_Project String projectName ) throws Exception {
        return this.getProjectMetaAttributes(this.getProjectId(projectName));
    }

    public List<ProjectMetaAttribute> getProjectMetaAttributes( @JCVI_Project Long projectId ) throws Exception {
        Long[] projectIds = {projectId};
        return this.getProjectMetaAttributes(Arrays.asList(projectIds));
    }

    public List<ProjectMetaAttribute> getUniqueProjectMetaAttributes() throws Exception {
        Session session = startTransactedSession();

        List<ProjectMetaAttribute> pmaBeans = Collections.emptyList();
        try {
            ProjectMetaAttributeDAO pmaDao = daoFactory.getProjectMetaAttributeDAO();
            pmaBeans = pmaDao.readAllUnique( session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return pmaBeans;
    }

    @Override
    public List<ProjectAttribute> getProjectAttributes( @JCVI_Project String projectName ) throws Exception {
        return this.getProjectAttributes(this.getProjectId(projectName));
    }

    public List<ProjectAttribute> getProjectAttributes(Long projectId) throws Exception {
        Session session = startTransactedSession();

        List<ProjectAttribute> paBeans = Collections.emptyList();
        try {
            ProjectAttributeDAO paDao = daoFactory.getProjectAttributeDAO();
            paBeans = paDao.readAll(projectId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return paBeans;
    }

    @WebMethod
    public List<ProjectAttribute> getProjectAttributes( @JCVI_Project List<Long> projectIds ) throws Exception {
        Session session = startTransactedSession();

        List<ProjectAttribute> paBeans = Collections.emptyList();
        try {
            ProjectAttributeDAO paDao = daoFactory.getProjectAttributeDAO();
            paBeans = paDao.readAll(projectIds, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return paBeans;
    }

    /**
     * Get Child Projects
     */
    @WebMethod
    public List<Project> getChildProjects( @JCVI_Project Long projectId ) throws Exception, IllegalAccessException {

        List<Project> rtnVal;
        try {
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            Session session = this.startTransactedSession();
            rtnVal = projectDAO.getChildProjects( projectId, session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;

    }

    //-------------------------------------------------------------------SAMPLE QUERIES
    @WebMethod
    public Sample getSample( @JCVI_Sample String sampleName ) throws Exception {
        Sample sample;
        try {
            SampleDAO sampleDao = daoFactory.getSampleDAO();
            Session session = this.startTransactedSession();
            sample = sampleDao.getSample(sampleName, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return sample;
    }

    public Sample getSample( @JCVI_Sample Long sampleId ) throws Exception {
        Sample sample;
        try {
            SampleDAO sampleDao = daoFactory.getSampleDAO();
            Session session = this.startTransactedSession();
            sample = sampleDao.getSample(sampleId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return sample;
    }

    public Sample getSample( @JCVI_Project Long projectId, @JCVI_Sample String sampleName ) throws Exception {
        Sample sample;
        try {
            SampleDAO sampleDao = daoFactory.getSampleDAO();
            Session session = this.startTransactedSession();
            sample = sampleDao.getSample(projectId, sampleName, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return sample;
    }

    @Override
    @WebMethod
    public List<Sample> getSamplesForProject( @JCVI_Project String projectName ) throws Exception {
        return this.getSamplesForProject(this.getProjectId(projectName));
    }

    public List<Sample> getSamplesForProject( @JCVI_Project Long projectId ) throws Exception {
        Session session = startTransactedSession();

        List<Sample> sBeans = Collections.emptyList();
        try {
            SampleDAO sDao = daoFactory.getSampleDAO();
            sBeans = sDao.getAllSamples(projectId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return sBeans;
    }

    @WebMethod
    public List<Sample> getSamplesForProjects( @JCVI_Project List<Long> projectIds ) throws Exception {
        Session session = startTransactedSession();

        List<Sample> sBeans = Collections.emptyList();
        try {
            SampleDAO sDao = daoFactory.getSampleDAO();
            sBeans = sDao.getAllSamples(projectIds, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return sBeans;
    }

    @ExcludeClassInterceptors
    @WebMethod
    public List<Sample> getSamplesForProjectByPublicFlag( @JCVI_Project Long projectId, boolean isPublic ) throws Exception {
        Session session = startTransactedSession();

        List<Sample> sBeans = Collections.emptyList();
        try {
            SampleDAO sDao = daoFactory.getSampleDAO();
            sBeans = sDao.getSamplesByPublicFlag(projectId, isPublic, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return sBeans;
    }

    @WebMethod
    public List<Sample> getAllSamples(Long flexId, String type, String sSearch, String sortCol, String sortDir) throws Exception {
        Session session = startTransactedSession();

        List<Sample> sBeans = Collections.emptyList();
        try {
            SampleDAO sDao = daoFactory.getSampleDAO();
            sBeans = sDao.getAllSamples(flexId, type, sSearch, sortCol, sortDir, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return sBeans;
    }

    @WebMethod
    public List<Sample> getAllSamplesBySearch(String projectIds, String attributeNames, String sSearch, String sortType, String sortCol, String sortDir) throws Exception {
        Session session = startTransactedSession();

        List<Sample> sBeans = Collections.emptyList();
        try {
            SampleDAO sDao = daoFactory.getSampleDAO();
            sBeans = sDao.getAllSamples(projectIds, attributeNames, sSearch, sortType, sortCol, sortDir, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return sBeans;
    }

    @WebMethod
    public List<Sample> getChildSamples(Long sampleId) throws Exception {
        Session session = startTransactedSession();
        List<Sample> sBeans = Collections.emptyList();
        try {
            SampleDAO sDao = daoFactory.getSampleDAO();
            sBeans = sDao.getChildSamples(sampleId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return sBeans;
    }

    @WebMethod
    public List<SampleMetaAttribute> getSampleMetaAttributes( @JCVI_Project List<Long> projectIds ) throws Exception {
        List<SampleMetaAttribute> smaBeans = new ArrayList<SampleMetaAttribute>();
        try {
            SampleMetaAttributeDAO smaDao = daoFactory.getSampleMetaAttributeDAO();
            Session session = this.startTransactedSession();
            smaBeans.addAll( smaDao.readAll(projectIds, session) );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return smaBeans;
    }

    public List<SampleMetaAttribute> getSampleMetaAttributes( @JCVI_Project Long projectId ) throws Exception {
        List<SampleMetaAttribute> smaBeans = new ArrayList<SampleMetaAttribute>();
        try {
            SampleMetaAttributeDAO smaDao = daoFactory.getSampleMetaAttributeDAO();
            Session session = this.startTransactedSession();
            smaBeans.addAll( smaDao.readAll(projectId, session) );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return smaBeans;
    }

    @WebMethod
    public List<SampleMetaAttribute> getUniqueSampleMetaAttributes() throws Exception {
        List<SampleMetaAttribute> smaBeans = new ArrayList<SampleMetaAttribute>();
        try {
            SampleMetaAttributeDAO smaDao = daoFactory.getSampleMetaAttributeDAO();
            Session session = this.startTransactedSession();
            smaBeans =  smaDao.readAllUnique( session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return smaBeans;
    }

    /** This is needed by app to learn whether it is needed to include sample-specific information wrt event. */
    @WebMethod
    public Boolean isSampleRequired( @JCVI_Project String projectName, String eventName ) throws Exception {
        Boolean rtnVal = true; // Burdened until shown otherwise.
        try {
            EventDAO eventDAO = daoFactory.getEventDAO();
            Session session = this.startTransactedSession();
            rtnVal = eventDAO.isSampleRequired( projectName, eventName, session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;
    }

    @Override
    public List<SampleAttribute> getSampleAttributes( @JCVI_Sample String sampleName ) throws Exception {
        return this.getSampleAttributes(this.getSample(sampleName).getSampleId());
    }

    public List<SampleAttribute> getSampleAttributes(Long sampleId) throws Exception {
        Session session = startTransactedSession();

        List<SampleAttribute> sampleAttributes = Collections.emptyList();
        try {
            SampleAttributeDAO saDao = daoFactory.getSampleAttributeDAO();
            sampleAttributes = saDao.getAllAttributes(sampleId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return sampleAttributes;
    }

    /** Get all attributes associated with a list of samples, by ID. */
    @WebMethod
    public List<SampleAttribute> getSampleAttributes( @JCVI_Sample List<Long> sampleIds ) throws Exception {
        Session session = startTransactedSession();

        List<SampleAttribute> sampleAttributes = Collections.emptyList();
        try {
            SampleAttributeDAO saDao = daoFactory.getSampleAttributeDAO();
            sampleAttributes = saDao.getAllAttributes(sampleIds, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return sampleAttributes;
    }

    //-------------------------------------------------------------------EVENT QUERIES
    @Override
    @WebMethod
    public List<Event> getEventsForProject( @JCVI_Project String projectName ) throws Exception {
        return this.getEventsForProject(this.getProjectId(projectName));
    }

    public List<Event> getEventsForProject(Long projectId) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getAllEvents(projectId, "Project", null, null, null, -1, -1, null, null, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<Event> getEventsForProjects( @JCVI_Project List<Long> projectIds ) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getAllEvents(projectIds, "Project", session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }


        return evtBeans;
    }

    @WebMethod
    public List<Event> getAllEvents(Long flexId, String type, String sSearch, String sortCol, String sortDir, int start, int count, String fromd, String tod) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getAllEvents(flexId, type, sSearch, sortCol, sortDir, start, count, fromd, tod, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<Event> getEventsForSample(@JCVI_Sample Long sampleId ) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getAllEvents(sampleId, "Sample", null, null, null, -1, -1, null, null, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<Event> getEventsForSamples( @JCVI_Sample List<Long> sampleIds ) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getAllEvents(sampleIds, "Sample", session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<Event> getEventByType( @JCVI_Project Long projectId, Long eventTypeId ) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getEventByType(projectId, eventTypeId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<Event> getEventByTypeAndSample( @JCVI_Sample Long sampleId, Long eventTypeId ) throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getEventByTypeAndSample(sampleId, eventTypeId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<Event> getUniqueEventTypes() throws Exception {
        Session session = startTransactedSession();

        List<Event> evtBeans = Collections.emptyList();
        try {
            EventDAO evtDao = daoFactory.getEventDAO();
            evtBeans = evtDao.getUniqueEventTypes(session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    @WebMethod
    public List<LookupValue> getEventTypesForProject( @JCVI_Project Long projectId)
            throws Exception {
        Session session = startTransactedSession();

        List<LookupValue> evtBeans = Collections.emptyList();
        try {
            LookupValueDAO lvDao = daoFactory.getLookupValueDAO();
            evtBeans = lvDao.getEventLookupValueListForProjectAndSample(projectId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return evtBeans;
    }

    public List<EventAttribute> getEventAttributes(Long eventId,  @JCVI_Project Long projectId ) throws Exception {
        Session session = startTransactedSession();

        List<EventAttribute> eaBeans = Collections.emptyList();
        try {
            EventAttributeDAO eaDao = daoFactory.getEventAttributeDAO();
            eaBeans = eaDao.getEventAttributes(eventId, projectId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return eaBeans;
    }

    @WebMethod
    public List<EventAttribute> getEventAttributes(List<Long> eventIds, @JCVI_Project Long projectId) throws Exception {
        Session session = startTransactedSession();

        List<EventAttribute> eaBeans = Collections.emptyList();
        try {
            EventAttributeDAO eaDao = daoFactory.getEventAttributeDAO();
            eaBeans = eaDao.getEventAttributes(eventIds, projectId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return eaBeans;
    }

    public List<EventMetaAttribute> getEventMetaAttributes( @JCVI_Project Long projectId ) throws Exception {
        return this.getEventMetaAttributes(projectId, null);
    }

    public List<EventMetaAttribute> getEventMetaAttributes( @JCVI_Project Long projectId, Long eventTypeId) throws Exception {
        Session session = startTransactedSession();

        List<EventMetaAttribute> emaBeans = Collections.emptyList();
        try {
            EventMetaAttributeDAO emaDao = daoFactory.getEventMetaAttributeDAO();
            emaBeans = emaDao.readAll(projectId, eventTypeId, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return emaBeans;
    }

    public List<EventMetaAttribute> getEventMetaAttributes(@JCVI_Project String projectName, String eventTypeName ) throws Exception {

        List<EventMetaAttribute> emaBeans = new ArrayList<EventMetaAttribute>();
        try {
            Session session = this.startTransactedSession();
            // Get expansions of project and lookup value-for-event.
            ProjectDAO projectDAO = daoFactory.getProjectDAO();
            Project project = projectDAO.getProject( projectName, session );
            if ( project != null ) {
                Long projectId = project.getProjectId();

                Long eventTypeLookupId = null;
                if (eventTypeName != null) {
                    LookupValueDAO lookupValueDAO = daoFactory.getLookupValueDAO();
                    LookupValue lv = lookupValueDAO.getLookupValue( eventTypeName, session );
                    if(lv!=null) {
                        eventTypeLookupId = lv.getLookupValueId();
                    }
                }

                EventMetaAttributeDAO emaDao = daoFactory.getEventMetaAttributeDAO();
                List<Long> projectIds = new ArrayList<Long>();
                projectIds.add( projectId );
                emaBeans.addAll( emaDao.readAll( projectIds, eventTypeLookupId, session ) );

            }
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return emaBeans;
    }

    @WebMethod
    public List<EventMetaAttribute> getEventMetaAttributes( @JCVI_Project List<Long> projectIds ) throws Exception {
        Session session = startTransactedSession();

        List<EventMetaAttribute> emaBeans = new ArrayList<EventMetaAttribute>();
        try {
            EventMetaAttributeDAO emaDao = daoFactory.getEventMetaAttributeDAO();
            emaBeans.addAll( emaDao.readAll(projectIds, null, session) );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return emaBeans;
    }

    @WebMethod
    public List<EventMetaAttribute> getUniqueEventMetaAttributes() throws Exception {
        Session session = startTransactedSession();

        List<EventMetaAttribute> emaBeans = new ArrayList<EventMetaAttribute>();
        try {
            EventMetaAttributeDAO emaDao = daoFactory.getEventMetaAttributeDAO();
            emaBeans = emaDao.readAllUnique( session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return emaBeans;
    }

    public LookupValue getLookupValue ( String name, String type) throws Exception, IllegalAccessException {

        LookupValue rtnVal = null;
        try {
            LookupValueDAO lookupValueDao = daoFactory.getLookupValueDAO();
            Session session = this.startTransactedSession();
            rtnVal = lookupValueDao.getLookupValue( name, type, session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;

    }

    public Event getEvent ( Long eventId ) throws Exception, IllegalAccessException {

        Event rtnVal = null;
        try {
            EventDAO eventDao = daoFactory.getEventDAO();
            Session session = this.startTransactedSession();
            List<Event> events = eventDao.getAllEvents( eventId, "Event", null, null, null, -1, -1, null, null, session);

            if( events.size() > 1 )
                throw new Exception( "EventDAO - There is more than one event under eventId: "+ eventId );
            rtnVal = events.get( 0 );

            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;

    }

    public List<String> getAuthorizedProjectNames( List<String> names,
                                                   String username,
                                                   ResponseToFailedAuthorization failureResponse,
                                                   AccessLevel accessLevel,
                                                   QueryEntityType queryEntityType ) throws Exception {
        List<String> rtnVal = null;

        try {
            Session session = this.startTransactedSession();
            SecurityDAO securityDAO = daoFactory.getSecurityDAO();
            rtnVal = securityDAO.getListOfAuthorizedByName( names, username, failureResponse, accessLevel, session, queryEntityType);

            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;
    }

    public List<Project> getAuthorizedProjects( String username, AccessLevel accessLevel ) throws Exception {
        List<Project> rtnVal = null;

        try {
            Session session = this.startTransactedSession();
            SecurityDAO securityDAO = daoFactory.getSecurityDAO();
            rtnVal = securityDAO.getListOfAuthorizedProjects( username, accessLevel, session);

            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;
    }

    public List<Group> getUserGroup() throws Exception {
        List<Group> rtnVal = null;

        try {
            GroupDAO groupDAO= daoFactory.getGroupDAO();
            Session session = this.startTransactedSession();
            rtnVal = groupDAO.getAllUserGroup( session );
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return rtnVal;
    }

    public List<LookupValue> getLookupValueByType(String type) throws Exception {
        List<LookupValue> rtnVal = null;

        try {
            LookupValueDAO lookupValueDAO = daoFactory.getLookupValueDAO();
            Session session = this.startTransactedSession();
            rtnVal = lookupValueDAO.getLookupValueByType(type, session);
            sessionAndTransactionManager.commitTransaction();
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }

        return rtnVal;
    }

    //---------------------------------------------HELPER METHODS
    private Session startTransactedSession() throws DAOException {
        Session session = sessionAndTransactionManager.getSession();
        sessionAndTransactionManager.startTransaction();
        return session;
    }

}
