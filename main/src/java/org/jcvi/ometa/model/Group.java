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

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/9/11
 * Time: 11:30 PM
 *
 * Models a group row from the database.
 */

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="groups")
public class Group implements Serializable {
    private Long groupId;
    private Long nameLookupId;
    private LookupValue groupNameLookupValue;

    @Id
    @Column(name="group_id", nullable=false)
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @OneToOne
    @JoinColumn(name = "group_name_lkuvl_id", referencedColumnName = "lkuvlu_id", insertable = false, updatable = false)
    public LookupValue getGroupNameLookupValue() {
        return groupNameLookupValue;
    }

    public void setGroupNameLookupValue(LookupValue groupNameLookupValue) {
        this.groupNameLookupValue = groupNameLookupValue;
    }

    @Column(name="group_name_lkuvl_id")
    public Long getNameLookupId() {
        return nameLookupId;
    }

    public void setNameLookupId(Long nameLookupId) {
        this.nameLookupId = nameLookupId;
    }
}
