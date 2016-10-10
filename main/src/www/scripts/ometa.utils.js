/*
 * Copyright J. Craig Venter Institute, 2013
 *
 * The creation of this program was supported by J. Craig Venter Institute
 * and National Institute for Allergy and Infectious Diseases (NIAID),
 * Contract number HHSN272200900007C.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var utils = {
  getProjectName: function() {
    var pnode = $("#_projectSelect option:selected");
    return pnode.val() === '0' ? null : pnode.text();
  },
  getProjectId: function() {
    var pnode = $("#_projectSelect option:selected");
    return pnode.val();
  },
  getEventName: function(en) {
    var enode = $("#_eventSelect option:selected");
    return en || (enode.val() === '0' ? null : enode.text());
  },
  getEventId: function() {
    var enode = $("#_eventSelect option:selected");
    return enode.val();
  },
  getSampleName: function() {
    return $("#_sampleSelect option:selected").text();
  },
  getLoadType: function() {
    return $('input[name="loadType"]:checked').val();
  },
  checkSR: function(en) {
    en = this.getEventName(en);
    en = en ? en.toLowerCase() : "";
    return en.indexOf('sample') >= 0 && en.indexOf('registration') > 0;
  },
  checkPR: function(en) {
    en = this.getEventName(en);
    en = en ? en.toLowerCase() : "";
    return en.indexOf('project') >= 0 && en.indexOf('registration') > 0;
  },
  checkPU: function(en) {
    en = this.getEventName(en);
    en = en ? en.toLowerCase() : "";
    return en.indexOf('project') >= 0 && en.indexOf('update') > 0;
  },
  checkSU: function(en) {
    en = this.getEventName(en);
    en = en ? en.toLowerCase() : "";
    return en.indexOf('sample') >= 0 && en.indexOf('update') > 0;
  },
  checkNP: function(en) {
    en = this.getEventName(en);
    en = en ? en.toLowerCase() : "";
    return en.indexOf('project') < 0;
  },
  combonize: function(div, id) {
    var selector='select';
    if(div) { selector='#'+div+' '+selector; }
    if(id) { selector+='[id$="'+id+'"]'; }
    $(selector).combobox();
  },
  preSelect: function(id, val) {
    var $selNode=$('#'+id);
    $selNode.change(function() {
      $(this).next().val($(this).children(':selected').text());
    });
    $("#"+id+" option").filter(function() {
      return $(this).text()==val || $(this).val()==val;
    }).attr('selected', true);
    $selNode.change();
  },
  preSelect2: function(id, val) {
    $("#"+id+" option").filter(function() {
      return $(this).text()==val || $(this).val()==val;
    }).attr('selected', true);
  },
  preSelectWithNode: function($node, val) {
    var valArr = val.split(';');
    $($node).find("select option").filter(function() {
      return valArr.indexOf($(this).text()) >= 0 || valArr.indexOf($(this).val()) >= 0;
    }).attr('selected', true);
  },
  listToOptions: function(l, t, k, k2) {
    var os='', o=t==='vv'?vs.vvoption:vs.vnoption;
    $.each(l, function(i1,v1) {
      if(v1 && v1[k]) {
        os+=o.replace(/\$v\$/g,v1[k]);
      }
    });
    return os;
  },
  checkCB: function(id, val) {
    $('#'+id).prop('checked', val && (val===1||val===true?true:false));
  },
  initDatePicker: function() {
    $('input[id*="Date"], input[id*="date"], input[id*="DOB"]').datepicker({
      dateFormat: 'yy-mm-dd',showOn: 'button',
      buttonImageOnly: true, buttonImage: 'images/jqueryUI/icon_cal_21x19.png'
    });
  },
  smartDatePicker: function($node) {
    $node.find('input[id^="date_"]').datepicker({dateFormat: 'yy-mm-dd'});
  },
  error: {
    check: function() {
      var _error = $('#error_messages').val();
      if(typeof _error!='undefined' && _error!='') {
        this.add(_error);
      }
    },
    add: function(msg) {
      this.remove();
      $('#errorMessagesPanel').append(
          $('<input type="hidden" id="error_messages">').attr('value',msg),
          $('<input type="button" class="btn btn-danger" id="error_btn" '
              + 'onclick="utils.error.show(\'error_messages\');return false;" '
              + 'value="ERROR: Click Here to See the Error." />'
          )
      );
    },
    remove: function() {
      if($('#error_messages')) {
        $('#error_messages, #error_btn, [class^="alert"]').remove();
      }
    },
    show: function(objectId) {
      $.openPopupLayer({
        name: "erroPopup",
        width: 450,
        url: "html/errorPopup.html?msg="+$('#'+objectId).val()
      });
    },
    baloon: function(msg) {
      var $errorNode = $('<div class="alert_error btn btn-danger" onclick="$(\'.alert_error\').remove();">').html(
          "<strong>" + msg + "</strong>"
      );
      $('#errorMessagesPanel').append($errorNode);
    },
    alert: function(msg) {
      var $errorNode = $('<div class="alert alert-danger alert-dismissible" role="alert">').html(
          '<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>' +
          '<strong>Warning!</strong> ' + msg);
      $('#errorMessagesPanel').append($errorNode);
    },
    message: {
      permission: "You do not have the permission to access the project"
    }
  },
  formatFileSize: function (bytes) {
    if(bytes === 0) { return "0.00 B"; }
    var e = Math.floor(Math.log(bytes) / Math.log(1024));
    return (bytes/Math.pow(1024, e)).toFixed(2)+' '+' KMGTP'.charAt(e)+'B';
  }
};

var vs = {
    vvoption: '<option value="$v$">$v$</option>',
    vnoption: '<option value="$v$">$n$</option>',
    ynoption: '<option value="1">Yes</option><option value="0">No</option>',
    nyoption: '<option value="0">No</option><option value="1">Yes</option>',
    alloption: '<option value="0">All..</option>',
    empty: '<option value=""></option>'
};

//additional jquery methods
(function($) {
	// jquery.fieldCollapse
	$.fn.fieldCollapse = function(options) {
		var settings = {
			collapseClass: 'jquery-fieldcollapse-collapseclass',
			contentSelector: ':not(legend)',
			startCollapsed: false
		}
		return this.each(function() {	
			if(options) {
				$.extend(settings, options);
			}
			var $this = $(this);
			$this.find('legend').click(function(e) {
				$this.children(settings.contentSelector).slideToggle(250, function() {
					if($this.hasClass(settings.collapseClass)) {
						$this.removeClass(settings.collapseClass);
					} else {
						$this.addClass(settings.collapseClass);
					}
				});
			});
			if(settings.startCollapsed) {
				$this.find(settings.contentSelector).slideToggle(0);
				$this.addClass(settings.collapseClass);
			}
		});
	};

    // jquery.jmpopups.js 
    var openedPopups = [];
    var popupLayerScreenLocker = false;
    var focusableElement = [];
    var setupJqueryMPopups = {
        screenLockerBackground: "#000",
        screenLockerOpacity: "0.5"
    };

    $.setupJMPopups = function(settings) {
        setupJqueryMPopups = jQuery.extend(setupJqueryMPopups, settings);
        return this;
    };

    $.openPopupLayer = function(settings) {
        if (typeof(settings.name) != "undefined" && !checkIfItExists(settings.name)) {
            settings = jQuery.extend({
                width: "auto",
                height: "auto",
                parameters: {},
                target: "",
                success: function() {},
                error: function() {},
                beforeClose: function() {},
                afterClose: function() {},
                reloadSuccess: null,
                cache: false
            }, settings);
            loadPopupLayerContent(settings, true);
            return this;
        }
    };
    
    $.closePopupLayer = function(name) {
        if (name) {
            for (var i = 0; i < openedPopups.length; i++) {
                if (openedPopups[i].name == name) {
                    var thisPopup = openedPopups[i];
                    
                    openedPopups.splice(i,1)
                    
                    thisPopup.beforeClose();
                    
                    $("#popupLayer_" + name).fadeOut(function(){
                        $("#popupLayer_" + name).remove();
                    
                        focusableElement.pop();
    
                        if (focusableElement.length > 0) {
                            $(focusableElement[focusableElement.length-1]).focus();
                        }
    
                        thisPopup.afterClose();
                        hideScreenLocker(name);
                    });
                    
                    
   
                    break;
                }
            }
        } else {
            if (openedPopups.length > 0) {
                $.closePopupLayer(openedPopups[openedPopups.length-1].name);
            }
        }
        
        return this;
    }
    
    $.reloadPopupLayer = function(name, callback) {
        if (name) {
            for (var i = 0; i < openedPopups.length; i++) {
                if (openedPopups[i].name == name) {
                    if (callback) {
                        openedPopups[i].reloadSuccess = callback;
                    }
                    
                    loadPopupLayerContent(openedPopups[i], false);
                    break;
                }
            }
        } else {
            if (openedPopups.length > 0) {
                $.reloadPopupLayer(openedPopups[openedPopups.length-1].name);
            }
        }
        
        return this;
    }

    function setScreenLockerSize() {
        if (popupLayerScreenLocker) {
            $('#popupLayerScreenLocker').height($(document).height() + "px");
            $('#popupLayerScreenLocker').width($(document.body).outerWidth(true) + "px");
        }
    }
    
    function checkIfItExists(name) {
        if (name) {
            for (var i = 0; i < openedPopups.length; i++) {
                if (openedPopups[i].name == name) {
                    return true;
                }
            }
        }
        return false;
    }
    
    function showScreenLocker() {
        if ($("#popupLayerScreenLocker").length) {
            if (openedPopups.length == 1) {
                popupLayerScreenLocker = true;
                setScreenLockerSize();
                $('#popupLayerScreenLocker').fadeIn();
            }
   
            if ($.browser.msie && $.browser.version < 7) {
                $("select:not(.hidden-by-jmp)").addClass("hidden-by-jmp hidden-by-" + openedPopups[openedPopups.length-1].name).css("visibility","hidden");
            }
            
            $('#popupLayerScreenLocker').css("z-index",parseInt(openedPopups.length == 1 ? 999 : $("#popupLayer_" + openedPopups[openedPopups.length - 2].name).css("z-index")) + 1);
        } else {
            $("body").append("<div id='popupLayerScreenLocker'><!-- --></div>");
            $("#popupLayerScreenLocker").css({
                position: "absolute",
                background: setupJqueryMPopups.screenLockerBackground,
                left: "0",
                top: "0",
                opacity: setupJqueryMPopups.screenLockerOpacity,
                display: "none"
            });
            showScreenLocker();

            $("#popupLayerScreenLocker").click(function() {
                $.closePopupLayer();
            });
        }
    }
    
    function hideScreenLocker(popupName) {
        if (openedPopups.length == 0) {
            screenlocker = false;
            $('#popupLayerScreenLocker').fadeOut();
        } else {
            $('#popupLayerScreenLocker').css("z-index",parseInt($("#popupLayer_" + openedPopups[openedPopups.length - 1].name).css("z-index")) - 1);
        }
   
        if ($.browser.msie && $.browser.version < 7) {
            $("select.hidden-by-" + popupName).removeClass("hidden-by-jmp hidden-by-" + popupName).css("visibility","visible");
        }
    }
    
    function setPopupLayersPosition(popupElement, animate) {
        if (popupElement) {
            if (popupElement.width() < $(window).width()) {
                var leftPosition = (document.documentElement.offsetWidth - popupElement.width()) / 2;
            } else {
                var leftPosition = document.documentElement.scrollLeft + 5;
            }

            if (popupElement.height() < $(window).height()) {
                var topPosition = document.documentElement.scrollTop + ($(window).height() - popupElement.height()) / 2;
            } else {
                var topPosition = document.documentElement.scrollTop + 5;
            }
            
            var positions = {
                left: leftPosition + "px",
                top: topPosition + "px"
            };
            
            if (!animate) {
                popupElement.css(positions);
            } else {
                popupElement.animate(positions, "slow");
            }
                        
            setScreenLockerSize();
        } else {
            for (var i = 0; i < openedPopups.length; i++) {
                setPopupLayersPosition($("#popupLayer_" + openedPopups[i].name), true);
            }
        }
    }

    function showPopupLayerContent(popupObject, newElement, data) {
        var idElement = "popupLayer_" + popupObject.name;

        if (newElement) {
            showScreenLocker();
            
            $("body").append("<div id='" + idElement + "'><!-- --></div>");
            
            var zIndex = parseInt(openedPopups.length == 1 ? 1000 : $("#popupLayer_" + openedPopups[openedPopups.length - 2].name).css("z-index")) + 2;
        }  else {
            var zIndex = $("#" + idElement).css("z-index");
        }

        var popupElement = $("#" + idElement);
        
        popupElement.css({
            visibility: "hidden",
            'min-width': popupObject.width == "auto" ? "" : popupObject.width + "px",
            height: popupObject.height == "auto" ? "" : popupObject.height + "px",
            position: "absolute",
            "z-index": zIndex
        });
        
        var linkAtTop = "<a href='#' class='jmp-link-at-top' style='position:absolute; left:-9999px; top:-1px;'>&nbsp;</a><input class='jmp-link-at-top' style='position:absolute; left:-9999px; top:-1px;' />";
        var linkAtBottom = "<a href='#' class='jmp-link-at-bottom' style='position:absolute; left:-9999px; bottom:-1px;'>&nbsp;</a><input class='jmp-link-at-bottom' style='position:absolute; left:-9999px; top:-1px;' />";

        popupElement.html(linkAtTop + data + linkAtBottom);
        
        setPopupLayersPosition(popupElement);

        popupElement.css("display","none");
        popupElement.css("visibility","visible");
        
        if (newElement) {
            popupElement.fadeIn();
        } else {
            popupElement.show();
        }

        $("#" + idElement + " .jmp-link-at-top, " +
          "#" + idElement + " .jmp-link-at-bottom").focus(function(){
            $(focusableElement[focusableElement.length-1]).focus();
        });
        
        var jFocusableElements = $("#" + idElement + " a:visible:not(.jmp-link-at-top, .jmp-link-at-bottom), " +
                                   "#" + idElement + " *:input:visible:not(.jmp-link-at-top, .jmp-link-at-bottom)");
                           
        if (jFocusableElements.length == 0) {
            var linkInsidePopup = "<a href='#' class='jmp-link-inside-popup' style='position:absolute; left:-9999px;'>&nbsp;</a>";
            popupElement.find(".jmp-link-at-top").after(linkInsidePopup);
            focusableElement.push($(popupElement).find(".jmp-link-inside-popup")[0]);
        } else {
            jFocusableElements.each(function(){
                if (!$(this).hasClass("jmp-link-at-top") && !$(this).hasClass("jmp-link-at-bottom")) {
                    focusableElement.push(this);
                    return false;
                }
            });
        }
        
        $(focusableElement[focusableElement.length-1]).focus();

        popupObject.success();
        
        if (popupObject.reloadSuccess) {
            popupObject.reloadSuccess();
            popupObject.reloadSuccess = null;
        }
    }
    
    function loadPopupLayerContent(popupObject, newElement) {
        if (newElement) {
            openedPopups.push(popupObject);
        }
        
        if (popupObject.target != "") {
            showPopupLayerContent(popupObject, newElement, $("#" + popupObject.target).html());
        } else {
            $.ajax({
                url: popupObject.url,
                data: popupObject.parameters,
                cache: popupObject.cache,
                dataType: "html",
                method: "GET",
                success: function(data) {
                    showPopupLayerContent(popupObject, newElement, data);
                },
                error: popupObject.error
            });
        }
    }
    
    $(window).resize(function(){
        setScreenLockerSize();
        setPopupLayersPosition();
    });
    
    $(document).keydown(function(e){
        if (e.keyCode == 27) {
            $.closePopupLayer();
        }
    });
})(jQuery);