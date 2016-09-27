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

package org.jcvi.ometa.engine.support;

import org.apache.log4j.Logger;
import org.jcvi.ometa.utils.EjbBuilder;

import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.naming.Context;

/**
 * Copyright J. Craig Venter Institute, 2008.
 * <p/>
 * User: lfoster
 * Date: Feb 26, 2008
 * Time: 5:27:24 PM
 *
 * Manage the transaction for the app.  To use for client-managed transactions.  Borrowed
 * from GSFLXServer project.
 */
public class ClientTransactionManager {
//    private static final String TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";
    private static final String TRANSACTION_JNDI_NAME = "UserTransaction";
    private static final int TRANSACTION_TIMEOUT_IN_SECONDS = 2 * 60 * 60;  // 2 hours.

    private UserTransaction trax;
    private boolean transactionRolledBack = false;
    private Logger logger = Logger.getLogger(ClientTransactionManager.class);

    /**
     * Start a transaction, and keep its handle-like object within the
     * EJB client's instance.
     *
     * @throws Exception thrown in response to any caught exception.
     */
    public void startTransaction( String server, String user, String pass ) throws Exception {
        try {
            Context context = new EjbBuilder().getContext( server, user, pass, "none", logger );
            logger.debug("Looking up a transaction.");
            trax = (UserTransaction)context.lookup(TRANSACTION_JNDI_NAME);
            transactionRolledBack = false;
            logger.debug("Setting timeout and starting transaction.");
            trax.setTransactionTimeout(TRANSACTION_TIMEOUT_IN_SECONDS);
            trax.begin();
        } catch (NotSupportedException traxns) {
            logger.error("Attempting to start a non-supported transaction");
            trax = null;
            throw new Exception(traxns);
        } catch (Exception ex) {
            logger.error("Failed to start a transaction, due to " + ex.getMessage());
            trax = null;
            throw new Exception(ex);
        }
    }


    /**
     * Commit the current transaction, using its handle-like object within the
     * EJB client's instance.
     *
     * @throws Exception thrown in response to any caught exception.
     */
    public void commitTransaction() throws Exception {
        try {
            if (! transactionRolledBack) {
                if (trax == null)
                    throw new Exception("Must first start the transaction.");
                else
                    trax.commit();
            }
        } catch (Exception ex) {
            logger.error("Failed to commit a transaction due to " + ex.getMessage());
            throw new Exception(ex);
        }
    }

    /**
     * Start a transaction, and keep its handle-like object within the
     * EJB client's instance.
     *
     * @throws Exception thrown in response to any caught exception.
     */
    public void rollbackTransaction() throws Exception {
        try {
            if (! transactionRolledBack) {
                if (trax == null) {
                    logger.info("No transaction to roll back.");
                }
                else {
                    trax.rollback();
                }
                transactionRolledBack = true;
            }
        } catch (Exception ex) {
            logger.error("Failed to start a transaction");
            throw new Exception(ex);
        }
    }

    /**
     * End any outstanding transactions.
     *
     * @throws Exception if all attempts to end transaction fail.
     */
    public void endTransaction() throws Exception {
        if (trax != null) {
            try {
                if (trax.getStatus() == Status.STATUS_ACTIVE)
                    trax.commit();
            } catch (Exception ex) {
                try {
                    trax.rollback();
                    transactionRolledBack = true;
                } catch (Exception ex2) {
                    throw new Exception(ex);
                }
            } finally {
                trax = null;
            }
        }
    }

}
