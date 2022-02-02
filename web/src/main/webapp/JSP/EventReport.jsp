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
    tr.odd td, tr.even td { padding: 6px 8px; margin: 0; vertical-align: top; }
    .checkbox{margin-top: 0px;}
    .hidden_event { display: none; }
    .border-bottom { border-bottom: 1px solid #abb2b9; margin-bottom: 5px; width: 95%;}
  </style>
</head>
<body class="smart-style-2">
<div id="container">
  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <s:form id="eventReportPage" name="eventReportPage" namespace="/" action="eventReport" method="post" theme="simple">
          <div class="page-header">
            <h1>Event Report</h1>
          </div>

          <div id="HeaderPane">
            <!-- <div class="panelHeader">Event Report</div> -->
            <div id="errorMessagesPanel" style="margin-top:15px;"></div>
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
            <div id="statusTableDiv">
              <div id="tableTop">
                <div class="row">
                  <div class="col-md-2">Project Name</div>
                  <div class="col-lg-5 input-group">
                    <s:select id="_projectSelect" list="projectList" name="selectedProjectId" headerKey="0" headerValue="" listValue="projectName" listKey="projectId" required="true"/>
                  </div>
                </div>
              </div>

              <div id="attributesTableHeader" style="margin-top:25px;">
                <h1 class="csc-firstHeader middle-header">Attributes</h1>
              </div>
              <div id="attributesTableDiv">
                <div class="row" style="margin-bottom:25px;">
                  <div class="col-sm-1" style="padding-top:7px;">Date Range</div>
                  <div class="col-sm-2">
                    <div class="input-group col-sm-12">
                      <input id="fromDate" name="fromDate" type="text" class="form-control" style="position: initial" placeholder="from"/>
                      <label for="fromDate" class="input-group-addon">
                        <span class=""><i class="fa fa-calendar"></i></span>
                      </label>
                    </div>
                  </div>
                  <div class="col-sm-1" style="width:auto;padding-top:7px;">~</div>
                  <div class="col-sm-2">
                    <div class="input-group col-sm-11">
                      <input id="toDate" name="toDate" type="text" class="form-control"  style="position: initial" placeholder="to"/>
                      <label for="toDate" class="input-group-addon">
                        <span class=""><i class="fa fa-calendar"></i></span>
                      </label>
                    </div>
                  </div>
                </div>
                <table name="attributesTable" id="attributesTable" class="contenttable" style="width:95%;">
                  <tbody id="attributesTableBody">
                  <tr class="even">
                    <td width="5%"><label>Project</label></td>
                    <td colspan="2" id="projectMetaAttributesTD"></td>
                  </tr>
                  <tr class="odd">
                    <td width="5%"><label>Subject</label></td>
                    <td colspan="2" id="sampleMetaAttributesTD"></td>
                  </tr>
                  <tr class="even">
                    <td width="5%"><label>Event</label></td>
                    <td id="eventMetaAttributesTD" ></td>
                  </tr>
                  </tbody>
                </table>
              </div>
              <div id="submitDiv" style="margin:15px 0 0 0;">
                <input type="button" class="btn btn-primary" onclick="javascript:open_status_page();" id="eventReportPageButton" value="Generate Status Page"/>
                <input type="button" class="btn btn-default" style="margin-left:15px;" onclick="javascript:doClear();" value="Clear" />
              </div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />
<script>
  $(document).ready(function() {
    $('.navbar-nav li').removeClass('active');
    $('.navbar-nav > li:nth-child(5)').addClass('active');

    $('#fromDate, #toDate').datepicker({ dateFormat: 'yy-mm-dd' });
    $( "#_projectSelect" ).combobox();
    utils.error.check();
    $("#submitDiv").hide();
    $("#attributesTableDiv").hide();
    $("#attributesTableHeader").hide();
  });

  var event_types = [];
  var openBtn = "glyphicon-plus-sign",
      closeBtn = "glyphicon-minus-sign";
  var _selAllEventHtml = '';

  var h_s = {
            bt: '<button name="selection" class="btn btn-default" id="$a$SelOne" type="button"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></button><button name="selection" class="btn btn-default" id="$b$SelAll" type="button"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></button><br><br>' +
                '<button name="selection" class="btn btn-default" id="$c$DeOne" type="button"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button><button name="selection" class="btn btn-default" id="$d$DeAll" type="button"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button>',
            fl: '<tr><td><div class="input-group"><input type="text" class="form-control" id="filter$f$" placeholder="Type for attribute..." style="position: initial" ><span class="input-group-btn"><button class="btn btn-default" type="button" disabled=""><span class="glyphicon glyphicon-search" aria-hidden="true"></span></button></span></div></td><td></td>' +
                '<td><div class="input-group"><input type="text" class="form-control" id="defilter$f$" placeholder="Type for attribute..." style="position: initial" ><span class="input-group-btn"><button class="btn btn-default" type="button" disabled=""><span class="glyphicon glyphicon-search" aria-hidden="true"></span></button></span></div></td></tr>',
            sl: '<select class="form-control" name="$s$" id="$g$" size="5" multiple="multiple">',
            opt: '<option value="$v$">$v$</option>',
            cb: '<div class="checkbox"><label><input type="checkbox" name="$g$Attr" value="$v$">$v$</label></div>'
          },
          callbacks = {
            meta: function(data) {
              $.each(data, function(_i, _o) {
                if(_o) {
                  var keys = ['project', 'sample', 'event'];
                  $.each(keys, function(k_i, k) {
                    var values = _o[k], _html='';
                    if(values) {
                      if(k==='event') {
                        $.each(values, function(et, attrs) {
                          var et_us = et.replace(/ /g, "_");
                          event_types.push(et_us.toLowerCase());
                          _html+='<sort><a href="javascript:buttonSwitch(null, \'expand_'+et_us.toLowerCase()+'\');" class="col-sm-1" style="width: 4%"><span name="expandEvents" id="expand_'+et_us.toLowerCase()+'" class="glyphicon glyphicon-plus-sign" aria-hidden="true" style="color:green;cursor: pointer;"></span></a>' +
                              '<div class="checkbox col-sm-11"><label><input class="ev_attr" type="checkbox" name="sAll" id="'+et_us.toLowerCase()+'" value="'+et+'">'+et+'</label></div>' +
                              '<div class="hidden_event col-sm-12" name="'+et_us.toLowerCase()+'" id="'+et_us+'"><table><tr>';
                          $.each(attrs, function(a_i, a) {
                            _html+= '<td>' + h_s.cb.replace('$g$',k).replace(/\$v\$/g, a) + '</td>';
                            if(a_i!=0 && a_i%4==0) {
                              _html += '</tr><tr>';
                            }
                          });
                          _html+='</tr></table></div><br></sort>';
                        });

                        if(_html.length>0) {
                          /*Will be added after sorting*/
                          _selAllEventHtml ='<div class="row"><sort><br><div class="checkbox col-sm-11"><label><input type="checkbox" name="sAll" value="'+k+'">Select All '+k+'</label></div></sort></div>';
                        }
                      } else {
                        _html += '<table>';
                        _html += h_s.fl.replace(/\$f\$/g, k).replace('$g$',k);
                        _html += '<tr><td>';
                        _html += h_s.sl.replace('$s$', 'select').replace('$g$', '_' + k);
                        $.each(values, function(a_i,a) {
                          _html += h_s.opt.replace(/\$v\$/g, a);
                        });
                        _html += '</select></td><td style="padding-top: 25px;padding-right: 25px;">';
                        _html += h_s.bt.replace('$a$',k).replace('$b$',k).replace('$c$',k).replace('$d$',k);
                        _html += '</td><td>';
                        _html += h_s.sl.replace('$s$', 'selected').replace('$g$', 'selected_' + k) + '</select></td></tr></table>';
                      }

                      $('#'+k+'MetaAttributesTD').html(_html);
                    }
                  });
                }
              });

              $('input:checkbox[name=sAll]').live('click', function() {
                var type=$(this).val(), checked=$(this).is(':checked');
                if(type === 'project' || type === 'sample' || type === 'event') {
                  $('input:checkbox[name=' + type + 'Attr]').attr('checked', checked);

                  if(type === 'event'){
                    for(var i = 0; i < event_types.length; i++) {
                      $('input:checkbox[id='+ event_types[i]+']').attr('checked', checked);
                    }
                  }
                } else{
                  var type_us = type.replace(/ /g, "_");
                  $('#'+type_us+' input[type="checkbox"]').attr('checked', checked);
                }
              });

              $('select[name=select]').on('dblclick', 'option', function () {
                var id = $(this).parent('select').attr('id');

                moveOptionUpdateFilter('#' + id, '#selected' + id, false);
              });

              $('select[name=selected]').on('dblclick', 'option', function () {
                var id = $(this).parent('select').attr('id');

                if(id.indexOf('project') > -1){
                  moveOptionUpdateFilter('#' + id, '#_project', false);
                } else {
                  moveOptionUpdateFilter('#' + id, '#_sample', false);
                }
              });

              $('button[name=selection]').click(function(){
                var id = $(this).attr('id');
                var cObj = '', pObj = '';
                var type = (id.indexOf('project') >  -1) ? 'project' : 'sample';
                var all = (id.indexOf('One') >  -1) ? false : true;

                if(id.indexOf('Sel') >  -1){
                  cObj = '#_'+type;
                  pObj = '#selected_'+type;
                } else {
                  cObj = '#selected_'+type;
                  pObj = '#_'+type;
                }

                moveOptionUpdateFilter(cObj, pObj, all);
              });

              $('#_project').filterByText($('#filterproject'));
              $('#_sample').filterByText($('#filtersample'));

              $('#selected_project').filterByText($('#defilterproject'));
              $('#selected_sample').filterByText($('#defiltersample'));

              $("#submitDiv").show();
              $("#attributesTableDiv").show();
              $("#attributesTableHeader").show();

              /*Sorting events in an alphabetical order*/
              sortEvents();

              /*Select Project Name and Sample Name by default*/
              moveOptionByValue("#_project", "#selected_project", "Project Name");
              moveOptionByValue("#_sample", "#selected_sample", "Sample Name");
            }
          };

  function moveOptionUpdateFilter(cObj, pObj, all){
    var selected = $(cObj);
    var target = $(pObj);
    var action = (all === true) ? 'option' : 'option:selected';

    var options = selected.data('options');
    var selectedVal = [];
    $(cObj + ' > ' + action).each(function () {
      selectedVal.push($(this).val());
    });

    var tempOption = [];
    $.each(options, function (i) {
      var option = options[i];
      var check = false;
      for(var j = 0; j < selectedVal.length; j++){
        if (option.value == selectedVal[j]) {
          check = true;
        }
      }
      if(!check) tempOption.push(option);
    });

    var targetOptions = target.data('options');
    $(cObj + ' > ' + action).each(function () {
      targetOptions.push({value: $(this).val(), text: $(this).text()});
    });

    target.data('options', targetOptions);

    selected.find(action).remove().appendTo(pObj);
    selected.data('options', tempOption);
  }

  function moveOptionByValue(org, dest, val){
    var selected = $(org);
    var target = $(dest);
    var options = selected.data('options');
    var selectedVal = val;
    var tempOption = [];

    $.each(options, function (i) {
      var option = options[i];
      if (option.value != selectedVal) {
        tempOption.push(option);
      }
    });

    var targetOptions = target.data('options');
    targetOptions.push({value: val, text: val});

    target.data('options', targetOptions);

    selected.find('option[value="'+val+'"]').remove().appendTo(dest);
    selected.data('options', tempOption);
  }

  function sortEvents(){
    var eventGroup = $('#eventMetaAttributesTD'),
            els    = $('sort', eventGroup).get(),
            sorted = els.sort(function(a, b) {
              return $(a).text().toUpperCase()
                      .localeCompare( $(b).text().toUpperCase() );
            });

    $.each(sorted, function(idx, itm) {
      eventGroup.append($("<div/>").attr({"class": "row border-bottom"}).append(itm));
    });
    eventGroup.append(_selAllEventHtml);
  }

  function buttonSwitch(node, id) {
    if(node==null) { node = document.getElementById(id); }
    var attr_name = id.replace("expand_", "");
    if(node.classList.contains(openBtn)){
      $('[name="'+attr_name+'"]').show();
      node.classList.remove(openBtn);
      node.classList.add(closeBtn);
      node.style.color = "red";
    } else {
      $('[name="'+attr_name+'"]').hide();
      node.classList.remove(closeBtn);
      node.classList.add(openBtn);
      node.style.color = "green";
    }
  }

  function comboBoxChanged(option) {
    if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
      makeAjax("ma", option.value, callbacks.meta);
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
      data: "type="+type+"&projectId="+projectId+"&sampleId=0&eventId=0&subType=DOD",
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
    //Clear filter
    var defiltersample = $('#defiltersample');
    var defilterproject = $('#defilterproject');
    defilterproject.val('');
    defilterproject.keyup();
    defiltersample.val('');
    defiltersample.keyup();

    var attributesValue = "";

    $.each($('select[name=selected] option'), function() {
      attributesValue += $(this).val() + ',';
    });

    $.each($('input:checkbox'), function() {
      if($(this).is(':checked') && $(this).val()!=='project' && $(this).val()!=='sample' && $(this).val()!=='event'
              && $(this).attr('class') !== 'ev_attr')
        attributesValue+=$(this).val()+',';
    });

    var $statusForm = $('<form>').attr({
      id: 'statusForm',
      method: 'POST',
      target: '_blank',
      action: 'productionStatus.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'projectNames',
      name: 'projectNames',
      value : $('#_projectSelect option:selected').text()
    }).appendTo($statusForm);

    $('<input>').attr({
      id: 'attributes',
      name: 'attributes',
      value : attributesValue
    }).appendTo($statusForm);

    $('body').append($statusForm);
    $statusForm.submit();

    // window.open("productionStatus.action?iss=true&projectNames="+$('#_projectSelect option:selected').text()+"&attributes="+attributes);
  }

  function doClear() {
    $("#_projectSelect").val(0);
    $("input.ui-autocomplete-input").val('');
    $("#projectMetaAttributesTD").html('');
    $("#sampleMetaAttributesTD").html('');
    $("#eventMetaAttributesTD").html('');
    $("#fromDate_datepicker").val('');
    $("#toDate_datepicker").val('');
    $("#submitDiv").hide();
    $("#attributesTableDiv").hide();
    $("#attributesTableHeader").hide();
  }

  jQuery.fn.filterByText = function(textbox) {
    return this.each(function() {
      var select = this;
      var options = [];
      $(select).find('option').each(function() {
        options.push({value: $(this).val(), text: $(this).text()});
      });
      $(select).data('options', options);
      $(textbox).bind('change keyup', function() {
        var options = $(select).empty().scrollTop(0).data('options');
        var search = $.trim($(this).val());
        var regex = new RegExp(search,'gi');

        $.each(options, function(i) {
          var option = options[i];
          if(option.text.match(regex) !== null) {
            $(select).append(
                    $('<option>').text(option.text).val(option.value)
            );
          }
        });
      });
    });
  };
</script>
</body>
</html>
