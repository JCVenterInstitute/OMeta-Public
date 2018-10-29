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
  <jsp:include page="header.jsp"/>
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

  <jsp:include page="top.jsp"/>

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <div class="page-header">
          <h1>Edit Project </h1>
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

        <s:form id="editProjectPage" name="editProjectPage" namespace="/" action="editProject" method="post" theme="simple" class="form-horizontal">
          <s:hidden name="projectId" value="%{project.projectId}"/>
          <s:hidden name="action" value="update"/>

          <div class="form-group">
            <label for="_projectName" class="control-label">Project Name</label>
            <div class="col-sm-3">
              <p class="form-control-static"><s:property value="project.projectName" /></p>
            </div>
          </div>
          <div class="form-group">
            <label for="_isPublic" class="control-label">Public</label>
            <div class="col-sm-3">
              <s:select id="_isPublic" list="#{0:'No', 1:'Yes'}" name="project.isPublic" required="true" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_isSecure" class="control-label">Secure</label>
            <div class="col-sm-3">
              <s:select id="_isSecure" list="#{0:'No', 1:'Yes'}" name="project.isSecure" required="true" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_editGroupSelect" class="control-label">Edit Group</label>
            <div class="col-sm-3">
              <s:select id="_editGroupSelect"
                        list="groupList" headerKey="0" headerValue="Select Edit Group"
                        listValue="groupNameLookupValue.name" listKey="groupId"
                        name="project.editGroup" required="true" disabled="false" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label for="_viewGroupSelect" class="control-label">View Group</label>
            <div class="col-sm-3">
              <s:select id="_viewGroupSelect"
                        list="groupList" headerKey="0" headerValue="Select View Group"
                        listValue="groupNameLookupValue.name" listKey="groupId"
                        name="project.viewGroup" required="true" disabled="false" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <div class="buttons col-sm-5">
              <input type="submit" class="btn btn-success" id="_updateProject" value="Update Project"/>
              <a class="btn btn-default" href="projectManagement.action" role="button">Back to Project Management</a>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>
</body>
</html>

