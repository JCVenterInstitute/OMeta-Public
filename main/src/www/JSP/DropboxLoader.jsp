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
<!doctype html>

  <%@ page contentType="text/html; charset=UTF-8" %>
  <%@ taglib uri="/struts-tags" prefix="s" %>
  <%@ page isELIgnored="false" %>

  <head>
    <jsp:include page="../html/header.html" />
    <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/multiple-select.css" type='text/css' media='all' />

    <link rel="stylesheet" href="style/version01.css" />
    <style>
     	#dropzone {
     		border-style: dashed;
     		border-color: #3276b1;
     		width: 500px;
     		height: 100px;
     	}
      .bar {
        height: 18px;
        background: green;
      }
   	</style>
  </head>

  <body class="smart-style-2">
    <div id="container">

      <jsp:include page="top.jsp" />
        <div id="main" class="container">
          <div id="inner-content" class="">
            <div id="content" class="" role="main">
              <div class="page-header">
                <h1>Data Submission Dropbox</h1>
              </div>
              <div id="tableTop">
                <div class="row col-md-12"><h5><strong> </strong></h5></div>
                <div class="row">
                  <div id="progress">
                    <div class="bar" style="width: 0%;"></div>
                  </div>
                </div>
                <div class="row row_spacer">
                  <div class="panel-body">
                    <div class="form-group">
                      <div class="row row_spacer" id="projectSelectRow">
                        <div class="col-md-1"><strong>Select file</strong></div>
                        <div class="col-md-11">
                          <input id="uploadFile" type="file" name="upload" data-url="fileUploadAjax.action">
                        </div>
                      </div>
                    </div>
                    <p>Drap and Drop file in box to upload (Max file size is 2GB) </p>
                    <div id="dropzone" class="well">Drop files here</div>
                    <div class="row row_space">
                      <div id="files" class="files"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>


      <jsp:include page="../html/footer.html" />

    </div>

    <script src="scripts/jquery/jquery.ui.widget.js"></script>
    <script src="scripts/jquery/jquery.iframe-transport.js"></script>
    <script src="scripts/jquery/jquery.fileupload.js"></script>

   	<script>
      $(function () {
        $('#uploadFile').fileupload({
          dataType: 'json',
          done: function (e, data) {
            $.each(data.result.result.files, function (index, file) {
              $('<p/>').html("'<strong>" + file.name + "</strong>' has been uploaded.").appendTo('#files');
            });
            //$('#progress .bar').css('width', '0%');
          },
          dropZone: $('#dropzone'),
          progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('#progress .bar').css('width', progress + '%');
          }
        });

        $(document).bind('dragover', function (e) {
          var dropZone = $('#dropzone'),
              timeout = window.dropZoneTimeout;
          if(!timeout) {
            dropZone.addClass('in');
          } else {
            clearTimeout(timeout);
          }
          var found = false, node = e.target;
          do {
            if(node === dropZone[0]) {
              found = true;
              break;
            }
            node = node.parentNode;
          } while(node != null);
          if(found) {
            dropZone.addClass('hover');
          } else {
            dropZone.removeClass('hover');
          }
          window.dropZoneTimeout = setTimeout(function () {
            window.dropZoneTimeout = null;
            dropZone.removeClass('in hover');
          }, 100);
        });

        $(document).bind('drop dragover', function (e) {
          e.preventDefault();
        });
     	});
   	</script>

  </body>
</html>