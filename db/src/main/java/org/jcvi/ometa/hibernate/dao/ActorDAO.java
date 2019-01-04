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

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.ActorGroup;
import org.jcvi.ometa.model.Group;
import org.jcvi.ometa.model.LookupValue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
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
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Actor> criteriaQuery = builder.createQuery(Actor.class);
            Root<Actor> actorRoot = criteriaQuery.from(Actor.class);
            criteriaQuery.select(actorRoot)
                    .where(builder.equal(actorRoot.get("username"), loginName));
            List modelObjects = session.createQuery(criteriaQuery).getResultList();
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
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Actor> criteriaQuery = builder.createQuery(Actor.class);
            Root<Actor> actorRoot = criteriaQuery.from(Actor.class);
            criteriaQuery.select(actorRoot)
                    .where(builder.equal(actorRoot.get("loginId"), loginId));
            List modelObjects = session.createQuery(criteriaQuery).getResultList();
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
        boolean isAdmin;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();

            CriteriaQuery<Actor> criteriaQuery = builder.createQuery(Actor.class);
            Root<Actor> actorRoot = criteriaQuery.from(Actor.class);
            Root<ActorGroup> actorGroupRoot = criteriaQuery.from(ActorGroup.class);

            Join<ActorGroup, Group> group = actorGroupRoot.join("group");
            Join<Group, LookupValue> lookupValue = group.join("groupNameLookupValue");

            criteriaQuery.select(actorRoot)
                    .where(builder.and(
                            builder.equal(actorRoot.get("loginId"), actorGroupRoot.get("actorId")),
                            builder.equal(actorRoot.get("username"), loginName),
                            builder.equal(lookupValue.get("name"), "General-Admin")
                    ));

            List<Actor> actorList = session.createQuery(criteriaQuery).getResultList();
            isAdmin = actorList!=null && actorList.size()>0;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }

        return isAdmin;
    }

    public List<Actor> getAllActor(Session session) throws DAOException {
        List<Actor> actors = new ArrayList<>();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Actor> criteriaQuery = builder.createQuery(Actor.class);
            Root<Actor> actorRoot = criteriaQuery.from(Actor.class);
            criteriaQuery.select(actorRoot);
            List modelObjects = session.createQuery(criteriaQuery).getResultList();
            if (modelObjects != null && modelObjects.size() > 0) {
                for (Object modelObject : modelObjects) {
                    actors.add((Actor) modelObject);
                }
            }
        } catch(Exception ex) {
            throw new DAOException(ex);
        }
        return actors;
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

    public void writeActorGroup(List<ActorGroup> groups, Session session) throws DAOException {
        for(ActorGroup group : groups) {
            this.writeActorGroup(group, session);
        }
    }
    public void writeActorGroup(ActorGroup ag, Session session) throws DAOException {
        try {
            session.saveOrUpdate(ag);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    public void update( Actor actor, Session session ) throws DAOException {
        try {
            actor = (Actor) session.merge(actor);
            session.update(actor);
            session.flush();
        } catch ( Exception ex ) {
            throw new DAOException( ex );
        }
    }

    public void deleteActorGroup(List<ActorGroup> groups, Session session) throws DAOException {
        for(ActorGroup group : groups) {
            this.deleteActorGroup(group, session);
        }
    }
    public void deleteActorGroup(ActorGroup ag, Session session) throws DAOException {
        try {
            session.delete(session.contains(ag) ? ag : session.merge(ag));
        } catch(Exception ex) {
            throw new DAOException(ex);
        }
    }

    public List<ActorGroup> getActorGroup(Long userId, Session session) throws DAOException {
        List<ActorGroup> groups = new ArrayList<>();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<ActorGroup> agCriteriaQuery = builder.createQuery(ActorGroup.class);
            Root<ActorGroup> actorGroupRoot = agCriteriaQuery.from(ActorGroup.class);
            agCriteriaQuery.select(actorGroupRoot)
                    .where(builder.equal(actorGroupRoot.get("actorId"), userId));
            List modelObjects = session.createQuery(agCriteriaQuery).getResultList();
            if (modelObjects != null && modelObjects.size() > 0) {
                for (Object modelObject : modelObjects) {
                    groups.add((ActorGroup) modelObject);
                }
            }
        } catch(Exception ex) {
            throw new DAOException(ex);
        }

        return groups;
    }
}
