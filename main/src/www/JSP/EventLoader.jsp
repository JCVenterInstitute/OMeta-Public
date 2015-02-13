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
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
    <jsp:include page="header.jsp" />
    <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/multiple-select.css" type='text/css' media='all' />

    <%--<link rel="stylesheet" href="style/version01.css" />--%>
    <style>
        .loadRadio {
            margin-left: 10px;
            margin-right: 3px;
        }
        #gridBody .ui-autocomplete-input {
            width: 150px;
        }
        .gridIndex {
            max-width: 20px !important;
            min-width: 15px;
            text-align: center;
        }
        .ms-choice {
            line-height: 20px;
        }
        .ms-choice, .ms-choice > div {
            height: 20px;
        }

        /* dropbox */
        #dropzone {
            border-style: dashed;
            border-color: #3276b1;
            width: 500px;
            height: 100px;
        }
        .bar {
            height: 18px;
            background: green;
        }
    </style>
</head>

<body class="smart-style-2">
<div id="container">

    <jsp:include page="top.jsp" />

    <div id="main" class="">
        <div id="inner-content" class="">
            <div id="content" class="container max-container" role="main">
                <div id="ribbon">
                    <ol class="breadcrumb">
                        <li>
                            <a href="/ometa/secureIndex.action">Dashboard</a>
                        </li>
                        <li id="breadcrumb2">Data Submission</li>
                        <li>Submit Data</li>
                    </ol>
                </div>

                <div class="page-header">
                    <h1>Submit Data</h1>
                </div>

                <div id="HeaderPane">
                    <!-- error messages -->
                    <div id="errorMessagesPanel" style="margin-top:15px;margin-bottom: 15px;"></div>
                    <s:if test="hasActionErrors()">
                        <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
                    </s:if>

                    <!-- action messages -->
                    <s:if test="hasActionMessages()">
                        <div class="row" style="margin-top: 15px;margin-bottom: 15px;margin-left: 0px;">
                            <div class="alert_info" onclick="$('.alert_info').remove();">
                                <strong style="color: #ffffff;background-color: #348338;border-color: #348338;padding: 7px;"><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
                            </div>
                        </div>
                    </s:if>

                    <div class="row">
                        <div class="col-lg-11">
                            <p>The DPCC offers multiple interfaces to submit research data. The three Web-based options are interactive in that they offer the opportunity to review and validate your data before submitting it to the DPCC.</p>
                            <p id="toBulkP">If you prefer to simply transmit your data in bulk using one of the standard submission templates, please click <input type="button" onclick="javascript:toBulk();" class="btn-xs btn-primary" value="HERE" />.</p>
                            <p id="toInteractiveP" style="display:none;">Please click <input type="button" onclick="javascript:toInteractive();" class="btn-xs btn-primary" value="HERE" /> to switch to the Interactive Submission.</p>
                        </div>
                    </div>
                    <div class="row" id="loadingImg">
                        <div class="container" style="padding-left:5px;">
                            <img src="images/loading.gif" />
                        </div>
                    </div>
                </div>

                <div id="mainContent" style="">

                    <!-- file drop box -->
                    <div id="dropBoxDiv" style="display:none;float:left;width:100%;">
                        <div id="tableTop">
                            <div class="middle-header row col-md-12"><h2>Bulk Submission</h2></div>
                            <div class="row row_spacer">
                                <div class="panel-body">
                                    <div class="form-group">
                                        <div class="row row_spacer" id="projectSelectRow">
                                            <div class="col-lg-2 col-md-4"><strong>Select file</strong></div>
                                            <div class="col-lg-10 col-md-8">
                                                <input id="uploadFile" type="file" name="upload" data-url="fileUploadAjax.action">
                                            </div>
                                        </div>
                                    </div>
                                    <p>Drag and Drop file in box to upload (Max file size is 2GB) </p>
                                    <div id="dropzone" class="well">Drop files here</div>
                                    <div class="row row_spacer">
                                        <div id="progress">
                                            <div class="bar" style="width: 0%;"></div>
                                        </div>
                                    </div>
                                    <div class="row row_spacer">
                                        <div id="files" class="files" style="padding-left:20px;"></div>
                                    </div>
                                    <p>
                                        Bulk data submissions must use one of the standard DPCC data submission templates available <a href="dpcc_help.action">here</a>. Please select your submission file using the “Choose File” button or drag it directly to the upload area.
                                        </br><br>
                                        The data will be processed by the DPCC and upon completion you will receive an email notification containing a submission summary. The email notification may also include a list of data processing errors and instructions for re-submission.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>



                    <!-- regular interactive event loader -->
                    <s:form id="eventLoaderPage" name="eventLoaderPage" namespace="/" action="eventLoader" method="post" theme="simple" enctype="multipart/form-data">
                        <s:hidden name="jobType" id="jobType"/>
                        <s:hidden name="status" id="status"/>
                        <s:hidden name="label" id="label"/>
                        <s:hidden name="filter" id="filter"/>
                        <s:hidden name="eventName" id="eventName" />
                        <s:hidden name="projectName" id="projectName" />
                        <s:hidden name="sampleName" id="sampleName" />
                        <div id="interactiveDiv" style="float:left;width:100%;">
                            <div id="statusTableDiv">
                                <div class="row"></div>
                                <div id="tableTop">
                                    <div class="middle-header row col-md-12"><h2>Interactive Submission</h2></div>
                                    <div class="row col-md-12">
                                        <table id="interactive-submission-table" style="width: 70%">
                                            <tr>
                                                <td>Submit Data For</td>
                                                <td><div class="col-lg-11 col-md-10">
                                                    <input type="radio" name="loadType" class="loadRadio" value="form" id="r_sw" />
                                                    <label for="r_sw">Single Sample</label>
                                                    <input type="radio" name="loadType" class="loadRadio" value="grid" id="r_mw" style="margin-left:15px;"/>
                                                    <label for="r_mw">Multiple Samples (Web Form)</label>
                                                    <input type="radio" name="loadType" class="loadRadio" value="file" id="r_mf" style="margin-left:15px;"/>
                                                    <label for="r_mf">Multiple Samples (Excel Template)</label>
                                                </div></td>
                                                <td></td>
                                            </tr>
                                            <tr>
                                                <td>Center Project</td>
                                                <td><div class="col-lg-11 col-md-11 combobox">
                                                    <s:select label="Project" id="_projectSelect" cssStyle="width:150px;margin:0 5 0 10;"
                                                              list="projectList" name="projectId" headerKey="0" headerValue="Select by Center Project ID"
                                                              listValue="projectName" listKey="projectId" required="true"/>
                                                </div></td>
                                                <td><button type="button" class="btn btn-xs btn-info" id="projectPopupBtn" onclick="button.projectPopup();">Display Project Details</button></td>
                                            </tr>
                                            <tr>
                                                <td>Data Template</td>
                                                <td><div class="col-md-11 combobox">
                                                    <s:select id="_eventSelect" list="#{0:'Select by Data Type Template'}" name="eventId" required="true" disabled="true"/>
                                                </div></td>
                                                <td></td>
                                            </tr>
                                            <tr>
                                                <td>Sample</td>
                                                <td><div class="col-md-11 combobox">
                                                    <s:select id="_sampleSelect" cssStyle="margin:0 5 0 10;" list="#{0:'Select by Sample'}" name="sampleName"  required="true"/>
                                                </div></td>
                                                <td></td>
                                            </tr>
                                        </table>
                                    </div>
                                </div>
                                <div class="row"></div>
                                <div id="projectDetailInputDiv" style="display:none;margin-top: 25px;">
                                    <div class="middle-header">
                                        <h4>Project Information</h4>
                                    </div>
                                    <div id="projectDetailSubDiv">
                                        <div class="row row_spacer">
                                            <div class="col-md-1">Project Name</div>
                                            <div class="col-md-11">
                                                <input type="text" id="_projectName" name="loadingProject.projectName" size="33px"/>
                                                <hidden name="loadingProject.isPublic" value="1" />
                                            </div>
                                        </div>
                                        <!-- <div class="row row_spacer">
                          <div class="col-md-1">Public</div>
                          <div class="col-md-11">
                            <s:select id="_isProjectPublic" list="#{0:'No', 1:'Yes'}" name="loadingProject.isPublic" required="true" />
                          </div>
                        </div> -->
                                    </div>
                                </div>

                                <div id="sampleDetailInputDiv" style="display:none;">
                                    <div style="margin:25px 10px 0 0;">
                                        <h1 class="csc-firstHeader middle-header">Sample Information</h1>
                                    </div>
                                    <div id="sampleDetailSubDiv">
                                        <div class="row row_spacer">
                                            <div class="col-md-1">Sample Name</div>
                                            <div class="col-md-11">
                                                <input type="text" id="_sampleName" name="loadingSample.sampleName" size="33px"/>
                                            </div>
                                        </div>
                                        <div class="row row_spacer">
                                            <div class="col-md-1">Parent Sample</div>
                                            <div class="col-md-11 combobox">
                                                <s:select id="_parentSampleSelect" list="#{'0':''}" name="loadingSample.parentSampleName" required="true"/>
                                            </div>
                                        </div>
                                        <div class="row row_spacer">
                                            <div class="col-md-1">Public</div>
                                            <div class="col-md-11">
                                                <s:select id="_isSamplePublic" list="#{0:'No', 1:'Yes'}" name="loadingSample.isPublic" required="true" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="dataSubmissionScope">
                                <div class="row row_spacer">
                                    <div class="col-lg-3 col-md-4 middle-header">
                                        <h3>Data Submission</h3>
                                    </div>
                                    <div style="font-size:0.9em;padding-top:30px;" class="col-lg-9 col-md-8">
                                        [<img style="vertical-align:bottom;" src="images/icon/info_r.png"/>-Information, <img style="vertical-align:bottom;" src="images/icon/req_red.gif">-Required]
                                    </div>
                                </div>
                                <div id="attributeInputDiv" style="clear:both;display:none;">
                                    <s:if test="beanList != null && beanList.size() > 0">
                                        <table>
                                            <s:iterator value="beanList" var="attrName" status="stat">
                                                <tr class="gappedTr">
                                                    <s:hidden name="beanList[%{#stat.index}].projectName" />
                                                    <s:hidden name="beanList[%{#stat.index}].sampleName" />
                                                    <s:hidden name="beanList[%{#stat.index}].attributeName" />
                                                    <td align="right"><s:property value="attributeName"/></td>
                                                    <td><s:textfield name="beanList[%{#stat.index}].attributeValue"/></td>
                                                </tr>
                                            </s:iterator>
                                        </table>
                                    </s:if>
                                </div>
                                <div id="gridInputDiv" style="margin:25px 10px 0 0 ;overflow-x: auto;display:none;">
                                    <table name="eventTable" id="eventTable" class="contenttable">
                                        <thead id="gridHeader"></thead>
                                        <tbody id="gridBody"></tbody>
                                    </table>
                                </div>
                                <div id="fileInputDiv" style="margin:25px 10px 0 0 ;display:none;">
                                    <table>
                                        <tr>
                                            <td>Load CSV File</td>
                                            <td>
                                                <s:file name="dataTemplate" id="dataTemplate" cssStyle="margin:0 0 0 14px;" size="75px" />
                                            </td>
                                        </tr>
                                    </table>
                                </div>

                                <div id="submitDiv" style="margin:15px 10px 5px 0;width:100%;">
                                    <input type="button" class="btn btn-info" onclick="javascript:button.submit('save');" id="saveButton" value="Save Progress" disabled="true" title="Saves the data currently entered in the form to the DPCC database. This does not submit data to the DPCC but allows the user to complete the data submission task at a later time. A temporary submission ID will be generated for later retrieval."/>
                                    <input type="button" class="btn btn-primary" onclick="javascript:button.submit('validate');" id="validateButton" value="Validate Submission" disabled="true" title="Performs validation of the data currently entered in the form and ensures compliance with the CEIRS data standards. Returns a list of validation errors encountered, if any. This does not submit the data to the DPCC."/>
                                    <input type="button" class="btn btn-success" onclick="javascript:button.submit('submit');" id="submitButton" value="Submit to OMETA" disabled="true" title="Submits the data entered in the form to the DPCC validation process. If errors are encountered (missing required fields, data format issues, etc), the page will highlights errors on the form and give users an opportunity to correct errors. This is the same process as the one users can run manually from the Validate Submission button. If no error, the data will be submitted to DPCC and a submission receipt sent to the user."/>
                                    <input type="button" class="btn btn-info" onclick="javascript:button.add_event();" id="gridAddLineButton" value="Add Row" style="display:none;"/>
                                    <!-- <input type="button" class="btn btn-info" onclick="javascript:button.template();" id="templateButton" value="Download Template"/> -->
                                    <!--<input type="button" class="btn btn-info" onclick="javascript:return;" id="exportButton" value="Export to .csv Template"/>  -->
                                    <!-- <input type="button" onclick="javascript:button.clear_form();" value="Clear Form" /> -->
                                </div>
                            </div>
                        </div>

                    </s:form>
                </div>

            </div>
        </div>
    </div>
    <div class="row"></div>
