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
<%@ page language="java" contentType="application/vnd.ms-excel;name='excel', text/html;" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%
    response.setContentType("application/vnd.ms-excel");

    String fileName = request.getParameter("projectNames").length()>30?"sample":request.getParameter("projectNames");
    response.setHeader("Content-Disposition", "attachment;filename="+new String( "JCVI-" + fileName + "-status.xls" ) );
%>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title></title>
        <meta http-equiv="Content-Type" content="text/html" />
        <meta http-equiv="Cache-Control" content="No-Cache" />
        <meta http-equiv="Pragma" content="No-Cache" />
        <style type="text/css">
            .statusList th {font-size:16px;font-family:Dotum;margin:0;background-color:#637396;height:20px;text-align:center;}
            .statusList td {text-align:center;}
            .statusList td p {font-size:16px;font-family:Dotum;margin:0;padding-left:10px;text-align:left;}
        </style>
    </head>

    <body>
        <table cellspacing="0" cellpadding="0" class="statusList" border="1">
            <thead>
                <tr class="tableHeader">
                    <s:iterator value="parameterizedAttributes" >
                        <th><p><s:property /></p></th>
                    </s:iterator>
                </tr>
            </thead>
            <tbody>
                <s:iterator value="pageElementList" var="element" status="elementStatus">
                        <tr>
                            <s:iterator value="parameterizedAttributes" var="attrName">
                                <td><p>
                                    <s:property value="#element[#attrName]" escape="false"/>
                                </p></td>
                            </s:iterator>

                        </tr>
                    </s:iterator>

            </tbody>
        </table>
    </body>
</html>
