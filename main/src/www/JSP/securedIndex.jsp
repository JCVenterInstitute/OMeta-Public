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
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<head>
  <jsp:include page="header.jsp" />
  <style>
    #welcomeMessage{width: 10%; background-color: #d9edf7; border-color: #bce8f1;
      color: #31708f;margin-bottom: 10px;border: 1px solid transparent;
      border-radius: 4px;padding: 15px 0 15px 15px;width: 70%;
      margin-left: auto; margin-right: auto;margin-top: 100px;text-align: center;
    }

    .page-header{text-align: center;padding-top: 15px}
  </style>
</head>

<body class="smart-style-2">
<div id="container">
  <jsp:include page="top.jsp" />

  <div id="main" class="">
    <div id="content" class="container max-container" role="main">
      <div class="page-header">
        <h1> O-META: Ontologies based Metadata Tracking Application</h1>
      </div>

      <div id="mainContent" style="">
        <div id="welcomeMessage" >Welcome to OMETA!</div>
      </div>
    </div>
  </div>
</div>
<jsp:include page="../html/footer.html" />
<script>
</script>
</body>
</html>