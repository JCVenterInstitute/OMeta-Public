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
  <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
  <head>
    <c:import url="../header.jsp" />
    <style>
    </style>
  </head>

  <body class="smart-style-2">
    <div id="container">
      <jsp:include page="../top.jsp" />

      <div id="main" class="">
        <div id="content" class="container max-container" role="main">
          <%@ page import = "java.util.Properties" %>
          <%@ page import = "org.jtc.common.util.property.PropertyHelper" %>
          <% 
            Properties props = PropertyHelper.getHostnameProperties("resource/LoadingEngine");
            String websiteUrl=props.getProperty("ometa.dpcc.website.url");
            pageContext.setAttribute("websiteUrl", websiteUrl);
          %>
          <div id="ribbon">
            <ol class="breadcrumb">
              <li>
                <a href="/ometa/secureIndex.action">Dashboard</a>
              </li>
              <li>Help</li>
            </ol>
          </div>

          <style type="text/css">
          .panel h4 {margin-bottom: .5em}
          </style>

          <div class="row">
            <div class="col-xs-12">
              <h1 class="page-title page-header">DPCC Help</h1>
            </div>
            <div class="col-xs-12">
              <p class="lead">Get support when you need it. We have a variety of options to help.</p>
            </div>
          </div>

          <div class="row">

            <div class="col-sm-6">
              <div class="panel panel-info">
                <div class="panel-heading">
                  <h3 class="panel-title">Contact Us</h3>
                </div>
                <div class="panel-body">
                  <ul class="list-unstyled">
                    <li>
                      <h4><span class="fa fa-fw fa-phone"></span> By Phone</h4>
                      <p>Call the CEIRS DPCC hotline: 1-855-846-2697</p>
                    </li>
                    <li>
                      <h4><span class="fa fa-fw fa-envelope-o"></span> By Email</h4>
                      <p>Email the CEIRS DPCC Help Desk: <a href="mailto:support@niaidceirs.org">support@niaidceirs.org</a></p>
                    </li>
                    <li>
                      <h4><span class="fa fa-fw fa-wrench"></span> Online</h4>
                      <p><a href="/ometa/support.action">Submit a support request</a> online.</p>
                    </li>
                  </ul>
                </div>
              </div>

              <div class="panel panel-info">
                <div class="panel-heading">
                  <h3 class="panel-title">Training</h3>
                </div>
                <div class="panel-body">
                  <div class="well well-sm">
                    Coming soon.
                  </div>
                  <!-- <ul class="list-unstyled">
                    <li>
                      <h4><span class="fa fa-fw fa-info-circle"></span> Resources</h4>
                      <p><a href="#">DPCC Document Repository</a> with manuals and training materials</p>
                    </li>
                  </ul> -->
                </div>
              </div>

            </div>

            <div class="col-sm-6">
              <div class="panel panel-info">
                <div class="panel-heading">
                  <h3 class="panel-title">Knowledge Base</h3>
                </div>
                <div class="panel-body">
                  <ul class="list-unstyled">
                    <li>
                      <h4><span class="fa fa-fw fa-files-o"></span> DPCC Data Standard References &amp; Templates</h4>
                      <h5>Surveillance</h5>
                      <table class="table">
                        <thead>
                          <tr>
                            <th width="50%">Submission Type</th>
                            <th>Standard Reference</th>
                            <th>Submission Template</th>
                          </tr>
                        </thead>
                        <tr>
                          <td>Human Surveillance</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Human_Surveillance_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Human_Surveillance_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-template/DPCC_Data_Standard_Template_for_Human_Surveillance_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Non-Human Mammal Surveillance</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Non-Human_Mammal_Surveillance_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Non-Human_Mammal_Surveillance_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-template/DPCC_Data_Standard_Template_for_Non-Human_Mammal_Surveillance_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Avian Surveillance</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Wild_and_Domestic_Bird_Surveillance_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Wild_and_Domestic_Bird_Surveillance_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-template/DPCC_Data_Standard_Template_for_Wild_and_Domestic_Bird_Surveillance_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                    </table>
                    <h5>Assays</h5>
                    <table class="table">
                        <thead>
                          <tr>
                            <th width="50%">Submission Type</th>
                            <th>Standard Reference</th>
                            <th>Submission Template</th>
                          </tr>
                        </thead>
                        <tr>
                          <td>Serological Data</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Serological_Data_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Serological_Data_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-template/DPCC_Data_Standard_Template_for_Serological_Data_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Viral Isolate Data</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Viral_Isolate_Data_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Viral_Isolate_Data_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-template/DPCC_Data_Standard_Template_for_Viral_Isolate_Data_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Sequence Data</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_METADATA_Reference_for_Sequence_Submission_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_METADATA_Reference_for_Sequence_Submission_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_FASTA_Reference_for_Sequence_Submission_v1.0.xlsx"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> FASTA Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_FASTA_Reference_for_Sequence_Submission_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> FASTA PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC_Sequence_Submission_Template_v1.zip"><span class="fa fa-file-zip-o" style="color:#0000cc"></span> ZIP</a></td>
                        </tr>
                      </table>
                      <h5>Reagents</h5>
                      <table class="table">
                        <thead>
                          <tr>
                            <th width="50%">Submission Type</th>
                            <th>Standard Reference</th>
                            <th>Submission Template</th>
                          </tr>
                        </thead>
                        <tr>
                          <td>Clinical Study</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Clinical_Study_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Clinical_Study_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Clinical_Study_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Monoclonal Antibody</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Monoclonal_Antibody_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Monoclonal_Antibody_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Monoclonal_Antibody_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Plasmid</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Plasmid_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Plasmid_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Plasmid_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Protein</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Protein_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Protein_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Protein_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Reverse Genetic Virus</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Reverse_Genetic_Virus_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Reverse_Genetic_Virus_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Reverse_Genetic_Virus_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Sera and Polyclonal Antibody</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Sera_and_Polyclonal_Antibody_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Sera_and_Polyclonal_Antibody_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Sera_and_Polyclonal_Antibody_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                        <tr>
                          <td>Strain</td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Strain_Repository_v1.0.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a><br><a href="https://s3.amazonaws.com/dpcc-docs/data-standard-reference/DPCC_Data_Standard_Reference_for_Strain_Repository_v1.0.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                          <td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/reagents/DPCC_Data_Standard_Template_for_Strain_Repository_v1.0.csv"><span class="fa fa-file-o" style="color:#009900"></span> CSV</a></td>
                        </tr>
                      </table>
                    </li>
                  </ul>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
      
      <jsp:include page="../../html/footer.html" />
    </div>
  </body>
</html>