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

package org.jcvi.ometa.engine;

import org.jcvi.ometa.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/10/11
 * Time: 12:29 PM
 * 
 * Bears all of the data required for a multi-project-websites-type load.
 */
public class MultiLoadParameter implements Serializable {
    
    private List<List<LookupValue>> lookupValues;
    private List<List<Project>> projects;
    private List<List< Sample>> samples;
    private List<List<ProjectMetaAttribute>> pmas;
    private List<List<SampleMetaAttribute>> smas;
    private List<List<EventMetaAttribute>> emas;
    private List<List<FileReadAttributeBean>> projectRegistrationEventAttributes;
    private List<List<FileReadAttributeBean>> sampleRegistrationEventAttributes;
    private List<LoadableEventBean> otherEvents;

    private String projectRegistrationEventName;
    private String sampleRegistrationEventName;

    /**
     * Adders: all add list to the list of list.
     */
    public void addLookupValues( List<LookupValue> lvs ) {
        if ( getLookupValues() == null ) {
            lookupValues = new ArrayList<List<LookupValue>>();
        }
        getLookupValues().add(lvs);
    }

    public void addProjects( List<Project> newProjects ) {
        if ( getProjects() == null ) {
            projects = new ArrayList<List<Project>>();
        }
        getProjects().add(newProjects);
    }

    public void addSamples( List<Sample> newSamples ) {
        if ( getSamples() == null ) {
            samples = new ArrayList<List<Sample>>();
        }
        getSamples().add(newSamples);
    }

    public void addSampleMetaAttributes( List<SampleMetaAttribute> newMetaAttributes ) {
        if ( getSmas() == null ) {
            smas = new ArrayList<List<SampleMetaAttribute>>();
        }
        getSmas().add(newMetaAttributes);
    }

    public void addProjectMetaAttributes( List<ProjectMetaAttribute> newMetaAttributes ) {
        if ( getPmas() == null ) {
            pmas = new ArrayList<List<ProjectMetaAttribute>>();
        }
        getPmas().add(newMetaAttributes);
    }

    public void addEventMetaAttributes( List<EventMetaAttribute> newMetaAttributes ) {
        if ( getEmas() == null ) {
            emas = new ArrayList<List<EventMetaAttribute>>();
        }
        getEmas().add(newMetaAttributes);
    }

    public void addProjectRegistrations( String eventName, List<FileReadAttributeBean> registrationAttributes ) {
        if ( getProjectRegistrationEventAttributes() == null ) {
            projectRegistrationEventAttributes = new ArrayList<List<FileReadAttributeBean>>();
        }
        getProjectRegistrationEventAttributes().add(registrationAttributes);
        projectRegistrationEventName = eventName;
    }

    public void addSampleRegistrations( String eventName, List<FileReadAttributeBean> registrationAttributes ) {
        if ( getSampleRegistrationEventAttributes() == null ) {
            sampleRegistrationEventAttributes = new ArrayList<List<FileReadAttributeBean>>();
        }
        getSampleRegistrationEventAttributes().add( registrationAttributes );
        sampleRegistrationEventName = eventName;
    }

    public void addEvents( String eventName, List<FileReadAttributeBean> eventAttributes ) {
        if ( getOtherEvents() == null ) {
            otherEvents = new ArrayList<LoadableEventBean>();
        }
        LoadableEventBean leBean = new LoadableEventBean();
        leBean.setAttributes( eventAttributes );
        leBean.setEventName( eventName );
        getOtherEvents().add(leBean);
    }

    /** Getters all return list-of-list. */
    public List<List<LookupValue>> getLookupValues() {
        return lookupValues;
    }

    public List<List<Project>> getProjects() {
        return projects;
    }

    public List<List<Sample>> getSamples() {
        return samples;
    }

    public List<List<ProjectMetaAttribute>> getPmas() {
        return pmas;
    }

    public List<List<SampleMetaAttribute>> getSmas() {
        return smas;
    }

    public List<List<EventMetaAttribute>> getEmas() {
        return emas;
    }

    public List<List<FileReadAttributeBean>> getProjectRegistrationEventAttributes() {
        return projectRegistrationEventAttributes;
    }

    public List<List<FileReadAttributeBean>> getSampleRegistrationEventAttributes() {
        return sampleRegistrationEventAttributes;
    }

    public List<LoadableEventBean> getOtherEvents() {
        return otherEvents;
    }

    public String getProjectRegistrationEventName() {
        return projectRegistrationEventName;
    }

    public String getSampleRegistrationEventName() {
        return sampleRegistrationEventName;
    }

    /** Provides a way to group event type with its attributes. */
    public static class LoadableEventBean implements Serializable {
        private String eventName;
        private List<FileReadAttributeBean> attributes;

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public List<FileReadAttributeBean> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<FileReadAttributeBean> attributes) {
            this.attributes = attributes;
        }
    }
}
