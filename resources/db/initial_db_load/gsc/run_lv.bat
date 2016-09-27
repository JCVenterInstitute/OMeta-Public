REM
REM   Loading all lookup values.
REM 

set CP=..\..\..\dist\ProjectWebsitesApp.jar
set LOC=Y_pestis

java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\EventAttribute_LookupValues.tsv
java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\EventType_LookupValues.tsv
java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\ProjectRegistration_LookupValues.tsv
java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\SampleRegistration_LookupValues.tsv
