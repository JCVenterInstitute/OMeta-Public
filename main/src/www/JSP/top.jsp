
<!-- top menu starts -->

<header id="header" class="header clearfix" role="banner">
  <div class="site-title">
    <h1>Centers of Excellence for Influenza Research and Surveillance</h1>
  </div>

  <div class="top-links container">
    <div class="pull-right">
      <div class="HeaderLink" id="currUserName" display="none"></div>
      <div class="noauthuser"><a href="secureIndex.action">CEIRS Member Login</a></div>
    </div>
  </div>

  <div class="inner-header container">
    <div id="logo" class="h1"><a href="/" rel="nofollow"></a></div>

    <div class="pull-right">
  
      <!-- collapse menu button -->
      <div id="hide-menu" class="btn-header pull-right visible-xs visible-sm visible-md">
        <span> <a href="javascript:void(0);" data-action="toggleMenu" title="Collapse Menu"><i class="fa fa-reorder"></i></a> </span>
      </div>
      <!-- end collapse menu -->
    
      <!-- #MOBILE -->            

      <nav class="navbar visible-lg" role="navigation">
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
              <li><a href="eventLoader.action">Submit/Edit Data</a></li>
              <li><a href="eventDetail.action">View Data</a></li>
              <li><a href="eventReport.action">Report</a></li>
            </ul>
          </li>
          <li id="menu-item-10" class="dropdown">
            <a href="#" data-toggle="dropdown">Admin<span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="projectSetup.action">Create Project</a></li>
              <li><a href="metadataSetup.action?type=e">Metadata Setup</a></li>
              <li><a href="actorRole.action">User/Role</a></li>
            </ul>
          </li>
          <li id="menu-item-187"><a href="#">Support</a></li>
        </ul>
      </nav>

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