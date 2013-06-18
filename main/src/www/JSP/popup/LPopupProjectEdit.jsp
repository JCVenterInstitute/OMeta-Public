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
    		$('form#LPopupProjectEdit').submit();
    	}
    </script>
</head>


<body>
<s:form id="LPopupProjectEdit" name="LPopupProjectEdit"
        namespace="/"
        action="projectEditProcess"
        method="post" theme="simple">
		<s:hidden name="projectId" />
        <s:hidden name="projectName" value="%{project.projectName}"/>
		<s:hidden name="editType" id="editType" value="" />
    <div class="popup">
        <div class="popup-header">
            <h2>Edit Project Detail</h2>
            <a href="javascript:;" onclick="$.closePopupLayer('LPopupProjectEdit')" title="Close" class="close-link">Close</a>
            <br clear="both" />
        </div>
        <div style="padding:10px;">
            <fieldset style="padding:5px;">
                <legend style="margin-left:10px;font-size:14px;"> Project </legend>
				<div style="margin:5px 5px 5px 5px;">
					<div style="margin-top:3px;">&nbsp;</div>
					<label>Public</label>
					<s:select list="#{0:'No', 1:'Yes'}" name="isPublic" value="%{project.isPublic}" /><!--cssStyle="margin-left:43px"/>-->
					<div style="margin-top:3px;">&nbsp;</div>
					<label>Secure</label>
					<s:select list="#{0:'No', 1:'Yes'}" name="isSecure" value="%{project.isSecure}" /><!--cssStyle="margin-left:36px"/>-->
				</div>
				<div style="float:right;margin:5px 10px;">
					<input type="button" value="Project Update" onclick="doSubmit('project');"/>
				</div>
            </fieldset>
			<div style="margin:5px 0 5px;">&nbsp;</div>
			<s:if test="%{projectElements != null && projectElements.size() > 0}">
			<fieldset>
                <legend style="margin-left:10px;font-size:14px;">Project Attribute</legend>
				<div style="margin:5px 5px 5px 5px;">
					<table>
					<s:iterator value="projectElements" var="projAttr" status="projAttrStat">
						<s:if test="#projAttr.metaAttribute.lookupValue.name != 'Project Name'">
							<tr>
							<s:hidden name="projectElements[%{#projAttrStat.index}].id" />
							<s:hidden name="projectElements[%{#projAttrStat.index}].projectId"  />
							<s:hidden name="projectElements[%{#projAttrStat.index}].nameLookupValueId"  />
							<s:hidden name="projectElements[%{#projAttrStat.index}].creationDate"  />
							<s:hidden name="projectElements[%{#projAttrStat.index}].createdBy"  />
							<td align="right"><label><s:property value="#projAttr.metaAttribute.lookupValue.name"/></label></td>
							<td>
							<s:if test="%{projectElements.get(#projAttrStat.index).getAttributeStringValue() != null}">
								<s:textfield name="projectElements[%{#projAttrStat.index}].attributeStringValue" size="40"/>
							</s:if>
							<s:elseif test="%{projectElements.get(#projAttrStat.index).getAttributeIntValue() != null}">
								<s:textfield name="projectElements[%{#projAttrStat.index}].attributeIntValue" size="40"/>
							</s:elseif>
							<s:elseif test="%{projectElements.get(#projAttrStat.index).getAttributeFloatValue() != null}">
								<s:textfield name="projectElements[%{#projAttrStat.index}].attributeFloatValue" size="40"/>
							</s:elseif>
							<s:elseif test="%{projectElements.get(#projAttrStat.index).getAttributeDateValue() != null}">
								<s:textfield name="projectElements[%{#projAttrStat.index}].attributeDateValue" size="40"/>
							</s:elseif>
							</td>
							</tr>
							<tr><td>&nbsp;</td></tr>
						</s:if>
					</s:iterator>
					</table>
				</div>
				<div style="float:right;margin:5px 10px;">
					<input type="button" value="Attribute Update" onclick="doSubmit('projectAttribute');"/>
				</div>
            </fieldset>
			</s:if>
			
        </div>
		
    </div>

</s:form>
</body>
</html>