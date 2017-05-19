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
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.utils.GuidGetter;
import org.jcvi.ometa.validation.ModelValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/12/11
 * Time: 12:42 AM
 *
 * For looking up values from a table which acts as a namespace enforcer.
 */
public class LookupValueDAO extends HibernateDAO {

    public LookupValue getLookupValue( String lvName, Session session ) throws DAOException {
        LookupValue returnVal = null;
        try {
            Criteria crit = session.createCriteria( LookupValue.class );
            crit.add( Restrictions.eq( "name", lvName) );

            returnVal = mustBeOnlyOne(lvName, crit);

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
        return returnVal;
    }

    /** Get a lookup value of the type given, name given. */
    public LookupValue getLookupValue( String lvName, String lookupType, Session session ) throws DAOException {
        LookupValue returnVal = null;
        try {
            Criteria crit = session.createCriteria( LookupValue.class );
            crit.add( Restrictions.eq( "name", lvName) );
            crit.add( Restrictions.eq( "type", lookupType ) );

            returnVal = mustBeOnlyOne(lvName, crit);

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
        return returnVal;
    }

    public LookupValue getEventStatusLookupValue( boolean active, Session session ) throws DAOException {
        LookupValue returnVal = null;
        try {
            returnVal = this.getLookupValue(active?EVENT_STATUS_ACTIVE:EVENT_STATUS_INACTIVE, ModelValidator.EVENT_STATUS_LV_TYPE_NAME, session);
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
        return returnVal;
    }

    /** Return ALL event-type lookup values. */
    public List<LookupValue> getEventLookupValueList( Session session ) throws DAOException {
        List<LookupValue>  rtnVal = null;
        try {
            rtnVal = this.getLookupValueByType(ModelValidator.EVENT_TYPE_LV_TYPE_NAME, session);
        } catch ( Exception ex ) {
            rtnVal = Collections.emptyList();
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<LookupValue> getEventLookupValueListForProjectAndSample( Long projectId, Session session ) throws DAOException {
        List<LookupValue>  rtnVal = new ArrayList<LookupValue>();
        try {
            String hql = "from LookupValue where lookupValueId in "
                    + "(select distinct(eventTypeLookupId) from EventMetaAttribute where projectId=" + projectId + ") ";

            Query query = session.createQuery( hql );
            rtnVal = query.list();
        } catch ( Exception ex ) {
            rtnVal = Collections.emptyList();
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public List<LookupValue> getLookupValueByType(String type, Session session) throws DAOException {
        List<LookupValue> rtnVal = null;
        try {
            Criteria crit = session.createCriteria( LookupValue.class );
            crit.add(Restrictions.eq("type", type));
            rtnVal = crit.list();
        } catch ( Exception ex ) {
            rtnVal = Collections.emptyList();
            throw new DAOException( ex );
        }
        return rtnVal;
    }

    /**
     * Make a new lookup value out of the parameters given.
     *
     * @param lkv lookup value with data to be written.  May/may not have tracking info.
     * @param session used in db interaction
     * @throws DAOException thrown in response to any exception.
     */
    public LookupValue createLookupValue( LookupValue lkv, Date transactionDate, Session session )
            throws DAOException {
        try {
            Long lkuvluId = new GuidGetter().getGuid(); //new GuidBlock().getGuidBlock(1L);
            if ( lkv.getCreationDate() == null ) {
                lkv.setCreationDate( transactionDate );
            }
            lkv.setLookupValueId( lkuvluId );

            session.saveOrUpdate( lkv );
            return lkv;
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    private LookupValue mustBeOnlyOne(String lvName, Criteria crit) throws Exception {
        List results = crit.list();
        LookupValue returnVal = null;
        if ( results != null  &&  results.size() > 0 ) {
            if ( results.size() == 1 ) {
                returnVal = (LookupValue)results.get(0);
            }
            else if ( results.size() > 1 ) {
                throw new Exception( "more than one lookup value has name " + lvName );
            }

        }
        return returnVal;
    }

}
