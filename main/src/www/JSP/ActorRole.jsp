<!DOCTYPE HTML>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
  <link rel="stylesheet" href="style/chosen.css" />
  <style>
    tr.spacer > td {
      padding-top: 1em;
    }
    ul {
      margin: 0 !important;
      padding: 0 !important;
    }
  </style>
</head>

<body>
<s:form id="addActorPage" name="addActorPage" namespace="/" action="addActor" method="post" theme="simple">
  <s:include value="TopMenu.jsp" />
  <div id="HeaderPane" style="margin:15px 0 0 30px;">
    <table cellpadding="0" cellspacing="0" border="0">
      <tr><td class="panelHeader">Actor Role Management</td></tr>
      <tr>
        <td>
          <div id="errorMessagesPanel" style="margin-top:15px;"></div>
          <s:if test="hasActionErrors()">
            <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
          </s:if>
        </td>
      </tr>
    </table>
  </div>
  <div id="middle_content_template">
    <div id="mainDiv">
      <div id="tableTop">
        <table>
          <tr>
            <div id="actorDropBox">
              <td>Actor</td>
              <td style="padding-left:10px" class="ui-combobox">
                <s:select id="actorSelect"
                            list="actors" name="actorId" headerKey="0" headerValue="None"
                            listValue="username + ' - ' + firstName + ' ' +lastName" listKey="loginId" required="true" />
              </td>
            </div>
          </tr>
          <tr>
            <div id="groupDropBox">
              <td>Groups</td>
              <td style="padding-left:10px" class="ui-combobox" width="450px">
                <s:select id="groupSelect"
                            list="groups" name="groupIds"
                            listValue="groupNameLookupValue.name" listKey="groupNameLookupValue.lookupValueId" 
                            multiple="true" required="true" style="width:400px;height:19px;
                            "/>
              </td>
            </div>
          </tr>
        </table>
      </div>
    </div>
    <div id="buttonDiv" style="margin:15px 10px 5px 0;width:100%;">
      <input type="button" onclick="doSubmit();" id="submit" value="Setup Role"/>
      <input type="button" onclick="popup();" id="addRole" value="Add Actor Role"/>
      <input type="button" onclick="doClear();" value="Clear" />
    </div>
  </div>
</s:form>

<script src="scripts/jquery/chosen.jquery.min.js"></script>

<script type="text/javascript">
  (function() {
    utils.error.check();
    $('#actorSelect').chosen().change(function() {
      var selectedUser = $(this).find("option:selected");
      makeAjax(selectedUser.val());
      console.log(selectedUser.text(), selectedUser.val());
    });
    $('#groupSelect').chosen();
  })();

  function doClear() {
    $("#actorSelect, #groupSelect").val('');
  }

  function makeAjax(actorId, cb) {
    $.ajax({
      url: 'actorRole.action',
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
            //preselect current actor's roles
            $("#groupSelect").trigger("chosen:updated");
          }
        }
      },
      fail: function(html) {
        utils.error.add("Ajax Process has Failed.");
      }
    });
  }

  function popup() {
    $.openPopupLayer({
      name: 'LPopupAddLookupValue',
      width: 450,
      url: 'addLookupValue.action?type=gr'
    });  
  }

  function doSubmit() {
    var actorId = $('#actorSelect').val();
    if(actorId && actorId != 0) {
      $('form').submit();
    }
  }
</script>
</body>
</html>
