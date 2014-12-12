
<!-- top menu starts -->

<header id="header" class="header clearfix" role="banner">

  <div class="site-title"></div>
  
  <div class="inner-header container max-container">
    
    <h1>Data Processing and Coordinating Center</h1>

    <div class="top-links clearfix">
      <div class="pull-right">
        <div class="user-info">
          <button style="display:none;background:transparent;color:white" id="userNameDropdown" class="btn dropdown-toggle" type="button" data-toggle="dropdown" style="color:white;background:transparent"><span class="glyphicon glyphicon-user" style="margin-right:5px;background-color:#C2C2C2;padding:3px"></span><div class="HeaderLink" id="currUserName" style="display:inline-block"></div> <span class="caret"></span></button>
          <ul class="dropdown-menu" role="menu" aria-labelledby="userNameDropdown" style="left:auto;right:0;top:30px">
            <li><a href="accountInformation.action">Account Information</a></li>
            <li><a href="logout.action">Log Out</a></li>
          </ul>
        </div>
        <div class="noauthuser"><a href="secureIndex.action">CEIRS Member Login</a></div>
      </div>
    </div>

    <div id="logo" class="h1"><a href="http://www.niaidceirs.org/" rel="nofollow"></a></div>

    <div class="pull-right">

      <!-- collapse menu button -->
      <div id="hide-menu" class="btn-header pull-right visible-xs visible-sm">
        <span> <a href="javascript:void(0);" data-action="toggleMenu" title="Collapse Menu"><i class="fa fa-reorder"></i></a> </span>
      </div>
      <!-- end collapse menu -->

      <nav class="navbar visible-md visible-lg visible-xl" role="navigation">
        <ul class="nav navbar-nav" id="menu-the-main-menu" style="display:none;">
          <li id="menu-item-9" class="dropdown">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Reports &amp; Analytics <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="centerreport.action">CEIRS Center Reports</a></li>
              <li><a href="analytics.action">Web Analytics</a></li>
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
          <li id="admin_li" class="dropdown" style="display:none;">
            <a href="#" data-toggle="dropdown">Admin <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action?filter=pr">Project Registration</a></li>
              <li><a href="projectSetup.action">Project Setup</a></li>
              <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
              <li><a href="actorRole.action">User/Role</a></li>
            </ul>
          </li>
          <li id="menu-item-11" class="dropdown last">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Help <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="dpcc_help.action">Contact Us</a></li>
              <li><a href="support.action">Request Support</a></li>
             <!--  <li><a href="dpcc_help.action">Knowledge Base</a></li>
              <li><a href="dpcc_help.action">Training</a></li> -->
            </ul>
          </li>
        </ul>
      </nav>
      
    </div>
  </div>

</header>

<aside id="left-panel">
  <nav role="navigation">
    <ul id="menu-the-main-menu-1" class="nav mobile-nav">
      <li id="menu-item-9" class="dropdown">
        <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Reports &amp; Analytics</a>
        <ul class="sub-menu" role="menu">
          <li><a href="centerreport.action">CEIRS Center Reports</a></li>
          <li><a href="analytics.action">Web Analytics</a></li>
        </ul>
      </li>
      <li id="menu-item-10" class="dropdown">
        <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Data Submission</a>
        <ul class="sub-menu" role="menu">
          <li><a href="eventLoader.action?filter=sr">Submit Data</a></li>
          <li><a href="eventDetail.action">Search and Edit Data</a></li>
          <li><a href="eventHistory.action">Event History</a></li>
          <li><a href="eventReport.action">Report</a></li>
        </ul>
      </li>
      <li id="admin_li" class="dropdown" style="display:hidden;">
        <a href="#" data-toggle="dropdown">Admin</a>
        <ul class="sub-menu" role="menu">
          <li><a href="eventLoader.action?filter=pr">Project Registration</a></li>
          <li><a href="projectSetup.action">Project Setup</a></li>
          <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
          <li><a href="actorRole.action">User/Role</a></li>
        </ul>
      </li>
      <li id="menu-item-11" class="dropdown last">
        <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Help</a>
        <ul class="sub-menu" role="menu">
          <li><a href="dpcc_help.action">Contact Us</a></li>
          <li><a href="support.action">Request Support</a></li>
         <!--  <li><a href="dpcc_help.action">Knowledge Base</a></li>
          <li><a href="dpcc_help.action">Training</a></li> -->
        </ul>
      </li>
    </ul>
  </nav>
</aside>

<jsp:include page="include/i_admin.jsp" />
<script>
  $(document).ready(function() {
    $('div#nav ul li').mouseover(function() {
      $(this).find('ul:first').show();
    });
    $('div#nav ul li, div#nav ul li ul').mouseleave(function() {
      $('div#nav ul li ul').hide();
    });
  });
</script>