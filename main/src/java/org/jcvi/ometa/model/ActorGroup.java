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
import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 9/27/12
 * Time: 11:42 AM
 */

@Entity
@Table(name="actor_group")
public class ActorGroup implements Serializable {
    private Long actorGroupId;
    private Long actorId;
    private Long groupId;
    private Group group;
    private Date creationDate;

    @Id
    @Column(name="actgrp_id", nullable = false)
    public Long getActorGroupId() {
        return actorGroupId;
    }

    public void setActorGroupId(Long actorGroupId) {
        this.actorGroupId = actorGroupId;
    }

    @Column(name = "actgrp_actor_id", nullable = false)
    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    @Column(name = "actgrp_group_id", nullable = false)
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Column(name = "actgrp_create_date", nullable = false)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @OneToOne
    @JoinColumn(name = "actgrp_group_id", referencedColumnName = "group_id", insertable = false, updatable = false)
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
