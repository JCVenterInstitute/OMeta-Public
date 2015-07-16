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
 * Date: Oct 22, 2010
 * Time: 5:40:02 PM
 *
 * This is a "character" of a value pertaining to a sample.  The full set of these will
 * tell all the possible things that can be used to describe the given sample, so that
 * changes to those values can be tracked downstream.  OR: Meta-Attributes of the project.
 * OR: describes what/how attributes can appear for the named project.
 *
 * Sample attributes do not describe 1 sample, per-se, but rather what attributes may
 * be applied to ALL samples within a project.
 */
@Entity
@Table(name="sample_meta_attribute")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SampleMetaAttribute implements MetaAttributeModelBean, ProjectReferencingModelBean, ProjectNamerOnFileRead {
    private Long id;
    private Long projectId;
    private String projectName;
    private String dataType;
    private Boolean required;
    private Boolean active;
    private String attributeName;
    private String desc;
    private Date createDate;
    private Date modifiedDate;
    private Long createdBy;
    private Long modifiedBy;
    private Long nameLookupId;
    private String options;  // semicolon-separated values.
    private String label;
    private String ontology;

    private LookupValue lookupValue;
    private Integer valueLength;

    @Id
    @Column(name="sampma_id",nullable=false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public String getProjectName() {
        return projectName;
    }

    @JCVI_BeanPopulator_Column
    public void setProjectName( String projectName) {
        this.projectName = projectName;
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

    @Transient
    public Integer getValueLength() {
        return valueLength;
    }

    @JCVI_BeanPopulator_Column
    public void setValueLength(Integer valueLength) {
        this.valueLength = valueLength;
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
     * This version is for hibernate-only use.  Used by hibernate, and may be ignored when the boolean value is needed.
     * @return 1 = T, 0 = F
     * @See isRequired
     */
    @Column(name="sampma_is_required",nullable=false)
    public Integer getRequiredDB() {
        return required ? 1 : 0;
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
    @Column(name="sampma_is_active", nullable=false)
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

    @Column(name="sampma_options",nullable=true)
    public String getOptions() {
        return options;
    }

    @JCVI_BeanPopulator_Column
    public void setOptions(String options) {
        this.options = options;
    }

    @Transient
    public String getAttributeName() {
        if ( attributeName == null   &&   lookupValue != null ) {
            return lookupValue.getName();
        }
        else {
            return attributeName;
        }
    }

    @JCVI_BeanPopulator_Column
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Column(name="sampma_attribute_desc",nullable=true)
    public String getDesc() {
        return desc;
    }

    @JCVI_BeanPopulator_Column
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Column(name="sampma_projet_id",nullable=false)
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Column(name="sampma_create_date",nullable=false)
    public Date getCreationDate() {
        return createDate;
    }

    public void setCreationDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name="sampma_modified_date",nullable=true)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Column(name="sampma_actor_created_by",nullable=false)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="sampma_actor_modified_by",nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Column(name="sampma_lkuvlu_attribute_id", nullable=false)
    public Long getNameLookupId() {
        return nameLookupId;
    }

    public void setNameLookupId(Long nameLookupId) {
        this.nameLookupId = nameLookupId;
    }


    @OneToOne
    @JoinColumn(name = "sampma_lkuvlu_attribute_id", referencedColumnName = "lkuvlu_id", insertable = false, updatable = false)
    public LookupValue getLookupValue() {
        return lookupValue;
    }

    public void setLookupValue(LookupValue lookupValue) {
        this.lookupValue = lookupValue;
    }

    @Column(name="sampma_label",nullable = true)
    public String getLabel() {
        return label;
    }

    @JCVI_BeanPopulator_Column
    public void setLabel(String label) {
        this.label = label;
    }

    @Column(name = "sampma_ontology", nullable = true)
    public String getOntology() {
        return ontology;
    }

    @JCVI_BeanPopulator_Column
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }
}