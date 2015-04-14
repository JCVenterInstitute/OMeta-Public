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
    <jsp:include page="header.jsp" />
    <link rel="stylesheet" href="style/dataTables.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />

    <link rel="stylesheet" href="style/version01.css" />
    <style>
        #EMADiv .ui-autocomplete-input,
        #SMADiv .ui-autocomplete-input,
        #PMADiv .ui-autocomplete-input {
            width: 150px;
        }
        /* row background: mouse hover & new row */
        tr.borderBottom:hover>td.comboBoxCB,
        tr.borderBottom:hover>td input,
        tr.borderBottom:hover>td>textarea {
            background: #bbdcf8;
        }
        .buttonAdded>td.comboBoxCB,
        .buttonAdded>td input,
        .buttonAdded>td>textarea {
            background: #e9f4fd;
        }
        tr>td.fix172 {
            min-width:172px;
        }
        ul.ui-autocomplete.ui-menu {
            width:300px
        }
    </style>
</head>

<body class="smart-style-2">
<div id="container">

    <jsp:include page="top.jsp" />

    <div id="main" class="">
        <div id="inner-content" class="">
            <div class="container max-container" role="main">
                <div id="ribbon">
                    <ol class="breadcrumb">
                        <li>
                            <a href="/ometa/secureIndex.action">Dashboard</a>
                        </li>
                        <li>Admin</li>
                        <li>Metadata Setup</li>
                    </ol>
                </div>

                <s:form id="metadataSetupPage" name="metadataSetupPage" namespace="/" action="metadataSetup" method="post" theme="simple">
                    <s:hidden name="type" id="type" />

                    <div class="page-header">
                        <h1>Metadata Setup</h1>
                    </div>

                    <div id="HeaderPane">
                        <div id="errorMessagesPanel" style="margin-top:15px;"></div>
                        <s:if test="hasActionErrors()">
                            <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
                        </s:if>
                        <s:if test="hasActionMessages()">
                            <div class="alert_info" onclick="$('.alert_info').remove();">
                                <strong><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
                            </div>
                        </s:if>
                    </div>
                    <div id="mainContent">
                        <div id="statusTableDiv">
                            <div id="tableTop">
                                <div class="row row_spacer">
                                    <div class="col-md-1">Project Name</div>
                                    <div class="col-md-5 combobox">
                                        <s:select id="_projectSelect" list="projectList" name="projectId" headerKey="0" headerValue="" listValue="projectName" listKey="projectId" required="true" />
                                    </div>
                                    <div id="loadingImg" style="display:none;">
                                      <img src="images/loading.gif" style="width:24px"/>
                                    </div>
                                </div>
                                <div class="row row_spacer">
                                    <div class="col-md-1">Event</div>
                                    <div class="col-md-11 combobox">
                                        <s:select id="_eventSelect" list="#{'0':''}" required="true" disabled="true" />
                                    </div>
                                </div>
                            </div>
                            <div id="PMADiv" style="display:none;">
                                <div style="margin:10px 10px 0 0;">
                                    <h1 class="csc-firstHeader">Project Meta Attributes</h1>
                                </div>
                                <div id="PMAInputDiv" style="margin:5px 10px 0 0;">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th class="tableHeaderNoSort boxesHeader">Attribute</th>
                                            <th class="tableHeaderNoSort boxesHeader">Label</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Active</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Required</th>
                                            <th class="tableHeaderNoSort boxesHeader">Default Options</th>
                                            <th class="tableHeaderNoSort boxesHeader">Desc</th>
                                            <th class="tableHeaderNoSort boxesHeader">Ontology</th>
                                        </tr>
                                        </thead>
                                        <tbody id="pmaAdditionTbody"></tbody>
                                    </table>
                                </div>
                                <div id="PMASubmitDiv" style="margin:15px 10px 5px 0;width:100%;">
                                    <input type="button" onclick="_utils.submit();" id="projectLoadButton" value="Setup Project"/>
                                    <input type="button" onclick="_utils.add.ma('a','pmaAdditionTbody');" id="pmaAddButton" value="Add Project Meta Attribute" disabled="disabled"/>
                                    <input type="button" onClick="_utils.ontology();" id="ontologyBtn" value="Ontology"/>
                                    <input type="button" onclick="_utils.clean.all();" value="Clear" />
                                </div>
                            </div>
                            <div id="SMADiv" style="display:none;">
                                <div style="margin:10px 10px 0 0;">
                                    <h1 class="csc-firstHeader">Sample Meta Attributes</h1>
                                </div>
                                <div id="SMAInputDiv" style="margin:5px 10px 0 0;">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th class="tableHeaderNoSort boxesHeader">Attribute</th>
                                            <th class="tableHeaderNoSort boxesHeader">Label</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Active</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Required</th>
                                            <th class="tableHeaderNoSort boxesHeader">Default Options</th>
                                            <th class="tableHeaderNoSort boxesHeader">Desc</th>
                                            <th class="tableHeaderNoSort boxesHeader">Ontology</th>
                                        </tr>
                                        </thead>
                                        <tbody id="smaAdditionTbody"></tbody>
                                    </table>
                                </div>
                                <div id="SMASubmitDiv" style="margin:15px 10px 5px 0;width:100%;">
                                    <input type="button" onclick="_utils.submit();" id="sampleLoadButton" value="Setup Sample"/>
                                    <input type="button" onclick="_utils.add.ma('a','smaAdditionTbody');" id="smaAddButton" value="Add Sample Meta Attribute" disabled="disabled"/>
                                    <input type="button" onclick="_utils.ontology();" id="ontologyButton" value="Ontology" />
                                    <input type="button" onclick="_utils.clean.all();" value="Clear" />
                                </div>
                            </div>
                            <div id="EMADiv" style="display:none;">
                                <div style="margin:10px 10px 0 0;">
                                    <h1 class="csc-firstHeader middle-header">Event Meta Attributes</h1>
                                </div>
                                <div id="EMAInputDiv" style="margin:5px 10px 0 0;">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th class="tableHeaderNoSort boxesHeader">Event Type</th>
                                            <th class="tableHeaderNoSort boxesHeader">Attribute</th>
                                            <th class="tableHeaderNoSort boxesHeader">Label</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Active</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Required</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Sample Required</th>
                                            <th class="tableHeaderNoSort boxesHeader">Default Options</th>
                                            <th class="tableHeaderNoSort boxesHeader">Desc</th>
                                            <th class="tableHeaderNoSort boxesHeader">Ontology</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Project Metadata</th>
                                            <th class="tableHeaderNoSort checkBoxHeader">Sample Metadata</th>
                                            <th class="tableHeaderNoSort boxesHeader">Pos</th>
                                        </tr>
                                        </thead>
                                        <tbody id="etAdditionTbody"></tbody>
                                    </table>
                                </div>
                                <div id="EMASubmitDiv" style="margin:15px 10px 5px 0;">
                                    <input type="button" class="btn btn-success" onclick="_utils.submit();" id="eventLoadButton" value="Setup Event"/>
                                    <input type="button" class="btn btn-primary" onclick="_utils.add.ema('add');" id="eventAddButton" value="Add Event Meta Attribute" disabled="disabled" />
                                    <input type="button" class="btn btn-info" onclick="_utils.popup.attribute('et');" id="newEventButton" value="New Event Type" />
                                    <input type="button" class="btn btn-info" onclick="_utils.popup.attribute('a');" id="newAttributeButton" value="New Attribute" />
                                    <input type="button" class="btn btn-info" onclick="_utils.ontology();" id="ontologyButton" value="Ontology" />
                                    <input type="button" class="btn btn-default" onclick="_utils.clean.all();" value="Clear" />
                                </div>
                            </div>
                        </div>
                    </div>

                </s:form>

            </div>
        </div>
    </div>
</div>

<jsp:include page="../html/footer.html" />

<script>
    var maCnt = 0, type;
    var maOptions, etOptions, emaDict;

    $(document).ready(function() {
        $('select').combobox();
        type = $('input[name="type"]').val();
        if(type==='s') {
            _utils.flip('Sample', 'SMADiv');
        } else if(type==='e'){
            _utils.flip('Event', 'EMADiv');
            $('div#eventDropBox').show();
        } else {
            _utils.flip('Project', 'PMADiv');
        }

        utils.error.check();
    });
</script>
<script src="scripts/page/metadata.setup.js"></script>
</body>
</html>
