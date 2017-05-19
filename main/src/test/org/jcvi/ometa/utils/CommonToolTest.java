package org.jcvi.ometa.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jcvi.ometa.model.EventAttribute;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.LookupValue;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.jcvi.ometa.validation.ModelValidator.INT_DATA_TYPE;
import static org.jcvi.ometa.validation.ModelValidator.PST_ACCEPT_DATE_FORMAT;
import static org.jcvi.ometa.validation.ModelValidator.STRING_DATA_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: movence
 * Date: 9/25/14
 * Time: 1:01 PM
 * org.jcvi.ometa.utils
 */
public class CommonToolTest {

    @Test
    public void decorateAttributeMap() throws Exception {

    }

    @Test
    public void decorateAttribute() throws Exception {
        String sraAccession = "sra accession", sraStatus = "sra status", submittedValue = "submitted";
        String annAccession = "annotation accession", annStatus = "annotation status";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(sraAccession, "test1");
        map.put(sraStatus, submittedValue);
        map.put(annAccession, "tes2");
        map.put(annStatus, submittedValue);
        map.put("nullTest", "null");

        assertEquals(submittedValue, CommonTool.decorateAttribute(map, sraAccession, null));
        assertEquals(submittedValue, CommonTool.decorateAttribute(map, annAccession, null));
        assertEquals("", CommonTool.decorateAttribute(map, "nullTest", null));
    }

    @Test
    public void displayLinkOnlyForPublished() throws Exception {
        String url = "www.jcvi.org/url/test", type = "testType";

        assertEquals("submitted", CommonTool.displayLinkOnlyForPublished("Submitted", url, type));
        assertEquals("other", CommonTool.displayLinkOnlyForPublished("other", url, type));
        assertEquals(CommonTool.convertIntoATag(url, type), CommonTool.displayLinkOnlyForPublished("published", url, type));
    }

    @Test
    public void truncateURL() throws Exception {
        String URL = "www.jcvi.org/url/test";
        int length = 13;

        String expected = "www.jcvi.org/...";

        assertEquals(expected, CommonTool.truncateURL(URL, length));
    }

    @Test
    public void convertIntoATag() throws Exception {
        String url = "url", displayValue = "testValue";
        String expected = String.format(Constants.A_TAG_HTML, "#", String.format(Constants.NEW_WINDOW_LINK_HTML, url), url, "tooltip")+
                displayValue + Constants.A_TAG_CLOSING_HTML;

        assertEquals(expected, CommonTool.convertIntoATag(url, displayValue));

    }

    @Test
    public void convertIntoATagWithTooltip() throws Exception {
        String url = "url", displayValue = "testValue";
        String expected = String.format(Constants.A_TAG_HTML_WITH_TOOLTIP, "#", String.format(Constants.NEW_WINDOW_LINK_HTML, url), url, "tooltip")+
                displayValue + Constants.A_TAG_CLOSING_HTML;

        assertEquals(expected, CommonTool.convertIntoATagWithTooltip(url, displayValue));
    }

    @Test
    public void convertIntoFileLink() throws Exception {
        String filePath = "\\path\\fileName", lookupValueName = "testLV", sampleName = "testSample";
        Long projectId = 1L, attributeId = 2L;

        String expectedURL = "<a href=\"downloadfile.action?fileName=fileName&attributeName=testLV&projectId=1&sampleVal=testSample\">fileName</a>";

        assertEquals(expectedURL, CommonTool.convertIntoFileLink(filePath, lookupValueName, projectId, sampleName, attributeId));
    }

