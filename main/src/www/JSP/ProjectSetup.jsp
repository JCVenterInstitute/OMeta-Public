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
<s:form id="projectLoaderPage" name="projectLoaderPage" namespace="/" action="projectSetup" method="post" theme="simple">
  <s:hidden name="jobType" id="jobType"/>
  <s:include value="TopMenu.jsp" />
  <div id="HeaderPane" style="margin:15px 0 0 30px;">
    <div class="panelHeader">Project Setup</div>
    <div id="errorMessagesPanel" style="margin-top:15px;"></div>
    <s:if test="hasActionErrors()">
      <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
    </s:if>
    <s:if test="hasActionMessages()">
      <div class="alert_info" onclick="$('.alert_info').remove();">
        <strong><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
      </div>
    </s:if>
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
        <input type="button" onclick="loadProject();" id="projectLoadButton" value="Load Project"/>
        <input type="button" onclick="addAttribute();" id="attributeAddButton" value="Add Project Attribute" />
        <input type="button" onclick="newAttribute('a');" id="newAttributeButton" value="New Attribute" />
        <input type="button" onclick="doClear();" value="Clear" />
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
      success: function(res){
        if(res.dataMap && res.dataMap.a) {
          pmaOptions += vs.empty;
          $.each(res.dataMap.a, function(i1,v1) {
            if(v1 && v1.lookupValueId && v1.name) {
              pmaOptions += vs.vvoption.replace(/\\$v\\$/g, v1.name);
            }
          });
        }
      }
    });

    utils.error.check();
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
      url: "addLookupValue.action?type="+type
    });
  }

  function loadProject() {
    var errMsg = '';
    if($('#_editGroupSelect').val()==='0' || $('#_viewGroupSelect').val()==='0') {
      errMsg += "Please select project EDIT GROUP and VIEW GROUP";
    }
    if(errMsg.length>0) {
      utils.error.add(errMsg);
      return;
    }
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
