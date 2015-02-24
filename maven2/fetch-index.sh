
source config.sh

mkdir -p $DB/index

curl http://mirrors.ibiblio.org/maven2/.index/nexus-maven-repository-index.gz -o $DB/index/nexus-maven-repository-index.gz
gunzip $DB/index/nexus-maven-repository-index.gz
