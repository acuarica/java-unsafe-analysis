

mirrors="http://mirrors.ibiblio.org/maven2/ http://maven.antelink.com/content/repositories/central/ http://scalasbt.artifactoryonline.com/scalasbt/repo/ http://repo.jfrog.org/artifactory/simple/libs-release-bintray/"

while read line; do
	line=($line)
	path=${line[0]}
	sha1=${line[1]}

	for mirror in $mirrors; do
		echo -n -e "$mirror$path\t"
	done
	echo 
	echo "    out=$path"
#	echo "    checksum=sha-1=$sha1"
done < db/artifacts.list
