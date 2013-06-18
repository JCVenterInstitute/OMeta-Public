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
	<form method="POST" action="j_security_check" id="loginPage" name="loginPage">
		<jsp:include page="TopMenu.jsp" />
		<div id="pageTitle" class="panelHeader">O-META: Ontologies based Matadata Tracking Application</div>
		<div id="middle_content_template" style="overflow:visible;width:auto;height:auto;margin:35px 25px 25px 25px;">
			<div id="loginContents">
				<table>
					<tr class="gappedTr">
						<td style="text-align:right;"><strong>User Name</strong></td>
						<td style="padding-left:25px;"><input type="text" name="j_username" id="usertext"/>
					</tr>
					<tr class="gappedTr">
						<td style="text-align:right;"><strong>Password</strong></td>
						<td style="padding-left:25px;"><input type="password" name="j_password" id="password"/></td>
					</tr>
				</table>
				<div id="button">
					<input type="button" name="Login" id="loginButton" value="Login" onclick="javascript:document.loginPage.submit();" style="float:left;margin:20px 0 0 190px;"/>
				</div>
			</div>
			<div id="contactDiv" style="clear:both;overflow:visible;float:left;margin:10px 20px">
				<div style="margin:5px 0 0 0;">
					or please <input type="button" name="Register" value="Register" onclick="javascript:addActor();"/> to use the system.
				</div>
				<div id="info" style="margin-top:15px;float:left;">
					<p>The latest versions of <strong>Firefox(v.14+), IE(v.9+) or Chrome(v.22+)</strong> are recommended in using O-META.<br/>
					If you experience difficulties with a particular browser, please contact <a href="./help.action">Help</a>.</p>
				</div>
			</div>
		</div>
	</form>

	<script>
		function addActor() {
			document.loginPage.action='addActor.action';
			document.loginPage.submit();
		}

		$(function() {
			$("#password").keyup(function(event){
			    if(event.keyCode == 13){
			        $("#loginButton").click();
			    }
			});
		});
		
	</script>
</body>
</html>

