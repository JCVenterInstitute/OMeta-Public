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

import org.jcvi.ometa.configuration.JCVI_BeanPopulator_Column;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/8/12
 * Time: 4:44 PM
 */
public class MetadataSetupReadBean implements Serializable {
    private String et;
    private String name;
    private String required;
    private String sampleRequired;
    private String active;
    private String options;
    private String desc;
    private Integer valueLength;
    private String value;
    private String label;
    private String ontology;

    private String projectMeta;
    private String sampleMeta;

    private String order;

    public String getEt() {
        return et;
    }

    @JCVI_BeanPopulator_Column
    public void setEt(String et) {
        this.et = et;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequired() {
        return required;
    }

    public Integer getRequiredDB() {
        return this.getRequired()!=null&&this.getRequired().equals("on") ? 1 : 0;
    }

    @JCVI_BeanPopulator_Column
    public void setRequired(String required) {
        this.required = required;
    }

    public String getSampleRequired() {
        return sampleRequired;
    }

    public Integer getSampleRequiredDB() {
        return this.getSampleRequired()!=null&&this.getSampleRequired().equals("on") ? 1 : 0;
    }

    @JCVI_BeanPopulator_Column
    public void setSampleRequired(String sampleRequired) {
        this.sampleRequired = sampleRequired;
    }

    public String getActive() {
        return active;
    }

    public Integer getActiveDB() {
        return this.getActive()!=null&&this.getActive().equals("on") ? 1 : 0;
    }

    @JCVI_BeanPopulator_Column
    public void setActive(String active) {
        this.active = active;
    }

    public String getOptions() {
        return options;
    }

    @JCVI_BeanPopulator_Column
    public void setOptions(String options) {
        this.options = options;
    }

    public String getDesc() {
        return desc;
    }

    @JCVI_BeanPopulator_Column
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    @JCVI_BeanPopulator_Column
    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    @JCVI_BeanPopulator_Column
    public void setLabel(String label) {
        this.label = label;
    }

    public String getOntology() {
        return ontology;
    }

    @JCVI_BeanPopulator_Column
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public Integer getValueLength() {
        return valueLength;
    }

    @JCVI_BeanPopulator_Column
    public void setValueLength(Integer valueLength) {
        this.valueLength = valueLength;
    }

    public String getProjectMeta() {
        return projectMeta;
    }
    public boolean getProjectMetaDB() {
        return this.getProjectMeta()!=null&&this.getProjectMeta().equals("on");
    }

    @JCVI_BeanPopulator_Column
    public void setProjectMeta(String projectMeta) {
        this.projectMeta = projectMeta;
    }

    public String getSampleMeta() {
        return sampleMeta;
    }
    public boolean getSampleMetaDB() {
        return this.getSampleMeta()!=null&&this.getSampleMeta().equals("on");
    }

    @JCVI_BeanPopulator_Column
    public void setSampleMeta(String sampleMeta) {
        this.sampleMeta = sampleMeta;
    }

    public String getOrder() {
        return order;
    }

    @JCVI_BeanPopulator_Column
    public void setOrder(String order) {
        this.order = order;
    }
}
