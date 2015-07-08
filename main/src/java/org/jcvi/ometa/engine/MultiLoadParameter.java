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
    private List<List<Dictionary>> dictionaries;
    private List<List<Project>> projects;
    private List<ProjectPair> projectPairs;
    private List<SamplePair> samplePairs;
    private List<List<Sample>> samples;
    private List<List<ProjectMetaAttribute>> pmas;
    private List<List<SampleMetaAttribute>> smas;
    private List<List<EventMetaAttribute>> emas;
    private List<LoadableEventBean> otherEvents;
    private String eventName;

    private String submitterId;
    private Long submitterActorId;

    /**
     * Adders: all add list to the list of list.
     */
    public void addLookupValues( List<LookupValue> lvs ) {
        if ( getLookupValues() == null ) {
            lookupValues = new ArrayList<List<LookupValue>>();
        }
        getLookupValues().add(lvs);
    }

    public void addDictionaries( List<Dictionary> dicts) {
        if( getDictionaries() == null) {
            dictionaries = new ArrayList<List<Dictionary>>();
        }

        getDictionaries().add(dicts);
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

    public void addEvents(String eventName, List<FileReadAttributeBean> eventAttributes, int rowIndex) {
        if ( getOtherEvents() == null ) {
            otherEvents = new ArrayList<LoadableEventBean>();
        }
        LoadableEventBean leBean = new LoadableEventBean();
        leBean.setAttributes( eventAttributes );
        leBean.setEventName( eventName );
        leBean.setRowIndex(rowIndex);
        getOtherEvents().add(leBean);
    }

    /** Getters all return list-of-list. */
    public List<List<LookupValue>> getLookupValues() {
        return lookupValues;
    }

    public List<List<Dictionary>> getDictionaries() {
        return dictionaries;
    }

    public List<List<Project>> getProjects() {
        return projects;
    }

    public List<List<Sample>> getSamples() {
        return samples;
    }

    public void addProjectPair(Project project, List<FileReadAttributeBean> attributes, List<ProjectMetaAttribute> pmas, List<SampleMetaAttribute> smas, List<EventMetaAttribute> emas, int rowIndex) {
        if(this.getProjectPairs() == null) {
            projectPairs = new ArrayList<ProjectPair>();
        }
        ProjectPair projectPair = new ProjectPair();
        projectPair.setProject(project);
        projectPair.setPmas(pmas);
        projectPair.setSmas(smas);
        projectPair.setEmas(emas);
        projectPair.setAttributes(attributes);
        projectPair.setRowIndex(rowIndex);
        this.getProjectPairs().add(projectPair);
    }

    public List<ProjectPair> getProjectPairs() {
        return projectPairs;
    }

    public void addSamplePair(Sample sample, List<FileReadAttributeBean> attributes, int rowIndex) {
        if(this.getSamplePairs() == null) {
            samplePairs = new ArrayList<SamplePair>();
        }
        SamplePair samplePair = new SamplePair();
        samplePair.setSample(sample);
        samplePair.setAttributes(attributes);
        samplePair.setRowIndex(rowIndex);
        this.getSamplePairs().add(samplePair);
    }

    public List<SamplePair> getSamplePairs() {
        return samplePairs;
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

    public List<LoadableEventBean> getOtherEvents() {
        return otherEvents;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /** Provides a way to group event type with its attributes. */
    public static class LoadableEventBean implements Serializable {
        private String eventName;
        private List<FileReadAttributeBean> attributes;
        private int rowIndex;

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

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }
    }

    public class ProjectPair implements Serializable {
        private Project project; //project or sample
        private List<FileReadAttributeBean> attributes; //attributes for project/sample registration event
        private List<ProjectMetaAttribute> pmas;
        private List<SampleMetaAttribute> smas;
        private List<EventMetaAttribute> emas;
        private int rowIndex;

        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public List<FileReadAttributeBean> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<FileReadAttributeBean> attributes) {
            this.attributes = attributes;
        }

        public List<ProjectMetaAttribute> getPmas() {
            return pmas;
        }

        public void setPmas(List<ProjectMetaAttribute> pmas) {
            this.pmas = pmas;
        }

        public List<SampleMetaAttribute> getSmas() {
            return smas;
        }

        public void setSmas(List<SampleMetaAttribute> smas) {
            this.smas = smas;
        }

        public List<EventMetaAttribute> getEmas() {
            return emas;
        }

        public void setEmas(List<EventMetaAttribute> emas) {
            this.emas = emas;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }
    }

    public class SamplePair implements Serializable {
        private Sample sample;
        private List<FileReadAttributeBean> attributes;
        private int rowIndex;

        public Sample getSample() {
            return sample;
        }

        public void setSample(Sample sample) {
            this.sample = sample;
        }

        public List<FileReadAttributeBean> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<FileReadAttributeBean> attributes) {
            this.attributes = attributes;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }
    }


    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public Long getSubmitterActorId() {
        return submitterActorId;
    }

    public void setSubmitterActorId(Long submitterActorId) {
        this.submitterActorId = submitterActorId;
    }
}
