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
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.ProjectAttribute;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 4:37 PM
 *
 * Database interchange for Project Attributes - modifiers to the project, whose names are controlled by
 * meta attributes.
 */
public class ProjectAttributeDAO extends HibernateDAO {

    public void write( ProjectAttribute model, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareForWriteback( model, null, transactionDate, session );
            session.saveOrUpdate( model );

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    public void update( ProjectAttribute model, Date transactionDate, Session session ) throws DAOException {
        try {
            if ( model.getModifiedDate() == null ) {
                model.setModifiedDate( transactionDate );
            }
            session.saveOrUpdate( model );

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    private void prepareForWriteback( ProjectAttribute model, String actorName, Date transactionDate, Session session )
            throws Exception {
        handleCreationTracking( model, actorName, transactionDate, session );
    }

    public List<ProjectAttribute> readAll( Long projectId, Session session ) throws DAOException {
        List<ProjectAttribute> attributeList = new ArrayList<>();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<ProjectAttribute> criteriaQuery = builder.createQuery(ProjectAttribute.class);
            Root<ProjectAttribute> projectAttributeRoot = criteriaQuery.from(ProjectAttribute.class);

            criteriaQuery.select(projectAttributeRoot)
                    .where(builder.equal(projectAttributeRoot.get("projectId"), projectId));

            List results = session.createQuery(criteriaQuery).getResultList();

            if ( results != null ) {
                for ( Object result: results ) {
                    attributeList.add( (ProjectAttribute) result);
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    public ProjectAttribute getProjectAttribute( Long projectId, Long attributeLookupValueId, Session session )
            throws DAOException {

        ProjectAttribute attribute = null;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<ProjectAttribute> criteriaQuery = builder.createQuery(ProjectAttribute.class);
            Root<ProjectAttribute> projectAttributeRoot = criteriaQuery.from(ProjectAttribute.class);

            criteriaQuery.select(projectAttributeRoot)
                    .where(builder.and(
                            builder.equal(projectAttributeRoot.get("projectId"), projectId),
                            builder.equal(projectAttributeRoot.get("nameLookupValueId"), attributeLookupValueId)
                    ));

            List<ProjectAttribute> results = session.createQuery(criteriaQuery).getResultList();

            if ( results != null  &&  results.size() > 0 ) {
                attribute = results.get( 0 );
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attribute;
    }

    public List<ProjectAttribute> readAll( List<Long> projectIds, Session session ) throws DAOException {
        List<ProjectAttribute> attributeList = new ArrayList<>();
        try {
            if ( projectIds.size() > 0 ) {
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<ProjectAttribute> criteriaQuery = builder.createQuery(ProjectAttribute.class);
                Root<ProjectAttribute> projectAttributeRoot = criteriaQuery.from(ProjectAttribute.class);

                criteriaQuery.select(projectAttributeRoot)
                        .where(projectAttributeRoot.get("projectId").in(projectIds));

                List results = session.createQuery(criteriaQuery).getResultList();

                if ( results != null ) {
                    for ( Object result: results ) {
                        attributeList.add( (ProjectAttribute) result);
                    }
                }
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

}