    @Test
    public void getAttributeValueMap() throws Exception {
        String test1Name = "test1", test1Value = "testValue1";

        LookupValue lv1 = new LookupValue();
        lv1.setLookupValueId(1L);
        lv1.setDataType(STRING_DATA_TYPE);
        lv1.setName(test1Name);

        EventMetaAttribute ema1 = new EventMetaAttribute();
        ema1.setEventMetaAttributeId(1L);
        ema1.setLookupValue(lv1);

        EventAttribute ea1 = new EventAttribute();
        ea1.setId(1L);
        ea1.setMetaAttribute(ema1);
        ea1.setAttributeStringValue(test1Value);

        String test2Name = "test2";
        int test2Value = 2;

        LookupValue lv2 = new LookupValue();
        lv2.setLookupValueId(2L);
        lv2.setDataType(INT_DATA_TYPE);
        lv2.setName(test2Name);

        EventMetaAttribute ema2 = new EventMetaAttribute();
        ema2.setEventMetaAttributeId(2L);
        ema2.setLookupValue(lv2);

        EventAttribute ea2 = new EventAttribute();
        ea2.setId(2L);
        ea2.setMetaAttribute(ema2);
        ea2.setAttributeIntValue(test2Value);

        Map<String, Object> expectedMap = new HashMap<String, Object>();
        expectedMap.put(test1Name, test1Value);
        expectedMap.put(test2Name, test2Value);

        assertEquals(expectedMap, CommonTool.getAttributeValueMap(Arrays.asList(ea1, ea2), 1L, "testSample", false, null));
    }

    @Test
    public void convertTimestampToDate() throws Exception {
        String date1 = "03/14/2017";
        assertEquals("2017-03-14", CommonTool.convertTimestampToDate(date1));

        String date2 = "2017/03/14";
        assertNotEquals("2017-03-14", CommonTool.convertTimestampToDate(date2));
    }

    @Test
    public void filterEventMetaAttribute() throws Exception {
        String test1Name = "test1", test1Value = "testValue1";

        LookupValue lv1 = new LookupValue();
        lv1.setLookupValueId(1L);
        lv1.setDataType(STRING_DATA_TYPE);
        lv1.setName(test1Name);

        EventMetaAttribute ema1 = new EventMetaAttribute();
        ema1.setEventMetaAttributeId(1L);
        ema1.setActive(true);
        ema1.setOptions("{validate:DataValidator.checkFieldUniqueness}");
        ema1.setLookupValue(lv1);

        String test2Name = "test2";
        int test2Value = 2;

        LookupValue lv2 = new LookupValue();
        lv2.setLookupValueId(2L);
        lv2.setDataType(INT_DATA_TYPE);
        lv2.setName(test2Name);

        EventMetaAttribute ema2 = new EventMetaAttribute();
        ema2.setEventMetaAttributeId(2L);
        ema2.setActive(true);
        ema2.setLookupValue(lv2);

        CommonTool.filterEventMetaAttribute(Arrays.asList(ema1, ema2), null);
    }

