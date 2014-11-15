
<!-- top menu starts -->

<header id="header" class="header clearfix" role="banner">
  <div class="site-title">
    <h1 style="margin-left:20px;">Centers of Excellence for Influenza Research and Surveillance</h1>
  </div>

  <div class="top-links container">
    <div class="pull-right">
      <div class="HeaderLink" id="currUserName" display="none"></div>
      <div class="noauthuser"><a href="secureIndex.action">CEIRS Member Login</a></div>
    </div>
  </div>

  <div class="inner-header">
    <div id="logo" class="h1"><a href="secureIndex.action" rel="nofollow"></a></div>

    <div class="pull-right">
      <!-- <div style="min-height:50px;line-height:50px" class="pull-right authuser">
        <div id="currUserName" style="color:#fff">Welcome, Username</div>
      </div> -->
  
      <!-- collapse menu button -->
      <div id="hide-menu" class="btn-header pull-right visible-xs visible-sm visible-md">
        <span> <a href="javascript:void(0);" data-action="toggleMenu" title="Collapse Menu"><i class="fa fa-reorder"></i></a> </span>
      </div>
      <!-- end collapse menu -->
    
      <!-- #MOBILE -->            
      <nav class="navbar visible-lg" role="navigation">
        <ul id="menu-the-main-menu" class="nav navbar-nav">
          <li id="menu-item-9" class="dropdown">
            <a href="/dpcc/reports" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Reports &amp; Analytics <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="centerreport.action">CEIRS Center Reports</a></li>
              <li><a href="webanalytic.action">Web Analytics</a></li>
            </ul>
          </li>
          <li id="menu-item-10" class="dropdown">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Data Submission <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action?filter=sr">Submit Data</a></li>
              <li><a href="eventDetail.action">Search and Edit Data</a></li>
              <li><a href="eventHistory.action">Event History</a></li>
              <li><a href="eventReport.action">Report</a></li>
            </ul>
          </li>
          <li id="admin_li" class="dropdown">
            <a href="#" data-toggle="dropdown">Admin<span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action?filter=pr">Project Registration</a></li>
              <li><a href="projectSetup.action">Project Setup</a></li>
              <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
              <li><a href="actorRole.action">User/Role</a></li>
            </ul>
          </li>
          <li id="menu-item-11" class="dropdown">
            <a href="/dpcc/help/" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Help <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="support.action">Support</a></li>
              <li><a href="#">Knowledgebase</a></li>
              <li><a href="#">Training</a></li>
            </ul>
          </li>
          <!-- <li id="menu-item-187"><a href="#">Support</a></li> -->
          <li style="margin-right: 90px;"></li>
        </ul>
      </nav>
      <!-- <nav class="navbar visible-lg" role="navigation">
        <ul id="menu-the-main-menu" class="nav navbar-nav">
          <li id="menu-item-9" class="dropdown">
            <a href="#" data-toggle="dropdown">Data Statistics <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="#">DPCC Overall</a></li>
              <li><a href="#">Centers Specific</a></li>
              <li><a href="#">Web Traffic</a></li>
            </ul>
          </li>
          <li id="menu-item-11" class="dropdown">
            <a href="#" data-toggle="dropdown">Data Submission<span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action?filter=pr">Project Registration</a></li>
              <li><a href="eventLoader.action?filter=sr">Submit Data</a></li>
              <li><a href="eventDetail.action">Search and Edit Data</a></li>
              <li><a href="eventHistory.action">Event History</a></li>
              <li><a href="eventReport.action">Report</a></li>
            </ul>
          </li>
          <li id="admin_li" class="dropdown">
            <a href="#" data-toggle="dropdown">Admin<span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="projectSetup.action">New Project</a></li>
              <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
              <li><a href="actorRole.action">User/Role</a></li>
            </ul>
          </li>
          <li id="menu-item-187"><a href="#">Support</a></li>
        </ul>
      </nav> -->

    </div>
  </div>

</header>

<aside id="left-panel">
  <nav role="navigation">
    <ul id="menu-the-main-menu-1" class="nav top-nav">
      <li id="menu-item-9" class="dropdown">
        <a href="#" data-toggle="dropdown">Data Statistics <span class="caret"></span></a>
        <ul class="dropdown-menu" role="menu">
          <li><a href="#">DPCC Overall</a></li>
          <li><a href="#">Centers Specific</a></li>
          <li><a href="#">Web Traffic</a></li>
        </ul>
      </li>
      <li id="menu-item-188"><a href="#">Data Submission</a></li>
      <li id="menu-item-187"><a href="#">Support</a></li>
    </ul>
  </nav>
</aside>

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
    (isAdmin!=null && isAdmin!=='null' && isAdmin==='true')?$('#admin_li').show():$('#admin_li').hide();
    if(userName!=null && userName!=='null') {
      $('div#currUserName').html('<font color="#b6cad9">' + userName + '</font>&nbsp;&nbsp;<a class="headerLink" href="logout.action">Log Out</a>');
      $('.noauthuser').hide();
    } else {
      $('.authuser').hide();
      $('.noauthuser').show();
    }
  });
</script>