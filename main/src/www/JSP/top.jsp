<style>
  #userNameDropdown {
    padding: 3px !important;
  }
</style>
<!-- top menu starts -->

<header id="header" class="header clearfix" role="banner">
  <div class="site-title"></div>

  <div class="inner-header container max-container">
    <div class="top-links clearfix" style="margin-top: 10px;float:left;">
      <a data-nav="home" href="/ometa/secureIndex.action">
        <img class="headerImage" src="images/ometa_logo.png" alt="Ontology based Metadata Tracking" />
      </a>
    </div>

    <div class="pull-right">
      <!-- collapse menu button -->
      <div id="hide-menu" class="btn-header pull-right visible-xs visible-sm">
        <span> <a href="javascript:void(0);" data-action="toggleMenu" title="Collapse Menu"><i class="fa fa-reorder"></i></a> </span>
      </div>
      <!-- end collapse menu -->

      <nav class="navbar visible-md visible-lg visible-xl" role="navigation">
        <ul class="nav navbar-nav" id="menu-the-main-menu" style="display:none;">
          <li id="menu-item-10" class="dropdown">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Data Submission <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action">Submit Data</a></li>
              <li><a href="eventDetail.action">Search and Edit Data</a></li>
              <li><a href="eventHistory.action">Event History</a></li>
              <li><a href="eventReport.action">Report</a></li>
            </ul>
          </li>
          <li id="admin_li" class="dropdown" style="display:none;">
            <a href="#" data-toggle="dropdown">Admin <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action?filter=pr">Project Registration</a></li>
              <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
              <li><a href="actorRole.action">User Management</a></li>
              <li><a href="projectManagement.action">Project Management</a></li>
            </ul>
          </li>
          <li id="menu-item-11" class="dropdown">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true"><span class="glyphicon glyphicon-user" style="margin-right:5px;background-color:#C2C2C2;padding:3px"></span><div class="HeaderLink" id="currUserName" style="display:inline-block"></div> <span class="caret"></span></span></a>
            <ul class="dropdown-menu" role="menu">
              <%--<li><a href="accountInformation.action">Account Information</a></li>--%>
              <li><a href="logout.action">Log Out</a></li>
            </ul>
          </li>
        </ul>
      </nav>
    </div>
  </div>

  <div id="header-border"></div>
</header>

<jsp:include page="include/i_admin.jsp" />
<script>
  $(document).ready(function() {
    $('#menu-the-main-menu li').mouseover(function() {
      $(this).find('ul:first').show();
    });
    $('#menu-the-main-menu li, #menu-the-main-menu li ul').mouseleave(function() {
      $('#menu-the-main-menu li ul').hide();
    });
  });
</script>