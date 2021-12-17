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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.Date;

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

    private Session session;
    //private Transaction transaction;
    private Date traxStartDate;
    private String sessionFactoryName;
    private boolean sessionIsUsed = false;

    Logger logger = LogManager.getLogger(ContainerizedSessionAndTransactionManager.class);

    @PersistenceContext(unitName="OMETAPersistenceUnit")
    private EntityManager em;

    @Override
    public void startTransaction() throws DAOException {
        if(session == null) {
            throw new DAOException("No session yet established.");
        }
        session.beginTransaction();
        Timestamp ts = new Timestamp( new java.util.Date().getTime() );
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
            if(transactionCanBeEnded(tx) && tx.getStatus().isOneOf(TransactionStatus.ACTIVE)) {
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
            if(transactionCanBeEnded(tx) && tx.getStatus().isOneOf(TransactionStatus.ACTIVE)) {
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
        Transaction transaction = session.getTransaction();
        if(transactionCanBeEnded(transaction)  &&  (transaction.getStatus() != null && transaction.getStatus().isOneOf(TransactionStatus.ACTIVE))) {
           transaction.commit();
            transaction = null;  // Pushing this away, to guarantee won't be re-used.
        }
        if(session != null && session.isOpen()) {
            try {
               session.close();
            } catch(Exception ex) {
                logger.error("Failed to close session TRAX ref: " + ex.toString());
                ex.printStackTrace();
            }
        }
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
        if(session == null || !em.isOpen()) {
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
        try {
            session = em.unwrap(Session.class)
                    .getSessionFactory().openSession();

        } catch(Exception ex) {
            logger.error("Failed to invoke the getter to obtain the hibernate session " + ex.getMessage());
            ex.printStackTrace();
        }

        if(session == null) {
            logger.error("Failed to find hibernate session from " + em.toString());
        }
    }

    /** Helper: determine whether transaction can be committed or rolled back. */
    private boolean transactionCanBeEnded(Transaction tx) {
        return tx != null && !(tx.getStatus() != null && tx.getStatus().isOneOf(TransactionStatus.COMMITTED, TransactionStatus.ROLLED_BACK));
    }


}
