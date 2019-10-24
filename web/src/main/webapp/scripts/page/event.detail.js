var _html = {
  fm: '<div id="attach-file-dialog-$id$" class="attach-file-dialog" style="display:none;">' +
  '<div class="file-dialog-heading"><h2 style="margin-top: 0px;margin-bottom: 9px;" title="Attach Files:&nbsp;">Attach Files<span style="display:none" class="header-separator">:&nbsp;</span></h2></div>' +
  '<div class="buttons-container form-footer" style="overflow: visible;min-height: 51px;height: 100%;margin: 0;padding: 10px;"><fieldset class="fm-fieldset" style="width: 600px;padding: 4px 0 4px 0px;"><div id="files-$id$" class="files" style="padding-left:20px;">$existingFileField$</div></fieldset>' +
  '<div class="buttons" style="float: right">$downloadallbutton$<button type="button" class="btn btn-primary" id="attach-file-done-$id$" style="margin: 10px;">Done</button></div></div></div>'
}

var openBtn = "glyphicon-plus-sign",
    closeBtn = "glyphicon-minus-sign",
    subrow_html='<table class="table table-bordered table-striped table-condensed table-hover row-details-table">$d$</table>';

var sDT, //sample detail table
    eDT; //event detail table

//dataTables functions
jQuery.fn.dataTableExt.oApi.fnFilterClear  = function ( oSettings )
{
  var i, iLen;

  /* Remove global filter */
  oSettings.oPreviousSearch.sSearch = "";

  /* Remove the text of the global filter in the input boxes */
  if ( typeof oSettings.aanFeatures.f != 'undefined' )
  {
    var n = oSettings.aanFeatures.f;
    for ( i=0, iLen=n.length ; i<iLen ; i++ )
    {
      $('input', n[i]).val( '' );
    }
  }

  /* Remove the search text for the column filters - NOTE - if you have input boxes for these
   * filters, these will need to be reset
   */
  for ( i=0, iLen=oSettings.aoPreSearchCols.length ; i<iLen ; i++ )
  {
    oSettings.aoPreSearchCols[i].sSearch = "";
  }

  /* Redraw */
  oSettings.oApi._fnReDraw( oSettings );
};

