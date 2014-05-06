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
  <link rel="stylesheet" href="style/dataTables.css" />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" />
  <style>
    #popup {
      height: 100%;
      width: 100%;
      background: #000000;
      position: absolute;
      top: 0;
      -moz-opacity:0.75;
      -khtml-opacity: 0.75;
      opacity: 0.75;
      filter:alpha(opacity=75);
    }

    #window {
      width: 600px;
      height: 300px;
      margin: 0 auto;
      border: 1px solid #000000;
      background: #ffffff;
      position: absolute;
      top: 200px;
      left: 25%;
    }
    td._details {
      text-align:left;
      padding:0 0 0 35px;
      border: 1px gray dotted;
    }
    td._details div { position: relative; overflow: auto; overflow-y: hidden; }
    td._details table td { border:1px solid white; }

    .datatable_top, .datatable_table, .datatable_bottom { float:left; clear:both; width:100%;}
  </style>
</head>
<body>
<s:form id="eventDetailPage" name="eventDetailPage" namespace="/" action="eventDetail" method="post" theme="simple">
  <s:hidden id="editable" name="editable" value="0" />
  <s:include value="TopMenu.jsp" />
  <div id="HeaderPane" style="margin:15px 0 0 30px;">
    <div class="panelHeader">Event Detail</div>
    <div id="errorMessagesPanel" style="margin-top:15px;"></div>
    <s:if test="hasActionErrors()">
      <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
    </s:if>
    <s:if test="hasActionMessages()">
      <div class="alert_info" onclick="$('.alert_info').remove();">
        <strong><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
      </div>
    </s:if>
  </div>
  <div id="middle_content_template">
    <!--<div id="columnsTable"></div>  for column listing-->
    <div id="statusTableDiv">
      <div id="tableTop">
        <table>
          <tr>
            <td align="right">Project</td>
            <td class="ui-combobox">
              <s:select label="Project" id="_projectSelect" cssStyle="width:150px;margin:0 5 0 10;"
                        list="projectList" name="selectedProjectId" headerKey="0" headerValue=""
                        listValue="projectName" listKey="projectId" required="true"/>
            </td>
          </tr>
          <tr>
            <td align="right">Sample</td>
            <td class="ui-combobox">
              <s:select id="_sampleSelect" cssStyle="margin:0 5 0 10;" list="#{'0':''}"
                        name="selectedSampleId" required="true"/>
            </td>
          </tr>
        </table>
      </div>
      <div style="margin:25px 10px 0 0;"><h1 class="csc-firstHeader">Project Details</h1></div>
      <div id="projectTableDiv" style="margin:0 10px 5px 0;">
        <table name="projectTable" id="projectTable" class="contenttable" style="width:95%;">
          <tbody id="projectTableBody">
          </tbody>
        </table>
        <input onclick="_page.popup.project();" style="margin-top:10px;" disabled="true" type="button" value="Edit Project" id="editProjectBtn" />
      </div>
      <div style="margin:25px 10px 0 0;">

        <h1 class="csc-firstHeader">Sample Details
          <a href="javascript:$('#sampleTableDiv').toggle(400);buttonSwitch(null, 'sampleToggleImage');">
            <img id="sampleToggleImage"/>
          </a>
        </h1>
      </div>
      <div id="sampleTableDiv" style="margin:0 10px 5px 0;clear:both">
        <table name="sampleTable" id="sampleTable" class="contenttable" style="width:95%;">
          <thead id="sampleTableHeader">
          <tr>
            <th style="width:23px !important;text-align:center"><img id="table_openBtn"/></th>
            <th class="tableHeaderStyle">Sample Name</th>
            <th class="tableHeaderStyle">Parent</th>
            <th class="tableHeaderStyle">User</th>
            <th class="tableHeaderStyle">Date</th>
            <th>Hidden</th>
          </tr>
          </thead>
          <tbody id="sampleTableBody"/>
        </table>
      </div>
      <div style="margin:25px 10px 0 0;">
        <h1 class="csc-firstHeader">Event Details
          <a href="javascript:$('#eventDateDiv').toggle(400);$('#eventTableDiv').toggle(400);buttonSwitch(null,'eventToggleImage');">
            <img id="eventToggleImage"/>
          </a>
        </h1>
      </div>
      <div id="eventDateDiv" style="margin:3px 10px 0 0;">
        Date Range:
        <s:textfield id="fromDate" name="fromDate"/> ~ <s:textfield id="toDate" name = "toDate"/>
      </div>
      <div id="eventTableDiv" style="margin:10px 10px 5px 0;clear:both">
        <table name="eventTable" id="eventTable" class="contenttable" style="width:95%;">
          <thead id="eventTableHeader">
          <tr>
            <th style="width:23px !important;text-align:center"><img id="table_openBtn"/></th>
            <th class="tableHeaderStyle">Event Type</th>
            <th class="tableHeaderStyle">Sample Name</th>
            <th class="tableHeaderStyle">Date</th>
            <th class="tableHeaderStyle">User</th>
            <th class="tableHeaderStyle">Status</th>
            <th>Hidden</th>
          </tr>
          </thead>
          <tbody id="eventTableBody" />
        </table>
        <div/>
      </div>
    </div>
  </div>

