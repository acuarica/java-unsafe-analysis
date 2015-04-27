
CURL=curl
DB=db
NEXUSINDEX=nexus-maven-repository-index
NEXUSINDEXGZ=$(NEXUSINDEX).gz

.PHONY: all stats

all: $(DB)/$(NEXUSINDEX)

$(DB)/index.list:
	python nexus.py

$(DB)/$(NEXUSINDEX): $(DB)/$(NEXUSINDEXGZ)
	gunzip $<

$(DB)/$(NEXUSINDEXGZ): | $(DB)
	$(CURL) http://mirrors.ibiblio.org/maven2/.index/$(NEXUSINDEXGZ) -o $(DB)/$(NEXUSINDEXGZ)

$(DB):
	mkdir -p $(DB)

stats:
	echo Database size: `du -sh $(DB)/`
	echo "#" of .jar files: `find $(DB)/ -name "*.jar" | grep -v "\-sources\.jar" | wc -l`
	echo "#" of .pom files: `find $(DB)/ -name "*.pom" | wc -l`
	echo "#" of -sources.jar files: `find $(DB)/ -name "*-sources.jar" | wc -l`
