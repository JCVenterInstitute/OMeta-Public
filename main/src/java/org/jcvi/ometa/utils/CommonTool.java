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

package org.jcvi.ometa.utils;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/29/13
 * Time: 3:27 PM
 */
public class CommonTool {
    private static Logger logger = Logger.getLogger(CommonTool.class);

    public static Map<String, String> decorateAttributeMap(Map<String, Object> attributeMap, List<String> attributeList, Project project) {
        Map<String, String> newSampleAttrMap = new HashMap<String, String>();

        if(attributeList==null) { //when attribute list is not available, utilizes keyset from the map
            attributeList = new ArrayList<String>(attributeMap.size());
            attributeList.addAll(attributeMap.keySet());
        } else if(attributeList.contains("Project Name") && !attributeMap.containsKey("Project Name")){
            attributeMap.put("Project Name", project.getProjectName());
        }

        for(String attribute : attributeList) {
            newSampleAttrMap.put(attribute, decorateAttribute(attributeMap, attribute, project));
        }
        return newSampleAttrMap;
    }

    public static String decorateAttribute(Map<String, Object> attributeMap, String attributeName, Project project) {
        String decoratedValue = "";
        if(attributeMap.containsKey(attributeName)) {
            decoratedValue = "" + attributeMap.get(attributeName);
            if (decoratedValue.equals("null") || "".equals(decoratedValue)) {
                decoratedValue = "";
            } else {
                String loweredKey = attributeName.trim().toLowerCase();
                if ("sra accession".equals(loweredKey)) {
                    decoratedValue = displayLinkOnlyForPublished((String)attributeMap.get("sra status"), Constants.TRACESRA_URL + attributeMap.get("Taxonomy ID"), "SRA");
                } else if ("annotation accession".equals(loweredKey)) {
                    decoratedValue = displayLinkOnlyForPublished((String)attributeMap.get("annotation status"), Constants.ANNOTATION_URL + decoratedValue + "[PACC]", "ANNOTATION");
                } else if ("wgs accession".equals(loweredKey)) {
                    decoratedValue = displayLinkOnlyForPublished((String)attributeMap.get("wgs status"), Constants.WGS_URL + decoratedValue, "WGS");
                } else if ("dbsnp accession".equals(loweredKey)) {
                    decoratedValue = displayLinkOnlyForPublished((String)attributeMap.get("dbsnp status"), Constants.dbSNP_URL + decoratedValue, "dbSNP");
                } else if ("isolate repository accession".equals(loweredKey)) {
                    Object typeObj = attributeMap.get("Isolate Repository Type");
                    if (typeObj != null) {
                        String type = (String)typeObj;  //BEI;FGSC;NARSA;STEC;NCPF;OTHER
                        String destination = type.equals("BEI")?Constants.BEI_URL
                                :type.equals("FGSC")?Constants.FGSC_URL
                                :type.equals("NARSA")?Constants.NARSA_URL
                                :type.equals("STEC")?Constants.STEC_URL
                                :type.equals("NCPF")?Constants.NCPF_URL
                                :"";
                        decoratedValue = convertIntoATag(destination, decoratedValue);
                    }
                } else if ("taxonomy id".equals(loweredKey)) {
                    decoratedValue = convertIntoATag(Constants.TAXON_URL + decoratedValue, decoratedValue);
                } else if ("project id".equals(loweredKey)) {
                    decoratedValue = convertIntoATag(Constants.NCBI_PROJECT_PAGE + decoratedValue, decoratedValue);
                } else if ("sample status".equals(loweredKey) && !decoratedValue.equals("")) {
                    decoratedValue = decoratedValue.equals("0") ? "Analysis" : decoratedValue.equals("1") ? "Complete" : decoratedValue;
                } else if ("organism".equals(loweredKey)) {
                    decoratedValue = convertIntoATag(
                            Constants.SAMPLE_DETAIL_URL +
                                    "projectName=" + project.getProjectName().replaceAll(" ", "%20") +
                                    "&projectId=" + project.getProjectId() +
                                    "&sampleName=" + ((String)attributeMap.get(Constants.ATTR_SAMPLE_NAME)).replaceAll(" ", "%20") +
                                    "&sampleId=" + attributeMap.get("sampleId"),
                            decoratedValue
                    );
                } else if(decoratedValue.contains("http://")){
                    String[] decoratedValueArr = decoratedValue.split(",");
                    StringBuilder dvBuild = new StringBuilder();
                    for(String url : decoratedValueArr){
                        if(dvBuild.length() > 0) dvBuild.append(" - ");
                        dvBuild.append(convertIntoATag(url, url));
                    }

                    decoratedValue = dvBuild.toString();
                }
            }
        }
        return decoratedValue;
    }

