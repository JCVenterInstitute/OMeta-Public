package org.jcvi.ometa.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by mkuscuog on 6/25/2015.
 */
@Entity
@Table(name="dictionary_dependency")
public class DictionaryDependency {

    private Long dictionaryDependencyId;
    private Long dictionaryId;
    private Long parentId;

    private Date createdDate;
    private Date modifiedDate;

    @Id
    @Column(name="dict_dpcy_id", nullable=false)
    public Long getDictionaryDependencyId() {
        return dictionaryDependencyId;
    }

    public void setDictionaryDependencyId(Long dictionaryDependencyId) {
        this.dictionaryDependencyId = dictionaryDependencyId;
    }

    @Column(name="dict_id", nullable=false)
    public Long getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    @Column(name="parent_id", nullable=false)
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Column(name="dict_dpcy_create_date", nullable=false)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(name="dict_dpcy_modify_date")
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
