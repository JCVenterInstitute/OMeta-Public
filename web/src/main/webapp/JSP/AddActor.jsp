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

<!DOCTYPE HTML>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <jsp:include page="header.jsp" />
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
          <h1>User Registration</h1>
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

        <s:form id="addActorPage" name="addActorPage" namespace="/" action="addActor" method="post" theme="simple" class="form-horizontal">
          <div class="form-group">
            <label for="_userName" class="control-label"><small class="text-danger">*</small>User ID</label>
            <div class="col-sm-3">
              <s:textfield id="_userName" name="actor.username" class="form-control" placeholder="User ID"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_firstName" class="control-label"><small class="text-danger">*</small>First Name</label>
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
            <label for="_lastName" class="control-label"><small class="text-danger">*</small>Last Name</label>
            <div class="col-sm-3">
              <s:textfield id="_lastName" name="actor.lastName" class="form-control" placeholder="Last Name"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_email" class="control-label"><small class="text-danger">*</small>Email</label>
            <div class="col-sm-3">
              <s:textfield id="_email" name="actor.email" class="form-control" placeholder="Email"/>
            </div>
          </div>
          <div class="form-group">
            <div class="buttons col-sm-3">
              <input type="button" class="btn btn-primary" onclick="javascript:loadActor();" id="loadButton" value="Register"/>
              <input type="button" class="btn btn-default" onclick="javascript:doClear();" value="Clear" />
              <a class="btn btn-default" href="actorRole.action" role="button">Cancel</a>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />
<script type="text/javascript">

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
    $("#_userName, #_firstName, #_lastName, #_middleName, #_email").val('');
  }

  utils.error.check();
</script>
</body>
</html>
