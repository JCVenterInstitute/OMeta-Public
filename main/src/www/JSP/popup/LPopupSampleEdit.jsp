<%--
  ~ Copyright J. Craig Venter Institute, 2013
  ~
  ~ The creation of this program was supported by J. Craig Venter Institute
  ~ and National Institute for Allergy and Infectious Diseases (NIAID),
  ~ Contract number HHSN272200900007C.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}" />
    <script>
    	var doSubmit = function(type) {
    		$('input#editType:hidden').val(type);
    		$('form#LPopupSampleEdit').submit();
    	}
    </script>
</head>


<body>
<s:form id="LPopupSampleEdit" name="LPopupSampleEdit"
        namespace="/"
        action="sampleEditProcess"
        method="post" theme="simple">
		<s:hidden name="projectId" />
		<s:hidden name="projectName" value="%{project.projectName}"/>
        <s:hidden name="sampleId" />
        <s:hidden name="sampleName" value="%{sample.sampleName}"/>
		<s:hidden name="editType" id="editType" value="" />
    <div class="popup">
        <div class="popup-header">
            <h2>Edit Sample Detail</h2>
            <a href="javascript:;" onclick="$.closePopupLayer('LPopupSampleEdit')" title="Close" class="close-link">Close</a>
            <br clear="both" />
        </div>
        <div style="padding:10px;">
            <fieldset style"padding:5px;">
                <legend style="margin-left:10px;font-size:14px;"> Sample </legend>
				<div style="margin:5px 5px 5px 5px;">
					<label>Public</label>
					<s:select list="#{0:'No', 1:'Yes'}" name="isPublic" value="%{sample.isPublic}" /><!--cssStyle="margin-left:43px"/>-->
				</div>
				<div style="float:right;margin:5px 10px;">
					<input type="button" value="Sample Update" onclick="doSubmit('sample');"/>
				</div>
            </fieldset>
			<div style="margin:5px 0 5px;">&nbsp;</div>
            <s:if test="%{sampleElements != null && sampleElements.size() > 0}">
			<fieldset>
                <legend style="margin-left:10px;font-size:14px;">Sample Attribute</legend>
				<div style="margin:5px 5px 5px 5px;">
					<table>
					<s:iterator value="sampleElements" var="sampAttr" status="sampAttrStat">
						<s:if test="#sampAttr.metaAttribute.lookupValue.name != 'Sample Name' && #sampAttr.metaAttribute.lookupValue.name != 'Project Name'">
							<tr>
							<s:hidden name="sampleElements[%{#sampAttrStat.index}].id" />
							<s:hidden name="sampleElements[%{#sampAttrStat.index}].projectId"  />
              <s:hidden name="sampleElements[%{#sampAttrStat.index}].sampleId"  />
							<s:hidden name="sampleElements[%{#sampAttrStat.index}].nameLookupValueId"  />
							<s:hidden name="sampleElements[%{#sampAttrStat.index}].creationDate"  />
							<s:hidden name="sampleElements[%{#sampAttrStat.index}].createdBy"  />
							<td align="right"><label><s:property value="#sampAttr.metaAttribute.lookupValue.name"/></label></td>
							<td>
							<s:if test="%{sampleElements.get(#sampAttrStat.index).getAttributeStringValue() != null}">
								<s:textfield name="sampleElements[%{#sampAttrStat.index}].attributeStringValue" size="40"/>
							</s:if>
							<s:elseif test="%{sampleElements.get(#sampAttrStat.index).getAttributeIntValue() != null}">
								<s:textfield name="sampleElements[%{#sampAttrStat.index}].attributeIntValue" size="40"/>
							</s:elseif>
							<s:elseif test="%{sampleElements.get(#sampAttrStat.index).getAttributeFloatValue() != null}">
								<s:textfield name="sampleElements[%{#sampAttrStat.index}].attributeFloatValue" size="40"/>
							</s:elseif>
							<s:elseif test="%{sampleElements.get(#sampAttrStat.index).getAttributeDateValue() != null}">
								<s:textfield name="sampleElements[%{#sampAttrStat.index}].attributeDateValue" size="40"/>
							</s:elseif>
							</td>
							</tr>
							<tr><td>&nbsp;</td></tr>
						</s:if>
					</s:iterator>
					</table>
				</div>
				<div style="float:right;margin:5px 10px;">
                    <input type="button" value="Attribute Update" onclick="doSubmit('sampleAttribute');"/>
				</div>
            </fieldset>
			</s:if>
        </div>
    </div>
</s:form>
</body>
</html>