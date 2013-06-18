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

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 7/24/12
 * Time: 11:20 AM
 */
public class GridBean {
    private String projectName;
    private String projectPublic;
    private String sampleName;
    private String samplePublic;
    private String parentSampleName;
    private List<FileReadAttributeBean> beanList;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPublic() {
        return projectPublic;
    }

    public void setProjectPublic(String projectPublic) {
        this.projectPublic = projectPublic;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getSamplePublic() {
        return samplePublic;
    }

    public void setSamplePublic(String samplePublic) {
        this.samplePublic = samplePublic;
    }

    public String getParentSampleName() {
        return parentSampleName;
    }

    public void setParentSampleName(String parentSampleName) {
        this.parentSampleName = parentSampleName;
    }

    public List<FileReadAttributeBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<FileReadAttributeBean> beanList) {
        this.beanList = beanList;
    }
}
