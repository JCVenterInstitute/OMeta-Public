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

<%--
  Created by IntelliJ IDEA.
  User: hkim
  Date: 8/30/11
  Time: 10:52 AM
  To change this template use File | Settings | File Templates.
  --%>
<!DOCTYPE html>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; {$charset|default:'charset=utf-8'}" />
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <title>O-META</title>
    <link rel="stylesheet" href="style/main.css" />
    <link rel="stylesheet" href="style/version01.css" />
    <style>
        #header { height:auto !important; }
        #header, #nav { margin: 0 10px !important; }
        #nav{
            height:32px;
            line-height:32px;
            background:#0081b3;
            padding:0 10px;
        }
        #nav ul, #nav ul li {
            margin:0;
            padding:0;
            list-style:none;
        }
        #nav ul li{
            float:left;
            display:block;
        }

        #nav ul li a:link, #nav ul li a:visited{
            color:#FFF;
            font-size:14px;
            font-weight:bold;
            text-decoration:none;
            padding:0 20px 0 6px;
            display:block;
        }
        #nav ul li a:hover{
            color:#EBEFF7;
        }
        #nav ul li ul li{
            float:none;
            display:block;
        }
        #nav ul li ul li a:link, #nav ul li ul li a:visited{
            color:#444;
            font-size:11px;
            font-weight:bold;
            text-decoration:none;
            padding:0 10px;
            clear:both;
            border-bottom:solid 1px #DEDEDE;
        }
        #nav ul li ul li a:hover{
            color:#3B5998;
            background:#EBEFF7;
        }

        .submenu {
            position: absolute;
            background: #FFF;
            padding: 10px;
            border: solid 1px #0081b3;
            border-top: none;
            display: none;
            line-height: 26px;
            z-index: 1000;
        }

        .headerLink {
            font-family: Arial, sans-serif;
            font-size: 9pt;
            font-style: normal;
            font-weight: normal;
            text-decoration: underline;
        }
    </style>
    
    <script src="scripts/jquery/jquery-1.7.2.js"></script>
    <script src="scripts/jquery/jquery-ui.js"></script>
    <script src="scripts/ometa.utils.js"></script>
    <script>
        <jsp:useBean id="userBean" class="org.jcvi.ometa.web_bean.UserInfoWebBean"/>
        <jsp:setProperty name="userBean" property="userId" value="<%=request.getRemoteUser()%>"/>

        var _searchArr = window.location.search.substr(1).split("&"),
            paramP, _temparr;
        if(_searchArr) {
            $(_searchArr).each(function() {
                _temparr=this.split("=");
                _temparr[0]==='p'?sessionStorage.setItem('pst.project', _temparr[1]):null;
            });
        }

        $(document).ready(function() {
            $('div#nav ul li').mouseover(function() {
                $(this).find('ul:first').show();
            });
            $('div#nav ul li, div#nav ul li ul').mouseleave(function() {
                $('div#nav ul li ul').hide();
            });

            var userName='<jsp:getProperty name="userBean" property="fullname"/>',
                isAdmin='<jsp:getProperty name="userBean" property="admin"/>';
            _=(isAdmin!=null && isAdmin!=='null' && isAdmin==='true')?$('#admin_li').show():$('#admin_li').hide();
            if(userName!=null && userName!=='null') {
                $('div#currUserName').html(userName+', <a class="headerLink" href="logout.action">Log Out</a>');
                $('.noauthuser').hide();
            } else {
                $('.authuser').hide();
                $('.noauthuser').show();   
            }
        });
    </script>
</head>
<body>
    <div id="header">
        <table cellspacing="0" cellpadding="0">
            <tbody>
            <tr>
                <td align="left" style="vertical-align: top;">
                    <table cellspacing="0" cellpadding="0" class="HeaderPanel">
                        <tbody>
                        <tr>
                            <td align="left" style="vertical-align: top;padding: 10px;">
                                <img class="headerImage" src="images/ometa_logo.png" alt="Ontology based Metadata Tracking">
                            </td>
                            <td align="left" style="padding: 0 0 0 25px;">
                                <table cellspacing="0" cellpadding="0" class="HeaderLinkPanel">
                                    <tbody>
                                    <tr>
                                        <td align="right">
                                            <div>
                                                <a class="headerLink" href="help.action">Help</a>
                                            </div>
                                        </td>
                                        <td align="right">
                                            <div class="HeaderLinkSeparator">|</div>
                                        </td>
                                        <td align="right" class="authuser">
                                            <div class="HeaderLink" id="currUserName" display="none"></div>
                                        </td>
                                        <td align="right" class="noauthuser">
                                            <div>
                                                <a class="headerLink" href="addActor.action">Register</a>
                                            </div>
                                        </td>
                                        <td align="right" class="noauthuser">
                                            <div class="HeaderLinkSeparator">|</div>
                                        </td>
                                        <td align="right" class="noauthuser">
                                            <div>
                                                <a class="headerLink" href="secureIndex.action">Log in</a>
                                            </div>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="nav">
        <ul>
            <!-- Custome menu item for fast access to projects
            <li id="project_li"><a href="#">Project</a>
                <ul class="submenu">
                    // status page
                    <li><a href="productionStatus.action?projectNames=<projectName>&iss=true">ProjectName</a></li>
                    // display custom menubar with given project name
                    <li><a href="?p=<projectName>">ProjectName</a></li>
                </ul>
            </li>
            -->
            <li id="admin_li"><a href="#">Admin</a>
                <ul class="submenu">
                    <li><a href="projectSetup.action">Project Setup</a></li>
                    <!--<li><a href="metadataSetup.action?type=p">Project Metadata Setup</a></li>
                    <li><a href="metadataSetup.action?type=s">Sample Metadata Setup</a></li>-->
                    <li><a href="metadataSetup.action?type=e">Event Metadata Setup</a></li>
                </ul>
            </li>
            <li id="event_li"><a href="#">Project Events</a>
                <ul class="submenu">
                    <!--<li><a href="sampleLoader.action">Load Sample</a></li>-->
                    <li><a href="eventLoader.action">Load Event</a></li>
                </ul>
            </li>
            <li id="report_li"><a href="#">Report</a>
                <ul class="submenu">
                    <li><a href="eventDetail.action">Event Detail</a></li>
                    <li><a href="eventReport.action">Event Report</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <script src="scripts/jquery/jquery-1.7.2.js"></script>
    <script src="scripts/jquery/jquery-ui.js"></script>
    <script src="scripts/ometa.utils.js"></script>
    <script>
        <jsp:useBean id="userBean" class="org.jcvi.ometa.web_bean.UserInfoWebBean"/>
        <jsp:setProperty name="userBean" property="userId" value="<%=request.getRemoteUser()%>"/>

        $(document).ready(function() {
            $('div#nav ul li').mouseover(function() {
                $(this).find('ul:first').show();
            });

            $('div#nav ul li, div#nav ul li ul').mouseleave(function() {
                $('div#nav ul li ul').hide();
            });
            var userName='<jsp:getProperty name="userBean" property="fullname"/>',
                isAdmin='<jsp:getProperty name="userBean" property="admin"/>';
            _=(isAdmin!=null && isAdmin!=='null' && isAdmin==='true')?$('#admin_li').show():$('#admin_li').hide();
            if(userName!=null && userName!=='null') {
                $('div#currUserName').html(userName+', <a class="headerLink" href="logout.action">Log Out</a>');
                $('.noauthuser').hide();
            } else {
                $('.authuser').hide();
                $('.noauthuser').show();
            }
        });
    </script>
</body>
</html>