$.fn.dataTableExt.afnFiltering.push(
    function( oSettings, aData, iDataIndex ) {
      var fromDate = $('#fromDate').val(), toDate = $('#toDate').val();
      if ( (fromDate == "" && toDate == "" ) || typeof fromDate === "undefined" || typeof toDate === "undefined" ) {
        return true;
      }
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
            createSampleDataTable();
            _page.change.sample(projectId, sampleId?sampleId:0)
            $("#editProjectBtn, #editSampleBtn").attr("disabled", ($("#editable").val()!=1));
          } else {
            //reset
            $("#_sampleSelect").html(vs.empty);
            $("tbody#projectTableBody").empty();
            if(sDT) { //} && eDT) {
              sDT.fnNewAjax("eventDetailAjax.action?type=sdt&projectId=0");
              // eDT.fnNewAjax("eventDetailAjax.action?type=sdt&projectId=0");
            }
            $("#editProjectBtn, #editSampleBtn").attr("disabled", "true");
            utils.error.add(utils.error.message.permission);
          }
        },
        sample: function(projectId, sampleId) {
          _page.get.sdt(projectId, sampleId);
          // _page.get.edt(projectId, sampleId, 0);
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
        sdt: function(projectId, sampleId) {
          $('#fromDate, #toDate').val('');
          sDT.fnNewAjax("eventDetailAjax.action?type=sdt&projectId="+projectId+"&sampleId="+sampleId+"&eventId=0");
        },
        edt: function(projectId, sampleId, eventId) {
          var dates = "&fd="+$('input[name="fromDate"]').val()+"&td="+$('input[name="toDate"]').val();
        }
      },
      edit: {
        project: function() {
          utils.addInput('eventDetailPage', 'projectName', $('#_projectSelect:selected').text());
          utils.addInput('eventDetailPage', 'eventName', 'ProjectUpdate');
          utils.addInput('eventDetailPage', 'jobType', 'projectedit');
          this.submit('project');
        },
        sampleEvent: function() {
          var sampleIds = '', eventName = null, eventError = '';
          $('#sampleTableBody input[id^=sampleCB]:checked').each(function(i,v) {
            var info = v.id.substr(v.id.indexOf('_') + 1);
            sampleIds += info.substring(0, info.indexOf('[')) + ',';
            var t_eventName = info.substring(info.indexOf('[') + 1, info.indexOf(']'));
            if(!eventName) {
              eventName = t_eventName;
            } else {
              if(eventName !== t_eventName) {
                eventError = "Please select ONLY ONE Event type. Current sample selections include: " + eventName + ", " + t_eventName;
                return;
              }
            }
          });
          if(sampleIds.length === 0) {
            utils.error.baloon("Please select sample to load or edit event.");
          } else if(eventError.length !== 0) {
            utils.error.baloon(eventError);
          } else {
            utils.addInput('eventDetailPage', 'ids', sampleIds);
            utils.addInput('eventDetailPage', 'eventName', eventName.replace('Registration', 'Update'));
            this.submit('sample');
          }
        },
        exportSample : function() {
          var sampleIds = '';
          $('#sampleTableBody input[id^=sampleCB]:checked').each(function(i,v) {
            sampleIds += v.id.substr(v.id.indexOf('_') + 1) + ',';
          });
          if(sampleIds.length === 0) {
            utils.error.baloon("Please select sample to export!");
          } else {
            $.ajax({
              url: 'getEventTypes.action',
              cache: false,
              async: false,
              data: "t=sel_e&projectId=" + $("#_projectSelect").val(),
              success: function(html){
                if(html.eventTypeList) {
                  var eventTypes = html.eventTypeList;
                  $("#eventName").html('');
                  for(var i=0; i<eventTypes.length; i++) {
                    $("#eventName").append($("<option/>")
                        .attr("value", eventTypes[i].lookupValueId)
                        .text(eventTypes[i].name))
                  }
                }
              }
            });
            $("#ids").val(sampleIds);
            $('#export-samples-modal').modal('show');
          }
        },
        submitExportSample: function () {
          $("<form action='eventLoader.action'/>")
              .append($("<input/>").attr({
                type: 'hidden',
                name: 'jobType',
                value: 'export'
              }))
              .append($("<input/>").attr({
                type: 'hidden',
                name: 'projectId',
                value: $("#_projectSelect").val()
              }))
              .append($("<input/>").attr({
                type: 'hidden',
                name: 'projectName',
                value: $("#_projectSelect option:selected").text()
              }))
              .append($("<input/>").attr({
                type: 'hidden',
                name: 'ids',
                value: $("#ids").val()
              }))
              .append($("<input/>").attr({
                type: 'hidden',
                name: 'eventId',
                value: $("#eventName").val()
              }))
              .append($("<input/>").attr({
                type: 'hidden',
                name: 'eventName',
                value: $("#eventName option:selected").text()
              })).appendTo('body').submit();
          $('#export-samples-modal').modal('hide');
        },
        submit: function(type) {
          if (!$('input[name="label"]').length){
            $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'label', value: type}));
          }else{
            $('input[name="label"]').val(type);
          }
          $('#eventDetailPage').attr('target', '_blank');
          $('#eventDetailPage').attr('action', 'eventLoader.action?filter=su').submit();
        }
      },
      button: {
        toggleSample: function() {
          $('#sampleTableDiv').toggle(400);
          buttonSwitch(null, 'sampleToggleImage');
        },
        showSample: function() {
          if(!$('#sampleTableDiv').is(":visible")) {
            this.toggleSample();
          }
        }
      },
      select: {
        all: function() {
          $("#selectAllSamples").attr('checked', true);
          $('#sampleTableBody > tr ').each(function() {
            $(this).find("td:nth-child(2) > input").attr('checked', true);
          });
        }
      },
      deselect: {
        all: function() {
          $("#selectAllSamples").attr('checked', false);
          $('#sampleTableBody > tr ').each(function() {
            $(this).find("td:nth-child(2) > input").attr('checked', false);
          });
        }
      },
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
    }
  }
}

