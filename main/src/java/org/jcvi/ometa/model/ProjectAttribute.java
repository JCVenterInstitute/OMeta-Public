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
 * Date: Oct 25, 2010
 * Time: 11:17:34 AM
 *
 * Bean that represents an attribute of a project.  Very little data here.  NOTE: with the four-way variant on
 * data type, will not be representing this in the usual way, with bean (annotation) driven file-read.
 */
@Entity
@Table(name="project_attribute")
public class ProjectAttribute implements ModelBean, AttributeModelBean {
    // These are examples of potential project attributes.
    //    ProjectName
    //    ProjectDescription
    //    ParentProjectName
    //    GrantName
    //    GrantAccountCode
    //    ProjectGroup
    //    PathogenCategory
    //    Collaborator
    //    ProjectStatus
    //    ProjectURL
    //    References

    // One of these attribute values will be used, based on the type given in the meta attribute.
    private Date attributeDateValue;
    private String attributeStringValue;
    private Double attributeFloatValue;
    private Integer attributeIntValue;

    private Long id;
    private Long projectId;
    private Long nameLookupValueId;

    // These are other pieces of information used in database tracking functions.  But they will
    // be based on other information gleaned from the environment at update time.
    private Long createdBy;
    private Long modifiedBy;
    private Date creationDate;
    private Date modificationDate;

    private ProjectMetaAttribute metaAttribute;

    @Id
    @Column(name="projea_id", nullable=false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="projea_attribute_str", nullable=true)
    public String getAttributeStringValue() {
        return attributeStringValue;
    }

    public void setAttributeStringValue(String attributeStringValue) {
        this.attributeStringValue = attributeStringValue;
    }

    @Column(name="projea_attribute_int", nullable=true)
    public Integer getAttributeIntValue() {
        return attributeIntValue;
    }

    public void setAttributeIntValue(Integer attributeIntValue) {
        this.attributeIntValue = attributeIntValue;
    }

    @Column(name="projea_attribute_float", nullable=true)
    public Double getAttributeFloatValue() {
        return attributeFloatValue;
    }

    public void setAttributeFloatValue(Double attributeFloatValue) {
        this.attributeFloatValue = attributeFloatValue;
    }

    @Column(name="projea_attribute_date", nullable=true)
    public Date getAttributeDateValue() {
        return attributeDateValue;
    }

    public void setAttributeDateValue(Date attributeValue) {
        this.attributeDateValue = attributeValue;
    }

    @Column(name="projea_projet_id", nullable=false)
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Column(name="projea_actor_created_by", nullable=false)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="projea_actor_modified_by", nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Column(name="projea_create_date", nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="projea_modified_date", nullable=true)
    public Date getModifiedDate() {
        return modificationDate;
    }

    public void setModifiedDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Column(name="projea_lkuvlu_attribute_id", nullable=false)
    public Long getNameLookupValueId() {
        return nameLookupValueId;
    }

    public void setNameLookupValueId(Long maId) {
        nameLookupValueId = maId;
    }

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "projea_lkuvlu_attribute_id", referencedColumnName = "projma_lkuvlu_attribute_id",insertable = false,updatable = false),
            @JoinColumn(name = "projea_projet_id", referencedColumnName = "projma_projet_id",insertable = false,updatable = false)
    })
    public ProjectMetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    public void setMetaAttribute(ProjectMetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }
}
