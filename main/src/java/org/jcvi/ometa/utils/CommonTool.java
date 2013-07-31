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

import org.jcvi.ometa.model.AttributeModelBean;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.validation.ModelValidator;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/29/13
 * Time: 3:27 PM
 */
public class CommonTool {

    public static Map<String, String> decorateAttributeMap(Map<String, Object> attributeMap, List<String> attributeList, Project project) {
        Map<String, String> newSampleAttrMap = new HashMap<String, String>();

        if(attributeList==null) { //when attribute list is not available, utilizes keyset from the map
            attributeList = new ArrayList<String>(attributeMap.size());
            attributeList.addAll(attributeMap.keySet());
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
                                    "&sampleName=" + ((String)attributeMap.get("Sample Name")).replaceAll(" ", "%20") +
                                    "&sampleId=" + attributeMap.get("sampleId"),
                            decoratedValue
                    );
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
    public static String convertIntoATag(String url, String displayValue) {
        return (
                String.format(Constants.A_TAG_HTML, "#", String.format(Constants.NEW_WINDOW_LINK_HTML, url))+
                        displayValue+Constants.A_TAG_CLOSING_HTML
        ).replaceAll("\\\"", "\\\\\"");

    }

    public static String convertIntoFileLink(String fileName, Long attributeId) {
        String justFileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        return "<a href=\"getFile.action?fn="+fileName+"\">"+justFileName+"</a>";
    }

    public static <M extends AttributeModelBean> Map<String, Object> getAttributeValueMap(List<M> attributeList, boolean decorate, String[] skipArr) {
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

    public static String convertTimestampToDate(Object value) {
        return ModelValidator.PST_DEFAULT_DATE_FORMAT.format(value);
    }
}
