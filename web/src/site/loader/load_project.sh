#!/bin/bash
if [ "$1" == "" ]
then
    echo USAGE: load_project.sh \<directory-location\>
    echo     directory-location has one or more \*_project.tsv and \*_sample.tsv files.
    exit 0
fi
#DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
DBG=
DEPLOY=val
SRVR=jnp://limsdev5.jcvi.org:1299
JAVA=/usr/bin/java
JLOC=/Users/hkim/workspace/Workspace/INTELLIJ/projectWebsites/hkim/dist
$JAVA $DBG -classpath $JLOC/ProjectWebsitesApp.jar org.jcvi.project_websites.utils.ProjectLoader $1 $SRVR
