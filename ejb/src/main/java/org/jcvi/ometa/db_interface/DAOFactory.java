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

package org.jcvi.ometa.db_interface;

import org.jcvi.ometa.hibernate.dao.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/6/11
 * Time: 11:22 PM
 *
 * Data Access Object factory: the means of getting the DAO for persisting/de-persisting data.
 */
public class DAOFactory {
    private ActorDAO actorDAO;
    private LookupValueDAO lookupValueDAO;
    private ProjectDAO projectDAO;
    private ProjectAttributeDAO projectAttributeDAO;
    private ProjectMetaAttributeDAO projectMetaAttributeDAO;
    private SampleDAO sampleDAO;
    private SampleAttributeDAO sampleAttributeDAO;
    private SampleMetaAttributeDAO sampleMetaAttributeDAO;
    private EventMetaAttributeDAO eventMetaAttributeDAO;
    private EventAttributeDAO eventAttributeDAO;
    private EventDAO eventDAO;
    private SecurityDAO securityDAO;
    private GroupDAO groupDAO;
    private DictionaryDAO dictionaryDAO;

    public ProjectDAO getProjectDAO() {
        if (projectDAO == null) {
            projectDAO = new ProjectDAO();
        }
        return projectDAO;
    }

    public ProjectAttributeDAO getProjectAttributeDAO() {
        if (projectAttributeDAO == null) {
            projectAttributeDAO = new ProjectAttributeDAO();
        }
        return projectAttributeDAO;
    }

    public ProjectMetaAttributeDAO getProjectMetaAttributeDAO() {
        if (projectMetaAttributeDAO == null) {
            projectMetaAttributeDAO = new ProjectMetaAttributeDAO();
        }
        return projectMetaAttributeDAO;
    }

    public SampleDAO getSampleDAO() {
        if (sampleDAO == null) {
            sampleDAO = new SampleDAO();
        }
        return sampleDAO;
    }

    public SampleAttributeDAO getSampleAttributeDAO() {
        if (sampleAttributeDAO == null) {
            sampleAttributeDAO = new SampleAttributeDAO();
        }
        return sampleAttributeDAO;
    }

    public SampleMetaAttributeDAO getSampleMetaAttributeDAO() {
        if (sampleMetaAttributeDAO == null) {
            sampleMetaAttributeDAO = new SampleMetaAttributeDAO();
        }
        return sampleMetaAttributeDAO;
    }

    public EventMetaAttributeDAO getEventMetaAttributeDAO() {
        if (eventMetaAttributeDAO == null) {
            eventMetaAttributeDAO = new EventMetaAttributeDAO();
        }
        return eventMetaAttributeDAO;
    }

    public ActorDAO getActorDAO() {
        if (actorDAO == null) {
            actorDAO = new ActorDAO();
        }
        return actorDAO;
    }

    public LookupValueDAO getLookupValueDAO() {
        if (lookupValueDAO == null) {
            lookupValueDAO = new LookupValueDAO();
        }
        return lookupValueDAO;
    }


    public EventAttributeDAO getEventAttributeDAO() {
        if (eventAttributeDAO == null) {
            eventAttributeDAO = new EventAttributeDAO();
        }
        return eventAttributeDAO;
    }

    public EventDAO getEventDAO() {
        if ( eventDAO == null ) {
            eventDAO = new EventDAO();
        }
        return eventDAO;
    }

    public SecurityDAO getSecurityDAO() {
        if ( securityDAO == null ) {
            securityDAO = new SecurityDAO();
        }
        return securityDAO;
    }

    public GroupDAO getGroupDAO() {
        if( groupDAO == null ) {
            groupDAO = new GroupDAO();
        }
        return groupDAO;
    }

    public DictionaryDAO getDictionaryDAO(){
        if( dictionaryDAO == null ) {
            dictionaryDAO = new DictionaryDAO();
        }
        return dictionaryDAO;
    }
}
