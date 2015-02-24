<%--
  Created by IntelliJ IDEA.
  User: mkuscuog
  Date: 2/18/2015
  Time: 2:07 PM
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
            <li>
              <a href="/ometa/projectManagement.action">Project Management</a>
            </li>
            <li>Edit Project</li>
          </ol>
        </div>

        <s:form id="editProjectPage" name="editProjectPage" namespace="/" action="editProject" method="post" theme="simple">
        <s:hidden name="projectId" value="%{project.projectId}" />
        <s:hidden name="action" value="update" />
        <div class="page-header">
          <h1>Edit Project </h1>
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
                <table id="project-information-table" style="width: 70%">
                  <tr>
                    <td>Project Name</td>
                    <td><div class="col-md-11">
                      <s:label name="project.projectName"/>
                    </div></td>
                  </tr>
                  <tr class="projectSelection">
                    <td>Public</td>
                    <td><div class="col-md-11">
                      <s:select id="_isPublic" list="#{0:'No', 1:'Yes'}" name="project.isPublic" required="true" />
                    </div></td>
              </tr>
              <tr class="projectSelection">
                <td>Secure</td>
                <td><div class="col-md-11">
                  <s:select id="_isSecure" list="#{0:'No', 1:'Yes'}" name="project.isSecure" required="true" />
                </div></td>
              </tr>
              <tr class="projectSelection">
                <td>Edit Group</td>
                <td><div class="col-md-11">
                  <s:select id="_editGroupSelect"
                            list="groupList" headerKey="0" headerValue="Select Edit Group"
                            listValue="groupNameLookupValue.name" listKey="groupId"
                            name="project.editGroup" required="true" disabled="false" />
                </div></td>
              </tr>
              <tr class="projectSelection">
                <td>View Group</td>
                <td><div class="col-md-11">
                  <s:select id="_viewGroupSelect"
                            list="groupList" headerKey="0" headerValue="Select View Group"
                            listValue="groupNameLookupValue.name" listKey="groupId"
                            name="project.viewGroup" required="true" disabled="false" />
                </div></td>
              </tr>
              <tr class="projectSelection">
                <td></td>
                <td><div class="col-md-11">
                  <input type="submit" class="btn btn-warning" id="_updateProject" value="Update Project"/>
                  <input type="button" class="btn btn-info" tyle="margin-left:15px;" onclick="self.close()" value="Back" />
                </div>
                </td>
              </tr>
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

<script type="text/javascript">
  $(document).ready(function() {
    utils.combonize(null, '_projectSelect');
    //$(".projectSelection").hide();
  });
</script>
</body>
</html>

