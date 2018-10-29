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
  <link rel="stylesheet" href="datatables/datatables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <style>
    #column_filter{margin: 5px 0 18px;float: left;}
    .column_filter_box{margin: 5px 0 5px 15px;}
    #columnSearchBtn{margin:10px 0 0 15px;float:left;}
    .select_column, .select_operation, .filter_text, .removeColumnFilter, #addMoreColumnFilter{margin-left: 4px;}
    .scrollButton{padding:10px;text-align:center;font-weight: bold;color: #FFFAFA;text-decoration: none;position:fixed;right:40px;background: rgb(0, 129, 179);display: none;}
    #sampleTable_filter label, #sampleTable_filter .input-group-btn {float:left;}
    #col_filter_border_l{border-left: 2px solid #333333;position: absolute;margin-left: 18px;left: 0;top: 55px;bottom: 0;}
    #col_filter_border_b{border-bottom: 2px solid #333333;position: absolute;right: 90%;margin-left: 18px;left: 0;bottom: 0;}
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
        <!-- Modal -->
        <div class="modal fade" id="export-samples-modal" role="dialog">
          <div class="modal-dialog" style="max-width: 450px;">

            <!-- Modal content-->
            <div class="modal-content" >
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa-angle-right"></i> <strong>Select Project Event</strong></h4>
              </div>
              <div class="modal-body">
                <div style="display: flex;align-items: center;">
                  <label class="col-sm-4 control-label"><strong>Event Name</strong></label>
                  <div class="col-sm-8 input-group">
                    <select id="eventName" name="eventName" class="form-control"></select>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-info" onclick="_page.edit.submitExportSample();">Download</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>

          </div>
        </div>

        <s:form id="eventDetailPage" name="eventDetailPage" namespace="/" action="eventDetail" method="post" theme="simple">
          <input type="hidden" id="ids" name="ids"/>
          <s:hidden id="editable" name="editable" value="0" />
          <div class="page-header">
            <h1>Search and Edit Data</h1>
          </div>
          <div id="HeaderPane" style="margin-top:15px;margin-bottom: 15px;">
            <div id="errorMessagesPanel"></div>
            <s:if test="hasActionErrors()">
              <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
            </s:if>
            <s:if test="hasActionMessages()">
              <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
                <div class="alert_info" onclick="$('.alert_info').remove();">
                  <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
          </div>
          <div id="mainContent">

            <div id="popupLayerScreenLocker" style="position: fixed; left: 0; top: 0; opacity: 0.5; height: 100%; width: 100%; z-index: 1000; display: none; background: rgb(0, 0, 0);"><!-- --></div>
            <div id="processingDiv" class="show_processing" style="display: none;position: fixed;">Processing your request. Please wait...</div>

            <div id="statusTableDiv">
              <div id="tableTop">
                <div class="row">
                  <div class="col-md-2">Project Name</div>
                  <div class="col-lg-5 input-group">
                    <s:select label="Project" id="_projectSelect" cssStyle="width:150px;margin:0 5 0 10;"
                              list="projectList" name="projectId" headerKey="0" headerValue="Select by Project Name"
                              listValue="projectName" listKey="projectId" required="true"/>
                    <button type="button" class="btn btn-default btn-xs" id="refreshDataBtn" onclick="refreshData();" data-tooltip="Refresh Data" style="display: none;">
                      <span class="glyphicon glyphicon-refresh" aria-hidden="true"></span>
                    </button>
                  </div>
                </div>
              </div>

              <!-- project -->
              <div id="projectTableDivHeader" style="margin:25px 0 0 0;display: none;">
                <h2 class="csc-firstHeader middle-header">Project Details <small><span id="toggleProjectDetails" class="glyphicon glyphicon-plus-sign" aria-hidden="true" style="color:green;cursor: pointer;"></span></small></h2>
              </div>

              <div id="projectTableDiv" style="margin:0 10px 5px 0;display: none;">
                <table name="projectTable" id="projectTable" class="table table-bordered table-striped table-hover">
                  <tbody id="projectTableBody">
                  </tbody>
                </table>
              </div>

              <!-- sample -->
              <div id="sampleTableDivHeader" style="display: none;">
                <h2 class="csc-firstHeader middle-header">Sample Details</h2>
              </div>
              <div id="sampleTableDiv" style="display: none;">
                <table name="sampleTable" id="sampleTable" class="table table-bordered table-striped table-condensed table-hover">
                  <thead id="sampleTableHeader"><tr></tr></thead>
                  <tbody id="sampleTableBody"/>
                </table>
                <div id="buttons"></div>
                <input onclick="_page.edit.sampleEvent();" class="btn btn-primary" disabled="true" type="button" value="Edit Sample" id="editSampleBtn" style="margin-top: 20px;" />
                <input onclick="_page.edit.exportSample();" class="btn btn-primary" type="button" value="Export Sample(s)" id="exportSampleBtn" style="margin-top: 20px;" />
              </div>

            </div>
          </div>

        </s:form>

      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />

<script src="datatables/datatables.js"></script>
<script src="datatables/pdfmake-0.1.32/pdfmake.js"></script>
<script src="datatables/pdfmake-0.1.32/vfs_fonts.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.colVis.js"></script>
<script src="scripts/page/event.detail.js"></script>
<script>
  $(document).ready(function() {
    $('.navbar-nav li').removeClass('active');
    $('.navbar-nav > li:nth-child(3)').addClass('active');

    utils.combonize('statusTableDiv');
    utils.initDatePicker();

    //empty project select box
    $('#_projectSelect ~ input').click(function() {
      var $projectNode = $('#_projectSelect');
      if($projectNode.val() === '0') {
        $(this).val('');
        $projectNode.val('0');
      }
    });

    $('#fromDate, #toDate').change( function() {
      _page.get.edt($('#_projectSelect').val(), $('#_sampleSelect').val());
    });
    $('#eventToggleImage, #sampleToggleImage, #table_openBtn').attr('src', openBtn);

    //add click listener on row expander
    $('tbody td #rowDetail_openBtn').live('click', function () {
      var _row = this.parentNode.parentNode, _is_event=(_row.parentNode.id.indexOf('event')>=0), _table=_is_event?eDT:sDT;
      if(this.src.indexOf('details_close')>=0){
        this.src = openBtn;
        _table.fnClose(_row);
      } else {
        this.src = closeBtn;
        _table.fnOpen(_row, subrow_html.replace(/\$d\$/, _table.fnGetData(_row)[(_is_event?6:5)]), '_details');
        $('td._details').attr('colspan', 7); //fix misalignment issue in chrome by incresing colspan by 1
        $('td._details>div').css('width', $('#projectTableDiv').width()-90);
      }
    });

    $('thead #table_openBtn').live('click', function () {
      var _is_event=this.parentNode.parentNode.parentNode.id.indexOf('event')>=0;
      $('#'+(_is_event?'eventTable':'sampleTable')+' #rowDetail_openBtn').click();
      buttonSwitch(this);
    });

    $('#toggleProjectDetails').live('click', function () {
      $('#projectTableDiv').toggle();
      buttonSwitch(this);
    });

    //preload page with data if available
    var projectId = '${projectId}', sampleId='${sampleId}';
    if(projectId && projectId != 0) {
      utils.preSelect('_projectSelect', projectId);
      _page.change.project(projectId, 0);
      if(sampleId && sampleId != 0) {
        utils.preSelect('_sampleSelect', sampleId);
      }

      $('#refreshDataBtn').show();
    }
    utils.error.check();


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
  });
</script>
</body>
</html>