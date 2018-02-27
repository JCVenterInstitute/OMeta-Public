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
  <link rel="stylesheet" href="datatables/datatables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="datatables/Buttons-1.4.2/css/buttons.bootstrap.css" type='text/css' media='all' />
</head>
<style>
  label { font-weight: normal !important;}
</style>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="inner-content" class="">
      <div id="content" class="container max-container" role="main">
        <s:form id="infoDictionaryPage" name="infoDictionaryPage" theme="simple">
          <div class="page-header">
            <h1>Dictionary Information</h1>
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
                  <table id="dictionary-information-table" class="table table-bordered table-striped table-condensed table-hover">
                    <thead>
                    <tr>
                      <th>Dictionary Type</th>
                      <th>Dictionary Code</th>
                      <th>Dictionary Value</th>
                      <th>Parent Dependency</th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator value="dictionaryList" var="dictionary">
                      <tr>
                        <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="dictionaryType"><s:property value="#dictionary.dictionaryType"/></label></td>
                        <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="dictionaryCode"><s:property value="#dictionary.dictionaryCode"/></label></td>
                        <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="dictionaryValue"><s:property value="#dictionary.dictionaryValue"/></label></td>
                        <td><label id="<s:property value="#dictionary.dictionaryId"/>" class="parentDictionary"><s:property value="dependencyMap[#dictionary.dictionaryId]"/></label></td>
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


<script src="datatables/datatables.js"></script>
<script src="datatables/Buttons-1.4.2/js/dataTables.buttons.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.colVis.js"></script>
<script src="datatables/JSZip-2.5.0/jszip.js"></script>
<script src="datatables/pdfmake-0.1.32/pdfmake.js"></script>
<script src="datatables/pdfmake-0.1.32/vfs_fonts.js"></script>
<script src="datatables/Buttons-1.4.2/js/buttons.bootstrap.js"></script>

<script type="text/javascript">
  $(document).ready(function() {
    $('.navbar-nav li').removeClass('active');
    $('.navbar-nav > li:nth-child(6)').addClass('active');
    generateParentDependencyInfo();

    var table = $('#dictionary-information-table').DataTable( {
      lengthChange: false,
      buttons: [ 'copy', 'excel', 'pdf', 'csv', 'colvis']
    } );

    table.buttons().container()
        .appendTo( '#dictionary-information-table_wrapper .col-sm-6:eq(0)' );
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
</script>
</body>
</html>