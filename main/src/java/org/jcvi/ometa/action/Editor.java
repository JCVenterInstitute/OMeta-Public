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

package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 6/20/11
 * Time: 11:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class Editor extends ActionSupport {
    private Logger logger = Logger.getLogger(Editor.class);

    private static final String LOOKUP_VALUE_TYPE_EVENT = "Event Type";
    private static final String LOOKUP_VALUE_TYPE_ATTRIBUTE = "Attribute";
    private static final String LOOKUP_VALUE_NAME_PROJECT_UPDATE = "ProjectUpdate";
    private static final String LOOKUP_VALUE_NAME_SAMPLE_UPDATE = "SampleUpdate";
    private final String LOOKUP_VALUE_NAME_IS_PUBLIC = "isPublic";
    private final String LOOKUP_VALUE_NAME_IS_SECURE = "isSecure";

    private String editType;

    private Long projectId;
    private String projectName;
    private Long sampleId;
    private String sampleName;
    private Long eventId;

    private int isPublic;
    private int isSecure;

    private Project project;
    private List<ProjectAttribute> projectElements;

    private Sample sample;
    private List<SampleAttribute> sampleElements;

    private Event event;

    private ReadBeanPersister readPersister;
    private ProjectSampleEventWritebackBusiness psewt;


    public Editor() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);

        UploadActionDelegate udelegate = new UploadActionDelegate();
        psewt = udelegate.initializeBusinessObject(logger, psewt);
    }

    public String projectEditOpen() {
        String rtnVal = ERROR;

        try {
            project = readPersister.getProject(projectId);
            projectElements = readPersister.getProjectAttributes(projectId);
            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
        }

        return rtnVal;
    }

    public String projectEditProcess() {
        String rtnVal = ERROR;
        UserTransaction tx = null;

        try {
            if (editType != null) {
                tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
                List<LookupValue> lkvList = new ArrayList<LookupValue>();
                List<FileReadAttributeBean> attrBeanList = new ArrayList<FileReadAttributeBean>();

                List<EventMetaAttribute> existingEmaList = readPersister.getEventMetaAttributes(projectId);
                List<String> existingEmaNames = new ArrayList<String>();
                for (EventMetaAttribute ema : existingEmaList) {
                    if (ema.getEventTypeLookupValue().getName().equals(LOOKUP_VALUE_NAME_PROJECT_UPDATE))
                        if (!existingEmaNames.contains(ema.getLookupValue().getName()))
                            existingEmaNames.add(ema.getLookupValue().getName());
                }

                if (readPersister.getLookupValue(LOOKUP_VALUE_NAME_PROJECT_UPDATE, ModelValidator.EVENT_TYPE_LV_TYPE_NAME) == null)
                    lkvList.add(this.createLookup(LOOKUP_VALUE_NAME_PROJECT_UPDATE, ModelValidator.EVENT_TYPE_LV_TYPE_NAME, ModelValidator.STRING_DATA_TYPE));

                if (editType.equals("project")) {
                    project = readPersister.getProject(projectId);

                    if (isPublic != project.getIsPublic() || isSecure != project.getIsSecure()) {
                        if (isPublic != project.getIsPublic()) {
                            if (readPersister.getLookupValue(LOOKUP_VALUE_NAME_IS_PUBLIC, ModelValidator.ATTRIBUTE_LV_TYPE_NAME) == null) {
                                lkvList.add(this.createLookup(LOOKUP_VALUE_NAME_IS_PUBLIC, ModelValidator.ATTRIBUTE_LV_TYPE_NAME, ModelValidator.INT_DATA_TYPE));
                            }

                            if (!existingEmaNames.contains(LOOKUP_VALUE_NAME_IS_PUBLIC))
                                emaList.add(
                                        this.createEventMetaAttribute(
                                                projectId, projectName, LOOKUP_VALUE_NAME_PROJECT_UPDATE, LOOKUP_VALUE_NAME_IS_PUBLIC,
                                                false, true, ModelValidator.INT_DATA_TYPE, "Project availability", false
                                        )
                                );

                            attrBeanList.add(this.createFileReadBean(LOOKUP_VALUE_NAME_IS_PUBLIC, "" + project.getIsPublic(), projectName, null));

                            project.setIsPublic(isPublic);
                        }

                        if (isSecure != project.getIsSecure()) {
                            if (readPersister.getLookupValue(LOOKUP_VALUE_NAME_IS_SECURE, ModelValidator.ATTRIBUTE_LV_TYPE_NAME) == null) {
                                lkvList.add(this.createLookup(LOOKUP_VALUE_NAME_IS_SECURE, ModelValidator.ATTRIBUTE_LV_TYPE_NAME, ModelValidator.INT_DATA_TYPE));
                            }

                            if (!existingEmaNames.contains(LOOKUP_VALUE_NAME_IS_SECURE))
                                emaList.add(
                                        this.createEventMetaAttribute(
                                                projectId, projectName, LOOKUP_VALUE_NAME_PROJECT_UPDATE,LOOKUP_VALUE_NAME_IS_SECURE,
                                                false, true, ModelValidator.INT_DATA_TYPE, "Project security", false
                                        )
                                );

                            attrBeanList.add(this.createFileReadBean(LOOKUP_VALUE_NAME_IS_SECURE, "" + project.getIsSecure(), projectName, null));

                            project.setIsSecure(isSecure);
                        }

                        if (lkvList.size() > 0)
                            psewt.loadLookupValues(lkvList);
                        if (emaList.size() > 0)
                            psewt.loadEventMetaAttributes(emaList);
                        if (attrBeanList.size() > 0)
                            psewt.loadAttributes(attrBeanList, LOOKUP_VALUE_NAME_PROJECT_UPDATE);
                        psewt.updateProject(project);
                    }

                } else if (editType.equals("projectAttribute")) {
                    Object oldValue = null, newValue = null;
                    List<ProjectAttribute> projectAttributes = readPersister.getProjectAttributes(projectId);

                    for (ProjectAttribute pa : projectAttributes) {
                        for (ProjectAttribute spa : projectElements) {
                            if(pa != null && spa != null) {
                                if (pa.getId().compareTo(spa.getId()) == 0) {
                                    oldValue = ModelValidator.getModelValue(pa.getMetaAttribute().getLookupValue(), pa);
                                    newValue = ModelValidator.getModelValue(pa.getMetaAttribute().getLookupValue(), spa);

                                    if (spa.getAttributeDateValue() != null && spa.getAttributeDateValue().compareTo(pa.getAttributeDateValue()) != 0) {
                                        pa.setAttributeDateValue(spa.getAttributeDateValue());
                                    } else if (spa.getAttributeFloatValue() != null && spa.getAttributeFloatValue().compareTo(pa.getAttributeFloatValue()) != 0) {
                                        pa.setAttributeFloatValue(spa.getAttributeFloatValue());
                                    } else if (spa.getAttributeIntValue() != null && spa.getAttributeIntValue().compareTo(pa.getAttributeIntValue()) != 0) {
                                        pa.setAttributeIntValue(spa.getAttributeIntValue());
                                    } else if (spa.getAttributeStringValue() != null && !spa.getAttributeStringValue().equals(pa.getAttributeStringValue())) {
                                        pa.setAttributeStringValue(spa.getAttributeStringValue());
                                    } else
                                        break;

                                    if (!existingEmaNames.contains(pa.getMetaAttribute().getAttributeName()))
                                        emaList.add(
                                                this.createEventMetaAttribute(
                                                        projectId, projectName, LOOKUP_VALUE_NAME_PROJECT_UPDATE, pa.getMetaAttribute().getAttributeName(),
                                                        false, true, pa.getMetaAttribute().getDataType(), pa.getMetaAttribute().getAttributeName(), false
                                                )
                                        );
                                    attrBeanList.add(createFileReadBean(pa.getMetaAttribute().getAttributeName(), "" + newValue, projectName, null));

                                }
                            }
                        }
                    }
                    if (lkvList.size() > 0)
                        psewt.loadLookupValues(lkvList);
                    if (emaList.size() > 0)
                        psewt.loadEventMetaAttributes(emaList);
                    if (attrBeanList.size() > 0)
                        psewt.loadAttributes(attrBeanList, LOOKUP_VALUE_NAME_PROJECT_UPDATE);
                }
            }

            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError(se.toString());
            }
        } finally {
            try {
                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                    tx.commit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return rtnVal;
    }

    public String sampleEditOpen() {
        String rtnVal = ERROR;
        try {
            sample = readPersister.getSample(sampleId);
            project = readPersister.getProject(projectId);
            sampleElements = readPersister.getSampleAttributes(sampleId);
            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
        }
        return rtnVal;
    }

    public String sampleEditProcess() {
        String rtnVal = ERROR;
        UserTransaction tx = null;
        try {
            if (editType != null) {
                tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
                List<LookupValue> lkvList = new ArrayList<LookupValue>();
                List<FileReadAttributeBean> attrBeanList = new ArrayList<FileReadAttributeBean>();

                List<EventMetaAttribute> existingEmaList = readPersister.getEventMetaAttributes(projectId);
                List<String> existingEmaNames = new ArrayList<String>();
                for (EventMetaAttribute ema : existingEmaList) {
                    if (ema.getEventTypeLookupValue().getName().equals(LOOKUP_VALUE_NAME_SAMPLE_UPDATE))
                        if (!existingEmaNames.contains(ema.getLookupValue().getName()))
                            existingEmaNames.add(ema.getLookupValue().getName());
                }

                if (readPersister.getLookupValue(LOOKUP_VALUE_NAME_SAMPLE_UPDATE, LOOKUP_VALUE_TYPE_EVENT) == null)
                    lkvList.add(this.createLookup(LOOKUP_VALUE_NAME_SAMPLE_UPDATE, LOOKUP_VALUE_TYPE_EVENT, ModelValidator.STRING_DATA_TYPE));

                if (editType.equals("sample")) {
                    sample = readPersister.getSample(sampleId);

                    if (isPublic != sample.getIsPublic()) {
                        if (readPersister.getLookupValue(LOOKUP_VALUE_NAME_IS_PUBLIC, LOOKUP_VALUE_TYPE_ATTRIBUTE) == null) {
                            lkvList.add(this.createLookup(LOOKUP_VALUE_NAME_IS_PUBLIC, LOOKUP_VALUE_TYPE_ATTRIBUTE, ModelValidator.INT_DATA_TYPE));
                        }

                        if (!existingEmaNames.contains(LOOKUP_VALUE_NAME_IS_PUBLIC))
                            emaList.add(
                                    this.createEventMetaAttribute(
                                            projectId, projectName, LOOKUP_VALUE_NAME_SAMPLE_UPDATE, LOOKUP_VALUE_NAME_IS_PUBLIC,
                                            false, true, ModelValidator.INT_DATA_TYPE, "Sample availability", true
                                    )
                            );

                        attrBeanList.add(this.createFileReadBean(LOOKUP_VALUE_NAME_IS_PUBLIC, "" + sample.getIsPublic(), projectName, sampleName));

                        sample.setIsPublic(isPublic);
                    }

                    if (lkvList.size() > 0)
                        psewt.loadLookupValues(lkvList);
                    if (emaList.size() > 0)
                        psewt.loadEventMetaAttributes(emaList);
                    if (attrBeanList.size() > 0)
                        psewt.loadAttributes(attrBeanList, LOOKUP_VALUE_NAME_SAMPLE_UPDATE);

                    sample.setProjectName(projectName);
                    psewt.updateSample(sample);

                } else if (editType.equals("sampleAttribute")) {
                    Object oldValue = null, newValue = null;
                    List<SampleAttribute> sampleAttributes = readPersister.getSampleAttributes(sampleId);

                    for (SampleAttribute sa : sampleAttributes) {
                        for (SampleAttribute ssa : sampleElements) {
                            if(sa != null && ssa != null) {
                                if (sa.getId().compareTo(ssa.getId()) == 0) {
                                    oldValue = ModelValidator.getModelValue(sa.getMetaAttribute().getLookupValue(), sa);
                                    newValue = ModelValidator.getModelValue(sa.getMetaAttribute().getLookupValue(), ssa);

                                    if (ssa.getAttributeDateValue() != null && ssa.getAttributeDateValue().compareTo(sa.getAttributeDateValue()) != 0) {
                                        sa.setAttributeDateValue(ssa.getAttributeDateValue());
                                    } else if (ssa.getAttributeFloatValue() != null && ssa.getAttributeFloatValue().compareTo(sa.getAttributeFloatValue()) != 0) {
                                        sa.setAttributeFloatValue(ssa.getAttributeFloatValue());
                                    } else if (ssa.getAttributeIntValue() != null && ssa.getAttributeIntValue().compareTo(sa.getAttributeIntValue()) != 0) {
                                        sa.setAttributeIntValue(ssa.getAttributeIntValue());
                                    } else if (ssa.getAttributeStringValue() != null && !ssa.getAttributeStringValue().equals(sa.getAttributeStringValue())) {
                                        sa.setAttributeStringValue(ssa.getAttributeStringValue());
                                    } else
                                        break;

                                    if (!existingEmaNames.contains(sa.getMetaAttribute().getAttributeName()))
                                        emaList.add(
                                                this.createEventMetaAttribute(
                                                        projectId, projectName, LOOKUP_VALUE_NAME_SAMPLE_UPDATE, sa.getMetaAttribute().getAttributeName(),
                                                        false, true, sa.getMetaAttribute().getDataType(), sa.getMetaAttribute().getAttributeName(), true
                                                )
                                        );
                                    attrBeanList.add(createFileReadBean(sa.getMetaAttribute().getAttributeName(), "" + newValue, projectName, sampleName));

                                }
                            }
                        }
                    }

                    if (lkvList.size() > 0)
                        psewt.loadLookupValues(lkvList);
                    if (emaList.size() > 0)
                        psewt.loadEventMetaAttributes(emaList);
                    if (attrBeanList.size() > 0)
                        psewt.loadAttributes(attrBeanList, LOOKUP_VALUE_NAME_SAMPLE_UPDATE);
                }
            }

            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError(se.toString());
            }
        } finally {
            try {
                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                    tx.commit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return rtnVal;
    }

    public String eventEditProcess(Long eventId) {
        String rtnVal = ERROR;

        logger.info("Editor - Process Event Edit");
        try {

            event = readPersister.getEvent(eventId);
            psewt.updateEventStatus(event);

            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        } finally {
        }

        return rtnVal;
    }

    private LookupValue createLookup(String name, String type, String dataType) {
        LookupValue rtnVal = new LookupValue();
        rtnVal.setName(name);
        rtnVal.setType(type);
        rtnVal.setDataType(dataType);

        return rtnVal;
    }

    private EventMetaAttribute createEventMetaAttribute(
            Long projectId, String projectName, String eventName,
            String attrName, boolean required,
            boolean active, String dataType, String desc, boolean sampleRequired) {
        EventMetaAttribute rtnVal = new EventMetaAttribute();
        rtnVal.setProjectId(projectId);
        rtnVal.setProjectName(projectName);
        rtnVal.setEventName(eventName);
        rtnVal.setAttributeName(attrName);
        rtnVal.setRequired(required);
        rtnVal.setActive(active);
        rtnVal.setDataType(dataType);
        rtnVal.setDesc(desc);
        rtnVal.setSampleRequired(sampleRequired);

        return rtnVal;
    }

    private FileReadAttributeBean createFileReadBean(String attrName, String attrValue, String projectName, String sampleName) {
        FileReadAttributeBean rtnVal = new FileReadAttributeBean();
        rtnVal.setAttributeName(attrName);
        rtnVal.setAttributeValue(attrValue);
        rtnVal.setProjectName(projectName);
        rtnVal.setSampleName(sampleName);

        return rtnVal;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public int getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(int isPublic) {
        this.isPublic = isPublic;
    }

    public int getIsSecure() {
        return isSecure;
    }

    public void setIsSecure(int isSecure) {
        this.isSecure = isSecure;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<ProjectAttribute> getProjectElements() {
        return projectElements;
    }

    public void setProjectElements(List<ProjectAttribute> projectElements) {
        this.projectElements = projectElements;
    }

    public String getEditType() {
        return editType;
    }

    public void setEditType(String editType) {
        this.editType = editType;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public List<SampleAttribute> getSampleElements() {
        return sampleElements;
    }

    public void setSampleElements(List<SampleAttribute> sampleElements) {
        this.sampleElements = sampleElements;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
