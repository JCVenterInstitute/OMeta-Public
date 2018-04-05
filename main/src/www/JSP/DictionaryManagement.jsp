<%--
  Created by IntelliJ IDEA.
  User: mkuscuog
  Date: 2/20/2015
  Time: 3:53 PM
  To change this template use File | Settings | File Templates.
--%>
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <jsp:include page="header.jsp"/>
  <link rel="stylesheet" href="datatables/datatables.css" type='text/css' media='all'/>
  <style>
    #dictionary-information-table tbody label {
      font-weight: normal;
    }
  </style>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp"/>

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <!-- Modal -->
        <div class="modal fade" id="add-dictionary" role="dialog">
          <div class="modal-dialog" style="max-width:450px;">

            <!-- Modal content-->
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><i class="fa fa-angle-right"></i> <strong>Add Dictionary</strong></h4>
              </div>
              <div class="modal-body">
                <div class="alert alert-danger" style="display: none;"></div>
                <div class="alert alert-success" style="display: none;"></div>
                <div class="form-group" style="display: flex;align-items: center;">
                  <label class="col-sm-4 control-label"><strong>Type</strong></label>
                  <div class="col-sm-8 input-group">
                    <input type="text" name="dictType" size="30" id="dictType" class="form-control">
                  </div>
                </div>
                <div class="form-group" style="display: flex;align-items: center;">
                  <label class="col-sm-4 control-label"><strong>Value</strong></label>
                  <div class="col-sm-8 input-group">
                    <input type="text" name="dictValue" size="30" id="dictValue" class="form-control">
                  </div>
                </div>
                <div class="form-group" style="display: flex;align-items: center;">
                  <label class="col-sm-4 control-label"><strong>Code</strong></label>
                  <div class="col-sm-8 input-group">
                    <input type="text" name="dictCode" size="30" id="dictCode" class="form-control">
                  </div>
                </div>
                <div class="form-group" style="display: flex;align-items: center;">
                  <label class="col-sm-4 control-label"></label>
                  <div class="col-sm-8 input-group">
                    <input type="checkbox" id="hasDependency" name="hasDependency" size="30" style="margin-right: 5px;"/>Dependant
                  </div>
                </div>
                <div id="parentDictTypeCodeTR" class="form-group" style="display: none;align-items: center;">
                  <label class="col-sm-4 control-label"><strong>Parent Type</strong></label>
                  <div class="col-sm-8 input-group">
                    <s:select list="types" name="parentDictTypeCode" id="parentDictTypeCode" class="form-control"/>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-info" onclick="_popup.add();">Add</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>

          </div>
        </div>

        <div class="page-header">
          <h1>Dictionary Management</h1>
        </div>
        <div id="HeaderPane" style="margin:15px 0 0 30px;">
          <div id="errorMessagesPanel" style="margin-top:15px;margin-bottom: 15px;"></div>
          <s:if test="hasActionErrors()">
            <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
              <div class="alert_info" onclick="$('.alert_info').remove();">
                <strong style="color: #ffffff;background-color: #a90329;padding: 3px;border-color: #900323;border: 1px solid transparent;padding: 6px 12px;"><s:iterator
                        value='actionErrors'><s:property/></s:iterator></strong>
              </div>
            </div>
          </s:if>
          <s:if test="hasActionMessages()">
            <div class="alert_info" onclick="$('.alert_info').remove();" style="margin-bottom: 15px;">
              <div class="alert_info" onclick="$('.alert_info').remove();">
                <strong style="color: #31708f;background-color: #d9edf7;padding: 3px;border-color: #bce8f1;border: 1px solid transparent;padding: 6px 12px;"><s:iterator
                        value='actionMessages'><s:property/></s:iterator></strong>
              </div>
            </div>
          </s:if>
        </div>

        <s:form id="infoDictionaryPage" name="infoDictionaryPage" theme="simple">
          <input type="button" class="btn btn-success" onclick="newDictionaryPopup()" id="newDictionaryButton" value="New Dictionary"/>
          <table id="dictionary-information-table" class="table table-bordered table-striped table-condensed table-hover">
            <thead>
            <tr>
              <th>Dictionary Type</th>
              <th>Dictionary Code</th>
              <th>Dictionary Value</th>
              <th>Parent Dependency</th>
              <th>Active</th>
              <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <s:iterator value="dictionaryList" var="dictionary">
              <tr>
                <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="dictionaryType"><s:property value="#dictionary.dictionaryType"/></label></td>
                <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="dictionaryCode"><s:property value="#dictionary.dictionaryCode"/></label></td>
                <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="dictionaryValue"><s:property value="#dictionary.dictionaryValue"/></label></td>
                <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="parentDictionary"><s:property value="dependencyMap[#dictionary.dictionaryId]"/></label></td>
                <td><label id="<s:property value="#dictionary.dictionaryId"/>"><s:if test="%{#dictionary.isActive==1}">Yes</s:if><s:else>No</s:else></label></td>
                <td><label id="<s:property value="#dictionary.dictionaryId"/>">
                  <s:if test="%{#dictionary.isActive == 1}">
                    <input type="button" class="btn btn-xs btn-warning" id="<s:property value="#dictionary.dictionaryId"/>" value="Deactivate"
                           onclick="activateDictionary(false,this.id);"/>
                  </s:if>
                  <s:else>
                    <input type="button" class="btn btn-xs btn-primary" id="<s:property value="#dictionary.dictionaryId"/>" value="Activate"
                           onclick="activateDictionary(true,this.id);"/>
                  </s:else>
                </label></td>
              </tr>
            </s:iterator>
            </tbody>
          </table>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html"/>

