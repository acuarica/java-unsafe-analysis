
source config.sh

echo Database size: `du -sh $DB/`
echo "#" of .jar files: `find $DB/ -name "*.jar" | grep -v "\-sources\.jar" | wc -l`
echo "#" of .pom files: `find $DB/ -name "*.pom" | wc -l`
echo "#" of -sources.jar files: `find $DB/ -name "*-sources.jar" | wc -l`
