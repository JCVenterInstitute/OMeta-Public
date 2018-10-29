#!/bin/bash
#DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
DBG=
URL=jnp://limsdev5.jcvi.org:1299
DEPLOY=val
JLOC=/Users/hkim/workspace/Workspace/INTELLIJ/projectWebsites/hkim/dist
JAVA=/usr/bin/java
$JAVA $DBG -classpath $JLOC/ProjectWebsitesApp.jar org.jcvi.project_websites.engine.LoadingEngine -inputfile $1 -serverUrl $URL
