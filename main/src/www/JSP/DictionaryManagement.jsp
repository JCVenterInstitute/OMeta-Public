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
  <jsp:include page="header.jsp" />
  <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/chosen.css" />
  <style>
    td._details {
      text-align:left;
      padding:0 0 0 35px;
      border: 1px gray dotted;
    }
    td._details div {
      position: relative; overflow: auto; overflow-y: hidden;
    }
    td._details table td {
      border:1px solid white;
    }

    .datatable_top, .datatable_table, .datatable_bottom {
      float:left;
      clear:both;
      width:100%;
      min-width: 165px;
    }
    .datatable_bottom {margin-top: 10px}
    .dataTables_length {
      height: 29px;
      vertical-align: middle;
      min-width: 165px !important;
      margin-top: 2px;
    }
    .dataTables_filter {
      width: 260px !important;
    }
    .dataTables_info {
      padding-top: 0 !important;
    }
    .dataTables_paginate {
      float: left !important;
    }
    label{font-weight: normal !important;}

    #newDictionaryButton{float: right;margin-right: 300px;margin-bottom: 20px;}
  </style>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <div id="ribbon">
          <ol class="breadcrumb">
            <li>
              <a href="/ometa/secureIndex.action">Dashboard</a>
            </li>
            <li>Admin</li>
            <li>Dictionary Management</li>
          </ol>
        </div>

        <!-- Modal -->
        <div class="modal fade" id="add-dictionary" role="dialog">
          <div class="modal-dialog" style="max-width:450px;">

            <!-- Modal content-->
            <div class="modal-content" >
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
                <div id="parentDictTypeCodeTR" class="form-group" style="display: none;align-items: center;" >
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

        <s:form id="infoDictionaryPage" name="infoDictionaryPage" theme="simple">
          <div class="page-header">
            <h1>Dictionary Management</h1>
          </div>
          <div id="HeaderPane" style="margin:15px 0 0 30px;">
            <div id="errorMessagesPanel" style="margin-top:15px;margin-bottom: 15px;"></div>
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
                <div class="row">
                  <input type="button" class="btn btn-success" onclick="newDictionaryPopup()" id="newDictionaryButton" value="New Dictionary"/>
                  <table id="dictionary-information-table" style="width: 80%">
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
                        <td><label id="<s:property value="#dictionary.dictionaryId"/>" ><s:if test="%{#dictionary.isActive==1}">Yes</s:if><s:else>No</s:else></label></td>
                        <td><label id="<s:property value="#dictionary.dictionaryId"/>" >
                          <s:if test="%{#dictionary.isActive == 1}">
                            <input type="button" class="btn btn-xs btn-warning" id="<s:property value="#dictionary.dictionaryId"/>" value="Deactivate" onclick="activateDictionary(false,this.id);"/>
                          </s:if>
                          <s:else>
                            <input type="button" class="btn btn-xs btn-primary" id="<s:property value="#dictionary.dictionaryId"/>" value="Activate" onclick="activateDictionary(true,this.id);"/>
                          </s:else>
                        </label></td>
                      </tr>
                    </s:iterator>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </s:form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html" />

<script src="scripts/jquery/chosen.jquery.min.js"></script>
<script src="scripts/jquery/jquery.dataTables.js"></script>
<script src="scripts/jquery/jquery.colReorderWithResize.js"></script>

<script type="text/javascript">
  $(document).ready(function() {
    generateParentDependencyInfo();

    $('#dictionary-information-table').dataTable({
      "sDom": '<"datatable_top"lf><"datatable_table"rt><"datatable_bottom"ip>',
      "sPaginationType": "full_numbers",
      "bAutoWidth" : true,
      "aoColumnDefs": [
        {"sWidth": "25%", "aTargets": [ 0 ]},
        {"sWidth": "15%", "aTargets":[1]},
        {"sWidth": "30%", "aTargets":[2]},
        {"sWidth": "20%", "aTargets":[3]},
        {"sWidth": "5%", "aTargets":[4]},
        {"sWidth": "5%", "aTargets":[5]}
      ]
    });

    $('#hasDependency').change(function() {
      if($(this).is(":checked")) {
        $('#parentDictTypeCodeTR').css('display', 'flex');
      } else{
        $("#parentDictTypeCodeTR").hide();
      }
    });
  });

  function generateParentDependencyInfo(){
    $('.parentDictionary').each(function(i, obj) {
      var obj = $(obj);
      var parentId = obj.text();

      if(parentId != "") {
        obj.text($("label[id='"+ parentId + "'][class='dictionaryCode']").text()
        + " - " +
        $("label[id='"+ parentId + "'][class='dictionaryValue']").text());
      }
    });
  }

  function newDictionaryPopup() {
    $('#add-dictionary').modal('show');
  }

  function activateDictionary(activate, id){
    var $updateDictionaryForm = $('<form>').attr({
      id: 'updateDictionaryForm',
      method: 'POST',
      action: 'updateDictionary.action'
    }).css('display', 'none');

    $('<input>').attr({
      id: 'dictionaryId',
      name: 'dictionaryId',
      value : id
    }).appendTo($updateDictionaryForm);

    $('<input>').attr({
      id: 'active',
      name: 'active',
      value : activate
    }).appendTo($updateDictionaryForm);

    $('body').append($updateDictionaryForm);
    $updateDictionaryForm.submit();
  }

  var _popup = {
    add: function() {
      var dictionaryType = $('input[name="dictType"]').val();
      var dictionaryValue = $('input[name="dictValue"]').val();
      var dictionaryCode = $('input[name="dictCode"]').val();
      var dictionaryParentType;
      if(!dictionaryType) {
        $('.modal-body .alert-danger').text("Type field is empty!").show();
      } else if(!dictionaryValue){
        $('.modal-body .alert-danger').text("Value field is empty!").show();
      } else {
        if($("#hasDependency").is(':checked')){
          dictionaryParentType = $('select[name="parentDictTypeCode"]').val();
          if(!dictionaryParentType){
            $('.modal-body .alert-danger').text("Parent Type field is empty!").show();
          }
        }
        $.ajax({
          url: 'metadataSetupAjax.action',
          cache: false,
          async: false,
          data: 'type=dict&dictType='+dictionaryType+'&dictValue='+dictionaryValue+'&dictCode='+dictionaryCode+'&parentDictTypeCode='+dictionaryParentType,
          success: function(res){
            if(res.dataMap) {
              var dataMap = res.dataMap;
              if(dataMap.isError && dataMap.isError === true) {
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
    closeError: function() {$('#errorMsg').remove();}
  };
</script>
</body>
</html>