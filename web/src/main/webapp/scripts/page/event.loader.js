var _html = {
  fm: '<div id="attach-file-dialog-$id$" class="attach-file-dialog" style="display:none;">' +
  '<div class="file-dialog-heading"><h2 style="margin-top: 0px;margin-bottom: 9px;" title="Attach Files:&nbsp;">Attach Files<span style="display:none" class="header-separator">:&nbsp;</span>' +
  '</h2></div><div class="form-body" style="max-height: 364px;position: relative;overflow: auto;padding: 20px;"><fieldset class="fm-fieldset"><legend><span>Select</span></legend>' +
  '<input class="upload-file" id="uploadFile-$id$" multiple="multiple" type="file" name="$lt$upload" style="margin-bottom: 10px;"><div id="dropzone-$id$" class="dropzone" class="well">Drop files here</div><div id="attach-files-$id$">$uploadedFiles$</div>' +
  '<button type="button" class="btn btn-primary" id="attach-file-upload-$id$" style="margin-top: 10px;">UPLOAD</button><img id="loading-$id$" src="images/loading.gif" style="margin: 10px 0 0 10px;width: 24px;display: none;"></fieldset><fieldset class="fm-fieldset" style="width: 660px;margin-top: 5px;"><b>Tip:</b> After uploading or removing the file(s), click “Back to Submit Page”  button and submit the page to compete the changes!</fieldset>' +
  '</div><div class="buttons-container form-footer" style="overflow: visible;min-height: 51px;height: 100%;margin: 0;padding: 10px;"><fieldset class="fm-fieldset" style="width: 600px;"><legend><span>Attachment(s)</span></legend><div id="files-$id$" class="files" style="padding-left:20px;">$existingFileField$</div></fieldset>' +
  '<div class="buttons" style="float: right"><button type="button" class="btn btn-primary" id="attach-file-done-$id$" style="margin: 10px;" disabled>Back to Submit Page</button><button type="button" class="btn btn-default" id="attach-file-cancel-$id$" style="margin: 10px;">Cancel</button></div></div></div>'
};

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
        // If samples are uploaded as CSV, set event lines based on row size, else have at least 5 event lines for the grid view
        var $gridSize = $('#gridListSize').val();
        if($gridSize === '' && g_sampleIds) $gridSize = g_sampleIds.split(',').length;
        var rowSize = utils.checkPU(en) ? 1 : ($gridSize > 0) ? $gridSize : 5;
        if(g_sampleIds == null) {
          for (var _rows = $('#gridBody > tr.borderBottom').length; _rows < rowSize; _rows++) {
            button.add_event(pn, en);
          }
        }
      },
      showPS: function(eventName) {
        $('#sampleSelectRow').hide();
        if(utils.checkSR(eventName)) { // triggers sample loader
          $('#sampleDetailInputDiv').show();
          if(typeof publicEnabled !== 'undefined'  && !publicEnabled) $("#publicSampleRow").hide(); // hide public from form view
          if(typeof parentEnabled !== 'undefined'  && !parentEnabled) $("#parentSampleRow").hide(); // hide Parent ID from form view
          $('#sampleSelect, #searchSample').prop("disabled", true);$('#form-sample-name').hide();
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
        $('#sampleSelect, #searchSample').prop("disabled", true);$('#form-sample-name').hide();
      },
      ontologify: function(desc, $inputNode) {
        var ontologyInfo = desc.substring(desc.lastIndexOf('[')+1, desc.length-1).split(',');

        if(ontologyInfo.length === 2) {
          var tid = ontologyInfo[1].replace(/^\s+|\s+$/g, '');
          var ot = ontologyInfo[0].replace(/^\s+|\s+$/g, '');
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
                beforeSend: function() {
                  utils.processing(true);
                },
                complete: function() {
                  utils.processing(false);
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
      },
      downloadTemplate: function () {
        $("<form action='eventLoader.action'/>")
            .append($("<input/>").attr({
              type: 'hidden',
              name: 'jobType',
              value: $("#templateJobType").val()
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
              value: (g_sampleIds ? g_sampleIds : "")
            }))
            .append($("<input/>").attr({
              type: 'hidden',
              name: 'eventId',
              value: $("#_eventSelect").val()
            }))
            .append($("<input/>").attr({
              type: 'hidden',
              name: 'eventName',
              value: $("#_eventSelect option:selected").text()
            })).appendTo('body').submit();
        $('#download-template').modal('hide');
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
        var filterVal = $('#filter').val();

        if(data['aaData'] == null && data['err'].indexOf('Forbidden') > 0) {
          utils.error.add(utils.error.message.permission);
          return false;
        }
        $.each(data, function(i1,v1) {
          if(v1 != null) {
            $.each(v1, function(i2,v2) {
              if(v2 != null && v2.lookupValueId != null && v2.name != null && (filterVal==='pr' || !utils.checkPR(v2.name))) {
                list += vs.vnoption.replace('$v$',v2.lookupValueId).replace('$n$',v2.name);
              }
            });
          }
        });

        $("#_eventSelect").html(list);

        //Reload event attributes after submit
        if(typeof oldEventName !== 'undefined' && oldEventName) {
          utils.preSelect("_eventSelect", oldEventName);
          changes.event(oldEventName, $('#_eventSelect').val());
        }
        return;
      },
      meta: function(data, en) {
        var content = '';
        var count= 0;
        var selectPrefix='dropdown(';
        var multiSelectPrefix='multi-dropdown(';
        var radioSelectPrefix='radio(';
        var hasDependantDict = false;
        publicEnabled = data.publicEnabled;
        //parentEnabled = data.parentEnabled;
        parentEnabled = en.indexOf('Subject') < 0;  //disable parent if subject event

        var $attributeDiv = $("#attributeInputDiv"); //where attributes are placed
        $attributeDiv.empty(); //empty any existing contents

        var $autofillLine = $('<tr style="display: none;"/>');
        $autofillLine.append('<td class="gridIndex"></td>'); //grid row index
        var autofill_no = 2; //to specify which column is going to be autofilled (nth-element)

        g_eventAttributes = [];
        g_gridLineCount=0;
        g_avDic={};

        var tooltipSpan = '<span class="glyphicon glyphicon-info-sign" aria-hidden="true" style="color: #1aac1a;"></span>';
        var autofillButtonHtml = '<button type="button" class="btn btn-default btn-xs autofill-title" data-placement="left" data-html="true" id="all-$a$" onClick="dataAutofill(this.id)" title="Autofill column"><img src="images/autofill.png" style="width: 24px;height: 22px;"></i></button>' +
            '<button type="button" id="sequence-$b$" onclick="dataAutofill(this.id)" data-placement="left" data-html="true" class="btn btn-default btn-xs autofill-title" title="Autofill column in sequence"><img src="images/autofill_sequence.png" style="width: 24px;height: 22px;"></button>' +
            '<button type="button" class="btn btn-default btn-xs autofill-title" data-placement="left" data-html="true" id="clear-$c$" onClick="dataAutofill(this.id)" title="Remove data in column"><img src="images/autofill_clear.png" style="width: 24px;height: 22px;"></i></button>';


        // //add table headers for grid view
        var gridHeaders = '', $gridHeaders = $('<tr/>');
        $gridHeaders.append($('<th/>').addClass('gridIndex').append('#')); //grid row index
        if(utils.checkNP(en)) {
          var idLabel, parentIdLabel;
          if (en.indexOf('Subject') > -1) {
            idLabel = "Subject ID";
            parentIdLabel = "";
          } else if (en.indexOf('Sample') > -1) {
            idLabel = "Sample ID";
            parentIdLabel = "Visit ID";
          } else {
            idLabel = "Visit ID";
            parentIdLabel = "Subject ID"
          }
          if(!utils.checkSR(en)) {
            $gridHeaders.append($('<th/>').addClass('resizable-header').append('<small class="text-danger">*</small>'+idLabel));
            $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '135').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
            autofill_no+=1;
          } else {
            $gridHeaders.append($('<th/>').addClass('resizable-header').append('<small class="text-danger">*</small>'+idLabel+'<br/>'));
            $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '95').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
            autofill_no+=1;

            if(parentEnabled) {
              $gridHeaders.append($('<th/>').addClass('resizable-header').append(parentIdLabel));
              $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '190').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
              autofill_no+=1;
            } else {
              $("#parentSampleRow").hide(); // hide public from form view
            }

            if(publicEnabled) {
              $gridHeaders.append($('<th/>').addClass('resizable-header').append('Public<br/>'));
              $autofillLine.append($('<td/>')); // empty for Public
              $("#publicSampleRow").show(); // hide public from form view
              autofill_no+=1; //set to 5 to pass Public column
            } else {
              $("#publicSampleRow").hide(); // hide public from form view
            }
          }
        } else {
          if(utils.checkPR(en)) {
            $gridHeaders.append(
                $('<th/>').addClass('resizable-header').append('<small class="text-danger">*</small>Project Name<br/>', tooltipSpan),
                $('<th/>').addClass('resizable-header').append('Public<br/>', tooltipSpan)
            );
            $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '145').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
            $autofillLine.append($('<td/>')); // empty for Public
            autofill_no+=2;
          }
        }

        //meta attribute loop to genereate input fields
        $.each(data.aaData, function(ma_i, _ma) {
          if(_ma && _ma.eventMetaAttributeId && _ma.projectId) {
            g_eventAttributes.push(_ma); //stores event attributes for other uses

            //options
            var isDesc = (_ma.desc && _ma.desc !== '');
            var isRequired = _ma.requiredDB;
            var isDictionary = _ma.dictionary;
            var hasOntology = (_ma.ontology && _ma.ontology !== '');
            var $attributeTr = $('<div class="form-group"/>');

            $attributeTr.append(//icons and hover over information
                $('<label/>').attr('data-toggle', (isDesc ? 'tooltip' : '')).attr('data-container','body').attr('data-html','true').attr('data-placement','right').attr(
                    'data-original-title', (isDesc ? _ma.desc : '')).append(
                    isRequired ? '<small class="text-danger">*</small>' : '',
                    (_ma.label != null && _ma.label !== '' ?_ma.label:_ma.lookupValue.name),
                    "&nbsp;",
                    isDesc ? tooltipSpan : ''
                    , (hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                )
            );

            var inputElement='';
            var isSelect = (_ma.options && _ma.options !== '' && ((_ma.options.substring(0, selectPrefix.length)===selectPrefix) ||
                _ma.options.indexOf(';') > 0 || _ma.options.indexOf("[{") === 0));
            var isMulti = false, isRadio = false;
            var isText = false;
            var isReadOnly = (_ma.options && _ma.options !== '' && (_ma.options.indexOf('ReadOnly:') >= 0)),

            inputElement = '<input type="hidden" value="' + utils.getProjectName() + '" name="$lt$projectName"/>';
            inputElement += '<input type="hidden" value="' + _ma.lookupValue.name + '" name="$lt$attributeName"/>';

            if(isSelect) { //select box for option values
              //is this multi select
              isMulti = (_ma.options.substring(0, multiSelectPrefix.length)===multiSelectPrefix) && (_ma.options.lastIndexOf(')')===_ma.options.length-1);
              //check if radio button
              isRadio = (_ma.options.substring(0, radioSelectPrefix.length)===radioSelectPrefix) && (_ma.options.lastIndexOf(')')===_ma.options.length-1);
              var options = isMulti || isRadio ? '' : '<option value=""></option>';
              var givenOptions = _ma.options;

              if(isMulti) { //trim multi select wrapper
                givenOptions = givenOptions.substring(multiSelectPrefix.length, givenOptions.length-1);
              } else if(isRadio){
                givenOptions = givenOptions.substring(radioSelectPrefix.length, givenOptions.length-1);
              } else if(_ma.options.substring(0, selectPrefix.length)===selectPrefix) {
                givenOptions = givenOptions.substring(selectPrefix.length, givenOptions.length-1);
              }

              //convert 0 or 1 options to yes/no
              if(givenOptions === '0;1' || givenOptions === '1;0') {
                options = '<option value="0">No</option><option value="1">Yes</option>';
              } else {
                if(givenOptions.indexOf("[{") === 0 && givenOptions.indexOf("parent_attribute") > -1){
                  var childDictJson = JSON.parse(givenOptions);
                  childFieldName = this.attributeName;
                  parentChildDict = []

                  for(var index in childDictJson){
                    var key = childDictJson[index].name;
                    if(key == 'parent_attribute'){
                      parentFieldName = childDictJson[index].value;
                    } else if(key == 'parent_dict_type'){
                      parentDictName = childDictJson[index].value;
                    }
                  }

                  parentChildDict[parentFieldName] = {"dictName":parentDictName,"childFieldName":childFieldName};

                  options = '<option>Select '+parentFieldName +' </option>';
                  hasDependantDict = true;
                } else {
                  if(isDictionary) {
                    $.each(givenOptions.split(';'), function (o_i, o_v) {
                      options += '<option value="' + o_v.split(" - ")[0] + '">' + o_v + '</option>';
                    });
                  } else {
                    $.each(givenOptions.split(';'), function (o_i, o_v) {
                      options += '<option value="' + o_v + '">' + o_v + '</option>';
                    });
                  }
                }
              }

              inputElement += '<select id="'  + (isRequired ? 'req_' : '') + 'select_$id$" name="$lt$attributeValue" style="' + (isMulti || isRadio ? 'width:200px;" multiple="multiple"':'min-width:200px;" class="form-control"') + '>' + options + '</select>';

              $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '130').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
            } else {
              var maDatatype = _ma.lookupValue.dataType;
              if(maDatatype === 'file') { //file
                inputElement += '<button type="button" id="' + maDatatype + '_$id$"  class="btn btn-default" data-toggle="tooltip" data-container="body" data-html="true" ' +
                    'data-placement="right" data-original-title="FILE MANAGEMENT" onclick="showFMPopup(this.id)">File Store</button>';
                inputElement += _html.fm;
                $autofillLine.append($('<td/>'));
              } else if(maDatatype ==='date') {
                inputElement +=
                    '<div class="input-group col-sm-5">'+
                    '  <input type="text" placeholder="YYYY-MM-DD" id="' + maDatatype + '_$id$" name="$lt$attributeValue" value="$val$" class="form-control" style="min-width:160px;" autocomplete="off"/>' +
                    '  <label for="' + maDatatype + '_$id$" class="input-group-addon"><span><i class="fa fa-calendar"></i></span></label>' +
                    '</div>';

                $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '110').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
              } else { //text input
                isText = true;
                inputElement += '<input type="text" id="' + (isRequired ? 'req_' : '') + maDatatype + '_$id$" name="$lt$attributeValue" value="$val$" ' +
                    'class="form-control" style="min-width:160px;" ' + (isReadOnly ? 'readonly' : '') + '/> ';

                $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '94').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
              }
            }
            inputElement = inputElement.replace(/\$id\$/g,_ma.lookupValue.name.replace(/ /g,"_") + "_$id$");

            $gridHeaders.append(
                $('<th/>').addClass('resizable-header')
                    .attr('title', (isDesc ? _ma.desc : '')).attr('data-toggle','tooltip').attr('data-placement','top').attr('data-html', 'true').append(
                    isRequired ? '<small class="text-danger">*</small>' : '',
                    (_ma.label ? _ma.label : _ma.lookupValue.name) + '<br/>',
                    isDesc ? tooltipSpan : ''
                    ,(hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                    /*,(isSelect || isText ? '<img class="attributeIcon" src="images/icon/resize_icon.gif" style="position:absolute;bottom:0;right:0;"/>':'')*/
                )
            );

            g_avDic[_ma.lookupValue.name] = { //store html contents with its attribute name for later use in adding row
              'ma': _ma,
              'inputElement': inputElement,
              'isText': isText,
              'hasOntology': hasOntology,
              'isSelect': isSelect,
              'isMulti': isMulti,
              'isRadio': isRadio,
              'valueLength' :_ma.valueLength
            };

            var $inputNode = $('<div/>').append(inputElement.replace(/\$existingFileField\$/g, "").replace(/\$uploadedFiles\$/g, "")
                .replace(/\$val\$/g, '').replace(/\$id\$/g, 'f_' + count).replace(/\$lt\$/g,"beanList["+count+"]."));
            utils.smartDatePicker($inputNode);

            if(isText && hasOntology) {
              _utils.ontologify(_ma.ontology, $inputNode);
            }

            /* multiple select jquery plugin */
            if(isMulti) {
              $inputNode.find('select').multipleSelect({
                multipleWidth: '200px'});
            } else if(isRadio){
              $inputNode.find('select').multipleSelect({
                single: true,
                multipleWidth: '200px'
              });
            }

            $attributeDiv.append($attributeTr.append($inputNode));
            count++;
          }

          autofill_no+=1;
          $('#dataSubmissionScope').show();
        });
        //utils.smartDatePicker(); //initialise date fields

        //add attribut headers to the grid view and add empty rows
        $('thead#gridHeader').append($gridHeaders);
        $("#gridBody").append($autofillLine);
        $('[data-toggle="tooltip"]').tooltip({
          container: 'body'
        });
        _utils.addGridRows(null, en);

        $("[id^='req_select_sample_type'], [id^='date_Visit_Date']").on('change', function() {
          generateID(this);
        });

        if(hasDependantDict){
          $("select[id*='select_"+parentFieldName.replace(/\s+/g, '_')+"']").change(function(event, data){
            var val = parentChildDict[parentFieldName];
            var childFieldName = val['childFieldName'];
            var parentDictName = val['dictName'];
            var selectedParent = ($(this).val()).split(" - ")[0];
            var selectedParentIdArr = ($(this).attr('id')).split("_");
            var attrIndex = (selectedParentIdArr[selectedParentIdArr.length-2] == 'f') ? "" : selectedParentIdArr[selectedParentIdArr.length-1];
            var childSelectInput = $("select[id*='select_"+childFieldName.replace(/\s+/g, '_')+"_" + selectedParentIdArr[selectedParentIdArr.length-2] + "_" + attrIndex +"']");
            var childValue = (data && data.sampleData == true) ? data.sampleAttrMap[childFieldName] : null;
            childSelectInput.html('');
            childSelectInput.append('<option value=""></option>');

            if(selectedParent && selectedParent != "") {
              $.ajax({
                url: 'getChildDictionary.action',
                data: 'parentDictType=' + parentDictName +'&parentDictCode=' + selectedParent,
                cache: false,
                async: false,
                success: function (data) {
                  $.each(data.aaData, function (i1, v1) {
                    childSelectInput.append('<option value="' + v1.split(" - ")[0] + '">' + v1 + '</option>');
                  });
                }
              });

              if (childValue != null) childSelectInput.val(childValue);
            }
          });
        }

        $("#autofill-option").width($('thead#gridHeader').width() + 70);

        // Populate current project data for Project Update
        if(utils.checkPU(en)){
          _utils.makeAjax(
              'getprojectbyuser.action',
              'projectName=' + utils.getProjectName(),
              "",
              callbacks.populateProjectInfo
          );
        }

        $(".autofill-title").tooltip({
          container: 'body',
          position: {
            my: "center bottom-20",
            at: "center top",
            using: function( position, feedback ) {
              $( this ).css( position );
            }
          }
        });

        $(".resizable-header").resizable({
          handles: 'e',
          resize: function( event, ui ) {
            var columnIndex = this.cellIndex + 1;
            var text = $(this).text();
            var textWidth;

            if(text == 'ID' || text == '*ID'){
              textWidth = 168;
            } else{
              var canvas = document.createElement('canvas');
              var ctx = canvas.getContext("2d");
              ctx.font = "13px Open Sans";
              textWidth = ctx.measureText(text).width + 10;
            }

            if(textWidth < ui.size.width){
              $('#gridBody tr td:nth-child(' + columnIndex + ') select,' +
              '#gridBody tr td:nth-child(' + columnIndex + ') input,' +
              '#gridBody tr td:nth-child(' + columnIndex + ') div.ms-parent,' +
              '#gridBody tr td:nth-child(' + columnIndex + ') div.input-group input').each(function (i, v) {
                if (v.id != '' || v.className != '') {
                  var $v = $(v);
                  $v.css("width", ui.size.width);
                }
              });
            }
            $(this).removeAttr('style');
          }
        });

        initializeFileManagementFunctions();

        return;
      },
      eventAttribute: function(data, eventName) {
        if(data && data.aaData) {
          g_gridLineCount = 0;
          var $autofillRow = $("#gridBody tr:first");
          $('#gridBody').empty();
          $('#gridBody').append($autofillRow);
          $('[name^="gridList"]').remove();
          $.each(data.aaData, function(i,v) {
            if(v.object && v.attributes) {
              var gridLine={}, beans=[];
              $.each(v.attributes, function(ai, at) {
                beans.push([at.attributeName, at.attributeValue]);
              });
              gridLine['pn']= v.projectName;
              gridLine['psn']= v.object.parentSampleName;
              gridLine['sn']= v.object.sampleName;
              gridLine['beans']=beans;
              button.add_event(null,eventName,gridLine);
            }
          });
        }
        utils.processing(false);
        $("#sample-pagination-nav").show();
        $("#pagination-loadingImg").hide();
        $("#gridInputDiv").show();
        $("#autofill-option").width($('thead#gridHeader').width() + 70);

        //_utils.addGridRows(null,eventName);
      },
      populateProjectInfo: function(data, eventName) {
        if(data && data.aaData) {
          var projAttrMap = data.aaData[1].attributes;
          var keyArr = Object.keys(projAttrMap).sort(); //move keys to an array to loop them in order

          for(var i in keyArr){
            var key = keyArr[i];
            var value  = projAttrMap[key];

            key = key.replace(/ /g, "_");
            key = key.replace(/'/g, "\\'");

            //jquery regex for single quotation
            var $input = $("input[id*='"+key.replace(/([ #;&,.+*~\':"!^$[\]()=>|\/@])/g,'\$1')+"']")
                .filter("input[id*='_f_']:first, input[id*='_g_']:first");

            if($input.length > 0) $input.val(value);
            else {
              var $select =  $("select[id*='"+key+"']");

              if($select.get(0)){
                if($select.get(0).getAttribute('multiple') == null) $select.val(value);
                else {
                  var valueArr = value.split(",");

                  for(var j in valueArr){
                    if(valueArr[j].charAt(0) == ' ') valueArr[j] = valueArr[j].replace(" ", "");
                    $select.multipleSelect('setSelects', valueArr);
                  }
                }
              } else{
                $select.val(value);
              }
            }
          }
        }
      },
      populateSampleInfo: function(data, selectName) {
        if(data && data.aaData) {
          var index; //grid row number

          var singleSample = (selectName === 'sampleName');

          //Clear existing data
          if (singleSample) {  //single sample view
            $('#attributeInputDiv input[type="text"]').val('');
            $('[id^=files-][id*=_f_]').html('');
            $("button[id^=file_][id*=_f_]").attr("data-tooltip", "FILE MANAGEMENT");
          } else {  // multiple samples view
            index = selectName.charAt(9);
            $('#gridBody .borderBottom:eq(' + index+ ') input[type="text"][name!="gridList[' + index + '].sampleName"]').val('');
            $('[id^="files-"][id$="_g_' + index + '"]').html('');
            $('[id^="file_"][id$="_g_' + index + '"]').attr("data-tooltip", "FILE MANAGEMENT");
          }

          $('select[id^="select_"], select[id^="req_select_"]').each(function(){
            var $this = $(this);
            var name = $this.get(0).getAttribute('name');
            if( singleSample ? (name.indexOf("gridList") < 0)
                    : (name.indexOf("gridList") > -1 && name.charAt(9) === index)) {
              if ($this.get(0).getAttribute('multiple') == null) $this.val('');
              else $this.multipleSelect('uncheckAll');
            }
          });
          $('input[type="file"][id^="file_"], input[type="file"][id^="req_file_"]').each(function(){
            var $this = $(this);
            var name = $this.get(0).getAttribute('name');
            if(singleSample ? (name.indexOf("gridList") < 0)
                    : name.indexOf("gridList") > -1 && name.charAt(9) === index) {
              $this.prev('strong').remove();
            }
          });
          $('input[type="radio"][class="radio_attr"], input[type="radio"][class="req_radio_attr"]').each(function(){
            var $this = $(this);
            var name = $this.get(0).getAttribute('name');
            if(singleSample ? name.indexOf("gridList") < 0
                    : name.indexOf("gridList") > -1 && name.charAt(9) === index) {
              $this.prop('checked', false);
            }
          });

          var sampleAttrMap = data.aaData[0];

          for (var i in sampleAttrMap) {
            var key = i;
            var value = sampleAttrMap[i];

            var $input = (selectName === 'sampleName') ? $('input[name^="beanList"][value="' + key + '"]').next()
                : $('input[name^="gridList[' + index + '"][value="' + key + '"]').next();

            if($input.is("div")) $input = $input.children(":first");

            var firstObj = $input.get(0);

            if (firstObj) {
              if (firstObj.getAttribute('type') == 'button') {
                var id = $(firstObj).attr('id');
                id = id.substring(id.indexOf("_") + 1);
                var name = $("#uploadFile-" + id).attr('name');
                name = name.substring(0, name.lastIndexOf('.'));
                var $files = $('#files-' + id);

                var valArr = value.split(',');
                var valArrLength = valArr.length;
                var fileNameList = "", separator = "", fileNameCharCount = 0;
                for(var j=0; j < valArr.length; j++){
                  var fileName = valArr[j].substring(valArr[j].indexOf("_") + 1);
                  if(fileName != "") {
                    $files.append("<div id='file-" + id + "-" + j + "'><strong><input type='hidden' name='" + name + ".existingFileName' value='" + fileName + "' >" + fileName + "</strong> " +
                        "<button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Download' style='float: right;margin-left: 2px;' onclick='downloadFile(\"" + fileName + "\",\"" + selectName + "\",\"" + key + "\");'><img src='images/download_file.png' style='height: 20px;'></button>" +
                        "<button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Remove' style='float: right;' onclick='removeFile(\"file-" + id + "-" + j + "\");'><img src='images/cancel.png' style='height: 20px;'></button></div><br>");

                    fileNameList += separator + fileName;
                    fileNameCharCount = (fileName.length > fileNameCharCount) ? fileName.length : fileNameCharCount;
                    separator = " ";
                  } else {
                    valArrLength -= 1;
                  }
                }

                if(valArrLength > 1) {
                  $files.append("<button type='button' class='btn btn-primary btn-xs' onclick='downloadFile(\"DOWNLOADALL\",\"" + selectName + "\",\"" + key + "\");'>Download All</button>")
                }

                if(valArrLength > 0) {
                  $files.append("<input type='hidden' name='" + name + ".existingFileName' value='    ' >");
                }

                if(fileNameList != "") {  //update tooltip
                  $("#file_" + id).attr("data-tooltip", fileNameList);

                  $("head").append("<style> #file_" + id + ":hover:after {width : " + ((fileNameCharCount + 1) * 7) + "px !important;}</style>");
                }
              } else if(firstObj.getAttribute('type') == 'radio'){
                var name = firstObj.getAttribute('name');

                $("input[name="+name+"][value=" + value + "]").attr('checked', 'checked');
              } else{
                if (firstObj.getAttribute('multiple') == null) {
                  $input.val(value);
                  $input.trigger("change", [{sampleData:true, sampleAttrMap:sampleAttrMap}]);
                } else {
                  var valueArr = value.split(",");

                  for (var j in valueArr) {
                    if (valueArr[j].charAt(0) == ' ') valueArr[j] = valueArr[j].replace(" ", "");
                    $input.multipleSelect('setSelects', valueArr);
                  }
                }
              }
            } else {
              $input.val(value);
            }
          }
        }
      }
    },
    changes = {
      project: function(projectId) {
        $('#loadingImg').show();
        _utils.hidePS();
        g_sampleIds = null;
        $("#_sampleSelect").attr("disabled", false);
        $("#_eventSelect").attr("disabled", false);
        _utils.makeAjax('sharedAjax.action', 'type=event&projectId='+projectId+'&filter=' + $('#filter').val(), null, callbacks.event);
        /*_utils.makeAjax('sharedAjax.action', 'type=sample&projectId='+projectId, null, callbacks.sample);*/
        //$('#_sampleSelect').empty();
        clearSampleAutoComplete();
        $("#confirmDiv").empty(); // Clean autofill confirmation dialogs
        $('#_sampleSelect+.ui-autocomplete-input, #_eventSelect+.ui-autocomplete-input').val('');
        $('#loadingImg').hide();
      },
      sample: function(){ /*nothing to do when sample changes*/ },
      event: function(eventName, eventId) {
        utils.processing(true);
        _utils.hidePS();
        $("#confirmDiv").empty(); // Clean autofill confirmation dialogs

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
          $("#sample-pagination-nav").hide();
          $("#gridInputDiv").hide();
          sampleData = [];
          $("#sample-pagination-nav ul").empty();
          var sampleIds = g_sampleIds.split(",");
          $("input[name='ids']").remove();
          $('#eventLoaderPage').append($('<input/>').attr({type: 'hidden', name: 'ids'}).val(g_sampleIds));
          if(sampleIds.length <= 100){
            _utils.makeAjax(
                'sharedAjax.action',
                'type=sa&projectName=' + utils.getProjectName() + '&projectId=' + $('#_projectSelect').val() + '&eventId=' + eventId + '&eventName=' + eventName + '&ids=' + g_sampleIds,
                eventName,
                callbacks.eventAttribute
            );
            sampleData.push(sampleIds);
          } else {
            var unifiedSampleIds = "";
            for (var i = 0; i < sampleIds.length; i++) {
              unifiedSampleIds += sampleIds[i];

              if(i != 0 && (i+1) % 100 == 0) {
                sampleData.push(unifiedSampleIds);
                unifiedSampleIds = "";
              } else{
                unifiedSampleIds += ",";
              }
            }

            if(unifiedSampleIds != "" || unifiedSampleIds != ","){
              sampleData.push(unifiedSampleIds);
            }

            _utils.makeAjax(
                'sharedAjax.action',
                'type=sa&projectName=' + utils.getProjectName() + '&projectId=' + $('#_projectSelect').val() + '&eventId=' + eventId + '&eventName=' + eventName + '&ids=' + sampleData[g_sampleArrIndex],
                eventName,
                callbacks.eventAttribute
            );
            for(var i=0; i<sampleData.length; i++){
              $("#sample-pagination-nav ul").append('<li><a onclick="getNextSamples('+i+','+eventId+',\''+eventName+'\')">'+(i+1)+'</a></li>');
            }

            $("#sample-pagination-nav ul li:nth-child("+(parseInt(g_sampleArrIndex,10)+1)+")").addClass("active");
            $("#pagination-warning").show();
          }

          initializeFileManagementFunctions();
        } else{
          utils.processing(false);
        }
      }
    };
// end _utils

function adjustParentDivHeight(status){
  if(status == 'open') {
    var gridInputDiv = document.getElementById('gridInputDiv');
    if (gridInputDiv.clientHeight < gridInputDiv.scrollHeight) {
      gridInputDiv.style.height = (gridInputDiv.scrollHeight + 25) + "px";
    }
  } else{
    $('#gridInputDiv').height($('#eventTable').height() + 21);
  }
}

//Stores up to 100 ids per object
var sampleData = [];

function getNextSamples(index,eventId,eventName){
  $("#pagination-loadingImg").show();
  g_sampleArrIndex = index;
  utils.error.remove();
  $("#sample-pagination-nav ul li").removeClass("active");
  $("#sample-pagination-nav ul li:nth-child("+(index+1)+")").addClass("active");
  _utils.makeAjax(
      'sharedAjax.action',
      'type=sa&projectName=' + utils.getProjectName() + '&projectId=' + $('#_projectSelect').val() + '&eventId=' + eventId + '&eventName=' + eventName + '&ids=' + sampleData[index],
      eventName,
      callbacks.eventAttribute
  );
}

var button = {
  submit: function(status) {
    utils.processing(true);
    var loadType = $('input[name="loadType"]:radio:checked').val();

    if(loadType === 'form') { //check is form is empty
      var hasAllReq = true, reqErrorMsg = ""; // require field check
      var $formFields = $('#attributeInputDiv > div.form-group > div');
      $formFields.find('[id^="req_"]:not(:hidden), [id^="req_select_"]').each(function(i, v) {
        var $node = $(v);
        if($node.val() === null || $node.val() === '') {
          hasAllReq = false;
          reqErrorMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
        }
      });

      if(!hasAllReq) {
        utils.error.add("Required Field(s):<br/>" + reqErrorMsg);
        utils.processing(false);
        return;
      }

      var allEmpty = true; // empty submission form
      var hasValueLengthErr = false; // empty submission form
      $formFields.find('[name$=".attributeValue"], [name$=".upload"]').each(function(i,v) {
        var $node = $(v);
        if($node.val() !== null && $node.val() !== '') {
          allEmpty = false;

          if($node.attr('id') && $node.siblings('[name$="attributeName"]').val()){
            var attrName = $node.siblings('[name$="attributeName"]').val();

            var valueLength = g_avDic[attrName].valueLength;
            var count = $node.val().length;

            if(valueLength != null && count > valueLength) {
              reqErrorMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + " has more than " + valueLength + " character! <br />";
              hasValueLengthErr = true;
            }
          }
        }
      });

      if(allEmpty) {
        utils.error.add("Data submission form is empty. Please insert value(s).");
        utils.processing(false);
        return;
      } else if(hasValueLengthErr){
        utils.error.add("Value Length Limitation Error(s):<br/>" + reqErrorMsg);
        utils.processing(false);
        return;
      }
    } else if(loadType === "grid"){ // check grid table data
      var listHasData = true, hasAllReq = true, allEmpty = true, hasValErr = false; reqErrorMsg = ""; // require field check
      var $formFields = $('#gridBody > tr:not(:first) > td').find(':not(:hidden):not(option), select[multiple=multiple]');  //:not(option) added for firefox

      var rowCheck = [], rowMsg = "", empty = true, rowAllReq = true, rowHasData = false, hasValueLengthErr = false;;

      if(utils.checkPR($("#_eventSelect option:selected").text())) { // if data temp is Project Registration
        $formFields.each(function (i, v) {
          var $node = $(v);

          if ($node.val() === null || $node.val() === '' || $node.val() === 'FILE MANAGEMENT') {
            if ($node.attr('id').indexOf("req_") !== -1) {
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
            } else if ($node.attr('id') && $node.attr('id').indexOf("projectName") !== -1) {
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;Project Name<br />";
            }
          } else if ($node.attr('name').indexOf("projectPublic") === -1) {
            empty = false;

            if($node.attr('id') && $node.attr('id').indexOf("projectName") === -1) {
              rowHasData = true;

              if($node.attr('id') && $node.siblings('[name$="attributeName"]').val()){
                var attrName = $node.siblings('[name$="attributeName"]').val();

                var valueLength = g_avDic[attrName].valueLength;
                var count = $node.val().length;

                if(valueLength != null && count > valueLength) {
                  rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + " has more than " + valueLength + " character! <br />";
                  hasValueLengthErr = true;
                }
              }
            }
          }

          if ($(this).attr('id') && $(this).parent().is(':last-child')) {
            rowCheck.push({"e" : empty, "r" : rowAllReq, "m" : rowMsg, "h" : rowHasData, 'v' : hasValueLengthErr});
            empty = true;
            rowMsg = "";
            rowAllReq = true;
            rowHasData = false;
            hasValueLengthErr = false;
          }
        });
      } else if(utils.checkSR($("#_eventSelect option:selected").text())) {
        $formFields.each(function (i, v) {
          var $node = $(v);

          if ($node.val() === null || $node.val() === '' || $node.val() === 'FILE MANAGEMENT') {
            if ($node.attr('id') && $node.attr('id').indexOf("req_") !== -1) {
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
            } else if($node.attr('id') && $node.attr('id').indexOf("sampleName") !== -1){
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;ID<br />";
            }
          } else if($node.is('select')){
            if($node.find('option:eq(0)').val() === '') {
              empty = false;
              rowHasData = true;
            }
          } else {
            empty = false;

            if($node.attr('id') && $node.attr('id').indexOf("sampleName") === -1) {
              rowHasData = true;

              if($node.attr('id') && $node.siblings('[name$="attributeName"]').val()){
                var attrName = $node.siblings('[name$="attributeName"]').val();

                var valueLength = g_avDic[attrName].valueLength;
                var count = $node.val().length;

                if(valueLength != null && count > valueLength) {
                  rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + " has more than " + valueLength + " character! <br />";
                  hasValueLengthErr = true;
                }
              }
            }
          }

          if($(this).attr('id') && $(this).parents('td').is(':last-child')){
            rowCheck.push({"e" : empty, "r" : rowAllReq, "m" : rowMsg, "h" : rowHasData, 'v' : hasValueLengthErr});
            empty = true;
            rowMsg = "";
            rowAllReq = true;
            rowHasData = false;
            hasValueLengthErr = false;
          }
        });
      } else {
        $formFields.each(function (i, v) {
          var $node = $(v);

          if ($node.val() === null || $node.val() === '' || $node.val() === 'FILE MANAGEMENT') {
            if ($node.attr('id') && $node.attr('id').indexOf("req_") !== -1) {
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
            } else if($node.attr('id') && $node.attr('id').indexOf("sampleSelect") !== -1){
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;ID<br />";
            }
          } else if($node.is('select')){
            if($node.find('option:eq(0)').val() === '') {
              empty = false;
              rowHasData = true;
            }
          } else {
            empty = false;

            if($node.attr('id') && $node.attr('id').indexOf("sampleSelect") === -1) {
              rowHasData = true;

              if($node.attr('id') && $node.siblings('[name$="attributeName"]').val()){
                var attrName = $node.siblings('[name$="attributeName"]').val();

                var valueLength = g_avDic[attrName].valueLength;
                var count = $node.val().length;

                if(valueLength != null && count > valueLength) {
                  rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + " has more than " + valueLength + " character! <br />";
                  hasValueLengthErr = true;
                }
              }
            }
          }

          if($(this).attr('id') && $(this).parents('td').is(':last-child')){
            rowCheck.push({"e" : empty, "r" : rowAllReq, "m" : rowMsg, "h" : rowHasData, 'v' : hasValueLengthErr});
            empty = true;
            rowMsg = "";
            rowAllReq = true;
            rowHasData = false;
            hasValueLengthErr = false;
          }
        });
      }

      for(var i=0; i < rowCheck.length; ++i){
        var e = rowCheck[i].e;

        if(e === false){
          allEmpty = false;
          if(rowCheck[i].r === false){
            hasAllReq = false;
            reqErrorMsg += "&nbsp;&nbsp;" + (i+1) + " : " + rowCheck[i].m + "<br>";
          } else if(rowCheck[i].h === false){
            listHasData = false;
            reqErrorMsg += "&nbsp;&nbsp;" + (i+1) + " :  Data submission is empty. Please insert value(s). <br>";
          } else if(rowCheck[i].v === true) {
            hasValErr = true;
            reqErrorMsg += "&nbsp;&nbsp;" + (i+1) + " : " + rowCheck[i].m + "<br>";
          }
        }
      }

      if(allEmpty){
        utils.error.add("There is no data to submit!");
        utils.processing(false);
        return;
      } else if (!hasAllReq) {
        utils.error.add("Required Field(s):<br/>" + reqErrorMsg);
        utils.processing(false);
        return;
      } else if(!listHasData) {
        utils.error.add("Error(s):<br/>" + reqErrorMsg);
        utils.processing(false);
        return;
      } else if(hasValErr){
        utils.error.add("Value Length Limitation Error(s):<br/>" + reqErrorMsg);
        utils.processing(false);
        return;
      } else{
        for(var i=0; i < rowCheck.length; ++i){
          if(rowCheck[i].e === true){
            $("#gridBody").find("tr td.gridIndex").each(function (){
              if($(this).html() == (i+1)){
                $(this).parent().remove();
              }
            });
          }
        }
      }
    }

    this.submit_form(loadType, status);
  },
  template: function() {
    if(_utils.validation()) {
      $('#download-template').modal('show');
    }
  },
  exportSample: function() {
    if(_utils.validation()) {
      var $exportSampleForm = $('<form>').attr({
        id: 'exportSampleForm',
        method: 'POST',
        action: 'eventLoader.action'
      }).css('display', 'none');

      $('<input>').attr({
        id: 'jobType',
        name: 'jobType',
        value : 'export'
      }).appendTo($exportSampleForm);

      $('<input>').attr({
        id: 'projectName',
        name: 'projectName',
        value : $("#_projectSelect option:selected").text()
      }).appendTo($exportSampleForm);

      $('<input>').attr({
        id: 'projectId',
        name: 'projectId',
        value : $("#_projectSelect").val()
      }).appendTo($exportSampleForm);

      $('<input>').attr({
        id: 'eventName',
        name: 'eventName',
        value : $("#_eventSelect option:selected").text()
      }).appendTo($exportSampleForm);

      $('<input>').attr({
        id: 'eventId',
        name: 'eventId',
        value : $("#_eventSelect").val()
      }).appendTo($exportSampleForm);

      $('<input>').attr({
        id: 'ids',
        name: 'ids',
        value : (g_sampleIds ? g_sampleIds : "")
      }).appendTo($exportSampleForm);

      $('body').append($exportSampleForm);
      $exportSampleForm.submit();
    }
  },
  submit_form: function(type, status) {
    $("#projectName").val(utils.getProjectName());
    var eventName = utils.getEventName();
    $("#eventName").val(eventName);
    var sampleName = $("#sampleSelect").val();

    if(sampleName != null) $("#sampleName").remove();
    else sampleName = $("#sampleName").val();

    $("#jobType").val(type);
    $("#status").val(status);

    //Project & ID validation
    if(type === 'form'){
      if(utils.checkPR(eventName)){
        var projectRegName = $("#_projectName").val();
        if(projectRegName == null || projectRegName === ''){
          utils.error.add("Project Name is empty!");
          utils.processing(false);
          return;
        }
      } else if(utils.checkSR(eventName)) {
        var sampleRegName = $("#_sampleName").val();
        if(sampleRegName == null || sampleRegName === ''){
          utils.error.add("ID is empty!");
          utils.processing(false);
          return;
        }
      } else if(sampleName.length == 0 && eventName.toLowerCase().indexOf('project') < 0){
        utils.error.add("Data cannot be submitted without sample. Please select a sample! If the project does not have any sample, create a new one first!");
        utils.processing(false);
        return;
      }
    } else if(type === 'file') {
      var $fileNode = $('#dataTemplate');
      if($fileNode.val() == null || $fileNode.val().length === 0) { //check if file is there
        utils.error.add("Please select a data template file.");
        utils.processing(false);
        return false;
      }
      // else {
      //   $("form#eventLoaderPage").attr("enctype", "multipart/form-data");
      // }
    }

    $('<input>').attr({
      id: 'sampleArrIndex',
      name: 'sampleArrIndex',
      value : g_sampleArrIndex
    }).css('display', 'none').appendTo($('form#eventLoaderPage'));

    $('#loadingImg').show();
    $('#pagination-loadingImg').show();
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
          var $input = $('<input/>').attr({
            'type': 'text',
            'name': 'gridList[' + g_gridLineCount + '].sampleName',
            'class': 'form-control',
            'id': '_sampleName' + g_gridLineCount,
            'style' : 'min-width:160px'
          });
          if(parentEnabled)
            $input.attr("readonly", "readonly");

          $eventLine.append(
              $('<td/>').append($input)
          );
          $eventLine.append(
              $('<td/>').attr({
                'style': (parentEnabled) ? '' : 'display:none;'
              }).append(
                  $('<div/>').attr({
                    'class': 'input-group',
                    'style': 'width: 205px;'
                  }).append(
                      $('<input/>').attr({
                        'name': 'gridList[' + g_gridLineCount + '].parentSampleName',
                        'id': '_parentSelect' + g_gridLineCount,
                        'class': 'form-control'
                      })
                  ).append(
                      $('<span/>').attr({
                        'class': 'input-group-btn'
                      }).append(
                          $('<button/>').attr({
                            'type': 'button',
                            'class': 'btn btn-primary',
                            'id': '_searchParentSample' + g_gridLineCount,
                            'onclick': 'searchSamples(this.id);'
                          }).append(
                              $('<span/>').attr({
                                'class': 'glyphicon glyphicon-search',
                                'aria-hidden': 'true'
                              })
                          )
                      )
                  )
              )
          );

          $eventLine.append(
              $('<td/>').attr({
                'style': (publicEnabled) ? '' : 'display:none;'
              }).append($('<select/>').attr({
                'name': 'gridList[' + g_gridLineCount + '].samplePublic',
                'class': 'form-control',
                'style': 'width:70px'
              }).append(vs.nyoption))
          );
        } else {
          $eventLine.append(
              $('<td/>').append(
                  $('<div/>').attr({
                    'class': 'input-group',
                    'style': 'width: 205px;'
                  }).append(
                      $('<input/>').attr({
                        'type': 'text',
                        'name': 'gridList[' + g_gridLineCount + '].sampleName',
                        'id': '_sampleSelect' + g_gridLineCount,
                        'class': 'form-control'
                      })
                  ).append(
                      $('<span/>').attr({
                        'class': 'input-group-btn'
                      }).append(
                          $('<button/>').attr({
                            'type': 'button',
                            'class': 'btn btn-primary',
                            'id': '_searchSample' + g_gridLineCount,
                            'onclick': 'searchSamples(this.id);'
                          }).append(
                              $('<span/>').attr({
                                'class': 'glyphicon glyphicon-search',
                                'aria-hidden': 'true'
                              })
                          )
                      )
                  )
              )
          );
        }
      } else {
        if(utils.checkPR(_en)) { //add project information fields for project registration
          $eventLine.append(
              $('<td/>').append($('<input/>').attr({
                    'type': 'text',
                    'name': 'gridList[' + g_gridLineCount + '].projectName',
                    'id': '_projectName' + g_gridLineCount,
                    'class': 'form-control',
                    'style' : 'min-width:160px'
                  })
              )
          );
          $eventLine.append(
              $('<td/>').append(
                  $('<select/>').attr({
                    'name': 'gridList[' + g_gridLineCount + '].projectPublic',
                    'class': 'form-control',
                    'style': 'width:70px'
                  }).append(vs.nyoption)
              )
          );
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
        var existingFileField = "";
        var uploadedFiles = "";
        if(attributeField.indexOf('type="file"') >= 0) {
          if(bean) {
            //attributeField += ("<strong>" + bean[1].substring(bean[1].indexOf("_") + 1) + "</strong>");
            var id = av_v.ma.lookupValue.name + "_$id$";
            var valArr = bean[1].split(',');
            var selectName = "gridList[" + g_gridLineCount + "].sampleName";

            //Attached Files
            var filePaths = null;
            if(dict['filePaths']) {
              $.each(dict['filePaths'], function (b_i, b_v) {
                if (av_v.ma.lookupValue.name === b_v[0]) {
                  filePaths = b_v;
                }
              });
            }
            var successfullData = $("#action_message").length && $("#action_message").text().indexOf('successfully') >=0;

            var fileNameArr = [];
            if(filePaths && filePaths[1]){
              var paths = filePaths[1];

              for(var i in paths) {
                var fileName = paths[i].substring(paths[i].lastIndexOf("/") + 1);
                fileName = fileName.substring(fileName.indexOf("_") + 1);
                fileNameArr.push(fileName);

                if(!successfullData) {
                  var $hiddenFileValue = $('<input>').attr({
                    type: 'hidden',
                    name: ltVal + "uploadFilePath",
                    value: paths[i]
                  });

                  var $fileRow = $('<p/>').text(fileName).css("margin-top", "10px");
                  $fileRow.prepend($('<span class="label label-info" style="margin-right: 10px;"/>').text("Uploaded Successfully")).append($hiddenFileValue);

                  uploadedFiles += $fileRow.prop('outerHTML');

                }
              }
            }

            var valArrLength = valArr.length;
            for(var j=0; j < valArr.length; j++){
              var fileName = valArr[j].substring(valArr[j].indexOf("_") + 1);
              if(fileName != "") {
                var exist = false;
                if(!successfullData) {
                  for (var index in fileNameArr) {
                    if (fileNameArr[index] == fileName) {
                      exist = true;
                      break;
                    }
                  }
                }
                if (!exist) {
                  existingFileField += "<div id='file-" + id + "-" + j + "'><strong><input type='hidden' name='" + ltVal + "existingFileName' value='" + fileName + "' >" + fileName + "</strong> " +
                      "<button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Download' style='float: right;margin-left: 2px;' onclick='downloadFile(\"" + fileName + "\",\"" + selectName + "\",\"" + av_v.ma.lookupValue.name + "\");'><img src='images/download_file.png' style='height: 20px;'></button>" +
                      "<button type='button' class='btn btn-default btn-xs table-tooltip' data-tooltip='Remove' style='float: right;' onclick='removeFile(\"file-" + id + "-" + j + "\");'><img src='images/cancel.png' style='height: 20px;'></button></div><br>";
                } else {
                  valArrLength -= 1;
                }
              } else {
                valArrLength -= 1;
              }
            }

            if(valArrLength > 1) {
              existingFileField += "<button type='button' class='btn btn-primary btn-xs' onclick='downloadFile(\"DOWNLOADALL\",\"" + selectName + "\",\"" + av_v.ma.lookupValue.name + "\");'>Download All</button>";
            }

            if(valArrLength > 0) {
              existingFileField += "<input type='hidden' name='" + ltVal + "existingFileName' value='   ' >";
            }
          }
        }
        attributeField = attributeField.replace(/\$existingFileField\$/g, existingFileField).replace(/\$uploadedFiles\$/g, uploadedFiles);

        var $inputNode = $('<td/>').append(
            attributeField.replace(/\$lt\$/g, ltVal).replace(/\$id\$/g, 'g_' + g_gridLineCount)
        );
        if(av_v.isSelect === true && bean) {
          utils.preSelectWithNode($inputNode, bean[1]);
        }
        if(av_v.isMulti === true) {
          $inputNode.find('select').multipleSelect({
            onOpen: function () {adjustParentDivHeight("open")},
            onClose: function () {adjustParentDivHeight("close")}
          });
          if(bean != null) utils.multiSelectWithNode($inputNode, bean[1]);
        } else if(av_v.isRadio === true){
          $inputNode.find('select').multipleSelect({
            onOpen: function () {adjustParentDivHeight("open")},
            onClose: function () {adjustParentDivHeight("close")},
            single:true});
        }

        if(av_v.isText && av_v.hasOntology) {
          _utils.ontologify(av_v.ma.ontology, $inputNode);
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
          $('input[name="gridList[' + g_gridLineCount + '].parentSampleName"]').val(dict['psn']);
          //utils.preSelect('_parentSelect' + g_gridLineCount, dict['psn']);
          $('select[name="gridList[' + g_gridLineCount + '].samplePublic"]').val(dict['sp']);
        } else if(utils.checkPR(_en)) {
          $('#_projectName' + g_gridLineCount).val(dict['pn']);
          $('select[name="gridList[' + g_gridLineCount + '].projectPublic"]').val(dict['pp']);
        } else {
          $('input[name="gridList[' + g_gridLineCount + '].sampleName"]').val(dict['sn']);
          //utils.preSelect('_sampleSelect' + g_gridLineCount, dict['sn']);
        }
      }

      $('select[id^="_"]').combobox();

      //set minimum width for date and autocomplete TDs
      $('#gridBody .hasDatepicker').parent('td').attr('style', 'min-width:163px !important;');
      $('#gridBody .ui-autocomplete-input').each(function() {
        var $this = $(this);
        if($this.next().is("span")) {
          $this.parent('td').attr('style', 'min-width:200px !important;');
        }
      });

      if(g_gridLineCount == 1) $('#gridRemoveLineButton').removeAttr("disabled");

      g_gridLineCount++;
    }

    if(beans != null && typeof parentFieldName != "undefined"){
      $("select[id*='select_"+parentFieldName+"']").change(function(event, data){
        var val = parentChildDict[parentFieldName];
        var childFieldName = val['childFieldName'];
        var parentDictName = val['dictName'];
        var selectedParent = ($(this).val()).split(" - ")[0];
        var selectedParentIdArr = ($(this).attr('id')).split("_");
        var attrIndex = (selectedParentIdArr[selectedParentIdArr.length-2] == 'f') ? parseInt(selectedParentIdArr[selectedParentIdArr.length-1],10) + 1 : selectedParentIdArr[selectedParentIdArr.length-1];
        var childSelectInput = $("select[id*='select_"+childFieldName+"_" + selectedParentIdArr[selectedParentIdArr.length-2] + "_" + attrIndex +"']");
        var childValue = (data && data.sampleData == true) ? data.sampleAttrMap[childFieldName] : null;
        childSelectInput.html('');
        childSelectInput.append('<option value=""></option>');

        if(selectedParent && selectedParent != "") {
          $.ajax({
            url: 'getChildDictionary.action',
            data: 'parentDictType=' + parentDictName + '&parentDictCode=' + selectedParent,
            cache: false,
            async: false,
            success: function (data) {
              $.each(data.aaData, function (i1, v1) {
                childSelectInput.append('<option value="' + v1.split(" - ")[0] + '">' + v1 + '</option>');
              });

              if (childValue != null) childSelectInput.val(childValue);
            }
          });
        }
      });

      var sampleAttrMap = [];

      for(var i=0; i<beans.length; i++){
        if(beans[i][0] == childFieldName){
          sampleAttrMap[childFieldName] = beans[i][1];
        }
      }

      $("select[id='select_"+parentFieldName+"_g_"+ltVal.charAt(9)+"']").trigger("change", [{sampleData:true, sampleAttrMap:sampleAttrMap}]);
    }
    initializeFileManagementFunctions();
  },
  remove_event: function() {
    var $lastChild = $("#gridBody tr:last-child");

    if(!$lastChild.is("#gridBody tr:first-child")){
      if($lastChild.is("#gridBody tr:nth-child(2)")) $('#gridRemoveLineButton').attr("disabled", true);

      $lastChild.remove();
      g_gridLineCount--;
    }
  },
  clear_form: function() {
    $("#_projectSelect").val(0);
    utils.preSelect("_projectSelect", 0);
    changes.project(0);
    $('#_parentProjectSelect, #_parentSampleSelect').combobox();
    this.clear_attr();
    $('#dataSubmissionScope').hide();
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
      var selectedProjectId = utils.getProjectId();
      utils.getProjectDetailsIntoModal(selectedProjectId, '#project-detail-table');
      $('#project-details').modal('show');
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
      $('input[name="ids"]').remove();
      changes.event(option.text, option.value);

      //Hide Sample dropdown if Registration is selected
      var _selectedType = $('input[name="loadType"]:checked').val();
      var _eventName = option.text;
      if(utils.checkSR(_eventName) || _selectedType === 'grid') {
        $('#sampleSelect, #searchSample').prop("disabled", true);
        $('#form-sample-name').hide();
      } else{
        if(!utils.checkPR(_eventName) && utils.getEventName(_eventName).toLowerCase().indexOf('project') < 0) {
          $('#sampleSelect, #searchSample').prop("disabled", false);
          $('#form-sample-name').show();
        }
      }

      var idLabel, parentIdLabel;
      if (_eventName.indexOf('Subject') > -1) {
        idLabel = "Subject ID";
        parentIdLabel = "";
      } else if (_eventName.indexOf('Sample') > -1) {
        idLabel = "Sample ID";
        parentIdLabel = "Visit ID";
      } else {
        idLabel = "Visit ID";
        parentIdLabel = "Subject ID;"
      }
      $('.id-label').text(idLabel);
      $('.parentid-label').text(parentIdLabel);
      if(parentEnabled) {
        $("#_sampleName").attr("readonly", "readonly");
        $("#parentSampleRow").show();
      } else {
        $("#_sampleName").removeAttr("readonly");
        $("#parentSampleRow").hide();
      }

      if(utils.checkPU(_eventName)){
        $('#gridAddLineButton').prop('disabled', true);
        $('#gridRemoveLineButton').prop('disabled', true);
      } else{
        $('#gridAddLineButton').removeAttr('disabled');
        $('#gridRemoveLineButton').removeAttr('disabled');
      }

      //Clear id field after event change
      $("#sampleSelect, #parentSelect").val("");
      $("#_eventSelect option").filter(function() { return this.text == option.label; }).attr("selected", true);
      $("button[id*='searchSample'], #parentSelect").click();
      /*var sampleName = $("#sampleSelect").val();

      if(sampleName && sampleName != "" && !utils.checkPU(_eventName)) {
        _utils.makeAjax(
            'getsample.action',
            'type=single&projectId=' + utils.getProjectId() + '&sampleVal=' + sampleName,
            "sampleName",
            callbacks.populateSampleInfo
        );
      }*/
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

function dataAutofill(id){
  var idArr = id.split("-");
  if(utils.checkSR($("#_eventSelect option:selected").text()) && idArr[1] > 3) idArr[1] = parseInt(idArr[1], 10) + 1;
  var autofillValue = $('#gridBody tr:nth-child(2) td:nth-child(' + idArr[1] +') input[type=text]').val();
  if(autofillValue == undefined) autofillValue = $('#gridBody tr:nth-child(2) td:nth-child(' + idArr[1] +') select').val();
  var $sampleFields = $('#gridBody tr td:nth-child(' + idArr[1] +') input, #gridBody tr td:nth-child(' + idArr[1] +') select');

  if(idArr[0] === "all"){
    var index = 1;
    var override = false;
    var overrideInputs = [];
    var overrideInputsValue = [];
    var multi = false;

    $sampleFields.each(function (i, v) {
      if(v.id != '' || v.className != '') {
        var $v = $(v);
        if(typeof $v.attr("multiple") !== 'undefined') multi = true;

        if(!multi) {
          if ($(v).val() != '' && $(v).val() != autofillValue) {
            override = true;
            overrideInputs.push($v);
            overrideInputsValue.push(autofillValue);
          } else {
            $v.val(autofillValue);
            $v.trigger("change");
          }
        } else{
          var selected_options = $v.find("option:selected");

          if(selected_options.length > 0 && v.name.indexOf("gridList[0]") != 0 && $(v).val() != autofillValue.toString()){
            override = true;
            overrideInputs.push($v);
            overrideInputsValue.push(autofillValue);
          } else {
            if(Array.isArray(autofillValue)){
              for(var i in autofillValue){
                $v.find("option[value='"+autofillValue[i]+"']").prop('selected', true);
              }
            } else{
              $v.find("option[value='"+autofillValue+"']").prop('selected', true);
            }
          }
          $v.multipleSelect("refresh");
        }
        index += 1;
      }
    });

    if(override) {
      confirmBox("","Are you sure to overwrite existing data?", function () {
        for (var j = 0; j < overrideInputs.length; j++) {
          if (Array.isArray(overrideInputsValue[j])) {
            overrideInputs[j].find("option:selected").prop("selected", false);
            for (var i in overrideInputsValue[j]) {
              overrideInputs[j].find("option[value='" + overrideInputsValue[j][i] + "']").prop('selected', true);
            }

            overrideInputs[j].multipleSelect("refresh");
          } else {
            if(multi){
              overrideInputs[j].find("option:selected").prop("selected", false);
              overrideInputs[j].find("option[value='" + overrideInputsValue[j] + "']").prop('selected', true);
              overrideInputs[j].multipleSelect("refresh");
            } else{
              overrideInputs[j].val(overrideInputsValue[j]);
              overrideInputs[j].trigger("change");
            }
          }
        }
      });
    }
  } else if(idArr[0] === "sequence"){
    var index = 0;
    var override = false;
    var overrideInputs = [];
    var overrideInputsValue = [];
    var multi = false;

    $sampleFields.each(function (i, v) {
      if(v.id != '' || v.className != ''){
        var $v = $(v);
        if(typeof $v.attr("multiple") !== 'undefined') multi = true;

        if(!multi) {
          var value = autofillValue.slice(-1) != "" && !isNaN(autofillValue.slice(-1))
              ? increaseNumberInString(autofillValue, index)
              : autofillValue + (index + 1);
          if (v.name.indexOf("gridList[0]") != 0 && $(v).val() != '' && $(v).val() != value) {
            override = true;
            overrideInputs.push($v);
            overrideInputsValue.push(value);
          } else {
            $v.val(value);
            $v.trigger("change");
          }
        } else{
          var selected_options = $v.find("option:selected");

          if(selected_options.length > 0 && v.name.indexOf("gridList[0]") != 0){
            if (Array.isArray(autofillValue)) {
              var value = [];
              for (var i in autofillValue) {
                value.push(autofillValue[i].slice(-1) != "" && !isNaN(autofillValue[i].slice(-1))
                    ? increaseNumberInString(autofillValue[i], index)
                    : autofillValue[i] + (index + 1));
              }

              overrideInputsValue.push(value);
            } else {
              var value = autofillValue != null && autofillValue.slice(-1) != "" && !isNaN(autofillValue.slice(-1))
                  ? increaseNumberInString(autofillValue, index)
                  : autofillValue + (index + 1);

              overrideInputsValue.push(value);
            }

            override = true;
            overrideInputs.push($v);
          } else {
            if (Array.isArray(autofillValue)) {
              for (var i in autofillValue) {
                var value = autofillValue[i].slice(-1) != "" && !isNaN(autofillValue[i].slice(-1))
                    ? increaseNumberInString(autofillValue[i], index)
                    : autofillValue[i] + (index + 1);

                $v.find("option[value='" + value + "']").prop('selected', true);
              }
            } else {
              var value = autofillValue != null && autofillValue.slice(-1) != "" && !isNaN(autofillValue.slice(-1))
                  ? increaseNumberInString(autofillValue, index)
                  : autofillValue + (index + 1);

              $v.find("option[value='" + value + "']").prop('selected', true);
            }

            $v.multipleSelect("refresh");
          }
        }
        index+=1;
      }
    });

    if(override) {
      confirmBox("","Are you sure to overwrite existing data?", function () {
        for (var j = 0; j < overrideInputs.length; j++) {
          if (Array.isArray(overrideInputsValue[j])) {
            overrideInputs[j].find("option:selected").prop("selected", false);
            for (var i in overrideInputsValue[j]) {
              overrideInputs[j].find("option[value='" + overrideInputsValue[j][i] + "']").prop('selected', true);
            }

            overrideInputs[j].multipleSelect("refresh");
          } else {
            if(multi){
              overrideInputs[j].find("option:selected").prop("selected", false);
              overrideInputs[j].find("option[value='" + overrideInputsValue[j] + "']").prop('selected', true);
              overrideInputs[j].multipleSelect("refresh");
            } else{
              overrideInputs[j].val(overrideInputsValue[j]);
              overrideInputs[j].trigger("change");
            }
          }
        }
      });
    }
  } else{
    var columnName = $("#gridHeader tr th:nth-child("+idArr[1]+")").text().replace('*','');
    confirmBox("Remove","Are you sure to delete contents in "+columnName+" ?",  function () {
      $sampleFields.each(function (i, v) {
        var $v = $(v);
        if(typeof $v.attr("multiple") === 'undefined'){
          $v.val("");
        } else{
          $v.find("option").each(function(i, v){
            $(this).prop('selected', false);
          });
          $v.multipleSelect("refresh");
        }
      });
    });
  }
}

function increaseNumberInString(value, increase) {
  var number = "";

  while(value !== "" && !isNaN(value.slice(-1))){
    number =  value.slice(-1) + number;
    value = value.substring(0, value.length - 1);
  }

  return value + (parseInt(number) + increase);
}

function confirmBox(action,text,func) {
  var html = '<div style="margin-top: 10px;" id="confirmOverride'+action+'">' +
      '<label class="confirm-text" style="margin-right: 5px;color:#a90329;">'+text+'</label>' +
      '<button class="yes btn btn-primary btn-xs" type="button" style="margin-right:2px">Yes</button><button class="no btn btn-primary btn-xs" type="button">No</button>' +
      '</div>';

  $('#confirmOverride'+action).remove(); //remove if exists
  $("#confirmDiv").append(html);
  var $confirmBox = $('#confirmOverride'+action);
  $confirmBox.show();
  $('#confirmOverride'+action+' > .yes').click(function () {
    func();
    $confirmBox.remove();
    func = function () {};
  });
  $('#confirmOverride'+action+' > .no').click(function () {
    $confirmBox.remove();
    func = function () {};
  });
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
      var $cancelBtn = $('<button type="button" class="btn btn-xs btn-default cancel" style="margin-left:15px;" />').text("Cancel").click(function() {
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
            $node.prepend($('<span class="label label-info" style="margin-right: 10px;"/>').text("Uploaded Successfully"));
            $node.find('button').hide();  // remove cancel button after successful upload
          }
        }
      });
      data.files.length = 0;

      //$('#progress .bar').css('width', '0%');
    },
    fail: function (e, data) {
      $('#files').find('p').each(function(i,v) {
        var $node = $(v);

        if($node.text().indexOf(data.files[0].name) >= 0) {
          if($node.find('span.label').length < 1) {
            $node.prepend($('<span class="label label-danger" style="margin-right: 10px;"/>').text("Failed to upload!"));
            $node.find('button').hide();
          }
        }
      });
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

function searchSamples(id) {
  var arrResult = [];
  var inputId = (id.indexOf("Parent") > -1) ? id.replace("searchParentSample", "parentSelect") : id.replace("searchSample", "sampleSelect");
  var $sampleSelect = $("#" + inputId);
  var sampleVal = $sampleSelect.val();
  var $sampleSelectAutocomplete;
  var searchIndex = 0;
  var maxResult = 25;
  var closeDropdown = true;

  fetchSample(arrResult, sampleVal, searchIndex*25, maxResult);

  $sampleSelect.autocomplete({
    source: function (request, response) {
      if(!$sampleSelectAutocomplete) $sampleSelectAutocomplete = $(this.menu.element);
      // get current input value
      var sValue = request.term;
      // init new search array
      var aSearch = [];
      // for each element in the main array ...
      $.each(arrResult,function(iIndex, sElement) {
        // ... if element starts with input value ...
        if (sElement.value.toLowerCase().indexOf(sValue.toLowerCase()) > -1) {
          aSearch.push(sElement);
        } else if(sElement.value === 'loadMoreData'){
          aSearch.push(sElement);
        }
      });

      response(aSearch);
    },
    select: function (a, b) {
      if(b.item.value === 'loadMoreData'){
        ++searchIndex;
        arrResult.pop(); //Remove Load More Data option

        fetchSample(arrResult, sampleVal, searchIndex*25, maxResult);
        $sampleSelect.blur();
        $(this).autocomplete("search");

        closeDropdown = false;
        return false;
      } else{
        closeDropdown = true;

        _utils.makeAjax(
            'getsample.action',
            'type=single&subType=sample&projectId=' + utils.getProjectId() + '&sampleVal=' + b.item.value,
            this.name,
            callbacks.populateSampleInfo
        );
      }
    },
    change: function () {
      generateID(this);
    },
    minLength: 0,
    delay: 0
  }).focus(function () {
    //reset result list's pageindex when focus on
    window.pageIndex = 0;
    $(this).autocomplete("search");
  }).data("autocomplete").close = function(e){
    if(closeDropdown)
      clearTimeout(this.closing), this.menu.element.is(":visible") && (this.menu.element.hide(), this.menu.deactivate(), this._trigger("close", e));
    else
      return false;
  };

  $(document).mouseup(function (e){
    if ( (!$sampleSelect.is(e.target) && $sampleSelect.has(e.target).length === 0) &&
        ($sampleSelectAutocomplete && !$sampleSelectAutocomplete.is(e.target) && $sampleSelectAutocomplete.has(e.target).length === 0)){
      $sampleSelectAutocomplete.hide();
    }
  });
}

function downloadFile(fileName, sampleNameInput, attrName) {
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
    value : $("input[type!='hidden'][name='" + sampleNameInput + "']").val()
  }).appendTo($downloadFileForm);

  $('body').append($downloadFileForm);
  $downloadFileForm.submit();
}