    @Test
    public void sortEventAttributeByOrder() throws Exception {
        EventMetaAttribute ema1 = new EventMetaAttribute();
        ema1.setEventMetaAttributeId(1L);
        ema1.setOrder(1);

        EventMetaAttribute ema2 = new EventMetaAttribute();
        ema2.setEventMetaAttributeId(2L);
        ema2.setOrder(2);

        EventMetaAttribute ema3 = new EventMetaAttribute();
        ema3.setEventMetaAttributeId(3L);
        ema3.setOrder(3);

        EventMetaAttribute ema4 = new EventMetaAttribute();
        ema4.setEventMetaAttributeId(4L);
        ema4.setOrder(4);

        EventAttribute ea1 = new EventAttribute();
        ea1.setId(1L);
        ea1.setMetaAttribute(ema1);

        EventAttribute ea2 = new EventAttribute();
        ea2.setId(2L);
        ea2.setMetaAttribute(ema2);

        EventAttribute ea3 = new EventAttribute();
        ea3.setId(3L);
        ea3.setMetaAttribute(ema3);

        EventAttribute ea4 = new EventAttribute();
        ea4.setId(4L);
        ea4.setMetaAttribute(ema4);

        List<EventAttribute> expected = Arrays.asList(ea1, ea2, ea3, ea4);
        List<EventAttribute> actual = Arrays.asList(ea1, ea4, ea2, ea3);

        CommonTool.sortEventAttributeByOrder(actual);

        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));

    }

    @Test
    public void sortEventMetaAttributeByOrder() throws Exception {
        EventMetaAttribute ema1 = new EventMetaAttribute();
        ema1.setEventMetaAttributeId(1L);
        ema1.setOrder(1);

        EventMetaAttribute ema2 = new EventMetaAttribute();
        ema2.setEventMetaAttributeId(2L);
        ema2.setOrder(2);

        EventMetaAttribute ema3 = new EventMetaAttribute();
        ema3.setEventMetaAttributeId(3L);
        ema3.setOrder(3);

        EventMetaAttribute ema4 = new EventMetaAttribute();
        ema4.setEventMetaAttributeId(4L);
        ema4.setOrder(4);

        List<EventMetaAttribute> expected = Arrays.asList(ema1, ema2, ema3, ema4);
        List<EventMetaAttribute> actual = Arrays.asList(ema1, ema4, ema2, ema3);

        CommonTool.sortEventMetaAttributeByOrder(actual);

        assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @Test
    public void createEMA() throws Exception {
        Long projectId = 0L;
        String projectName = "testProject", eventName = "testEvent", attrName = "testAttribute", dataType = "testDataType";
        String desc = "testDescription", ontology = "testOntology", label = "testLabel", options = "testOptions";
        boolean required = true, active = false, sampleRequired = false;
        int order = 3;

        EventMetaAttribute emaExpected = new EventMetaAttribute();
        emaExpected.setProjectId(projectId);
        emaExpected.setProjectName(projectName);
        emaExpected.setEventName(eventName);
        emaExpected.setAttributeName(attrName);
        emaExpected.setRequired(required);
        emaExpected.setActive(active);
        emaExpected.setDataType(dataType);
        emaExpected.setDesc(desc);
        emaExpected.setSampleRequired(sampleRequired);
        emaExpected.setLabel(label);
        emaExpected.setOntology(ontology);
        emaExpected.setOptions(options);
        emaExpected.setOrder(order);

        assertTrue(EqualsBuilder.reflectionEquals(emaExpected, CommonTool.createEMA(projectId, projectName, eventName, attrName, required
        , active, dataType, desc, ontology, label, options, sampleRequired, order)));
    }

    @Test
    public void createLookupValue() throws Exception {
        String name = "testName", type = "testType", dataType = "testDataType";
        LookupValue lvExpected = new LookupValue();
        lvExpected.setName(name);
        lvExpected.setType(type);
        lvExpected.setDataType(dataType);

        assertTrue(EqualsBuilder.reflectionEquals(lvExpected, CommonTool.createLookupValue(name, type, dataType)));
    }

    @Test
    public void getGuid() throws Exception {
        //Guid needs to be deployed
        assertTrue(CommonTool.getGuid() instanceof Long);
    }

    @Test
    public void testParsingDate() throws Exception {
        String defaultFormat = "2014-09-25";
        String slash = "2014/09/25";
        String slash2 = "09/25/2014";
        String ddmmmyyyy = "25-Sep-2014";
        String mmmyyyy = "Sep-2014";
        String yyyy = "2014";
        String whole = "2014-09-25T12:23:20";

        //Assert.assertNotNull(CommonTool.convertTimestampToDate(yyyy));

        SimpleDateFormat dateFormatYYYY = new SimpleDateFormat("yyyy");
        System.out.println(dateFormatYYYY.parse(yyyy));
        SimpleDateFormat dateFormatMMMYYYY = new SimpleDateFormat("MMM-yyyy");
        System.out.println(dateFormatMMMYYYY.parse(mmmyyyy));
        SimpleDateFormat dateFormatDDMMMYYYY = new SimpleDateFormat("dd-MMM-yyyy");
        System.out.println(dateFormatDDMMMYYYY.parse(ddmmmyyyy));
        SimpleDateFormat dateFormatSlashYYYY = new SimpleDateFormat("MM/dd/yyyy");
        System.out.println(dateFormatSlashYYYY.parse(slash2));
        SimpleDateFormat dateFormatSlash = new SimpleDateFormat("yyyy/MM/dd");
        System.out.println(dateFormatSlash.parse(slash));
        SimpleDateFormat dateFormatWhole = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        System.out.println(dateFormatWhole.parse(whole));

    }
}
