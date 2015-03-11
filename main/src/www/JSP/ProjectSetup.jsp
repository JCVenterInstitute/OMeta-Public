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

<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <jsp:include page="header.jsp" />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />

  <%--<link rel="stylesheet" href="style/version01.css" />--%>
  <style>
    #pmaTbody .ui-autocomplete-input { width: 150px; }
  </style>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <div id="ribbon">
          <ol class="breadcrumb">
            <li>
              <a href="/ometa/secureIndex.action">Dashboard</a>
            </li>
            <li>Admin</li>
            <li>Project Setup</li>
          </ol>
        </div>

        <s:form id="projectLoaderPage" name="projectLoaderPage" namespace="/" action="projectSetup" method="post" theme="simple">
          <s:hidden name="jobType" id="jobType"/>
          <div class="page-header">
            <h1>Create Project</h1>
          </div>

          <div id="HeaderPane">
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
          <div id="mainContent">
            <!--<div id="columnsTable"></div>  for column listing-->
            <div id="statusTableDiv">
              <div class="middle-header">
                <h2>Project Information</h2>
              </div>
              <div id="tableTop">
                <table id="project-information-table" style="width: 70%">
                  <tr>
                    <td>Project Name</td>
                    <td><div class="col-md-11">
                      <s:textfield id="_projectName" name="loadingProject.projectName" size="35px"/>
                    </div></td>
                  </tr>
                  <tr>
                    <td>Parent Project</td>
                    <td><div class="col-md-11 combobox">
                      <s:select id="_parentProjectSelect"
                                list="projectList" name="loadingProject.parentProjectId" headerKey="0" headerValue="None"
                                listValue="projectName" listKey="projectId" required="true" />
                    </div></td>
                  </tr>
                  <tr>
                    <td>Public</td>
                    <td><div class="col-md-11">
                      <s:select id="_isPublic" list="#{0:'No', 1:'Yes'}" name="loadingProject.isPublic" required="true" />
                    </div></td>
                  </tr>
                  <tr>
                    <td>Secure</td>
                    <td><div class="col-md-11">
                      <s:select id="_isSecure" list="#{0:'No', 1:'Yes'}" name="loadingProject.isSecure" required="true" />
                    </div></td>
                  </tr>
                  <tr>
                    <td>Edit Group</td>
                    <td><div class="col-md-11">
                      <s:select id="_editGroupSelect"
                                list="groupList" headerKey="0" headerValue="Select Edit Group"
                                listValue="groupNameLookupValue.name" listKey="groupId"
                                name="loadingProject.editGroup" required="true" disabled="false" />
                    </div></td>
                  </tr>
                  <tr>
                    <td>View Group</td>
                    <td><div class="col-md-11">
                      <s:select id="_viewGroupSelect"
                                list="groupList" headerKey="0" headerValue="Select View Group"
                                listValue="groupNameLookupValue.name" listKey="groupId"
                                name="loadingProject.viewGroup" required="true" disabled="false" />
                    </div></td>
                  </tr>

                </table>
              </div>
              <s:div id="submitDiv" cssStyle="margin:15px 10px 5px 0;width:100%;">
                <input type="button" class="btn btn-success" onclick="loadProject();" id="projectLoadButton" value="Create Project"/>
                <!--<input type="button" onclick="addAttribute();" id="attributeAddButton" value="Add Project Attribute" />
                <input type="button" onclick="newAttribute('a');" id="newAttributeButton" value="New Attribute" />-->
                <input type="button" class="btn btn-info" onclick="doClear();" value="Clear" />
                <input type="button" class="btn" tyle="margin-left:15px;" onclick="self.close()" value="Cancel" />
              </s:div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />

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
    $("#_parentProjectSelect, #_editGroupSelect, #_viewGroupSelect, #_isPublic, #_isSecure").val(0);
    $("#pmaTbody").html('');
    $('#_projectName').val('');
  }
</script>
</body>
</html>
