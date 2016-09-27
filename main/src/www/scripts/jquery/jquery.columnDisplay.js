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

/**
 * jQuery plugin for managing the visibility of table columns.  Management is done through a set of constructed checkboxes.
 * 
 * Configuration
 * -------------
 * @options : an object literal containing configuration settings.  Available settings are:
 *   checkBoxContainer : A valid jQuery selector representing the checkbox list container.  Defaults to the document's body element.
 *   checkBoxDivClass: This plugin wraps each checkbox in a div to give flexibility in styling.  This is the class that will be applied to it.  Defaults to cd-box-container.
 *   checkBoxDisabledClass: An additional class to the checkBoxDivClass that is applied when a column's toggling ability is disabled.  Defaults to cd-cbox-container-disabled.
 *   checkBoxClass: The name of the class applied to each generated checkbox.  Defaults to cd-cbox.
 *   checkBoxLabelClass: Each checkbox has an associated label element.  This is the class associated with it.  Defaults to cd-cbox-label.
 *   hideCols: An array containing 0-based indices indicating columns to initially hide.  Using [0, 1] would hide the first two columns.  Defaults to [].
 *   disableCols: An array containing 0-based indices indicating columns to initially disable toggling for.  Using [0, 1] would disable toggling for the first two columns.  Defaults to [].
 *   toggleCallback: A function that is called each time a column is toggled.  This function is passed the following parameters:
 *      @index : The 0-based index of the column toggled.
 *      @is_hidden : A boolean describing whether the toggled column is now hidden or shown.  A true result indicates the column is hidden post-toggle.
 *      @table : A jQuery element for the table containing the toggled column.
 *
 * Methods
 * -------
 * $el.columnDisplay('hideCol', index) : Partially implemented.  Hides column at specified index in table $el.
 * $el.columnDisplay('showCol', index) : Partially implemented.  Shows column at specified index in table $el.
 * $el.columnDisplay('disableCol', index) : To be implemented.  Disables toggling for the column at specified index in table $el.
 * $el.columnDisplay('enableCol', index) : To be implemented.  Enables toggling for the column at specified index in table $el.
 * $el.columnDisplay('removeColumnDisplay') : To be implemented.  Removes columnDisplay functionality from the table $el.  Any hidden columns will remain hidden.
 */
var displayedAttributes = new Array();

(function($) {
	
	var methods = {
		init: function(options) {
			
			var settings = {
				checkBoxContainer: 'body',
				checkBoxDivClass: 'cd-cbox-container',
				checkBoxDivDisabledClass: 'cd-cbox-container-disabled',
				checkBoxClass: 'cd-cbox',
				checkBoxLabelClass: 'cd-cbox-label',
				hideCols: [],
				disableCols: [],
				toggleCallback: function(index, is_hidden, $table) {}
			}
			
			return this.each(function(index, el) {

				var container_id = 'cd-' + index;
				
				if(options) {
					$.extend(settings, options);
				}
				
				var $this = $(this);
				
				$this.find('thead th').each(function(index, el) {

                    displayedAttributes[index] = $(el).text();
					
					var $cbox = $('<input type="checkbox" checked="checked" id="' + container_id + '-cbox-' + index + '" class="' + settings.checkBoxClass + '" />');
					$cbox.click(function(e, options) {
						if($cbox.attr('checked')) {
							$(el).show();
							$this.find('tbody td:nth-child(' + (index + 1) + ')').show();
                            displayedAttributes[index] = $(el).text();
						} else {
							$(el).hide();
							$this.find('tbody td:nth-child(' + (index + 1) + ')').hide();
                            displayedAttributes[index] = '';
						}
						if(!(options && options.disableCallback)) {
							settings.toggleCallback(index, ($(el).css('display') == 'none'), $this);
						}
					});
										
					
					var $div = $('<div id="' + container_id + '" class="' + settings.checkBoxDivClass + '"></div>');
					$div.append($cbox);
					$div.append(' <label for="' + container_id + '-cbox-' + index + '" class="' + settings.checkBoxClass + '" class="' + settings.checkBoxLabelClass + '">' + $(el).text() + '</label>');
					$(settings.checkBoxContainer).append($div);
					
					if($.inArray(index, settings.hideCols) != -1) {
						$cbox.trigger('click', {disableCallback: true});
						$this.find('thead th:nth-child(' + (index + 1) + ')').hide();
						$this.find('tbody td:nth-child(' + (index + 1) + ')').hide();
					}
					
					if($.inArray(index, settings.disableCols) != -1) {
						$cbox.attr('disabled', 'disabled');				
						$cbox.parent().addClass(settings.checkBoxDivDisabledClass);
					}

				});	

                // The "Check All" button.
				var $checkall = $('<input type="checkbox" checked="checked" id="' + container_id + '-cbox-checkall" />');
				$checkall.click(function(e) {
					if($checkall.attr('checked')) {
						$checked = $(settings.checkBoxContainer).find('input:not(:checked):not(#' + container_id + '-cbox-checkall)');
						$checked.trigger('click', {disableCallback: true});
						$checked.each(function(index, el) {
							var id = $(el).attr('id');
							var col = id.split(/-/)[3];
							$this.find('thead th:nth-child(' + (parseInt(col, 10) + 1) + ')').show();
							$this.find('tbody td:nth-child(' + (parseInt(col, 10) + 1) + ')').show();
                            displayedAttributes[index] = $this.find('thead th:nth-child(' + (parseInt(col, 10) + 1) + ')').text();
						});
					} else {
						$checked = $(settings.checkBoxContainer).find('input:checked:not(#' + container_id + '-cbox-checkall)');
						$checked.trigger('click', {disableCallback: true});
						$checked.each(function(index, el) {
							if($(el).attr('disabled')) {
							  return true;
							}
							var id = $(el).attr('id');
							var col = id.split(/-/)[3];
							$this.find('thead th:nth-child(' + (parseInt(col, 10) + 1) + ')').hide();
							$this.find('tbody td:nth-child(' + (parseInt(col, 10) + 1) + ')').hide();
                            displayedAttributes[index] = '';
						});
					}
				});
				$(settings.checkBoxContainer).append($('<div />').append($checkall, '<label for="' + container_id + '-cbox-checkall"> Check All</label>'));
				// End the "Check All" button.

				$this.data('columnDisplay', {
					target: $this,
					container_id: container_id,
					configuration: settings,
					state: {
				    hideCols: settings.hideCols.slice(0),
				    disableCols: settings.disableCols.slice(0)
					}
				});
				
			});
			
		},
		'hideCol': function(index) {
		  var data = $(this).data('columnDisplay');
			var settings = data.settings;
			var container_id = data.container_id;
			$('#' + container_id + '-cbox-' + index).attr('checked', false);
      $(this).find('thead th:nth-child(' + (index + 1) + ')').hide();
			$(this).find('tbody td:nth-child(' + (index + 1) + ')').hide();
			
		},
		'showCol': function(index) {
		  var data = $(this).data('columnDisplay');
			var settings = data.settings;
			var container_id = data.container_id;
			$('#' + container_id + '-cbox-' + index).attr('checked', true);
      $(this).find('thead th:nth-child(' + (index + 1) + ')').show();
			$(this).find('tbody td:nth-child(' + (index + 1) + ')').show();
		}
	} // End methods
	
	$.fn.columnDisplay = function(method) {
		
    if(methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call( arguments, 1 ));
    } else if(typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' +  method + ' does not exist on jQuery.columnDisplay');
    }
		
	};
	
})(jQuery);