#!/bin/bash
# FILE: make_template.sh
# AUTHOR: lfoster
# DESCRIPTION:  Will take the parameters as constraints to produce a template file for creating a loadable event file.
#     To use this, give the name of the event type, the name of the project, and an optional sample name to the command
#     line.  Once it runs, the full path to the template will echo to stdout.  Edit that and re-push it as an event.
# CAUTION: if the project name, sample name, or event name has spaces within it, please use double-quotes around it on the command line.
#
if [ "$2" == "" ]
then
  echo USAGE: $0 \"eventType\" \"projectName\" \[\"sampleName\"\]
  exit 0
fi


#DBG="-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
DBG=
URL=jnp://limsdev5.jcvi.org:1299
OUTLOC="."
DEPLOY=val
JLOC=/Users/hkim/workspace/Workspace/INTELLIJ/projectWebsites/hkim/dist
JAVA=/usr/bin/java

SAMP=
if [ "$3" != "" ]
then
  SAMP="$3"
fi
PROJ="$2"
EVT="$1"
# DEBUG echo Project is $PROJ .
if [ "$SAMP"=="" ]
then
$JAVA $DBG -classpath $JLOC/ProjectWebsitesApp.jar org.jcvi.project_websites.engine.LoadingEngine -outputLocation $OUTLOC -serverUrl $URL -makeEvent -projectName "$PROJ" -templateEventName "$EVT" -sampleName "$SAMP"
else
$JAVA $DBG -classpath $JLOC/ProjectWebsitesApp.jar org.jcvi.project_websites.engine.LoadingEngine -outputLocation $OUTLOC -serverUrl $URL -makeEvent -projectName "$PROJ" -templateEventName "$EVT"
fi

