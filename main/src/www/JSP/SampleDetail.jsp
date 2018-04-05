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

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<s:if test="iss == null || iss.equals(\"false\")">
  <jsp:include page="header.jsp"/>
</s:if>
<body>
<s:form id="statusPage" name="statusPage" namespace="/" action="sampleDetail" method="post" theme="simple">
  <s:hidden name="projectName"/>
  <s:hidden name="attributesOnScreen"/>
  <s:hidden name="attributes"/>
  <s:if test="iss.equals(\"true\")">
    <link rel="stylesheet" href="style/bootstrap.css"/>
    <script src="scripts/jquery/jquery-1.7.2.js"></script>
    <script src="scripts/jquery/jquery-ui.js"></script>
    <script src="scripts/ometa.utils.js"></script>
  </s:if>
  <s:else>
    <jsp:include page="top.jsp"/>
    <div id="HeaderPane" style="margin:15px 0 0 30px;">
      <div class="panelHeader" style="margin:0;">Sample Detail</div>
    </div>
  </s:else>
  <div class="page-header">
    <h1>
      <s:if test="detailMap.get(\"Organism\")==null">
        <s:property value="sample.sampleName"/>
      </s:if>
      <s:else>
        <s:property value="detailMap.Organism"/>
      </s:else>
    </h1>
  </div>
  <div style="width:100%;">
    <table class="table table-bordered table-striped table-condensed table-hover">
      <thead>
      <tr>
        <th>Event</th>
        <th>Status</th>
        <th>Description</th>
        <th>Date</th>
      </tr>
      </thead>
      <tbody>
      <s:iterator value="detailMap.event" var="event">
        <tr>
          <td><p><s:property value="eventName"/></p></td>
          <td><p><s:property value="eventStatus"/></p></td>
          <td>
            <s:iterator value="eventAttr" var="eAttr">
              <s:if test="name!=null && value!=null">
                <p><s:property value="name"/> - <s:property value="value" escapeHtml="false"/></p>
              </s:if>
            </s:iterator>
          </td>
          <td>
            <p><s:property value="date"/></p>
          </td>
        </tr>
      </s:iterator>
      </tbody>
    </table>
  </div>
</s:form>
</body>
</html>
