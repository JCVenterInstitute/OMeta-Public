NOTES ON DEPLOYMENT:
====================

jboss-log4j: goes into ${base.server.location}/server/servername/conf
   where "servername" is the name of your server.  For example, dev-lfoster-projectwebsites-singleton. Please change servername
   to the path/name of your own server.

   This one is a standardized log4j config.

pws_appdbinterface_MySQL-ds: goes into ${base.server.location}/server/servername/deploy/. Please change
   servername to the name of your own server. For example, dev-lfoster-projectwebsites-singleton.

   This one is a standardized JBoss style Data Source config.