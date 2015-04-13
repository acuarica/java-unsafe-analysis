
source config.sh

mkdir -p $DB/index

curl http://mirrors.ibiblio.org/maven2/.index/nexus-maven-repository-index.gz -o $DB/nexus-maven-repository-index.gz
gunzip $DB/nexus-maven-repository-index.gz
