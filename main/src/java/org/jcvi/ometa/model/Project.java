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
 * Date: Oct 26, 2010
 * Time: 5:26:45 PM
 *
 * This is a project.  It contains only basic, simple information, but may be described
 * by an endless set of attributes.  Which attributes?  Why, the extent of them is
 * unbounded, and new ones may be added at any time.
 */
@Entity
@Table(name="project")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Project implements ModelBean, ProjectReferencingModelBean, ProjectNamerOnFileRead {
    private Long projectId;
    private Long parentProjectId;
    private String parentProjectName; // Points to parent.  May be null.

    private String projectName;
    private Integer projectLevel;

    private Date creationDate;
    private Date modificationDate;
    private Long createdBy;
    private Long modifiedBy;

    private Long editGroup;
    private Long viewGroup;

    private Integer isPublic;
    private Integer isSecure;

    @Transient
    public String getParentProjectName() {
        return parentProjectName;
    }

    @JCVI_BeanPopulator_Column
    public void setParentProjectName(String parentProjectName) {
        this.parentProjectName = parentProjectName;
    }

    @Column(name="projet_name", nullable=false)
    public String getProjectName() {
        return projectName;
    }

    @JCVI_BeanPopulator_Column
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Column(name="projet_level", nullable=false)
    public Integer getProjectLevel() {
        return projectLevel;
    }

    @JCVI_BeanPopulator_Column
    public void setProjectLevel(Integer projectLevel) {
        this.projectLevel = projectLevel;
    }

    @Id
    @Column(name="projet_id", nullable=false)
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /** Try to keep "public" variable to 1 or 0 at all times. */
    @Column(name="projet_is_public", nullable=false)
    public Integer getIsPublic() {
        return (isPublic != null && isPublic > 0) ? 1 : 0;
    }

    @JCVI_BeanPopulator_Column("Public")
    public void setIsPublic( Integer isPublic ) {
        this.isPublic = (isPublic != null && isPublic > 0) ? 1 : 0;
    }

    @Column(name="projet_is_secure", nullable=false)
    public Integer getIsSecure() {
        return (isSecure != null && isSecure > 0) ? 1 : 0;
    }

    @JCVI_BeanPopulator_Column("Secure")
    public void setIsSecure( Integer isSecure ) {
        this.isSecure = (isSecure != null && isSecure > 0) ? 1 : 0;
    }

    @Column(name="projet_create_date", nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="projet_modified_date", nullable=true)
    public Date getModifiedDate() {
        return modificationDate;
    }

    public void setModifiedDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Column(name="projet_actor_created_by", nullable=false)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="projet_actor_modified_by", nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Column(name="projet_projet_parent_id", nullable=true)
    public Long getParentProjectId() {
        return parentProjectId;
    }

    public void setParentProjectId(Long parentProjectId) {
        this.parentProjectId = parentProjectId;
    }

    @Column(name="projet_edit_group_id", nullable=true)
    public Long getEditGroup() {
        return editGroup;
    }

    public void setEditGroup(Long editGroup) {
        this.editGroup = editGroup;
    }

    @Column(name="projet_view_group_id", nullable=true)
    public Long getViewGroup() {
        return viewGroup;
    }

    public void setViewGroup(Long viewGroup) {
        this.viewGroup = viewGroup;
    }
}
