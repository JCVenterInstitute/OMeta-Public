package org.jcvi.ometa.helper;

import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.Sample;

import java.util.List;

/**
 * User: movence
 * Date: 10/6/14
 * Time: 10:56 PM
 * org.jcvi.ometa.helper
 */
public class AttributePair {
    private String type;
    private String projectName;
    private Project project;
    private Sample sample;
    private List<FileReadAttributeBean> attributeList;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public List<FileReadAttributeBean> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<FileReadAttributeBean> attributeList) {
        this.attributeList = attributeList;
    }
}
