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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
    public List<Group> getAllGroup(Session session) throws DAOException {
        List<Group> returnVal;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
            Root<Group> groupRoot = criteriaQuery.from(Group.class);

            criteriaQuery.select(groupRoot);
            returnVal = new ArrayList<>(session.createQuery(criteriaQuery).getResultList());
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return returnVal;
    }

    public Group getGroupByLookupId(Long nameId, Session session) throws DAOException {
        Group rtnVal;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Group> criteriaQuery = builder.createQuery(Group.class);
            Root<Group> groupRoot = criteriaQuery.from(Group.class);

            criteriaQuery.select(groupRoot)
                    .where(builder.equal(groupRoot.get("nameLookupId"), nameId));

            rtnVal = session.createQuery(criteriaQuery).getSingleResult();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return rtnVal;
    }

    public void addGroup(Group group, Session session) throws Exception {
        try {
            session.saveOrUpdate(group);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }
}
