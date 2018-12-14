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

package org.jcvi.ometa.model.web;

import org.jcvi.ometa.model.EventMetaAttribute;

import java.io.Serializable;

/**
 * User: hkim
 * Date: 6/7/13
 * Time: 11:21 AM
 * org.jcvi.ometa.model.web
 */
public class EventMetaAttributeContainer implements Serializable {
    private EventMetaAttribute ema;
    private boolean projectMeta;
    private boolean sampleMeta;

    public EventMetaAttributeContainer(EventMetaAttribute ema) {
        this.ema = ema;
    }

    public EventMetaAttribute getEma() {
        return ema;
    }

    public void setEma(EventMetaAttribute ema) {
        this.ema = ema;
    }

    public boolean isProjectMeta() {
        return projectMeta;
    }

    public void setProjectMeta(boolean projectMeta) {
        this.projectMeta = projectMeta;
    }

    public boolean isSampleMeta() {
        return sampleMeta;
    }

    public void setSampleMeta(boolean sampleMeta) {
        this.sampleMeta = sampleMeta;
    }
}
