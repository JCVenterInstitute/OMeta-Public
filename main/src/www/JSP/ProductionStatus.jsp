
<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/main.css" />
  <link rel="stylesheet" href="style/version01.css" />
  <link rel="stylesheet" href="style/rte.css" />
  <link rel="stylesheet" href="style/dataTables.css" />
  <link rel="stylesheet" href="style/tableTools.css" />
  <style>
    fieldset legend:hover { cursor: pointer; }
    fieldset { padding: 5px 10px 5px 10px; }
    .collapsed { border-width: 1px 0px 0px 0px; padding: 5px 12px 0px 12px; }
    .headerContainer {width: 100%;}
    .dataTables_processing{height: 50px !important;}
    .dataTables_length { padding-left: 25px; }
    .DTTT_container { float: left !important; }
    button {font-family: inherit;font-size: inherit;line-height: inherit;color: inherit;
      font: inherit;margin: 0;overflow: visible;-webkit-appearance: button;cursor: pointer;
    }input[type="button"].btn-block {width: 100%;}
    .btn {display: inline-block;margin-bottom: 0;font-weight: normal;text-align: center;
      vertical-align: middle;cursor: pointer;background-image: none;border: 1px solid transparent;
      white-space: nowrap;padding: 6px 12px;font-size: 13px;line-height: 1.42857143;
      border-radius: 2px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;user-select: none;
    }
    .btn:focus,.btn:active:focus,.btn.active:focus {outline: thin dotted;outline: 5px auto -webkit-focus-ring-color;outline-offset: -2px;}
    .btn:hover,.btn:focus {color: #333333;text-decoration: none;}
    .btn:active,.btn.active {outline: 0;background-image: none;-webkit-box-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);
      box-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);}
    .btn-default {color: #333333;background-color: #ffffff;border-color: #cccccc;}
    .btn-default:hover,.btn-default:focus,.btn-default:active,.btn-default.active,.open > .dropdown-toggle.btn-default {
      color: #333333; background-color: #e6e6e6;border-color: #adadad;}
    .btn-default:active,.btn-default.active,.open > .dropdown-toggle.btn-default {background-image: none;}
    .btn-default.disabled,.btn-default[disabled],fieldset[disabled] .btn-default,.btn-default.disabled:hover,
    .btn-default[disabled]:hover,fieldset[disabled] .btn-default:hover,.btn-default.disabled:focus,
    .btn-default[disabled]:focus,fieldset[disabled] .btn-default:focus,.btn-default.disabled:active,
    .btn-default[disabled]:active,fieldset[disabled] .btn-default:active,.btn-default.disabled.active,
    .btn-default[disabled].active,fieldset[disabled] .btn-default.active {background-color: #ffffff;border-color: #cccccc;}
    .btn-default .badge {color: #ffffff;background-color: #333333;}
    .btn-xs,.btn-group-xs > .btn {padding: 1px 5px;font-size: 12px;line-height: 1.5;border-radius: 2px;}
    .navbar-btn.btn-xs {margin-top: 14px;margin-bottom: 14px;}
    .btn-xs .badge {top: 0;padding: 1px 5px;}
    @font-face {font-family: 'Glyphicons Halflings';src: url('/ometa/fonts/glyphicons-halflings-regular.eot');
      src: url('/ometa/fonts/glyphicons-halflings-regular.eot?#iefix') format('embedded-opentype'), url('/ometa/fonts/glyphicons-halflings-regular.woff') format('woff'), url('/ometa/fonts/glyphicons-halflings-regular.ttf') format('truetype'), url('/ometa/fonts/glyphicons-halflings-regular.svg#glyphicons_halflingsregular') format('svg');
    }
    .glyphicon {position: relative;top: 1px;display: inline-block;font-family: 'Glyphicons Halflings';
      font-style: normal;font-weight: normal;line-height: 1;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;}
    .glyphicon-filter:before {content: "\e138";}
    .glyphicon-search:before {content: "\e003";}
    #columnFilterBtn:hover:after{ background: #333; background: rgba(0,0,0,.8);
      border-radius: 5px; bottom: 0px; color: #fff; content: attr(data-tooltip);
      left: 140%; padding: 5px 15px; position: absolute; z-index: 98; width: auto; display: inline-table; }
    #columnFilterBtn:hover:before{border: solid; border-color: transparent #333;border-width: 6px 6px 6px 0px;
      bottom: 8px; content: ""; left: 125%; position: absolute; z-index: 99;}
    #column_filter{padding-bottom: 8px;margin: 10px 0 18px;}
    #col_filter_border_l{border-left: 2px solid #333333;position: absolute;margin-left: 4.4px;left: 0;top: 48px;bottom: 0;}
    #col_filter_border_b{border-bottom: 2px solid #333333;position: absolute;right: 95.1%;margin-left: 5px;left: 0;bottom: 0;}
    .column_filter_box{margin: 5px 0 5px 15px;}
    #columnSearchBtn{margin:10px 0 0 15px;width: 59px;}
    .select_column, .select_operation, .filter_text, .removeColumnFilter{margin-left: 4px;}
    .removeColumnFilter, #addMoreColumnFilter{height: 17px;top: 4px;position: relative;}
    .select_logicgate{width: 59px;}
  </style>
</head>
<body>
<s:form id="statusPage" name="statusPage"
        namespace="/"
        action="productionStatus"
        method="post" theme="simple">
  <s:hidden name="projectNames" />
  <s:hidden name="attributesOnScreen" id="attributesOnScreen"/>
  <s:hidden name="attributes" />
  <div id="HeaderPane" style="margin:15px 0 0 30px;">
    <div class="panelHeader" style="margin:0;">Project Status</div>
    <div id="errorMessagesPanel" style="margin-top:15px;"></div>
    <s:if test="hasActionErrors()">
      <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
    </s:if>
  </div>
  <div id="middle_content_template">
    <p>An Excel version of this data is also available for download
      <s:submit type="input" value="here"
                onclick="document.statusPage.action='productionStatusExcel.action';javascript:getDisplayedAttributes();"/>.</p>
    <!--<div id="columnsTable"></div>  for column listing-->
    <fieldset>
      <legend> Show / Hide Columns </legend>
      <div id="statusTableColumnToggler"></div>
    </fieldset>
    <div id="statusTableDiv">
      <table id="statusTable" style="float:left;width:100%"></table>
    </div>
  </div>

</s:form>
<script src="scripts/jquery/jquery-1.7.2.js"></script>
<script src="scripts/jquery/jquery-ui.js"></script>
<script src="scripts/ometa.utils.js"></script>
<script type='text/javascript' src='scripts/bootstrap.js'></script>
<script src="scripts/jquery/jquery.tablesorter.js"></script>
<script src="scripts/jquery/jquery.columnDisplay.js"></script>
<script src="scripts/jquery/jquery.dataTables.js"></script>
<script src="scripts/jquery/jquery.tableTools.js"></script>
<script src="scripts/jquery/jquery.colReorderWithResize.js"></script>

<script type="text/javascript">
  var headerList = [], pDT, attributeTypeMap = {};
  var _page = {
    columnfilter: {
      toggle: function (btn) {
        var $btn = $(btn);
        var $columnFilter = $("#column_filter");
        if ($btn.attr("name").indexOf("show") > -1) {
          $columnFilter.show();
          $btn.attr("name", "hideColumnFilter");
        } else {
          $columnFilter.hide();
          $btn.attr("name", "showColumnFilter");
        }
      },
      isActive: function () {
        return $("#columnFilterBtn") && $("#columnFilterBtn").attr("name") && $("#columnFilterBtn").attr("name").indexOf("hide") > -1;
      }
    }
  }

  $(document).ready(function() {
    var attributeType = "${attributeTypeMap}";
    attributeType = attributeType.replace("{","").replace("}","").replace(/, /gi, ",");
    var attributeTypes = attributeType.split(",");
    for(var i = 0; i < attributeTypes.length; i++) {
      var x = attributeTypes[i].split("=");
      attributeTypeMap[x[0]] = x[1];
    }
    var attrs = '${attributes}', aoColumns=[];
    attrs=attrs.split(',');
    var header='<thead><tr>', hd="<th class='tableHeaderStyle'><p style='color:#FFFFFF;'>$hd$</th>";;
    headerList = [];
    $.each(attrs, function(i,v) {
      if(v!=='') {
        aoColumns.push({'mDataProp':v});
        header+=hd.replace('$hd$', v);
        headerList.push(v);
      }
    });
    header+='</tr></thead>';
    $('#statusTable').html(header);

    pDT = $("#statusTable").dataTable({
      "sDom": '<"statusTop"Tlf><"statusMain"rt><"statusBottom"ip>Rlfrtip',
      "sPaginationType": "full_numbers",
      "bProcessing": true,
      "bServerSide": true,
      "aoColumns": aoColumns,
      "sServerMethod": "POST",
      "sAjaxSource": "productionStatusAjax.action?projectNames=${projectNames}&attributes=${attributes}",
      "fnServerParams": function (aoData){
        if(_page.columnfilter.isActive()) {
          var index = 0;  // keep count to have an accurate list size in case of empty filter values
          $('.column_filter_box').each(function (i, elem) {
            var $filterText = $(this).children('input:text[class="filter_text"]');
            var filterTextVal = $filterText.val();

            if (index == 0 || (filterTextVal && filterTextVal != '')) {
              var nth = $filterText.attr('id').split("_")[2];
              aoData.push({"name": "columnName[" + index + "]", "value": $("#select_column_" + nth).val()});
              aoData.push({
                "name": "columnSearchArguments[" + index + "]",
                "value": filterTextVal + ";" + $("#select_operation_" + nth).val() + ";" + $("#select_logicgate_" + nth).val()
              });

              index++;
            }
          });
        }
      },
      "aaSorting": [],
      "oTableTools": {
        "sSwfPath": "media/copy_csv_xls_pdf.swf",
        "sRowSelect": "multi",
        "aButtons": [
          {"sExtends":"csv", "bSelectedOnly":true},
          {"sExtends":"xls", "bSelectedOnly":true},
          {"sExtends":"pdf", "bSelectedOnly":true},
          {"sExtends":"copy", "bSelectedOnly":true},
          "select_all", "select_none"]

      }
    });

    generateColumnFilter();

    $(".dataTables_filter").append(
            $('<button/>').attr({
              'type': 'button',
              'class': 'btn btn-default btn-xs',
              'id': 'triggerSearchBtn',
              'onclick': 'triggerSearch();',
              'style': 'height: 17px;margin-top: -4px;'
            }).append(
                    $('<span/>').attr({
                      'class': 'glyphicon glyphicon-search',
                      'aria-hidden': 'true',
                      'style': 'top: -1px;font-size: 0.9em;'
                    })
            )
    );
    $(".dataTables_filter:first").append(
            $('<button/>').attr({
              'type': 'button',
              'class': 'btn btn-default btn-xs',
              'id': 'columnFilterBtn',
              'data-tooltip': 'Column Filter',
              'name':'showColumnFilter',
              'onclick': '_page.columnfilter.toggle(this);',
              'style': 'margin-left:10px;height: 24px;'
            }).append(
                    $('<span/>').attr({
                      'class': 'glyphicon glyphicon-filter',
                      'aria-hidden': 'true'
                    })
            )
    );
    var $colTable = $('#statusTable').columnDisplay({
      checkBoxContainer: '#statusTableColumnToggler',
      checkBoxDivClass: 'jcvi-cd-cbox-container',
      checkBoxDivDisabledClass: 'jcvi-cd-cbox-container-disabled',
      checkBoxClass: 'jcvi-cd-cbox',
      checkBoxLabelClass: 'jcvi-cd-cbox-label'
    });

    $('fieldset').fieldCollapse({
      collapseClass: 'collapsed',
      contentSelector: 'div',
      startCollapsed: true
    });

    utils.error.check();
  });

  function generateColumnFilter(){
    var $statusTop = $(".statusTop");
    var $columnFilterDiv = $("<div>", {id: "column_filter", style:"display:none;"}).append(
            $('<span/>').attr({'class': 'glyphicon glyphicon-filter','aria-hidden': 'true'})).append(
            $("<div>", {id: "col_filter_border_l"})).append($("<div>", {id: "col_filter_border_b"}));
    $columnFilterDiv.insertAfter($statusTop);
    $statusTop.after("<br/>").after("<br/>");
    $columnFilterDiv.append($("<button>")
            .attr({'type':'button', 'class':'btn btn-default btn-xs', 'id':'columnSearchBtn', 'name':'columnSearchBtn', 'onclick':'triggerSearch()'})
            .html('Apply'));

    addNewFilter(-1);
  }

  function addNewFilter(i){
    var $addMoreBtn = $("<img>").attr({'src':'images/dataTables/details_open.png', 'id':'addMoreColumnFilter', 'onclick':'addNewFilter('+ ++i +');'});
    var $columnFilterBox = $("<div>", {'class': 'column_filter_box'});
    var $columnFilterSelect = $("<select>", {class:"select_column", id: "select_column_"+i, name:"column_name", 'onchange':'updateOperation(this.value,'+ i + ')'});
    var $columnFilterOperation = $("<select>", {class:"select_operation", id: "select_operation_"+i, name:"operation"});

    $.each(headerList, function(i2,v2) {
      $columnFilterSelect.append($("<option></option>").attr("value", v2).text(v2));
    });

    $columnFilterOperation.append($("<option></option>").attr("value", "equals").text("="));
    $columnFilterOperation.append($("<option></option>").attr("value", "like").text("LIKE"));
    $columnFilterOperation.append($("<option></option>").attr("value", "in").text("IN"));

    //Automatically add "AND" gate to first column filter
    if(i == 0){
      $columnFilterBox.append($("<input>").attr({'type':'hidden', class: "select_logicgate", id: "select_logicgate_0", name: "logicgate", value:"and"}))
              .append($("<label>").attr({'id':'first_filter_label','style':'width: 59px;text-align: center;display: inline-block;font-weight: bold;'}).text('AND'));
    } else {
      var $columnFilterLogicGate = $("<select>", {
        class: "select_logicgate",
        id: "select_logicgate_" + i,
        name: "logicgate"
      });
      $columnFilterLogicGate.append($("<option></option>").attr("value", "and").text("AND"));
      $columnFilterLogicGate.append($("<option></option>").attr("value", "or").text("OR"));
      $columnFilterLogicGate.append($("<option></option>").attr("value", "not").text("NOT"));

      $columnFilterBox.append($columnFilterLogicGate)
    }

    $columnFilterBox.append($columnFilterSelect);
    $columnFilterBox.append($columnFilterOperation);
    $columnFilterBox.append($("<input>").attr({'type':'text', 'class':'filter_text', 'id':'filter_text_'+i, 'name':'filter_text'}));
    if(i != 0) {
      $columnFilterBox.append($("<img>").attr({'src':'images/dataTables/details_close.png', 'class':'removeColumnFilter'})
              .click(function(){
                var $columnFilterBox = $(this).parent();

                if($columnFilterBox.get(0) === $(".column_filter_box:last").get(0)){
                  $(".column_filter_box").eq(-2).append($addMoreBtn);
                }
                $columnFilterBox.remove();
              }));
    }

    if($("#addMoreColumnFilter")) $("#addMoreColumnFilter").remove();
    $columnFilterBox.append($addMoreBtn);
    $columnFilterBox.insertBefore($('#columnSearchBtn'));

    var $currentSelectInput = $('#select_column_'+i);
    $currentSelectInput.combobox({
      selected: function (event, ui) {
        $currentSelectInput.trigger("onchange");
      }
    });
    var $autocompleteInput = $currentSelectInput.next();
    $autocompleteInput.removeClass();
    $autocompleteInput.css("width", "200px").css("margin-left", "4px");
    $autocompleteInput.next().css("top", "3px").css("height", "17px");
  }

  function updateOperation(val,i){
    var $lessOption = $("<option></option>").attr("value", "less").text("<");
    var $equalsOption = $("<option></option>").attr("value", "equals").text("=")
    var $greaterOption = $("<option></option>").attr("value", "greater").text(">");
    var $likeOption = $("<option></option>").attr("value", "like").text("LIKE");
    var $inOption = $("<option></option>").attr("value", "in").text("IN");
    var $select = $("#select_operation_" + i);
    $select.empty().append($equalsOption).append($likeOption).append($inOption);

    var type = attributeTypeMap[val];

    if(type !== 'string'){
      $select.append($lessOption).append($greaterOption);
    }
  }

  function comboBoxChanged(option, id) {
    if(id==='_projectSelect') {
      document.getElementById('eventTable').getElementsByTagName('img')[0].src = openBtn;
      if(option.value!=null && option.value!=0 && option.text!=null && option.text!='') {
        _page.change.project(option.value, 0);
        $('.ui-autocomplete-input').val('');
      } else {
        $("#_sampleSelect").html(vs.empty);
      }
    } else if(id==='_sampleSelect') {
      if(option.value!=null && option.text!=null && option.text!='') {
        _page.change.sample($('#_projectSelect').val(), option.value);
        //_page.button.showSample();
      }
    }
  }

  function triggerSearch(){
    var searchVal = $(".dataTables_filter > label > input").val();
    pDT.fnFilter(searchVal);
  }

  function getDisplayedAttributes() {
    $('#attributesOnScreen').val(displayedAttributes);
  }
</script>
</body>
</html>
