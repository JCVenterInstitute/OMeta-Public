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
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <link rel="stylesheet" href="style/dataTables.css" />
    <link rel="stylesheet" href="style/rte.css" />
</head>
<body>
    <s:form id="statusPage" name="statusPage"
            namespace="/"
            action="sampleDetail"
            method="post" theme="simple">
        <s:hidden name="projectName" />
        <s:hidden name="attributesOnScreen" />
        <s:hidden name="attributes" />
        <jsp:include page="TopMenu.jsp"/>
         <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div class="panelHeader" style="margin:0;">Sample Detail</div>
        </div>
        <div id="middle_content_template">
            <h1 class="csc-firstHeader">
                <s:if test="detailMap.Organism==null">
                    <s:property value="sample.name" />
                </s:if>
                <s:else>
                    <s:property value="detailMap.Organism" />
                </s:else>
            </h1>
            <p></p>
            <div style="width:100%;">
                <table class="contenttable tablesorter" id="statusTable">
                    <thead>
                    <tr class="tableHeader">
                        <th style="width:25%;"><p>Event</p></th>
                        <th style="width:12%;"><p>Status</p></th>
                        <th style="width:45%;"><p>Description</p></th>
                        <th style="width:12%;"><p>Date</p></th>
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
                                            <p><s:property value="name" /> - <s:property value="value" escape="false"/></p>
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
        </div>
    </s:form>
    
    <script src="scripts/jquery/jquery.dataTables.js"></script>
    <script>
        $(document).ready(function() {
            $('#statusTable tbody tr:even').addClass('even');
            $('#statusTable tbody tr:odd').addClass('odd');
        })
    </script>
</body>
</html>
