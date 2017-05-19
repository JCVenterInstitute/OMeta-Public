@ECHO off
REM set DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
set DBG=
set DBE=limsdev5.jcvi.org:1299
set JLOC=c:\current_projects\svnfiles\ProjectWebsites\dist
set U=hkim
java %DBG% -classpath %JLOC%\ProjectWebsitesApp.jar org.jcvi.project_websites.engine.LoadingEngine -username %U% -serverUrl %DBE% -inputfile %1%