function refreshData(){
  var selectedProjectId = $("#_projectSelect").val()
  if(selectedProjectId != '0') _page.change.project(selectedProjectId, 0);
  else utils.error.add("Please select a project!");
}

var headerList = [];
var attributeTypeMap = [];

<!-- Generate html content using Ajax by type -->
function gethtmlByType(ajaxType, projectId, sampleId, eventId) {
  utils.processing(true);

  var content = '', rtnVal = false;
  $.ajax({
    url: ajaxType === 'project' ? 'getproject.action' : 'sharedAjax.action',
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
                  else {
                    if(typeof v2 === "string" && v2.indexOf("http://") > -1) {
                      v2 = '<a href="#" onclick="window.open(&quot;' + v2 + '&quot;);">' + v2 + '</a>';
                    }
                    content += '<tr class="even"><td width="25%">'+i2+'</td><td>'+v2+'</td></tr>';
                  }
                }
              });
              rtnVal = true;
            } else if(v1 && i1 == 1){  //create dynamic table header based on sample meta attributes
              /*var $header = $("#sampleTableHeader tr");
              $header.empty();
              headerList = [];

              $header.append("<th><input type='checkbox' id='selectAllSamples' onchange='selectSamples();'/></th>")
                  .append("<th>ID</th>")
                  .append("<th>Parent</th>")
                  .append("<th>User</th>")
                  .append("<th>Date</th>");

              $.each(v1, function(i2,v2) {
                if(v2) {
                  $header.append("<th>" + v2 + "</th>");
                }
                headerList.push(v2);
              });*/
            } else if(v1 && i1 == 2){  //Assign lookup value/type to global var
              attributeTypeMap = v1;
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
  return rtnVal;
}

