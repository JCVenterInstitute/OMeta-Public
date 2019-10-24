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
  <link rel="stylesheet" href="datatables/Buttons-1.4.2/css/buttons.bootstrap.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <style>
    .row-details-table tr.even{font-weight: bold;}
    #column_filter{margin: 5px 0 18px;float: left;}
    #col_filter_border_l{border-left: 2px solid #333333;position: absolute;margin-left: 18px;left: 0;top: 55px;bottom: 0;}
    #col_filter_border_b{border-bottom: 2px solid #333333;position: absolute;right: 90%;margin-left: 18px;left: 0;bottom: 0;}
    .column_filter_box{margin: 5px 0 5px 15px;}
    #columnSearchBtn{margin:10px 0 0 15px;}
    .select_column, .select_operation, .filter_text, .removeColumnFilter, #addMoreColumnFilter{margin-left: 4px;}
  </style>
</head>

<body class="smart-style-2">
<div id="container">
  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <s:form id="eventHistoryPage" name="eventHistoryPage" namespace="/" action="eventHistory" method="post" theme="simple">
          <s:hidden id="editable" name="editable" value="0" />
          <div class="page-header">
            <h1>Event History</h1>
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
            <!--<div id="columnsTable"></div>  for column listing-->
            <div id="statusTableDiv">
              <div id="tableTop">
                <div class="row">
                  <div class="col-lg-2 col-md-4">Project Name</div>
                  <div class="col-lg-5 input-group">
                    <s:select label="Project" id="_projectSelect" cssStyle="width:150px;margin:0 5 0 10;"
                              list="projectList" name="projectId" headerKey="0" headerValue="Select by Project Name"
                              listValue="projectName" listKey="projectId" required="true"/>
                  </div>
                </div>
              </div>

              <!-- event -->
              <div id="eventDetailsSection" style="display: none;">
                <div style="margin:25px 10px 0 0;">
                  <h1 class="csc-firstHeader middle-header">Event Details</h1>
                </div>
                <div id="eventDateDiv" style="margin:3px 10px 0 0;" class="row">
                  <fieldset class="row">
                    <div class="col-sm-1" style="padding-top:7px;">Date Range</div>
                    <div class="col-sm-2">
                      <div class="input-group col-sm-12">
                        <input id="fromDate" type="text" class="form-control"  style="position: initial" placeholder="from"/>
                        <label for="fromDate" class="input-group-addon">
                          <span class=""><i class="fa fa-calendar"></i></span>
                        </label>
                      </div>
                    </div>
                    <div class="col-sm-1" style="width:auto;padding-top:7px;">~</div>
                    <div class="col-sm-2">
                      <div class="input-group col-sm-11">
                        <input id="toDate" type="text" class="form-control"  style="position: initial" placeholder="to"/>
                        <label for="toDate" class="input-group-addon">
                          <span class=""><i class="fa fa-calendar"></i></span>
                        </label>
                      </div>
                    </div>
                  </fieldset>
                </div>
                <div id="eventTableDiveventTableDiv" style="margin:10px 10px 5px 0;clear:both">
                  <table name="eventTable" id="eventTable" class="table table-bordered table-striped table-condensed table-hover">
                    <thead>
                    <tr>
                      <th style="padding-right: 0"><span id="table_openBtn" class="glyphicon glyphicon-plus-sign" aria-hidden="true" style="color:green;cursor: pointer;"></span></th>
                      <th>Event Type</th>
                      <th>ID</th>
                      <th>Date</th>
                      <th>User</th>
                      <th>Hidden</th>
                    </tr>
                    </thead>
                    <tbody/>
                  </table>
                </div>
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
<script>
  var openBtn = "glyphicon-plus-sign",
      closeBtn = "glyphicon-minus-sign",
      subrow_html='<table class="table table-bordered table-striped table-condensed table-hover row-details-table">$d$</table>';

  var eDT; //event detail table

  $(document).ready(function() {
    $('.navbar-nav li').removeClass('active');
    $('.navbar-nav > li:nth-child(4)').addClass('active');
  });

  //dataTables functions
  $.fn.dataTableExt.afnFiltering.push(
          function( oSettings, aData, iDataIndex ) {
            var fromDate = $('#fromDate').val(), toDate = $('#toDate').val();
            if ( fromDate == "" && toDate == "" ) { return true; }
            var iMin = parseInt(fromDate===''?'0':fromDate.split("-").join("")),
                    iMax = parseInt(toDate===''?'0':toDate.split("-").join("")),
                    iDate = parseInt(aData[3].split("-").join(""));
            if ((iMin===0 && iDate<=iMax) || (iMin<=iDate && 0===iMax) || (iMin<=iDate && iDate<=iMax))
              return true;
            else
              return false;
          }
  );
  $.fn.dataTableExt.oApi.fnFilterOnReturn = function (oSettings) {
    var _settings = this, anControl = $('input', _settings.fnSettings().aanFeatures.f);
    $('img[id^="dosearch_'+oSettings.sTableId+'"]').live('click', function(e) {
      if(anControl.val()!=null && anControl.val().trim()!=='')
        _settings.fnFilter(anControl.val());
    });

    _settings.each(function (i) {
      $.fn.dataTableExt.iApiIndex = i;
      anControl.unbind('keyup').bind('keypress', function (e) {
        if (e.which == 13) {
          $.fn.dataTableExt.iApiIndex = i;
          _settings.fnFilter(anControl.val());
        }
      });
      return this;
    });
    return _settings;
  };
  $.fn.dataTableExt.oApi.fnNewAjax = function (oSettings, sNewSource) {
    if (sNewSource != null) {
      oSettings.sAjaxSource = sNewSource;
    }
    $('input', this.fnSettings().aanFeatures.f).val('');
    oSettings.oPreviousSearch.sSearch='';
    this.fnDraw();
  };

  var _page = {
            change: {
              project: function(projectId, sampleId) {
                if(_page.get.project(projectId) == true) {
                  _page.get.sample(projectId);
                  _page.change.sample(projectId, sampleId?sampleId:0);
                } else {
                  //reset
                  $("#_sampleSelect").html(vs.empty);
                  if(eDT) {
                    eDT.fnNewAjax("eventDetailAjax.action?type=edt&projectId=0");
                  }
                  utils.error.add(utils.error.message.permission);
                }
              },
              sample: function(projectId, sampleId) {
                _page.get.edt(projectId, sampleId, 0);
              },
              eventStatus: function(eventId) {
                gethtmlByType("ces", 0, 0, eventId);
              }
            },
            get: {
              project: function(projectId) {
                return gethtmlByType("project", projectId, 0, 0);
              },
              sample: function(projectId) {
                return gethtmlByType("sample", projectId, 0, 0);
              },
              edt: function(projectId, sampleId, eventId) {
                var dates = "&fd="+$('#fromDate').val()+"&td="+$('#toDate').val();
                if(typeof sampleId === "undefined") sampleId = 0;
                eDT.fnNewAjax("eventDetailAjax.action?type=edt&projectId="+projectId+"&sampleId="+sampleId+"&eventId=0"+dates);
              }
            },
            edit: {
              project: function() {
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'projectName', value: $('#_projectSelect:selected').text()}));
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'eventName', value: 'ProjectUpdate'}));
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'jobType', value: 'projectedit'}));
                this.submit('project');
              },
              sampleEvent: function() {
                var sampleIds = '';
                if(sampleIds.length === 0) {
                  utils.error.baloon("Please select sample to load or edit event.");
                } else {
                  $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'ids'}).val(sampleIds));
                  this.submit('sample');
                }
              },
              submit: function(type) {
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'label', value: type}));
                $('#eventDetailPage').attr('action', 'eventLoader.action?filter=su').submit();
              }
            },
            button: {},
            columnfilter: {
              toggle: function (btn) {
                var $btn = $(btn);
                var $columnFilter = $("#column_filter");
                if ($btn.attr("name").indexOf("show") > -1) {
                  $columnFilter.show();
                  $btn.attr("name", "hideColumnFilter");
                } else {
                  $columnFilter.hide();
                  $btn.attr("name", "showColumnFilter");
                }
              },
              isActive: function () {
                return $("#columnFilterBtn").attr("name") != null && $("#columnFilterBtn").attr("name").indexOf("hide") > -1;
              }
            }
          },
          changeEventStatus = function(eventId) {
            _page.change.eventStatus(eventId);
          },
          buttonSwitch = function(node, name) {
            if(node==null) { node = document.getElementById(name); }
            if(node.classList.contains(openBtn)){
              node.classList.remove(openBtn);
              node.classList.add(closeBtn);
              node.style.color = "red";
            } else {
              node.classList.remove(closeBtn);
              node.classList.add(openBtn);
              node.style.color = "green";
            }
          };

  function comboBoxChanged(option, id) {
    if(id==='_projectSelect') {
      if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
        _page.change.project(option.value, 0);
        $('.ui-autocomplete-input').val('');
      } else {
        $("#_sampleSelect").html(vs.empty);
      }
    } else if(id==='_sampleSelect') {
      if(option.value!=null && option.text!=null && option.text!='') {
        _page.change.sample($('#_projectSelect').val(), option.value);
        //_page.button.showSample();
      }
    }
  }

  var attributeTypeMap = [];
  var headerList = [];

  <!-- Generate html content using Ajax by type -->
  function gethtmlByType(ajaxType, projectId, sampleId, eventId) {
    //Reset filter operations
    if($("#column_filter")) $("#column_filter").remove();
    $("#columnFilterBtn").attr("name", "showColumnFilter");

    var content = '', rtnVal = false;
    $.ajax({
      url:"sharedAjax.action",
      cache: false,
      async: ajaxType === 'project'?false:true,
      data: "type="+ajaxType+"&projectId="+projectId+"&sampleId="+sampleId+"&eventId="+eventId,
      success: function(html){
        if(html.aaData) {
          if(ajaxType == "project") {
            $(html.aaData).each(function(i1,v1) {
              if(v1 && i1 == 0) {
                $.each(v1, function(i2,v2) {
                  if(v2) {
                    if(i2==="editable") { $("#editable").val(v2); }
                    else { content += '<tr class="even"><td width="25%">'+i2+'</td><td>'+v2+'</td></tr>'; }
                  }
                });
                rtnVal = true;
              } else if(v1 && i1 == 1){  //Assign lookup value/type to global var
                headerList = [];
                attributeTypeMap = v1;
                for (var key in v1){
                  headerList.push(key);
                }
              }
            });
            $("tbody#projectTableBody").html(content);
          } else if(ajaxType === "sample") {
            var list = vs.alloption;
            $(html.aaData).each(function(i1,v1) {
              if(v1) { list += vs.vnoption.replace("$v$",v1.id).replace("$n$",v1.name); }
            });
            if(sampleId == null || sampleId == 0) { $("#_sampleSelect").html(list); }
          } else if(ajaxType == "ces") { //change event status
            if(html.aaData && html.aaData[0]==='success') {
              _page.get.edt($('#_projectSelect').val(), $('#_sampleSelect').val(), 0);
              rtnVal = true;
            }
          }
        }
      }
    });

    generateColumnFilter();
    return rtnVal;
  }

  function triggerSearch(){
    var searchVal = $("#eventTable_filter > label > input").val();
    eDT.fnFilter(searchVal);
  }

  function generateColumnFilter(){
    var $columnFilterDiv = $("<div>", {id: "column_filter", style:"display:none;"}).append(
            $('<span/>').attr({'class': 'glyphicon glyphicon-filter','aria-hidden': 'true'})).append(
            $("<div>", {id: "col_filter_border_l"})).append($("<div>", {id: "col_filter_border_b"}));
    $columnFilterDiv.insertAfter($(".dataTables_filter"));
    $columnFilterDiv.append($("<button>")
            .attr({'type':'button', 'class':'btn btn-default btn-sm', 'id':'columnSearchBtn', 'name':'columnSearchBtn', 'onclick':'triggerSearch()'})
            .html('Apply'));

    addNewFilter(-1);
  }

  function addNewFilter(i){
    var $addMoreBtn = $("<span>").attr({'class':'glyphicon glyphicon-plus-sign', 'style':'color:green;cursor: pointer;', 'id':'addMoreColumnFilter', 'onclick':'addNewFilter('+ ++i +');'});
    var $columnFilterBox = $("<div>", {'class': 'column_filter_box'});
    var $columnFilterSelect = $("<select>", {class:"select_column", id: "select_column_"+i, name:"column_name", 'onchange':'updateOperation(this.value,'+ i + ')'});
    var $columnFilterOperation = $("<select>", {class:"select_operation form-control input-sm", id: "select_operation_"+i, name:"operation"});

    $.each(headerList, function(i2,v2) {
      $columnFilterSelect.append($("<option></option>").attr("value", v2).text(v2));
    });

    $columnFilterOperation.append($("<option></option>").attr("value", "equals").text("="));
    $columnFilterOperation.append($("<option></option>").attr("value", "like").text("LIKE"));
    $columnFilterOperation.append($("<option></option>").attr("value", "in").text("IN"));

    //Automatically add "AND" gate to first column filter
    if(i == 0){
      $columnFilterBox.append($("<input>").attr({'type':'hidden', class: "select_logicgate", id: "select_logicgate_0", name: "logicgate", value:"and"}))
              .append($("<label>").attr({'id':'first_filter_label','style':'width: 69px;text-align: center;'}).text('AND'));
    } else {
      var $columnFilterLogicGate = $("<select>", {
        class: "select_logicgate form-control input-sm",
        id: "select_logicgate_" + i,
        name: "logicgate"
      });
      $columnFilterLogicGate.append($("<option></option>").attr("value", "and").text("AND"));
      $columnFilterLogicGate.append($("<option></option>").attr("value", "or").text("OR"));
      $columnFilterLogicGate.append($("<option></option>").attr("value", "not").text("NOT"));

      $columnFilterBox.append($columnFilterLogicGate)
    }

    $columnFilterBox.append($("<div>").attr({'class':'input-group', 'style':'margin-left:4px;'}).append($columnFilterSelect).append($columnFilterOperation));
    $columnFilterBox.append($("<input>").attr({'type':'text', 'class':'filter_text form-control input-sm', 'id':'filter_text_'+i, 'name':'filter_text', 'style':'width: 150px; '}));
    if(i != 0) {
      $columnFilterBox.append($("<span>").attr({'class':'removeColumnFilter glyphicon glyphicon-minus-sign', 'style':'color:red;cursor: pointer;'})
              .click(function(){
                var $columnFilterBox = $(this).parent();

                if($columnFilterBox.get(0) === $(".column_filter_box:last").get(0)){
                  $(".column_filter_box").eq(-2).append($addMoreBtn);
                }
                $columnFilterBox.remove();
              }));
    }

    if($("#addMoreColumnFilter")) $("#addMoreColumnFilter").remove();
    $columnFilterBox.append($addMoreBtn);
    $columnFilterBox.insertBefore($('#columnSearchBtn'));

    var $currentSelectInput = $('#select_column_'+i);
    $currentSelectInput.combobox({
      selected: function (event, ui) {
        $currentSelectInput.trigger("onchange");
      }
    });
    var $autocompleteInput = $currentSelectInput.next();
    $autocompleteInput.attr("class", "form-control");
  }

  function updateOperation(val,i){
    var $lessOption = $("<option></option>").attr("value", "less").text("<");
    var $equalsOption = $("<option></option>").attr("value", "equals").text("=")
    var $greaterOption = $("<option></option>").attr("value", "greater").text(">");
    var $likeOption = $("<option></option>").attr("value", "like").text("LIKE");
    var $inOption = $("<option></option>").attr("value", "in").text("IN");
    var $select = $("#select_operation_" + i);
    $select.empty().append($equalsOption).append($likeOption).append($inOption);

    var type = attributeTypeMap[val];

    if(type !== 'string'){
      $select.append($lessOption).append($greaterOption);
    }
  }

  $(function() {
    utils.combonize('statusTableDiv');
    $('#fromDate, #toDate').datepicker({ dateFormat: 'yy-mm-dd' });

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

    var _html = {
      fm: '<div id="attach-file-dialog-$id$" class="attach-file-dialog" style="display:none;">' +
      '<div class="file-dialog-heading"><h2 title="Attach Files:&nbsp;">Attach Files<span style="display:none" class="header-separator">:&nbsp;</span></h2></div>' +
      '<div class="buttons-container form-footer" style="overflow: visible;min-height: 51px;height: 100%;margin: 0;padding: 10px;"><fieldset class="fm-fieldset" style="width: 600px;"><legend><span>Attachment(s)</span></legend><div id="files-$id$" class="files" style="padding-left:20px;">$existingFileField$</div></fieldset>' +
      '<div class="buttons" style="float: right"><button type="button" class="btn btn-primary" id="attach-file-done-$id$" style="margin: 10px;">Done</button></div></div></div>'
    }

    <!-- EVENT TABLE -->
    eDT =  $("#eventTable").dataTable({
      "language": {
        "processing": "Processing your request. Please wait..."
      },
      "bProcessing": true,
      "bServerSide": true,
      "sPaginationType": "full_numbers",
      "iDeferLoading": 0,
      "sAjaxSource": "",
      "fnServerData": function ( sSource, aoData, fnCallback ) {
        if(_page.columnfilter.isActive()) {
          var index = 0;  // keep count to have an accurate list size in case of empty filter values
          $('.column_filter_box').each(function (i, elem) {
            var $filterText = $(this).children('input:text[class="filter_text form-control input-sm"]');
            var filterTextVal = $filterText.val();

            if (index == 0 || (filterTextVal && filterTextVal != '')) {
              var nth = $filterText.attr('id').split("_")[2];
              aoData.push({"name": "columnName[" + index + "]", "value": $("#select_column_" + nth).val()});
              aoData.push({
                "name": "columnSearchArguments[" + index + "]",
                "value": filterTextVal + ";" + $("#select_operation_" + nth).val() + ";" + $("#select_logicgate_" + nth).val()
              });

              index++;
            }
          });
        }
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
                          "<span id='rowDetail_openBtn' class='glyphicon glyphicon-plus-sign' aria-hidden='true' style='color:green;cursor: pointer;margin-left: 2px;'></span>",
                          rowData.eventName,
                          rowData.sampleName,
                          rowData.createdOn,
                          rowData.actor
                  );
                  // if(rowData.eventStatus) {
                  //   row.push(rowData.eventStatus + "<a href='javascript:changeEventStatus(" + rowData.eventId + ");'><img src='images/blue/" + (rowData.eventStatus === 'Active' ? 'cross' : 'tick') + ".png'/></a>");
                  // }
                  if(rowData.attributes) {
                    var headers = '', values = '';
                    var ri = 0;
                    $.each(rowData.attributes, function(ai, av) {
                      headers += '<td>' + ai + '</td>';

                      if(av && av.toString().indexOf("downloadfile") > -1 ) {
                        var id = ai + '_' + ri;

                        var valArr = av.toString().split(',');
                        var valArrLength = valArr.length;
                        var existingFileField = "", downloadAllButton = "", fileNameList = "", separator = "", fileNameCharCount = 0;

                        for(var j=0; j < valArr.length; j++) {
                          var fileName = valArr[j].substring(valArr[j].indexOf("_") + 1, valArr[j].indexOf("&"));
                          if (fileName != "") {
                            existingFileField += "<div id='file-" + id + "-" + j + "'><strong>" + fileName + "</strong><button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Download' style='float: right;margin-left: 2px;' onclick='downloadFile(\"" + fileName + "\",\"" + rowData.sampleName + "\",\"" + ai + "\");'><img src='images/download_file.png' style='height: 20px;'></button></div><br>";

                            fileNameList += separator + fileName;
                            fileNameCharCount = (fileName.length > fileNameCharCount) ? fileName.length : fileNameCharCount;
                            separator = " ";
                          } else {
                            valArrLength -= 1;
                          }
                        }

                        if(valArrLength > 1) {
                          downloadAllButton = "<button type='button' class='btn btn-success' onclick='downloadFile(\"DOWNLOADALL\",\"" + rowData.sampleName + "\",\"" + ai + "\");'>Download All</button>";
                        }

                        values += '<td><button type="button" id="file_' + id + '"  class="btn btn-default btn-xs table-tooltip" data-tooltip="'+ fileNameList +'" style="white-space: pre-line;" value="FILE MANAGEMENT" onclick="showFMPopup(this.id)">File Store</button>' + _html.fm.replace(/\$id\$/g,id).replace(/\$existingFileField\$/g, existingFileField).replace(/\$downloadallbutton\$/g, downloadAllButton) + '</td>';
                        $("head").append("<style> #file_" + id + ":hover:after {width : " + ((fileNameCharCount + 1) * 7) + "px !important;}</style>");
                      } else {
                        values += '<td>' + av + '</td>';
                      }

                      ri += 1;
                    })
                    attributes = '<tr class="even">' + headers + '</tr><tr class="odd">' + values + '</tr>';
                  } else {
                    attributes = '<tr class="odd"><td colspan="7">No Data</td></tr>';
                  }
                  row.push(attributes);
                  rows.push(row);
                })
              }
              json.aaData = rows;
              fnCallback(json);

              $('#eventDetailsSection').show();
              $('#eventTable th:first').removeClass('sorting_asc');
              $('#eventTable_filter').parent("div.col-sm-6").attr('class', 'col-sm-8').insertBefore($('#eventTable_length').parent("div.col-sm-6"));
              $('#eventTable_length').parent("div.col-sm-6").attr('class', 'col-sm-4').css("text-align", "right")
              $('#eventTable_filter').css("float", "left")
            }
          });
        }
      },
      "bAutoWidth" : false,
      columnDefs: [
        {"orderable": false, "targets": 0},
        {width: "30%", targets:1},
        {width: "30%", targets:2},
        {width: "20%", targets:3},
        {width: "20%", targets:4},
        {"bSearchable": true, "bVisible": false, targets: 5}
      ]
    }).fnFilterOnReturn();

    $(".dataTables_filter").append(
            $('<span/>').append(
                    $('<button/>').attr({
                      'type': 'button',
                      'class': 'btn btn-default btn-sm',
                      'id': 'triggerSearchBtn',
                      'onclick': 'triggerSearch();',
                    }).append(
                            $('<span/>').attr({
                              'class': 'glyphicon glyphicon-search',
                              'aria-hidden': 'true'
                            })
                    )
            ).append(
                    $('<button/>').attr({
                      'type': 'button',
                      'class': 'btn btn-default btn-sm',
                      'id': 'columnFilterBtn',
                      'data-tooltip': 'Column Filter',
                      'name': 'showColumnFilter',
                      'onclick': '_page.columnfilter.toggle(this);',
                      'style': 'margin-left:10px;'
                    }).append(
                            $('<span/>').attr({
                              'class': 'glyphicon glyphicon-filter',
                              'aria-hidden': 'true'
                            })
                    )
            )
    );

    //add click listener on row expander
    $('tbody td #rowDetail_openBtn').live('click', function () {
      if(this.classList.contains(closeBtn)) toggleRow(this,'close')
      else toggleRow(this,'open')
    });

    $('thead #table_openBtn').live('click', function () {
      if(this.classList.contains(closeBtn)){
        this.classList.remove(closeBtn);
        this.classList.add(openBtn);
        this.style.color = "green";
        $('#eventTable #rowDetail_openBtn').each(function(){toggleRow(this,'close')});
      } else {
        this.classList.remove(openBtn);
        this.classList.add(closeBtn);
        this.style.color = "red";
        $('#eventTable #rowDetail_openBtn').each(function(){toggleRow(this,'open')});
      }
    });

    function toggleRow(item, action) {
      var _row = item.parentNode.parentNode;
      if(action == 'open'){
        item.classList.remove(openBtn);
        item.classList.add(closeBtn);
        item.style.color = "red";
        eDT.fnOpen(_row, subrow_html.replace(/\$d\$/, eDT.fnGetData(_row)[5]), '_details');
        $('td._details').attr('colspan', 6); //fix misalignment issue in chrome by incresing colspan by 1
        $('td._details>div').css('width', $('#statusTableDiv').width()-90);
      } else {
        item.classList.remove(closeBtn);
        item.classList.add(openBtn);
        item.style.color = "green";
        eDT.fnClose(_row);
      }
    }

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

  function showFMPopup(id){
    id = id.substring(id.indexOf("_") + 1);
    $("#attach-file-dialog-" + id).show();
    $(".ui-blanket").css({"visibility":"visible"});

    $('#attach-file-done-' + id).click(function() {
      $("#attach-file-dialog-" + id).hide();
      $(".ui-blanket").css({"visibility":"hidden"});
    });
  }

  function downloadFile(fileName, sampleName, attrName) {
    var $downloadFileForm = $('<form>').attr({
      id: 'downloadFileForm',
      method: 'POST',
      action: 'downloadfile.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'attributeName',
      name: 'attributeName',
      value : attrName
    }).appendTo($downloadFileForm);

    $('<input>').attr({
      id: 'fileName',
      name: 'fileName',
      value : fileName
    }).appendTo($downloadFileForm);

    $('<input>').attr({
      id: 'projectId',
      name: 'projectId',
      value : utils.getProjectId()
    }).appendTo($downloadFileForm);

    $('<input>').attr({
      id: 'sampleVal',
      name: 'sampleVal',
      value : sampleName
    }).appendTo($downloadFileForm);

    $('body').append($downloadFileForm);
    $downloadFileForm.submit();
  }
</script>
</body>
</html>

