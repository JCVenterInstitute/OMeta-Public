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
            <li>User Registration</li>
          </ol>
        </div>

        <s:form id="addActorPage" name="addActorPage" namespace="/" action="addActor" method="post" theme="simple">
          <div class="page-header">
            <h1>User Registration </h1>
          </div>
          <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div id="errorMessagesPanel" style="margin-top:15px;"></div>
            <s:if test="hasActionErrors()">
              <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
            </s:if>
            <s:if test="hasActionMessages()">
              <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
                <div class="alert_info" onclick="$('.alert_info').remove();">
                  <strong style="color: #ffffff;background-color: #a90329;padding: 3px;border-color: #900323;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
            <div style="font-size:0.9em;margin-left: 300px;" >
              [<small class="text-danger" style="vertical-align: bottom">*</small>-Required]
            </div>
          </div>

          <div id="mainContent">
            <div id="statusTableDiv">
              <div id="tableTop">
                <table id="addActorTable">
                  <tr class="gappedTr">
                    <td align="left"><small class="text-danger">*</small>User ID </td>
                    <td style="padding-left:7px;"><s:textfield id="_usertName" name="actor.username" size="35px"/></td>
                    <!-- <td style="padding-left:7px;"><strong>User ID must match your UNIX ID.</strong></td> -->
                  </tr>
                  <tr class="gappedTr">
                    <td align="left"><small class="text-danger">*</small>Password </td>
                    <td style="padding-left:7px;"><s:password id="_password" name="actor.password" size="35px"/></td>
                    <!-- <td style="padding-left:7px;"><strong>User ID must match your UNIX ID.</strong></td> -->
                  </tr>
                  <tr class="gappedTr">
                    <td align="left"><small class="text-danger">*</small>Confirm Password </td>
                    <td style="padding-left:7px;"><s:password id="_passVerify" name="passVerify" size="35px"/></td>
                    <!-- <td style="padding-left:7px;"><strong>User ID must match your UNIX ID.</strong></td> -->
                  </tr>
                  <tr class="gappedTr">
                    <td align="left"><small class="text-danger">*</small>First Name </td>
                    <td style="padding-left:7px;"><s:textfield id="_firstName" name="actor.firstName" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left">Middle Name </td>
                    <td style="padding-left:7px;"><s:textfield id="_middleName" name="actor.middleName" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left"><small class="text-danger">*</small>Last Name </td>
                    <td style="padding-left:7px;"><s:textfield id="_lastName" name="actor.lastName" size="35px"/></td>
                  </tr>
                  <tr class="gappedTr">
                    <td align="left"><small class="text-danger">*</small>Email </td>
                    <td style="padding-left:7px;"><s:textfield id="_email" name="actor.email" size="35px"/></td>
                  </tr>
                    <%--<tr class="gappedTr">
                      <td align="right">Phone Number </td>
                      <td style="padding-left:7px;"><s:textfield id="_phone" name="actor.phone" size="35px"/></td>
                    </tr>
                    <tr class="gappedTr">
                      <td align="right"><small class="text-danger">*</small>Ceirs Center Name </td>
                      <td style="padding-left:7px;">
                        <s:select label="Project" id="_center" name="actor.centerName" cssStyle=""
                                    headerKey="" headerValue="" required="true"
                                    list="#{'CRIP':'CRIP', 'Emory-UGA CEIRS':'Emory-UGA CEIRS',
                                    'Johns Hopkins CEIRS':'Johns Hopkins CEIRS', 'NYICE':'NYICE',
                                    'St. Jude CEIRS':'St. Jude CEIRS', 'Other':'Other'}"/>
                      </td>
                    </tr>
                    <tr class="gappedTr">
                      <td align="right"><small class="text-danger">*</small>Role </td>
                      <td style="padding-left:7px;">
                        <s:select label="Project" id="_role" name="actor.role" cssStyle=""
                                    headerKey="" headerValue="" required="true"
                                    list="#{'CEIRS Center PI':'CEIRS Center PI', 'CEIRS Center Coordinator':'CEIRS Center Coordinator',
                                    'CEIRS Center Data Manager':'CEIRS Center Data Manager', 'Clinician':'Clinician',
                                    'Laboratory PI':'Laboratory PI', 'Researcher':'Researcher',
                                    'Physician':'Physician', 'Other':'Other'}"/>
                        </select>
                      </td>
                    </tr>
                    <tr class="gappedTr">
                      <td align="right">Lab PI Name</td>
                      <td style="padding-left:7px;"><s:textfield id="_piName" name="actor.piName" size="35px"/></td>
                    </tr>
                    <tr class="gappedTr">
                      <td align="right">Lab PI Email </td>
                      <td style="padding-left:7px;"><s:textfield id="_piEmail" name="actor.piEmail" size="35px"/></td>
                    </tr>--%>
                </table>
              </div>
              <s:div id="submitDiv" cssStyle="margin:15px 10px 5px 200px;width:100%;">
                <input type="button" class="btn btn-primary" onclick="javascript:loadActor();" id="loadButton" value="Register"/>
                <input type="button" class="btn btn-info" tyle="margin-left:15px;" onclick="javascript:doClear();" value="Clear" />
                <input type="button" class="btn" tyle="margin-left:15px;" onclick="self.close()" value="Cancel" />
              </s:div>
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
    } else if($('#_password').val() !== $('#_passVerify').val()) {
      alert('Verify Password does not match with the password');
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
