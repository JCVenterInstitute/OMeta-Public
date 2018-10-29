
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <jsp:include page="header.jsp" />
  <link rel="stylesheet" href="style/chosen.css" />
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

  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
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
        </div>

        <s:form id="editActorPage" name="editActorPage" namespace="/" action="editActor" method="post" theme="simple" class="form-horizontal">
          <input type="hidden" name="actorId" value="<s:property value='actor.loginId' />"/>

          <div class="form-group">
            <label class="control-label">User ID</label>
            <div class="col-sm-3">
              <p class="form-control-static"><s:property value="actor.username"/></p>
            </div>
          </div>
          <div class="form-group">
            <label for="_firstName" class="control-label">First Name</label>
            <div class="col-sm-3">
              <s:textfield id="_firstName" name="actor.firstName" class="form-control" placeholder="First Name"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_middleName" class="control-label">Middle Name</label>
            <div class="col-sm-3">
              <s:textfield id="_middleName" name="actor.middleName" class="form-control" placeholder="Middle Name"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_lastName" class="control-label">Last Name</label>
            <div class="col-sm-3">
              <s:textfield id="_lastName" name="actor.lastName" class="form-control" placeholder="Last Name"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_email" class="control-label">Email</label>
            <div class="col-sm-3">
              <s:textfield id="_email" name="actor.email" class="form-control" placeholder="Email"/>
            </div>
          </div>
          <div class="form-group">
            <label for="groupSelect" class="control-label">Groups</label>
            <div class="col-sm-3">
              <s:select id="groupSelect"
                        list="groups" name="groupIds"
                        listValue="groupNameLookupValue.name" listKey="groupId"
                        multiple="true" required="true" class="form-control" />
            </div>
          </div>
          <div class="form-group">
            <div class="buttons col-sm-3">
              <input type="button" class="btn btn-primary" onclick="javascript:loadActor();" id="loadButton" value="Update"/>
              <a class="btn btn-default" href="actorRole.action" role="button">Back</a>
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
