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
        var $gridSize = $('#gridListSize').val();
        var rowSize = ($gridSize > 0) ? $gridSize : 5;
        //have at least 5 event lines for the grid view
        for(var _rows=$('#gridBody > tr').length;_rows<rowSize;_rows++) {
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
            list += vs.vvoption.replace(/\$v\$/g, v1.name);
          }
        });
        $("#_sampleSelect, #_parentSampleSelect").html(list);

        sample_options=list;
        return;
      },
      event: function(data) {
        var list = vs.empty;

        if(data['aaData'] == null && data['err'].indexOf('Forbidden') > 0) {
          utils.error.add(utils.error.message.permission);
          return false;
        }
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

        var requireImgHtml = '<img class="attributeIcon" src="images/icon/info_r.png"/>';

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
                $('<th/>').addClass('tableHeaderNoBG').append('Project Name<br/>', requireImgHtml)
                // $('<th/>').addClass('tableHeaderNoBG').append('Public<br/>', requireImgHtml)
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
                $('<td align="right"/>').attr('class', (isDesc ? 'table-tooltip' : '')).attr(
                    'title', (isDesc ? _ma.desc : '')).append(
                    isRequired ? '<small class="text-danger">*</small>' : '',
                    (_ma.label != null && _ma.label !== '' ?_ma.label:_ma.lookupValue.name),
                    "&nbsp;",
                    isDesc ? requireImgHtml : ''
                    //, (hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                )
            );
            $gridHeaders.append(
                $('<th/>').addClass('tableHeaderNoBG').attr('title', (isDesc ? _ma.desc : '')).append(
                    isRequired ? '<small class="text-danger">*</small>' : '',
                    (_ma.label ? _ma.label : _ma.lookupValue.name) + '<br/>',
                    isDesc ? requireImgHtml : ''
                    //,(hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
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
              inputElement += '<select id="'  + (isRequired ? 'req_' : '') + 'select_$id$" name="$lt$attributeValue" style="min-width:35px;width:200px;" ' + (isMulti ? 'multiple="multiple"':'') + '>' + options + '</select>';
            } else {
              var maDatatype = _ma.lookupValue.dataType;
              if(maDatatype === 'file') { //file
                inputElement += '<input type="file" id="' + maDatatype + '_$id$" name="$lt$upload"/>';
              } else if(maDatatype ==='date') {
                inputElement +=
                    '<div class="input-group col-sm-12">'+
                    '  <input type="text" id="' + maDatatype + '_$id$" name="$lt$attributeValue" value="$val$"/>' +
                    '  <label for="' + maDatatype + '_$id$" class="input-group-addon" style="padding:4px;"><span><i class="fa fa-calendar"></i></span></label>' +
                    '</div>';
              } else { //text input
                isText = true;
                inputElement += '<input type="text" id="' + (isRequired ? 'req_' : '') + maDatatype + '_$id$" name="$lt$attributeValue" value="$val$"/>';
              }
            }
            inputElement = inputElement.replace(/\$id\$/g,_ma.lookupValue.name.replace(/ /g,"_") + "_$id$");

            g_avDic[_ma.lookupValue.name] = { //store html contents with its attribute name for later use in adding row
              'ma': _ma,
              'inputElement': inputElement,
              'isText': isText,
              'hasOntoloty': hasOntology,
              'isSelect': isSelect,
              'isMulti': isMulti
            };

            var $inputNode = $('<td/>').append(inputElement.replace(/\$val\$/g, '').replace(/\$id\$/g, 'f_' + count).replace(/\$lt\$/g,"beanList["+count+"]."));
            utils.smartDatePicker($inputNode);

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

          $('#dataSubmissionScope').show();
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
// end _utils

var button = {
  submit: function(status) {
    var loadType = $('input[name="loadType"]:radio:checked').val();

    if(loadType === 'form') { //check is form is empty

      var hasAllReq = true, reqErrorMsg = ""; // require field check
      var $formFields = $('#attributeInputDiv > tr > td');
      $formFields.find('[id^="req_"]:not(:hidden)').each(function(i, v) {
        var $node = $(v);
        if($node.val() === null || $node.val() === '') {
          hasAllReq = false;
          reqErrorMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
        }
      });

      if(!hasAllReq) {
        utils.error.add("Required Field(s):<br/>" + reqErrorMsg);
        return;
      }

      var allEmpty = true; // empty submission form
      $formFields.find('[name$=".attributeValue"], [name$=".upload"]').each(function(i,v) {
        var $node = $(v);
        if($node.val() !== null && $node.val() !== '') {
          allEmpty = false;
        }
      });

      if(allEmpty) {
        utils.error.add("Data submission form is empty. Please insert value(s).");
        return;
      }
    }

    this.submit_form(loadType, status);
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
    //$("#sampleName").val(utils.getSampleName());
    $("#eventName").val(utils.getEventName());
    $("#jobType").val(type);
    $("#status").val(status);

    if(type === 'file') {
      var $fileNode = $('#dataTemplate');
      if($fileNode.val() == null || $fileNode.val().length === 0) { //check if file is there
        utils.error.add("Please select a data template file.");
        return false;
      }
      // else {
      //   $("form#eventLoaderPage").attr("enctype", "multipart/form-data");
      // }
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
              ).append($('<hidden/>').attr({
                    'name': 'gridList[' + g_gridLineCount + '].projectPublic',
                    'value': '1'
                  }))
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
        if(attributeField.indexOf('type="file"') >= 0) {
          if(bean) {
            attributeField += ("<strong>" + bean[1].substring(bean[1].indexOf("_") + 1) + "</strong>");
          }
        }

        var $inputNode = $('<td/>').append(
            attributeField.replace(/\$lt\$/g, ltVal).replace(/\$id\$/g, 'g_' + g_gridLineCount)
        );
        if(av_v.isSelect === true && bean) {
          utils.preSelectWithNode($inputNode, bean[1]);
        }
        if(av_v.isMulti === true) {
          $inputNode.find('select').multipleSelect();
        }
        $eventLine.append($inputNode);
      });

      utils.smartDatePicker($eventLine);
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
  },
  projectPopup: function() {
    var selectedProject = utils.getProjectName();
    if(!selectedProject) {
      utils.error.add("Please select a project");
      return;
    } else{

      var whs = $(window).width()*3/4;
      whs = whs < 550 ? 550 : whs;
      $.openPopupLayer({
        name: "LPopupProjectDetails",
        width: whs,
        height: 50,
        url: "popup.action?t=projectDetails_pop&projectName=" + selectedProject
      });
    }
  }
};
// end buton

