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
 * Time: 6:22:29 PM
 *
 * Describes (in turn) modifiers of an event.  What categories can be ascribed to an event.
 */
@Entity
@Table(name="event_meta_attribute")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EventMetaAttribute implements MetaAttributeModelBean, ProjectReferencingModelBean, ProjectNamerOnFileRead {
    private Long eventMetaAttributeId;
    private Long projectId;

    private boolean required;
    private boolean active;
    private Boolean sampleRequired;
    private String desc;
    private String options;
    private String dataType;
    private Date creationDate;
    private Date modifiedDate;
    private Long createdBy;
    private Long modifiedBy;

    private String attributeName;
    private String projectName;
    private String eventName;
    private Long nameLookupId;
    private Long eventTypeLookupId;

    private LookupValue eventTypeLookupValue;
    private LookupValue lookupValue;

    private String label;
    private String ontology;
    private Integer order;


    @Id
    @Column(name="evenma_id", nullable=false)
    public Long getEventMetaAttributeId() {
        return eventMetaAttributeId;
    }

    public void setEventMetaAttributeId(Long eventMetaAttributeId) {
        this.eventMetaAttributeId = eventMetaAttributeId;
    }

    @Column(name="evenma_projet_id", nullable=false)
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * This version is for hibernate/db use.  Let hibernate set using this hook.  Do not use it from a pojo client.
     * @param required 1 = T, 0 = F
     * @See setRequired
     */
    public void setRequiredDB( Integer required ) {
        this.required = required == 0 ? false : true;
    }

    /**
     * This version is for hibernate-only use.  Used by hibernate, and may be ignored when the boolean value is needed.
     * @return 1 = T, 0 = F
     * @See isRequired
     */
    @Column(name="evenma_is_required", nullable=false)
    public Integer getRequiredDB() {
        return required ? 1 : 0;
    }

    /**
     * This version is for internal use, as a boolean.  Use this when this pojo model is just a pojo.
     * @return required or not.
     * @See getRequiredDB
     */
    @Transient
    public Boolean isRequired() {
        return required;
    }

    /**
     * This version is for internal and file use.  Set here when this pojo model is just a pojo.
     * @param required T -> it is an error not to set this attribute.
     * @See setRequiredDB
     */
    @JCVI_BeanPopulator_Column
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * This version is for hibernate/db use.  Let hibernate set using this hook.  Do not use it from a pojo client.
     * @param active 1 = T, 0 = F
     * @See setRequired
     */
    public void setActiveDB( Integer active ) {
        this.active = active == 0 ? false : true;
    }

    /**
     * This version is for hibernate-only use.  Used by hibernate, and may be ignored when the boolean value is needed.
     * @return 1 = T, 0 = F
     * @See isRequired
     */
    @Column(name="evenma_is_active", nullable=false)
    public Integer getActiveDB() {
        return active ? 1 : 0;
    }

    /**
     * This version is for internal use, as a boolean.  Use this when this pojo model is just a pojo.
     * @return required or not.
     * @See getRequiredDB
     */
    @Transient
    public Boolean isActive() {
        return active;
    }

    /**
     * This version is for internal and file use.  Set here when this pojo model is just a pojo.
     * @param active T -> it is an error to use this attribute.
     * @See setRequiredDB
     */
    @JCVI_BeanPopulator_Column
    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setSampleRequiredDB( Integer sampleRequired ) {
        this.sampleRequired = sampleRequired == 0 ? false : true;
    }
    @Column(name="evenma_is_sample_required", nullable=false)
    public Integer getSampleRequiredDB() {
        return sampleRequired ? 1 : 0;
    }
    @Transient
    public Boolean isSampleRequired() {
        return sampleRequired;
    }
    @JCVI_BeanPopulator_Column
    public void setSampleRequired(Boolean sampleRequired) {
        this.sampleRequired = sampleRequired;
    }

    @Column(name="evenma_desc", nullable=true)
    public String getDesc() {
        return desc;
    }

    @JCVI_BeanPopulator_Column
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Column(name="evenma_options", nullable=true)
    public String getOptions() {
        return options;
    }

    @JCVI_BeanPopulator_Column
    public void setOptions(String options) {
        this.options = options;
    }

    @Column(name="evenma_create_date", nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="evenma_modified_date", nullable=true)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Column(name="evenma_actor_created_by", nullable=false)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="evenma_actor_modified_by", nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Transient
    public String getAttributeName() {
        return attributeName;
    }

    @JCVI_BeanPopulator_Column
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Transient
    public String getProjectName() {
        return projectName;
    }

    @JCVI_BeanPopulator_Column
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Transient
    public String getEventName() {
        if ( eventName == null   &&  eventTypeLookupValue != null ) {
            return eventTypeLookupValue.getName();
        }
        else {
            return eventName;
        }
    }

    @JCVI_BeanPopulator_Column
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Column(name="evenma_lkuvlu_attribute_id", nullable=false)
    public Long getNameLookupId() {
        return nameLookupId;
    }

    public void setNameLookupId(Long nameLookupId) {
        this.nameLookupId = nameLookupId;
    }

    @Transient
    public String getDataType() {
        if ( dataType == null   &&  lookupValue != null ) {
            return lookupValue.getDataType();
        }
        else {
            return dataType;
        }
    }

    @JCVI_BeanPopulator_Column
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Column(name="evenma_event_type_lkuvl_id", nullable=false)
    public Long getEventTypeLookupId() {
        return eventTypeLookupId;
    }

    public void setEventTypeLookupId(Long eventTypeLookupId) {
        this.eventTypeLookupId = eventTypeLookupId;
    }

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evenma_event_type_lkuvl_id", referencedColumnName = "lkuvlu_id", insertable = false, updatable = false)
    public LookupValue getEventTypeLookupValue() {
        return eventTypeLookupValue;
    }

    public void setEventTypeLookupValue(LookupValue eventTypeLookupValue) {
        this.eventTypeLookupValue = eventTypeLookupValue;
    }

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evenma_lkuvlu_attribute_id", referencedColumnName = "lkuvlu_id", insertable = false, updatable = false)
    public LookupValue getLookupValue() {
        return lookupValue;
    }

    public void setLookupValue(LookupValue lookupValue) {
        this.lookupValue = lookupValue;
    }

    @Column(name = "evenma_label", nullable = true)
    public String getLabel() {
        return label;
    }

    @JCVI_BeanPopulator_Column
    public void setLabel(String label) {
        this.label = label;
    }

    @Column(name = "evenma_ontology", nullable = true)
    public String getOntology() {
        return ontology;
    }

    @JCVI_BeanPopulator_Column
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @Column(name = "evenma_order", nullable = true)
    public Integer getOrder() {
        return order;
    }

    @JCVI_BeanPopulator_Column
    public void setOrder(Integer order) {
        this.order = order;
    }
}
