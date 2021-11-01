
<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>O-META | Ontologies Based Metadata Tracking Application</title>
  <link rel="icon" href="images/ometa_icon.png">
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Open+Sans%3A300italic%2C400italic%2C600italic%2C300%2C400%2C600&#038;subset=latin%2Clatin-ext' type='text/css' media='all' />
  <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Open+Sans%3A400italic%2C700italic%2C300%2C400%2C700' type='text/css' media='screen' />
  <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Lato&#038;subset=latin%2Clatin-ext' type='text/css' media='screen' />
  <link rel='stylesheet' href="style/bootstrap.css" type='text/css' media='all' />
  <link rel='stylesheet' href="style/font-awesome.css" type='text/css' media='all' />
  <!--[if lt IE 9]>
  <link rel='stylesheet' href='css/ie.css' type='text/css' media='all' />
  <![endif]-->
  <link rel='stylesheet' href='https://fonts.googleapis.com/css?family=Lato%3A400%2C700%2C400italic%2C700italic' type='text/css' media='all' />

  <link rel="stylesheet" href="datatables/datatables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="datatables/Buttons-1.4.2/css/buttons.bootstrap.css" type='text/css' media='all' />
  <style>
    #statusTable_info, #statusTable_filter {float:left;}
    .dt-buttons {float:right;}
    #statusTable_processing {width: 400px;border: 1px solid #aed0ea;background: #d7ebf9;font-weight: bold;color: #2779aa;}
    #column_filter{margin: 5px 0 18px;float: left;}
    .column_filter_box{margin: 5px 0 5px 15px;}
    #columnSearchBtn{margin:10px 0 0 15px;}
    .select_column, .select_operation, .filter_text, .removeColumnFilter, #addMoreColumnFilter{margin-left: 4px;}
    #statusTable_filter label, #statusTable_filter button.btn {float:left;}
    #col_filter_border_l{border-left: 2px solid #333333;position: absolute;margin-left: 18px;left: 0;top: 55px;bottom: 0;}
    #col_filter_border_b{border-bottom: 2px solid #333333;position: absolute;right: 90%;margin-left: 18px;left: 0;bottom: 0;}
  </style>
</head>
<body>
<div id="content" class="container-fluid" role="main">
  <s:form id="statusPage" name="statusPage" namespace="/" action="productionStatusExcel" method="post" theme="simple">
  <s:hidden name="projectNames" />
  <s:hidden name="attributesOnScreen" id="attributesOnScreen"/>
  <s:hidden name="attributes" />
  <div class="page-header">
    <h1>Project Status</h1>
  </div>
  <div id="HeaderPane">
    <div id="errorMessagesPanel" style="margin-top:15px;"></div>
    <s:if test="hasActionErrors()">
      <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
    </s:if>
    <s:if test="hasActionMessages()">
      <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
        <div class="alert_info" onclick="$('.alert_info').remove();">
          <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
        </div>
      </div>
    </s:if>
  </div>
  <div id="middle_content_template">
    <div id="statusTableDiv">
      <table id="statusTable" class="table table-bordered table-striped table-condensed table-hover" style="width:100%"></table>
      <div id="buttons"></div>
    </div>
  </div>

