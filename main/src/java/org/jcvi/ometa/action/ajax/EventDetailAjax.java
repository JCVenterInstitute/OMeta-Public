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
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.configuration.QueryEntityType;
import org.jcvi.ometa.configuration.ResponseToFailedAuthorization;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 5/23/12
 * Time: 2:16 PM
 */
public class EventDetailAjax extends ActionSupport implements IAjaxAction {
    private Logger logger = Logger.getLogger(EventDetailAjax.class);

    private ReadBeanPersister readPersister;

    private List aaData;

    private String type;
    private Long projectId;
    private Long sampleId;
    private Long eventId;

    String projectName;

    //dataTable request parameters
    private int sEcho;
    private int iColumns;
    private int iDisplayStart;
    private int iDisplayLength;
    private int iTotalRecords;
    private int iTotalDisplayRecords;
    private String sSearch;
    private String iSortCol_0;
    private String sSortDir_0;

    //date
    private String fd;
    private String td;

    public EventDetailAjax() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public EventDetailAjax(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    public String runAjax() {
        String rtnVal = ERROR;

        try {
            aaData = new ArrayList<Map>();
            int canEdit = checkEditPrivilege();

            if("sdt".equals(type)) {
                List<Sample> samples;
                Long flexId = (sampleId!=null && sampleId!=0)?sampleId:projectId;
                String flexType = (sampleId!=null && sampleId!=0)?"sample":"project";
                String sortCol = iSortCol_0.equals("1")?"sample":iSortCol_0.equals("2")?"parent":iSortCol_0.equals("3")?"user":iSortCol_0.equals("4")?"date":null;
                samples = readPersister.getAllSamples(flexId, flexType,sSearch, sortCol, sSortDir_0);

                List<Sample> filteredList = samples.subList(iDisplayStart, iDisplayStart+iDisplayLength>samples.size()?samples.size():iDisplayLength+iDisplayStart);

                if(filteredList.size() == 0) filteredList = samples;

                Map<Long, List<SampleAttribute>> sampleIdVsAttributes = this.getSampleVsAttributeList(filteredList);
                Map<Long, String> sampleNameById = new HashMap<Long, String>();
                Map<Long, String> actors = new HashMap<Long, String>();
                for (Sample sample : filteredList) {
                    if (sample.getSampleLevel() != null && sample.getSampleLevel() > 1 && sample.getParentSampleId() != null) {
                        if(!sampleNameById.containsKey(sample.getParentSampleId())) {
                            Sample parentSample = readPersister.getSample(sample.getParentSampleId());
                            sampleNameById.put(parentSample.getSampleId(), parentSample.getSampleName());
                        }
                    }

                    if (!actors.containsKey(sample.getCreatedBy())) {
                        Actor tempActor = readPersister.getActor(sample.getCreatedBy());
                        actors.put(tempActor.getLoginId(), tempActor.getLastName() + ", " + tempActor.getFirstName());
                    }

                    Map<String, Object> sampleMap = new HashMap<String, Object>();
                    sampleMap.put("sample", sample);
                    sampleMap.put("sampleName", sample.getSampleName());
                    sampleMap.put("parentSampleName", sampleNameById.get(sample.getParentSampleId()));
                    sampleMap.put("actor", actors.get(sample.getCreatedBy()));
                    sampleMap.put("createdOn", ModelValidator.PST_DEFAULT_TIMESTAMP_FORMAT.format(sample.getCreationDate()));

                    if(sampleIdVsAttributes.containsKey(sample.getSampleId())) {
                        Map<String, Object> attributeMap = new LinkedHashMap<String, Object>();

                        List<Event> sampleEvents = this.readPersister.getEventsForSample(sample.getSampleId());
                        Event registrationEvent = sampleEvents.get(0);
                        List<EventMetaAttribute> registrationEMA = this.readPersister.getEventMetaAttributes(sample.getProjectId(), registrationEvent.getEventTypeLookupValue().getLookupValueId());
                        final List<String> orderedEMANames = new ArrayList<String>(registrationEMA.size());
                        for(EventMetaAttribute ema : registrationEMA) {
                            orderedEMANames.add(ema.getLookupValue().getName());
                        }

                        List<SampleAttribute> sampleAttributes = sampleIdVsAttributes.get(sample.getSampleId());
                        Collections.sort(sampleAttributes, new Comparator<SampleAttribute>() {
                            @Override
                            public int compare(SampleAttribute sa1, SampleAttribute sa2) {
                                Integer sa1Index = orderedEMANames.indexOf(sa1.getMetaAttribute().getLookupValue().getName());
                                Integer sa2Index = orderedEMANames.indexOf(sa2.getMetaAttribute().getLookupValue().getName());

                                //Sample Attrs on top of list
                                if(sa1Index == -1) sa1Index = orderedEMANames.size() + 1;
                                if(sa2Index == -1) sa2Index = orderedEMANames.size() + 1;
                                return sa1Index.compareTo(sa2Index);
                            }
                        });
                        for (SampleAttribute sa : sampleIdVsAttributes.get(sample.getSampleId())) {
                            if(sa.getMetaAttribute() != null) {
                                LookupValue tempLookupValue = sa.getMetaAttribute().getLookupValue();
                                Object attrValue = ModelValidator.getModelValue(tempLookupValue, sa);
                                attributeMap.put(tempLookupValue.getName(), attributeDecorator(tempLookupValue, attrValue));
                            }
                        }
                        sampleMap.put("attributes", attributeMap);
                        sampleMap.put("event", registrationEvent.getEventTypeLookupValue().getName());
                    }
                    aaData.add(sampleMap);

                    iTotalDisplayRecords = iTotalRecords = samples.size();
                }

            } else if ("edt".equals(type)) {
                String sortCol = iSortCol_0.equals("1")?"event":iSortCol_0.equals("2")?"sample":iSortCol_0.equals("3")?"date":iSortCol_0.equals("4")?"user":null;
                List<Event> eventList;
                if (sampleId != 0) {
                    eventList = readPersister.getAllEvents(sampleId, "Sample", sSearch, sortCol, sSortDir_0, -1, -1, fd, td);
                } else {
                    eventList = readPersister.getAllEvents(projectId, "Eventlist", sSearch, sortCol, sSortDir_0, -1, -1, fd, td);
                }

                List<Event> filteredList = eventList.subList(iDisplayStart, iDisplayStart+iDisplayLength>eventList.size()?eventList.size():iDisplayLength+iDisplayStart);
                Map<Long, List<EventAttribute>> eventIdVsAttributes = this.getEventIdVsAttributeList(filteredList);

                Map<Long, String> sampleIdtoNames = new HashMap<Long, String>();
                Map<Long, String> actors = new HashMap<Long, String>();
                for (Event event : filteredList) {
                    Map<String, Object> eventMap = new HashMap<String, Object>();

                    String sampleName = null;
                    if(event.getSampleId() != null && event.getSampleId() != 0) {
                        if(sampleIdtoNames.containsKey(event.getSampleId()))
                            sampleName = sampleIdtoNames.get(event.getSampleId());
                        else {
                            sampleName = readPersister.getSample(event.getSampleId()).getSampleName();
                            sampleIdtoNames.put(event.getSampleId(), sampleName);
                        }
                    }
                    if (!actors.containsKey(event.getCreatedBy())) {
                        Actor tempActor = readPersister.getActor(event.getCreatedBy());
                        actors.put(tempActor.getLoginId(), tempActor.getLastName() + ", " + tempActor.getFirstName());
                    }

                    String eventStatus = event.getEventStatusLookupValue().getName();

                    eventMap.put("event", event);
                    eventMap.put("eventId", event.getEventId());
                    eventMap.put("eventName", event.getEventTypeLookupValue().getName());
                    eventMap.put("sampleName", sampleName);
                    eventMap.put("createdOn", ModelValidator.PST_DEFAULT_TIMESTAMP_FORMAT.format(event.getCreationDate()));
                    eventMap.put("actor", actors.get(event.getCreatedBy()));
                    eventMap.put("eventStatus", eventStatus);
                    eventMap.put("canEdit", canEdit);

                    if(eventIdVsAttributes.containsKey(event.getEventId())) {
                        Map<String, Object> attributeMap = new HashMap<String, Object>();
                        for (EventAttribute ea : eventIdVsAttributes.get(event.getEventId())) {
                            if(ea.getMetaAttribute()!=null) {
                                LookupValue tempLookupValue = ea.getMetaAttribute().getLookupValue();
                                Object attrValue = ModelValidator.getModelValue(tempLookupValue, ea);
                                attributeMap.put(tempLookupValue.getName(), attributeDecorator(tempLookupValue, attrValue));
                            }
                        }
                        eventMap.put("attributes", attributeMap);
                    }
                    aaData.add(eventMap);

                    iTotalRecords = eventList.size();
                    iTotalDisplayRecords =iTotalRecords;
                }
            } else {
                throw new Exception("undefined AJAX action for (" + type + ")");
            }

            rtnVal = SUCCESS;
        } catch (Exception ex) {
            logger.error("Exception in EventDetail AJAX : " + ex.toString());
            ex.printStackTrace();
        }

        return rtnVal;
    }

    private int checkEditPrivilege() throws Exception { //check edit permission
        int canEdit = 0;
        String userName = ServletActionContext.getRequest().getRemoteUser();
        Project currProject = readPersister.getProject(projectId);
        projectName = currProject.getProjectName();
        List<String> projectNamesList = new ArrayList<String>();
        projectNamesList.add(projectName);
        try {
            readPersister.getAuthorizedProjectNames(projectNamesList, userName, ResponseToFailedAuthorization.ThrowException, AccessLevel.Edit, QueryEntityType.Project);
            canEdit = 1;
        } catch (IllegalAccessException iaex) {
            canEdit = 0;
        }

        return canEdit;
    }

    private Object attributeDecorator(LookupValue lookupValue, Object attrVal) throws Exception {
        if(attrVal!=null) {
            if(attrVal.getClass() == Timestamp.class || attrVal.getClass() == Date.class) {
                attrVal = CommonTool.convertTimestampToDate(attrVal);
            } else if(lookupValue.getDataType().equals(ModelValidator.FILE_DATA_TYPE)) {
                String justFileName = (String)attrVal;
                justFileName = justFileName.substring(justFileName.lastIndexOf(File.separator)+1);
                attrVal = "<a href=\"getFile.action?fn="+attrVal+"\">"+justFileName+"</a>";
            }
        }
        return attrVal;
    }

    private Map<Long, List<SampleAttribute>> getSampleVsAttributeList(List<Sample> samples) throws Exception {
        List<Long> allSampleIds = new ArrayList<Long>();
        for (Sample sample : samples) {
            allSampleIds.add(sample.getSampleId());
        }

        List<SampleAttribute> allSampleAttributes = readPersister.getSampleAttributes(allSampleIds);
        Map<Long, List<SampleAttribute>> sampleIdVsAttributeList = new HashMap<Long, List<SampleAttribute>>();
        for (SampleAttribute att : allSampleAttributes) {
            if(att.getMetaAttribute().isActive()) {
                List<SampleAttribute> attributeList = sampleIdVsAttributeList.get(att.getSampleId());
                if (attributeList == null) {
                    attributeList = new ArrayList<SampleAttribute>();
                    sampleIdVsAttributeList.put(att.getSampleId(), attributeList);
                }
                attributeList.add(att);
            }
        }

        return sampleIdVsAttributeList;
    }

    private Map<Long, List<EventAttribute>> getEventIdVsAttributeList(List<Event> events) throws Exception {
        Map<Long, List<EventAttribute>> eventIdVsAttributes = null;

        List<Long> allEventIds = new ArrayList<Long>();
        for (Event evt : events) {
            allEventIds.add(evt.getEventId());
        }

        if (allEventIds.size() > 0) {
            List<EventAttribute> allEventAttributes = readPersister.getEventAttributes(allEventIds, projectId);
            eventIdVsAttributes = new HashMap<Long, List<EventAttribute>>();
            for (EventAttribute ea : allEventAttributes) {
                List<EventAttribute> lea = eventIdVsAttributes.get(ea.getEventId());
                if (lea == null) {
                    lea = new ArrayList<EventAttribute>();
                    eventIdVsAttributes.put(ea.getEventId(), lea);
                }
                lea.add(ea);
            }
        }
        return eventIdVsAttributes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List getAaData() {
        return aaData;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public int getsEcho() {
        return sEcho;
    }

    public void setSEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public int getiColumns() {
        return iColumns;
    }

    public void setIColumns(int iColumns) {
        this.iColumns = iColumns;
    }

    public int getiDisplayStart() {
        return iDisplayStart;
    }

    public void setIDisplayStart(int iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public int getiDisplayLength() {
        return iDisplayLength;
    }

    public void setIDisplayLength(int iDisplayLength) {
        this.iDisplayLength = iDisplayLength;
    }

    public int getiTotalRecords() {
        return iTotalRecords;
    }

    public void setITotalRecords(int iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public int getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setITotalDisplayRecords(int iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }

    public String getsSearch() {
        return sSearch;
    }

    public void setSSearch(String sSearch) {
        this.sSearch = sSearch;
    }

    public String getiSortCol_0() {
        return iSortCol_0;
    }

    public void setISortCol_0(String iSortCol_0) {
        this.iSortCol_0 = iSortCol_0;
    }

    public String getsSortDir_0() {
        return sSortDir_0;
    }

    public void setSSortDir_0(String sSortDir_0) {
        this.sSortDir_0 = sSortDir_0;
    }

    public String getFd() {
        return fd;
    }

    public void setFd(String fd) {
        this.fd = fd;
    }

    public String getTd() {
        return td;
    }

    public void setTd(String td) {
        this.td = td;
    }
}
