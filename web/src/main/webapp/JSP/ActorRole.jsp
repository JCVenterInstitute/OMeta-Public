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
        <s:form id="actorRolePage" name="actorRolePage" namespace="/" action="actorRole" method="post" theme="simple">
          <s:hidden name="type" id="type"/>

          <div class="page-header">
            <h1>User Management</h1>
          </div>

          <div id="HeaderPane">
            <div id="errorMessagesPanel" style="margin-top:15px;"></div>
            <s:if test="hasActionErrors()">
              <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
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
          <div id="mainContent">
            <div id="mainDiv">
              <input type="button" class="btn btn-primary" onclick="popup();" id="addRole" value="Add Actor Role" style="display: none;">
              <a class="btn btn-primary" href="addActor.action" role="button" id="addNewActor" style="display: none;margin-left: 0.5em;">Add New Actor</a>
              <table id="userManagementTable" class="table table-bordered table-striped table-condensed table-hover">
                <thead>
                <tr>
                  <th>User ID</th>
                  <th>Name</th>
                  <th>E-mail</th>
                  <th>Groups</th>
                  <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <s:iterator value="actors" var="actor">
                  <tr>
                    <td class="actorUsername"><s:property value="#actor.username"/></td>
                    <td class="actorName"><s:property value="#actor.firstName"/> <s:property value="#actor.lastName"/></td>
                    <td class="actorEmail"><s:property value="#actor.email"/></td>
                    <td class="actorGroup" id="<s:property value="#actor.loginId" />"></td>
                    <td class=""><input type="button" class="btn btn-default" id="<s:property value="#actor.loginId"/>" value="Edit Actor" onclick="editActor(this.id);"/></td>
                  </tr>
                </s:iterator>
                </tbody>
              </table>
            </div>
          </div>
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
    var table = $('#userManagementTable').DataTable({
      responsive: true,
      lengthChange: false,
      buttons: ['copy', 'excel', 'pdf', 'csv', 'colvis'],
      autoWidth: false,
      columnDefs: [
        {"width": "10%", "targets": [0]},
        {"width": "20%", "targets": [1]},
        {"width": "20%", "targets": [2]},
        {"width": "40%", "targets": [3]},
        {"width": "10%", "targets": [4]}
      ],
      "fnDrawCallback": function () {
        $('.actorGroup').each(function (i, obj) {
          var $this = $(this);
          var actorId = $this.attr('id');

          $.ajax({
            url: 'actorRoleAjax.action',
            cache: false,
            async: true,
            data: 'actorId=' + parseInt(actorId),
            success: function (res) {
              if (res.errorMsg) {
                utils.error.add(res.errorMsg);
              } else {
                var actorGroups = res.actorGroups;
                if (actorGroups) {
                  var _html = '';
                  var len = actorGroups.length;
                  $.each(actorGroups, function (i, ag) {
                    //$('#groupSelect option[value="' + ag.groupId + '"]').attr('selected', 'selected');
                    _html += ag.group.groupNameLookupValue.name;
                    if (i != len - 1) _html += ', ';
                  });

                  $this.html(_html);
                }
              }
            },
            fail: function (html) {
              utils.error.add("Ajax Process has Failed.");
            }
          });
        });
      }
    });

    table.buttons().container()
        .appendTo('#userManagementTable_wrapper .col-sm-6:eq(0)');

    $("#addNewActor").show().appendTo('#userManagementTable_filter');
    $("#addRole").show().appendTo('#userManagementTable_filter');
  });

  function popup() {
    $.openPopupLayer({
      name: 'LPopupAddLookupValue',
      width: 450,
      url: 'addLookupValue.action?type=gr'
    });
  }

  function editActor(id) {
    var $editActorForm = $('<form>').attr({
      id: 'editActorForm',
      method: 'POST',
      action: 'editActor.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'actorId',
      name: 'actorId',
      value: id
    }).appendTo($editActorForm);

    $('body').append($editActorForm);
    $editActorForm.submit();
  }
</script>
</body>
</html>
