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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Sample;
import org.jcvi.ometa.model.SampleAttribute;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 4:37 PM
 *
 * Last modified by : hkim
 * date: 4/12/12
 * log: added sample id check for getSampleAttribute()
 *
 * Database interchange for Sample Attributes - modifiers to samples.
 */
public class SampleAttributeDAO extends HibernateDAO {

    /** Writeback model with standardized date, for whole transaction. */
    public void write( SampleAttribute model, Date transactionDate, Session session ) throws DAOException {
        try {
            prepareForWriteback( model, null, transactionDate, session );
            session.saveOrUpdate( model );

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    //  Sample attributes are "dispatched", and will not have names for samples and projects within them.
    private void prepareForWriteback( SampleAttribute model, String actorName, Date transactionDate, Session session ) throws DAOException {
        handleCreationTracking(model, actorName, transactionDate, session);
    }

    public SampleAttribute getSampleAttribute( Long projectId, Long sampleId, Long attributeLookupValueId, Session session )
        throws DAOException {

        SampleAttribute attribute = null;
        try {
            Criteria crit = session.createCriteria( SampleAttribute.class );
            crit.add( Restrictions.eq( "projectId", projectId ) );
            crit.add(Restrictions.eq("sampleId", sampleId));
            crit.add( Restrictions.eq( "nameLookupValueId", attributeLookupValueId ) );
            List<SampleAttribute> results = crit.list();

            if ( results != null  &&  results.size() > 0 ) {
                attribute = results.get( 0 );
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attribute;
    }

    public SampleAttribute getSampleAttribute(String projectName, String sampleName, String attributeName, Session session )
            throws DAOException {

        SampleAttribute attribute = null;
        try {
            String sql = "select sa.* from ifx_projects.sample_attribute sa " +
                    "join ifx_projects.sample s on s.sample_id = sa.sampla_sample_id " +
                    "join ifx_projects.project p on p.projet_id = s.sample_projet_id " +
                    "join ifx_projects.lookup_value lv on lv.lkuvlu_id = sa.sampla_lkuvlu_attribute_id " +
                    "where p.projet_name = :projectName " +
                    "and s.sample_name = :sampleName " +
                    "and lv.lkuvlu_name = :attributeName";
            Query query = session.createSQLQuery(sql)
                    .addEntity(SampleAttribute.class)
                    .setParameter("projectName",projectName)
                    .setParameter("sampleName",sampleName)
                    .setParameter("attributeName",attributeName);

            List<SampleAttribute> results = query.list();

            if ( results != null  &&  results.size() > 0 ) {
                attribute = results.get( 0 );
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attribute;
    }

    public List<SampleAttribute> getSampleAttributesFromProject( Long projectId, Long attributeLookupValueId, Session session )
            throws DAOException {

        List<SampleAttribute> results = null;
        try {
            Criteria crit = session.createCriteria( SampleAttribute.class );
            crit.add( Restrictions.eq( "projectId", projectId ) );
            crit.add( Restrictions.eq( "nameLookupValueId", attributeLookupValueId ) );
            results = crit.list();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return results;
    }

    public List<SampleAttribute> getAllAttributes( Long sampleId, Session session ) throws DAOException {
        List<SampleAttribute> attributeList = new ArrayList<SampleAttribute>();
        try {
            Criteria crit = session.createCriteria( SampleAttribute.class );
            crit.add( Restrictions.eq("sampleId", sampleId) );
            List<SampleAttribute> results = crit.list();
            attributeList.addAll( results );
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

    public List<SampleAttribute> getAllAttributes( List<Long> sampleIds, Session session ) throws DAOException {
        List<SampleAttribute> attributeList = new ArrayList<SampleAttribute>();

        try {
            if ( sampleIds != null  &&  sampleIds.size() > 0 ) {
                Criteria crit = session.createCriteria( SampleAttribute.class );
                crit.add( Restrictions.in( "sampleId", sampleIds ) );
                List results = crit.list();

                if ( results != null ) {
                    for ( Object result: results ) {
                        attributeList.add( (SampleAttribute) result);
                    }
                }

            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return attributeList;
    }

}
