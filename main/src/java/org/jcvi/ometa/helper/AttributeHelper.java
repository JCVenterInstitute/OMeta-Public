package org.jcvi.ometa.helper;

import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.sql.Timestamp;
import java.util.*;

/**
 * User: movence
 * Date: 10/6/14
 * Time: 1:29 PM
 * org.jcvi.ometa.helper
 */
public class AttributeHelper {
    private ReadBeanPersister readPersister;

    public AttributeHelper(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    public AttributeHelper() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public List<AttributePair> getAllAttributeByIDs(Long projectId, Long eventId, String ids, String idType) throws Exception {
        List<AttributePair> pairList = new ArrayList<AttributePair>();

        Project currProject = readPersister.getProject(projectId);

        if(ids != null && !ids.isEmpty()) {
            String[] idArr = ids.split(",");
            boolean isProject = "p".equals(idType); //p for project, otherwise sample

            for(String currIdString : idArr) {
                Long currId = Long.parseLong(currIdString);
                ModelBean currCoreObject = isProject ? readPersister.getProject(currId) : readPersister.getSample(currId);

                Sample currSample = null;
                if(!isProject) {
                    currSample = (Sample)currCoreObject;
                }

                if(currCoreObject != null) {
                    Event currEvent = readPersister.getLatestEventForSample(projectId, isProject ? null : currSample.getSampleId(), eventId);

                    List<FileReadAttributeBean> beanList = new ArrayList<FileReadAttributeBean>();

                    Map<Long, EventAttribute> eaMap = new HashMap<Long, EventAttribute>();
                    if(currEvent != null) { //there could be no event, then move on to project/sample attributes
                        //get the latest event attributes
                        List<EventAttribute> eaList = readPersister.getEventAttributes(currEvent.getEventId(), projectId);
                        for(EventAttribute ea : eaList) {
                            eaMap.put(ea.getMetaAttribute().getLookupValue().getLookupValueId(), ea);
                        }
                    }

                    //get project/sample attributes
                    List<ProjectAttribute> paList = null;
                    Map<Long, ProjectAttribute> paMap = null;
                    List<SampleAttribute> saList = null;
                    Map<Long, SampleAttribute> saMap = null;
                    if(isProject) {
                        paList = readPersister.getProjectAttributes(projectId);
                        paMap = new HashMap<Long, ProjectAttribute>(paList.size());
                        for(ProjectAttribute pa : paList) {
                            paMap.put(pa.getMetaAttribute().getLookupValue().getLookupValueId(), pa);
                        }
                    } else {
                        saList = readPersister.getSampleAttributes(currSample.getSampleId());
                        saMap = new HashMap<Long, SampleAttribute>(saList.size());
                        for(SampleAttribute sa : saList) {
                            saMap.put(sa.getMetaAttribute().getLookupValue().getLookupValueId(), sa);
                        }
                    }

                    List<EventMetaAttribute> emaList = readPersister.getEventMetaAttributes(projectId, eventId);
                    for(EventMetaAttribute ema : emaList) {
                        FileReadAttributeBean frBean = new FileReadAttributeBean();
                        frBean.setAttributeName(ema.getLookupValue().getName());

                        AttributeModelBean attributeModelBean = null;
                        Long emaLookupValueId = ema.getLookupValue().getLookupValueId();
                        if(eaMap.containsKey(emaLookupValueId)) { //get the latest event attribute value
                            attributeModelBean = eaMap.get(emaLookupValueId);
                        } else {
                            if(isProject) {
                                if(paMap != null && paMap.containsKey(emaLookupValueId)) {
                                    attributeModelBean = paMap.get(emaLookupValueId);
                                }
                            } else {
                                if(saMap != null && saMap.containsKey(emaLookupValueId)) { //get sample attribute value if event attribute has no record
                                    attributeModelBean = saMap.get(emaLookupValueId);
                                }
                            }
                        }

                        Object attributeValue = "";
                        if(attributeModelBean != null) {
                            attributeValue = ModelValidator.getModelValue(ema.getLookupValue(), attributeModelBean);
                            if(attributeValue != null) {
                                if(attributeValue.getClass() == Date.class || attributeValue.getClass() == Timestamp.class) {
                                    attributeValue = CommonTool.convertTimestampToDate(attributeValue);
                                }
                            }
                        }
                        frBean.setAttributeValue("" + attributeValue);
                        frBean.setSampleName(currSample == null ? null : currSample.getSampleName());
                        frBean.setProjectName(currProject.getProjectName());
                        beanList.add(frBean);
                    }

                    AttributePair pair = new AttributePair();
                    pair.setType(currSample == null ? "project" : "sample");
                    pair.setProject(currProject);
                    pair.setProjectName(currProject.getProjectName());
                    pair.setSample(currSample);
                    pair.setAttributeList(beanList);
                    pairList.add(pair);
                }
            }
        }

        return pairList;
    }

    public static Map<String, String> attributeListToMap(List<FileReadAttributeBean> attributeList) {
        Map<String, String> resultMap = new HashMap<String, String>();

        if(attributeList != null && attributeList.size() > 0) {
            for(FileReadAttributeBean bean : attributeList) {
                resultMap.put(bean.getAttributeName(), bean.getAttributeValue());
            }
        }

        return resultMap;
    }
}
