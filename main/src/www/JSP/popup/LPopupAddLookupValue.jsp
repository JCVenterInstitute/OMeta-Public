<%--
  ~ Copyright J. Craig Venter Institute, 2013
  ~
  ~ The creation of this program was supported by J. Craig Venter Institute
  ~ and National Institute for Allergy and Infectious Diseases (NIAID),
  ~ Contract number HHSN272200900007C.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}"/>
</head>
<body>
<s:form id="LPopupAddLookupValue" name="LPopupAddLookupValue" namespace="/" method="post" theme="simple">
  <s:hidden name="w"/>
  <div class="popup">
    <div class="modal-dialog" style="max-width: 450px;">

      <!-- Modal content-->
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" onclick="$.closePopupLayer('LPopupAddLookupValue');">&times;</button>
          <h4 class="modal-title" id="popupHeader"><i class="fa fa-angle-right"></i> <strong>Add </strong></h4>
        </div>
        <div class="modal-body form-horizontal">
          <div class="alert alert-danger popup_alert_info" style="display: none;"><strong id="popupAlertDetail"></strong></div>
          <div class="form-group">
            <label class="col-sm-3 control-label"><strong>Type</strong></label>
            <div class="col-sm-9">
              <s:select list="types" name="lvType" id="lvType" class="form-control"/>
            </div>
          </div>
          <div class="form-group">
            <label class="col-sm-3 control-label"><strong>Name</strong></label>
            <div class="col-sm-9">
              <s:textfield id="lvName" name="lvName" size="30" class="form-control"/>
            </div>
            <p class="col-sm-offset-3 col-sm-9 help-block">Use comma(,) as delimiter to load multiple values</p>
          </div>
          <div class="form-group" id="dataTypeSelectTr">
            <label class="col-sm-3 control-label"><strong>Date Type</strong></label>
            <div class="col-sm-9">
              <s:select list="dataTypes" name="lvDataType" id="lvDataType" class="form-control"/>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <input type="button" class="btn btn-info" value="Add" onclick="_popup.add();"/>
          <button type="button" class="btn btn-default" onclick="$.closePopupLayer('LPopupAddLookupValue');">Close</button>
        </div>
      </div>
    </div>
  </div>
</s:form>
<script>
  function initPopup(which) {
    var type = which === 'et' ? 'Event Type' : which === 'gr' ? 'Actor Group' : 'Attribute';
    $('#popupHeader strong').append(type);
    $('#lvDataType').val('string');
    if (which === 'et' || which === 'a') { //preselect lookup value type
      $('#lvType').val(type).attr('disabled', 'disabled');
    }
    if (which === 'et' || which === 'gr') { //hide lookup value datatype for Event Type and Actor Group
      $('#dataTypeSelectTr').hide();
    }
    //utils.error.remove();
  }

  var _popup = {
    add: function () {
      var attributeName = $('input[name="lvName"]').val();
      if (!attributeName) {
        $('#popupAlertDetail').text("Name field is empty!");
        $('.popup_alert_info').show();
        return;
      } else if (attributeName.indexOf('Project Name') > -1) {
        $('#popupAlertDetail').text("Attribute name cannot be 'Project Name'!");
        $('.popup_alert_info').show();
        return;
      } else if (attributeName.indexOf('Sample Name') > -1) {
        $('#popupAlertDetail').text("Attribute name cannot be 'Sample Name'!");
        $('.popup_alert_info').show();
        return;
      } else {
        $.ajax({
          url: 'metadataSetupAjax.action',
          cache: false,
          async: false,
          data: 'type=a_lv&lvName=' + $('#lvName').val() + '&lvType=' + $('#lvType').val() + '&lvDataType=' + $('#lvDataType').val(),
          success: function (res) {
            if (res.dataMap) {
              var dataMap = res.dataMap;
              if (dataMap.isError && dataMap.isError === true) {
                $('#popupAlertDetail').text(dataMap.errorMsg);
                $('.popup_alert_info').show();
              } else {
                var newOption = vs.vvoption.replace(/\$v\$/g, res.lvName);
                if (res.lvType === 'Attribute') {
                  if (typeof emaOptions != 'undefined' && emaOptions.length > 0) emaOptions += newOption;
                  else if (typeof pmaOptions != 'undefined' && emaOptions.length > 0) pmaOptions += newOption;
                } else if (res.lvType === 'Event Type' && typeof etOptions === 'string' && etOptions.length > 0) etOptions += newOption;
                $.closePopupLayer('LPopupAddLookupValue');
              }
            }
          }
        });
      }
    },
    closeError: function () {
      $('.popup_alert_info').hide();
    }
  };
  (function () {
    initPopup('${type}');
  })();
</script>
</body>
</html>