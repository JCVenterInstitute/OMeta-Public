<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <link rel="stylesheet" href="style/version01.css" />

  <jsp:include page="header.jsp" />

  <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/version01.css" />
  <style>
  </style>
</head>

<body class="smart-style-2">
<div id="container">
  <jsp:include page="top.jsp" />
    
  <div id="main" class="container max-container">
    <div id="inner-content" class="">
      <div id="content" role="main">
        <div class="col-xs-12 page-header">
          <h1 class="page-title">Selected Project</h1>
        </div>

        <div id="HeaderPane" class="row">
          <div id="errorMessagesPanel" class="container"></div>
          <s:if test="hasActionErrors()">
            <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
          </s:if>
          <s:if test="hasActionMessages()">
            <div class="alert_info" onclick="$('.alert_info').remove();">
              <strong><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
            </div>
          </s:if>
        </div>
        <div id="mainContent">
          <div class="col-sm-12 col-lg-12">
            <div class="row">
              <div style="margin:0">
                <table name="projectTable" id="projectTable" class="contenttable" style="width:95%;">
                  <thead>
                  <tr id="projectTableHeader">
                  </tr>
                  </thead>
                  <tbody id="projectTableBody"/>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <jsp:include page="../html/footer.html" />
</div>

<script src="scripts/jquery/jquery.dataTables.js"></script>
<script>
  $(function() {
    var displayAttributes;
    var query = document.URL.split('?')[1];
    if(query){
      query = query.split('&');
      for(var i = 0; i < query.length; i++){
        attrs = query[i].split('=');
        displayAttributes = (decodeURIComponent(attrs[1]).replace(/\+/g, ' ').split(","));
      }
    };

    $.each(displayAttributes, function(i,v) {
      $('#projectTableHeader').append('<th class="tableHeaderStyle">' + v + '</th>');
    });

    var pDT = $("#projectTable").dataTable({
      "sDom": '<"datatable_top"><"datatable_table"rt><"datatable_bottom">',
      "bProcessing": true,
      "bServerSide": true,
      "sAjaxSource": "getprojectbyuser.action?projectName=",
      "fnServerData": function (sSource, aoData, fnCallback) {
        if(sSource!=='') {
          $.ajax({
            dataType: 'json',
            type: "POST",
            url: sSource,
            data: aoData,
            success: function(json) {
              if(json && json.aaData) {
                var rows = [];

                $.each(json.aaData, function(ri,rowData) {
                  if(rowData.project) {
                    var row = [];
                    $.each(displayAttributes, function(ai, av) {
                      row.push(rowData.attributes[av]);
                    });

                    rows.push(row);
                  }
                })
              }
              json.aaData = rows;
              fnCallback(json);
            }
          });
        }
      },
      "bAutoWidth" : true
    });
  });
</script>
</body>
</html>
