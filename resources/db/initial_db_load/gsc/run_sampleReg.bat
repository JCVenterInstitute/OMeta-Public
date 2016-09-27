REM set DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
set DBG=

set CP=..\..\..\dist\ProjectWebsitesApp.jar
set LOC=Y_pestis

java %DBG% -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\SampleRegistration_EventAttributes.tsv