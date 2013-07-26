
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
<head>
    <link rel="stylesheet" href="style/rte.css" />
    <link rel="stylesheet" href="style/dataTables.css" />
    <link rel="stylesheet" href="style/tableTools.css" />
    <style>
        fieldset legend:hover { cursor: pointer; }
        fieldset { padding: 5px 10px 5px 10px; }
        .collapsed { border-width: 1px 0px 0px 0px; padding: 5px 12px 0px 12px; }
        .headerContainer {width: 100%;}

        .dataTables_length { padding-left: 25px; }
        .DTTT_container { float: left !important; }
    </style>
</head>
<body>
    <s:form id="statusPage" name="statusPage"
            namespace="/"
            action="productionStatus"
            method="post" theme="simple">
        <s:hidden name="projectNames" />
        <s:hidden name="attributesOnScreen" id="attributesOnScreen"/>
        <s:hidden name="attributes" />
        <s:include value="TopMenu.jsp" />
        <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div class="panelHeader" style="margin:0;">Project Status</div>
            <div id="errorMessagesPanel" style="float:left;margin-top:15px;"></div>
            <s:if test="hasActionErrors()">
                <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
            </s:if>
        </div>
        <div id="middle_content_template">
            <p>An Excel version of this data is also available for download
                <s:submit type="input" value="here"
                          onclick="document.statusPage.action='productionStatusExcel.action';javascript:getDisplayedAttributes();"/>.</p>
            <!--<div id="columnsTable"></div>  for column listing-->
            <fieldset>
                <legend> Show / Hide Columns </legend>
                <div id="statusTableColumnToggler"></div>
            </fieldset>
            <div id="statusTableDiv">
                <table id="statusTable" style="float:left;width:100%"></table>
            </div>
        </div>

    </s:form>
    
    <script src="scripts/jquery/jquery.tablesorter.js"></script>
    <script src="scripts/jquery/jquery.columnDisplay.js"></script>
    <script src="scripts/jquery/jquery.dataTables.js"></script>
    <script src="scripts/jquery/jquery.tableTools.js"></script>

    <script type="text/javascript">
        $(document).ready(function() {
            var attrs = '${attributes}', aoColumns=[];
            attrs=attrs.split(',');
            var header='<thead><tr>', hd="<th class='tableHeaderStyle'><p style='color:#FFFFFF;'>$hd$</th>";;
            $.each(attrs, function(i,v) {
                if(v!=='') {
                    aoColumns.push({'mDataProp':v});
                    header+=hd.replace('$hd$', v);
                }
            });
            header+='</tr></thead>';
            $('#statusTable').html(header);

            $("#statusTable").dataTable({
                "sDom": '<"statusTop"Tlf><"statusMain"rt><"statusBottom"ip>',
                "sPaginationType": "full_numbers",
                "bProcessing": true,
                "bServerSide": true,
                "aoColumns": aoColumns,
                "sServerMethod": "POST",
                "sAjaxSource": "productionStatusAjax.action?projectNames=${projectNames}&attributes=${attributes}",
                "aaSorting": [],
                "oTableTools": {
                    "sSwfPath": "media/copy_csv_xls_pdf.swf",
                    "sRowSelect": "multi",
                    "aButtons": [
                        {"sExtends":"csv", "bSelectedOnly":true},
                        {"sExtends":"xls", "bSelectedOnly":true},
                        {"sExtends":"pdf", "bSelectedOnly":true},
                        {"sExtends":"copy", "bSelectedOnly":true},
                        "select_all", "select_none"]

                }
            });
            var $colTable = $('#statusTable').columnDisplay({
                checkBoxContainer: '#statusTableColumnToggler',
                checkBoxDivClass: 'jcvi-cd-cbox-container',
                checkBoxDivDisabledClass: 'jcvi-cd-cbox-container-disabled',
                checkBoxClass: 'jcvi-cd-cbox',
                checkBoxLabelClass: 'jcvi-cd-cbox-label'
            });

            $('fieldset').fieldCollapse({
                collapseClass: 'collapsed',
                contentSelector: 'div',
                startCollapsed: true
            });
            
            utils.error.check();
        });

        function getDisplayedAttributes() {
            $('#attributesOnScreen').val(displayedAttributes);
        }
    </script>
</body>
</html>