</s:form>

<script src="scripts/jquery/jquery.dataTables.js"></script>
<script>
var openBtn = "images/dataTables/details_open.png",
    closeBtn = "images/dataTables/details_close.png",
    subrow_html='<div><table cellpadding="6" cellspacing="0">$d$</table></div>';

var sDT, //sample detail table
    eDT; //event detail table

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
            _page.change.sample(projectId, sampleId?sampleId:0)
            $("#editProjectBtn").attr("disabled", ($("#editable").val()!=1));
          } else {
            //reset
            $("#_sampleSelect").html(vs.empty);
            $("tbody#projectTableBody").empty();
            if(sDT && eDT) {
                sDT.fnNewAjax("eventDetailAjax.action?type=sdt&projectId=0");
                eDT.fnNewAjax("eventDetailAjax.action?type=sdt&projectId=0");
            }
            $("#editProjectBtn").attr("disabled", "true");
            utils.error.add("You do not have permission to access the project.");
          }
        },
        sample: function(projectId, sampleId) {
          _page.get.sdt(projectId, sampleId);
          _page.get.edt(projectId, sampleId, 0);
        },
        event: function(eventId) {
          gethtmlByType("ces", 0, 0, eventId);
        }
      },
      popup: {
        project: function() {
          $.openPopupLayer({
            name: "LPopupProjectEdit",
            width: 450,
            url: "projectEditOpen.action?projectId="+$('#_projectSelect').val()
          });
        },
        sample: function(sampleId) {
          $.openPopupLayer({
            name: "LPopupSampleEdit",
            width: 450,
            url: "sampleEditOpen.action?projectId="+$('#_projectSelect').val()+"&sampleId="+sampleId
          });
        }
      },
      get: {
        project: function(projectId) {
          return gethtmlByType("Project", projectId, 0, 0);
        },
        sample: function(projectId) {
          return gethtmlByType("Sample", projectId, 0, 0);
        },
        sdt: function(projectId, sampleId) {
          $('#fromDate, #toDate').val('');
          sDT.fnNewAjax("eventDetailAjax.action?type=sdt&projectId="+projectId+"&sampleId="+sampleId+"&eventId=0");
        },
        edt: function(projectId, sampleId, eventId) {
          var dates = "&fd="+$('input[name="fromDate"]').val()+"&td="+$('input[name="toDate"]').val();
          eDT.fnNewAjax("eventDetailAjax.action?type=edt&projectId="+projectId+"&sampleId="+sampleId+"&eventId=0"+dates);
        }
      },
      callbacks: {
        project: function() {},
        sample: function() {},
        event: function() {}
      }
    },
    openSampleEditPopup = function(sampleId) {
      _page.popup.sample(sampleId);
    },
    changeEventStatus = function(eventId) {
      _page.change.event(eventId);
    },
    buttonSwitch = function(node, name) {
      if(node==null) { node = document.getElementById(name); }
      if(node.src.match('details_close')){ node.src = openBtn; } else { node.src = closeBtn; }
    };


