#!/bin/bash
#DBG="-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5555"
DBG=
SRVR=jnp://limsdev5.jcvi.org:1299
DEPLOY=val
JAVA=/usr/bin/java
JLOC=/Users/hkim/workspace/Workspace/INTELLIJ/projectWebsites/hkim/dist
JB=/local/devel/JTC/jboss-5.1.0.GA
CP=${JB}/lib/*:${JB}/client/*:${JB}/server/default/lib/*:${JLOC}/ProjectWebsitesApp.jar
$JAVA $DBG -classpath ${CP} org.jcvi.project_websites.engine.LoadingEngine -multipartInputfile $1 -serverUrl $SRVR

# java -cp %JBOSS_HOME%\lib\*;%JBOSS_HOME%\client\*;%JBOSS_HOME%\server\default\lib\*;.
