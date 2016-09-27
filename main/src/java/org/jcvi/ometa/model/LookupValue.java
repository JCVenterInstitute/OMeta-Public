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

import javax.persistence.Transient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import org.jcvi.ometa.configuration.JCVI_BeanPopulator_Column;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Nov 16, 2010
 * Time: 6:15:39 PM
 *
 * Broadly-purposed name-value pair in database.
 */
@Entity
@Table(name="lookup_value")
public class LookupValue implements ModelBean {
    private Long lookupValueId;
    private String name;
    private String dataType;
    private String type;  // Q: should be a lookupType?

    private Long createdBy;
    private Long modifiedBy;
    private Date creationDate;
    private Date modifiedDate;

    @Id
    @Column(name="lkuvlu_id", nullable=false)
    public Long getLookupValueId() {
        return lookupValueId;
    }

    public void setLookupValueId(Long lookupValueId) {
        this.lookupValueId = lookupValueId;
    }

    @Column(name="lkuvlu_name", nullable=false)
    public String getName() {
        return name;
    }

    @JCVI_BeanPopulator_Column("AttributeName")
    public void setName(String name) {
        this.name = name;
    }

    @Column(name="lkuvlu_data_type", nullable=false)
    public String getDataType() {
        return dataType;
    }

    @JCVI_BeanPopulator_Column("AttributeDataType")
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Column(name="lkuvlu_type", nullable=false)
    public String getType() {
        return type;
    }

    @JCVI_BeanPopulator_Column("AttributeType")
    public void setType(String type) {
        this.type = type;
    }

    //@Column(name="lkuvlu_created_by", nullable=false)
    @Transient
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    //@Column(name="lkuvlu_modified_by", nullable=false)
    @Transient
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Column(name="lkuvlu_create_date", nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="lkuvlu_modify_date", nullable=true)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

}
