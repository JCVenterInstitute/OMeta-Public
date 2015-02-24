<%--
  Created by IntelliJ IDEA.
  User: mkuscuog
  Date: 2/20/2015
  Time: 3:53 PM
  To change this template use File | Settings | File Templates.
--%>
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <jsp:include page="header.jsp" />
  <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/chosen.css" />
  <style>
    td._details {
      text-align:left;
      padding:0 0 0 35px;
      border: 1px gray dotted;
    }
    td._details div {
      position: relative; overflow: auto; overflow-y: hidden;
    }
    td._details table td {
      border:1px solid white;
    }

    .datatable_top, .datatable_table, .datatable_bottom {
      float:left;
      clear:both;
      width:100%;
      min-width: 165px;
    }
    .datatable_bottom {margin-top: 10px}
    .dataTables_length {
      height: 29px;
      vertical-align: middle;
      min-width: 165px !important;
      margin-top: 2px;
    }
    .dataTables_filter {
      width: 260px !important;
    }
    .dataTables_info {
      padding-top: 0 !important;
    }
    .dataTables_paginate {
      float: left !important;
    }
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
            <li>Project Management</li>
          </ol>
        </div>

        <s:form id="editProjectPage" name="editProjectPage" namespace="/" action="editProject" method="post" theme="simple">
          <div class="page-header">
            <h1>Project Management </h1>
          </div>
          <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div id="errorMessagesPanel" style="margin-top:15px;margin-bottom: 15px;"></div>
            <s:if test="hasActionErrors()">
              <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
                <div class="alert_info" onclick="$('.alert_info').remove();">
                  <strong style="color: #ffffff;background-color: #a90329;padding: 3px;border-color: #900323;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionErrors'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
            <s:if test="hasActionMessages()">
              <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
                <div class="alert_info" onclick="$('.alert_info').remove();">
                  <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
          </div>

          <div id="mainContent">
            <div id="statusTableDiv">
              <div id="tableTop">
                <div class="row">
                  <table id="project-information-table" style="width: 80%">
                    <thead>
                    <tr>
                      <th>Project Name</th>
                      <th>Public</th>
                      <th>Secure</th>
                      <th>Edit Group</th>
                      <th>View Group</th>
                      <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator value="projectList" var="project">
                      <tr>
                        <td class="projectName"><s:property value="#project.projectName"/></td>
                        <td class="projectPublic"><s:if test="%{#project.isPublic==1}">Yes</s:if><s:else>No</s:else></td>
                        <td class="projectSecure"><s:if test="%{#project.isSecure==1}">Yes</s:if><s:else>No</s:else></td>
                        <td class="projectEditGroup">
                          <s:iterator value="groupList" var="group">
                            <s:if test="%{#group.groupId==#project.editGroup}"><s:property value="#group.groupNameLookupValue.name" /></s:if>
                          </s:iterator>
                        </td>
                        <td class="projectViewGroup">
                          <s:iterator value="groupList" var="group">
                            <s:if test="%{#group.groupId==#project.viewGroup}"><s:property value="#group.groupNameLookupValue.name" /></s:if>
                          </s:iterator>
                        </td>
                        <td class=""><input type="button" class="btn btn-warning" id="<s:property value="#project.projectId"/>" value="Edit Project" onclick="editProject(this.id);"/></td>
                      </tr>
                    </s:iterator>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />

<script src="scripts/jquery/chosen.jquery.min.js"></script>
<script src="scripts/jquery/jquery.dataTables.js"></script>
<script src="scripts/jquery/jquery.colReorderWithResize.js"></script>

<script type="text/javascript">
  $(document).ready(function() {
    $('#project-information-table').dataTable({
      "sDom": '<"datatable_top"lf><"datatable_table"rt><"datatable_bottom"ip>',
      "sPaginationType": "full_numbers",
      "bAutoWidth" : true,
      "aoColumnDefs": [
        {"sWidth": "30%", "aTargets": [ 0 ]},
        {"sWidth": "10%", "aTargets":[1]},
        {"sWidth": "10%", "aTargets":[2]},
        {"sWidth": "20%", "aTargets":[3]},
        {"sWidth": "20%", "aTargets":[4]},
        {"sWidth": "10%", "aTargets":[5]}
      ]
    });
  });

  function editProject(id){
    var $editProjectForm = $('<form>').attr({
      id: 'editProjectForm',
      method: 'POST',
      target: '_blank',
      action: 'editProject.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'projectId',
      name: 'projectId',
      value : id
    }).appendTo($editProjectForm);

    $('<input>').attr({
      id: 'action',
      name: 'action',
      value : 'lookup'
    }).appendTo($editProjectForm);

    $('body').append($editProjectForm);
    $editProjectForm.submit();
  }
</script>
</body>
</html>