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

package org.jcvi.ometa.action.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.model.ProjectMetaAttribute;
import org.jcvi.ometa.model.SampleMetaAttribute;
import org.jcvi.ometa.model.web.EventMetaAttributeContainer;
import org.jcvi.ometa.utils.PresentationActionDelegate;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/15/11
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataSetupAjax extends ActionSupport implements IAjaxAction {
    private Logger logger = Logger.getLogger(MetadataSetupAjax.class);

    private ProjectSampleEventPresentationBusiness psept;

    private List dynamicList;
    private String type;
    private Long projectId;
    private String eventName;

    private String lvDataType;
    private String lvName;
    private String lvType;

    private String lerror;
    private String errorMsg;

    public MetadataSetupAjax() {
        PresentationActionDelegate pdeledate = new PresentationActionDelegate();
        psept = pdeledate.initializeEjb(logger, psept);
    }

    public String runAjax() {
        String returnValue = ERROR;
        try {
            if("g_ema".equals(type)) { //event meta attribute
                LookupValue eventTypeLV=null;
                if(eventName!=null && !eventName.isEmpty()){
                    eventTypeLV = psept.getLookupValue(eventName, ModelValidator.EVENT_TYPE_LV_TYPE_NAME);
                }
                List<EventMetaAttribute> emas = psept.getEventMetaAttributes(projectId, eventTypeLV==null?null:eventTypeLV.getLookupValueId());
                if(emas.size()>0) {

                    //order by orders
                    //group ema by event type
                    Map<String, List<EventMetaAttribute>> groupedList = new HashMap<String, List<EventMetaAttribute>>();
                    for (EventMetaAttribute ema : emas) {
                        LookupValue lv = ema.getEventTypeLookupValue();
                        if(groupedList.containsKey(lv.getName())) {
                            groupedList.get(lv.getName()).add(ema);
                        } else {
                            List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
                            emaList.add(ema);
                            groupedList.put(lv.getName(), emaList);
                        }
                    }
                    //sort by orders
                    emas = new ArrayList<EventMetaAttribute>(emas.size());
                    for(String et : groupedList.keySet()) {
                        List<EventMetaAttribute> sortedList = new ArrayList<EventMetaAttribute>(groupedList.get(et).size());
                        Map<Integer, EventMetaAttribute> treeMap = new TreeMap<Integer, EventMetaAttribute>();
                        for(EventMetaAttribute ema : groupedList.get(et)) {
                            if(ema.getOrder()==null) {
                                sortedList.add(ema);
                            } else {
                                treeMap.put(ema.getOrder(), ema);
                            }
                        }
                        sortedList.addAll(0, treeMap.values());
                        emas.addAll(sortedList);
                    }

                    //project meta attribute
                    List<ProjectMetaAttribute> pmas = psept.getProjectMetaAttributes(projectId);
                    List<String> pmaNames = new ArrayList<String>(pmas.size());
                    for(ProjectMetaAttribute pma : pmas) {
                        pmaNames.add(pma.getAttributeName());
                    }
                    //sample meta attribute
                    List<SampleMetaAttribute> smas = psept.getSampleMetaAttributes(projectId);
                    List<String> smaNames = new ArrayList<String>();
                    for(SampleMetaAttribute sma : smas) {
                        smaNames.add(sma.getAttributeName());
                    }

                    dynamicList = new ArrayList<EventMetaAttributeContainer>(emas.size());
                    for(EventMetaAttribute ema : emas) {
                        EventMetaAttributeContainer container = new EventMetaAttributeContainer(ema);
                        container.setProjectMeta(pmaNames.contains(ema.getAttributeName()));
                        container.setSampleMeta(smaNames.contains(ema.getAttributeName()));
                        dynamicList.add(container);
                    }
                }
            } else if("g_et".equals(type)) { //all event type
                dynamicList = psept.getLookupValueByType(ModelValidator.EVENT_TYPE_LV_TYPE_NAME);
            } else if("g_pet".equals(type)) { //event types for a project
                dynamicList = psept.getEventTypesForProject(projectId);
            } else if("g_a".equals(type)) { //all available attribute
                dynamicList = psept.getLookupValueByType(ModelValidator.ATTRIBUTE_LV_TYPE_NAME);
            } else if("g_sma".equals(type)) { //sample meta attribute
                Long[] projectId = {this.getProjectId()};
                dynamicList = psept.getSampleMetaAttributes(Arrays.asList(projectId));
            } else if("g_pma".equals(type)) { //project meta attribute
                dynamicList = psept.getProjectMetaAttributes(this.getProjectId());
            } else if("a_lv".equals(type)) { //add new look up value by type
                UploadActionDelegate udelegate = new UploadActionDelegate();
                ProjectSampleEventWritebackBusiness psewt = null;
                psewt = udelegate.initializeBusinessObject(logger, psewt);

                if(lvDataType!=null && lvName!=null && lvType!=null) {
                    String[] lvNames = lvName.split(",");
                    List<LookupValue> lvList = new ArrayList<LookupValue>();
                    for(String name : lvNames) {
                        if(name!=null && name.trim().length()>0) {
                            LookupValue lv = new LookupValue();
                            lv.setName(name.trim());
                            lv.setType(lvType);
                            lv.setDataType(lvDataType);

                            lvList.add(lv);
                        }
                    }
                    psewt.loadLookupValues(lvList);
                    lerror = "false";
                }
            }

        } catch (Exception ex) {
            logger.error("Exception in MetadataSetupAjax : " + ex.toString());
            ex.printStackTrace();
            errorMsg = ex.toString();
            lerror = "true";
        }

        return returnValue;
    }

    public List getDynamicList() {
        return dynamicList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getLvDataType() {
        return lvDataType;
    }

    public void setLvDataType(String lvDataType) {
        this.lvDataType = lvDataType;
    }

    public String getLvName() {
        return lvName;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public String getLvType() {
        return lvType;
    }

    public void setLvType(String lvType) {
        this.lvType = lvType;
    }

    public String getLerror() {
        return lerror;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}