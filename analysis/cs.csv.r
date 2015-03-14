
source('utils.r')

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

df <- read.csv('csv/unsafe-maven.csv', strip.white=TRUE, sep=',', header=TRUE);

df$methodName = NULL;
df$methodDesc = NULL;
df$owner = NULL;
df$desc = NULL;
df$version = NULL;
df$size = NULL;
df$ext = NULL;

df <- merge(df, df.methods, by.x="name", by.y="method", all.x=FALSE);
df$id <- factor(paste(df$groupId, df$artifactId, sep=':'));

df$className <- as.character(df$className);

df$package <- NA;
#df$class = NA;
df$classunit <- NA;

for (i in 1:nrow(df)) {
  #fqn <- as.character(df$className[i]);
  fqn <- df$className[i];
  a <- strsplit(fqn, '/')[[1]];
  df$package[i] <- paste(a[1:length(a)-1], collapse='/');
  cls = a[length(a)];
  
  #df$class[i] <- cls;
  df$classunit[i] <- fcls(cls);
}

#df$class <- factor(df$class);
#df$fid <- factor(paste(df$className, df$id, sep='@'));  

df$groupId = NULL;
df$artifactId = NULL;
df$className = NULL;

save.csv(df, outfile);
