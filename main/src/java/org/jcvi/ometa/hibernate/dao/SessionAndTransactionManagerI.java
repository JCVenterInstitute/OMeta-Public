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

import org.hibernate.Session;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/1/11
 * Time: 2:27 PM
 *
 * Implement this to create a means of getting transactions, and transactions-on-sessions.
 */
public interface SessionAndTransactionManagerI {
    Session getSession() throws DAOException;

    void setSessionFactoryName( String sessionFactoryName );

    void startTransaction() throws DAOException;

    void commitTransaction() throws DAOException;

    void rollBackTransaction();

    Date getTransactionStartDate();

    void closeSession();
}
