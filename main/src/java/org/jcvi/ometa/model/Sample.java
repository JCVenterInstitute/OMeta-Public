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
 * Time: 5:26:50 PM
 *
 * This is a sample.  It is populated with a basic set of values, but then it can be
 * described by a lot of attributes. Which attributes?  The set of attributes describing
 * it is unbounded, and may be modified at any time.
 */
@Entity
@Table(name="sample")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Sample implements ModelBean, ProjectReferencingModelBean, ProjectNamerOnFileRead {
    private Long sampleId;
    private String sampleName;
    private Long projectId;
    private String projectName;

    private Long parentSampleId;
    private String parentSampleName;
    private Integer sampleLevel;

    private Long createdBy;
    private Long modifiedBy;

    private Date modifiedDate;
    private Date createdDate;

    private Integer isPublic;

    @Column(name="sample_name", nullable=false)
    public String getSampleName() {
        return sampleName;
    }

    @JCVI_BeanPopulator_Column
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    @Transient
    public String getProjectName() {
        return projectName;
    }

    @JCVI_BeanPopulator_Column
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Id
    @Column(name="sample_id", nullable=false)
    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    /** Try to keep "public" variable to 1 or 0 at all times. */
    @Column(name="sample_is_public", nullable=false)
    public Integer getIsPublic() {
        return (isPublic != null && isPublic > 0) ? 1 : 0;
    }

    @JCVI_BeanPopulator_Column("Public")
    public void setIsPublic( Integer isPublic ) {
        this.isPublic = (isPublic != null && isPublic > 0) ? 1 : 0;
    }

    @Column(name="sample_projet_id", nullable=false)
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Column(name = "sample_sample_parent_id", nullable = true)
    public Long getParentSampleId() {
        return parentSampleId;
    }

    public void setParentSampleId(Long parentSampleId) {
        this.parentSampleId = parentSampleId;
    }

    @Transient
    public String getParentSampleName() {
        return parentSampleName;
    }

    @JCVI_BeanPopulator_Column
    public void setParentSampleName(String parentSampleName) {
        this.parentSampleName = parentSampleName;
    }

    @Column(name = "sample_level", nullable = false)
    public Integer getSampleLevel() {
        return sampleLevel;
    }

    @JCVI_BeanPopulator_Column
    public void setSampleLevel(Integer sampleLevel) {
        this.sampleLevel = sampleLevel;
    }

    @Column(name="sample_modified_date", nullable=true)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Column(name="sample_create_date", nullable=false)
    public Date getCreationDate() {
        return createdDate;
    }

    public void setCreationDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name="sample_created_by", nullable=false)
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="sample_modified_by", nullable=true)
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
