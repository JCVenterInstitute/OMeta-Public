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
  <jsp:include page="header.jsp"/>
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all'/>
  <style>
    .form-horizontal .control-label {
      text-align: left;
      float: left;
      min-width: 10%;
      padding-left: 13px;
      padding-right: 13px;
    }

    .form-horizontal .buttons {
      margin-left: 10%;
    }
  </style>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp"/>

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <div class="page-header">
          <h1>Create Project</h1>
        </div>

        <div id="HeaderPane" style="margin:15px 0 0 30px;">
          <div id="errorMessagesPanel" style="margin-top:15px;margin-bottom: 15px;"></div>
          <s:if test="hasActionErrors()">
            <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
              <div class="alert_info" onclick="$('.alert_info').remove();">
                <strong style="color: #ffffff;background-color: #a90329;padding: 3px;border-color: #900323;border: 1px solid transparent;padding: 6px 12px;"><s:iterator
                        value='actionErrors'><s:property/></s:iterator></strong>
              </div>
            </div>
          </s:if>
          <s:if test="hasActionMessages()">
            <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
              <div class="alert_info" onclick="$('.alert_info').remove();">
                <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator
                        value='actionMessages'><s:property/></s:iterator></strong>
              </div>
            </div>
          </s:if>
        </div>

        <s:form id="projectLoaderPage" name="projectLoaderPage" namespace="/" action="projectSetup" method="post" theme="simple" class="form-horizontal">
          <s:hidden name="jobType" id="jobType"/>

          <div class="form-group">
            <label for="_projectName" class="control-label">Project Name</label>
            <div class="col-sm-3">
              <s:textfield id="_projectName" name="loadingProject.projectName" class="form-control" placeholder="Project Name"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_parentProjectSelect" class="control-label">Parent Project</label>
            <div class="col-sm-3">
              <div class="input-group">
                <s:select id="_parentProjectSelect"
                          list="projectList" name="loadingProject.parentProjectId" headerKey="0" headerValue="None"
                          listValue="projectName" listKey="projectId" required="true" class="form-control"/>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label for="_isPublic" class="control-label">Public</label>
            <div class="col-sm-3">
              <s:select id="_isPublic" list="#{0:'No', 1:'Yes'}" name="loadingProject.isPublic" required="true" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_isSecure" class="control-label">Secure</label>
            <div class="col-sm-3">
              <s:select id="_isSecure" list="#{0:'No', 1:'Yes'}" name="loadingProject.isSecure" required="true" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_editGroupSelect" class="control-label">Edit Group</label>
            <div class="col-sm-3">
              <s:select id="_editGroupSelect"
                        list="groupList" headerKey="0" headerValue="Select Edit Group"
                        listValue="groupNameLookupValue.name" listKey="groupId"
                        name="loadingProject.editGroup" required="true" disabled="false" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_viewGroupSelect" class="control-label">View Group</label>
            <div class="col-sm-3">
              <s:select id="_viewGroupSelect"
                        list="groupList" headerKey="0" headerValue="Select View Group"
                        listValue="groupNameLookupValue.name" listKey="groupId"
                        name="loadingProject.viewGroup" required="true" disabled="false" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <div class="buttons col-sm-5">
              <input type="button" class="btn btn-primary" onclick="loadProject();" id="projectLoadButton" value="Create Project"/>
              <input type="button" class="btn btn-default" onclick="doClear();" value="Clear"/>
              <a class="btn btn-default" href="projectManagement.action" role="button">Back to Project Management</a>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html"/>

<script>
  var attrCnt = 0,
      pmaOptions = '',
      attrHtml = '<tr class="borderBottom"><td><select name="beanList[$cnt$].name">$o$</select></td>' +
          '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].active"/></td>' +
          '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].required" /></td>' +
          '<td><input type="text" name="beanList[$cnt$].options" size="32"/></td>' +
          '<td><input type="text" name="beanList[$cnt$].desc" size="32"/></td>' +
          '<td><input type="text" name="beanList[$cnt$].value" size="32"/></td></tr>';

  $(document).ready(function () {
    currentAttributeCount = 0; //attribute counter reset

    utils.combonize(null, '_parentProjectSelect');

    $.ajax({
      url: 'metadataSetupAjax.action',
      cache: false,
      async: false,
      data: 'type=g_a&projectId=0',
      success: function (res) {
        if (res.dataMap && res.dataMap.a) {
          pmaOptions += vs.empty;
          $.each(res.dataMap.a, function (i1, v1) {
            if (v1 && v1.lookupValueId && v1.name) {
              pmaOptions += vs.vvoption.replace(/\$v\$/g, v1.name);
            }
          });
        }
      }
    });

    utils.error.check();
  });

  function comboBoxChanged(option, id) {
    return;
  }

  function addAttribute() {
    $("tbody#pmaTbody").append(attrHtml.replace(/\$cnt\$/g, attrCnt).replace("$o$", pmaOptions));
    attrCnt++;
    $('tbody#pmaTbody select:last').combobox();
  }

  function newAttribute(type) {
    $.openPopupLayer({
      name: "LPopupAddLookupValue",
      width: 450,
      url: "addLookupValue.action?type=" + type
    });
  }

  function loadProject() {
    var errMsg = '';
    if ($('#_editGroupSelect').val() === '0' || $('#_viewGroupSelect').val() === '0') {
      errMsg += "Please select project EDIT GROUP and VIEW GROUP";
    }
    if (errMsg.length > 0) {
      utils.error.add(errMsg);
      return;
    }
    $("#jobType").val("insert");
    $('form#projectLoaderPage').submit();
  }

  function doClear() {
    attrCnt = 0;
    $("#_parentProjectSelect, #_editGroupSelect, #_viewGroupSelect, #_isPublic, #_isSecure").val(0);
    $("#pmaTbody").html('');
    $('#_projectName').val('');
  }
</script>
</body>
</html>
