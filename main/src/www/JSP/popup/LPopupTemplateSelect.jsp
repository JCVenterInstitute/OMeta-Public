<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}" />
</head>

<body>
<s:form id="LPopupTemplateSelect" name="LPopupTemplateSelect" namespace="/" method="post" theme="simple" action="eventLoader">
    <s:hidden name="projectName"/>
    <s:hidden name="eventName"/>
    <div class="popup">
        <div class="popup-header">
            <h2>Select Template Format</h2>
            <a href="#" onclick="_popup.close" title="Close" class="close-link">Close</a>
            <br clear="both" />
        </div>
        <div style="padding:10px;">
            <fieldset style="padding:5px;">
                <legend style="margin-left:10px;font-size:14px;"></legend>
                <div style="margin:5px 5px 5px 5px;">
                    <table>
                        <tr>
                            <td>Format</td>
                            <td>
                                <select name="jobType" id="jobType">
                                    <option value="template_c">CSV</option>
                                    <option value="template_e">Excel</option>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>
                <div style="float:right;margin:5px 10px;">
                    <input type="button" value="Download" onclick="_popup.run();"/>
                </div>
            </fieldset>
        </div>
    </div>
</s:form>

<script>
    var _popup = {
        run: function() {
            $('#LPopupTemplateSelect').submit();
            this.close();
        },
        close: function() {
            $.closePopupLayer('LPopupTemplateSelect');    
        }
    }
</script>
</body>
</html>