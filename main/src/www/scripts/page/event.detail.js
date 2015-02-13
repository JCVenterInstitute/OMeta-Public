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
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'projectName', value: $('#_projectSelect:selected').text()}));
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'eventName', value: 'ProjectUpdate'}));
                $('#eventDetailPage').append($('<input/>').attr({type: 'hidden', name: 'jobType', value: 'projectedit'}));
                this.submit('project');
            },
            sampleEvent: function() {
                var sampleIds = '';
                $('#sampleTableBody input[id^=sampleCB]:checked').each(function(i,v) {
                    sampleIds += v.id.substr(v.id.indexOf('_') + 1) + ',';
                });
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
        }
    },
    buttonSwitch = function(node, name) {
        if(node==null) { node = document.getElementById(name); }
        if(node.src.match('details_close')){ node.src = openBtn; } else { node.src = closeBtn; }
    };


function comboBoxChanged(option, id) {
    if(id==='_projectSelect') {
        document.getElementById('sampleTable').getElementsByTagName('img')[0].src = openBtn;
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
        url: ajaxType === 'project' ? 'getproject.action' : 'sharedAjax.action',
        cache: false,
        async: ajaxType === 'project'?false:true,
        data: "type="+ajaxType+"&projectId="+projectId+"&sampleId="+sampleId+"&eventId="+eventId,
        success: function(html){
            if(html.aaData) {
                if(ajaxType == "project") {
                    $(html.aaData).each(function(i1,v1) {
                        if(v1) {
                            $.each(v1, function(i2,v2) {
                                if(v2) {
                                    if(i2==="editable") { $("#editable").val(v2); }
                                    else {
                                        if(v2.indexOf("http://") > -1) v2 = '<a href="#" onclick="window.open(&quot;' + v2 + '&quot;);">' + v2 + '</a>';
                                        content += '<tr class="even"><td width="25%">'+i2+'</td><td>'+v2+'</td></tr>';
                                    }
                                }
                            });
                            rtnVal = true;
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