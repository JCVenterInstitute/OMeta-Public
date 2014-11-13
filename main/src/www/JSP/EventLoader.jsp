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
    <link rel="stylesheet" href="style/multiple-select.css" type='text/css' media='all' />

    <link rel="stylesheet" href="style/version01.css" />
    <style>
      .loadRadio { margin-left: 10px; margin-right: 3px; }
      #gridBody .ui-autocomplete-input { width: 150px; }
      .gridIndex { max-width: 20px !important; min-width: 15px; text-align: center;}
      .ms-choice {line-height: 20px; }
      .ms-choice, .ms-choice > div { height: 20px; }
    </style>
  </head>

  <body class="smart-style-2">
    <div id="container">

      <jsp:include page="top.jsp" />

      <div id="main" class="">
        <div id="content" role="main">

          <s:form id="eventLoaderPage" name="eventLoaderPage" namespace="/" action="eventLoader" method="post" theme="simple" enctype="multipart/form-data">
            <s:hidden name="jobType" id="jobType"/>
            <s:hidden name="status" id="status"/>
            <s:hidden name="label" id="label"/>
            <s:hidden name="filter" id="filter"/>
            <s:hidden name="eventName" id="eventName" />
            <s:hidden name="projectName" id="projectName" />
            
            <div id="HeaderPane" style="margin:15px 0 0 30px;">
              <div class="panelHeader" id="pageTitle">Submit Data</div>
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

              <div id="statusTableDiv">
                <div id="tableTop">
                  <div class="row col-md-12"><h5><strong>Submission Information</strong></h5></div>
                  <div class="row">
                    <div class="col-md-1"><strong>Submit Data By:</strong></div>
                    <div class="col-md-11">
                      <input type="radio" name="loadType" class="loadRadio" value="form" id="r_sw" />
                      <label for="r_sw"><strong>Single Sample (Web Form)</strong></label>
                      <input type="radio" name="loadType" class="loadRadio" value="grid" id="r_mw" />
                      <label for="r_mw"><strong>Multiple Samples (Web Form)</strong></label>
                      <input type="radio" name="loadType" class="loadRadio" value="file" id="r_mf" />
                      <label for="r_mf"><strong>Multiple Samples (Excel Template)</strong></label>
                    </div>
                  </div>
                  <div class="row row_spacer" id="projectSelectRow">
                    <div class="col-md-1">Center Project</div>
                    <div class="col-md-11 combobox">
                      <s:select label="Project" id="_projectSelect" cssStyle="width:150px;margin:0 5 0 10;"
                                  list="projectList" name="projectId" headerKey="0" headerValue="--select center project--"
                                  listValue="projectName" listKey="projectId" required="true"/>  
                    </div>
                  </div>
                  <div class="row row_spacer">
                    <div class="col-md-1" id="eventTitle">Data Template</div>
                    <div class="col-md-11 combobox">
                      <s:select id="_eventSelect" list="#{0:'--select template--'}" name="eventId" required="true" disabled="true"/>
                    </div>
                  </div>
                  <!-- <div class="row row_spacer" id="sampleSelectRow">
                    <div class="col-md-1">Sample</div>
                    <div class="col-md-11 combobox">
                      <s:select id="_sampleSelect" cssStyle="margin:0 5 0 10;" list="#{'':''}" name="sampleName" required="true"/> 
                    </div>
                  </div> -->
                </div>
                <div id="projectDetailInputDiv">
                  <div style="margin:25px 10px 0 0;">
                    <h1 class="csc-firstHeader">Project Information</h1>
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
                <div id="sampleDetailInputDiv">
                  <div style="margin:25px 10px 0 0;">
                    <h1 class="csc-firstHeader">Sample Information</h1>
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


              <div class="row row_spacer">
                <div class="col-md-2">
                  <h5><strong>Data Submission</strong></h5>
                </div>
                <div style="font-size:0.9em;padding-top:15px;" class="col-md-10">
                  [<img style="vertical-align:bottom;" src="images/icon/req.png"/><img style="vertical-align:bottom;" src="images/icon/info_r.png"/>-Required, <img style="vertical-align:bottom;" src="images/icon/ontology.png"/>-Ontology]
                </div>
              </div>
              <div id="attributeInputDiv" style="clear:both;">
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
              <div id="gridInputDiv" style="margin:25px 10px 0 0 ;overflow-x: auto;">
                <table name="eventTable" id="eventTable" class="contenttable">
                  <thead id="gridHeader"></thead>
                  <tbody id="gridBody"></tbody>
                </table>
              </div>
              <div id="fileInputDiv" style="margin:25px 10px 0 0 ;">
                <table>
                  <tr>
                    <td>Loader CSV File</td>
                    <td>
                      <s:file name="dataTemplate" id="upload" cssStyle="margin:0 0 0 14px;" size="75px" />
                    </td>
                  </tr>
                </table>
              </div>

              <div id="submitDiv" style="margin:15px 10px 5px 0;width:100%;">
                <input type="button" class="btn btn-info" onclick="javascript:button.submit('save');" id="saveButton" value="Save Progress" disabled="true"/>
                <input type="button" class="btn btn-primary" onclick="javascript:button.submit('validate');" id="validateButton" value="Validate Submission" disabled="true"/>
                <input type="button" class="btn btn-success" onclick="javascript:button.submit('submit');" id="submitButton" value="Submit to DPCC" disabled="true"/>
                <input type="button" class="btn btn-default" onclick="javascript:button.add_event();" id="gridAddLineButton" value="Add Event Line" style="display:none;"/>
                <input type="button" class="btn btn-default" onclick="javascript:button.template();" id="templateButton" value="Download Template"/>
                <!-- <input type="button" class="btn btn-default" onclick="javascript:return;" id="exportButton" value="Export to .csv Template"/> -->
                <!-- <input type="button" onclick="javascript:button.clear_form();" value="Clear Form" /> -->
              </div>
            </div>
          </s:form>
        </div>
      </div>

      <jsp:include page="../html/footer.html" />
      
    </div>

    <script src="scripts/jquery/jquery.multiple.select.js"></script>
    
    <script>
      var g_eventAttributes = [];
      var g_gridLineCount=0;
      var g_avDic= {};
      var g_sampleIds;
      var g_transferType;
      var avHtml;
      var sample_options;

      var _utils = {
            makeAjax: function(u,d,p,cb) {
              $.ajax({
                url:u,
                cache: false,
                async: false,
                data: d,
                success: function(data){ cb(data,p); },
                fail: function() { alert("Ajax Process has failed."); }
              });
            },
            addGridRows: function(pn,en) {
              //have at least 5 event lines for the grid view
              for(var _rows=$('#gridBody > tr').length;_rows<5;_rows++) {
                button.add_event(pn,en);
              }
            },
            showPS: function(eventName) {
              $('#sampleSelectRow').hide();
              if(utils.checkSR(eventName)) { // triggers sample loader
                //$('#sampleDetailInputDiv').show();
              } else if(utils.checkPR(eventName)) {
                $('#projectDetailInputDiv').show();
              } else {
                if(utils.checkNP(eventName)) { //do not display sample select box for project events
                  $('#sampleSelectRow').show();
                }
              }
            },
            hidePS: function() {
              $('#sampleDetailInputDiv, #projectDetailInputDiv').hide();
            },
            ontologify: function(desc, $inputNode) {
              var ontologyInfo = desc.substring(desc.lastIndexOf('[')+1, desc.length-1).split(',');

              if(ontologyInfo.length === 2) {
                var tid = ontologyInfo[0].replace(/^\s+|\s+$/g, '');
                var ot = ontologyInfo[1].replace(/^\s+|\s+$/g, '');
                $inputNode.find('input').autocomplete({
                  source: function(request, response) {
                    $.ajax({
                      url: "ontologyAjax.action?t=child",
                      data: {
                        maxRows: 12,
                        sw: request.term.replace(' ', '%20'),
                        tid: tid,
                        ot: ot
                      },
                      success: function( data ) {
                        //cleans decorated input fields when fails
                        if(!data || !data.result) {
                          utils.error.remove();
                          $('input[id^="ontology"]').removeClass('ui-autocomplete-loading').removeAttr('style');
                          utils.error.add("Ontolo gy search failed. Please try again.");
                        } else {
                          response( $.map( data.result, function( item ) {
                            //decorate options
                            if(item.ontology) {
                              return {
                                label: item.tlabel + " - " + item.ontolabel,
                                value: item.tlabel + "[" + item.taccession + "]"
                              };
                            } else {
                              return {
                                label: item,
                                value: '',
                                ontology: null,
                                term: null
                              };
                            }
                          }));
                        }
                      }
                    });
                  },
                  minLength: 3,
                  select: function(event, ui) {
                    return;
                  }
                }); //.css('width', '175px');
              }
            },
            validation: function() {
              var valid = true;
              if($("#_projectSelect").val()==='0' || $("#_eventSelect").val()==='0') {
                utils.error.add("Select a Project and Event");
                valid = false;
              }
              return valid;
            }
          },
          callbacks = {
            added: function(data,id) {
              var list = vs.empty;
              $.each(data.aaData, function(i1,v1) {
                if(i1 != null && v1 != null) {
                  list += vs.vnoption.replace('$v$',v1.id).replace('$n$',v1.name);
                }
              });
              $("#a_"+id+"Select").append(list);
              return;
            },
            sample: function(data) {
              var list = vs.empty;
              $.each(data.aaData, function(i1,v1) {
                if(i1!=null && v1!=null) {
                  list += vs.vvoption.replace(/\\$v\\$/g, v1.name);
                }
              });
              $("#_sampleSelect, #_parentSampleSelect").html(list);

              sample_options=list;
              return;
            },
            event: function(data) {
              var list = vs.empty;
              $.each(data, function(i1,v1) {
                if(v1 != null) {
                  $.each(v1, function(i2,v2) {
                    if(v2 != null && v2.lookupValueId != null && v2.name != null) {
                      list += vs.vnoption.replace('$v$',v2.lookupValueId).replace('$n$',v2.name);
                    }
                  });
                }
              });

              $("#_eventSelect").html(list);
              return;
            },
            meta: function(data, en) {
              var content = '';
              var count= 0;
              var multiSelectPrefix='multi(';
                
              var $attributeDiv = $("#attributeInputDiv"); //where attributes are placed
              $attributeDiv.empty(); //empty any existing contents
              
              g_eventAttributes = [];
              g_gridLineCount=0;
              g_avDic={};

              var requireImgHtml = '<img class="attributeIcon" src="images/icon/req.png"/>';

              // //add table headers for grid view
              var gridHeaders = '', $gridHeaders = $('<tr/>');
              $gridHeaders.append($('<th/>').addClass('tableHeaderNoBG gridIndex').append('#')); //grid row index
              if(utils.checkNP(en)) {
                if(!utils.checkSR(en)) {
                  $gridHeaders.append($('<th/>').addClass('tableHeaderNoBG').append('Sample<br/>', requireImgHtml));
                } else {
                  // hide sample information
                  // $gridHeaders.append(
                  //   $('<th/>').addClass('tableHeaderNoBG').append('Sample Name<br/>', requireImgHtml),
                  //   $('<th/>').addClass('tableHeaderNoBG').append('Parent Sample'),
                  //   $('<th/>').addClass('tableHeaderNoBG').append('Public<br/>', requireImgHtml)
                  // );
                }
              } else {
                if(utils.checkPR(en)) {
                  $gridHeaders.append(
                    $('<th/>').addClass('tableHeaderNoBG').append('Project Name<br/>', requireImgHtml),
                    $('<th/>').addClass('tableHeaderNoBG').append('Public<br/>', requireImgHtml)
                  );
                }
              }

              //meta attribute loop to genereate input fields
              $.each(data.aaData, function(ma_i, _ma) {
                if(_ma && _ma.eventMetaAttributeId && _ma.projectId) {
                  g_eventAttributes.push(_ma); //stores event attributes for other uses

                  //options
                  var isDesc = (_ma.desc && _ma.desc !== '');
                  var isRequired = _ma.requiredDB;
                  var hasOntology = (_ma.ontology && _ma.ontology !== '');
                  var $attributeTr = $('<tr class="gappedTr"/>');

                  $attributeTr.append(//icons and hover over information
                    $('<td align="right"/>').attr('title', (isDesc ? _ma.desc : '')).append(
                      (_ma.label != null && _ma.label !== '' ?_ma.label:_ma.lookupValue.name),
                      "&nbsp;",
                      (
                        isDesc && isRequired ? '<img class="attributeIcon" src="images/icon/info_r.png"/>'
                          : isDesc ? '<img class="attributeIcon" src="images/icon/info.png"/>'
                            : isRequired ? requireImgHtml : ''
                      ),
                      (hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                    )
                  );
                  $gridHeaders.append(
                    $('<th/>').addClass('tableHeaderNoBG').attr('title', (isDesc ? _ma.desc : '')).append(
                      (_ma.label ? _ma.label : _ma.lookupValue.name) + '<br/>',
                      (isRequired ? requireImgHtml : ''),
                      (hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                    )
                  );

                  var inputElement='';
                  var isSelect = (_ma.options && _ma.options !== '' && _ma.options.indexOf(';') > 0);
                  var isMulti = false;
                  var isText = false;

                  inputElement = '<input type="hidden" value="' + utils.getProjectName() + '" name="$lt$projectName"/>';
                  inputElement += '<input type="hidden" value="' + _ma.lookupValue.name + '" name="$lt$attributeName"/>';

                  if(isSelect) { //select box for option values
                    //is this multi select
                    isMulti = (_ma.options.substring(0, multiSelectPrefix.length)===multiSelectPrefix) && (_ma.options.lastIndexOf(')')===_ma.options.length-1);
                    var options = isMulti ? '' : '<option value=""></option>';
                    var givenOptions = _ma.options;

                    if(isMulti) { //trim multi select wrapper
                      givenOptions = givenOptions.substring(multiSelectPrefix.length, givenOptions.length-1);
                    }

                    //convert 0 or 1 options to yes/no
                    if(givenOptions === '0;1' || givenOptions === '1;0') {
                      options = '<option value="0">No</option><option value="1">Yes</option>';
                    } else {
                      $.each(givenOptions.split(';'), function(o_i,o_v) {
                        options += '<option value="' + o_v + '">' + o_v + '</option>';
                      });
                    }
                    inputElement += '<select id="select_$id$" name="$lt$attributeValue" style="min-width:35px;width:200px;" ' + (isMulti ? 'multiple="multiple"':'') + '>' + options + '</select>';
                  } else {
                    if(_ma.lookupValue.dataType==='file') { //file
                      inputElement += '<input type="file" id="' + _ma.lookupValue.dataType + '_$id$" name="$lt$upload"/>';
                    } else { //text input
                      isText = true;
                      inputElement += '<input type="text" id="' + _ma.lookupValue.dataType + '_$id$" name="$lt$attributeValue" value="$val$"/>';
                    }
                  }
                  inputElement = inputElement.replace("$id$",_ma.lookupValue.name.replace(/ /g,"_")+"_$id$");

                  g_avDic[_ma.lookupValue.name] = { //store html contents with its attribute name for later use in adding row
                    'ma': _ma,
                    'inputElement': inputElement,
                    'isText': isText,
                    'hasOntoloty': hasOntology,
                    'isSelect': isSelect,
                    'isMulti': isMulti
                  };

                  var $inputNode = $('<td/>').append(inputElement.replace(/\\$val\\$/g, '').replace("$id$", 'f_' + count).replace(/\\$lt\\$/g,"beanList["+count+"]."));

                  if(isText && hasOntology) {
                    _utils.ontologify(_ma.desc, $inputNode);
                  }

                  /* multiple select jquery plugin */
                  if(isMulti) {
                    $inputNode.find('select').multipleSelect();
                  }
                  $attributeDiv.append($attributeTr.append($inputNode));
                  count++;
                }
              });
              //utils.smartDatePicker(); //initialise date fields

              //add attribut headers to the grid view and add empty rows
              $('thead#gridHeader').append($gridHeaders);
              _utils.addGridRows(null, en);

              return;
            },
            eventAttribute: function(data, eventName) {
              if(data && data.aaData) {
                g_gridLineCount = 0;
                $('#gridBody').html('');
                $('[name^="gridList"]').remove();
                $.each(data.aaData, function(i,v) {
                  if(v.object && v.attributes) {
                    var gridLine={}, beans=[];
                    $.each(v.attributes, function(ai, at) {
                      beans.push([at.attributeName, at.attributeValue]);
                    });
                    gridLine['pn']= data.projectName;
                    gridLine['sn']= v.object.sampleName;
                    gridLine['beans']=beans;
                    button.add_event(null,eventName,gridLine);
                  }
                });
              }
              
              _utils.addGridRows(null,eventName);
            }
          },
          changes = {
            project: function(projectId) {
              _utils.hidePS();
              g_sampleIds = null;
              $("#_sampleSelect").attr("disabled", false);
              $("#_eventSelect").attr("disabled", false);
              _utils.makeAjax('sharedAjax.action', 'type=event&projectId='+projectId+'&filter=' + $('#filter').val(), null, callbacks.event);
              _utils.makeAjax('sharedAjax.action', 'type=sample&projectId='+projectId, null, callbacks.sample);
              $('#_sampleSelect+.ui-autocomplete-input, #_eventSelect+.ui-autocomplete-input').val('');
            },
            sample: function(){ /*nothing to do when sample changes*/ },
            event: function(eventName, eventId) {
              _utils.hidePS();

              $("#saveButton, #validateButton, #submitButton").attr("disabled", false);
              if(utils.getLoadType()==='form') {
                _utils.showPS(eventName);
              }
              _utils.makeAjax(
                'sharedAjax.action',
                'type=ea&projectName=' + utils.getProjectName() + '&eventName=' + eventName,
                eventName,
                callbacks.meta
              );
              if(utils.checkNP(eventName) && g_sampleIds) {
                _utils.makeAjax(
                  'sharedAjax.action',
                  'type=sa&projectName='+utils.getProjectName() + '&projectId=' + $('#_projectSelect').val() + '&eventId=' + eventId + '&eventName=' + eventName + '&ids=' + g_sampleIds,
                  eventName,
                  callbacks.eventAttribute
                );
              }
            }
          };

      var button = {
        submit: function(status) {
          var loadType = $('input[name="loadType"]:radio:checked').val();
          this.submit_form(loadType==='form'? 'insert' : loadType, status);
        },
        template: function() {
          if(_utils.validation()) {
            $.openPopupLayer({
              name: "LPopupTemplateSelect",
              width: 450,
              url: "popup.action?t=sel_t&projectName=" + $("#_projectSelect option:selected").text() +
                "&projectId=" + $("#_projectSelect").val() + "&eventName=" + $("#_eventSelect option:selected").text() +
                "&eventId=" + $("#_eventSelect").val() + "&ids=" + (g_sampleIds ? g_sampleIds : "")
            });
            //this.submit_form("template");
          }
        },
        submit_form: function(type, status) {
          $("#projectName").val(utils.getProjectName());
          $("#sampleName").val(utils.getSampleName());
          $("#eventName").val(utils.getEventName());
          $("#jobType").val(type);
          $("#status").val(status);
          if(type === 'file') {
            $("form#eventLoaderPage").attr("enctype", "multipart/form-data");
          }
          $('form#eventLoaderPage').submit();
        },
        add_event: function(pn,en,dict) { //add event to grid view
          var _pn = pn ? pn : utils.getProjectName(),
              _en = en ? en : utils.getEventName(),
              $eventLine = $('<tr class="borderBottom"/>');

          $eventLine.append('<td class="gridIndex">' + (g_gridLineCount + 1)+ '</td>'); //grid row index

          if(_pn && _en) {
            if(utils.checkNP(_en)){ //not project related
              if(utils.checkSR(_en)) { //add sample information fields for sample registration
                // hide sample information
                // $eventLine.append(
                //   $('<td/>').append($('<input/>').attr({
                //       'type': 'text',
                //       'name': 'gridList[' + g_gridLineCount + '].sampleName',
                //       'id': '_sampleName' + g_gridLineCount
                //     })
                //   )
                // );
                // $eventLine.append(
                //   $('<td/>').append(
                //       $('<select/>').attr({
                //         'name': 'gridList[' + g_gridLineCount + '].parentSampleName',
                //         'id': '_parentSelect' + g_gridLineCount
                //       }).append(sample_options)
                //     )
                // );
                // $eventLine.append(
                //   $('<td/>').append(
                //     $('<select/>').attr({
                //       'name': 'gridList[' + g_gridLineCount + '].samplePublic'
                //     }).append(vs.nyoption)
                //   )
                // );
              } else {
                $eventLine.append(
                  $('<td/>').append(
                      $('<select/>').attr({
                        'name': 'gridList[' + g_gridLineCount + '].sampleName',
                        'id': '_sampleSelect' + g_gridLineCount
                      }).append(sample_options)
                    )
                );
              }
            } else {
              if(utils.checkPR(_en)) { //add project information fields for project registration
                $eventLine.append(
                  $('<td/>').append($('<input/>').attr({
                      'type': 'text',
                      'name': 'gridList[' + g_gridLineCount + '].projectName',
                      'id': '_projectName' + g_gridLineCount
                    })
                  )
                );
                $eventLine.append(
                  $('<hidden/>').attr({
                    'name': 'gridList[' + g_gridLineCount + '].projectPublic',
                    'value': '1'
                  })
                );
                // $eventLine.append(
                //   $('<td/>').append(
                //     $('<select/>').attr({
                //       'name': 'gridList[' + g_gridLineCount + '].projectPublic'
                //     }).append(vs.ynoption)
                //   )
                // );
              }
            }
            var ltVal, bean, avCnt = 0;
            var beans = ((dict && dict['beans']) ? dict['beans'] : null);

            //add event meta attribute fields
            $.each(g_avDic, function(av_k, av_v) {
              bean = null;
              if(beans) {
                $.each(beans, function(b_i,b_v) {
                  if(av_v.ma.lookupValue.name === b_v[0]) {
                    bean = b_v;
                  }
                });
              }

              ltVal = 'gridList[' + g_gridLineCount + '].beanList[' + (avCnt++) + '].';

              var attributeField = g_avDic[av_v.ma.lookupValue.name]['inputElement'];
              attributeField = attributeField.replace('$val$', (bean ? bean[1] : ''));

              var $inputNode = $('<td/>').append(
                attributeField.replace(/\\$lt\\$/g, ltVal).replace(/\\$id\\$/g, 'g_' + g_gridLineCount)
              );
              if(av_v.isSelect === true && bean) {
                utils.preSelectWithNode($inputNode, bean[1]);
              }
              if(av_v.isMulti === true) {
                $inputNode.find('select').multipleSelect();
              }
              $eventLine.append($inputNode);
            });

            //add to grid body
            $('#gridBody').append($eventLine);

            if(dict) {
              //load existing data if any
              if(utils.checkSR(_en)) {
                $('input:text#_sampleName' + g_gridLineCount).val(dict['sn']);
                utils.preSelect('_parentSelect' + g_gridLineCount, dict['psn']);
                $('select[name="gridList[' + g_gridLineCount + '].samplePublic"]').val(dict['sp']);
              } else if(utils.checkPR(_en)) {
                $('#_projectName' + g_gridLineCount).val(dict['pn']);
                $('select[name="gridList[' + g_gridLineCount + '].projectPublic"]').val(dict['pp']);
              } else {
                utils.preSelect('_sampleSelect' + g_gridLineCount, dict['sn']);
              }
            }

            utils.smartDatePicker();
            $('select[id^="_"]').combobox();

            //set minimum width for date and autocomplete TDs
            $('#gridBody .hasDatepicker').parent('td').attr('style', 'min-width:163px !important;');
            $('#gridBody .ui-autocomplete-input').parent('td').attr('style', 'min-width:172px !important;');

            g_gridLineCount++;
          }
        },
        clear_form: function() {
          $("#_projectSelect").val(0);
          utils.preSelect("_projectSelect", 0);
          changes.project(0);
          $('#_parentProjectSelect, #_parentSampleSelect').combobox();
          this.clear_attr();
        },
        clear_attr: function() {
          utils.error.remove();
          $("#attributeInputDiv, #gridHeader, #gridBody").html('');
          $('[name^="beanList"], [name^="gridList"]').remove();
          $('#_projectName, #_parentProjectSelect~input, #_sampleName, #_parentSampleSelect~input').val('');
        }
      };

      function comboBoxChanged(option, id) {
        if(id==='_projectSelect') {
          button.clear_attr();
          $("#saveButton, #validateButton, #submitButton").attr("disabled", true);
          $("#_eventSelect").html(vs.empty);
          if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
            changes.project(option.value);
          } else {
            $("#_sampleSelect").html(vs.empty);
            $("#_eventSelect").html(vs.empty);
          }
        } else if(id==='_eventSelect') {
          button.clear_attr();
          if(option.value && option.value!=0 && option.text && option.text!='') {
            changes.event(option.text, option.value);
          }
        } else if(id==='_sampleSelect') {
          /*if(option.value && option.value!=0 && option.text && option.text!='' && $('#_eventSelect').val() != 0) {
            _utils.makeAjax(
                'sharedAjax.action',
                'type=ea&projectName='+utils.getProjectName()+'&eventName='+utils.getEventName(),
                utils.getEventName(),
                callbacks.meta
            );
          }*/
        }
      }
      $(document).ready(function() {
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
        var rtnJobType = (oldJobType===''||oldJobType==='insert'||oldJobType==='template'?'form':oldJobType);
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
            $("[id*='_" + currAttributeName + "_f']").val("${bean.attributeValue}");
          </s:iterator>
          <s:set name="oldLoadingSample" value="loadingSample" />
          <s:if test="%{#oldLoadingSample != null && #oldLoadingSample.getSampleName() != null}">
            $('#_sampleName').val('<s:property value="#oldLoadingSample.sampleName"/>');
            utils.preSelect('_parentSampleSelect', '<s:property value="#oldLoadingSample.parentSampleName"/>');
            utils.preSelect('_isSamplePublic', '<s:property value="#oldLoadingSample.isPublic"/>');
          </s:if>
          <s:else>
            <s:set name="oldLoadingProject" value="loadingProject" />
            <s:if test="%{#oldLoadingProject != null && #oldLoadingProject.getProjectName() != null}">
              $('#_projectName').val('<s:property value="#oldLoadingProject.projectName"/>');
              utils.preSelect('_isProjectPublic', '<s:property value="#oldLoadingProject.isPublic"/>');
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
        } else if(filter === 'su') { //edit data redirected from search and edit page
          $('#pageTitle').html('Edit Data');
          $('#eventTitle').html('Edit Data For');
        }
      });
    </script>
  </body>
</html>