<script src="datatables/datatables.js"></script>
<script src="datatables/Buttons-1.4.2/js/dataTables.buttons.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.colVis.js"></script>
<script src="datatables/JSZip-2.5.0/jszip.js"></script>
<script src="datatables/pdfmake-0.1.32/pdfmake.js"></script>
<script src="datatables/pdfmake-0.1.32/vfs_fonts.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.bootstrap.js"></script>

<script type="text/javascript">
  $(document).ready(function () {
    generateParentDependencyInfo();

    var table = $('#dictionary-information-table').DataTable({
      responsive: true,
      lengthChange: false,
      buttons: ['copy', 'excel', 'pdf', 'csv', 'colvis'],
      autoWidth: false,
      columnDefs: [
        {"width": "25%", "targets": [0]},
        {"width": "15%", "targets": [1]},
        {"width": "30%", "targets": [2]},
        {"width": "20%", "targets": [3]},
        {"width": "5%", "targets": [4]},
        {"width": "5%", "targets": [5]}
      ]
    });

    table.buttons().container()
        .appendTo('#dictionary-information-table_wrapper .col-sm-6:eq(0)');

    $("#newDictionaryButton").show().appendTo('#dictionary-information-table_filter');

    $('#hasDependency').change(function () {
      if ($(this).is(":checked")) {
        $('#parentDictTypeCodeTR').css('display', 'flex');
      } else {
        $("#parentDictTypeCodeTR").hide();
      }
    });
  });

  function generateParentDependencyInfo() {
    $('.parentDictionary').each(function (i, obj) {
      var obj = $(obj);
      var parentId = obj.text();

      if (parentId != "") {
        obj.text($("label[id='" + parentId + "'][class='dictionaryCode']").text()
            + " - " +
            $("label[id='" + parentId + "'][class='dictionaryValue']").text());
      }
    });
  }

  function newDictionaryPopup() {
    $('#add-dictionary').modal('show');
  }

  function activateDictionary(activate, id) {
    var $updateDictionaryForm = $('<form>').attr({
      id: 'updateDictionaryForm',
      method: 'POST',
      action: 'updateDictionary.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'dictionaryId',
      name: 'dictionaryId',
      value: id
    }).appendTo($updateDictionaryForm);

    $('<input>').attr({
      id: 'active',
      name: 'active',
      value: activate
    }).appendTo($updateDictionaryForm);

    $('body').append($updateDictionaryForm);
    $updateDictionaryForm.submit();
  }

  var _popup = {
    add: function () {
      var dictionaryType = $('input[name="dictType"]').val();
      var dictionaryValue = $('input[name="dictValue"]').val();
      var dictionaryCode = $('input[name="dictCode"]').val();
      var dictionaryParentType;
      if (!dictionaryType) {
        $('.modal-body .alert-danger').text("Type field is empty!").show();
      } else if (!dictionaryValue) {
        $('.modal-body .alert-danger').text("Value field is empty!").show();
      } else {
        if ($("#hasDependency").is(':checked')) {
          dictionaryParentType = $('select[name="parentDictTypeCode"]').val();
          if (!dictionaryParentType) {
            $('.modal-body .alert-danger').text("Parent Type field is empty!").show();
          }
        }
        $.ajax({
          url: 'metadataSetupAjax.action',
          cache: false,
          async: false,
          data: 'type=dict&dictType=' + dictionaryType + '&dictValue=' + dictionaryValue + '&dictCode=' + dictionaryCode + '&parentDictTypeCode=' + dictionaryParentType,
          success: function (res) {
            if (res.dataMap) {
              var dataMap = res.dataMap;
              if (dataMap.isError && dataMap.isError === true) {
                $('.modal-body .alert-danger').text(dataMap.errorMsg).show();
              } else {
                $('.modal-body .alert-danger').hide();
                $('.modal-body .alert-success').text("Dictionary added succesfully! Page will be reloaded in 5 seconds.").show();

                setTimeout(location.reload.bind(location), 5000);
              }
            }
          }
        });
      }
    },
    closeError: function () {
      $('#errorMsg').remove();
    }
  };
</script>
</body>
</html>