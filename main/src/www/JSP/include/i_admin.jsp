
<jsp:useBean id="userBean" class="org.jcvi.ometa.web_bean.UserInfoWebBean"/>
<jsp:setProperty name="userBean" property="userId" value="<%=request.getRemoteUser()%>"/>

<script>
var userName='<jsp:getProperty name="userBean" property="fullname"/>',
    isAdmin='<jsp:getProperty name="userBean" property="admin"/>';
(isAdmin!=null && isAdmin!=='null' && isAdmin==='true')?$('#admin_li').show():$('#admin_li').hide();

if (userName!=null && userName!=='null') {
  
  $('#currUserName').text(userName);
  $('#userNameDropdown').show();
  $('.noauthuser').hide();
  $('#currUserName, #menu-the-main-menu-1, #menu-the-main-menu').show();

} else {
  
	$('.noauthuser').show();
	$('#currUserName, #menu-the-main-menu-1, #menu-the-main-menu').hide();

	if ( /secureIndex/ig.test(document.location) ) {

		$('a[data-nav="home"]').click(function(e) {
			e.preventDefault();
			document.location = 'http://limsdev3.jcvi.org:8380/ometa';
		});

	}

}
</script>