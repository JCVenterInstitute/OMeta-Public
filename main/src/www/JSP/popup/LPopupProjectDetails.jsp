<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
  <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
  <link rel="stylesheet" href="style/version01.css" />

  <style>
    #popupLayer_LPopupProjectDetails {
      top: 50px !important; /* fix project popup to near top */
    }
  </style>
</head>
<body>
<div class="popup" style="overflow:auto;">
  <div class="popup-header">
    <h2>Selected Project Detail</h2>
    <a href="#" onclick="_popup.close();" title="Close" class="close-link">Close</a>
    <br clear="both" />
  </div>
  <div style="padding:0;">
    <fieldset style="padding:5px;">
      <legend style="margin-left:10px;font-size:14px;"></legend>
      <div style="margin:0">
        <table name="projectTable" id="projectTable" class="contenttable" style="width:95%;">
          <thead>
          <tr id="projectTableHeader">
            <!-- <th class="tableHeaderStyle">Project Code</th>
            <th class="tableHeaderStyle">Project Title</th>
            <th class="tableHeaderStyle">Project Description</th>
            <th class="tableHeaderStyle">Project PI</th>
            <th class="tableHeaderStyle">Project Co-PI</th>
            <th class="tableHeaderStyle">Collection Institution</th> -->
          </tr>
          </thead>
          <tbody id="projectTableBody"/>
        </table>
      </div>
    </fieldset>
  </div>
</div>
<script src="scripts/jquery/jquery.dataTables.js"></script>
<script>
  var _popup = {
    run: function() {
      $('#LPopupProjectDetails').submit();
      this.close();
    },
    close: function() {
      $.closePopupLayer('LPopupProjectDetails');
    }
  }

  $(function() {
    var projectParam = '${projectName}';
    var displayAttributes = '${ids}';
    displayAttributes = displayAttributes.split(",");

    $.each(displayAttributes, function(i,v) {
      $('#projectTableHeader').append('<th class="tableHeaderStyle">' + v + '</th>');
    });

    var pDT = $("#projectTable").dataTable({
      "sDom": '<"datatable_top"><"datatable_table"rt><"datatable_bottom">',
      "bProcessing": true,
      "bServerSide": true,
      "sAjaxSource": "getprojectbyuser.action?projectName=" + projectParam,
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