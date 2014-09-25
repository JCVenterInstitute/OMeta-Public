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
import com.opensymphony.xwork2.Preparable;
import org.apache.log4j.Logger;
import org.jcvi.ometa.action.ajax.IAjaxAction;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.model.web.EventMetaAttributeContainer;
import org.jcvi.ometa.model.web.MetadataSetupReadBean;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.PresentationActionDelegate;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.text.ParseException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/7/11
 * Time: 2:08 PM
 */
public class MetadataSetup extends ActionSupport implements IAjaxAction, Preparable {
    private Logger logger = Logger.getLogger(MetadataSetup.class);

    private ProjectSampleEventPresentationBusiness psept;
    private ProjectSampleEventWritebackBusiness psewt;

    private List<Project> projectList;
    private String projectNames;

    private String type;
    private List<MetadataSetupReadBean> beanList;
    private Long projectId;

    //AJAX parameters
    private Map<String, Object> dataMap;
    private String eventName;

    //Add Attribute lists
    private String lvDataType;
    private String lvName;
    private String lvType;
    private List<String> dataTypes;
    private List<String> types;

    public MetadataSetup() {
        getReadEJB();
    }

    public MetadataSetup(ProjectSampleEventPresentationBusiness ejb) {
        psept = ejb;
    }

    private void getReadEJB() {
        PresentationActionDelegate pdeledate = new PresentationActionDelegate();
        psept = pdeledate.initializeEjb(logger, psept);
    }

    @Override
    public void prepare() throws Exception {
        //may need to split ajax operations from action
    }

