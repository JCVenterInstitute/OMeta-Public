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
</head>
<body>
<s:form id="helpPage" name="helpPage" namespace="/" action="help" method="post" theme="simple">
  <jsp:include page="TopMenu.jsp" />
  <div id="HeaderPane" style="margin:15px 0 0 30px;">
    <div class="panelHeader">Help</div>
    <div id="errorMessagesPanel" style="margin-top:15px;"></div>
    <s:if test="hasActionErrors()">
      <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
    </s:if>
    <s:if test="hasActionMessages()">
      <div class="alert_info" onclick="$('.alert_info').remove();">
        <strong><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
      </div>
    </s:if>
  </div>
  <div id="middle_content_template">
    <div id="statusTableDiv">
      <div style="margin:0 10px 0 0;">
        <h1 class="csc-firstHeader">Help</h1>
      </div>
      <div id="tableTop">
        <table>
          <tr class="gappedTr">
            <td align="right">Name</td>
            <td><s:textfield id="_name" name="name" size="35px"/></td>
          </tr>
          <tr class="gappedTr">
            <td align="right">Email</td>
            <td><s:textfield id="_email" name="email" size="35px"/></td>
          </tr>
          <tr class="gappedTr">
            <td align="right" style="vertical-align:top">Description</td>
            <td><s:textarea id="_msg" name="msg" cols="35" rows="10"/></td>
          </tr>
        </table>
      </div>
      <div id="submitDiv" cssStyle="margin:15px 10px 5px 200px;width:100%;">
        <input type="submit" id="sendButton" value="Submit"/>
        <input type="button" style="margin-left:15px;" onclick="javascript:_page.clear();" value="Clear" />
      </div>
    </div>
  </div>
</s:form>

<script>
  var _page = {
    clear: function() {
      $("#_name, #_email, #_msg").val('');
    }
  }
</script>
</body>
</html>

