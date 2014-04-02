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

package org.jcvi.ometa.hibernate.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/10/11
 * Time: 3:59 PM
 *
 * Helper to setup transactions to be used by the DAO's, to be used in JEE containers.
 */
public class ContainerizedSessionAndTransactionManager implements SessionAndTransactionManagerI {
    private static final String PERSISTENCE_UNIT = "java:/OMETAPersistenceUnit";

    private static final String CONTAINERIZED_HIBERNATE_CFG_PROP = "hibernate.cfg.xml";
    private static final String FAILED_TO_OBTAIN_SESSION_FACTORY_ERROR = "Failed to obtain session factory: ";

    private static SessionFactory sessionFactoryObject;
    private Session session;
    //private Transaction transaction;
    private Date traxStartDate;
    private String sessionFactoryName;
    private String cfgXml;
    Logger logger = Logger.getLogger(ContainerizedSessionAndTransactionManager.class);

    @PersistenceContext(unitName="OMETAPersistenceUnit")
    private EntityManager em;
    //private EntityManagerFactory emf;

    public ContainerizedSessionAndTransactionManager(Properties props) {
        cfgXml = props.getProperty(CONTAINERIZED_HIBERNATE_CFG_PROP);
    }

    @Override
    public void startTransaction() throws DAOException {
        if(session == null) {
            throw new DAOException("No session yet established.");
        }
//        if(transaction != null) {
//            logger.warn("TRAX: " + transaction + " still exists, as new one is being started!.");
//            new Exception().printStackTrace();
//        }
//        if(logger.isDebugEnabled()) {
//            logger.debug("Strart-Transacton:: TRAX ref: " + transaction);
//        }
        Date ts = new Date();
        traxStartDate = ts;
    }

    @Override
    public void commitTransaction() throws DAOException {
        if(session != null) {
            Transaction tx = session.getTransaction();
            if(logger.isDebugEnabled()) {
                logger.debug("Commit-Transaction:: TRAX ref: " + tx);
            }
            if(tx == null) {
                logger.warn("Transaction was null at commit time: TRAX ref: null");
                new Exception().printStackTrace();
            }
            if(transactionCanBeEnded(tx) && tx.isActive()) {
                tx.commit();
                tx = null;  // Pushing this away, to guarantee won't be re-used.
            }
        }
    }

    @Override
    public void rollBackTransaction() {
        traxStartDate = null;
        if(session != null) {
            Transaction tx = session.getTransaction();
            if(tx == null) {
                logger.warn("Transaction was null at rollback time: TRAX ref: null");
                new Exception().printStackTrace();
            }
            if(transactionCanBeEnded(tx) && tx.isActive()) {
                tx.rollback();
                tx = null;  // Pushing this away, to guarantee won't be re-used.
            }
        }
    }

    /** Refer to this date for all "creation" or "modification" dates under the transaction. */
    @Override
    public Date getTransactionStartDate() {
        return traxStartDate;
    }

    @Override
    public void closeSession() {
//        if(logger.isDebugEnabled()) {
//            logger.debug("Close Session:: TRAX ref: " + transaction);
//        }
        //Transaction trax = session.getTransaction();
//        if(transactionCanBeEnded(transaction)  &&  transaction.isActive()) {
//            transaction.commit();
//            transaction = null;  // Pushing this away, to guarantee won't be re-used.
//        }
//        if(session != null  &&  session.isOpen()) {
//            try {
//                session.close();
//            } catch(Exception ex) {
//                logger.error("Failed to close session TRAX ref: " + transaction);
//                ex.printStackTrace();
//            }
//        }
    }

    @Override
    /** Keep this for all session factory creation use. */
    public void setSessionFactoryName(String sessionFactoryName) {
        this.sessionFactoryName = sessionFactoryName;
    }

    /**
     * Getter 'facility' method to aid all subclasses in getting session.
     *
     * @return valid hibernate session
     * @throws DAOException by any method calls.
     */
    public Session getSession() throws DAOException {
        // Injection fails for this non-managed class.
        DAOException ex = null;
        String pu = PERSISTENCE_UNIT;
        if(session == null) {
            session = null;
            try {
                em = (EntityManager)new InitialContext().lookup(pu);
            } catch(Exception loopEx) {
                ex = new DAOException(loopEx);
                logger.error("Persistence unit JNDI name " + pu + " failed.");
            }
        }
        getHibernateSession();
        if(ex != null) {
            ex.printStackTrace();
        }
        return session;
    }

