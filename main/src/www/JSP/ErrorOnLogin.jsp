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
<head>
  <jsp:include page="header.jsp"/>
</head>

<body class="smart-style-2">
<div id="container">

  <jsp:include page="top.jsp"/>

  <div id="main">
    <div id="content" class="container max-container" role="main">
      <div class="page-header">
        <h1>OMETA Login</h1>
      </div>
      <div class="row">

        <div class="col-12-xs col-sm-4 col-md-4">

          <div class="panel panel-primary">
            <div class="panel-heading">
              <h3 class="panel-title">Sign In</h3>
            </div>
            <div class="panel-body">
              <div class="row">
                <div class="alert alert-danger" role="alert">
                  <button type="button" class="close" data-dismiss="alert">
                    <span aria-hidden="true">&times;</span>
                    <span class="sr-only">Close</span>
                  </button>
                  Login failed. Please check username and password.
                </div>
              </div>
              <form method="POST" action="j_security_check" id="loginPage" name="loginPage" role="form">
                <div class="form-group">
                  <label for="usernameInput">Username</label>
                  <input type="text" class="form-control" name="j_username" placeholder="Enter username">
                </div>
                <div class="form-group">
                  <!-- <a class="pull-right" href="#">Forgot Password?</a> -->
                  
                  <label for="passwordInput">Password</label>
                  <input type="password" class="form-control" name="j_password" id="password" placeholder="Password">
                </div>
                <button id="loginButton" type="submit" class="btn btn-block btn-lg btn-primary">Sign in</button>
                <!-- <button type="submit" disabled="disabled" class="btn btn-sm btn-default">Sign in</button> -->
                <hr>
                <%--<div class="row">
                  <div class="col-sm-12">
                    <p><strong>Don't have an account?</strong> <a href="access.action">Request Access</a></p>
                  </div>
                  <div class="col-sm-12">
                    <p><strong>Need help?</strong> <a href="support.action">Send a support request</a> or email <a href="mailto:support@niaidceirs.org">support@niaidceirs.org</a></p>
                  </div>
                </div>--%>
              </form>
            </div>
          </div>

        </div>

        <div class="col-sm-8 col-md-8 hidden-xs">
          <%--<h4>Welcome to the CEIRS Data Processing and Coordinating Center site.</h4>

          <p style="margin-top:20px;">Here, members of the CEIRS network can:</p>
          <ol>
            <li>Submit data to the DPCC for processing and re-distribution to public databases</li>
            <li>Retrieve data submission metrics for their Center</li>
            <li>Request technical support</li>
            <li>Access training and education materials</li>
          </ol>

          <p>Please login to access these features or <a href="access.action">Request Access</a> if this is your first time here.</p>--%>
        </div>

      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html"/>

<script>
  $(function () {
    $("#password").keyup(function (event) {
      if (event.keyCode == 13) {
        $("#loginButton").click();
      }
    });
  });

</script>

</body>
</html>

