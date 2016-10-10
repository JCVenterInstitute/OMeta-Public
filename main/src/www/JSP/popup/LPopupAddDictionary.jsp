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
  <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}" />
  <style>
    .popup{background-color: white;}
    .popup-header{padding: 10px 10px 0 10px; border-bottom: thin solid rgb(184, 92, 92);}
  </style>
</head>
<body>
<s:form id="LPopupAddDictionary" name="LPopupAddDictionary" namespace="/" method="post" theme="simple">
  <s:hidden name="w"/>
  <div class="popup">
    <div class="popup-header">
      <h2 id="popupHeader" style="float:left">Add Dictionary</h2>
      <a href="#" onclick="$.closePopupLayer('LPopupAddDictionary');" title="Close" class="close-link" style="float: right;"><img src="images/xBtn.gif" title="Close" /></a>
      <br clear="both" />
    </div>
    <div style="padding:10px;">
      <fieldset style="padding:5px;">
        <legend id="popupLegend" style="margin-left:10px;font-size:14px;">Dictionary Information</legend>
        <div style="margin:5px 5px 5px 5px;">
          <table id="addDictionaryTable">
            <tr>
              <td style="width: 90px;"><strong>Type</strong></td><td><s:textfield id="dictType" name="dictType" size="30"/></td>
            </tr>
            <tr class="gappedTr">
              <td><strong>Value</strong></td><td><s:textfield id="dictValue" name="dictValue" size="30"/></td>
            </tr>
            <tr class="gappedTr">
              <td><strong>Code</strong></td><td><s:textfield id="dictCode" name="dictCode" size="30"/></td>
            </tr>
            <tr class="gappedTr">
              <td></td><td><input type="checkbox" id="hasDependency" name="hasDependency" size="30" style="margin-right: 5px;"/>Dependant</td>
            </tr>
            <tr id="parentDictTypeCodeTR" class="gappedTr" style="display: none">
              <td><strong>Parent Type</strong></td><td><s:select list="types" name="parentDictTypeCode" id="parentDictTypeCode" /></td>
            </tr>
            <tr>
              <td></td>
              <td><div class="popup_alert_info" style="margin-top: 15px; display: none">
                <strong id="popupAlertDetail" style="color: #ffffff;background-color: #a90329;padding: 3px;border-color: #900323;border: 1px solid transparent;padding: 6px 12px;"></strong>
              </div></td>
            </tr>
          </table>
        </div>
        <div style="float:right;margin:5px 10px;">
          <input type="button" value="Add" onclick="_popup.add();" class="btn btn-info"/>
        </div>
      </fieldset>
    </div>
  </div>
</s:form>
<script>
  $(document).ready(function() {
    $('#hasDependency').change(function() {
      if($(this).is(":checked")) {
        $("#parentDictTypeCodeTR").show();
      } else{
        $("#parentDictTypeCodeTR").hide();
      }
    });
  });
  function initPopup() {
    utils.error.remove();
  }
  var _popup = {
    add: function() {
      var dictionaryType = $('input[name="dictType"]').val();
      var dictionaryValue = $('input[name="dictValue"]').val();
      var dictionaryCode = $('input[name="dictCode"]').val();
      var dictionaryParentType;
      if(!dictionaryType) {
        $('#popupAlertDetail').text("Type field is empty!");
        $('.popup_alert_info').show();
        return;
      } else if(!dictionaryValue){
        $('#popupAlertDetail').text("Value field is empty!");
        $('.popup_alert_info').show();
        return;
      } else {
        if($("#hasDependency").is(':checked')){
          dictionaryParentType = $('select[name="parentDictTypeCode"]').val();
          if(!dictionaryParentType){
            $('#popupAlertDetail').text("Parent Type field is empty!");
            $('.popup_alert_info').show();
            return;
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
                var $errorMsg = $('<div id="errorMsg" style="clear:both;" class="alert_error" onclick="_popup.closeError()">' + dataMap.errorMsg + '</div>');
                $('fieldset').append($errorMsg);
              } else {
                var newOption = vs.vvoption.replace(/\$v\$/g, res.lvName);
                if(res.lvType==='Attribute') {
                  if(typeof emaOptions!='undefined' && emaOptions.length>0) emaOptions+=newOption;
                  else if(typeof pmaOptions!='undefined' && emaOptions.length>0) pmaOptions+=newOption;
                } else if(res.lvType==='Event Type' && typeof etOptions==='string' && etOptions.length>0) etOptions+=newOption;
                $.closePopupLayer('LPopupAddDictionary');
              }
            }
          }
        });
      }
    },
    closeError: function() {$('#errorMsg').remove();}
  };
  (function() {initPopup();})();
</script>
</body>
</html>