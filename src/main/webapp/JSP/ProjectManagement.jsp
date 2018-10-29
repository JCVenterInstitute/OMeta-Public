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
  <jsp:include page="header.jsp"/>
  <link rel="stylesheet" href="datatables/datatables.css" type='text/css' media='all'/>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp"/>

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <div class="page-header">
          <h1>Project Management </h1>
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

        <s:form id="editProjectPage" name="editProjectPage" namespace="/" action="editProject" method="post" theme="simple">
          <input type="button" class="btn btn-success" onclick="window.location.href='projectSetup.action'" id="addNewProject" value="Project Setup" style="display: none;"/>
          <table id="project-information-table" class="table table-bordered table-striped table-condensed table-hover">
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
                    <s:if test="%{#group.groupId==#project.editGroup}"><s:property value="#group.groupNameLookupValue.name"/></s:if>
                  </s:iterator>
                </td>
                <td class="projectViewGroup">
                  <s:iterator value="groupList" var="group">
                    <s:if test="%{#group.groupId==#project.viewGroup}"><s:property value="#group.groupNameLookupValue.name"/></s:if>
                  </s:iterator>
                </td>
                <td>
                  <input type="button" class="btn btn-xs btn-warning" id="<s:property value="#project.projectId"/>" value="Edit Project" onclick="editProject(this.id);"
                         style="float: left;"/>
                  <input type="button" class="btn btn-xs btn-info" id="<s:property value="#project.projectName"/>" value="Download Setup" onclick="downloadProjectSetup(this.id);"
                         style="float: right;"/>
                </td>
              </tr>
            </s:iterator>
            </tbody>
          </table>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html"/>

<script src="datatables/datatables.js"></script>
<script src="datatables/Buttons-1.4.2/js/dataTables.buttons.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.colVis.js"></script>
<script src="datatables/JSZip-2.5.0/jszip.js"></script>
<script src="datatables/pdfmake-0.1.32/pdfmake.js"></script>
<script src="datatables/pdfmake-0.1.32/vfs_fonts.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.bootstrap.js"></script>

<script type="text/javascript">
  $(document).ready(function () {
    var table = $('#project-information-table').DataTable({
      responsive: true,
      lengthChange: false,
      buttons: ['copy', 'excel', 'pdf', 'csv', 'colvis'],
      autoWidth: false,
      columnDefs: [
        {"width": "30%", "targets": [0]},
        {"width": "6%", "targets": [1]},
        {"width": "6%", "targets": [2]},
        {"width": "19%", "targets": [3]},
        {"width": "19%", "targets": [4]},
        {"width": "20%", "targets": [5]}
      ]
    });

    table.buttons().container()
        .appendTo('#project-information-table_wrapper .col-sm-6:eq(0)');

    $("#addNewProject").show().appendTo('#project-information-table_filter');
  });

  function editProject(id) {
    var $editProjectForm = $('<form>').attr({
      id: 'editProjectForm',
      method: 'POST',
      action: 'editProject.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'projectId',
      name: 'projectId',
      value: id
    }).appendTo($editProjectForm);

    $('<input>').attr({
      id: 'action',
      name: 'action',
      value: 'lookup'
    }).appendTo($editProjectForm);

    $('body').append($editProjectForm);
    $editProjectForm.submit();
  }

  function downloadProjectSetup(name) {
    var $projectSetupForm = $('<form>').attr({
      id: 'projectSetupForm',
      method: 'POST',
      action: 'downloadProjectSetup.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'projectName',
      name: 'projectName',
      value: name
    }).appendTo($projectSetupForm);

    $('<input>').attr({
      id: 'eventName',
      name: 'eventName',
      value: 'ProjectSetup'
    }).appendTo($projectSetupForm);

    $('body').append($projectSetupForm);
    $projectSetupForm.submit();
  }
</script>
</body>
</html>