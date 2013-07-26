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
    <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}" />
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <title>O-META Event File Upload</title>    
    <link rel="stylesheet" href="style/rte.css" />
    <link rel="stylesheet" href="style/dataTables.css" />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
</head>

<body>
    <s:form id="uploadLoaderPage" name="uploadLoaderPage"
            namespace="/" theme="simple"
            action="webLoader" method="post"
            enctype="multipart/form-data">
        <s:hidden name="projectName" id="projectName"/>
        <s:hidden name="eventName" id="eventName"/>
    	<s:include value="TopMenu.jsp" />
        <div id="HeaderPane" style="margin:15px 0 0 30px;">
    		<table cellpadding="0" cellspacing="0" border="0">
    			<tr><td class="panelHeader">Event File Upload</td></tr>
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
                            <s:div id="projectDropBox">
                                <td>Project</td>
                                <td style="padding-left:10px" class="ui-combobox"><s:select id="_projectSelect"
                                                  list="projectList" name="projectId" headerKey="0" headerValue=""
                                                  listValue="projectName" listKey="projectId" required="true"
                                    />
                                </td>
                            </s:div>
                        </tr>
                        <tr>
                            <s:div id="eventDropBox">
                                <td>Event</td>
                                <td style="padding-left:10px" class="ui-combobox"><s:select id="_eventSelect" list="#{'0':''}"
                                                name="eventId" required="true" disabled="true"
                                    />
                                </td>
                            </s:div>
                        </tr>
                        <tr><td>&nbsp;</td></tr>
                        <tr>
                            <td>Loader TSV File</td>
                            <td>
                                <s:file name="uploadFile" id="uploadFile" cssStyle="margin:0 0 0 14px;" size="55px" />
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <s:div id="eventTypeDropBox" cssStyle="margin:15px 10px 5px 0;width:100%;">
    			<input type="button" style="width:15%;" value="Upload" disabled="true" id="submitButton" onclick="javascript:doSubmit();"/>
                <!--<s:submit cssStyle="width:15%;" value="Upload" disabled="true" id="submitButton"/> -->
                <input type="button" style="margin-left:15px;" onclick="javascript:doClear();" value="Clear" />
            </s:div>
        </div>

    </s:form>

    <script src="scripts/jquery/jquery.tablesorter.js"></script>    
    <script src="scripts/jquery/jquery.columnDisplay.js"></script>
    <script src="scripts/jquery/jquery.dataTables.js"></script>

    <script>
        function comboBoxChanged(option, id) {
            if(id==='_projectSelect') {
                if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
                    projectChanged(option.value);
                } else {
                    $("#_eventSelect").html(vs.empty);
                }
            } 
        }

        function projectChanged(projectId) {
            getEventType(projectId, 0, 0);
        }

        function getEventType(projectId, sampleId, eventId) {
            $("#_eventSelect").attr("disabled", false);
            gethtmlByType("Event", projectId, sampleId, eventId);
        }

        <!-- Generate html content using Ajax by type -->
        function gethtmlByType(ajaxType, projectId, sampleId, eventId) {
            $.ajax({
                url:"sharedAjax.action",
                cache: false,
                async: false,
                data: "type="+ajaxType+"&projectId="+projectId+"&sampleId="+sampleId+"&eventTypeId="+eventId,
                success: function(html){
                    if(ajaxType == "Sample") {
                        var list = '<option value=0>Select Sample</option>';
                        $.each(html.aaData, function(i1,v1) {
                            if(i1!=null && v1!=null) {
                                list += '<option value="'+v1.id+'">' +v1.name + '</option>';
                            }
                        });

                        if(sampleId == null || sampleId == 0)
                            $("#_sampleSelect").html(list);

                    } else if(ajaxType == "Event") {
                        var list = '<option value=0>Select Event Type</option>';

                        $.each(html, function(i) {
                            if(html[i] != null) {
                                $.each(html[i], function(j) {
                                    if(html[i][j] != null && html[i][j].lookupValueId != null && html[i][j].name != null) {
                                        list += '<option value="'+html[i][j].lookupValueId+'">' +html[i][j].name + '</option>';
                                    }
                                });
                            }
                        });

                        $("#_eventSelect").html(list);
                    }
                },
                fail: function(html) {
                    alert("Ajax Process has Failed.");
                }
            });
        }

        function doClear() {
            $("#evnetName").val(1);
            $("#uploadFile").val('');
            $("#submitButton").attr("disabled", true);
        }

        function doSubmit() {
            var docObj = document.uploadLoaderPage;
            $("#projectName").val($("#_projectSelect option:selected").text());
            $("#eventName").val($("#_eventSelect option:selected").text());
            docObj.submit();
        }

        /* previous version #31968 */
        $(document).ready(function() {
            $('select').combobox();

            var projectId = <s:if test="projectId == null">0</s:if><s:else><s:property value="projectId"/></s:else>;
            var eventId = <s:if test="eventId == null">0</s:if><s:else><s:property value="eventId"/></s:else>;
            if( projectId != null && projectId != 0) {
                $('#_projectSelect').val(projectId);
                projectChanged(projectId);
                if( eventId != null && eventId != 0) {
                    $('#_eventSelect').val(eventId);
                }
            }

            $("#uploadFile").change(function() {
                if($(this).val() != null && $(this).val() != '') {
                    $("#submitButton").attr("disabled", false);
                }
            });

            utils.error.check();
        });
    </script>

</body>
</html>
