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
  <%--
  <link rel="stylesheet" href="style/version01.css" />--%>
  <style>
    td._details {
      text-align:left;
      padding:0 0 0 35px;
      border: 1px gray dotted;
    }
    td._details div {
      position: relative; overflow: auto; overflow-y: hidden;
    }
    td._details table td {
      border:1px solid white;
    }

    .datatable_top, .datatable_table, .datatable_bottom {
      float:left;
      clear:both;
      width:100%;
      min-width: 165px;
    }
    .dataTables_length {
      height: 29px;
      vertical-align: middle;
      min-width: 165px !important;
      margin-top: 2px;
    }
    .dataTables_filter {
      width: 260px !important;
    }
    .dataTables_info {
      padding-top: 0 !important;
    }
    .dataTables_paginate {
      float: left !important;
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
            <li>Data Submission</li>
            <li>Search and Edit Data</li>
          </ol>
        </div>

        <s:form id="eventDetailPage" name="eventDetailPage" namespace="/" action="eventDetail" method="post" theme="simple">
          <s:hidden id="editable" name="editable" value="0" />
          <div class="page-header">
            <h1>Search and Edit Data</h1>
          </div>
          <div id="HeaderPane">
            <div id="errorMessagesPanel" style="margin-top:15px;margin-bottom: 15px; color: #ffffff;background-color: #a90329;border-color: #900323;width: auto;display: inline-block;"></div>
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
            <div class="row" id="loadingImg" style="display: none;">
              <div class="container" style="padding-left:5px;">
                <img src="images/loading.gif" />
              </div>
            </div>
          </div>
          <div id="mainContent">
            <!--<div id="columnsTable"></div>  for column listing-->
            <div id="statusTableDiv">
              <div id="tableTop">
                <div class="row">
                  <div class="col-md-2">Project Name</div>
                  <div class="col-md-10 combobox">
                    <s:select label="Project" id="_projectSelect" cssStyle="width:150px;margin:0 5 0 10;"
                              list="projectList" name="projectId" headerKey="0" headerValue="Select by Project Name"
                              listValue="projectName" listKey="projectId" required="true"/>
                  </div>
                </div>
                <div class="row row_spacer">
                  <div class="col-md-2">Sample</div>
                  <div class="col-md-10 combobox">
                    <s:select id="_sampleSelect" cssStyle="margin:0 5 0 10;" list="#{'0':'Select by Sample'}"
                              name="selectedSampleId" required="true"/>
                  </div>
                </div>
              </div>

              <!-- project -->
              <div id="projectTableDivHeader" style="margin:25px 10px 0 0;">
                <h2 class="csc-firstHeader middle-header">Project Details <img id="toggleProjectDetails" src="images/dataTables/details_open.png"></h2>
              </div>

              <div id="projectTableDiv" style="margin:0 10px 5px 0;">
                <table name="projectTable" id="projectTable" class="contenttable" style="width:180%;">
                  <tbody id="projectTableBody">
                  </tbody>
                </table>
                <!-- <input onclick="_page.edit.project();" style="margin-top:10px;" disabled="true" type="button" value="Edit Project" id="editProjectBtn" /> -->
              </div>

              <!-- sample -->
              <div id="sampleTableDivHeader" style="margin:25px 10px 0 0;">
                <h2 class="csc-firstHeader middle-header">Sample Details</h2>
              </div>
              <div id="sampleTableDiv" style="margin:0 10px 5px 0;clear:both">
                <table name="sampleTable" id="sampleTable" class="contenttable" style="width:95%;">
                  <thead id="sampleTableHeader">
                  </thead>
                  <tfoot id="sampleTableFoot">
                  <tr>
                    <th>Sample Name</th>
                    <th>Parent</th>
                    <th>User</th>
                    <th>Date</th>
                  </tr>
                  </tfoot>
                  <tbody id="sampleTableBody"/>
                </table>
                <input onclick="_page.edit.sampleEvent();" class="btn btn-primary" disabled="true" type="button" value="Edit Sample" id="editSampleBtn" style="margin-top: 40px;display: block;" />
              </div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />

<script src="scripts/jquery/jquery.dataTables.js"></script>
<script src="scripts/jquery/jquery.colReorderWithResize.js"></script>
<script src="scripts/jquery/jquery.dataTables.columnFilter.js"></script>
<script src="scripts/page/event.detail.js"></script>
<script>
  $(document).ready(function() {
    $('#projectTableDivHeader').hide();
    $('#projectTableDiv').hide();
    $('#sampleTableDivHeader').hide();
    $('#sampleTableDiv').hide();

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

    <!-- SAMPLE TABLE -->
    /*sDT = $("#sampleTable").dataTable({
     "sDom": '<"datatable_top"lf><"datatable_table"rt><"datatable_bottom"ip>',
     "bProcessing": true,
     "bServerSide": true,
     "sPaginationType": "full_numbers",
     "sAjaxSource": "",
     "iDeferLoading": 0, // no initial loading flag
     "fnServerData": function (sSource, aoData, fnCallback) {
     if(sSource!=='') {
     $.ajax({
     dataType: 'json',
     type: "POST",
     url: sSource,
     data: aoData,
     success: function(json) {
     if(json && json.aaData) {
     var rows = [];
     $.each(json.aaData, function(ri,rowData) {
     var row = [], attributes;
     row.push(
     "<img src='images/dataTables/details_open.png' id='rowDetail_openBtn'/>",
     rowData.sampleName + "<input type='checkbox' style='margin-left:6px;' id='sampleCB_" + rowData.sample.sampleId + "'/>",
     rowData.parentSampleName,
     rowData.actor,
     rowData.createdOn
     );
     if (rowData.attributes) {
     var headers = '', values = '';
     $.each(rowData.attributes, function (ai, av) {
     headers += '<td>' + ai + '</td>';
     values += '<td>' + av + '</td>';
     })
     attributes = '<tr class="even">' + headers + '</tr><tr class="odd">' + values + '</tr>';
     } else {
     attributes = '<tr class="odd"><td colspan="6">No Data</td></tr>';
     }
     row.push(attributes);

     rows.push(row);
     })
     }
     json.aaData = rows;
     fnCallback(json);

     $('#projectTableDivHeader').show();
     $('#projectTableDiv').show();
     $('#toggleProjectDetails').attr('src', "images/dataTables/details_close.png");
     $('#sampleTableDivHeader').show();
     $('#sampleTableDiv').show();
     $("#loadingImg").hide();
     }
     });
     }
     },
     "bAutoWidth" : false,
     "aoColumnDefs": [
     {"sWidth": "23px", "bSortable": false, "aTargets": [ 0 ]},
     {"sWidth": "30%", "aTargets":[1]},
     {"sWidth": "30%", "aTargets":[2]},
     {"sWidth": "20%", "aTargets":[3]},
     {"sWidth": "20%", "aTargets":[4]},
     {"bSearchable": true, "bVisible": false, "aTargets": [ 5 ]}
     ]
     }).fnFilterOnReturn();*/

    //add click listener on row expander
    $('tbody td #rowDetail_openBtn').live('click', function () {
      var _row = this.parentNode.parentNode, _is_event=(_row.parentNode.id.indexOf('event')>=0), _table=_is_event?eDT:sDT;
      if(this.src.indexOf('details_close')>=0){
        this.src = openBtn;
        _table.fnClose(_row);
      }else {
        this.src = closeBtn;
        _table.fnOpen(_row, subrow_html.replace(/\\$d\\$/, _table.fnGetData(_row)[(_is_event ? 6 : 5)]), '_details');
        $('td._details').attr('colspan', 7); //fix misalignment issue in chrome by incresing colspan by 1
        $('td._details>div').css('width', $('#projectTableDiv').width() - 90);
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
    }

    utils.error.check();
  });

</script>
</body>
</html>

