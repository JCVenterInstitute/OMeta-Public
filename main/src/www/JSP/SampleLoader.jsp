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

<%--
  Created by IntelliJ IDEA.
  User: hkim
  Date: 2/10/12
  Time: 8:47 AM
--%>
<!DOCTYPE HTML>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>    
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
</head>

<body>

    <s:form id="sampleLoaderPage" name="sampleLoaderPage"
            namespace="/"
            action="sampleLoader"
            method="post" theme="simple">
        <s:hidden name="jobType" id="jobType"/>
        <s:hidden name="label" id="label"/>
        <s:include value="TopMenu.jsp" />
        <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <table cellpadding="0" cellspacing="0" border="0">
                <tr><td class="panelHeader">Sample Loader</td></tr>
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
                <div style="margin:0 10px 0 0;">
                    <h1 class="csc-firstHeader">Sample Information</h1>
                </div>
                <div id="tableTop">
                    <table>
                        <tr>
                            <td align="right" id="sampleNameLabel">Sample Name</td>
                            <td><s:textfield id="_sampleName" name="loadingSample.sampleName" size="35px"/></td>
                        </tr>
                        <tr><td>&nbsp;</td></tr>
                        <tr>
                            <td align="right">Project</td>
                            <td class="ui-combobox">
                                <s:select id="_projectSelect"
                                          list="projectList" name="loadingSample.projectId" headerKey="0" headerValue=""
                                          listValue="projectName" listKey="projectId" required="true" />
                            </td>
                        </tr>
                        <tr><td>&nbsp;</td></tr>
                        <tr id="parentSelectTr">
                            <td align="right" id="parentSampleLabel">Parent Sample</td>
                            <td class="ui-combobox">
                                <s:select id="_parentSampleSelect" list="#{'0':''}" name="loadingSample.parentSampleId" required="true"/>
                            </td>
                        </tr>
                        <tr><td>&nbsp;</td></tr>
                        <tr>
                            <td align="right">Public</td>
                            <td>
                                <s:select id="_isPublic" list="#{0:'No', 1:'Yes'}" name="loadingSample.isPublic" required="true" />
                            </td>
                        </tr>
                    </table>
                </div>
                <s:div id="submitDiv" cssStyle="margin:15px 10px 5px 0;width:100%;">
                    <input type="button" onclick="javascript:loadSample();" id="sampleLoadButton" value="Load Data"/>
                    <input type="button" onclick="javascript:doClear();" value="Clear" />
                </s:div>
            </div>
        </div>
    </s:form>

    <script>
        var currentAttributeCount = 0,smaOptions;
        $(document).ready(function() {
            var label=$('input:hidden#label').val();
            if(label!==null && label!=='') {
                $.each($('#_projectSelect').find('option'), function(i1,v1) {
                    if(v1.text===paramP) {
                        $('#_projectSelect').val(parseInt(v1.value));
                        projectChanged();
                    }
                });
                labeling(label);
                if(label==='Family')
                    $('div#tableTop table tr#parentSelectTr').hide();
            }
            
            <!-- DROPDOWN MENUS -->
            $('#_projectSelect').combobox();
            $('#_parentSampleSelect').combobox();

            var projectId = <s:if test="projectId == null">0</s:if><s:else><s:property value="projectId"/></s:else>;
            var parentSampleId = <s:if test="sampleId == null">0</s:if><s:else><s:property value="sampleId"/></s:else>;
            if( projectId != null && projectId != 0) {
                $('#_projectSelect').val(projectId);
                projectChanged(projectId);
                if( parentSampleId != null && parentSampleId != 0)
                    $('#_parentSampleSelect').val(parentSampleId);
                $("#sampleLoadButton").attr("disabled", false);
            }

            utils.error.check();
        });

        function labeling(l) {
            $('.panelHeader').html('Load '+l);
            $('.csc-firstHeader').html(l+' Information');
            $('#sampleNameLabel').html(l+' ID');
            if(l!=='Family')
                $('#parentSampleLabel').html(l==='Patient'?'Family ID':'Patient ID');
        }

        function comboBoxChanged(option, id) {
            if(id==='_projectSelect') {
                if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
                    projectChanged(option.value);
                    $('.ui-autocomplete-input').val('');
                } else {
                    $("#_parentSampleSelect").html(vs.empty);
                }
            }
        }

        function projectChanged(projectId) {            
            $("#attributeInputTable tr, #attributeAdditionDiv tr").remove();
            $("#sampleLoadButton").attr("disabled", false);
            currentAttributeCount = 0;
            smaOptions='';
            gethtmlByType("sample", projectId);
            gethtmlByType("MetaAttributes", projectId);
        }

        <!-- Generate html content using Ajax by type -->
        function gethtmlByType(ajaxType, projectId) {
            var label=$('input:hidden#label').val(),
                level=label==='Patient'?1:label==='Sample'?2:0;
            $.ajax({
                url:"sharedAjax.action",
                cache: false,
                async: false,
                data: "type="+ajaxType+"&projectId="+projectId+"&subType=S&sampleLevel="+level,
                success: function(html){
                    if(ajaxType == "sample") {
                        var list = vs.empty;
                        $.each(html.aaData, function(i1,v1) {
                            if(i1!=null && v1!=null) {
                                list += vs.vnoption.replace("$v$",v1.id).replace("$n$",v1.name);
                            }
                        });
                        $("#_parentSampleSelect").html(list);
                    } else if(ajaxType == "MetaAttributes") {
                        var list = '<option value=0>option>';
                        $.each(html, function(i1,v1) {
                            if(v1 != null) {
                                $.each(v1, function(i2,v2) {
                                    if(v2 != null && v2.sample != null) {
                                        $.each(v2.sample, function(i3,v3) {
                                            smaOptions+='<option value="'+v3+'">'+v3+'</option>';
                                        });
                                    }
                                });
                            }
                        });
                        

                    }
                },
                fail: function(html) {
                    alert("Ajax Process has Failed.");
                }
            });
        }

        function addAttribute() {
            var smaSelectBox='<select name="beanList['+currentAttributeCount+'].attributeName">'+smaOptions+'</select>',
                currentAttributesHtml = '<tr><td align="right">Attribute Name:'+smaSelectBox+'</td>'+'<td>&nbsp;&nbsp;</td>'
                    +'<td>Attribute Value:<input type="text" name="beanList['+currentAttributeCount+'].attributeValue" /></td></tr>';
            
            $("#attributeAdditionDiv").append(currentAttributesHtml);
            currentAttributeCount++;
        }

        function loadSample() {
            $("#jobType").val("insert");
            $('form').submit();
        }

        function doClear() {
            $("#_projectSelect").val(0);
            $("#attributeInputTable tr, #attributeAdditionDiv tr").remove();
        }
    </script>
</body>
</html>