    public static String displayLinkOnlyForPublished(String status, String url, String type) {
        String rtnVal = "";
        if (status != null) {
            String loweredStatus = status.trim().toLowerCase();
            if ("submitted".equals(loweredStatus)) {
                rtnVal = loweredStatus;
            } else if ("published".equals(loweredStatus)) {
                rtnVal = convertIntoATag(url, type);
            } else {
                rtnVal = status;
            }
        }
        return rtnVal;
    }

    public static String truncateURL(String URL, int length){
        return URL.substring(0, Math.min(URL.length(), length)) + "...";
    }

    public static String convertIntoATag(String url, String displayValue) {
        return (
                String.format(Constants.A_TAG_HTML, "#", String.format(Constants.NEW_WINDOW_LINK_HTML, url))+
                        displayValue+Constants.A_TAG_CLOSING_HTML
        ).replaceAll("\\\"", "\\\\\"");
    }

    public static String convertIntoATagWithTooltip(String url, String displayValue) {
        return (
                String.format(Constants.A_TAG_HTML_WITH_TOOLTIP, "#", String.format(Constants.NEW_WINDOW_LINK_HTML, url), url, "tooltip")+
                        displayValue+Constants.A_TAG_CLOSING_HTML
        ).replaceAll("\\\"", "\\\\\"");
    }

    public static String convertIntoFileLink(String filePath, Long attributeId) {
        String justFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        return "<a href=\"download.action?fp=" + filePath + "\">" + justFileName + "</a>";
    }

    public static <M extends AttributeModelBean> Map<String, Object> getAttributeValueMap(List<M> attributeList, boolean decorate, String[] skipArr) throws Exception {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        List<String> skipList = (skipArr!=null && skipArr.length>0) ? Arrays.asList(skipArr) : null;

        for(M attribute : attributeList) {
            if(attribute.getMetaAttribute() == null) {
                continue;
            }
            LookupValue lookupValue = attribute.getMetaAttribute().getLookupValue();
            if(skipList != null && skipList.contains(lookupValue.getName())) {
                continue;
            }
            Object attributeValue = ModelValidator.getModelValue(lookupValue, attribute);

            if(attributeValue!=null) {
                if(attributeValue.getClass() == Timestamp.class || attributeValue.getClass() == Date.class) {
                    attributeValue = convertTimestampToDate(attributeValue);
                } else if(lookupValue.getDataType().equals(ModelValidator.FILE_DATA_TYPE) && decorate) {
                    attributeValue = convertIntoFileLink((String)attributeValue, null);
                }
            } else {
                attributeValue = "";
            }

            valueMap.put(lookupValue.getName(), attributeValue);
        }
        return valueMap;
    }

    public static String convertTimestampToDate(Object value) throws Exception {
        String formattedDate = null;
        String[] formats = {"MM/dd/yyyy", Constants.DATE_USER_ENTER_FORMAT, "yyyy-MM-dd'T'HH:mm:ss"};
        Date parsedDate = null;
        boolean isString = (value.getClass() == String.class);

        if(isString) {
            for(String format : formats) {
                try {
                    parsedDate = new SimpleDateFormat(format).parse((String)value);
                } catch(ParseException e) { //ignore parse exception
                }
            }
        } else {
            parsedDate = (Date)value;
        }

        try {
            formattedDate = ModelValidator.PST_ACCEPT_DATE_FORMAT.format(parsedDate);
        } catch(Exception ex) {
            throw new ParseException("invalid date value: '" + value + "'", 0);
        }
        return formattedDate;
    }

