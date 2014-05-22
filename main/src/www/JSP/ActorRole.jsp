<!DOCTYPE HTML>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
  <style>
    tr.spacer > td {
      padding-top: 1em;
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
    <div id="statusTableDiv">
      <div id="tableTop">
        <table id="ddTable">
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
              <td style="padding-left:10px" class="ui-combobox">
                <s:select id="groupSelect"
                            list="groups" name="actorGroups" headerKey="0" headerValue="None"
                            listValue="groupNameLookupValue.name" listKey="groupNameLookupValue.lookupValueId" required="true" />
              </td>
            </div>
          </tr>
        </table>
      </div>
    </div>
  </div>
</s:form>

<script type="text/javascript">
  (function() {
    $('select[id$=Select]').combobox();
  })();

  function loadActor() {
    if(!validateEmail()) {
      alert('Invalid email address');
      return;
    }
    $('form#addActorPage').submit();
  }

  function validateEmail() {
    var email = $('input#_email').val(), re = /\S+@\S+\.\S+/;
    return re.test(email);
  }

  function doClear() {
    $("#_usertName, #_firstName, #_lastName, #_middleName, #_email").val('');
  }

  utils.error.check();
</script>
</body>
</html>
