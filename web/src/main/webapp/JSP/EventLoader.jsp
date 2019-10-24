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
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/multiple-select.css" type='text/css' media='all' />

  <style>
    .gridIndex {
      max-width: 20px !important;
      min-width: 15px;
      text-align: center;
    }

    /* dropbox */
    .dropzone, #dropzone {
      border-style: dashed;
      border-color: #3276b1;
      width: 500px;
      height: 100px;
    }
    .bar {
      height: 18px;
      background: green;
    }

    .scrollButton{padding:10px;text-align:center;font-weight: bold;color: #FFFAFA;text-decoration: none;position:fixed;right:40px;background: rgb(0, 129, 179);display: none;}

    #autofill-control:hover:after{ background: #333; background: rgba(0,0,0,.8);
      border-radius: 5px; bottom: 0px; color: #fff; content: attr(data-tooltip);
      left: 140%; padding: 5px 15px; position: absolute; z-index: 98; width: auto; display: inline-table; }
    #autofill-control:hover:before{border: solid; border-color: transparent #333;border-width: 6px 6px 6px 0px;
      bottom: 8px; content: ""; left: 125%; position: absolute; z-index: 99;}

    .input-group[class*="col-"], .col-md-6 {
      padding-left: 0;
    }
  </style>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <a href="#" class="scrollButton scrollToTop" style="top:75px;"><i class="glyphicon glyphicon-chevron-up"></i></a>
    <a href="#" class="scrollButton scrollToBottom" style="top:125px;"><i class="glyphicon glyphicon-chevron-down"></i></a>
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
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
                <strong id="action_message" style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
              </div>
            </div>
          </s:if>
        </div>

        <!-- Modal -->
        <div class="modal fade" id="project-details" role="dialog">
          <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content" >
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa-angle-right"></i> <strong></strong></h4>
              </div>
              <div class="modal-body">
                <table id="project-detail-table" class="table table-bordered table-striped table-responsive table-hover table-condensed"></table>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>

          </div>
        </div>

        <div class="modal fade" id="download-template" role="dialog">
          <div class="modal-dialog" style="max-width:450px;">

            <!-- Modal content-->
            <div class="modal-content" >
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa-angle-right"></i> <strong>Select Template Format</strong></h4>
              </div>
              <div class="modal-body">
                <div style="display: flex;align-items: center;">
                  <label class="col-sm-4 control-label"><strong>Format</strong></label>
                  <div class="col-sm-8 input-group">
                    <select name="jobType" id="templateJobType" class="form-control"><option value="template_c">CSV</option></select>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="_utils.downloadTemplate();">Download</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>

          </div>
        </div>

        <div id="popupLayerScreenLocker" style="position: fixed; left: 0; top: 0; opacity: 0.5; height: 100%; width: 100%; z-index: 1000; display: none; background: rgb(0, 0, 0);"><!-- --></div>
        <div id="processingDiv" class="show_processing" style="display: none;position: fixed;">Processing your request. Please wait...</div>

        <div id="mainContent" style="">
          <!-- regular interactive event loader -->
          <s:form id="eventLoaderPage" name="eventLoaderPage" namespace="/" action="eventLoader" method="post" theme="simple" enctype="multipart/form-data">
            <s:hidden name="jobType" id="jobType"/>
            <s:hidden name="status" id="status"/>
            <s:hidden name="label" id="label"/>
            <s:hidden name="filter" id="filter"/>
            <s:hidden name="eventName" id="eventName" />
            <s:hidden name="projectName" id="projectName" />
            <s:hidden name="sampleName" id="sampleName" />
            <input type="hidden" value="<s:property value="%{gridList.size}"/>" id="gridListSize" />
            <div id="interactiveDiv">
              <div id="statusTableDiv">
                <div id="interactive-submission-group" class="form-group">
                  <div class="btn-group btn-group-justified" data-toggle="buttons">
                    <label class="btn btn-default active"><input type="radio" name="loadType" value="form">Single Record</label>
                    <label class="btn btn-default"><input type="radio" name="loadType" value="grid">Multiple Records (Web Form)</label>
                    <label class="btn btn-default"><input type="radio" name="loadType" value="file">Multiple Records (Excel Template)</label>
                    <label class="btn btn-default"><input type="radio" name="loadType" value="bulk">Bulk Submission</label>
                  </div>
                </div>
                <div class="row form-group">
                  <div class="col-sm-2">
                    <label class="control-label">Project Name</label>
                  </div>
                  <div class="col-sm-4">
                    <div class="input-group">
                      <s:select label="Project" id="_projectSelect" cssClass="form-control"
                              list="projectList" name="projectId" headerKey="0" headerValue="Select by Project Name"
                              listValue="projectName" listKey="projectId" required="true"/>
                    </div>
                  </div>
                  <div class="col-sm-4">
                    <button type="button" class="btn btn-primary" id="projectPopupBtn" onclick="button.projectPopup();">Display Project Details</button>
                  </div>
                </div>
                <div class="row form-group">
                  <div class="col-sm-2">
                    <label class="control-label">Event Name</label>
                  </div>
                  <div class="col-sm-4">
                    <div class="input-group">
                      <s:select id="_eventSelect" list="#{0:'Select by Event Name'}" name="eventId" required="true"/>
                    </div>
                  </div>
                </div>
                <div id="form-sample-name" class="row form-group" style="display: none;">
                  <div class="col-sm-2">
                    <label class="control-label">ID</label>
                  </div>
                  <div class="col-sm-4">
                    <div class="input-group">
                      <s:textfield id="sampleSelect" placeholder="Select by ID" name="sampleName"
                                   required="true" cssClass="form-control sample-element" disabled="true"/>
                      <span class="input-group-btn">
                          <button type="button" class="btn btn-primary sample-element" id="searchSample" onclick="searchSamples(this.id);" disabled="true">
                            <span class="glyphicon glyphicon-search" aria-hidden="true"></span>
                          </button>
                        </span>
                    </div>
                  </div>
                </div>
                <div id="projectDetailInputDiv" style="display:none;">
                  <div class="middle-header">
                    <h4>Project Information</h4>
                  </div>
                  <div id="projectDetailSubDiv">
                    <div class="row form-group">
                      <div class="col-sm-2">Project Name</div>
                      <div class="col-sm-4">
                        <input type="text" id="_projectName" name="loadingProject.projectName" class="form-control"/>
                      </div>
                    </div>
                    <div class="row form-group">
                      <div class="col-sm-2">Public</div>
                      <div class="col-sm-2">
                        <div class="input-group">
                          <s:select id="_isProjectPublic" list="#{0:'No', 1:'Yes'}" name="loadingProject.isPublic" required="true" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div id="sampleDetailInputDiv" style="display:none;">
                  <div class="middle-header">
                    <h4>Record Information</h4>
                  </div>
                  <div id="sampleDetailSubDiv">
                    <div class="row form-group">
                      <div class="col-sm-2">ID</div>
                      <div class="col-sm-4">
                        <input type="text" id="_sampleName" name="loadingSample.sampleName" class="form-control"/>
                      </div>
                    </div>
                    <div class="row form-group" id="parentSampleRow" style="display: none;">
                      <div class="col-sm-2">Parent ID</div>
                      <div class="col-sm-4">
                        <div class="input-group">
                          <s:textfield id="parentSelect"  name="loadingSample.parentSampleName"  required="true" cssClass="form-control"/>
                          <span class="input-group-btn" id="basic-addon2">
                            <button type="button" class="btn btn-primary" id="searchParentSample" onclick="searchSamples(this.id);">
                              <span class="glyphicon glyphicon-search" aria-hidden="true"></span>
                            </button>
                          </span>
                        </div>
                      </div>
                    </div>
                    <div class="row form-group" id="publicSampleRow" style="display: none;">
                      <div class="col-sm-2">Public</div>
                      <div class="col-sm-2">
                        <div class="input-group">
                          <s:select id="_isSamplePublic" list="#{0:'No', 1:'Yes'}" name="loadingSample.isPublic" required="true" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div id="dataSubmissionScope" style="padding-top: 8px;border-top: 1px solid #eeeeee;">
                <div class="row row_spacer">
                  <div class="col-lg-9 col-md-8">
                    <input type="image" id="autofill-control" src="images/autofill_icon.png" onclick="triggerAutofill();return false;" data-toggle="tooltip" data-html="true"
                           data-placement="top" data-original-title="Toggle Autofill" style="float: left;margin-right: 10px;margin-top: -3px;"/>
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
                <div id="gridInputDiv" style="margin: 5px 0px 0px 0px; overflow-x: auto;display:none;">
                  <table name="eventTable" id="eventTable" class="table table-bordered table-striped table-hover">
                    <thead id="gridHeader"></thead>
                    <tbody id="gridBody"></tbody>
                  </table>
                </div>
                <nav id="sample-pagination-nav" style="display: none;">
                  <ul class="pagination">
                  </ul>
                  <div class="row" id="pagination-loadingImg" style="display: none;margin-top: -50px;">
                    <div class="container" style="padding-left:5px;">
                      <img src="images/loading.gif" style="width: 25px;"/>
                    </div>
                  </div>
                  <div id="pagination-warning" style="padding-top: 20px;display: none;">
                    <label style="color: rgb(169, 32, 32);">You will lose the updated data if you don't submit before pulling other records.</label>
                  </div>
                </nav>
                <div id="confirmDiv"></div>
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

                <div id="submitDiv" style="margin:15px 10px 5px 0;width:100%;padding-top: 15px;border-top: 1px solid #eeeeee;">
                  <input type="button" class="btn btn-primary" onclick="javascript:button.submit('submit');" id="submitButton" value="Submit to OMETA" disabled="true"/>
                  <input type="button" class="btn btn-default" onclick="javascript:button.add_event();" id="gridAddLineButton" value="Add Row" style="display:none;"/>
                  <input type="button" class="btn btn-default" onclick="javascript:button.remove_event();" id="gridRemoveLineButton" value="Remove Row" style="display:none;"/>
                  <input type="button" class="btn btn-default" onclick="javascript:button.template();" id="templateButton" value="Download Template"/>
                  <input type="button" class="btn btn-default" onclick="javascript:button.exportSample();" id="exportButton" value="Export Record(s)" style="display:none;"/>
                  <input type="button" class="btn btn-default" onclick="javascript:button.clear_form();" value="Clear" />
                </div>
              </div>
            </div>
          </s:form>
          <!-- file drop box -->
          <div id="dropBoxDiv" style="display:none;float:left;margin-top: 30px;">
            <div id="tableTop">
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
                  <div class="row row_spacer fileupload-buttonbar">
                    <input type="button" class="btn btn-primary start" id="uploadFilesBtn" style="margin-left:10px;" value="Submit to OMETA"/>
                  </div>
                  <div class="row row_spacer">
                    <div id="progress" style="margin: 10px;">
                      <div class="bar" style="width: 0%;"></div>
                    </div>
                  </div>
                  <div class="row row_spacer">
                    <div id="files" class="files" style="padding-left:20px;"></div>
                  </div>
                  <p>
                    <%--Bulk data submissions must use one of the standard OMETA data submission templates available <a href="dpcc_help.action">here</a>. --%>Please select your submission file using the “Choose File” button or drag it directly to the upload area.
                    </br><br>
                    The data will be processed by the OMETA and upon completion you will receive an email notification containing a submission summary. The email notification may also include a list of data processing errors and instructions for re-submission.
                  </p>
                </div>
              </div>
            </div>
          </div>
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
  var g_sampleArrIndex = 0;
  var g_transferType;
  var avHtml;
  var sample_options;

  $( "#autofill-control" ).tooltip({
    container: 'body',
    show: {
      effect: "slideDown",
      delay: 250
    }
  });

  function triggerAutofill(){
    $("#autofill-option, #gridBody tr:first, #autofill-type-button").toggle();
  }

  $(document).ready(function() {
    $('.navbar-nav li').removeClass('active');
    if(filter !== 'pr') $('.navbar-nav > li:nth-child(2)').addClass('active');

    $('#dataSubmissionScope').hide();

    $('input[name="submissionType"]').change(function() {
      var _selectedType = $(this).val();
      if(_selectedType === 'interactive') {
        toInteractive();
      } else {
        toBulk();
      }
    });
  });

  $(function() {
    utils.processing(true);

    $('#dropBoxDiv').hide();
    $('select[id$="Select"]').combobox();

    //retrieve existing values for preload
    var oldProjectId = '${projectId}', oldJobType = '${jobType}';
    var dataSubmissionDisplay = false;

    //load type radio button change event
    $('input[name="loadType"]').change(function() {
      $('div[id$="InputDiv"], #gridAddLineButton, #gridRemoveLineButton, #dropBoxDiv, #toInteractiveP, #confirmDiv, #autofill-control').hide();
      if(dataSubmissionDisplay) $('#dataSubmissionScope').show();
      $('div.row.form-group').show();
      utils.preSelect('_sampleSelect', '');
      var _selectedType = $(this).val();
      if(_selectedType === 'grid') {
        $('#gridInputDiv, #gridAddLineButton, #gridRemoveLineButton, #confirmDiv, #autofill-control').show();
        $('#sampleSelect, #searchSample').prop("disabled", true);$('#form-sample-name').hide();
        _utils.addGridRows(utils.getProjectName(), utils.getEventName());
        $("#autofill-option").width($('thead#gridHeader').width() + 70);
      } else if(_selectedType==='file') {
        $('#fileInputDiv').show();
        $('#sampleSelect, #searchSample').prop("disabled", true);$('#form-sample-name').hide();
      } else if(_selectedType==='bulk') {
        if($('#dataSubmissionScope').css('display') != 'none') dataSubmissionDisplay = true;
        else dataSubmissionDisplay = false;
        $('div.row.form-group, #dataSubmissionScope').hide();
        toBulk();
      } else {
        $('#attributeInputDiv').show();
        if (utils.checkSR($("#_eventSelect").val()) || $("#_eventSelect option:selected").text().toLowerCase().indexOf('project') > -1) {
          $('#sampleSelect, #searchSample').prop("disabled", true);
          $('#form-sample-name').hide();
        } else {
          $('#sampleSelect, #searchSample').prop("disabled", false);$('#form-sample-name').show();
        }
        _utils.showPS();
      }
    });
    //preselect load type radio button
    var rtnJobType = (oldJobType===''||oldJobType==='form'||oldJobType==='template'?'form':oldJobType);
    $('input[name="loadType"][value='+rtnJobType+']').attr('checked', true);
    $('input[name="loadType"][value='+rtnJobType+']').parent().click();
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
      oldEventName = '${eventName}';
      var ids = '${ids}';
      var transferType = '${label}';
      g_sampleArrIndex = '${sampleArrIndex}';

      if(g_sampleArrIndex == ''){
        g_sampleArrIndex = 0;
      }
      if(ids !== '' && ids.indexOf(',') > 0) { //gets sample IDs from Event Loader
        if(ids.slice(-1) == ',') g_sampleIds = ids.substr(0, ids.length - 1);
        else g_sampleIds = ids;
      }
      if(transferType !== '') {
        g_transferType = transferType;
      }
      if(oldEventName !== '') {
          utils.processing(true);
          utils.preSelect("_eventSelect", oldEventName);
          changes.event(oldEventName, $('#_eventSelect').val());
      }
      if(oldSampleName !== '') {
        utils.preSelect("_sampleSelect", oldSampleName);
        $('input[name="sampleName"]').val(oldSampleName);
      }
    }

    //keep any existing data
    <s:set var="oldGridList" value="gridList" />
    <s:set var="oldBeanList" value="beanList" />

    <s:if test="%{#oldGridList != null && #oldGridList.size() > 0}">
    //remove any existing dom elements
    g_gridLineCount = 0;
    var $autofillRow = $("#gridBody tr:first");
    $('#gridBody').html('');
    $('#gridBody').append($autofillRow);
    $('[name^="gridList"]').remove();

    <s:iterator value="#oldGridList" var="gbean" status="gstat">
    var gridLine={}, beans=[], uploadedFilePaths=[];

    <s:iterator value="beanList" var="fbean" status="fstat">
      beans.push(["${fbean.attributeName}", "${attributeValue}"]);
      var path = [];
      <s:iterator value="#fbean.uploadFilePath" var="path">
        path.push("${path}");
      </s:iterator>
      uploadedFilePaths.push(["${fbean.attributeName}", path]);
    </s:iterator>

    gridLine['pn']="${gbean.projectName}";
    gridLine['pp']="${gbean.projectPublic}";
    gridLine['sn']="${gbean.sampleName}";
    gridLine['psn']="${gbean.parentSampleName}";
    gridLine['sp']="${gbean.samplePublic}";
    gridLine['beans']=beans;
    gridLine['filePaths']=uploadedFilePaths;

    button.add_event(null,null,gridLine);
    </s:iterator>

    _utils.addGridRows(null,oldEventName);
    </s:if>
    <s:elseif test="%{#oldBeanList != null && #oldBeanList.size() >0}">
    //preload form view
    if(oldEventName && oldEventName.indexOf("Registration") === -1) {
      $('#sampleSelect, #searchSample').prop("disabled", false);$('#form-sample-name').show(); //show sample name
    }

    //remove any existing dom elements
    //$('[name^="beanList"]').remove();
    <s:iterator value="#oldBeanList" var="bean" status="bstat">
    var currAttributeName = "${bean.attributeName}".replace(/ /g,"_").replace("'", "''");;
    var currAttributeValue = "${bean.attributeValue}";
    $("[id*='_" + currAttributeName + "_f']:not(:file):not(:button)").val(currAttributeValue);
    //$("[id*='file_" + currAttributeName + "_f']").after("<strong>" + currAttributeValue.substring(currAttributeValue.indexOf("_") + 1) + "</strong>");

    if ( $("[id*='File-" + currAttributeName + "_f']").length ) {
      var id = $("[id*='File-" + currAttributeName + "_f']").attr('id');
      id = id.substring(id.indexOf("-") + 1);
      var name = $("#uploadFile-" + id).attr('name');
      name = name.substring(0, name.lastIndexOf('.'));
      var $files = $('#files-' + id);

      //Attached Files
      var uploadFilePath = "${bean.uploadFilePath}";
      var fileNameArr = [];
      if(uploadFilePath && uploadFilePath != ""){
        <s:iterator value="#bean.uploadFilePath" var="path">
        var attrName = $("[id*='File-" + currAttributeName + "_f']").attr('name');
        var attrFilePath = attrName.substring(0, attrName.lastIndexOf('.') + 1);
        var paths = "${path}".split(",");

        for(var i in paths) {
          var $hiddenFileValue = $('<input>').attr({type: 'hidden',name: attrFilePath + "uploadFilePath",value: paths[i]});
          $hiddenFileValue.appendTo($("#attach-file-dialog-" + id));

          var fileName = paths[i].substring(paths[i].lastIndexOf("/") + 1);
          fileName = fileName.substring(fileName.indexOf("_") + 1);
          fileNameArr.push(fileName);

          var $fileRow = $('<p/>').text(fileName).css("margin-top", "10px");
          $fileRow.prepend($('<span class="label label-info" style="margin-right: 10px;"/>').text("Uploaded Successfully"));
          $fileRow.appendTo($('#attach-files-' + id));
        }
        </s:iterator>
      }

      var valArr = currAttributeValue.split(',');
      var valArrLength = valArr.length;
      var fileNameList = "", separator = "", fileNameCharCount = 0;
      for(var j=0; j < valArr.length; j++){
        var fileName = valArr[j].substring(valArr[j].indexOf("_") + 1);
        if(fileName != "") {
          var exist = false;
          for (var index in fileNameArr) {
            if (fileNameArr[index] == fileName) {
              exist = true;
              break;
            }
          }
          if (!exist) {
            $files.append("<div id='file-" + id + "-" + j + "'><strong><input type='hidden' name='" + name + ".existingFileName' value='" + fileName + "' >" + fileName + "</strong> " +
                    "<button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Download' style='float: right;margin-left: 2px;' onclick='downloadFile(\"" + fileName + "\",\"sampleName\",\"" + currAttributeName + "\");'><img src='images/download_file.png' style='height: 20px;'></button>" +
                    "<button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Remove' style='float: right;' onclick='removeFile(\"file-" + id + "-" + j + "\");'><img src='images/cancel.png' style='height: 20px;'></button></div><br>");

            fileNameList += separator + fileName;
            fileNameCharCount = (fileName.length > fileNameCharCount) ? fileName.length : fileNameCharCount;
            separator = " ";
          } else {
            valArrLength -= 1;
          }
        } else {
          valArrLength -= 1;
        }
      }
      if(valArrLength > 1) {
        $files.append("<button type='button' class='btn btn-primary btn-xs' onclick='downloadFile(\"DOWNLOADALL\",\"sampleName\",\"" + currAttributeName + "\");'>Download All</button>")
      }
      if(valArrLength > 0) {
        $files.append("<input type='hidden' name='" + name + ".existingFileName' value='  ' >");
      }

      if(fileNameList != "") {  //update tooltip
        $("#file_" + id).attr("data-tooltip", fileNameList);

        $("head").append("<style> #file_" + id + ":hover:after {width : " + ((fileNameCharCount + 1) * 7) + "px !important;}</style>");
      }
    } else if($("select[id*='select_" + currAttributeName + "_f'][multiple]").length){
      var valueArr = currAttributeValue.split(",");
      var $node = $("select[id*='select_" + currAttributeName + "_f'][multiple]");

      for(var j in valueArr){
        if(valueArr[j].charAt(0) == ' ') valueArr[j] = valueArr[j].replace(" ", "");
        $node.multipleSelect('setSelects', valueArr);
      }
    }
    </s:iterator>
    <s:set var="oldLoadingSample" value="loadingSample" />
    <s:if test="%{#oldLoadingSample != null && #oldLoadingSample.getSampleName() != null}">
      $('#_sampleName').val('<s:property value="#oldLoadingSample.sampleName"/>');
    // utils.preSelect('_parentSampleSelect', '<s:property value="#oldLoadingSample.parentSampleName"/>');
    // utils.preSelect('_isSamplePublic', '<s:property value="#oldLoadingSample.isPublic"/>');
    </s:if>
    <s:else>
    <s:set var="oldLoadingProject" value="loadingProject" />
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
      /*$('#projectSelectRow').hide();*/
      $('#_eventSelect').prop('disabled', true);
      $('.page-header h1').html('Project Registration');
      $('#saveButton, #validateButton').hide(); //hide buttons
      $('input:radio[id^="r_"]').each(function(i,v) { //change view types to include project
        var $label = $(this).parent().contents().last()[0];
        $label.textContent = $label.textContent.replace('Sample', 'Project');
      })
      $('#breadcrumb2').text('Admin');
      $('#sampleSelect, #searchSample').prop("disabled", true);$('#form-sample-name').hide(); //Hide sample select for project registration
    } else if(filter === 'su') { //edit data redirected from search and edit page
      $('.page-header h1').html('Edit Data');
      $('#interactive-submission-group label:not(:nth-child(2))').addClass('disabled'); //Disable data submits except web form
      $('#exportButton').show(); //show export samples button
    }

    $('#sampleSelect').keypress(function (e) {
      var key = e.which;
      if(key == 13) { // the enter key code
        $('#searchSample').click();
        this.blur();
        return false;
      }
    });

    $('#s-name-autofill').on("keyup keypress change", function () {
      if ($(this).val() == "") $("#autofill-option-button").hide();
      else $("#autofill-option-button").show();
    });

    $('#autofill-clear').on("click", function(){
      $('#s-name-autofill').val("");
      $("#autofill-option-button").hide();
    });

    utils.processing(false);

    var offset = 250;
    var duration = 300;
    $(window).scroll(function() {
      if ($(this).scrollTop() > offset) {
        $('.scrollToTop').fadeIn(duration);
      } else {
        $('.scrollToTop').fadeOut(duration);
      }

      if ((($(document).height() - $(this).height()) - $(this).scrollTop()) > offset) {
        $('.scrollToBottom').fadeIn(duration);
      } else {
        $('.scrollToBottom').fadeOut(duration);
      }
    });

    $('.scrollToTop').click(function(event) {
      $('html, body').animate({scrollTop: 0}, duration);
      return false;
    })

    $('.scrollToBottom').click(function(event) {
      $('html, body').animate({scrollTop: $(document).height()}, duration);
      return false;
    })
  });</script><script src="scripts/page/event.loader.js"></script></body></html>