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
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.ActorGroup;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 1/3/11
 * Time: 3:07 PM
 *
 * Data Access Object for encapsulating database access to actor table.
 */
public class ActorDAO extends HibernateDAO {

    /** Want to get the actor's object (usually for its ID), but only have a login name. */
    public Actor getActorByLoginName( String loginName, Session session ) throws DAOException {
        Actor rtnVal = null;
        try {
            Criteria crit = session.createCriteria( Actor.class );
            crit.add( Restrictions.eq( "username", loginName ) );
            List modelObjects = crit.list();
            if ( modelObjects != null ) {
                if ( modelObjects.size() == 1 ) {
                    rtnVal = (Actor)modelObjects.get( 0 );
                }
                else if ( modelObjects.size() > 1 ) {
                    throw new IllegalStateException("Multiple actors have same login ID.");
                }

            }

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public Actor getActorById( Long loginId, Session session ) throws DAOException {
        Actor rtnVal = null;
        try {
            Criteria crit = session.createCriteria( Actor.class );
            crit.add( Restrictions.eq( "loginId", loginId ) );
            List modelObjects = crit.list();
            if ( modelObjects != null ) {
                if ( modelObjects.size() == 1 ) {
                    rtnVal = (Actor)modelObjects.get( 0 );
                }
                else if ( modelObjects.size() > 1 ) {
                    throw new IllegalStateException("Multiple actors have same login ID.");
                }

            }

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

        return rtnVal;
    }

    public boolean isActorAdmin( String loginName, Session session ) throws DAOException {
        boolean isAdmin = false;
        try {
            String sql = " select A.* from actor A, actor_group AG, groups G, lookup_value LV " +
                " where A.actor_username=:userName and AG.actgrp_actor_id=A.actor_id and G.group_id=AG.actgrp_group_id " +
                " and LV.lkuvlu_id=G.group_name_lkuvl_id and LV.lkuvlu_name='General-Admin' ";
            SQLQuery query = session.createSQLQuery( sql );
            query.setParameter("userName", loginName);

            List<Actor> actorList = query.list();
            isAdmin = actorList!=null && actorList.size()>0;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return isAdmin;
    }

    /**
     * Given an actor model, write its relevant data to the database.  This assumes that a session is handed in,
     * and that any transaction will be prepared ON that session and rolled back/committed BY the caller.
     *
     * @throws org.jcvi.ometa.hibernate.dao.DAOException thrown if state of database not as required.
     */
    public void write( Actor actor, Session session ) throws DAOException {
        try {
            session.saveOrUpdate( actor );

        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }

    }

    public void writeActorGroup(ActorGroup actgrp, Session session) throws DAOException {
        try {
            session.saveOrUpdate(actgrp);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

//    /**
//     * Given an actor model, write its relevant data to the database.
//     *
//     * @throws org.jcvi.project_websites.hibernate.dao.DAOException thrown if state of database not as required.
//     */
//    public void write( Actor actor ) throws DAOException {
//        Transaction trax = null;
//        try {
//            SessionFactory sFac = new ContainerizedSessionAndTransactionManager().getContainerizedSessionFactory();
//            Session session = sFac.getCurrentSession();
//            //Session session = getSessionFactory(sessionFactoryName).getCurrentSession();
//            trax = session.beginTransaction();
//
//            session.saveOrUpdate( actor );
//
//            trax.commit();
//        } catch ( Exception ex ) {
//            if ( trax != null  &&  trax.isActive() ) {
//                trax.rollback();
//            }
//            throw new DAOException( ex );
//        }
//
//    }

}