    public static List<EventMetaAttribute> filterEventMetaAttribute(List<EventMetaAttribute> list, String action) {
        List<EventMetaAttribute> filtered = new ArrayList<EventMetaAttribute>(list.size());

        List<String> hiddenAttributes = Arrays.asList(Constants.HIDDEN_ATTRIBUTES);
        Map<String, String> dictTypeMap = new HashMap<String, String>(0);

        for(EventMetaAttribute ema : list) {
            if(ema.isActive() && !hiddenAttributes.contains(ema.getLookupValue().getName())) {
                if(ema.getOptions() != null && ema.getOptions().startsWith("Dictionary:")){
                    String dictType = ema.getOptions().replace("Dictionary:", "");
                    boolean hasParent = dictType.contains("Parent:");

                    try {
                        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
                        ReadBeanPersister readPersister = new ReadBeanPersister(props);

                        if(hasParent){
                            String[] dictOpts = dictType.split(",Parent:");

                            if(action.equals("template")) {
                                ema.setOptions("Depends on " + dictOpts[1]);
                            } else {
                                String parentDictType = dictTypeMap.get(dictOpts[1]);
                                if (parentDictType == null) {
                                    for (EventMetaAttribute ema_ : list) {
                                        if (ema_.getLookupValue().getName().equals(dictOpts[1])) {
                                            parentDictType = ema_.getOptions().replace("Dictionary:", "");
                                            break;
                                        }
                                    }
                                }

                                if (parentDictType != null) {
                                    List<Dictionary> parentDictList = readPersister.getDictionaryByType(parentDictType);
                                    JSONArray jsonArray = new JSONArray();
                                    jsonArray.put(new JSONObject()
                                            .put("name", "parent_attribute")
                                            .put("value", dictOpts[1]));

                                    for (Dictionary parentDict : parentDictList) {
                                        List<Dictionary> childList = readPersister.getDictionaryDependenciesByType(parentDict.getDictionaryType(), parentDict.getDictionaryCode());

                                        StringBuilder sb = new StringBuilder();
                                        String delim = "";

                                        for (Dictionary dictionary : childList) {
                                            String code = dictionary.getDictionaryCode();
                                            String value = dictionary.getDictionaryValue();

                                            sb.append(delim);
                                            if (code.equals(value)) sb.append(value);
                                            else sb.append(code).append(" - ").append(value);

                                            delim = ";";
                                        }

                                        jsonArray.put(new JSONObject()
                                                .put("name", parentDict.getDictionaryCode())
                                                .put("value", sb.toString()));
                                    }

                                    ema.setOptions(jsonArray.toString());
                                }
                            }
                        } else{
                            List<Dictionary> dictList = readPersister.getDictionaryByType(dictType);
                            dictTypeMap.put(ema.getLookupValue().getName(), dictType);

                            StringBuilder sb = new StringBuilder();
                            String delim = "";

                            for(Dictionary dictionary : dictList){
                                sb.append(delim);

                                if(action.equals("template")) {
                                    sb.append(dictionary.getDictionaryCode());
                                } else {
                                    sb.append(dictionary.getDictionaryCode()).append(" - ").append(dictionary.getDictionaryValue());
                                }
                                delim = ";";
                            }

                            ema.setOptions(sb.toString());
                        }
                    } catch (Exception ex) {
                        logger.error("Exception in Shared AJAX : " + ex.toString());
                        ex.printStackTrace();
                    }
                }

                filtered.add(ema);
            }
        }
        return filtered;
    }

    public static void sortEventAttributeByOrder(List<EventAttribute> eaList) {
        Collections.sort(eaList, new Comparator<EventAttribute>() {
            @Override
            public int compare(EventAttribute o1, EventAttribute o2) {
                return o1.getMetaAttribute() == null && o2.getMetaAttribute() == null ? 0
                        : o1.getMetaAttribute() == null ? -1
                        : o2.getMetaAttribute() == null ? 1
                        : o1.getMetaAttribute().getOrder() == null && o2.getMetaAttribute().getOrder() == null ? 0
                        : o1.getMetaAttribute().getOrder() == null ? -1
                        : o2.getMetaAttribute().getOrder() == null ? 1
                        : o1.getMetaAttribute().getOrder().compareTo(o2.getMetaAttribute().getOrder());
            }
        });
    }

    public static void sortEventMetaAttributeByOrder(List<EventMetaAttribute> emaList) {
        Collections.sort(emaList, new Comparator<EventMetaAttribute>() {
            @Override
            public int compare(EventMetaAttribute o1, EventMetaAttribute o2) {
                return o1.getOrder() == null && o2.getOrder() == null ? 0
                        : o1.getOrder() == null ? -1
                        : o2.getOrder() == null ? 1
                        : o1.getOrder().compareTo(o2.getOrder());
            }
        });
    }

    public static EventMetaAttribute createEMA(
            Long projectId, String projectName, String eventName, String attrName,
            boolean required, boolean active, String dataType, String desc,
            String ontology, String label, String options, boolean sampleRequired, int order) {
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
        rtnVal.setLabel(label);
        rtnVal.setOntology(ontology);
        rtnVal.setOptions(options);
        rtnVal.setOrder(order);
        return rtnVal;
    }

    public static LookupValue createLookupValue(String name, String type, String dataType) {
        LookupValue rtnVal = new LookupValue();
        rtnVal.setName(name);
        rtnVal.setType(type);
        rtnVal.setDataType(dataType);
        return rtnVal;
    }

    public static Long getGuid() throws Exception {
        GuidGetter guidGetter = new GuidGetter();
        return guidGetter.getGuid();
    }

    public static String currentDateToDefaultFormat() {
        return new SimpleDateFormat(Constants.DATE_DEFAULT_FORMAT).format(Calendar.getInstance().getTime());
    }
}
