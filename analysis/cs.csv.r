
library(reshape2)

source('analysis/utils.r')

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

replacename = function(df, name, offheapdesc) {
  if (nrow(df[df$name == name & df$desc==offheapdesc,]) > 0) {
    df[df$name == name & df$desc==offheapdesc,]$name = sprintf('%s%s', name, ' (Off-Heap)');
  }
  df[df$name == name,]$name = sprintf('%s%s', name, ' (Heap)');
  
  df;
}

csv = load.csv('out/unsafe-maven.csv');
csv = filterlang(csv);


df = csv;
df$name = as.character(df$name);
df = replacename(df, 'copyMemory', '(JJJ)V');
df = replacename(df, 'setMemory', '(JJB)V');
#df = replacename(df, 'getBoolean', '(J)B');
df = replacename(df, 'getByte', '(J)B');
df = replacename(df, 'getChar', '(J)C');
df = replacename(df, 'getDouble', '(J)D');
df = replacename(df, 'getFloat', '(J)F');
df = replacename(df, 'getInt', '(J)I');
df = replacename(df, 'getLong', '(J)J');
df = replacename(df, 'getShort', '(J)S');
#df = replacename(df, 'putBoolean', '(JB)V');
df = replacename(df, 'putByte', '(JB)V');
df = replacename(df, 'putChar', '(JC)V');
df = replacename(df, 'putDouble', '(JD)V');
df = replacename(df, 'putFloat', '(JF)V');
df = replacename(df, 'putInt', '(JI)V');
df = replacename(df, 'putLong', '(JJ)V');
df = replacename(df, 'putShort', '(JS)V');

df = dcast(df, className+name+groupId+artifactId~'cs', value.var='name', fun.aggregate=length);

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
#df$className = NULL;

save.csv(df, outfile);