    public String process() {
        String returnValue = ERROR;
        UserTransaction tx = null;

        try {
            List<String> projectNameList = new ArrayList<String>();
            if(projectNames == null || projectNames.equals(""))
                projectNameList.add("ALL");
            else if(projectNames.contains(","))
                projectNameList.addAll(Arrays.asList(projectNames.split(",")));
            else
                projectNameList.add(projectNames);

            projectList = psept.getProjects(projectNameList);

            if(projectId!=null && projectId!=0 && type!=null && !type.isEmpty() && beanList!=null && beanList.size()>0) {
                tx=(UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                UploadActionDelegate udelegate = new UploadActionDelegate();
                psewt = udelegate.initializeBusinessObject(logger, psewt);

                //user selected project
                Project loadingProject = psept.getProject(projectId);

                //existing EMA for current project
                List<EventMetaAttribute> refEmaList = psept.getEventMetaAttributes(loadingProject.getProjectId());

                if("e".equals(type)) {  //event metadata setup
                    Map<String, Map<String, EventMetaAttribute>> existingEmaMap = new HashMap<String, Map<String, EventMetaAttribute>>();
                    for(EventMetaAttribute ema : refEmaList) {
                        if(existingEmaMap.containsKey(ema.getEventName())) {
                            existingEmaMap.get(ema.getEventName()).put(ema.getAttributeName(), ema);
                        } else {
                            HashMap<String, EventMetaAttribute> emaMap = new HashMap<String, EventMetaAttribute>();
                            emaMap.put(ema.getAttributeName(), ema);
                            existingEmaMap.put(ema.getEventName(), emaMap);
                        }
                    }

                    //process meta attribute orders
                    Map<String, List<MetadataSetupReadBean>> groupedList = new HashMap<String, List<MetadataSetupReadBean>>();
                    for (MetadataSetupReadBean bean : beanList) {
                        if(groupedList.containsKey(bean.getEt())) {
                            groupedList.get(bean.getEt()).add(bean);
                        } else {
                            List<MetadataSetupReadBean> beanList = new ArrayList<MetadataSetupReadBean>();
                            beanList.add(bean);
                            groupedList.put(bean.getEt(), beanList);
                        }
                    }
                    beanList = new ArrayList<MetadataSetupReadBean>(beanList.size());
                    for(String et : groupedList.keySet()) {
                        Map<Integer, MetadataSetupReadBean> treeMap = new TreeMap<Integer, MetadataSetupReadBean>();
                        List<MetadataSetupReadBean> unordered = new ArrayList<MetadataSetupReadBean>();
                        for(MetadataSetupReadBean bean : groupedList.get(et)) {
                            String order = bean.getOrder();
                            if(order==null || order.trim().length()==0) {
                                unordered.add(bean);
                            } else {
                                if(treeMap.containsKey(Integer.parseInt(order))) {
                                    throw new DuplicatedOrderException("Meta Attribute Orders are duplicated!");
                                } else {
                                    treeMap.put(Integer.parseInt(order), bean);
                                }
                            }
                        }

                        int newPosition = 1;
                        for(MetadataSetupReadBean bean : treeMap.values()) {
                            bean.setOrder(String.valueOf(newPosition++));
                            beanList.add(bean);
                        }
                        for(MetadataSetupReadBean bean : unordered) {
                            bean.setOrder(String.valueOf(newPosition++));
                            beanList.add(bean);
                        }
                    }

                    Map<String, SampleMetaAttribute> existingSmaMap = this.getSmaMap(loadingProject.getProjectId());
                    Map<String, ProjectMetaAttribute> existingPmaMap = this.getPmaMap(loadingProject.getProjectId());

                    List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
                    List<ProjectMetaAttribute> pmaList = new ArrayList<ProjectMetaAttribute>();
                    List<SampleMetaAttribute> smaList = new ArrayList<SampleMetaAttribute>();
                    for (MetadataSetupReadBean bean : beanList) {
                        EventMetaAttribute ema;
                        boolean isNewOrModified = true;

                        if(existingEmaMap.containsKey(bean.getEt()) && existingEmaMap.get(bean.getEt()).containsKey(bean.getName())) {
                            //updates existing EMA
                            ema = existingEmaMap.get(bean.getEt()).get(bean.getName());
                            //skips unchanged EMA
                            if(this.isUnchanged(bean, ema) && bean.getSampleRequiredDB()==ema.getSampleRequiredDB()
                                    && (ema.getOrder()!=null && ema.getOrder().equals(Integer.parseInt(bean.getOrder())))) {
                                isNewOrModified = false;
                            }
                        } else { //creates new EMA
                            ema = new EventMetaAttribute();
                            ema.setProjectId(loadingProject.getProjectId());
                            ema.setAttributeName(bean.getName());
                        }

                        //only cares for new or modified EMA
                        if(isNewOrModified) {
                            //sets EMA values
                            this.setMAValues(ema,
                                    bean.getActiveDB(), bean.getRequiredDB(),bean.getDesc(),
                                    bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                            ema.setEventName(bean.getEt());
                            ema.setSampleRequiredDB(bean.getSampleRequiredDB());
                            ema.setOrder(Integer.parseInt(bean.getOrder()));
                            emaList.add(ema);
                        }

                        //updates PMA or SMA associated with current EMA
                        if(existingPmaMap.containsKey(bean.getName())) {
                            ProjectMetaAttribute pma = existingPmaMap.get(bean.getName());
                            this.setMAValues(pma,
                                    bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                    bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                            pmaList.add(pma);
                        } else {
                            //handles Project Metadata checkbox by adding new project meta attribute
                            if(bean.getProjectMetaDB()) {
                                ProjectMetaAttribute newPma = new ProjectMetaAttribute();
                                newPma.setProjectName(loadingProject.getProjectName());
                                newPma.setProjectId(loadingProject.getProjectId());
                                newPma.setAttributeName(bean.getName());
                                this.setMAValues(newPma,
                                        bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                        bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                                pmaList.add(newPma);
                            }
                        }
                        if(existingSmaMap.containsKey(bean.getName())) {
                            SampleMetaAttribute sma = existingSmaMap.get(bean.getName());
                            this.setMAValues(sma,
                                    bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                    bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                            smaList.add(sma);
                        } else {
                            //handles Sample Metadata checkbox by adding new sample meta attribute
                            if(bean.getSampleMetaDB()) {
                                SampleMetaAttribute newSma = new SampleMetaAttribute();
                                newSma.setProjectName(loadingProject.getProjectName());
                                newSma.setProjectId(loadingProject.getProjectId());
                                newSma.setAttributeName(bean.getName());
                                this.setMAValues(newSma,
                                        bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                        bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                                smaList.add(newSma);
                            }
                        }
                    }
                    psewt.loadEventMetaAttributes(emaList);
                    if(pmaList.size()>0)
                        psewt.loadProjectMetaAttributes(pmaList);
                    if(smaList.size()>0)
                        psewt.loadSampleMetaAttributes(smaList);
                }
                /* Event Metadata Setup handles all metadata setup
                 * with checkboxes for project/sample meta attribute
                 *
                else if("p".equals(type)) { //project metadata setup
                    Map<String, ProjectMetaAttribute> existingPmaMap = this.getPmaMap(loadingProject.getProjectId());
                    Map<String, List<EventMetaAttribute>> refEmaMap = this.getEmaMap(refEmaList);

                    List<ProjectMetaAttribute> pmaList = new ArrayList<ProjectMetaAttribute>();
                    List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
                    for (MetadataSetupReadBean bean : beanList) {
                        ProjectMetaAttribute pma;
                        if(existingPmaMap.containsKey(bean.getName())) {
                            pma = existingPmaMap.get(bean.getName());
                            if(this.isUnchanged(bean, pma)) {
                                continue; //skip unchanged MA
                            }
                        } else {
                            pma = new ProjectMetaAttribute();
                            pma.setProjectId(loadingProject.getProjectId());
                            pma.setAttributeName(bean.getName());
                            //pma.setDataType(refEmaMap.get(bean.getName()).get(0).getDataType());
                        }
                        this.setMAValues(pma,
                                bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                        pmaList.add(pma);

                        List<EventMetaAttribute> emas = refEmaMap.get(bean.getName());
                        if(emas!=null && emas.size()>0) {
                            for(EventMetaAttribute ema : emas) {
                                this.setMAValues(ema,
                                        bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                        bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                                emaList.add(ema);
                            }
                        }
                    }
                    if(emaList.size()>0)
                        psewt.loadEventMetaAttributes(emaList);
                    if(pmaList.size()>0)
                        psewt.loadProjectMetaAttributes(pmaList);

                } else if ("s".equals(type)) {  //sample metadata setup
                    Map<String, SampleMetaAttribute> exsitingSmaMap = this.getSmaMap(loadingProject.getProjectId());
                    Map<String, List<EventMetaAttribute>> refEmaMap = this.getEmaMap(refEmaList);

                    List<SampleMetaAttribute> smaList = new ArrayList<SampleMetaAttribute>();
                    List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
                    for (MetadataSetupReadBean bean : beanList) {
                        SampleMetaAttribute sma;
                        if(exsitingSmaMap.containsKey(bean.getName())) {
                            sma = exsitingSmaMap.get(bean.getName());
                            if(this.isUnchanged(bean, sma)) {
                                continue; //skip unchanged MA
                            }
                        } else {
                            sma = new SampleMetaAttribute();
                            sma.setProjectId(loadingProject.getProjectId());
                            sma.setAttributeName(bean.getName());
                            //sma.setDataType(refEmaMap.get(bean.getName()).get(0).getDataType());
                        }
                        this.setMAValues(sma,
                                bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                        smaList.add(sma);

                        List<EventMetaAttribute> emas = refEmaMap.get(bean.getName());
                        if(emas!=null && emas.size()>0) {
                            for(EventMetaAttribute ema : emas) {
                                this.setMAValues(ema,
                                        bean.getActiveDB(), bean.getRequiredDB(), bean.getDesc(),
                                        bean.getOptions(), bean.getLabel(), bean.getOntology(), loadingProject.getProjectName());
                                emaList.add(ema);
                            }
                        }
                    }
                    if(emaList.size()>0)
                        psewt.loadEventMetaAttributes(emaList);
                    if(smaList.size()>0)
                        psewt.loadSampleMetaAttributes(smaList);

                }*/
                projectId = null;
                beanList = null;

                addActionMessage("Metadata has been loaded successfully.");
            }
            returnValue = SUCCESS;

        } catch(Exception ex) {
            logger.error("Exception in MetadataSetup : " + ex.toString());
            ex.printStackTrace();
            if( ex.getClass() == ForbiddenResourceException.class ) {
                addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
                return Constants.FORBIDDEN_ACTION_RESPONSE;
            } else if( ex.getClass() == ForbiddenResourceException.class ) {
                addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
                return LOGIN;
            } else if( ex.getClass() == ParseException.class ) {
                addActionError( Constants.INVALID_DATE_MESSAGE );
            } else if( ex.getClass() == DuplicatedOrderException.class ) {
                addActionError( "Error while processing meta attribute positions. Check for any duplicated position values." );
            } else {
                addActionError( "Error while adding or updating metadata." );
            }

            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError("Transaction Error! Use Help menu or contact the administrator.");
            }
        } finally {
            try {
                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                    tx.commit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        return returnValue;
    }

    public String runAjax() {
        String rtnVal = SUCCESS;
        boolean isError = false;
        dataMap = new HashMap<String, Object>();

        try {
            if("g_ema".equals(type)) { //event meta attribute
                LookupValue eventTypeLV=null;
                if(eventName != null && !eventName.isEmpty()){
                    eventTypeLV = psept.getLookupValue(eventName, ModelValidator.EVENT_TYPE_LV_TYPE_NAME);
                }
                List<EventMetaAttribute> emas = psept.getEventMetaAttributes(projectId, eventTypeLV==null?null:eventTypeLV.getLookupValueId());
                if(emas.size()>0) {
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
                        List<EventMetaAttribute> sortedList = groupedList.get(et);
                        CommonTool.sortEventMetaAttributeByOrder(sortedList);
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

                    List<EventMetaAttributeContainer> tempList = new ArrayList<EventMetaAttributeContainer>(emas.size());
                    for(EventMetaAttribute ema : emas) {
                        EventMetaAttributeContainer container = new EventMetaAttributeContainer(ema);
                        container.setProjectMeta(pmaNames.contains(ema.getAttributeName()));
                        container.setSampleMeta(smaNames.contains(ema.getAttributeName()));
                        tempList.add(container);
                    }

                    dataMap.put("ema", tempList);
                }
            } else if("g_et".equals(type)) { //all event type
                dataMap.put("et", psept.getLookupValueByType(ModelValidator.EVENT_TYPE_LV_TYPE_NAME));
            } else if("g_pet".equals(type)) { //event types for a project
                dataMap.put("pet", psept.getEventTypesForProject(projectId));
            } else if("g_a".equals(type)) { //all available attribute
                dataMap.put("a", psept.getLookupValueByType(ModelValidator.ATTRIBUTE_LV_TYPE_NAME));
            } else if("g_sma".equals(type)) { //sample meta attribute
                Long[] projectId = {this.getProjectId()};
                dataMap.put("sma", psept.getSampleMetaAttributes(Arrays.asList(projectId)));
            } else if("g_pma".equals(type)) { //project meta attribute
                dataMap.put("pma", psept.getProjectMetaAttributes(this.getProjectId()));
            } else if("a_lv".equals(type)) { //add new look up value by type
                UploadActionDelegate udelegate = new UploadActionDelegate();
                ProjectSampleEventWritebackBusiness psewt = null;
                psewt = udelegate.initializeBusinessObject(logger, psewt);

                if(lvDataType != null && lvName != null && lvType != null) {
                    String[] lvNames = lvName.split(",");
                    boolean isGroupInsert = lvType.endsWith("Group");

                    List<LookupValue> lvList = new ArrayList<LookupValue>();
                    for(String name : lvNames) {
                        if(name != null && name.trim().length() > 0) {
                            if(name.contains("[") || name.contains("]")) {
                                throw new Exception("Attribute names cannot contain '[' or ']'.");
                            }

                            LookupValue lv = new LookupValue();
                            lv.setName(name.trim());
                            lv.setType(lvType);
                            lv.setDataType(lvDataType);
                            lvList.add(lv);
                        }
                    }
                    psewt.loadLookupValues(lvList);

                    if(isGroupInsert && lvList.size() > 0) {
                        List<Group> groups = new ArrayList<Group>(lvNames.length);
                        for(LookupValue lv : lvList) {
                            LookupValue loadedLookupValue = psept.getLookupValue(lv.getName(), lv.getType());
                            Group group = new Group();
                            group.setGroupNameLookupValue(loadedLookupValue);
                            group.setNameLookupId(loadedLookupValue.getLookupValueId());
                            groups.add(group);
                        }
                        psewt.loadGroups(groups);
                    }
                }
            } else if("g_all".equals(type)) {
                Map<String, List> listMap = new HashMap<String, List>();
                List tempList = null;

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

                    tempList = new ArrayList<EventMetaAttributeContainer>(emas.size());
                    for(EventMetaAttribute ema : emas) {
                        EventMetaAttributeContainer container = new EventMetaAttributeContainer(ema);
                        container.setProjectMeta(pmaNames.contains(ema.getAttributeName()));
                        container.setSampleMeta(smaNames.contains(ema.getAttributeName()));
                        tempList.add(container);
                    }
                }
                dataMap.put("ema", tempList);

                dataMap.put("et", psept.getLookupValueByType(ModelValidator.EVENT_TYPE_LV_TYPE_NAME));
                dataMap.put("pet", psept.getEventTypesForProject(projectId));
                dataMap.put("a", psept.getLookupValueByType(ModelValidator.ATTRIBUTE_LV_TYPE_NAME));
            }

        } catch (Exception ex) {
            logger.error("Exception in runAjax of MetadataSetup : " + ex.toString());
            ex.printStackTrace();
            isError = true;
            dataMap.put("errorMsg", ex.toString());
        } finally {
            dataMap.put("isError", isError);
        }

        return rtnVal;
    }

    public String openNewAttribute() {
        ModelValidator modelValidator = new ModelValidator();
        dataTypes = new ArrayList<String>();
        dataTypes.addAll(modelValidator.getValidDataTypes());

        types = new ArrayList<String>();
        if(type.equals("gr")) { //actor group lookup value
            types.add(ModelValidator.VIEW_GROUP_LV_TYPE_NAME);
            types.add(ModelValidator.EDIT_GROUP_LV_TYPE_NAME);
        } else {
            types.addAll(modelValidator.getValidLookupValueTypes());
        }

        return SUCCESS;
    }

    private boolean isUnchanged(MetadataSetupReadBean b1, MetaAttributeModelBean b2) {
        return b1.getActiveDB()==b2.getActiveDB()
                && b1.getRequiredDB()==b2.getRequiredDB()
                && (b1.getDesc()!=null && b1.getDesc().equals(b2.getDesc()))
                && (b1.getOptions()!=null && b1.getOptions().equals(b2.getOptions()))
                && (b1.getLabel()!=null && b1.getLabel().equals(b2.getLabel()))
                && (b1.getOntology()!=null && b1.getOntology().equals(b2.getOntology()));
    }

    private void setMAValues(MetaAttributeModelBean b,
                             Integer active, Integer required,
                             String desc, String options, String label, String ontology, String projectName) {
        b.setActiveDB(active);
        b.setRequiredDB(required);
        b.setDesc(desc);
        b.setOptions(options);
        b.setLabel(label);
        b.setOntology(ontology);
        b.setProjectName(projectName);
    }

    private List<EventMetaAttribute> updateExistingEMA(List<EventMetaAttribute> emas, MetadataSetupReadBean bean, String projectName) {
        List<EventMetaAttribute> emaList = new ArrayList<EventMetaAttribute>();
        if(emas!=null && emas.size()>0) {
            for(EventMetaAttribute ema : emas) {
                if(!this.isUnchanged(bean, ema)) {
                    this.setMAValues(ema, bean.getActiveDB(), bean.getRequiredDB(),
                            bean.getDesc(), bean.getOptions(), bean.getLabel(), bean.getOntology(), projectName);
                    emaList.add(ema);
                }
            }
        }
        return emaList;
    }

    private Map<String, ProjectMetaAttribute> getPmaMap(Long projectId) throws Exception {
        List<ProjectMetaAttribute> existingPmaList = psept.getProjectMetaAttributes(projectId);
        Map<String, ProjectMetaAttribute> exsitingPmaMap = new HashMap<String, ProjectMetaAttribute>();
        for(ProjectMetaAttribute pma : existingPmaList) {
            exsitingPmaMap.put(pma.getAttributeName(), pma);
        }
        return exsitingPmaMap;
    }

    private Map<String, SampleMetaAttribute> getSmaMap(Long projectId) throws Exception {
        List<SampleMetaAttribute> existingSmaList = psept.getSampleMetaAttributes(projectId);
        Map<String, SampleMetaAttribute> exsitingSmaMap = new HashMap<String, SampleMetaAttribute>();
        for(SampleMetaAttribute sma : existingSmaList) {
            exsitingSmaMap.put(sma.getAttributeName(), sma);
        }
        return exsitingSmaMap;
    }

    private Map<String, List<EventMetaAttribute>> getEmaMap(List<EventMetaAttribute> emaList) {
        Map<String, List<EventMetaAttribute>> emas = new HashMap<String, List<EventMetaAttribute>>();
        for(EventMetaAttribute ema : emaList) {
            String attributeName = ema.getLookupValue().getName();
            if(emas.containsKey(attributeName)) {
                emas.get(attributeName).add(ema);
            } else {
                List<EventMetaAttribute> subEmas = new ArrayList<EventMetaAttribute>();
                subEmas.add(ema);
                emas.put(attributeName, subEmas);
            }
        }
        return emas;
    }

    private class DuplicatedOrderException extends Exception {
        public DuplicatedOrderException( String message ) {
            super( message ) ;
        }
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public String getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(String projectNames) {
        this.projectNames = projectNames;
    }

    public List<MetadataSetupReadBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<MetadataSetupReadBean> beanList) {
        this.beanList = beanList;
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

    public Map<String, Object> getDataMap() {
        return dataMap;
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

    public List<String> getDataTypes() {
        return dataTypes;
    }

    public List<String> getTypes() {
        return types;
    }
}
