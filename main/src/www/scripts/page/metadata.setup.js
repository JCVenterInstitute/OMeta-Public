
var _html = {
  tr: '<tr id="$trid$" class="borderBottom">$tds$</tr>',
  odo:
  '<td><input type="text" name="beanList[$cnt$].options" id="options$cnt$" size="27"/></td>' +
  '<td><textarea name="beanList[$cnt$].desc" id="desc$cnt$" cols="27" rows="1"/></td>' +
  '<td><input type="text" name="beanList[$cnt$].valueLength" id="valueLength$cnt$" size="10"/></td>' +
  '<td><input type="text" name="beanList[$cnt$].ontology" id="ontology$cnt$"  placeholder="Search Ontology"/></td>',
  lar:
  '<td><input type="text" name="beanList[$cnt$].label" id="label$cnt$" size="15"/></td>' +
  '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].active" id="active$cnt$"/>' +
  '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].required" id="required$cnt$"/>',
  s:
      '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].sampleRequired" id="sampleRequired$cnt$"/>',
  pso:
  '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].projectMeta" id="projectMeta$cnt$"/>' +
  '<td class="comboBoxCB"><input type="checkbox" name="beanList[$cnt$].sampleMeta" id="sampleMeta$cnt$"/>' +
  '<td><input type="text" name="beanList[$cnt$].order" id="order$cnt$" size="2"/></td>',
  ma:
      '<td class="fix172"><select name="beanList[$cnt$].name" id="ma$cnt$">$o$</select></td>',
  ema:
  //'<td><select name="beanList[$cnt$].et" id="et$cnt$">$et$</select></td>' +
  '<td id="etTD$cnt$" class="fix172">$etTD$</td>' +
  '<td class="fix172">'+
  '  <select name="beanList[$cnt$].name" id="ema$cnt$">$ema$</select>' +
  '</td>',
  etTD_c:
  '<div><table cellpadding="0" cellspacing="0">' +
  '  <tr><td class="etTD_c" style="text-align:left;float:left;"><strong>$et$</strong></td></tr>' +
  '</table>' +
  '<div class="btn-xs btn-warning" style="max-width:60%;" title="Add attribute to the event group" id="add_$imgid$">Add Attribute</div></div>',
  etSelect: '<select name="beanList[$cnt$].et" id="et$cnt$">$et_opts$</select>',
  etHidden: '<input type="hidden" name="beanList[$cnt$].et" id="et$cnt$" value="$et$">'
};
//end _html

