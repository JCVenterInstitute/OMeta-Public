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

package org.jcvi.ometa.model;

import org.jcvi.ometa.configuration.JCVI_BeanPopulator_Column;
import javax.persistence.*;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Nov 16, 2010
 * Time: 6:12:57 PM
 *
 * Holds event information.
 */
@Entity
@Table(name="event")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Event
        implements ModelBean, ProjectReferencingModelBean, SampleReferencingModelBean, ProjectNamerOnFileRead {
    public static final String ATTRIBUTE_NAME_HEADER  = "AttributeName";
    public static final String ATTRIBUTE_VALUE_HEADER = "AttributeValue";
    public static final String SAMPLE_NAME_HEADER = "SampleName";
    public static final String PROJECT_NAME_HEADER = "ProjectName";

    private Long eventId;
    private Long projectId;
    private Long sampleId;

    private Long createdBy;
    private Date creationDate;
    private Long modifiedBy;
    private Date modifiedDate;

    private String eventName;
    private String eventValue;
    private String sampleName;
    private String projectName;

    private Long eventTypeId;
    private Long eventStatusId;

    private LookupValue eventTypeLookupValue;
    private LookupValue eventStatusLookupValue;

    @Id
    @Column(name="event_id", nullable=false)
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Column(name="event_type_lkuvl_id", nullable=false)
    public Long getEventType() {
        return eventTypeId;
    }

    public void setEventType(Long eventType) {
        this.eventTypeId = eventType;
    }

    @Column(name="event_status_lkuvl_id", nullable=false)
    public Long getEventStatus() {
        return eventStatusId;
    }

    public void setEventStatus(Long eventStatusId) {
        this.eventStatusId = eventStatusId;
    }

    @Column(name="event_actor_created_by", nullable=false)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="event_create_date", nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="event_actor_modified_by", nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Column(name="event_modified_date", nullable=true)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Transient
    public String getEventName() {
        return eventName;
    }

    @JCVI_BeanPopulator_Column(ATTRIBUTE_NAME_HEADER)
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Transient
    public String getEventValue() {
        return eventValue;
    }

    @JCVI_BeanPopulator_Column(ATTRIBUTE_VALUE_HEADER)
    public void setEventValue(String eventValue) {
        this.eventValue = eventValue;
    }

    @Transient
    public String getSampleName() {
        return sampleName;
    }

    @JCVI_BeanPopulator_Column(SAMPLE_NAME_HEADER)
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    @Transient
    public String getProjectName() {
        return projectName;
    }

    @JCVI_BeanPopulator_Column(PROJECT_NAME_HEADER)
    public void setProjectName( String projectName ) {
        this.projectName = projectName;
    }

    @Column(name="event_projet_id", nullable=false)
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /** This column may not be populated, if this is not a sample-referencing event. */
    @Column(name="event_sampl_id", nullable=true)
    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId( Long sampleId ) {
        this.sampleId = sampleId;
    }

    @OneToOne
    @JoinColumn(name = "event_type_lkuvl_id", referencedColumnName = "lkuvlu_id", insertable = false, updatable = false)
    public LookupValue getEventTypeLookupValue() {
        return eventTypeLookupValue;
    }

    public void setEventTypeLookupValue(LookupValue eventTypeLookupValue) {
        this.eventTypeLookupValue = eventTypeLookupValue;
    }

    @OneToOne
    @JoinColumn(name = "event_status_lkuvl_id", referencedColumnName = "lkuvlu_id", insertable = false, updatable = false)
    public LookupValue getEventStatusLookupValue() {
        return eventStatusLookupValue;
    }

    public void setEventStatusLookupValue(LookupValue eventStatusLookupValue) {
        this.eventStatusLookupValue = eventStatusLookupValue;
    }
}