function comboBoxChanged(option, id) {
  if(id==='_projectSelect') {
    button.clear_attr();
    $("#saveButton, #validateButton, #submitButton").attr("disabled", true);
    $("#eventSelectRow").find("input.ui-widget, button.ui-widget").prop("disabled", false);
    $("#_eventSelect").html(vs.empty);
    if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
      changes.project(option.value);
    } else {
      $("#_sampleSelect").html(vs.empty);
      $("#_eventSelect").html(vs.empty);
    }
    $('#dataSubmissionScope').hide();
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

function toBulk() {
  utils.error.remove();
  //$('#interactiveDiv, #toBulkP').hide();
  $('#dropBoxDiv, #toInteractiveP').slideDown(400);

  $('#files > p').remove();
  $('#progress > .bar').css("width", "0%");

  // file drop box
  $('#uploadFile').fileupload({
    // maxChunkSize: 1000000,
    // multipart: false,
    add: function(e, data) {
      utils.error.remove();

      //file extension validation for .csv and .zip
      var acceptFileTypes = /(\.)(csv|zip)$/i;

      var acceptable = data.files[0]['name'].length && acceptFileTypes.test(data.files[0]['name']);
      var $fileRow = $('<p/>').text(
          data.files[0]['name']+ " (" + utils.formatFileSize(data.files[0]['size']) + ")"
      );

      if(!acceptable) {
        $fileRow.append($('&nbsp;<strong class="error text-danger"/>').text("[Only .csv and .zip files are supported]"));
        data.files.length = 0;
      }
      var $cancelBtn = $('<button type="button" class="btn btn-xs btn-warning cancel" style="margin-left:15px;" />').text("Cancel").click(function() {
        data.abort();
        data.files.length = 0;
        $(this).parent().remove();
      });
      data.context = $fileRow.append($cancelBtn).appendTo($('#files'));

      $('#uploadFilesBtn').on('click', function(e) {
        e.preventDefault();
        if (data.files.length > 0) {
          data.submit();
        }
      });
    },
    dataType: 'json',
    sequentialUploads: true,
    done: function (e, data) {
      // $.each(data.result.result.files, function (index, file) {
      //   $('<p/>').html("'<strong>" + file.name + "</strong>' has been uploaded.").appendTo('#files');
      // });
      $('#files').find('p').each(function(i,v) {
        var $node = $(v);

        if($node.text().indexOf(data.files[0].name) >= 0) {
          if($node.find('span.label').length < 1) {
            $node.prepend($('<span class="label label-info" style="margin-right: 10px;"/>').text("Uploaded Sucessfully"));
          }
        }
      });
      data.files.length = 0;

      //$('#progress .bar').css('width', '0%');
    },
    dropZone: $('#dropzone'),
    progressall: function (e, data) {
      var progress = parseInt(data.loaded / data.total * 100, 10);
      $('#progress .bar').css('width', progress + '%');
    }
  });

  $(document).bind('dragover', function (e) {
    var dropZone = $('#dropzone'),
        timeout = window.dropZoneTimeout;
    if(!timeout) {
      dropZone.addClass('in');
    } else {
      clearTimeout(timeout);
    }
    var found = false, node = e.target;
    do {
      if(node === dropZone[0]) {
        found = true;
        break;
      }
      node = node.parentNode;
    } while(node != null);
    if(found) {
      dropZone.addClass('hover');
    } else {
      dropZone.removeClass('hover');
    }
    window.dropZoneTimeout = setTimeout(function () {
      window.dropZoneTimeout = null;
      dropZone.removeClass('in hover');
    }, 100);
  });

  $(document).bind('drop dragover', function (e) {
    e.preventDefault();
  });
}

function toInteractive() {
  utils.error.remove();
  $('#dropBoxDiv, #toInteractiveP').hide();
  $('#interactiveDiv, #toBulkP').slideDown(400);
}