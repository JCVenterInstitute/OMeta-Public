
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<style>
  #userNameDropdown {
    padding: 3px !important;
  }
  .navbar-default .navbar-nav > li > a{color:#3276b1 !important;}
</style>
<!-- top menu starts -->

<header id="header" class="header clearfix" role="banner">
  <div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="site-title"></div>
    <div class="inner-header container max-container">
      <div class="top-links clearfix" style="float:left;">
        <a data-nav="home" href="<c:url value='/secureIndex.action' />">
          <img class="headerImage" src="<c:url value='/images/ometa_logo.png' />" alt="Ontology based Metadata Tracking" style="height: 45px;">
        </a>
      </div>
      <div class="navbar-header">
        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
      </div>
      <div class="navbar-collapse collapse" style="height: 0px;">
        <ul class="nav navbar-nav navbar-right" id="menu-the-main-menu">
          <li><a href="secureIndex.action">Dashboard</a></li>
          <li><a href="eventLoader.action">Submit Data</a></li>
          <li><a href="eventDetail.action">Search and Edit Data</a></li>
          <li><a href="eventHistory.action">Event History</a></li>
          <li><a href="eventReport.action">Report</a></li>
          <li><a href="dictionary.action">Dictionary</a></li>
          <li class="dropdown" id="admin_li">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true">Admin <span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="eventLoader.action?filter=pr">Project Registration</a></li>
              <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
              <li><a href="actorRole.action">User Management</a></li>
              <li><a href="projectManagement.action">Project Management</a></li>
              <li><a href="dictionaryManagement.action">Dictionary Management</a></li>
              <li><a href="jsonManagement.action">Json Management</a></li>
            </ul>
          </li>
          <li class="dropdown">
            <a href="#" data-toggle="dropdown" class="dropdown-toggle" aria-haspopup="true"><i class="fa fa-user" style="margin-right: 5px;color: #3276b19e;"></i><div class="HeaderLink" id="currUserName" style="display:inline-block"></div><span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="logout.action"><span class="glyphicon glyphicon-log-out" style="margin-right:5px;padding:3px"></span>Log Out</a></li>
            </ul>
          </li>
        </ul>
      </div><!--/.nav-collapse-->
    </div>
  </div>
</header>

<jsp:include page="include/i_admin.jsp" />
<script>
  $(document).ready(function() {
    function toggleNavbarMethod() {
      if (!isTouchDevice() && $(window).width() > 768) {
        $('.navbar .dropdown').on('mouseover', function(){
          $(this).find('ul:first').show();
        });
        $('.navbar .dropdown, .navbar .dropdown ul').on('mouseout', function(){
          $('.dropdown ul').hide();
        });
      } else {
        $('.navbar .dropdown').off('mouseover').off('mouseout');
      }
    }

    // toggle navbar hover
    toggleNavbarMethod();

    // bind resize event
    $(window).resize(toggleNavbarMethod);
  });

  function isTouchDevice() {
    return window.ontouchstart !== undefined;
  }

</script>