function comboBoxChanged(option, id) {
  if(id==='_projectSelect') {
    document.getElementById('sampleTable').getElementsByTagName('img')[0].src = openBtn;
    document.getElementById('eventTable').getElementsByTagName('img')[0].src = openBtn;
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

<!-- Generate html content using Ajax by type -->
function gethtmlByType(ajaxType, projectId, sampleId, eventId) {
  var content = '', rtnVal = false;
  $.ajax({
    url:"sharedAjax.action",
    cache: false,
    async: ajaxType==='Project'?false:true,
    data: "type="+ajaxType+"&projectId="+projectId+"&sampleId="+sampleId+"&eventId="+eventId,
    success: function(html){
      if(html.aaData) {
        if(ajaxType == "Project") {
          $(html.aaData).each(function(i1,v1) {
            if(v1) {
              $.each(v1, function(i2,v2) {
                if(v2) {
                  if(i2==="editable") { $("#editable").val(v2); }
                  else { content += '<tr class="even"><td width="25%">'+i2+'</td><td>'+v2+'</td></tr>'; }
                }
              });
              rtnVal = true;
            }
          });
          $("tbody#projectTableBody").html(content);
        } else if(ajaxType == "Sample") {
          var list = vs.alloption;
          $(html.aaData).each(function(i1,v1) {
            if(v1) { list += vs.vnoption.replace("$v$",v1.id).replace("$n$",v1.name); }
          });
          if(sampleId == null || sampleId == 0) { $("#_sampleSelect").html(list); }

        } else if(ajaxType == "ces") {
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

$(document).ready(function() {

  utils.combonize('statusTableDiv');
  utils.initDatePicker();
  $('#fromDate, #toDate').change( function() {
    _page.get.edt($('#_projectSelect').val(), $('#_sampleSelect').val());
  });
  $('#eventToggleImage, #sampleToggleImage, #table_openBtn').attr('src', openBtn);

  <!-- SAMPLE TABLE -->
  sDT = $("#sampleTable").dataTable({
    "sDom": '<"datatable_top"lf><"datatable_table"rt><"datatable_bottom"ip>',
    "bProcessing": true,
    "bServerSide": true,
    "sPaginationType": "full_numbers",
    "sAjaxSource": "",
    "fnServerData": function (sSource, aoData, fnCallback) {
      if(sSource!=='') {
        $.ajax({
          dataType: 'json',
          type: "POST",
          url: sSource,
          data: aoData,
          success: function(json) {
            fnCallback(json);
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
  }).fnFilterOnReturn();

  <!-- EVENT TABLE -->
  eDT =  $("#eventTable").dataTable({
    "sDom": '<"datatable_top"lf><"datatable_table"rt><"datatable_bottom"ip>',
    "bProcessing": true,
    "bServerSide": true,
    "sPaginationType": "full_numbers",
    "sAjaxSource": "",  
    "fnServerData": function ( sSource, aoData, fnCallback ) {
      if(sSource!=='') {
        $.ajax({
          dataType: 'json',
          type: "POST",
          url: sSource,
          data: aoData,
          success: function(json) {
            fnCallback(json);
          }
        });
      }
    },
    "bAutoWidth" : false,
    "aoColumnDefs": [
      {"sWidth": "23px", "bSortable": false, "aTargets": [ 0 ]},
      {"sWidth": "20%", "aTargets":[1]},
      {"sWidth": "30%", "aTargets":[2]},
      {"sWidth": "20%", "aTargets":[3]},
      {"sWidth": "20%", "aTargets":[4]},
      {"sWidth": "10%", "aTargets":[5]},
      {"bSearchable": true, "bVisible": false, "aTargets": [ 6 ]}
    ]
  }).fnFilterOnReturn();

  //add click listener on row expander
  $('tbody td #rowDetail_openBtn').live('click', function () {
    var _row = this.parentNode.parentNode, _is_event=(_row.parentNode.id.indexOf('event')>=0), _table=_is_event?eDT:sDT;
    if(this.src.indexOf('details_close')>=0){
      this.src = openBtn;
      _table.fnClose(_row);
    } else {
      this.src = closeBtn;
      _table.fnOpen(_row, subrow_html.replace(/\\$d\\$/, _table.fnGetData(_row)[(_is_event?6:5)]), '_details');
      $('td._details>div').css('width', $('#projectTableDiv').width()-90);
    }
  });

  $('thead #table_openBtn').live('click', function () {
    var _is_event=this.parentNode.parentNode.parentNode.id.indexOf('event')>=0;
    $('#'+(_is_event?'eventTable':'sampleTable')+' #rowDetail_openBtn').click();
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

  //add search button to filter box
  $('.dataTables_filter[id$="_filter"]').each(function(i1) {
    $(this).append('<img id="dosearch_'+this.id+'" style="float:right;" class="ui-icon ui-icon-search" title="Search" />&nbsp;');
  });
  $('#sampleTableDiv, #eventTableDiv, #eventDateDiv').toggle(300);

  utils.error.check();
});


</script>
</body>
</html>

