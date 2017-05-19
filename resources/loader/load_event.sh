#!/bin/bash
if [ "$2" == "" ]
then
  echo USAGE: $0 inputfile eventType
  exit 0
fi
#DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
DBG=
URL=jnp://limsdev5.jcvi.org:1299
DBE=development
EVT_TYPE=$2
echo $EVT_TYPE
DEPLOY=val
JLOC=/Users/hkim/workspace/Workspace/INTELLIJ/projectWebsites/hkim/dist
JAVA=/usr/bin/java
$JAVA $DBG -classpath $JLOC/ProjectWebsitesApp.jar org.jcvi.project_websites.engine.LoadingEngine -inputfile $1 -eventType "$EVT_TYPE" -serverUrl $URL