var _utils = {
  ontology: function() {
    window.open('http://bioportal.bioontology.org/search?opt=advanced',
        'ontologyWindow',
        'scrollbars=yes,toolbar=no,resizable=yes,width=800,height=600,left=50,top=50'
    );
    return;
  },
  flip: function(t, s) {
    $('.panelHeader').html(t+' Metadata Setup');
    $('div[id$="MADiv"]').hide();
    console.log(s);
    //$('#'+s).show();
  },
  makeAjax: function(t, projectId, eventName, cb) {
    $.ajax({
      url: 'metadataSetupAjax.action',
      cache: false,
      async: true,
      data: 'type='+t+'&projectId='+parseInt(projectId)+'&eventName='+eventName,
      success: function(res){
        if(res.dataMap.errorMsg) {
          var err = res.dataMap.errorMsg;
          if(err.indexOf("Forbidden") > 0) {
            err = utils.error.message.permission;
          }
          utils.error.add(err);
        } else {
          cb(res);
          $('#EMADiv').show();
          if($("#error_messages").get(0)) utils.error.remove();
        }
        _utils.loading.hide();
      },
      fail: function(html) {
        utils.error.add("Ajax Process has Failed.");
      },
      complete: function() {
        //_utils.loading.hide();
      }
    });
  },
  callback: {
    all: function(res) {
      var render = _utils.render;
      var dataMap = res.dataMap;
      if(dataMap && dataMap.et && dataMap.a && dataMap.pet) {
        render.et(dataMap.et);
        render.at(dataMap.a);
        render.pet(dataMap.pet);
        //render.ema(dataMap.ema); skip it for loading all meta attributes for faster rendering
      }
      if(!dataMap.ema) { //force loading image hide when there is no meta attributes
        _utils.loading.hide();
      }
    },
    ema: function(res) {
      if(res.dataMap) {
        _utils.render.ema(res.dataMap.ema);
      }
    }
  },
  render: {
    ema: function(list) {
      if(list) {
        $.each(list, function(_i,_ema) {
          _utils.add.ema(null,_ema.ema.eventName, _ema.ema.attributeName, _ema.ema.activeDB,
              _ema.ema.requiredDB, _ema.ema.sampleRequiredDB, _ema.ema.options,
              _ema.ema.desc, _ema.ema.label, _ema.ema.ontology,
              _ema.projectMeta, _ema.sampleMeta, _ema.ema.order, _ema.ema.valueLength);
        });
      }
      $('input:button[id$="AddButton"]').prop('disabled', false);
      _utils.loading.hide();
    },
    ema_d: function(res) {
      var projectId = null;
      maOptions = vs.vnoption.replace('$v$',0).replace('$n$','');
      $.each(res.dynamicList, function(i1,v1) {
        v1 = v1.ema;
        if(i1===0) {
          projectId = ''+v1.projectId;
        }
        if(v1!=null && v1.attributeName!=null) {
          maOptions+=vs.vvoption.replace(/\$v\$/g,v1.attributeName);
          emaDict[v1.attributeName]={'a':v1.activeDB,'r':v1.requiredDB,'o':v1.options,'d':v1.desc,'l':v1.label};
        }
      });
      //get project or sample meta attribute and insert table rows
      _utils.makeAjax(type==='s'?'g_sma':'g_pma', projectId, null, _utils.callback.ma);
    },
    ma: function(list) {
      $.each(list, function(i1,v1) {
        if(v1 && v1.attributeName) {
          _utils.add.ma(null,type==='s'?'smaAdditionTbody':'pmaAdditionTbody',
              v1.attributeName, v1.activeDB, v1.requiredDB, v1.options, v1.desc,v1.label, v1.ontology);
        }
      });
      _utils.loading.hide();
    },
    et: function(list) {
      if(list) {
        $.each(list, function(i1,v1) {
          if(v1!=null && v1.name!=null) {
            etOptions+=vs.vvoption.replace(/\$v\$/g,v1.name);
          }
        });
      }
    },
    at: function(list) {
      if(list) {
        maOptions=vs.vnoption.replace('$v$',0).replace('$n$','');
        $.each(list, function(i1,v1) {
          if(v1!=null && v1.name!=null) {
            maOptions+=vs.vvoption.replace(/\$v\$/g,v1.name);
          }
        });
      }
      //_utils.makeAjax('g_ema', projectId, null, _utils.callback.ema);
    },
    pet: function(list) {
      if(list) {
        var eo=vs.alloption;
        $.each(list, function(i1,v1) {
          if(v1!=null && v1.name!=null) {
            eo+=vs.vvoption.replace(/\$v\$/g,v1.name);
          }
        });
        $('#_eventSelect').html(eo);
        if(list[0]) {
          comboBoxChanged({value: list[0].name}, '_eventSelect');
          utils.preSelect('_eventSelect', list[0].name);
        }
      }
    }
  },
  add: {
    ma: function(added,div,n,a,r,o,d,l,ot) {
      $('#'+div).append(
          $(_html.tr.replace(/\$tds\$/, _html.ma+_html.lar+_html.odo)
              .replace(/\$trid\$/, '')
              .replace(/\$cnt\$/g,maCnt)
              .replace("$o$",maOptions)).toggleClass((added?'buttonAdded':''))
      );
      utils.combonize(div, 'ma'+maCnt);
      utils.preSelect('ma'+maCnt, n);
      this.setValues(a,r,o,d,l,ot);
    },
    ema: function(added,et,n,a,r,s,o,d,l,ot,pm,sm,pos,valLen) {
      //$('#etAdditionTbody').append(emaHtml.replace(/\$cnt\$/g,maCnt).replace("$et$",etOptions).replace("$ema$",maOptions));
      var _that = this,
          _et = et ? et : '',
          _ettrim = _et.split(' ').join(''), //for event types with spaces
          $_etTbody = $('#etAdditionTbody'),
          $_etTr = $_etTbody.find('tr[id*="'+_et+'"]:last'),
          $_row= $(_html.tr.replace(/\$trid\$/, 'tr_$et$_$cnt$')
              .replace(/\$tds\$/,
              (_et===''?'':_html.etHidden)
              + _html.ema.replace(/\$etTD\$/,
                  (_et===''?
                      _html.etSelect.replace(/\$et_opts\$/,etOptions):
                      _html.etTD_c.replace(/\$imgid\$/,_ettrim+'_$cnt$'))
              )
              +_html.lar
              +_html.s
              +_html.odo
              +_html.pso
          )
              .replace(/\$et\$/g, _et)
              .replace(/\$cnt\$/g,maCnt)
              .replace("$ema$",maOptions));
      added?$_row.toggleClass('buttonAdded'):null;
      //insert attribute row to a group or to tbody if the group does not exist yet
      ($_etTr&&$_etTr.length>0)?$_etTr.after($_row):$_etTbody.append($_row);

      //event type TD
      var $currETtd = $('#etTD'+maCnt);
      //merge event type rows for the same event type
      $currETtd.parent('tr').prevAll('tr[id*="'+et+'"]').each(function(i,v) {
        var $prevETtd = $(v).find('td:first[id^="etTD"]'),
            _rowspan = $prevETtd.attr('rowspan');
        if($prevETtd && $prevETtd.find('.etTD_c').text()===et) {
          $currETtd.remove();
          $prevETtd.attr('rowspan', (_rowspan?parseInt(_rowspan)+1:2));
        }
      });
      //add add image click event
      $('#add_'+_ettrim+'_'+maCnt).click(function() {
        _that.ema('add',et);
      })

      //create combobox
      utils.combonize('etAdditionTbody', maCnt);

      //preload values if given
      //utils.preSelect('et'+maCnt, et);
      utils.preSelect('ema'+maCnt, n);

      utils.checkCB('sampleRequired'+maCnt, s);
      utils.checkCB('projectMeta'+maCnt, pm);
      utils.checkCB('sampleMeta'+maCnt, sm);
      this.setValues(a,r,o,d,l,ot,pos,valLen);
    },
    setValues: function(a,r,o,d,l,ot,pos,valLen) {
      utils.checkCB('active'+maCnt, a);
      utils.checkCB('required'+maCnt, r);
      $('#options'+maCnt).val(o);
      $('#desc'+maCnt).val(d);
      $('#label'+maCnt).val(l);
      $('#order'+maCnt).val(pos);
      $('#valueLength'+maCnt).val(valLen);
      $('#ontology'+maCnt).val(ot).autocomplete({
        source: function( request, response ) {
          $.ajax({
            url: "ontologyAjax.action?t=sall",
            data: {
              maxRows: 12,
              sw: request.term.replace(' ', '%20')
            },
            success: function( data ) {
              //cleans decorated input fields when fails
              if(!data || !data.result) {
                utils.error.remove();
                $('input[id^="ontology"]').removeClass('ui-autocomplete-loading').removeAttr('style');
                utils.error.add("Ontolo	gy search failed. Please try again.");
              } else {
                response( $.map( data.result, function( item ) {
                  //decorate options
                  if(item.ontology) {
                    return {
                      label: item.tlabel + " - " + item.ontolabel,
                      value: item.tlabel,
                      ontologyId: item.ontology,
                      ontologyLabel: item.ontolabel,
                      accession: item.taccession,
                      term : item.tlabel
                    }
                  } else {
                    return {
                      label: item,
                      value: '',
                      ontology: null,
                      term: null
                    }
                  }
                }));
              }
            }
          });
        },
        minLength: 3,
        select: function(event, ui) {
          //insert ontology term to meta attribute description wrapped square brackets
          $(this).parent('td').prev('td').find('textarea:first-child').val(function(i,v){
            return (v==null ? '' : v.indexOf('[')>=0 ? v.substring(0,v.indexOf('[')) : v+' ')
                +(ui.item.accession?'['+ui.item.accession+','+ui.item.ontologyLabel+']':'');
          })
        }
      }).css('width', '100px');
      maCnt++;
    }
  },
  popup: {
    open: function(id, action) {
      $.openPopupLayer({
        name: id,
        width: 450,
        url: action
      });
    },
    attribute: function(t) {
      this.open('LPopupAddLookupValue', 'addLookupValue.action?type='+t);
    },
    dictionary: function() {
      this.open('LPopupAddDictionary', 'addDictionary.action')
    },
    ontology: function() {
      this.open('LPopupOntologySearch', 'ontologySearch.action');
    }
  },
  clean: {
    attribute: function() {
      $('tbody#etAdditionTbody, tbody#smaAdditionTbody, tbody#pmaAdditionTbody').html('');
      maCnt=0;
    },
    all: function() {
      $('input:button[id$="AddButton"]').prop('disabled', true);
      $('#_projectSelect, #_eventSelect').val(0);
      utils.preSelect('_projectSelect');
      maOptions=null;
      etOptions=null;
      emaDict={};
      this.attribute();
      $('#EMADiv').hide();
    }
  },
  loading: {
    show: function() {
      $('#loadingImg').show();
    },
    hide: function() {
      $('#loadingImg').hide();
    }
  },
  submit: function() {
    $("form").submit();
  }
};
// end _utils