</div>

<jsp:include page="../html/footer.html" />

<script src="scripts/jquery/jquery.multiple.select.js"></script>
<script src="scripts/jquery/jquery.ui.widget.js"></script>
<script src="scripts/jquery/jquery.iframe-transport.js"></script>
<script src="scripts/jquery/jquery.fileupload.js"></script>

<script>
    var g_eventAttributes = [];
    var g_gridLineCount=0;
    var g_avDic= {};
    var g_sampleIds;
    var g_transferType;
    var avHtml;
    var sample_options;

    $(document).ready(function() {
        $('#dataSubmissionScope').hide();
    });

    $(function() {
        $('#loadingImg').show();

        $('#dropBoxDiv').hide();
        $('select[id$="Select"]').combobox();

        //retrieve existing values for preload
        var oldProjectId = '${projectId}', oldJobType = '${jobType}';

        //load type radio button change event
        $('input[name="loadType"]').change(function() {
            $('div[id$="InputDiv"], #gridAddLineButton, #sampleSelectRow').hide();
            utils.preSelect('_sampleSelect', '');
            var _selectedType = $(this).val();
            if(_selectedType === 'grid') {
                $('#gridInputDiv, #gridAddLineButton').show();
                _utils.addGridRows(utils.getProjectName(), utils.getEventName());
            } else if(_selectedType==='file') {
                $('#fileInputDiv').show();
            } else {
                $('#attributeInputDiv, #sampleSelectRow').show();
                _utils.showPS();
            }
        });

        //preselect load type radio button
        var rtnJobType = (oldJobType===''||oldJobType==='form'||oldJobType==='template'?'form':oldJobType);
        $('input[name="loadType"][value='+rtnJobType+']').attr('checked', true);
        $('input[name="loadType"]:checked').change();

        //empty project select box
        $('#_projectSelect ~ input').click(function() {
            var $projectSel = $('#_projectSelect');
            if($projectSel.val() === '0') {
                $projectSel.val('0');
                $(this).val('');
            }
        });

        //preload project and event type
        if(oldProjectId) {
            changes.project(oldProjectId);

            var oldSampleName = '${sampleName}';
            var oldEventName = '${eventName}';
            var ids = '${ids}';
            var transferType = '${label}';

            if(ids !== '' && ids.indexOf(',') > 0) { //gets sample IDs from Event Loader
                g_sampleIds = ids.substr(0, ids.length - 1);
            }
            if(transferType !== '') {
                g_transferType = transferType;
            }
            if(oldEventName !== '') {
                utils.preSelect("_eventSelect", oldEventName);
                changes.event(oldEventName, $('#_eventSelect').val());
            }
            if(oldSampleName !== '') {
                utils.preSelect("_sampleSelect", oldSampleName);
            }
        }

        //keep any existing data
        <s:set name="oldGridList" value="gridList" />
        <s:set name="oldBeanList" value="beanList" />

        <s:if test="%{#oldGridList != null && #oldGridList.size() > 0}">
        //remove any existing dom elements
        g_gridLineCount = 0;
        $('#gridBody').html('');
        $('[name^="gridList"]').remove();
        <s:iterator value="#oldGridList" var="gbean" status="gstat">
        var gridLine={}, beans=[];
        <s:iterator value="beanList" var="fbean" status="fstat">
        beans.push(["${fbean.attributeName}", "${attributeValue}"]);
        </s:iterator>
        gridLine['pn']="${gbean.projectName}";
        gridLine['pp']="${gbean.projectPublic}";
        gridLine['sn']="${gbean.sampleName}";
        gridLine['psn']="${gbean.parentSampleName}";
        gridLine['sp']="${gbean.samplePublic}";
        gridLine['beans']=beans;
        button.add_event(null,null,gridLine);
        </s:iterator>
        _utils.addGridRows(null,oldEventName);
        </s:if>
        <s:elseif test="%{#oldBeanList != null && #oldBeanList.size() >0}">
        //preload form view

        //remove any existing dom elements
        //$('[name^="beanList"]').remove();
        <s:iterator value="#oldBeanList" var="bean" status="bstat">
        var currAttributeName = '${bean.attributeName}'.replace(/ /g,"_");
        var currAttributeValue = "${bean.attributeValue}";
        $("[id*='_" + currAttributeName + "_f']:not(:file)").val(currAttributeValue);
        $("[id*='file_" + currAttributeName + "_f']").after("<strong>" + currAttributeValue.substring(currAttributeValue.indexOf("_") + 1) + "</strong>");
        </s:iterator>
        <s:set name="oldLoadingSample" value="loadingSample" />
        <s:if test="%{#oldLoadingSample != null && #oldLoadingSample.getSampleName() != null}">
        //$('#_sampleName').val('<s:property value="#oldLoadingSample.sampleName"/>');
        // utils.preSelect('_parentSampleSelect', '<s:property value="#oldLoadingSample.parentSampleName"/>');
        // utils.preSelect('_isSamplePublic', '<s:property value="#oldLoadingSample.isPublic"/>');
        </s:if>
        <s:else>
        <s:set name="oldLoadingProject" value="loadingProject" />
        <s:if test="%{#oldLoadingProject != null && #oldLoadingProject.getProjectName() != null}">
        $('#_projectName').val('<s:property value="#oldLoadingProject.projectName"/>');
        //utils.preSelect('_isProjectPublic', '<s:property value="#oldLoadingProject.isPublic"/>');
        </s:if>
        </s:else>
        </s:elseif>

        utils.error.check();

        //handle Create Project
        var filter = '${filter}';
        if(filter === 'pr') { //project registration
            $('#projectSelectRow').hide();
            $('#_eventSelect').prop('disabled', true);
            $('#pageTitle').html('Project Registration');
            $('#saveButton, #validateButton').hide(); //hide buttons
            $('input:radio[id^="r_"]').each(function(i,v) { //change view types to include project
                var $labelNode = $('label[for="' + $(v).attr('id') + '"]');
                $labelNode.html($labelNode.html().replace('Sample', 'Project'));
            })
            $('#breadcrumb2').text('Admin');
        } else if(filter === 'su') { //edit data redirected from search and edit page
            $('#pageTitle').html('Edit Data');
            $('#eventTitle').html('Edit Data For');
        }

        $('#loadingImg').hide();
    });
</script>
<script src="scripts/page/event.loader.js"></script>
</body>
</html>