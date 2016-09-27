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

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Nov 16, 2010
 * Time: 6:18:18 PM
 *
 * Attributes about an event.  Something that happens to a project.
 */
@Entity
@Table(name="event_attribute")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventAttribute implements ModelBean, AttributeModelBean {
    private Long eventAttributeId;
    private Long eventId;
    private Long projectId;
    private Long sampleId;
    private Long nameLookupValueId;

    // One of these three values will be used.
    private Date attributeDateValue;
    private Double attributeFloatValue;
    private String attributeStringValue;
    private Integer attributeIntValue;

    private Long createdBy;
    private Long modifiedBy;
    private Date creationDate;
    private Date modifiedDate;

    private EventMetaAttribute metaAttribute;

    @Column(name="eventa_event_id",nullable=false)
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Id
    @Column(name="eventa_id",nullable=false)
    public Long getId() {
        return eventAttributeId;
    }

    public void setId(Long eventAttributeId) {
        this.eventAttributeId = eventAttributeId;
    }

    @Column(name="eventa_lkuvlu_attribute_id", nullable=false)
    public Long getNameLookupValueId() {
        return nameLookupValueId;
    }

    public void setNameLookupValueId(Long maId) {
        nameLookupValueId = maId;
    }

    @Column(name="eventa_attribute_date",nullable=true)
    public Date getAttributeDateValue() {
        return attributeDateValue;
    }

    public void setAttributeDateValue(Date attributeDate) {
        this.attributeDateValue = attributeDate;
    }

    @Column(name="eventa_attribute_int", nullable=true)
    public Integer getAttributeIntValue() {
        return attributeIntValue;
    }

    public void setAttributeIntValue(Integer attributeIntValue) {
        this.attributeIntValue = attributeIntValue;
    }

    @Column(name="eventa_attribute_float",nullable=true)
    public Double getAttributeFloatValue() {
        return attributeFloatValue;
    }

    public void setAttributeFloatValue(Double attributeFloat) {
        this.attributeFloatValue = attributeFloat;
    }

    @Column(name="eventa_attribute_str",nullable=true)
    public String getAttributeStringValue() {
        return attributeStringValue;
    }

    public void setAttributeStringValue(String attributeString) {
        this.attributeStringValue = attributeString;
    }

    @Column(name="eventa_actor_created_by",nullable=true)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="eventa_actor_modified_by",nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Column(name="eventa_create_date",nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="eventa_modified_date",nullable=true)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    //@Column(name="eventa_projet_id",nullable=false)
    @Transient
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Transient
    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    //@OneToOne
    //@JoinColumn(name = "eventa_lkuvlu_attribute_id", referencedColumnName = "evenma_lkuvlu_attribute_id", insertable = false, updatable = false)
    @Transient
    public EventMetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public void setMetaAttribute(EventMetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }
}
