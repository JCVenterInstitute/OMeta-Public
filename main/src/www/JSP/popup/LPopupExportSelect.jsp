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
<s:form id="LPopupExportSelect" name="LPopupExportSelect" namespace="/" method="post" theme="simple" action="eventLoader">
  <s:hidden name="projectName"/><s:hidden name="projectId"/><s:hidden name="jobType" value="export"/><s:hidden name="ids"/>
  <div class="popup">
    <div class="popup-header">
      <h2 id="popupHeader" style="float:left">Select Project Event</h2>
      <a href="#" onclick="_popup.close();" title="Close" class="close-link" style="float: right;"><img src="images/xBtn.gif" title="Close" /></a>
      <br clear="both" />
    </div>
    <div style="padding:10px;">
      <fieldset style="padding:5px;">
        <div style="margin:5px 5px 5px 5px;">
          <table>
            <tr>
              <td style="padding-right: 15px;"><strong>Event Name</strong></td>
              <td><s:select list="eventTypeList" listKey="lookupValueId" listValue="name" name="eventId" id="eventId" /></td>
            </tr>
          </table>
        </div>
        <div style="float:right;margin:5px 10px;">
          <input type="button" value="Download" onclick="_popup.run();" class="btn btn-info"/>
        </div>
      </fieldset>
    </div>
  </div>
</s:form>

<script>
  var _popup = {
    run: function() {
      $('<input>').attr({
        id: 'eventName',
        name: 'eventName',
        value : $("#eventId option:selected").text(),
        type: 'hidden'
      }).appendTo($('#LPopupExportSelect'));

      $('#LPopupExportSelect').submit();
      this.close();
    },
    close: function() {
      $.closePopupLayer('LPopupExportSelect');
    }
  }
</script>
</body>
</html>