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

package org.jcvi.ometa;

import org.hibernate.Session;
import org.jcvi.ometa.db_interface.DAOFactory;
import org.jcvi.ometa.hibernate.dao.DAOException;
import org.jcvi.ometa.hibernate.dao.SessionAndTransactionManagerI;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.ProjectMetaAttribute;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/29/13
 * Time: 11:31 AM
 */
public class DeleteContainer {
    private DAOFactory daoFactory;
    private SessionAndTransactionManagerI sessionAndTransactionManager;

    public DeleteContainer(SessionAndTransactionManagerI manager) {
        sessionAndTransactionManager = manager;
        daoFactory = new DAOFactory();
    }

    private Session startTransactedSession() throws DAOException {
        Session session = sessionAndTransactionManager.getSession();
        sessionAndTransactionManager.startTransaction();
        return session;
    }

    public boolean deleteProject(Project project) throws Exception {
        boolean rtnVal = false;
        try {
            Session session = this.startTransactedSession();
            session.delete(project);
            sessionAndTransactionManager.commitTransaction();
            rtnVal = true;
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;
    }

    public boolean deletePMA(List<ProjectMetaAttribute> pmas) throws Exception {
        boolean rtnVal = false;
        try {
            Session session = this.startTransactedSession();
            for(ProjectMetaAttribute pma : pmas) {
                session.delete(pma);
            }
            sessionAndTransactionManager.commitTransaction();
            rtnVal = true;
        } catch (Exception ex) {
            sessionAndTransactionManager.rollBackTransaction();
            throw ex;
        } finally {
            sessionAndTransactionManager.closeSession();
        }
        return rtnVal;
    }
}
