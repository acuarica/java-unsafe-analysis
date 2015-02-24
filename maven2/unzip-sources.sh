
source config.sh

find $DB -name "*-sources.jar" -exec unzip -o {} -d $DB/sources \;