function refreshData(){
  _utils.loading.show();
  var selectedProjectId = $("#_projectSelect").val()
  if(selectedProjectId != '0')
    _utils.makeAjax('g_all', selectedProjectId, null, _utils.callback.all);
  else {
    utils.error.add("Please select a project!");
    _utils.loading.hide();
  }
}

function comboBoxChanged(option, id) {
  var l;
  var cb = _utils.callback;
  if(id==='_projectSelect') {
    _utils.loading.show();
    if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
      //cleans attributes division and disable buttons
      _utils.clean.all();
      //for project or sample metatdata setup
      if(type==='s' || type==='p') {
        //create attribute list
        _utils.makeAjax('g_ema', option.value, null, cb.ema_d);
      } else { //event metadata setup
        etOptions=vs.vnoption.replace('$v$',0).replace('$n$','Select Event');
        _utils.makeAjax('g_all', option.value, null, cb.all);
      }
    } else {
      return;
    }
  } else if(id==='_eventSelect') {
    _utils.loading.show();
    //get an event specific meta attributes
    _utils.clean.attribute();
    _utils.makeAjax('g_ema', $('#_projectSelect option:selected').val(), option.value, cb.ema);
  } else {
    //loads data from ema dictionary for changing attribute, so it doesn't need to ask server for data
    if(id!=null && id.indexOf('ma')!=-1) {
      var currInd = id.substring(2), currEma;
      if(emaDict && ((currEma=emaDict[option.value])!=null)) {
        utils.checkCB('active'+currInd, currEma.a);
        utils.checkCB('required'+currInd, currEma.r);
        $('#options'+currInd).val(currEma.o);
        $('#desc'+currInd).val(currEma.d);
        $('#label'+currInd).val(currEma.l);
      }
    }
  }
}