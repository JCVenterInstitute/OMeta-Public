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

<!DOCTYPE HTML>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>    
    <link rel="stylesheet" href="style/dataTables.css" />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
    <style>
        #pmaTbody .ui-autocomplete-input { width: 150px; }
    </style>
</head>

<body>
    <s:form id="projectLoaderPage" name="projectLoaderPage"
            namespace="/"
            action="projectSetup"
            method="post" theme="simple">
        <s:hidden name="jobType" id="jobType"/>
        <s:include value="TopMenu.jsp" />
        <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <table cellpadding="0" cellspacing="0" border="0">
                <tr><td class="panelHeader">Project Setup</td></tr>
                <tr>
                    <td>
                        <div id="errorMessagesPanel" style="margin-top:15px;">
                            <s:if test="hasActionErrors()">
                                <input type="hidden" id="error_messages" value="<s:iterator value="actionErrors"><s:property/><br/></s:iterator>"/>
                                <input type="button" style="background-color:red;"
                                       value="PROCESSING ERROR: Click Here to See the Error." onclick="utils.error.show('error_messages');return false;"/>
                            </s:if>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
        <div id="middle_content_template">
            <!--<div id="columnsTable"></div>  for column listing-->
            <div id="statusTableDiv">
                <div style="margin:0 10px 0 0;">
                    <h1 class="csc-firstHeader">Project Information</h1>
                </div>
                <div id="tableTop">
                    <table>
                        <tr>
                            <td align="right">Project Name</td>
                            <td><s:textfield id="_projectName" name="loadingProject.projectName" size="35px"/></td>
                        </tr>
                        <tr class="gappedTr">
                            <td align="right">Parent Project</td>
                            <td class="ui-combobox">
                                <s:select id="_parentProjectSelect"
                                          list="projectList" name="loadingProject.parentProjectId" headerKey="0" headerValue="None"
                                          listValue="projectName" listKey="projectId" required="true" />
                            </td>
                        </tr>
                        <tr class="gappedTr">
                            <td align="right">Public</td>
                            <td>
                                <s:select id="_isPublic" list="#{0:'No', 1:'Yes'}" name="loadingProject.isPublic" required="true" />
                            </td>
                        </tr>
                        <tr class="gappedTr">
                            <td align="right">Secure</td>
                            <td>
                                <s:select id="_isSecure" list="#{0:'No', 1:'Yes'}" name="loadingProject.isSecure" required="true" />
                            </td>
                        </tr>
                        <tr class="gappedTr">
                            <td align="right">Edit Group</td>
                            <td>
                                <s:select id="_editGroupSelect"
                                          list="groupList" headerKey="0" headerValue="Select Edit Group"
                                          listValue="groupNameLookupValue.name" listKey="groupId"
                                          name="loadingProject.editGroup" required="true" disabled="false" />
                            </td>
                        </tr>
                        <tr class="gappedTr">
                            <td align="right">View Group</td>
                            <td>
                                <s:select id="_viewGroupSelect"
                                          list="groupList" headerKey="0" headerValue="Select View Group"
                                          listValue="groupNameLookupValue.name" listKey="groupId"
                                          name="loadingProject.viewGroup" required="true" disabled="false" />
                            </td>
                        </tr>
                    </table>
                </div>
                <div style="margin:10px 10px 0 0;">
                    <h1 class="csc-firstHeader">Project Attributes</h1>
                </div>
                <div id="attributeInputDiv" style="margin:5px 10px 0 0;">
                    <table>
    					<thead>
    						<tr>
    							<th class="tableHeaderNoSort boxesHeader">Attribute</th>
    							<th class="tableHeaderNoSort checkBoxHeader">Active</th>
    							<th class="tableHeaderNoSort checkBoxHeader">Required</th>
    							<th class="tableHeaderNoSort boxesHeader">Options</th>
    							<th class="tableHeaderNoSort boxesHeader">Desc</th>
                                <th class="tableHeaderNoSort boxesHeader">Value</th>
    						</tr>
    					</thead>
                        <tbody id="pmaTbody"></tbody>
                    </table>
                </div>
                <s:div id="submitDiv" cssStyle="margin:15px 10px 5px 0;width:100%;">
                    <input type="button" onclick="javascript:loadProject();" id="projectLoadButton" value="Load Project"/>
                    <input type="button" onclick="javascript:addAttribute();" id="attributeAddButton" value="Add Project Attribute" />
    				<input type="button" onclick="javascript:newAttribute('a');" id="newAttributeButton" value="New Attribute" />
                    <input type="button" onclick="javascript:doClear();" value="Clear" />
                </s:div>
            </div>
        </div>
    </s:form>

    <script>
        var attrCnt = 0,
            pmaOptions = '',
            attrHtml = '<tr class="borderBottom"><td><select name="beanList[$cnt$].name">$o$</select></td>' +
                '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].active"/></td>' +
                '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].required" /></td>' +
                '<td><input type="text" name="beanList[$cnt$].options" size="32"/></td>' +
                '<td><input type="text" name="beanList[$cnt$].desc" size="32"/></td>' +
                '<td><input type="text" name="beanList[$cnt$].value" size="32"/></td></tr>';

        $(document).ready(function() {
            currentAttributeCount = 0; //attribute counter reset
            
            utils.combonize(null, '_parentProjectSelect');
            
            $.ajax({
                url: 'metadataSetupAjax.action',
                cache: false,
                async: false,
                data: 'type=g_a&projectId=0',
                success: function(html){
                    if(html.dynamicList != null) {
                        pmaOptions += '<option value=0>Select Attribute</option>';
                        $.each(html, function(i1,v1) {
                            if(v1 != null) {
                                $.each(v1, function(i2,v2) {
                                    if(v2!=null && v2.lookupValueId!=null && v2.name!=null) {
                                        pmaOptions += vs.vvoption.replace(/\\$v\\$/g,v2.name);
                                    }
                                });
                            }
                        });
                    }
                },
                fail: function(html) {
                    alert("Ajax Process has Failed.");
                }
            });
        });
        
        function comboBoxChanged(option, id) { return; }

        function addAttribute() {
            $("tbody#pmaTbody").append(attrHtml.replace(/\\$cnt\\$/g,attrCnt).replace("$o$",pmaOptions));
            attrCnt++;
            $('tbody#pmaTbody select:last').combobox();
        }
        
        function newAttribute(type) {
            $.openPopupLayer({
                name: "LPopupAddLookupValue",
                width: 450,
                url: "addLookupValue.action?w="+type
            });
        }

        function loadProject() {
            /*var docObj = document.projectLoaderPage;
            var emptyTextBoxes = $('input:text').filter(function() { return this.value == ""; });
            var string = "Please fill blanks for : \n";
            var fields = '';

            emptyTextBoxes.each(function() {
                fields += "\n" + this.id;
            });

            if(fields != '') {
                alert(string + fields);
                return;
            }*/
            $("#jobType").val("insert");
            $('form#projectLoaderPage').submit();
        }

        function doClear() {
            attrCnt=0;
            $("#_parentProjectSelect, #_editGroupSelect, #_viewGroupSelect").val(0);
            $("#pmaTbody").html('');
        }
    </script>
</body>
</html>
