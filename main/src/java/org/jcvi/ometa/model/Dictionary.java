package org.jcvi.ometa.model;

import org.jcvi.ometa.configuration.JCVI_BeanPopulator_Column;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by mkuscuog on 6/23/2015.
 */
@Entity
@Table(name="dictionary")
public class Dictionary implements ModelBean {

    private Long dictionaryId;
    private String dictionaryType;
    private String dictionaryCode;
    private String dictionaryValue;
    private Integer isActive;

    private Date creationDate;
    private Date modifiedDate;
    private Long createdBy;
    private Long modifiedBy;
    private String parentDependency;

    @Id
    @Column(name="dict_id", nullable=false)
    public Long getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    @Column(name="dict_type", nullable=false)
    public String getDictionaryType() {
        return dictionaryType;
    }

    @JCVI_BeanPopulator_Column("DictionaryType")
    public void setDictionaryType(String dictionaryType) {
        this.dictionaryType = dictionaryType;
    }

    @Column(name="dict_code", nullable=false)
    public String getDictionaryCode() {
        return dictionaryCode;
    }

    @JCVI_BeanPopulator_Column("DictionaryCode")
    public void setDictionaryCode(String dictionaryCode) {
        this.dictionaryCode = dictionaryCode;
    }

    @Column(name="dict_value", nullable=false)
    public String getDictionaryValue() {
        return dictionaryValue;
    }

    @JCVI_BeanPopulator_Column("DictionaryValue")
    public void setDictionaryValue(String dictionaryValue) {
        this.dictionaryValue = dictionaryValue;
    }

    @Column(name="dict_is_active", nullable=false)
    public Integer getIsActive() {
        return (isActive != null && isActive > 0) ? 1 : 0;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = (isActive != null && isActive > 0) ? 1 : 0;
    }

    @Column(name="dict_create_date", nullable=false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Column(name="dict_modify_date")
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Transient
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Transient
    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Transient
    public String getParentDependency() {
        return parentDependency;
    }

    @JCVI_BeanPopulator_Column("ParentDictionary")
    public void setParentDependency(String parentDependency) {
        this.parentDependency = parentDependency;
    }
}