    /** This convenience method is set aside for readability. */
    private void getHibernateSession() {
        //import org.jboss.jpa.tx.TransactionScopedEntityManager;

        // Find a method for getting a hibernate session.
        Method[] emMethods = em.getClass().getMethods();
        for(Method method: emMethods) {
            if(method.getName().equals("getHibernateSession")) {
                // Once found, invoke the method.
                try {
                    Object returnObj = method.invoke(em);
                    if(returnObj instanceof Session) {
                        session = (Session)returnObj;
                    }
                    else {
                        logger.warn(
                                "Invoking 'getHibernateSession()' returned type of " + returnObj.getClass().getName() +
                                " instead of a hibernate session."
                       );
                    }
                } catch(Exception ex) {
                    logger.error("Failed to invoke the getter to obtain the hibernate session " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        if(session == null) {
            logger.error("Failed to find hibernate session from " + em.toString());
        }
    }

    /**
     * Obtain the session factory from the j2ee environment.  Designed as a 'facility' method
     * for both this class and subclasses.
     *
     * @param jndiSessionFactoryName what session factory to lookup.
     * @return whatever factory was found
     * @throws DAOException in event of any called method exceptions.
     */
//    protected SessionFactory getSessionFactory(String jndiSessionFactoryName) throws DAOException {
//        if (jndiSessionFactoryName == null  ||  jndiSessionFactoryName.trim().length() == 0)
//            return getSessionFactory();
//
//        String fullyQualifiedSessionFactory = jndiSessionFactoryName;
//        SessionFactory sessionFactory;
//        try {
//            Context ctx = new InitialContext();
//            logger.info(fullyQualifiedSessionFactory);
//            sessionFactory = (SessionFactory)ctx.lookup(fullyQualifiedSessionFactory);
//        } catch (ClassCastException cse) {
//            throw new DAOException(cse, FAILED_TO_OBTAIN_SESSION_FACTORY_ERROR + fullyQualifiedSessionFactory);
//        } catch (NamingException ne) {
//            throw new DAOException(ne, FAILED_TO_OBTAIN_SESSION_FACTORY_ERROR + fullyQualifiedSessionFactory);
//        }
//        return sessionFactory;
//    }

    /**
     * Getter for session factory.  This, with no param, forces the creation of a session factory
     * on-the-spot.  It is intended for use in JUnit tests.
     *
     * @return fully-operational session factory.
     */
//    protected SessionFactory getSessionFactory() throws DAOException {
//        if(sessionFactoryObject != null)
//            return sessionFactoryObject;
//
//        if (sessionFactoryName != null  &&  sessionFactoryName.trim().length() > 0) {
//            String message =
//                    "Attempt at creating a session factory from scratch when a container session factory /" +
//                            sessionFactoryName +
//                    "/ may be available.";
//            logger.error(message);
//            throw new DAOException(message);
//        }
//
//        try {
//            Configuration cfg = new AnnotationConfiguration();
//
//            File f = new File(cfgXml);
//            if (f.exists()) {
//                cfg = cfg.configure(f);
//            }
//            else {
//                cfg = cfg.configure(cfgXml);
//            }
//            logger.info("Got " + cfg.getProperties().size() + " properties in the " + cfgXml + " configuration.");
//
//            SessionFactory factory = cfg.buildSessionFactory();
//            sessionFactoryObject = factory;
//            logger.info("Created session factory on-the-spot");
//            return factory;
//
//        } catch (Exception ex) {
//            logger.error("Failed to create a session factory. " + ex.getMessage());
//            ex.printStackTrace();
//            throw new DAOException(ex);
//
//        }
//    }

    /** Helper: determine whether transaction can be committed or rolled back. */
    private boolean transactionCanBeEnded(Transaction tx) {
        return tx != null && !(tx.wasCommitted() || tx.wasRolledBack());
    }


}