function removeFile(fileDivId){
  var name = fileDivId.substring(fileDivId.indexOf("-")+1,fileDivId.lastIndexOf("-"));
  $("#" + fileDivId + " + br, #" + fileDivId).remove();
  $("#attach-file-done-" + name).prop('disabled', false);
}

function fetchSample(arrResult, sampleVal, firstResult, maxResult){
  var $sampleLoadingImg = $("#sampleLoadingImg");
  var projectId = utils.getProjectId();

  if(projectId != 0){
    $.ajax({
      url: 'sharedAjax.action',
      data: 'type=sample&projectId=' + projectId + '&sampleVal=' + sampleVal + "&firstResult=" + firstResult + "&maxResult=" + maxResult + '&eventName=' + utils.getEventName(),
      cache: false,
      async: false,
      beforeSend: function (){
        $sampleLoadingImg.show();
      },
      success: function (data) {
        $.each(data.aaData, function (i1, v1) {
          if (i1 != null && v1 != null) {
            arrResult.push({label: v1.name, value: v1.name});
          }
        });

        if(data.aaData.length == 25){
          arrResult.push({label: "Load More Sample...",value: "loadMoreData"});
        }
        $sampleLoadingImg.hide();
      }
    });
  }
}

function clearSampleAutoComplete(){
  //Not call autocomplete("destroy") as it causes conflict between jquery-ui.js and jquery.ui.widget.js
  var $sampleSelect = $('#sampleSelect'), $parentSelect = $('#parentSelect');
  $sampleSelect.val("         ");
  $parentSelect.val("         ");
  $("#searchSample").trigger("click");
  $("#searchParentSample").trigger("click");
  $sampleSelect.val("");
  $parentSelect.val("");
}

