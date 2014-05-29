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

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.wsf.spi.annotation.WebContext;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.configuration.JCVI_Project;
import org.jcvi.ometa.db_interface.WritebackBeanPersister;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.hibernate.dao.ContainerizedSessionAndTransactionManager;
import org.jcvi.ometa.interceptor.javaee.WriteableAllOrNothingAuthInterceptor;
import org.jcvi.ometa.intf.BeanPersistenceFacadeI;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 4/25/11
 * Time: 2:21 PM
 * <p/>
 * This POJO-like Stateless session bean (for EJB 3.0 standard) will allow project / sample tracking
 * with events, using Hibernate serialization.
 */
@Interceptors({WriteableAllOrNothingAuthInterceptor.class})
@WebService(
        name = "PSEWriteback",
        serviceName = "PSEWritebackService",
        targetNamespace = "PSEWritebackNS"
)
@WebContext(authMethod = "BASIC")
@SecurityDomain("jcvi")
@Stateless(name = "OMETA.Writeback")
@RolesAllowed("pstuser")
@Remote(org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackRemote.class)
@Local(org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackLocal.class)
public class ProjectSampleEventTrackerStateless implements ProjectSampleEventWritebackBusiness {

    @Resource
    private SessionContext context;
    private Logger logger = Logger.getLogger(ProjectSampleEventTrackerStateless.class);

    private Properties props;

    // Create the props at construction time.
    {
        props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
    }

    @PermitAll
    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void loadActor(Actor actor) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.writeBackActor(actor);
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void loadActorGroup(List<ActorGroup> actorGroups) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.writeBackActorGroup(actorGroups);
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteActorGroup(List<ActorGroup> actorGroups) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.deleteActorGroup(actorGroups);
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void loadGroups(List<Group> groups) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.writeBackGroups(groups);
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    /**
     * Loads all projects encountered.  NOTE: not restricting this.  Users who have logged in,
     * may load projects which do not yet exist.  If we force them to have access to
     * nonexistent projects, then no new project load will be possible.
     */
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadProjects(List<Project> beans) throws Exception {
        LocalBeanLoader localLoader = new LocalBeanLoader<Project>() {
            public void load(List<Project> beans, BeanPersistenceFacadeI beanPersister) throws Exception {
                beanPersister.writeBackProjects(beans, getUserName());
            }
        };
        return localLoader.loadBeans(beans);
    }

    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadSamples(@JCVI_Project List<Sample> beans) throws Exception {
        LocalBeanLoader localLoader = new LocalBeanLoader<Sample>() {
            public void load(List<Sample> beans, BeanPersistenceFacadeI beanPersister) throws Exception {
                beanPersister.writeBackSamples(beans, getUserName());
            }
        };
        return localLoader.loadBeans(beans);
    }

    /**
     * Loads to hibernate.
     *
     * @param beans all the Project meta attributes.
     * @return count of all beans loaded.
     */
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadProjectMetaAttributes(@JCVI_Project List<ProjectMetaAttribute> beans) throws Exception {
        LocalBeanLoader localLoader = new LocalBeanLoader<ProjectMetaAttribute>() {
            public void load(List<ProjectMetaAttribute> beans, BeanPersistenceFacadeI beanPersister) throws Exception {
                beanPersister.writeBackProjectMetaAttributes(beans, getUserName());
            }
        };
        return localLoader.loadBeans(beans);
    }

    /**
     * Loads to hibernate.
     *
     * @param beans all Sample meta attributes to be loaded.
     * @return count of all beans loaded.
     */
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadSampleMetaAttributes(@JCVI_Project List<SampleMetaAttribute> beans) throws Exception {
        LocalBeanLoader localLoader = new LocalBeanLoader<SampleMetaAttribute>() {
            public void load(List<SampleMetaAttribute> beans, BeanPersistenceFacadeI beanPersister) throws Exception {
                beanPersister.writeBackSampleMetaAttributes(beans, getUserName());
            }
        };
        return localLoader.loadBeans(beans);
    }

    /**
     * Loads to hibernate.
     *
     * @param beans all Event meta attributes to be loaded.
     * @return count of all beans loaded.
     */
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadEventMetaAttributes(@JCVI_Project List<EventMetaAttribute> beans) throws Exception {
        LocalBeanLoader localLoader = new LocalBeanLoader<EventMetaAttribute>() {
            public void load(List<EventMetaAttribute> beans, BeanPersistenceFacadeI beanPersister) throws Exception {
                beanPersister.writeBackEventMetaAttributes(beans, getUserName());
            }
        };
        return localLoader.loadBeans(beans);
    }

