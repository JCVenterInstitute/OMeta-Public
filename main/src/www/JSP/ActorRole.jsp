
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <jsp:include page="header.jsp" />
  <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/chosen.css" />
  <%--<link rel="stylesheet" href="style/version01.css" />--%>
  <style>
    /*tr.spacer > td {
      padding-top: 1em;
    }
    ul {
      margin: 0 !important;
      padding: 0 !important;
    }*/
    #addNewActor{
      float: right;
      margin-right: 20px;
      margin-bottom: 20px;
    }
    #addRole{
      float: right;
      margin-right: 300px;
      margin-bottom: 20px;
    }
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
            <li>User Management</li>
          </ol>
        </div>

        <s:form id="actorRolePage" name="actorRolePage" namespace="/" action="actorRole" method="post" theme="simple">
          <s:hidden name="type" id="type" />

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
                  <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
          </div>
          <div id="mainContent">
            <div id="mainDiv">
              <input type="button" class="btn btn-info" onclick="popup();" id="addRole" value="Add Actor Role"/>
              <input type="button" class="btn btn-success" onclick="window.open('addActor.action')" id="addNewActor" value="Add New Actor"/>
              <table id="userManagementTable">
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
                    <td class=""><input type="button" class="btn btn-warning" id="<s:property value="#actor.loginId"/>" value="Edit Actor" onclick="editActor(this.id);"/></td>
                  </tr>
                </s:iterator>
                </tbody>
              </table>

                <%--<div id="tableTop">
                  <div class="row row_spacer">
                    <div class="col-md-1">Actor</div>
                    <div class="col-md-11 combobox">
                      <s:select id="actorSelect"
                                      list="actors" name="actorId" headerKey="0" headerValue="None"
                                      listValue="username + ' - ' + firstName + ' ' +lastName" listKey="loginId" required="true" />
                    </div>
                  </div>
                  <div class="row row_spacer">
                    <div class="col-md-1">Groups</div>
                    <div class="col-md-11">
                      <s:select id="groupSelect"
                                      list="groups" name="groupIds"
                                      listValue="groupNameLookupValue.name" listKey="groupId"
                                      multiple="true" required="true" style="width:400px;height:19px;
                                      "/>
                    </div>
                  </div>
                </div>
              </div>
              <div id="buttonDiv" style="margin:15px 10px 5px 0;width:100%;">
                <input type="button" class="btn btn-success" onclick="submit();" id="setup" value="Setup Role"/>
                <input type="button" class="btn btn-warning" onclick="passReset();" id="resetPass" value="Reset Password"/>
                <input type="button" class="btn btn-default" onclick="doClear();" value="Clear" />
              </div>--%>
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
  $(document).ready(function (){
    $('#userManagementTable').dataTable({
      "sDom": '<"datatable_top"lf><"datatable_table"rt><"datatable_bottom"ip>',
      "sPaginationType": "full_numbers",
      "bAutoWidth" : true,
      "aoColumnDefs": [
        {"sWidth": "10%", "aTargets": [ 0 ]},
        {"sWidth": "20%", "aTargets":[1]},
        {"sWidth": "20%", "aTargets":[2]},
        {"sWidth": "40%", "aTargets":[3]},
        {"sWidth": "10%", "aTargets":[4]}
      ],
      "fnDrawCallback":function(){
        $('.actorGroup').each(function (i, obj){
          var $this = $(this);
          var actorId = $this.attr('id');

          $.ajax({
            url: 'actorRoleAjax.action',
            cache: false,
            async: true,
            data: 'actorId='+parseInt(actorId),
            success: function(res){
              if(res.errorMsg) {
                utils.error.add(res.errorMsg);
              } else {
                var actorGroups = res.actorGroups;
                if(actorGroups) {
                  var _html = '';
                  var len = actorGroups.length;
                  $.each(actorGroups, function(i, ag) {
                    //$('#groupSelect option[value="' + ag.groupId + '"]').attr('selected', 'selected');
                    _html += ag.group.groupNameLookupValue.name;
                    if(i != len - 1) _html += ', ';
                  });

                  $this.html(_html);
                }
              }
            },
            fail: function(html) {
              utils.error.add("Ajax Process has Failed.");
            }
          });
        });
      }
    });
  });

  (function() {
    utils.error.check();
    $('#actorSelect').combobox();
    // $('#actorSelect').chosen().change(function() {
    //   $("#groupSelect option:selected").removeAttr("selected");
    //   var selectedUser = $(this).find("option:selected");
    //   makeAjax(selectedUser.val());
    // });
    $('#groupSelect').chosen();
  })();

  function doClear() {
    $("#actorSelect, #groupSelect").val('');
    $('#groupSelect > option').removeAttr('selected');
    $("#groupSelect").trigger("chosen:updated");
  }

  function makeAjax(actorId, cb) {
    $.ajax({
      url: 'actorRoleAjax.action',
      cache: false,
      async: true,
      data: 'actorId='+parseInt(actorId),
      success: function(res){
        if(res.errorMsg) {
          utils.error.add(res.errorMsg);
        } else {
          if(cb) {
            cb(res);
          } else {
            $('#groupSelect > option').removeAttr('selected');
            var actorGroups = res.actorGroups;
            if(actorGroups) {
              var $groupOptions = $('#groupSelect option');
              $.each(actorGroups, function(i, ag) {
                $('#groupSelect option[value="' + ag.groupId + '"]').attr('selected', 'selected');
              })
            }

            $("#groupSelect").trigger("chosen:updated");
          }
        }
      },
      fail: function(html) {
        utils.error.add("Ajax Process has Failed.");
      }
    });
  }

  function comboBoxChanged(option, id) {
    if(id === 'actorSelect') {
      if(option && option.value !== 0) {
        makeAjax(option.value);
      }
    }
  }

  function popup() {
    $.openPopupLayer({
      name: 'LPopupAddLookupValue',
      width: 450,
      url: 'addLookupValue.action?type=gr'
    });
  }

  function passReset() {
    $("#type").val("reset");
    submit();
  }

  function submit() {
    var actorId = $('#actorSelect').val();
    if(actorId && actorId != 0) {
      $('form').submit();
    }
  }

  function editActor(id){
    var $editActorForm = $('<form>').attr({
      id: 'editActorForm',
      method: 'POST',
      target: '_blank',
      action: 'editActor.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'actorId',
      name: 'actorId',
      value : id
    }).appendTo($editActorForm);

    $('body').append($editActorForm);
    $editActorForm.submit();
  }
</script>
</body>
</html>
