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
      .loadRadio { margin-left: 10px; margin-right: 3px; }
      #gridBody .ui-autocomplete-input { width: 150px; }
      .gridIndex { max-width: 20px !important; min-width: 15px; text-align: center;}
      .ms-choice {line-height: 20px; }
      .ms-choice, .ms-choice > div { height: 20px; }

    </style>
      <style>

     	#dropbox {

     		border-style: dashed dashed dashed dashed ;
     		border-color: gray;
     		width: 500px;
     		height: 200px

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
                           <div class="row row_spacer">
                               <div class="panel-body">
                                   <div class="form-group">
                                       <div class="row row_spacer" id="projectSelectRow">
                                         <div class="col-md-1">Select file</div>
                                         <div class="col-md-11">
                                             <input id="multiple" type="file" multiple>
                                         </div>
                                       </div>
                                   </div>
                                   <p>Drap and Drop file in box to upload </p>
                                   <div id="dropbox"></div>
                               </div>
                           </div>
                       </div>
                   </div>
               </div>
           </div>


      <jsp:include page="../html/footer.html" />

    </div>

    <script src="scripts/jquery/jquery.multiple.select.js"></script>
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.min.js"></script>
   	<script type="text/javascript" src="scripts/jquery.html5uploader.min.js"></script>
   	<script type="text/javascript">
   	$(function() {
   		$("#dropbox, #multiple").html5Uploader({
   			name: "foo",
   			postUrl: "bar.aspx"
   		});
   	});
   	</script>

  </body>
</html>