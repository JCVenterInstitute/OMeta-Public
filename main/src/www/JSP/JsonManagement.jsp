
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <jsp:include page="header.jsp" />
  <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <%--<link rel="stylesheet" href="style/version01.css" />--%>
  <style>
    table td:nth-child(2) { padding-left:7px; }
    tr.even { padding: 2px; background-color: #e9e9e9; }
    tr.odd { padding: 2px; background-color: #f5f5f5; }
    tr.odd td, tr.even td { padding: 6px 8px; margin: 0; vertical-align: top; }
    .chkbox { margin:5px 5px 5px 15px !important; }
    .selection { width:300px;height:100px }
    #attributesTableBody > tr > td:first-child {
      border-right: 1px solid white;
    }
    .hidden_event { display: none; }
    input[type=text], select {width: 300px;}
  </style>
  <style>
  </style>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp" />

  <div id="popupLayerScreenLocker" style="position: fixed; left: 0; top: 0; opacity: 0.5; height: 100%; width: 100%; z-index: 1000; display: none; background: rgb(0, 0, 0);"><!-- --></div>
  <div id="processingDiv" class="show_processing" style="display: none;position: fixed;">Processing your request. Please wait...</div>

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <div id="ribbon">
          <ol class="breadcrumb">
            <li>
              <a href="/ometa/secureIndex.action">Dashboard</a>
            </li>
            <li>Admin</li>
            <li>Json Management</li>
          </ol>
        </div>

        <s:form id="jsonManagement" name="jsonManagement" namespace="/" theme="simple">
          <div class="page-header">
            <h1>Json Management </h1>
          </div>
          <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div id="errorMessagesPanel" style="margin-top:15px;"></div>
            <s:if test="hasActionErrors()">
              <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
                <div class="alert_info" onclick="$('.alert_info').remove();">
                  <strong style="color: #ffffff;background-color: #a90329;padding: 3px;border-color: #900323;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionErrors'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
            <s:if test="hasActionMessages()">
              <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
                <div class="alert_info" onclick="$('.alert_info').remove();">
                  <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator value='actionMessages'><s:property/></s:iterator></strong>
                </div>
              </div>
            </s:if>
          </div>

          <div id="mainContent">
            <div id="statusTableDiv">
              <div id="tableTop">
                <table id="jsonManagementTable">
                  <tr class="even">
                    <td align="left">Select CSV file name to edit:</td>
                    <td>
                      <s:select id="fileNameList"
                                list="fileNameList" name="fileNameList" onchange="fileNameChange(this);" value="fileName" style="margin-left: 8px;"
                      />
                    </td>
                  </tr>
                  <tr class="odd">
                    <td align="left">Projects</td>
                    <td>
                      <table>
                        <tbody>
                        <tr>
                          <td>
                            <input class="filter" id="filterProjectNames" type="text"><img src="images/search.png">
                          </td>
                          <td>
                          </td>
                          <td>
                            <input class="filter" id="defilterProjectNames" type="text"><img src="images/search.png">
                          </td>
                        </tr>
                        <tr>
                          <td style="padding: 0px 8px 6px 8px">
                            <s:select id="projectNames"
                                      list="projectList" name="projectNames"
                                      listValue="projectName" listKey="projectName"
                                      multiple="true" class="selection select" size="5"/>
                          </td>
                          <td style="padding-top: 25px;padding-right: 25px;">
                            <input type="button" class="selectionBtn" value=">" id="projectSelOne">
                            <input type="button" class="selectionBtn" value=">>" id="projectSelAll">
                            <br><br>
                            <input type="button" class="selectionBtn" value="<" id="projectDeOne">
                            <input type="button" class="selectionBtn" value="<<" id="projectDeAll">
                          </td>
                          <td style="padding: 0px 8px 6px 8px">
                            <select class="selection selected" name="selectedProjectNames" id="selectedProjectNames" size="5" multiple="multiple"/>
                          </td>
                          <td style="padding-top: 25px;padding-right: 25px;">
                            <input type="button" class="reorderBtn" value="Up" id="projectUp">
                            <input type="button" class="reorderBtn" value="Down" id="projectDown">
                          </td>
                        </tr>
                        </tbody>
                      </table>
                    </td>
                  </tr>
                  <tr class="even">
                    <td align="left">Attributes</td>
                    <td>
                      <table>
                        <tbody>
                        <tr>
                          <td>
                            <input class="filter" id="filterAttributes" type="text"><img src="images/search.png">
                          </td>
                          <td>
                          </td>
                          <td>
                            <input class="filter" id="defilterAttributes" type="text"><img src="images/search.png">
                          </td>
                        </tr>
                        <tr>
                          <td style="padding: 0px 8px 6px 8px">
                            <s:select id="attributes"
                                      list="attributeList" name="attributes"
                                      listValue="name" listKey="name"
                                      multiple="true" class="selection select" size="5"/>
                          </td>
                          <td style="padding-top: 25px;padding-right: 25px;">
                            <input type="button" class="selectionBtn" value=">" id="attributeSelOne">
                            <input type="button" class="selectionBtn" value=">>" id="attributeSelAll">
                            <br><br>
                            <input type="button" class="selectionBtn" value="<" id="attributeDeOne">
                            <input type="button" class="selectionBtn" value="<<" id="attributeDeAll">
                          </td>
                          <td style="padding: 0px 8px 6px 8px">
                            <select class="selection selected" name="selectedAttributes" id="selectedAttributes" size="5" multiple="multiple"/>
                          </td>
                          <td style="padding-top: 25px;padding-right: 25px;">
                            <input type="button" class="reorderBtn" value="Up" id="attributeUp">
                            <input type="button" class="reorderBtn" value="Down" id="attributeDown">
                          </td>
                        </tr>
                        </tbody>
                      </table>
                    </td>
                  </tr>
                  <tr class="odd">
                    <td align="left">Screen Attributes</td>
                    <td>
                      <table>
                        <tbody>
                        <tr>
                          <td>
                            <input class="filter" id="filterScreenAttributes" type="text"><img src="images/search.png">
                          </td>
                          <td>
                          </td>
                          <td>
                            <input class="filter" id="defilterScreenAttributes" type="text"><img src="images/search.png">
                          </td>
                        </tr>
                        <tr>
                          <td style="padding: 0px 8px 6px 8px">
                            <s:select id="screenAttributes"
                                      list="attributeList" name="screenAttributes"
                                      listValue="name" listKey="name"
                                      multiple="true"  class="selection select" size="5"/>
                          </td>
                          <td style="padding-top: 25px;padding-right: 25px;">
                            <input type="button" class="selectionBtn" value=">" id="screenAttributeSelOne">
                            <input type="button" class="selectionBtn" value=">>" id="screenAttributeSelAll">
                            <br><br>
                            <input type="button" class="selectionBtn" value="<" id="screenAttributeDeOne">
                            <input type="button" class="selectionBtn" value="<<" id="screenAttributeDeAll">
                          </td>
                          <td style="padding: 0px 8px 6px 8px">
                            <select class="selection selected" name="selectedScreenAttributes" id="selectedScreenAttributes" size="5" multiple="multiple"/>
                          </td>
                          <td style="padding-top: 25px;padding-right: 25px;">
                            <input type="button" class="reorderBtn" value="Up" id="screenAttributeUp">
                            <input type="button" class="reorderBtn" value="Down" id="screenAttributeDown">
                          </td>
                        </tr>
                        </tbody>
                      </table>
                    </td>
                  </tr>
                  <tr class="even">
                    <td align="left">Sorting</td>
                    <td>
                      <s:textfield name="sorting" style="margin-left: 8px;"/>
                    </td>
                  </tr>
                  <tr class="odd">
                    <td align="left">File Path</td>
                    <td>
                      <s:textfield name="filePath" style="margin-left: 8px;"/>
                    </td>
                  </tr>
                  <tr class="even">
                    <td align="left">Domain</td>
                    <td>
                      <s:textfield name="domain" style="margin-left: 8px;"/>
                    </td>
                  </tr>
                  <tr class="odd">
                    <td>
                      <div id="submitDiv" cssStyle="margin:15px 10px 5px 200px;width:100%;">
                        <input type="button" class="btn btn-primary" id="loadButton" value="Update" onclick="javascript:updateJson();" />
                      </div>
                    </td>
                    <td>
                      <a href="generateJson.action" class="btn btn-info">Generate JSON</a>
                    </td>
                  </tr>
                </table>
              </div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />
<script src="scripts/jquery/jquery.dataTables.js"></script>

<script type="text/javascript">
  $(document).ready(function() {
    $('select.select').on('dblclick', 'option', function () {
      var id = $(this).parent('select').attr('id');

      moveOptionUpdateFilter('#' + id, '#selected' + upperCaseFirstLetter(id), false);
    });

    $('select.selected').on('dblclick', 'option', function () {
      var id = $(this).parent('select').attr('id');

      if(id.indexOf('Project') > -1){
        moveOptionUpdateFilter('#' + id, '#projectNames', false);
      } else if(id.indexOf('Screen') > -1){
        moveOptionUpdateFilter('#' + id, '#screenAttributes', false);
      } else {
        moveOptionUpdateFilter('#' + id, '#attributes', false);
      }
    });

    $('input:button.selectionBtn').click(function(){
      var id = $(this).attr('id');
      var cObj = '', pObj = '';
      var type = (id.indexOf('project') >  -1) ? 'projectNames' : ((id.indexOf('screen') > -1) ? 'screenAttributes' : 'attributes');
      var all = (id.indexOf('One') >  -1) ? false : true;

      if(id.indexOf('Sel') >  -1){
        cObj = '#'+type;
        pObj = '#selected'+upperCaseFirstLetter(type);
      } else {
        cObj = '#selected'+upperCaseFirstLetter(type);
        pObj = '#'+type;
      }

      moveOptionUpdateFilter(cObj, pObj, all);
    });

    $('input:button.reorderBtn').click(function() {
      var $this = $(this);
      var id = $this.attr('id');
      var type = (id.indexOf('project') > -1) ? 'projectNames' : ((id.indexOf('screen') > -1) ? 'screenAttributes' : 'attributes');
      var $op = $('#selected' + upperCaseFirstLetter(type) + ' option:selected');

      if ($op.length) {
        ($this.val() == 'Up') ? $op.first().prev().before($op) : $op.last().next().after($op);

        if(type == 'projectNames')
          $('#selectedProjectNames').filterByText($('#defilterProjectNames'));
        else if(type == 'screenAttributes')
          $('#selectedScreenAttributes').filterByText($('#defilterScreenAttributes'));
        else
          $('#selectedAttributes').filterByText($('#defilterAttributes'));
      }
    });

    $('#projectNames').filterByText($('#filterProjectNames'));
    $('#attributes').filterByText($('#filterAttributes'));
    $('#screenAttributes').filterByText($('#filterScreenAttributes'));

    $('#selectedProjectNames').filterByText($('#defilterProjectNames'));
    $('#selectedAttributes').filterByText($('#defilterAttributes'));
    $('#selectedScreenAttributes').filterByText($('#defilterScreenAttributes'));

    fillSelectedValues();
  });

  function fillSelectedValues() {
    <s:iterator value="projectNames.split(',')">
    moveOptionByValue("#projectNames", "#selectedProjectNames", "<s:property />");
    </s:iterator>
    <s:iterator value="attributes.split(',')">
    moveOptionByValue("#attributes", "#selectedAttributes", "<s:property/>");
    </s:iterator>
    <s:iterator value="screenAttributes.split(',')">
    moveOptionByValue("#screenAttributes", "#selectedScreenAttributes", "<s:property/>");
    </s:iterator>
  }

  function moveOptionUpdateFilter(cObj, pObj, all){
    var selected = $(cObj);
    var target = $(pObj);
    var action = (all === true) ? 'option' : 'option:selected';

    var options = selected.data('options');
    var selectedVal = [];
    $(cObj + ' > ' + action).each(function () {
      selectedVal.push($(this).val());
    });

    var tempOption = [];
    $.each(options, function (i) {
      var option = options[i];
      var check = false;
      for(var j = 0; j < selectedVal.length; j++){
        if (option.value == selectedVal[j]) {
          check = true;
        }
      }
      if(!check) tempOption.push(option);
    });

    var targetOptions = target.data('options');
    $(cObj + ' > ' + action).each(function () {
      targetOptions.push({value: $(this).val(), text: $(this).text()});
    });

    target.data('options', targetOptions);

    selected.find(action).remove().appendTo(pObj);
    selected.data('options', tempOption);
  }

  function moveOptionByValue(org, dest, val){
    var selected = $(org);
    var target = $(dest);
    var options = selected.data('options');
    var selectedVal = val;
    var tempOption = [];

    $.each(options, function (i) {
      var option = options[i];
      if (option.value != selectedVal) {
        tempOption.push(option);
      }
    });

    var targetOptions = target.data('options');
    targetOptions.push({value: val, text: val});

    target.data('options', targetOptions);

    selected.find('option[value="'+val+'"]').remove().appendTo(dest);
    selected.data('options', tempOption);
  }

  function upperCaseFirstLetter(string){
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  function fileNameChange(fileName) {
    var val = fileName.value;
    $.ajax({
      beforeSend: processing(true),
      url: 'getJsonInfo.action',
      data: "fileName="+val,
      success: function(html) {
        //Clear filter
        $.each($("input[id*='defilter'], input[id*='filter']"), function( index, value ) {
          $(this).val('');
          $(this).keyup();
        });

        $("input[id*='DeAll']").click();
        var attributesArr = html.attributes.split(',');
        var projectNamesArr = html.projectNames.split(',');
        var screenAttributesArr = html.screenAttributes.split(',');

        for (var i = 0; i < projectNamesArr.length; i++) {
          moveOptionByValue("#projectNames", "#selectedProjectNames", projectNamesArr[i]);
        }
        for (var i = 0; i < attributesArr.length; i++) {
          moveOptionByValue("#attributes", "#selectedAttributes", attributesArr[i]);
        }
        for (var i = 0; i < screenAttributesArr.length; i++) {
          moveOptionByValue("#screenAttributes", "#selectedScreenAttributes", screenAttributesArr[i]);
        }

        $("input[name='sorting']").val(html.sorting);
        $("input[name='filePath']").val(html.filePath);
        $("input[name='domain']").val(html.domain);

        processing(false);
      }
    });
  }

  function updateJson() {
    processing(true);
    //Clear filter
    $.each($("input[id*='defilter']"), function( index, value ) {
      $(this).val('');
      $(this).keyup();
    });

    var projectNames = "", attributes = "", screenAttributes = "";

    $.each($('select[name=selectedProjectNames] option'), function() {
      projectNames += $(this).val() + ',';
    });
    $.each($('select[name=selectedAttributes] option'), function() {
      attributes += $(this).val() + ',';
    });
    $.each($('select[name=selectedScreenAttributes] option'), function() {
      screenAttributes += $(this).val() + ',';
    });

    var $jsonForm = $('<form>').attr({
      id: 'jsonForm',
      method: 'POST',
      action: 'updateJsonProducer.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'fileName',
      name: 'fileName',
      value : $('#fileNameList option:selected').text()
    }).appendTo($jsonForm);

    $('<input>').attr({
      id: 'projectNames',
      name: 'projectNames',
      value : projectNames
    }).appendTo($jsonForm);

    $('<input>').attr({
      id: 'attributes',
      name: 'attributes',
      value : attributes
    }).appendTo($jsonForm);

    $('<input>').attr({
      id: 'screenAttributes',
      name: 'screenAttributes',
      value : screenAttributes
    }).appendTo($jsonForm);

    $('<input>').attr({
      id: 'sorting',
      name: 'sorting',
      value : $("input[name='sorting']").val()
    }).appendTo($jsonForm);

    $('<input>').attr({
      id: 'filePath',
      name: 'filePath',
      value : $("input[name='filePath']").val()
    }).appendTo($jsonForm);

    $('<input>').attr({
      id: 'domain',
      name: 'domain',
      value : $("input[name='domain']").val()
    }).appendTo($jsonForm);

    $('body').append($jsonForm);
    $jsonForm.submit();
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

  jQuery.fn.filterByText = function(textbox) {
    return this.each(function() {
      var select = this;
      var options = [];
      $(select).find('option').each(function() {
        options.push({value: $(this).val(), text: $(this).text()});
      });
      $(select).data('options', options);
      $(textbox).bind('change keyup', function() {
        var options = $(select).empty().scrollTop(0).data('options');
        var search = $.trim($(this).val());
        var regex = new RegExp(search,'gi');

        $.each(options, function(i) {
          var option = options[i];
          if(option.text.match(regex) !== null) {
            $(select).append(
                $('<option>').text(option.text).val(option.value)
            );
          }
        });
      });
    });
  };
</script>
</body>
</html>