</s:form>
</div>
<script src="scripts/jquery/jquery-1.7.2.js"></script>
<script src="scripts/jquery/jquery-ui.js"></script>
<script src="scripts/ometa.utils.js"></script>
<script type='text/javascript' src='scripts/bootstrap.js'></script>
<script src="datatables/datatables.js"></script>
<script src="datatables/pdfmake-0.1.32/pdfmake.js"></script>
<script src="datatables/pdfmake-0.1.32/vfs_fonts.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.colVis.js"></script>

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
    var header='<thead><tr>', hd="<th class='tableHeaderStyle'>$hd$";;
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
      "scrollX": true,
      "language": {
        "processing": "Processing your request. Please wait..."
      },
      "sPaginationType": "full_numbers",
      "bProcessing": true,
      "bServerSide": true,
      "aoColumns": aoColumns,
      "sServerMethod": "POST",
      "lengthMenu": [[10, 25, 50, 500, -1], [10, 25, 50, 500, "All"]],
      "sAjaxSource": "productionStatusAjax.action",
      "fnServerParams": function (aoData){
        aoData.push({"name" : "projectNames", "value" : "${projectNames}"});
        aoData.push({"name" : "attributes", "value" : "${attributes}"});
        if(_page.columnfilter.isActive()) {``
          var index = 0;  // keep count to have an accurate list size in case of empty filter values
          $('.column_filter_box').each(function (i, elem) {
            var $filterText = $(this).children('input:text[class="filter_text form-control input-sm"]');
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
        $('#statusTable_filter').parent("div.col-sm-6").insertBefore($('#statusTable_length').parent("div.col-sm-6"));
        $('#statusTable_length').parent("div.col-sm-6").css("text-align", "right")
        $('#statusTable_filter').css("float", "left")
      },
      "aaSorting": []
    });

    var buttons = new $.fn.dataTable.Buttons(pDT, {
      buttons: [  'copyHtml5', 'excelHtml5',
        {
          text: 'Excel - ALL',
          action: function ( e, dt, node, config ) {
            var form = $('#statusPage');
            var isExcel = form.find("input[name=isExcel]");
            if (isExcel.length == 0) {
              form.append('<input type="hidden" name="isExcel" value="true">');
            } else {
              isExcel.val("true");
            }
            form.submit();
          }
        },'csvHtml5',
        {
          text: 'CSV - ALL',
          action: function ( e, dt, node, config ) {
            var form = $('#statusPage');
            var isExcel = form.find("input[name=isExcel]");
            if (isExcel.length == 0) {
              form.append('<input type="hidden" name="isExcel" value="false">');
            } else {
              isExcel.val("false");
            }
            form.submit();
          }
        },
        'pdfHtml5', 'colvis' ]
    }).container().appendTo($('#buttons'));

    $(".dataTables_filter").append(
            $('<button/>').attr({
              'type': 'button',
              'class': 'btn btn-default btn-sm',
              'id': 'triggerSearchBtn',
              'onclick': 'triggerSearch();'
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
              'class': 'btn btn-default btn-sm',
              'id': 'columnFilterBtn',
              'data-tooltip': 'Column Filter',
              'name':'showColumnFilter',
              'onclick': '_page.columnfilter.toggle(this);',
              'style': 'margin-left:10px;'
            }).append(
                    $('<span/>').attr({
                      'class': 'glyphicon glyphicon-filter',
                      'aria-hidden': 'true'
                    })
            )
    );

    generateColumnFilter();

    utils.error.check();
  });

  function generateColumnFilter(){
    var $columnFilterDiv = $("<div>", {id: "column_filter", style:"display:none;"}).append(
        $('<span/>').attr({'class': 'glyphicon glyphicon-filter','aria-hidden': 'true'})).append(
        $("<div>", {id: "col_filter_border_l"})).append($("<div>", {id: "col_filter_border_b"}));
    $columnFilterDiv.insertAfter($(".dataTables_filter"));
    $columnFilterDiv.append($("<button>")
        .attr({'type':'button', 'class':'btn btn-default btn-sm', 'id':'columnSearchBtn', 'name':'columnSearchBtn', 'onclick':'triggerSearch()'})
        .html('Apply'));

    addNewFilter(-1);
  }

  function addNewFilter(i){
    var $addMoreBtn = $("<span>").attr({'class':'glyphicon glyphicon-plus-sign', 'style':'color:green;cursor: pointer;', 'id':'addMoreColumnFilter', 'onclick':'addNewFilter('+ ++i +');'});
    var $columnFilterBox = $("<div>", {'class': 'column_filter_box'});
    var $columnFilterSelect = $("<select>", {class:"select_column", id: "select_column_"+i, name:"column_name", 'onchange':'updateOperation(this.value,'+ i + ')'});
    var $columnFilterOperation = $("<select>", {class:"select_operation form-control input-sm", id: "select_operation_"+i, name:"operation"});

    $.each(headerList, function(i2,v2) {
      $columnFilterSelect.append($("<option></option>").attr("value", v2).text(v2));
    });

    $columnFilterOperation.append($("<option></option>").attr("value", "equals").text("="));
    $columnFilterOperation.append($("<option></option>").attr("value", "like").text("LIKE"));
    $columnFilterOperation.append($("<option></option>").attr("value", "in").text("IN"));

    //Automatically add "AND" gate to first column filter
    if(i == 0){
      $columnFilterBox.append($("<input>").attr({'type':'hidden', class: "select_logicgate", id: "select_logicgate_0", name: "logicgate", value:"and"}))
              .append($("<label>").attr({'id':'first_filter_label','style':'width: 69px;text-align: center;'}).text('AND'));
    } else {
      var $columnFilterLogicGate = $("<select>", {
        class: "select_logicgate form-control input-sm",
        id: "select_logicgate_" + i,
        name: "logicgate"
      });
      $columnFilterLogicGate.append($("<option></option>").attr("value", "and").text("AND"));
      $columnFilterLogicGate.append($("<option></option>").attr("value", "or").text("OR"));
      $columnFilterLogicGate.append($("<option></option>").attr("value", "not").text("NOT"));

      $columnFilterBox.append($columnFilterLogicGate)
    }

    $columnFilterBox.append($("<div>").attr({'class':'input-group', 'style':'margin-left:4px;'}).append($columnFilterSelect).append($columnFilterOperation));
    $columnFilterBox.append($("<input>").attr({'type':'text', 'class':'filter_text form-control input-sm', 'id':'filter_text_'+i, 'name':'filter_text', 'style':'width: 150px; '}));
    if(i != 0) {
      $columnFilterBox.append($("<span>").attr({'class':'removeColumnFilter glyphicon glyphicon-minus-sign', 'style':'color:red;cursor: pointer;'})
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
    $autocompleteInput.attr("class", "form-control");
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
