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
    <jsp:include page="../html/header.html" />
    <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
    
    <link rel="stylesheet" href="style/version01.css" />
    <style>
      tr.even { padding: 2px; background-color: #e9e9e9; }
      tr.odd { padding: 2px; background-color: #f5f5f5; }
      tr.odd td, tr.even td { padding: 6px 8px; margin: 0; vertical-align: top; }
      .chkbox { margin:5px 5px 5px 15px !important; }
      #attributesTableBody > tr > td:first-child {
        border-right: 1px solid white;
      }
    </style>
  </head>

  <body class="smart-style-2">
    <div id="container">

      <jsp:include page="top.jsp" />

      <div id="main" class="">
        <div id="inner-content" class="">
          <div id="content" role="main">

            <s:form id="eventReportPage" name="eventReportPage" namespace="/" action="eventReport" method="post" theme="simple">
            
              <div class="page-header">
                <h1>Event Report</h1>
              </div>

              <div id="HeaderPane" style="margin:15px 0 0 30px;">
                <!-- <div class="panelHeader">Event Report</div> -->
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
              <div id="mainContent">
                <div id="statusTableDiv">
                  <div id="tableTop">
                    <div class="row">
                      <div class="col-md-1">Project</div>
                      <div class="col-md-11 combobox">
                        <s:select id="_projectSelect" list="projectList" name="selectedProjectId" headerKey="0" headerValue="" listValue="projectName" listKey="projectId" required="true"/>  
                      </div>
                    </div>
                    <div class="row row_spacer">
                      <div class="col-md-1">Date Range</div>
                      <div class="col-md-11 combobox">
                        <s:textfield id="fromDate" name="fromDate" label="from"/> ~ <s:textfield id="toDate" name = "toDate" label="to"/> 
                      </div>
                    </div>
                  </div>

                  <div style="margin-top:25px;">
                    <h1 class="csc-firstHeader">Attributes</h1>
                  </div>
                  <div id="attributesTableDiv">
                    <table name="attributesTable" id="attributesTable" class="contenttable" style="width:95%;">
                      <tbody id="attributesTableBody">
                      <tr class="even">
                        <td width="20%" style="padding:5px 0 5px 5px;">Project</td>
                        <td style="padding:5px 0 5px 0;" colspan="2" id="projectMetaAttributesTD"></td>
                      </tr>
                      <tr class="odd">
                        <td width="20%" style="padding:5px 0 5px 5px;">Sample</td>
                        <td style="padding:5px 0 5px 0;" colspan="2" id="sampleMetaAttributesTD"></td>
                      </tr>
                      <tr class="even">
                        <td width="20%" style="padding:5px 0 5px 5px;">Event</td>
                        <td style="padding:5px 0 5px 0;" id="eventMetaAttributesTD" ></td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                  <div id="submitDiv" style="margin:15px 0 0 0;">
                    <input type="button" class="btn btn-success" onclick="javascript:open_status_page();" id="eventReportPageButton" value="Generate Status Page"/>
                    <input type="button" class="btn btn-default" style="margin-left:15px;" onclick="javascript:doClear();" value="Clear" />
                  </div>
                </div>
              </div>
            </s:form>

          </div>
        </div>
      </div>

      <jsp:include page="../html/footer.html" />

    </div>
  
    <script src="scripts/jquery/jquery.dataTables.js"></script>
    <script>
      $(document).ready(function() {
        utils.initDatePicker();
        $( "#_projectSelect" ).combobox();
        utils.error.check();
      });

      var h_s = {
            cb: '<input class="chkbox" type="checkbox" name="$g$Attr" value="$v$"/><label class="checkboxLabel">$v$</label>'
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
                          _html+='<div><strong>'+et+'</strong></div><div>';
                          $.each(attrs, function(a_i, a) {
                            _html+=h_s.cb.replace('$g$',k).replace(/\\$v\\$/g, a);
                            if(a_i!=0 && a_i%4==0) {
                              _html += '<br/>';
                            }
                          });
                          _html+='</div>';
                        });
                      } else {
                        $.each(values, function(a_i,a) {
                          _html+=h_s.cb.replace('$g$',k).replace(/\\$v\\$/g, a);
                          if(a_i!=0 && a_i%4==0) _html += '<br/>';
                        });
                      }
                      if(_html.length>0) {
                        _html+= '<input class="chkbox" type="checkbox" name="sAll" value="'+k+'"/>'
                            + '<label class="checkboxLabel"><b>Select All</b></label>';
                      }
                      $('#'+k+'MetaAttributesTD').html(_html);
                    }
                  });
                }
              });

              $('input:checkbox[name=sAll]').live('click', function() {
                var type=$(this).val(), checked=$(this).is(':checked');
                $('input:checkbox[name='+type+'Attr]').attr('checked', checked);
              });
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
          data: "type="+type+"&projectId="+projectId+"&sampleId=0&eventId=0&subType=A",
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
        var attributes = "";
        $.each($('input:checkbox'), function() {
          if($(this).is(':checked') && $(this).val()!=='project' && $(this).val()!=='sample' && $(this).val()!=='event')
            attributes+=$(this).val()+',';
        });
        window.open("productionStatus.action?iss=true&projectNames="+$('#_projectSelect option:selected').text()+"&attributes="+attributes);
      }

      function doClear() {
        $("#_projectSelect").val(0);
        $("#projectMetaAttributesTD").html('');
        $("#sampleMetaAttributesTD").html('');
        $("#eventMetaAttributesTD").html('');
        $("#fromDate_datepicker").val('');
        $("#toDate_datepicker").val('');
      }
    </script>
  </body>
</html>

