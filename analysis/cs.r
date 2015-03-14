
source('utils.r')

df.maven <- (function() {
  fcls <- function(cls) {    
    if (substr(cls, 1, 1) == '$') {
      cls <- substring(cls, 2);
    }
    fqnpath <- strsplit(cls, "\\$")[[1]];
    cls <- fqnpath[1];
    if ( grepl('[Immutable|Mutable|Updatable].*ParallelKV.*GO', cls) ) {
      cls <- '[Immutable|Mutable|Updatable]ParallelKVGO';
    }
    
    sprintf("%s*", cls);
  }
  
  csv.maven <- read.csv('unsafe-maven.csv', strip.white=TRUE, sep=',', header=TRUE);
  df.maven <- merge(csv.maven, df.methods, by.x="name", by.y="method", all.x=FALSE);
  df.maven$id <- factor(paste(df.maven$groupId, df.maven$artifactId, sep=':'));
  
  df.maven$package <- NA;
  df.maven$class = NA;
  df.maven$classunit <- NA;
  for (i in 1:nrow(df.maven)) {
    fqn <- as.character(df.maven$className[i]);
    a <- strsplit(fqn, '/')[[1]];
    df.maven$package[i] <- paste(a[1:length(a)-1], collapse='/');
    df.maven$class[i] <- a[length(a)];
    df.maven$classunit[i] <- fcls(df.maven$class[i]);
  }
  df.maven$class <- factor(df.maven$class);
  df.maven$fid <- factor(paste(df.maven$className, df.maven$id, sep='@'));  
  
  df.maven
})();

save.csv(df.maven, 'unsafe-maven-cs');