function showFMPopup(id){
  id = id.substring(id.indexOf("_") + 1);
  $("#attach-file-dialog-" + id).show();
  $(".ui-blanket").css({"visibility":"visible"});
  $("#attach-file-done-" + id).prop('disabled', true);

  $('#attach-file-cancel-' + id + ',#attach-file-done-' + id).click(function() {
    $("#attach-file-dialog-" + id).hide();
    $(".ui-blanket").css({"visibility":"hidden"});
    $this = $(this);

    if($this.attr('id').indexOf("cancel") > -1) {
      $("#attach-files-" + id + " button.btn-default").each(function (i, file) {
        $this.click();
      });
    }
  });
}


function initializeFileManagementFunctions(){
  $('.upload-file').each(function(i, file) {
    var id = $(this).attr("id");
    id = id.substring(id.indexOf("-") + 1);
    $(file).fileupload({
      url: "temporaryFileUpload.action",
      paramName: "temporaryFiles",
      add: function(e, data) {
        var fileNameCount = data.files[0]['name'].length;

        var $fileRow = $('<p/>').text(
            data.files[0]['name']+ " (" + utils.formatFileSize(data.files[0]['size']) + ")"
        ).css("margin-top", "10px");

        if(fileNameCount > 50) {
          $fileRow.append($('&nbsp;<strong class="error text-danger"/>').text("[File Name is too long (Max 50 charachter)]"));
          data.files.length = 0;
        }

        var $cancelBtn = $('<button type="button" class="btn btn-xs btn-default cancel" style="margin-left:15px;" />').text("Cancel").click(function() {
          data.abort();
          data.files.length = 0;
          $(this).parent().remove();
        });
        data.context = $fileRow.append($cancelBtn).appendTo($('#attach-files-' + id));

        $('#attach-file-upload-' + id).on('click', function(e) {
          e.preventDefault();
          if (data.files.length > 0) {
            data.submit();
          }
        });

        $("#attach-file-done-" + id).prop('disabled', true);
      },
      dataType: 'json',
      sequentialUploads: true,
      done: function (e, data) {
        $('#attach-files-' + id).find('p').each(function(i,v) {
          var $node = $(v);

          if(data.files[0] && $node.text().indexOf(data.files[0].name) >= 0) {
            if($node.find('span.label').length < 1) {
              $node.prepend($('<span class="label label-info" style="margin-right: 10px;"/>').text("Uploaded Successfully"));
              $node.find('button').hide();  // remove cancel button after successful upload
            }
          }
        });
        data.files.length = 0;
        var attrName = $(this).attr("name");
        var result = data.result.result;

        var attrFilePath = attrName.substr(0, attrName.lastIndexOf('.') + 1);

        for(var i in result) {
          var $hiddenFileValue = $('<input>').attr({
            type: 'hidden',
            name: attrFilePath + "uploadFilePath",
            value: result[i]
          });

          $hiddenFileValue.appendTo($("#attach-file-dialog-" + id));
        }

        $("#attach-file-done-" + id).prop('disabled', false);
        $("#loading-" + id).hide();
      },
      fail: function (e, data) {
        $('#attach-files-' + id).find('p').each(function(i,v) {
          var $node = $(v);

          if(data.files[0] && $node.text().indexOf(data.files[0].name) >= 0) {
            if($node.find('span.label').length < 1) {
              $node.prepend($('<span class="label label-danger" style="margin-right: 10px;"/>').text("Failed to upload!"));
              $node.find('button').hide();
            }
          }
        });
        $("#loading-" + id).hide();
      },
      dropZone: $('#dropzone-' + id),
      progress: function (e, data) {
        $("#loading-" + id).show();
      }
    });

    $(document).bind('dragover', function (e) {
      var dropZone = $('#dropzone-' + id),
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
  });
}

function generateID(v) {
  var id = $(v).attr('id');
  if(id) {
    var en = utils.getEventName();
    var select;
    var isSample = false;
    if (en == "VisitRegistration" || en == "VisitUpdate")
      select = "date_Visit_Date";
    else if (en == "SampleRegistration" || en == "SampleUpdate") {
      select = "req_select_sample_type";
      isSample = true;
    }

    var isMulti = (id.indexOf("_g_") >= 0 || id.indexOf("_parentSelect") >= 0) ? true : false;
    if (isMulti) {
      var row = id.substr(id.length - 1);
      var parentVal = $("#_parentSelect" + row).val();
      var selVal = $("#" + select + "_g_" + row).val();
      selVal = (selVal) ? selVal.replace(/-/g, '') : '';
      if(isSample && selVal != '') selVal = selVal.match(/\((.*)\)/)[1];
      $("#_sampleName" + row).val(parentVal + "_" + selVal);
    } else {
      var parentVal = $("#parentSelect").val();
      var selVal =$("[id*='"+select+"_f']").val();
      selVal = (selVal) ? selVal.replace(/-/g, '') : '';
      if(isSample && selVal != '') selVal = selVal.match(/\((.*)\)/)[1];
      $("#_sampleName").val(parentVal + "_" + selVal);
    }
  }
}
