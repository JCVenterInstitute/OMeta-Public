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
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
    <style>
        tr.even { padding: 2px; background-color: #e9e9e9; } 
        tr.odd { padding: 2px; background-color: #f5f5f5; }
        tr.odd td, tr.even td { padding: 6px 8px; margin: 0; vertical-align: top; }
        .chkbox { margin:5px 5px 5px 15px; }
    </style>
</head>

<body>
    <s:form id="eventReportPage" name="eventReportPage"
            namespace="/"
            action="eventReport"
            method="post" theme="simple">
    	<s:include value="TopMenu.jsp" />
    	<div id="HeaderPane" style="margin:15px 0 0 30px;">
            <table cellpadding="0" cellspacing="0" border="0">
                <tr><td class="panelHeader">Event Report</td></tr>
                <tr>
                    <td>
                        <div id="errorMessagesPanel" style="margin-top:15px;"></div>
                        <s:if test="hasActionErrors()">
                            <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
                        </s:if>
                    </td>
                </tr>
            </table>
        </div>
    	<div id="middle_content_template">
    		<div id="statusTableDiv">
    			<div id="tableTop">
                    <table>
                        <tr>
                            <td  align="right">Project</td>
                            <td class="ui-combobox">
                                <s:select id="_projectSelect" list="projectList" name="selectedProjectId" headerKey="0" headerValue="" listValue="projectName" listKey="projectId" required="true"/>
                            </td>
                        </tr>
                        <tr>
                            <td align="right">Date Range</td>
                            <td>
                                <s:textfield id="fromDate" name="fromDate" label="from"/> ~ <s:textfield id="toDate" name = "toDate" label="to"/>
                            </td>
                        </tr>
                    </table>
    			</div>
    			<div style="margin-top:25px;">
    				<h1 class="csc-firstHeader">Attributes</h1>
    			</div>
    			<div id="attributesTableDiv">
    				<table name="attributesTable" id="attributesTable" class="contenttable" style="width:95%;">
    					<tbody id="attributesTableBody">
    					<tr class="even">
    						<td width="20%" style="padding:5px 0 5px 5px;">Project</td>
    						<td style="padding:5px 0 5px 0;" colspan="2" id="projectMetaAttributesTD"></td>
    					</tr>
    					<tr class="odd">
    						<td width="20%" style="padding:5px 0 5px 5px;">Sample</td>
    						<td style="padding:5px 0 5px 0;" colspan="2" id="sampleMetaAttributesTD"></td>
    					</tr>
    					<tr class="even">
    						<td width="20%" style="padding:5px 0 5px 5px;">Event</td>
    						<td style="padding:5px 0 5px 0;" id="eventMetaAttributesTD" ></td>
    					</tr>
    					</tbody>
    				</table>
    			</div>
    			<div id="submitDiv" style="margin:15px 0 0 0;">
    				<input type="button" onclick="javascript:open_status_page();" id="eventReportPageButton" value="Generate Status Page"/>
    				<input type="button" style="margin-left:15px;" onclick="javascript:doClear();" value="Clear" />
    			</div>
    		</div>
    	</div>
    </s:form>
    <script src="scripts/jquery/jquery.dataTables.js"></script>
    <script>
        $(document).ready(function() {
            utils.initDatePicker();
            $( "#_projectSelect" ).combobox();
            utils.error.check();
        });

        var callbacks = {
            meta: function(data) {
                $.each(data, function(_i, _o) {
                    console.log(_o.err);
                    if(_o) {
                        var keys = ['project', 'sample', 'event'];
                        $.each(keys, function(_k_i, _k) {
                            var values = _o[_k], _html='';
                            if(values) {
                                $.each(values, function(_a_i,_a) {
                                    _html += '<input class="chkbox" type="checkbox" name="'+_k+'Attr" value="'+_a+'"/>'
                                        + '<label class="checkboxLabel">'+_a+'</label>';
                                    if(_k_i!=0 && _k_i%4==0) _html += '<br/>';
                                });
                                if(_html.length>0)
                                    _html+='<input class="chkbox" type="checkbox" name="sAll" value="'+_k+'"/>'
                                        +'<label class="checkboxLabel"><b>Select All</b></label>';    
                                }
                                $('#'+_k+'MetaAttributesTD').html(_html);
                        });  
                    }
                });
        
                $('input:checkbox[name=sAll]').live('click', function() {
                    var type=$(this).val(), checked=$(this).is(':checked');
                    $('input:checkbox[name='+type+'Attr]').attr('checked', checked);
                });
            }
        }
        
        function comboBoxChanged(option) {
            if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
                makeAjax("MetaAttributes", option.value, callbacks.meta);
            } else {
                $("#_sampleSelect").html('<option value="0">Select Sample</option>');
                $("#_eventSelect").html('<option value="0">Select Event</option>');
            }    
        }

        function makeAjax(type, projectId, cb) {
            $.ajax({
                url:"sharedAjax.action",
                cache: false,
                async: false,
                data: "type="+type+"&projectId="+projectId+"&sampleId=0&eventId=0&subType=A",
                success: function(data){
                    if(data.err) {
                        utils.error.add(data.err.substring(data.err.indexOf(':')+1));    
                    } else {
                        cb((data.aaData?data.aaData:[]));    
                    }
                }
            });
        }

        function open_status_page() {
            var attributes = "";
            $.each($('input:checkbox'), function() {
                if($(this).is(':checked') && $(this).val()!=='project' && $(this).val()!=='sample' && $(this).val()!=='event')
                    attributes+=$(this).val()+',';
            });
            window.open("productionStatus.action?iss=true&projectNames="+$('#_projectSelect option:selected').text()+"&attributes="+attributes);
        }

        function doClear() {
            $("#_projectSelect").val(0);
            $("#projectMetaAttributesTD").html('');
            $("#sampleMetaAttributesTD").html('');
            $("#eventMetaAttributesTD").html('');
            $("#fromDate_datepicker").val('');
            $("#toDate_datepicker").val('');
        }
    </script>
</body>
</html>

