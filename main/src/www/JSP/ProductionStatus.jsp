
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
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>    
    <link rel="stylesheet" href="style/rte.css" />
    <link rel="stylesheet" href="style/dataTables.css" />
    <style>
        fieldset legend:hover { cursor: pointer; }
        fieldset {
            padding: 5px 10px 5px 10px;
        }
        .collapsed {
            border-width: 1px 0px 0px 0px;
            padding: 5px 12px 0px 12px;
        }
    </style>

    <script src="scripts/jquery/jquery-1.7.2.js"></script>
    <script src="scripts/jquery/jquery.tablesorter.js"></script>
    
    <script src="scripts/jquery/jquery.columnDisplay.js"></script>
    <script src="scripts/jquery/jquery.dataTables.js"></script>

    <script type="text/javascript">
        $(document).ready(function() {
            $('body').width(($('body').width() + $('#statusTable').width() - $('#middle_content_template').width()) + 'px');

            var $table = $('#statusTable').columnDisplay({
                checkBoxContainer: '#statusTableColumnToggler',
                checkBoxDivClass: 'jcvi-cd-cbox-container',
                checkBoxDivDisabledClass: 'jcvi-cd-cbox-container-disabled',
                checkBoxClass: 'jcvi-cd-cbox',
                checkBoxLabelClass: 'jcvi-cd-cbox-label',
                hideCols: [ <s:property value="hiding"/> ]
            });

            $('fieldset').fieldCollapse({
                collapseClass: 'collapsed',
                contentSelector: 'div',
                startCollapsed: true
            });

            $("#statusTable").dataTable({
                "sDom": '<"statusTop"lpf><"statusMain"rt><"statusBottom"i>',
                "aaSorting": [ <s:property value="sorting"/> ],
                "oLanguage": {
                    "oPaginate": {
                        "sNext": " ",
                        "sPrevious": " "
                    }
                }
            });
        });

        function getDisplayedAttributes() {
            document.statusPage.attributesOnScreen.value = displayedAttributes;
        }

        function openNewWindow() {
            window.open("productionStatus.action?projectNames="+$("#projectNames").val()+"&attributes="+$("#attributes").val());
        }
    </script>
</head>


<body>


<s:form id="statusPage" name="statusPage"
        namespace="/"
        action="productionStatus"
        method="post" theme="simple">
    <s:hidden name="projectNames" id="projectNames"/>
    <s:hidden name="attributesOnScreen" />
    <s:hidden name="attributes" id="attributes" />

    <div id="middle_content_template">
        <p>An Excel version of this data is also available for download
            <s:submit type="input" value="here"
                      onclick="document.statusPage.action='productionStatusExcel.action';javascript:getDisplayedAttributes();"/>.
            &nbsp;&nbsp;<input type="button" onclick="javascript:openNewWindow();" value="Full View"/>
        </p>
        <fieldset>
            <legend> Show / Hide Columns </legend>
            <div id="statusTableColumnToggler"></div>
        </fieldset>
        <div style="overflow:auto;" id="statusTableDiv">
            <display:table name="pageElementList" uid="chartRow" id="statusTable" style="float:left;" requestURI="/productionStatus.action">

                <s:iterator value="parameterizedAttributes" var="attrName" status="stat">
                    <display:column title='${attrName}' headerClass="tableHeaderStyle" style="width:15px;">
                        <p><s:property value="#attr.statusTable[#attrName]" escape="false"/></p>
                    </display:column>
                </s:iterator>
            </display:table>

        </div>
    </div>

</s:form>
</body>
</html>
