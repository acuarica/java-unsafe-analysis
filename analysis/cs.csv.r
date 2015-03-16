
library(reshape2)

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

csv = load.csv('csv/unsafe-maven.csv');
df = dcast(csv, className+name+groupId+artifactId~'cs', value.var='name', fun.aggregate=length);

df$methodName = NULL;
df$methodDesc = NULL;
df$owner = NULL;
df$desc = NULL;
df$version = NULL;
df$size = NULL;
df$ext = NULL;

df$id = factor(paste(df$groupId, df$artifactId, sep=':'));
df$className = as.character(df$className);

df$package <- NA;
df$classunit <- NA;

for (i in 1:nrow(df)) {
  fqn <- df$className[i];
  a <- strsplit(fqn, '/')[[1]];
  df$package[i] <- paste(a[1:length(a)-1], collapse='/');
  cls = a[length(a)];
  
  df$classunit[i] <- fcls(cls);
}

df$groupId = NULL;
df$artifactId = NULL;
df$className = NULL;

save.csv(df, outfile);
