@ECHO off
REM set DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
set DBG=
set SRVR=limsdev2.jcvi.org:1399
set USER=lfoster
set JLOC=c:\current_projects\svnfiles\ProjectWebsites\dist
java %DBG% -classpath %JLOC%\ProjectWebsitesApp.jar org.jcvi.project_websites.engine.LoadingEngine -username %USER% -serverUrl %SRVR% -multipartInputfile %1%