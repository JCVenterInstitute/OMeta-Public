REM set DBG=-Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
set DBG=
set JLOC=c:\current_projects\svnfiles\ProjectWebsites\dist
java %DBG% -classpath %JLOC%\ProjectWebsitesApp.jar org.jcvi.project_websites.utils.ProjectLoader %1 development