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
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<!doctype html>
<head>
  <jsp:include page="header.jsp"/>
</head>

<body class="smart-style-2">
<div id="container">
  <div id="main">
    <div id="content" class="container max-container" role="main">
      <div class="page-header text-center">
        <img class="headerImage" src="<c:url value='/images/ometa_logo.png' />" alt="Ontology based Metadata Tracking">
      </div>
      <div class="row">
        <div class="col-sm-4 col-sm-offset-4">
          <div class="panel panel-primary">
            <div class="panel-heading">
              <h3 class="panel-title">Sign In</h3>
            </div>
            <div class="panel-body">
              <form method="POST" action="j_security_check" id="loginPage" name="loginPage" role="form">
                <div class="form-group">
                  <label for="usernameInput">Username</label>
                  <input type="text" class="form-control" name="j_username" placeholder="Enter username">
                </div>
                <div class="form-group">
                  <label for="passwordInput">Password</label>
                  <input type="password" class="form-control" name="j_password" id="password" placeholder="Password">
                </div>
                <button id="loginButton" type="submit" class="btn btn-block btn-lg btn-primary">Sign in</button>
                <hr>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="../html/footer.html"/>

<script>
  $(document).on("keydown", function (e) {
    var keyCode = e.which || e.keyCode;
    if (keyCode == 13) { // enter key code
      $("#loginPage").submit();
    }
  });
</script>

</body>
</html>