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
    <script>
        var _popup = {
            init: function(which) {
                var type = which==='et' ? 'Event Type' : 'Attribute';
                $('#popupHeader').append(type);
                $('#popupLegend').prepend(type)
                $('#lvType').val(type);

                if(which==='et') {
                    $('#lvDataType').val('string');
                    $('#dataTypeSelectTr').hide();
                }
                utils.error.remove();
            },
            add: function() {
                if(!$('input[name="lvName"]').val()) {
                    alert("Name field is empty!");
                    return;
                } else {
                    $.ajax({
                        url: 'metadataSetupAjax.action',
                        cache: false,
                        async: false,
                        data: 'type=a_lv&lvName='+$('#lvName').val()+'&lvType='+$('#lvType').val()+'&lvDataType='+$('#lvDataType').val(),
                        success: function(html){
                            if(html.lerror!=null && html.lerror==='true') {
                                utils.error.add(html.errorMsg);
                            } else {
                                var newOption = vs.vvoption.replace(/\\$v\\$/g, html.lvName);
                                if(html.lvType==='Attribute') {
                                    if(typeof emaOptions!='undefined' && emaOptions.length>0) {
                                        emaOptions+=newOption;
                                    } else if(typeof pmaOptions!='undefined' && emaOptions.length>0) {
                                        pmaOptions+=newOption;
                                    }
                                }else if(html.lvType==='Event Type' && typeof etOptions==='string' && etOptions.length>0) {
                                    etOptions+=newOption;
                                }
                            }
                            $.closePopupLayer('LPopupAddLookupValue');
                        },
                        fail: function(html) {
                            alert("Lookup Value addition Failed.");
                        }
                    });
                }    
            }
        }
        $(document).ready(_popup.init('${w}'));
    </script>
</head>

<body>
<s:form id="LPopupAddLookupValue" name="LPopupAddLookupValue" namespace="/" action="addLookupValueProcess" method="post" theme="simple">
    <s:hidden name="w"/>
    <div class="popup">
        <div class="popup-header">
            <h2 id="popupHeader">Add </h2>
            <a href="#" onclick="$.closePopupLayer('LPopupAddLookupValue')" title="Close" class="close-link">Close</a>
            <br clear="both" />
        </div>
        <div style="padding:10px;">
            <fieldset style="padding:5px;">
                <legend id="popupLegend" style="margin-left:10px;font-size:14px;"> Information</legend>
                <div style="margin:5px 5px 5px 5px;">
                    <table>
                        <tr>
                            <td><strong>Type</strong></td><td><s:select list="types" name="lvType" id="lvType" disabled="true"/></td>
                        </tr>
                        <tr class="gappedTr">
                            <td><strong>Name</strong></td><td><s:textfield id="lvName" name="lvName" size="30"/></td>
                        </tr>
                        <tr>
                            <td/><td>Use comma(,) as delimiter to load multiple values</td>
                        </tr>
                        <tr class="gappedTr" id="dataTypeSelectTr">
                            <td><strong>Date Type</strong></td><td><s:select list="dataTypes" name="lvDataType" id="lvDataType"/></td>
                        </tr>
                    </table>
                </div>
                <div style="float:right;margin:5px 10px;">
                    <input type="button" value="Add" onclick="_popup.add();"/>
                </div>
            </fieldset>
        </div>
    </div>
</s:form>
</body>
</html>