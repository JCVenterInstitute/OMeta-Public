
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
            <li><a href="/ometa/actorRole.action">User Management</a></li>
            <li>Edit Actor</li>
          </ol>
        </div>

        <s:form id="editActorPage" name="editActorPage" namespace="/" action="editActor" method="post" theme="simple">
          <input type="hidden" name="actorId" value="<s:property value='actor.loginId' />"/>

          <div class="page-header">
            <h1>Edit Actor </h1>
          </div>
          <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div id="errorMessagesPanel" style="margin-top:15px;"></div>
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
            <div style="font-size:0.9em;margin-left: 300px;" >
              [<img style="vertical-align:bottom;" src="images/icon/info_r.png">-Not Editable]
            </div>
          </div>

          <div id="mainContent">
            <div id="statusTableDiv">
              <div id="tableTop">
                <table id="editActorTable">
                  <tr class="gappedTr">
                    <td align="left"><img class="attributeIcon" src="images/icon/info_r.png" style="float: right;"/>User ID </td>
                    <td style="padding-left:7px;"><s:label name="actor.username"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left">First Name </td>
                    <td style="padding-left:7px;"><s:textfield id="_firstName" name="actor.firstName" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left">Middle Name </td>
                    <td style="padding-left:7px;"><s:textfield id="_middleName" name="actor.middleName" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left">Last Name </td>
                    <td style="padding-left:7px;"><s:textfield id="_lastName" name="actor.lastName" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left">Email </td>
                    <td style="padding-left:7px;"><s:textfield id="_email" name="actor.email" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left">Groups</td>
                    <td style="padding-left:7px;">
                      <s:select id="groupSelect"
                                list="groups" name="groupIds"
                                listValue="groupNameLookupValue.name" listKey="groupId"
                                multiple="true" required="true" style="width:400px;height:19px;
                                      "/>
                    </td>
                  </tr>
                </table>
              </div>
              <s:div id="submitDiv" cssStyle="margin:15px 10px 5px 200px;width:100%;">
                <input type="button" class="btn btn-primary" onclick="javascript:loadActor();" id="loadButton" value="Update"/>
                <input type="button" class="btn btn-info" tyle="margin-left:15px;" onclick="self.close()" value="Cancel" />
              </s:div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />

<script src="scripts/jquery/chosen.jquery.min.js"></script>

<script type="text/javascript">
  $(document).ready(function (){
    fillActorGroups();
    $('#groupSelect').chosen();
  });

  function loadActor() {
    if(!validateEmail()) {
      alert('Invalid email address');
      return;
    }

    $('form#editActorPage').submit();
  }

  function validateEmail() {
    var email = $('input#_email').val(), re = /\S+@\S+\.\S+/;
    return re.test(email);
  }

  function fillActorGroups(){
    <s:iterator value="actorGroups" var="actorGroup">
    $('#groupSelect option[value="' + <s:property value="groupId"/> + '"]').attr('selected', 'selected');
    </s:iterator>
  }
</script>
</body>
</html>
