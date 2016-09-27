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
    <script src="scripts/jquery/jquery-1.7.2.js"></script>
    <script src="scripts/jquery/menubar.js"></script>
</head>


<body onload="initializeMenubar(<%=request.getRemoteUser()==null%>, "<%=request.getRemoteUser()%>");">
	<div id="headerDivContainer"></div>
		<div id="pageTitle" class="panelHeader" style="text-align: left; margin:15px 150px 25px 0;">Forbidden</div>
		<br/>
		<div id="middle_content_template">
			<h2>We are sorry, but one or more of the requested resources requires permissions you do not have, even when logged in.</h2>
		</div>
	</div>
</body>
</html>

