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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/2/11
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class GroupDAO extends HibernateDAO {
    public List<Group> getAllUserGroup( Session session ) throws DAOException {
        List<Group> returnVal = new ArrayList<Group>();
        try {
            Criteria crit = session.createCriteria( Group.class );
            returnVal.addAll( crit.list() );
        } catch ( Exception ex ) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public Group getGroupByLV(Long nameId, Session session) throws DAOException {
        Group rtnVal = null;
        try {
            Criteria crit = session.createCriteria(Group.class);
            crit.add(Restrictions.eq("nameLookupId", nameId));
            rtnVal = (Group)crit.uniqueResult();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return rtnVal;
    }
}