function createSampleDataTable(){
  //Destroy current data table to create new one
  if(typeof sDT !== 'undefined') {
    sDT.fnDestroy();
    $("#sampleTableBody").empty();
    utils.processing(false);
  }

  <!-- SAMPLE TABLE -->
  sDT = $("#sampleTable").dataTable({
    "language": {
      "processing": "Processing your request. Please wait..."
    },
    "scrollX": true,
    "bProcessing": true,
    "bServerSide": true,
    "bDestroy": true,
    "sPaginationType": "full_numbers",
    "sAjaxSource": "",
    "iDeferLoading": 0, // no initial loading flag
    "fnServerData": function (sSource, aoData, fnCallback) {
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
      if(headerList){
        for(var i=0; i < headerList.length; i++){
          aoData.push({"name":"attributeList[" + i + "]", "value":headerList[i]});
        }
      }
      if(sSource!=='') {
        var resultingHeaders = ["ID", "Parent", "User", "Date"];

        $.ajax({
          dataType: 'json',
          type: "POST",
          url: sSource,
          data: aoData,
          success: function(json) {
            var rows = [];
            if(json && json.aaData) {
              $.each(json.aaData, function(ri,rowData) {
                var row = [], attributes;
                row.push(
                    "<span id='rowDetail_openBtn' class='glyphicon glyphicon-plus-sign' aria-hidden='true' style='color:green;cursor: pointer;margin-left: 2px;'></span>",
                    rowData.sampleName + "<input type='checkbox' class='" + rowData.status + "' style='margin-left:6px;' id='sampleCB_" + rowData.sample.sampleId + "[" + rowData.event + "]'/>",
                    rowData.event.replace("Registration", ''),
                    rowData.parentSampleName,
                    rowData.actor,
                    rowData.createdOn
                );
                if(rowData.attributes) {
                  var headers = '', values = '';
                  $.each(rowData.attributes, function(ai, av) {
                    if(resultingHeaders.indexOf(ai) < 0) {
                      resultingHeaders.push(ai);
                    }
                    headers += '<td>' + ai + '</td>';
                    values += '<td>' + av + '</td>';
                  });
                  attributes = '<tr class="even">' + headers + '</tr><tr class="odd">' + values + '</tr>';
                } else {
                  attributes = '<tr class="odd"><td colspan="6">No Data</td></tr>';
                }
                row.push(attributes);

                rows.push(row);
              });

            }
            json.aaData = rows;
            fnCallback(json);
          },
          complete: function() {
            $('#sampleTable_filter').parent("div.col-sm-6").attr('class', 'col-sm-8').insertBefore($('#sampleTable_length').parent("div.col-sm-6"));
            $('#sampleTable_length').parent("div.col-sm-6").attr('class', 'col-sm-4').css("text-align", "right")
            $('#sampleTable_filter').css("float", "left")

            $('#projectTableDivHeader, #sampleTableDivHeader, #sampleTableDiv, refreshDataBtn').show();
            utils.processing(false);
            utils.error.remove();

            fixHeaderAlignment();

            $('.sampleCB').change(function(){ //".checkbox" change
              //uncheck "select all", if one of the listed checkbox item is unchecked
              if(this.checked == false){ //if this item is unchecked
                $("#selectAllSamples")[0].checked = false; //change "select all" checked status to false
              }

              //check "select all" if all checkbox items are checked
              if ($('.sampleCB:checked').length == $('.sampleCB').length ){
                $("#selectAllSamples")[0].checked = true; //change "select all" checked status to true
              }
            });
            // add any new attribute names to the global header list to populate dropdown list in the advanced search
            /*$.each(resultingHeaders, function(i, h) {
              if(advanceSearch.headerList.indexOf(h) < 0) {
                advanceSearch.addToHeaderList(h);

                // add the header to existing search dropdowns
                $("[id^=select_column_]").append($('<option/>').val(h).text(h));
              }
            });*/
          }
        });
      }
    },
    "aaSorting": [[5,'asc']],
    "bAutoWidth" : false,
    "aoColumnDefs": aoColumns()
  }).fnFilterOnReturn();

  sDT.fnAdjustColumnSizing();

  var buttons = new $.fn.dataTable.Buttons(sDT, {
    buttons: [  'copyHtml5', 'excelHtml5', 'csvHtml5', 'pdfHtml5', 'colvis' ]
  }).container().appendTo($('#buttons'));

  $(".dataTables_filter").append(
      $('<span/>').attr({
        'class': 'input-group-btn'
      }).append(
          $('<button/>').attr({
            'type': 'button',
            'class': 'btn btn-default btn-sm',
            'id': 'triggerSearchBtn',
            'onclick': 'triggerSearch();'
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
            'id': 'selectAllBtn',
            'onclick': '_page.select.all();',
            'style': 'margin-left:10px;'
          }).text("Select All")
      ).append(
          $('<button/>').attr({
            'type': 'button',
            'class': 'btn btn-default btn-sm',
            'id': 'deSelectAllBtn',
            'onclick': '_page.deselect.all();',
            'style': 'margin-left:10px;'
          }).text("Deselect All")
      ).append(
          $('<button/>').attr({
            'type': 'button',
            'class': 'btn btn-default btn-sm',
            'id': 'columnFilterBtn',
            'data-original-title': 'Column Filter',
            'data-toggle': 'tooltip',
            'data-placement': 'right',
            'name':'showColumnFilter',
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

  generateColumnFilter();
}

var fixed = false;
function fixHeaderAlignment() {
  if(!fixed) {
    sDT.fnFilterClear();
    fixed = true;
  }
}

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

  $columnFilterSelect.append($("<option></option>").attr("value", "Sample Name").text("ID"));
  $columnFilterSelect.append($("<option></option>").attr("value", "Parent").text("Parent"));
  $columnFilterSelect.append($("<option></option>").attr("value", "User").text("User"));
  $columnFilterSelect.append($("<option></option>").attr("value", "Date").text("Date"));

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

function triggerSearch(){
  var searchVal = $("#sampleTable_filter > label > input").val();
  sDT.fnFilter(searchVal);
}

function aoColumns() {
  var ao = [];
  ao.push({"aTargets":[0], "bSortable": false, "sWidth" : "10px"});
  return ao;
}

function selectSamples() {
  $(".sampleCB").prop('checked', $("#selectAllSamples").prop('checked'));
}