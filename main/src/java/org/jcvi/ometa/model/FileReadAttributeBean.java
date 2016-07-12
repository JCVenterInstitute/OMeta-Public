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

import java.io.File;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/7/11
 * Time: 11:26 AM
 *
 * This type of bean is solely for read-up.  It will not be used for writeback to database.  It represents
 * all different kinds of attributes, and hence cannot be fitted back to the Hibernate models as the code/tables
 * are currently implemented.
 */
public class FileReadAttributeBean implements ProjectNamerOnFileRead, Serializable {
    private String projectName;
    private String sampleName;
    private String attributeName;
    private String attributeValue;
    private File upload;
    private String uploadFileName;
    private String uploadContentType;
    private String[] existingFileName;
    private String[] uploadFilePath;

    public String getProjectName() {
        return projectName;
    }

    @JCVI_BeanPopulator_Column
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSampleName() {
        return sampleName;
    }

    @JCVI_BeanPopulator_Column
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @JCVI_BeanPopulator_Column
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    @JCVI_BeanPopulator_Column
    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public File getUpload() {
        return upload;
    }

    public void setUpload(File upload) {
        this.upload = upload;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    @JCVI_BeanPopulator_Column
    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getUploadContentType() {
        return uploadContentType;
    }

    @JCVI_BeanPopulator_Column
    public void setUploadContentType(String uploadContentType) {
        this.uploadContentType = uploadContentType;
    }

    public String[] getExistingFileName() {
        return existingFileName;
    }

    @JCVI_BeanPopulator_Column
    public void setExistingFileName(String[] existingFileName) {
        this.existingFileName = existingFileName;
    }

    public String[] getUploadFilePath() {
        return uploadFilePath;
    }

    @JCVI_BeanPopulator_Column
    public void setUploadFilePath(String[] uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }
}