    /**
     * Loads attributes, which together comprise events, to the database through hibernate.
     *
     * @param beans to be loaded
     * @return true if no errors, false if there were errors.
     */
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadAttributes(@JCVI_Project List<FileReadAttributeBean> beans, String eventName) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.writeBackAttributes(beans, eventName, getUserName());
            return beans.size();

        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }

    }

    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void loadAll(@JCVI_Project List<String> projectsToSecure, MultiLoadParameter multiLoadParameter) throws Exception {

        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            // Order: write back lookup values; projects
            if (multiLoadParameter.getLookupValues() != null) {
                for (List<LookupValue> lv : multiLoadParameter.getLookupValues()) {
                    beanPersister.writeBackLookupValues(lv);
                }
            }
            if (multiLoadParameter.getProjects() != null) {
                for (List<Project> projects : multiLoadParameter.getProjects()) {
                    beanPersister.writeBackProjects(projects, getUserName());
                }
            }
            if (multiLoadParameter.getSamples() != null) {
                for (List<Sample> samples : multiLoadParameter.getSamples()) {
                    beanPersister.writeBackSamples(samples, getUserName());
                }
            }
            if (multiLoadParameter.getPmas() != null) {
                for (List<ProjectMetaAttribute> pmas : multiLoadParameter.getPmas()) {
                    beanPersister.writeBackProjectMetaAttributes(pmas, getUserName());
                }
            }
            if (multiLoadParameter.getSmas() != null) {
                for (List<SampleMetaAttribute> smas : multiLoadParameter.getSmas()) {
                    beanPersister.writeBackSampleMetaAttributes(smas, getUserName());
                }
            }
            if (multiLoadParameter.getEmas() != null) {
                for (List<EventMetaAttribute> emas : multiLoadParameter.getEmas()) {
                    beanPersister.writeBackEventMetaAttributes(emas, getUserName());
                }
            }
            if (multiLoadParameter.getProjectRegistrationEventAttributes() != null) {
                for (List<FileReadAttributeBean> eas : multiLoadParameter.getProjectRegistrationEventAttributes()) {
                    beanPersister.writeBackAttributes(
                            eas, multiLoadParameter.getProjectRegistrationEventName(), getUserName());
                }
            }
            if (multiLoadParameter.getSampleRegistrationEventAttributes() != null) {
                for (List<FileReadAttributeBean> eas : multiLoadParameter.getSampleRegistrationEventAttributes()) {
                    beanPersister.writeBackAttributes(
                            eas, multiLoadParameter.getSampleRegistrationEventName(), getUserName());
                }
            }
            if (multiLoadParameter.getOtherEvents() != null) {
                for (MultiLoadParameter.LoadableEventBean bean : multiLoadParameter.getOtherEvents()) {
                    beanPersister.writeBackAttributes(bean.getAttributes(), bean.getEventName(), getUserName());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }

    }

    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateEventStatus(Event event) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.updateEventStatus(event, getUserName());
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateProject(Project project) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.updateProject(project, getUserName());
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateSample(Sample sample) throws Exception {
        BeanPersistenceFacadeI beanPersister = getBeanPersister();
        beanPersister.open();
        try {
            beanPersister.updateSample(sample, getUserName());
        } catch (Exception ex) {
            logger.error(ex);
            beanPersister.error();
            throw ex;
        } finally {
            beanPersister.close();
        }
    }

    /**
     * Loads to hibernate.
     *
     * @param beans all Lookup values to be loaded.
     * @return count of all beans loaded.
     */
    @Override
    @WebMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int loadLookupValues(List<LookupValue> beans) throws Exception {
        LocalBeanLoader localLoader = new LocalBeanLoader<LookupValue>() {
            public void load(List<LookupValue> beans, BeanPersistenceFacadeI beanPersister) throws Exception {
                // User better be logged in by this point.
                String userName = getUserName();
                if (userName == null)
                    throw new IllegalAccessException("User must be logged in, to write lookup values.");
                beanPersister.writeBackLookupValues(beans);
            }
        };
        return localLoader.loadBeans(beans);
    }

    private String getUserName() {
        return context.getCallerPrincipal().getName();
    }

    /**
     * General means of getting persister required by many methods.
     */
    private BeanPersistenceFacadeI getBeanPersister() {
        BeanPersistenceFacadeI rtnVal = new WritebackBeanPersister(props,new ContainerizedSessionAndTransactionManager(props));
        return rtnVal;
    }

    abstract class LocalBeanLoader<T extends ModelBean> {

        public int loadBeans(List<T> beans) throws Exception {
            BeanPersistenceFacadeI beanPersister = getBeanPersister();
            try {
                beanPersister.open();
                load(beans, beanPersister);
                return beans.size();
            } catch (Exception ex) {
                logger.error(ex);
                beanPersister.error();
                throw ex;
            } finally {
                beanPersister.close();
            }
        }

        public abstract void load(List<T> beans, BeanPersistenceFacadeI persister) throws Exception;
    }
}
