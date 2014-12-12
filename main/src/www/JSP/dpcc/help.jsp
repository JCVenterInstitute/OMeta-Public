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
            </div>

            <div class="col-sm-6">
              <div class="panel panel-info">
                <div class="panel-heading">
                  <h3 class="panel-title">Knowledge Base</h3>
                </div>
                <div class="panel-body">
                  <ul class="list-unstyled">
                    <li>
                      <h4><span class="fa fa-fw fa-files-o"></span> DPCC Data Standard Reference</h4>
                      
                      <table class="table">
                        <tr>
                          <th>Human Surveillance</th><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Human+Surveillance.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a></td><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Human+Surveillance.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                        </tr>
                        <tr>
                          <th>Non-Human Mammal Surveillance</th><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Non-Human+Mammal+Surveillance.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a></td><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Non-Human+Mammal+Surveillance.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                        </tr>
                        <tr>
                          <th>Avian Surveillance</th><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Wild+and+Domestic+Bird+Surveillance.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a></td><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Wild+and+Domestic+Bird+Surveillance.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                        </tr>
                        <tr>
                          <th>Serological Data</th><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Serological+Data.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a></td><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Serological+Data.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                        </tr>
                        <tr>
                          <th>Viral Isolate Data</th><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Viral+Isolate+Data.xlsx"><span class="fa fa-file-excel-o" style="color:green"></span> Excel</a></td><td><a href="https://s3.amazonaws.com/dpcc-docs/data-submission-templates/DPCC+Data+Standard+Reference+for+Viral+Isolate+Data.pdf"><span class="fa fa-file-pdf-o" style="color:#cc0000"></span> PDF</a></td>
                        </tr>
                      </table>
                    </li>
                  </ul>
                </div>
              </div>

            </div>
          </div>

          <div class="row">
            <div class="col-sm-6">
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

            <!-- <div class="col-sm-6">
              <div class="jarviswidget" role="widget">
                <header role="heading">
                  <span class="widget-icon"> <i class="fa fa-ticket"></i> </span>
                  <h2>Support Tickets Summary</h2>
                </header>
                <div role="content">
                  <div class="widget-body no-padding">
                    <table class="table table-bordered">
                      <thead>
                        <tr>
                          <th>Ticket #</th>
                          <th>Issue Date</th>
                          <th>Topic</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td><a href="#">SU-1234</a></td>
                          <td>12/8/2014</td>
                          <td>Data Submission</td>
                          <td>Assigned</td>
                        </tr>
                        <tr>
                          <td><a href="#">SU-4567</a></td>
                          <td>12/15/2014</td>
                          <td>Username/Password</td>
                          <td>Assigned</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div> -->

          </div>
        </div>
      </div>
      
      <jsp:include page="../../html/footer.html" />
    </div>
  </body>
</html>