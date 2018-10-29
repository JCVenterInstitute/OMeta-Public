<%@ page import = "java.util.Properties" %>
<%@ page import = "org.jtc.common.util.property.PropertyHelper" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page session="true" %>

<!--[if lt IE 7]><html lang="en-US" class="no-js lt-ie9 lt-ie8 lt-ie7"><![endif]-->
<!--[if (IE 7)&!(IEMobile)]><html lang="en-US" class="no-js lt-ie9 lt-ie8"><![endif]-->
<!--[if (IE 8)&!(IEMobile)]><html lang="en-US" class="no-js lt-ie9"><![endif]-->
<!--[if gt IE 8]><!--> <html lang="en-US" class="no-js public-site"><!--<![endif]-->

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>O-META | Ontologies Based Metadata Tracking Application</title>

<meta name="HandheldFriendly" content="True">
<meta name="MobileOptimized" content="320">
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>

<link rel="icon" href="<c:url value="/images/ometa_icon.png" />">
<!--[if IE]>
<link rel="shortcut icon" href="<c:url value="/images/ometa_icon.png" />">
<![endif]-->
<meta name="msapplication-TileColor" content="#f01d4f">

<meta name='robots' content='noindex,follow' />

<link rel='stylesheet' id='open-sans-css'  href='https://fonts.googleapis.com/css?family=Open+Sans%3A300italic%2C400italic%2C600italic%2C300%2C400%2C600&#038;subset=latin%2Clatin-ext' type='text/css' media='all' />
<link rel='stylesheet' id='google-open-sans-css'  href='https://fonts.googleapis.com/css?family=Open+Sans%3A400italic%2C700italic%2C300%2C400%2C700' type='text/css' media='screen' />
<link rel='stylesheet' id='google-lato-css'  href='https://fonts.googleapis.com/css?family=Lato&#038;subset=latin%2Clatin-ext' type='text/css' media='screen' />
<link rel='stylesheet' id='ometa-bootstrap-stylesheet-css'  href="<c:url value="/style/bootstrap.css" />" type='text/css' media='all' />
<link rel='stylesheet' id='ometa-font-awesome-stylesheet-css'  href="<c:url value="/style/font-awesome.css" />" type='text/css' media='all' />
<!--[if lt IE 9]>
<link rel='stylesheet' id='bones-ie-only-css'  href='css/ie.css' type='text/css' media='all' />
<![endif]-->
<link rel='stylesheet' id='googleFonts-css'  href='https://fonts.googleapis.com/css?family=Lato%3A400%2C700%2C400italic%2C700italic' type='text/css' media='all' />

<script id="modernizr" src="<c:url value="/scripts/modernizr.custom.min.js" />"></script>

<script src="<c:url value="/scripts/jquery/jquery-1.7.2.js" />"></script>
<script src="<c:url value="/scripts/jquery/jquery-ui.js" />"></script>

<!--JS to be replaced by min version-->
<script src="<c:url value="/scripts/ometa.utils.js" />"></script>
<script type='text/javascript' src="<c:url value="/scripts/bootstrap.js" />"></script>
<!--end-->
<!--[if lt IE 9]>
<script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js'></script>
<![endif]-->