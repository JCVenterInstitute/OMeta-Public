var _utils = {
      makeAjax: function(u,d,p,cb) {
        $.ajax({
          url:u,
          cache: false,
          async: true,
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
          $("#interactive-submission-table tr:last").hide();
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
        $("#interactive-submission-table tr:last").hide();
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
                  processing(true);
                },
                complete: function() {
                  processing(false);
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
        var multiSelectPrefix='multi(';
        var radioSelectPrefix='radio(';
        var hasDependantDict = false;

        var $attributeDiv = $("#attributeInputDiv"); //where attributes are placed
        $attributeDiv.empty(); //empty any existing contents

        var $autofillLine = $('<tr style="display: none;"/>');
        $autofillLine.append('<td class="gridIndex"></td>'); //grid row index
        var autofill_no = 2; //to specify which column is going to be autofilled (nth-element)

        g_eventAttributes = [];
        g_gridLineCount=0;
        g_avDic={};

        var requireImgHtml = '<img class="attributeIcon" src="images/icon/info_r.png"/>';
        var autofillButtonHtml = '<button type="button" class="btn btn-default btn-xs" id="all-$a$" onClick="dataAutofill(this.id)"><img src="images/autofill.png" style="width: 24px;height: 22px;"></i></button>' +
            '<button type="button" id="sequence-$b$" onclick="dataAutofill(this.id)" class="btn btn-default btn-xs"><img src="images/autofill_sequence.png" style="width: 24px;height: 22px;"></button>' +
            '<button type="button" class="btn btn-default btn-xs" id="clear-$c$" onClick="dataAutofill(this.id)"><img src="images/autofill_clear.png" style="width: 24px;height: 22px;"></i></button>';


        // //add table headers for grid view
        var gridHeaders = '', $gridHeaders = $('<tr/>');
        $gridHeaders.append($('<th/>').addClass('tableHeaderNoBG gridIndex').append('#')); //grid row index
        if(utils.checkNP(en)) {
          if(!utils.checkSR(en)) {
            $gridHeaders.append($('<th/>').addClass('tableHeaderNoBG').append('<small class="text-danger">*</small>Sample Name<br/>'));
            $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '135').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
            autofill_no+=1;
          } else {
            $gridHeaders.append(
                $('<th/>').addClass('tableHeaderNoBG').append('<small class="text-danger">*</small>Sample Name<br/>'),
                $('<th/>').addClass('tableHeaderNoBG').append('Parent Sample'),
                $('<th/>').addClass('tableHeaderNoBG').append('Public<br/>')
            );
            $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '95').replace('$a$', 2).replace('$b$', 2).replace('$c$', 2)));
            $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '190').replace('$a$', 3).replace('$b$', 3).replace('$c$', 3)));
            $autofillLine.append($('<td/>')); // empty for Public
            autofill_no = 5; //set to 5 to pass Public column
          }
        } else {
          if(utils.checkPR(en)) {
            $gridHeaders.append(
                $('<th/>').addClass('tableHeaderNoBG').append('<small class="text-danger">*</small>Project Name<br/>', requireImgHtml),
                $('<th/>').addClass('tableHeaderNoBG').append('Public<br/>', requireImgHtml)
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
            var hasOntology = (_ma.ontology && _ma.ontology !== '');
            var $attributeTr = $('<tr class="gappedTr"/>');

            $attributeTr.append(//icons and hover over information
                $('<td align="right"/>').attr('class', (isDesc ? 'table-tooltip' : '')).attr(
                    'data-tooltip', (isDesc ? _ma.desc : '')).append(
                    isRequired ? '<small class="text-danger">*</small>' : '',
                    (_ma.label != null && _ma.label !== '' ?_ma.label:_ma.lookupValue.name),
                    "&nbsp;",
                    isDesc ? requireImgHtml : ''
                    , (hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                )
            );
            $gridHeaders.append(
                $('<th/>').addClass('tableHeaderNoBG').attr('data-tooltip', (isDesc ? _ma.desc : '')).append(
                    isRequired ? '<small class="text-danger">*</small>' : '',
                    (_ma.label ? _ma.label : _ma.lookupValue.name) + '<br/>',
                    isDesc ? requireImgHtml : ''
                    ,(hasOntology ? '<img class="attributeIcon" src="images/icon/ontology.png"/>' : '')
                )
            );

            var inputElement='';
            var isSelect = (_ma.options && _ma.options !== '' && (_ma.options.indexOf(';') > 0 || _ma.options.indexOf("[{") === 0));
            var isMulti = false, isRadio = false;
            var isText = false;

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
                  $.each(givenOptions.split(';'), function (o_i, o_v) {
                    options += '<option value="' + o_v.split(" - ")[0] + '">' + o_v + '</option>';
                  });
                }
              }

              inputElement += '<select id="'  + (isRequired ? 'req_' : '') + 'select_$id$" name="$lt$attributeValue" style="min-width:35px;width:200px;" ' + (isMulti || isRadio ? 'multiple="multiple"':'') + '>' + options + '</select>';

              $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '130').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
            } else {
              var maDatatype = _ma.lookupValue.dataType;
              if(maDatatype === 'file') { //file
                inputElement += '<input type="file" id="' + maDatatype + '_$id$" name="$lt$upload" style="width:253px;"/>';
                $autofillLine.append($('<td/>'));
              } else if(maDatatype ==='date') {
                inputElement +=
                    '<div class="input-group col-sm-5">'+
                    '  <input type="text" id="' + maDatatype + '_$id$" name="$lt$attributeValue" value="$val$" style="width:160px;"/>' +
                    '  <label for="' + maDatatype + '_$id$" class="input-group-addon" style="padding:4px;"><span><i class="fa fa-calendar"></i></span></label>' +
                    '</div>';

                $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '110').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
              } else { //text input
                isText = true;
                inputElement += '<input type="text" id="' + (isRequired ? 'req_' : '') + maDatatype + '_$id$" name="$lt$attributeValue" value="$val$" style="width:160px;"/> ';

                $autofillLine.append($('<td/>').append(autofillButtonHtml.replace('$w$', '94').replace('$a$', autofill_no).replace('$b$', autofill_no).replace('$c$', autofill_no)));
              }
            }
            inputElement = inputElement.replace(/\$id\$/g,_ma.lookupValue.name.replace(/ /g,"_") + "_$id$");

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

            var $inputNode = $('<td/>').append(inputElement.replace(/\$val\$/g, '').replace(/\$id\$/g, 'f_' + count).replace(/\$lt\$/g,"beanList["+count+"]."));
            utils.smartDatePicker($inputNode);

            if(isText && hasOntology) {
              _utils.ontologify(_ma.desc, $inputNode);
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
        _utils.addGridRows(null, en);

        if(hasDependantDict){
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
        $('#loadingImg').hide();
        $("#sample-pagination-nav").show();
        $("#pagination-loadingImg").hide();
        $("#gridInputDiv").show();
        $("#autofill-option").width($('thead#gridHeader').width() + 70);

        //_utils.addGridRows(null,eventName);
      },
      populateProjectInfo: function(data, eventName) {
        if(data && data.aaData) {
          var projAttrMap = data.aaData[1].attributes;

          for(var i in projAttrMap){
            var key = i;
            var value  = projAttrMap[i];

            while(key.indexOf(" ") > -1) key = key.replace(" ", "_");

            //jquery regex for single quotation
            var $input = $("input[id*='"+key.replace(/([ #;&,.+*~\':"!^$[\]()=>|\/@])/g,'\\$1')+"']");

            if($input.length > 0) $input.val(value);
            else {
              var $select =  $("select[id*='"+key+"']");

              if($select.get(0)){
                if($select.get(0).getAttribute('multiple') == null) $select.val(value)
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
          } else {  // multiple samples view
            index = selectName.charAt(9);
            $('#gridBody .borderBottom:eq(' + index+ ') input[type="text"][name!="gridList[' + index + '].sampleName"]').val('');
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
              if (firstObj.getAttribute('type') == 'file') {
                $(firstObj).before("<strong>" + value.substring(value.indexOf("_") + 1) + "</strong>");
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
        $('#loadingImg').show();
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
        } else{
          $('#loadingImg').hide();
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
    processing(true);
    var loadType = $('input[name="loadType"]:radio:checked').val();

    if(loadType === 'form') { //check is form is empty
      var hasAllReq = true, reqErrorMsg = ""; // require field check
      var $formFields = $('#attributeInputDiv > tr > td');
      $formFields.find('[id^="req_"]:not(:hidden), [id^="req_select_"]').each(function(i, v) {
        var $node = $(v);
        if($node.val() === null || $node.val() === '') {
          hasAllReq = false;
          reqErrorMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
        }
      });

      if(!hasAllReq) {
        utils.error.add("Required Field(s):<br/>" + reqErrorMsg);
        processing(false);
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
        processing(false);
        return;
      } else if(hasValueLengthErr){
        utils.error.add("Value Length Limitation Error(s):<br/>" + reqErrorMsg);
        processing(false);
        return;
      }
    } else if(loadType === "grid"){ // check grid table data
      var listHasData = true, hasAllReq = true, allEmpty = true, hasValErr = false; reqErrorMsg = ""; // require field check
      var $formFields = $('#gridBody > tr:not(:first) > td').find(':not(:hidden):not(option), select[multiple=multiple]');  //:not(option) added for firefox

      var rowCheck = [], rowMsg = "", empty = true, rowAllReq = true, rowHasData = false, hasValueLengthErr = false;;

      if(utils.checkPR($("#_eventSelect option:selected").text())) { // if data temp is Project Registration
        $formFields.each(function (i, v) {
          var $node = $(v);

          if ($node.val() === null || $node.val() === '') {
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

          if ($node.val() === null || $node.val() === '') {
            if ($node.attr('id') && $node.attr('id').indexOf("req_") !== -1) {
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
            } else if($node.attr('id') && $node.attr('id').indexOf("sampleName") !== -1){
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;Sample Name<br />";
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

          if ($node.val() === null || $node.val() === '') {
            if ($node.attr('id') && $node.attr('id').indexOf("req_") !== -1) {
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;" + $node.siblings('[name$="attributeName"]').val() + "<br />";
            } else if($node.attr('id') && $node.attr('id').indexOf("sampleSelect") !== -1){
              rowAllReq = false;
              rowMsg += "&nbsp;&nbsp;Sample Name<br />";
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
        processing(false);
        return;
      } else if (!hasAllReq) {
        utils.error.add("Required Field(s):<br/>" + reqErrorMsg);
        processing(false);
        return;
      } else if(!listHasData) {
        utils.error.add("Error(s):<br/>" + reqErrorMsg);
        processing(false);
        return;
      } else if(hasValErr){
        utils.error.add("Value Length Limitation Error(s):<br/>" + reqErrorMsg);
        processing(false);
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
    var eventName = utils.getEventName();
    $("#eventName").val(eventName);
    var sampleName = $("#sampleSelect").val();

    if(sampleName != null) $("#sampleName").remove();
    else sampleName = $("#sampleName").val();

    $("#jobType").val(type);
    $("#status").val(status);

    //Project & Sample name validation
    if(type === 'form'){
      if(utils.checkPR(eventName)){
        var projectRegName = $("#_projectName").val();
        if(projectRegName == null || projectRegName === ''){
          utils.error.add("Project Name is empty!");
          processing(false);
          return;
        }
      } else if(utils.checkSR(eventName)) {
        var sampleRegName = $("#_sampleName").val();
        if(sampleRegName == null || sampleRegName === ''){
          utils.error.add("Sample Name is empty!");
          processing(false);
          return;
        }
      } else if(sampleName.length == 0 && eventName.toLowerCase().indexOf('project') < 0){
        utils.error.add("Data cannot be submitted without sample. Please select a sample! If the project does not have any sample, create a new one first!");
        processing(false);
        return;
      }
    } else if(type === 'file') {
      var $fileNode = $('#dataTemplate');
      if($fileNode.val() == null || $fileNode.val().length === 0) { //check if file is there
        utils.error.add("Please select a data template file.");
        processing(false);
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
          $eventLine.append(
              $('<td/>').append($('<input/>').attr({
                    'type': 'text',
                    'name': 'gridList[' + g_gridLineCount + '].sampleName',
                    'id': '_sampleName' + g_gridLineCount,
                    'style' : 'width:160px'
                  })
              )
          );
          $eventLine.append(
              $('<td/>').append(
                  $('<div/>').attr({
                    'class': 'input-group'
                  }).append(
                      $('<input/>').attr({
                        'name': 'gridList[' + g_gridLineCount + '].parentSampleName',
                        'id': '_parentSelect' + g_gridLineCount,
                        'class': 'form-control search-box',
                        'style': 'width: 168px;'
                      })
                  ).append(
                      $('<span/>').attr({
                        'class': 'input-group-btn'
                      }).append(
                          $('<button/>').attr({
                            'type': 'button',
                            'class': 'btn btn-default btn-xs search-button',
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
              $('<td/>').append(
                  $('<select/>').attr({
                    'name': 'gridList[' + g_gridLineCount + '].samplePublic',
                    'style': 'width:52px'
                  }).append(vs.nyoption)
              )
          );
        } else {
          $eventLine.append(
              $('<td/>').attr({'style': 'width: 205px;'}).append(
                  $('<div/>').attr({
                    'class': 'input-group'
                  }).append(
                      $('<input/>').attr({
                        'type': 'text',
                        'name': 'gridList[' + g_gridLineCount + '].sampleName',
                        'id': '_sampleSelect' + g_gridLineCount,
                        'class': 'form-control search-box',
                        'style': 'width: 168px;'
                      })
                  ).append(
                      $('<span/>').attr({
                        'class': 'input-group-btn'
                      }).append(
                          $('<button/>').attr({
                            'type': 'button',
                            'class': 'btn btn-default btn-xs search-button',
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
                    'style' : 'width:160px'
                  })
              )/*.append($('<hidden/>').attr({
               'name': 'gridList[' + g_gridLineCount + '].projectPublic',
               'value': '1'
               }))*/
          );
          $eventLine.append(
              $('<td/>').append(
                  $('<select/>').attr({
                    'name': 'gridList[' + g_gridLineCount + '].projectPublic',
                    'style': 'width:52px'
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
          $inputNode.find('select').multipleSelect({
            onOpen: function () {adjustParentDivHeight("open")},
            onClose: function () {adjustParentDivHeight("close")}
          });
        } else if(av_v.isRadio === true){
          $inputNode.find('select').multipleSelect({
            onOpen: function () {adjustParentDivHeight("open")},
            onClose: function () {adjustParentDivHeight("close")},
            single:true});
        }

        if(av_v.isText && av_v.hasOntology) {
          _utils.ontologify(av_v.ma.desc, $inputNode);
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
      var whs = $(window).width()*3/4;
      whs = whs < 550 ? 550 : whs;
      $.openPopupLayer({
        name: "LPopupProjectDetails",
        width: whs,
        height: 50,
        url: "popup.action?t=projectDetails_pop&projectName=" + selectedProject +"&projectId="+selectedProjectId
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
      $('input[name="ids"]').remove();
      changes.event(option.text, option.value);

      //Hide Sample dropdown if SampleRegistration is selected
      var _selectedType = $('input[name="loadType"]:checked').val();
      var _eventName = option.text;
      if(utils.checkSR(_eventName) || _selectedType === 'grid') {
        $("#interactive-submission-table tr:last").hide();
      } else{
        if(!utils.checkPR(_eventName) && utils.getEventName(_eventName).toLowerCase().indexOf('project') < 0) $("#interactive-submission-table tr:last").show();
      }

      if(utils.checkPU(_eventName)){
        $('#gridAddLineButton').prop('disabled', true);
        $('#gridRemoveLineButton').prop('disabled', true);
      } else{
        $('#gridAddLineButton').removeAttr('disabled');
        $('#gridRemoveLineButton').removeAttr('disabled');
      }

      var sampleName = $("#sampleSelect").val();

      if(sampleName && sampleName != "" && !utils.checkPU(_eventName)) {
        _utils.makeAjax(
            'getsample.action',
            'type=single&projectId=' + utils.getProjectId() + '&sampleVal=' + sampleName,
            "sampleName",
            callbacks.populateSampleInfo
        );
      }
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
  var autofillValue = $('#gridBody tr:nth-child(2) td:nth-child(' + idArr[1] +') input[type=text]').val();
  if(autofillValue == undefined) autofillValue = $('#gridBody tr:nth-child(2) td:nth-child(' + idArr[1] +') select').val();
  var $sampleFields = $('#gridBody tr td:nth-child(' + idArr[1] +') input, #gridBody tr td:nth-child(' + idArr[1] +') select');

  if(idArr[0] === "all"){
    var index = 1;
    var override = false;
    var overrideInputs = [];

    $sampleFields.each(function (i, v) {
      if(v.id != '' || v.className != '') {
        var $v = $(v), multi = false;
        if(typeof $v.attr("multiple") !== 'undefined') multi = true;

        if(!multi) {
          if ($(v).val() != '' && $(v).val() != autofillValue) {
            override = true;
            overrideInputs.push($v);
          } else {
            $v.val(autofillValue);
            $v.trigger("change");
          }
        } else{
          $v.find("option:selected").prop("selected", false);

          if(Array.isArray(autofillValue)){
            for(var i in autofillValue){
              $v.find("option[value='"+autofillValue[i]+"']").prop('selected', true);
            }
          } else{
            $v.find("option[value='"+autofillValue+"']").prop('selected', true);
          }

          $v.multipleSelect("refresh");
        }
        index += 1;
      }
    });

    if(override) {
      confirmBox("","Are you sure to overwrite existing data?", function () {
        for (var j = 0; j < overrideInputs.length; j++) {
          overrideInputs[j].val(autofillValue);
          overrideInputs[j].trigger("change");
        }
      });
    }
  } else if(idArr[0] === "sequence"){
    var index = 1;
    var override = false;
    var overrideInputs = [];

    $sampleFields.each(function (i, v) {
      if(v.id != '' || v.className != ''){
        var $v = $(v), multi = false;
        if(typeof $v.attr("multiple") !== 'undefined') multi = true;

        var value = autofillValue + index;
        if(!multi) {
          if ($(v).val() != '' && $(v).val() != autofillValue) {
            override = true;
            overrideInputs.push($v);
          } else {
            $v.val(value);
            $v.trigger("change");
          }
        } else{
          $v.find("option:selected").prop("selected", false);

          if(Array.isArray(autofillValue)){
            for(var i in autofillValue){
              $v.find("option[value='"+autofillValue[i]+"']").prop('selected', true);
            }
          } else{
            $v.find("option[value='"+autofillValue+"']").prop('selected', true);
          }

          $v.multipleSelect("refresh");
        }
        index+=1;
      }
    });

    if(override) {
      confirmBox("","Are you sure to overwrite existing data?", function () {
        for (var j = 0; j < overrideInputs.length; j++) {
          overrideInputs[j].val(autofillValue);
          overrideInputs[j].trigger("change");
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

function confirmBox(action,text,func) {
  var html = '<div style="margin-top: 10px;" id="confirmOverride'+action+'">' +
      '<label class="confirm-text" style="margin-right: 5px;color:#a90329;">'+text+'</label>' +
      '<button class="yes btn btn-success btn-xs" type="button" style="margin-right:2px">Yes</button><button class="no btn btn-primary btn-xs" type="button">No</button>' +
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

function fetchSample(arrResult, sampleVal, firstResult, maxResult){
  var $sampleLoadingImg = $("#sampleLoadingImg");
  var projectId = utils.getProjectId();

  if(projectId != 0){
    $.ajax({
      url: 'sharedAjax.action',
      data: 'type=sample&projectId=' + projectId + '&sampleVal=' + sampleVal + "&firstResult=" + firstResult + "&maxResult=" + maxResult,
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

function processing(isProcessing){
  if(isProcessing){
    $("#popupLayerScreenLocker").show();
    $("#processingDiv").show();
  } else{
    $("#popupLayerScreenLocker").hide();
    $("#processingDiv").hide();
  }
}