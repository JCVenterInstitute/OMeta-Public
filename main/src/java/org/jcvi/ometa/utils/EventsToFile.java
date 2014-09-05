package org.jcvi.ometa.utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.validation.ModelValidator;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * User: hkim
 * Date: 8/29/13
 * Time: 11:18 AM
 * org.jcvi.ometa.utils
 */
public class EventsToFile {
    private final String serverUrl = "jnp://limsdev5.jcvi.org:1299";
    private static Logger logger = Logger.getLogger(EventsToFile.class);

    public EventsToFile() {

    }

    public void execute(String userName, String password, Params params, String outputPath, String server) throws Exception {
        PresentationActionDelegate delegate = new PresentationActionDelegate();
        ProjectSampleEventPresentationBusiness ejb = delegate.getEjb(PresentationActionDelegate.EJB_NAME, server, userName, password, logger );

        StringBuilder outputs = new StringBuilder(); //tab-delimited output
        List<String> attributeList = params.getAttribute();

        StringBuilder headers = new StringBuilder();
        for(int i=0;i<attributeList.size();i++) {
            if(i>0) {
                headers.append("\t");
            }
            headers.append(attributeList.get(i));
        }
        outputs.append(headers.toString() + "\n");


        Project project = ejb.getProject(params.getProject());
        Map<String, Object> paMap = new HashMap<String, Object>();
        List<ProjectAttribute> paList = ejb.getProjectAttributes(project.getProjectId());
        this.getValueMap(attributeList, paList, paMap);

        //List<EventMetaAttribute> emaList = ejb.getEventMetaAttributes(project.getProjectName(), params.getEvent());
        LookupValue eventTypeLV = ejb.getLookupValue(params.getEvent(), ModelValidator.EVENT_TYPE_LV_TYPE_NAME);

        for(String sampleName : params.getSample()) {
            Map<String, Object> attributeMap = new HashMap<String, Object>(); //event attribute name-value map
            attributeMap.putAll(paMap);

            Sample sample = ejb.getSample(project.getProjectId(), sampleName); //current sample

            List<SampleAttribute> saList = ejb.getSampleAttributes(sample.getSampleId()); //sample attribute list
            if(saList!=null && saList.size()>0) {
                this.getValueMap(attributeList, saList, attributeMap);
            }

            List<Event> sampleEvents = ejb.getEventByTypeAndSample(sample.getSampleId(), eventTypeLV.getLookupValueId());
            if(sampleEvents!=null && sampleEvents.size()>0) {

                for(Event event : sampleEvents) { //events loop
                    List<EventAttribute> eaList = ejb.getEventAttributes(event.getEventId(), project.getProjectId());
                    this.getValueMap(attributeList, eaList, attributeMap);
                }

                attributeMap.put("Project Name", project.getProjectName());
                attributeMap.put("Sample Name", sample.getSampleName());

                boolean satisfyFilter = false;
                Map<String, Object> filters = params.getFilter();
                if(filters!=null && filters.size()>0) {
                    for(String filterName : filters.keySet()) {
                        if(attributeMap.containsKey(filterName)) {
                            Object filterValue = filters.get(filterName);
                            Object attributeValue = attributeMap.get(filterName);
                            if(attributeValue.getClass() == String.class) {
                                satisfyFilter |= ((String)attributeValue).equals((String)filterValue);
                            } else if(attributeValue.getClass() == Integer.class) {
                                satisfyFilter |= ((Integer)attributeValue).equals((Integer)filterValue);
                            } else if(attributeValue.getClass() == Float.class) {
                                satisfyFilter |= ((Float)attributeValue).equals((Float)filterValue);
                            }
                        }
                    }
                }

                if(satisfyFilter) {
                    StringBuilder row = new StringBuilder();
                    for(int i=0;i<attributeList.size();i++) {
                        Object value = attributeMap.get(attributeList.get(i));
                        if(i>0) {
                            row.append("\t");
                        }
                        row.append(value==null?"":value);
                    }
                    outputs.append(row.toString() + "\n");
                }
            }
        }

        //write to output file
        /*File outputFile = new File(outputPath);
        FileOutputStream outputStream = new FileOutputStream(outputFile);*/
        BufferedWriter output = new BufferedWriter(new FileWriter(outputPath));
        output.write(outputs.toString());
        output.close();
    }

