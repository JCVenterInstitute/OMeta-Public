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
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.jcvi.ometa.utils.Constants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.sql.Timestamp;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/10/11
 * Time: 3:59 PM
 *
 * Helper to setup transactions to be used by the DAO's.  First call obtainSession, then [ startTransaction ],
 * then getSession, then [commmit or rollBack Transaction] and finally closeSession.
 */
public class StandaloneSessionAndTransactionManager implements SessionAndTransactionManagerI {
    private static final String STANDALONE_PRODUCTION_CFG_XML_PROP = "standalone_prod_hibernate_config_xml";
    private static final String STANDALONE_DEVELOPMENT_CFG_XML_PROP = "standalone_dev_hibernate_config_xml";
    private static final String FAILED_TO_OBTAIN_SESSION_FACTORY_ERROR = "Failed to obtain session factory: ";

    private Transaction trax;
    private static SessionFactory sessionFactoryObject;
    private Session session;
    private Timestamp traxStartDate;
    private String sessionFactoryName;
    private String hibernateCfg;
    Logger logger = Logger.getLogger( StandaloneSessionAndTransactionManager.class );

    public StandaloneSessionAndTransactionManager( Properties props, String deploymentEnvironment  ) {
        if ( deploymentEnvironment.equalsIgnoreCase( Constants.DEVELOPMENT_DATABASE ) ) {
            hibernateCfg = props.getProperty( STANDALONE_DEVELOPMENT_CFG_XML_PROP );
        }
        else if ( deploymentEnvironment.equalsIgnoreCase( Constants.PRODUCTION_DATABASE ) ) {
            hibernateCfg = props.getProperty( STANDALONE_PRODUCTION_CFG_XML_PROP );
        }
        else {
            throw new IllegalArgumentException("Unknown deployment: " + deploymentEnvironment );
        }
    }

    @Override
    public void startTransaction( ) throws DAOException {
        if ( session == null ) {
            throw new DAOException( "No session yet established." );
        }
        trax = session.beginTransaction();
        Timestamp ts = new Timestamp( new java.util.Date().getTime() );
        traxStartDate = ts;
    }

    @Override
    public void commitTransaction() throws DAOException {
        if ( trax != null  &&  trax.isActive() ) {
            try {
                trax.commit();
            } catch ( Exception ex ) {
                throw new DAOException( ex );
            }
        }
    }

    @Override
    public void rollBackTransaction() {
        if ( trax != null   &&   trax.isActive() ) {
            trax.rollback();
        }
        traxStartDate = null;
    }

    /** Refer to this date for all "creation" or "modification" dates under the transaction. */
    @Override
    public Timestamp getTransactionStartDate() {
        return traxStartDate;
    }

    @Override
    public void closeSession() {
        try {
            if ( session != null  &&  session.isOpen() ) {
                session.close();
            }
        } catch ( Throwable th ) {
            // NOTE: Not seeing "logger.trace()" on this version of log4j.
            logger.warn( "Failed to close a hibernate session: " + th.getMessage() + " :: " +
                    (th.getStackTrace().length > 0 ? th.getStackTrace()[0] : " No stack trace available") );
        }
    }

    @Override
    /** Keep this for all session factory creation use. */
    public void setSessionFactoryName( String sessionFactoryName ) {
        this.sessionFactoryName = sessionFactoryName;
    }

    /**
     * Getter 'facility' method to aid all subclasses in getting session.
     *
     * @return valid hibernate session
     * @throws DAOException by any method calls.
     */
    public Session getSession() throws DAOException {
        try {
            session = getSessionFactory(sessionFactoryName).openSession();
        } catch (Exception ex) {
            throw new DAOException(ex, "Failed to obtain hibernate session.");
        }
        return session;
    }

    /**
     * Obtain the session factory from the j2ee environment.  Designed as a 'facility' method
     * for both this class and subclasses.
     *
     * @param jndiSessionFactoryName what session factory to lookup.
     * @return whatever factory was found
     * @throws DAOException in event of any called method exceptions.
     */
    protected SessionFactory getSessionFactory(String jndiSessionFactoryName) throws DAOException {
        if (jndiSessionFactoryName == null  ||  jndiSessionFactoryName.trim().length() == 0)
            return getSessionFactory();

        String fullyQualifiedSessionFactory = jndiSessionFactoryName;
        SessionFactory sessionFactory;
        try {
            Context ctx = new InitialContext();
            logger.info(fullyQualifiedSessionFactory);
            sessionFactory = (SessionFactory)ctx.lookup(fullyQualifiedSessionFactory);
        } catch (ClassCastException cse) {
            throw new DAOException(cse, FAILED_TO_OBTAIN_SESSION_FACTORY_ERROR + fullyQualifiedSessionFactory);
        } catch (NamingException ne) {
            throw new DAOException(ne, FAILED_TO_OBTAIN_SESSION_FACTORY_ERROR + fullyQualifiedSessionFactory);
        }
        return sessionFactory;
    }

    /**
     * Getter for session factory.  This, with no param, forces the creation of a session factory
     * on-the-spot.  It is intended for use in JUnit tests.
     *
     * @return fully-operational session factory.
     */
    protected SessionFactory getSessionFactory() throws DAOException {
        if ( sessionFactoryObject != null )
            return sessionFactoryObject;

        if (sessionFactoryName != null  &&  sessionFactoryName.trim().length() > 0 ) {
            String message =
                    "Attempt at creating a session factory from scratch when a container session factory /" +
                            sessionFactoryName +
                    "/ may be available.";
            logger.error(message);
            throw new DAOException(message);
        }

        try {
            Configuration cfg = new AnnotationConfiguration();

            File f = new File( hibernateCfg );
            if (f.exists()) {
                cfg = cfg.configure(f);
            }
            else {
                cfg = cfg.configure( hibernateCfg );
            }

            SessionFactory factory = cfg.buildSessionFactory();
            sessionFactoryObject = factory;
            logger.info("Created session factory on-the-spot");
            return factory;

        } catch (Exception ex) {
            logger.error("Failed to create a session factory. " + ex.getMessage());
            throw new DAOException(ex);

        }
    }

}
