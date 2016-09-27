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

<%@ page language="java" %>
<%@ page isErrorPage="true" %>
<%@ page import="java.util.*" %>
<%
// this string is only availble if the page is marked as an error page (above)
String request_uri = (String)request.getAttribute("javax.servlet.error.request_uri");

// handle j_security_checks by forwarding to the index page.
// people will still be confused because they might think they have logged in a second time.

if (request_uri != null && request_uri.indexOf("j_security_check") > 0) {
  request.getRequestDispatcher("/").forward(request, response);
}
%>

<!doctype html>
  <%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
  <head>
    <jsp:include page="header.jsp"/>
  </head>

  <body class="smart-style-2">
  <div id="container">

    <jsp:include page="top.jsp"/>

    <div id="main" class="container">
      <div id="inner-content" class="">
        <div id="content" class="" role="main">
          <div class="page-header">
            <h1>Error</h1>
          </div>
          <div class="row">

              <div id="panel-body">
                  <p>There was an error in processing your request. Please try it again.</p>

                  <p>To report the error go to <a href="dpcc_help.action">Help page</a></p>
                  <p>To login again to to <a href="secureIndex.action">Login page</a></p>

                  <br/><br/>

                  <c:catch>
                    <c:if test="${not empty pageContext.exception}">
                      <b>Exception stack trace:</b><br/>
                      <c:forEach var="trace" items="${pageContext.exception.stackTrace}">
                          <c:out value="${trace}" /><br/>
                      </c:forEach>
                    </c:if>

                    <p><b>Error code:</b> <c:out value="${pageContext.errorData.statusCode}" /></p>
                    <p><b>Exception:</b> <c:out value="${pageContext.errorData.throwable}" /></p><br />
                    <p><b>Request URI:</b> <c:out value="${pageContext.request.scheme}"/>://<c:out value="${header.host}" /><c:out value="${pageContext.errorData.requestURI}" /></p><br />
                  </c:catch>
              </div>

          </div>
        </div>
      </div>
    </div>

    <jsp:include page="../html/footer.html"/>

  </div>

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