    public Params parseJson(String filePath) throws Exception {
        Params params = new Params();
        try {
            InputStream inputStream = new FileInputStream(filePath);
            String fileContents = IOUtils.toString(inputStream);

            JSONObject jsonObject = new JSONObject(fileContents);
            params.setProject(jsonObject.getString("project"));
            params.setEvent(jsonObject.getString("event"));

            List<String> samples = new ArrayList<String>();
            JSONArray sampleArr = jsonObject.getJSONArray("sample");
            if(sampleArr.length()>0) {
                for(int i=0;i<sampleArr.length();i++) {
                    samples.add((String)sampleArr.get(i));
                }
            }
            params.setSample(samples);

            List<String> attributes = new ArrayList<String>();
            JSONArray attributeArr = jsonObject.getJSONArray("attribute");
            if(attributeArr.length()>0) {
                for(int i=0;i<attributeArr.length();i++) {
                    attributes.add((String)attributeArr.get(i));
                }
            }
            params.setAttribute(attributes);

            Map<String, Object> filters = new HashMap<String, Object>();
            JSONObject filterMap = jsonObject.getJSONObject("filter");
            Iterator filterIterator = filterMap.keys();
            while(filterIterator.hasNext()) {
                String key = (String)filterIterator.next();
                filters.put(key, filterMap.get(key));
            }
            params.setFilter(filters);
        } catch(JSONException jex) {
            throw new Exception("malformed json: " + jex.toString());
        }
        return params;
    }


    public static void main(String[] args) throws Exception {
        if(args.length < 3) {
            throw new Exception("Usage: <JSON file path> <output(TSV) path> <server>");
        }

        String userName = null;
        String password = null;
        Console console = System.console();
        if ( console == null ) {
            throw new Exception( "No console available.\n" );
        }
        else {
            userName = console.readLine("Enter your USERNAME: ");
            char[] passwordArr = console.readPassword("Enter your PASSWORD: ");
            password = new String(passwordArr);
        }

        if(userName!=null && userName.length()>0 && password!=null && password.length()>0) {
            EventsToFile etf = new EventsToFile();
            Params params = etf.parseJson(args[0]);
            etf.execute(userName, password, params, args[1], args[2]);
        } else {
            throw new Exception( "Please provide username and password.\n" );
        }
    }

    private <E extends AttributeModelBean> void getValueMap(List<String> requested, List<E> attributeList, Map<String, Object> attributeMap) throws Exception {
        for(AttributeModelBean attributeBean : attributeList) {
            if(attributeBean.getMetaAttribute() == null) {
                continue;
            }
            LookupValue lookupValue = attributeBean.getMetaAttribute().getLookupValue();
            if(requested.contains(lookupValue.getName())) { //only requested attributes
                Object attributeValue = ModelValidator.getModelValue(lookupValue, attributeBean);

                if(attributeValue!=null) {
                    if(attributeValue.getClass() == Timestamp.class || attributeValue.getClass() == Date.class) {
                        attributeValue = CommonTool.convertTimestampToDate(attributeValue);
                    }
                } else {
                    attributeValue = "";
                }
                attributeMap.put(lookupValue.getName(), attributeValue);
            }
        }
    }

    private class Params {
        private String project;
        private List<String> sample;
        private String event;
        private Map<String, Object> filter;
        private List<String> attribute;

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public List<String> getSample() {
            return sample;
        }

        public void setSample(List<String> sample) {
            this.sample = sample;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public Map<String, Object> getFilter() {
            return filter;
        }

        public void setFilter(Map<String, Object> filter) {
            this.filter = filter;
        }

        public List<String> getAttribute() {
            return attribute;
        }

        public void setAttribute(List<String> attribute) {
            this.attribute = attribute;
        }
    }
}
