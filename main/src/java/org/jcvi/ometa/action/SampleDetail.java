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
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.stateless_session_bean.ForbiddenResourceException;
import org.jcvi.ometa.stateless_session_bean.LoginRequiredException;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 2/28/11
 * Time: 9:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SampleDetail extends ActionSupport {
    private Logger logger = Logger.getLogger(SampleDetail.class);

    ReadBeanPersister readPersister;

    private Project project;
    private Sample sample;

    private String projectName;
    private Long projectId;
    private String sampleName;
    private Long sampleId;
    private Map<String, Object> detailMap;

    private final String TAXONOMY_ID = "Taxonomy ID";

    public SampleDetail() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister( props );
    }

    public String detailPage() {
        String rtnVal = SUCCESS;

        try {
            LookupValue tempLookupValue = null;

            detailMap = new HashMap<String, Object> ();

            this.setProject(readPersister.getProject(projectName));
            this.setSample(readPersister.getSample(sampleId));

            for(ProjectAttribute pa : readPersister.getProjectAttributes(projectId)) {
                tempLookupValue = pa.getMetaAttribute().getLookupValue();
                detailMap.put( tempLookupValue.getName(), ModelValidator.getModelValue( tempLookupValue, pa ) );
            }

            List<SampleAttribute> sampleAttributes = readPersister.getSampleAttributes( sample.getSampleId() );
            for(SampleAttribute sa : sampleAttributes) {
                tempLookupValue = sa.getMetaAttribute().getLookupValue();
                detailMap.put(tempLookupValue.getName(), ModelValidator.getModelValue(tempLookupValue, sa));
            }

            List<Map> eventList = new ArrayList<Map>();

            List<Event> sampleEvents = readPersister.getEventsForSample( sample.getSampleId() );
            for(Event evt : sampleEvents) {
                String tempEventName = evt.getEventTypeLookupValue().getName();

                Map<String, Object> eventMap = new HashMap<String, Object> ();
                eventMap.put("eventName", tempEventName);
                eventMap.put("date", ModelValidator.PST_DEFAULT_DATE_FORMAT.format(evt.getCreationDate()));

                List<Map<String, Object>> attrList = new ArrayList<Map<String, Object>>();
                Map<String, Object> attrMap;
                List<EventAttribute> eventAttributes = readPersister.getEventAttributes( evt.getEventId() , projectId);

                for(EventAttribute ea : eventAttributes) {
                    tempLookupValue = ea.getMetaAttribute().getLookupValue();
                    Object eventAttrValue = ModelValidator.getModelValue( tempLookupValue, ea );
                    String attributeName = tempLookupValue.getName();
                    attrMap = new HashMap<String, Object>();

                    if(attributeName.equals("Organism"))
                        detailMap.put("Organism", eventAttrValue);

                    if(tempLookupValue.getName().contains("status")) {
                        eventMap.put("eventStatus", eventAttrValue);
                    } else {
                        attrMap.put("name", tempLookupValue.getName());

                        if("sra accession".equals(attributeName))
                            eventAttrValue = ((String)eventAttrValue).replaceAll("[\\,\\;]", "\n");

                        String externalLink = null;
                        if("sra accession".equals(attributeName))
                            externalLink = String.format( Constants.NEW_WINDOW_LINK_HTML, Constants.TRACESRA_URL + detailMap.get(TAXONOMY_ID));
                        else if("annotation accession".equals(attributeName))
                            externalLink = String.format( Constants.NEW_WINDOW_LINK_HTML, Constants.ANNOTATION_URL + eventAttrValue + "[PACC]");
                        else if("wgs accession".equals(attributeName))
                            externalLink = String.format( Constants.NEW_WINDOW_LINK_HTML, Constants.WGS_URL + eventAttrValue);

                        if(externalLink != null)
                            eventAttrValue = String.format( Constants.A_TAG_HTML, "#", externalLink ) + eventAttrValue + Constants.A_TAG_CLOSING_HTML;

                        if(eventAttrValue!=null) {
                            if(eventAttrValue.getClass() == Timestamp.class || eventAttrValue.getClass() == Date.class)
                                eventAttrValue = ModelValidator.PST_DEFAULT_DATE_FORMAT.format(eventAttrValue);
                        }
                        attrMap.put("value", eventAttrValue);
                    }

                    if(!attrMap.isEmpty())
                        attrList.add(attrMap);
                }

                eventMap.put("eventAttr", attrList);
                eventList.add(eventMap);
            }

            if(detailMap.containsKey(TAXONOMY_ID)) {
                Map<String, Object> eventMap = new HashMap<String, Object> ();
                eventMap.put( "eventName", "Taxonomy" );
                eventMap.put( "eventStatus", "Available" );

                Map<String, Object> eventAttr = new HashMap<String, Object> ();
                eventAttr.put("name", TAXONOMY_ID );
                eventAttr.put("value", String.format(Constants.A_TAG_HTML, "#", String.format(Constants.NEW_WINDOW_LINK_HTML, Constants.TAXON_URL + detailMap.get("Taxonomy ID") + Constants.TAXON_URL_PARAM)) + detailMap.get("Taxonomy ID") + Constants.A_TAG_CLOSING_HTML);

                List<Map<String, Object>> attrList = new ArrayList<Map<String, Object>>();
                attrList.add(eventAttr);
                eventMap.put("eventAttr", attrList);
                eventList.add(eventMap);
            }

            detailMap.put("event", eventList);

        } catch ( ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_VIEW_MESSAGE );
            addActionError( Constants.DENIED_USER_VIEW_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch( LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch (Exception ex) {
            logger.error( "Exception in Status Page Action : " + ex.toString() );
            rtnVal = ERROR;
        }

        return rtnVal;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Map<String, Object> getDetailMap() {
        return detailMap;
    }

    public void setDetailMap(Map<String, Object> detailMap) {
        this.detailMap = detailMap;
    }
}
