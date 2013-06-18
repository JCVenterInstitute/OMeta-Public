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

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}" />
    <script type="text/javascript">
        $(document).ready(function() {
            utils.combonize();
            $('#_ontologySelect').change(function() {
            });
        });
    </script>
</head>


<body>
<s:form id="LPopupOntologySearch" name="LPopupOntologySearch"
        namespace="/"
        action="ontologySearch"
        method="post" theme="simple">
    <div class="popup">
        <div class="popup-header">
            <h2>Ontology Search</h2>
            <a href="" onclick="$.closePopupLayer('LPopupOntologySearch')" title="Close" class="close-link">Close</a>
            <br clear="both" />
        </div>
        <div style="padding:10px;">
            <fieldset style="padding:5px;">
                <legend style="margin-left:10px;font-size:14px;">Ontologies</legend>
                <div id="ontologySelectDiv">
                    <div style="float:left;width:65px;">Ontology</div>
                    <div><s:select id="_ontologySelect" list="ontologies" name="ontology_a" headerKey="0" headerValue="" required="true" /></div>
                    <div><label>Search Term</label><input id="searchTerm" name="searchTerm" type="text" size="20" margin-left="15px"/></div>
                </div>
            </fieldset>
            <fieldset style="padding:5px;">
                <legend style='margin-left:10px;font-size:14px;'>Terms</legend>
                <div id="termsDiv">

                </div>
            </fieldset>
        </div>
    </div>
</s:form>
</body>
</html>