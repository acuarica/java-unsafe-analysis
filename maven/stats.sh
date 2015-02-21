
echo Repository size: `du -sh db/`
echo "#" of .jar files: `find db/ -name "*.jar" | wc -l`
echo "#" of .pom files: `find db/ -name "*.pom" | wc -l`
echo "#" of -sources.jar files: `find db/ -name "*-sources.jar" | wc -l`
