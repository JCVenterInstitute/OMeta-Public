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

          <c:import url="${websiteUrl}/dpcc/help/support.php?framed=1" />
        </div>
      </div>
      
      <jsp:include page="../../html/footer.html" />
    </div>
  </body>
</html>