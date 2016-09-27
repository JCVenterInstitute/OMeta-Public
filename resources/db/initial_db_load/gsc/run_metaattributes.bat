set CP=..\..\..\dist\ProjectWebsitesApp.jar
set LOC=Y_pestis

java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\ProjectMetaAttributes.tsv
java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\SampleMetaAttributes.tsv
java -classpath %CP% org.jcvi.project_websites.engine.LoadingEngine -inputfile %LOC%\EventMetaAttributes.tsv