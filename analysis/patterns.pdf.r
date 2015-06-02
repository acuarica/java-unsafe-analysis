
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('analysis/utils.r')

csv.comments = load.csv('analysis/comments.csv');

patterns = unique(strsplit(paste(levels(csv.comments$patterns), collapse='&'), split='&')[[1]]);
patterns = patterns[2:length(patterns)];

df.maven = load.csv('out/cs.csv');
df.deps = load.csv('out/deps-prod.csv');
df.cu = dcast(df.maven, id+package+classunit~name, value.var='cs', fun.aggregate=sum);
df.clones = dcast(df.cu, classunit~'clones', value.var='id', fun.aggregate=length);

#levs = function(col) {
#  levels(factor(col));
#}

depsbyarts = function(arts) {
  length(unique(df.deps[df.deps$depId %in% arts,]$depCount)) + length(arts);
}

mostdeps = function(arts, n) {
  df = df.deps[df.deps$depId %in% arts,];
  df = dcast(df, depId~'value', value.var='depCount', fun.aggregate=length);
  df = df[order(-df$value),];
  
  df$depId[1:n];
  #as.character(paste(df$depId[1:10], collapse='\n') );
  #paste( c(as.character(df$depId), as.character(arts))[1:10], collapse='\n') ;
}

garts = list();

patterns = patterns[patterns != '.impl'];
for (i in 1:length(patterns)) {
  pattern = patterns[i];
  arts = csv.comments[grepl(pattern, csv.comments$pattern),]$id;
  garts[[length(garts)+1]] = list(pattern, arts);
}

garts[[length(garts)+1]] = (function() {
  ms = as.character(subset(df.methods, group %in% c('Fence', 'Volatile Get', 'Volatile Put'))$method);
  list('fence', unique(subset(df.maven, name %in% ms)$id));
})();

#dcast(df.maven, id~name, 

garts[[length(garts)+1]] = list('park2', unique(subset(df.maven, name %in% c('park', 'unpark'))$id));
garts[[length(garts)+1]] = list('monitor2', unique(subset(df.maven, name %in% c('monitorEnter', 'monitorExit'))$id));
garts[[length(garts)+1]] = list('class2', unique(subset(df.maven, name %in% c('defineClass'))$id));
garts[[length(garts)+1]] = list('throw2', unique(subset(df.maven, name %in% c('throwException'))$id));
garts[[length(garts)+1]] = list('page2', unique(subset(df.maven, name %in% c('pageSize'))$id));
garts[[length(garts)+1]] = list('alloc2', unique(subset(df.maven, name %in% c('allocateInstance'))$id));

rows = list();
txt = '';

for (i in 1:length(garts)) {
  pattern = garts[[i]][[1]];
  arts = garts[[i]][[2]];

  len = length(arts);
  deps = depsbyarts(arts);
  most = mostdeps(arts, 40);
  
  mosttext = as.character(paste(most, collapse='\n') );
  rows[[ length(rows)+1 ]] = list(pattern, len, deps, mosttext);
  
  txt = sprintf('%s\\newcommand{\\%slen}{%s}\n', txt, pattern, len);
  txt = sprintf('%s\\newcommand{\\%sdeps}{%s}\n', txt, pattern, deps);
  txt = sprintf('%s\\newcommand{\\%smost}{%s}\n', txt, pattern, mosttext);
  txt = sprintf('%s\n', txt);
}

f = file('out/patterns-stats.tex');
writeLines(txt, f);
close(f);

df = data.frame(matrix(unlist(rows), ncol=4, byrow=T));
colnames(df) = c('pattern', 'artifact.count', 'usedby.count', 'most.deps');
df$usedby.count = as.numeric(as.character(df$usedby.count));
df = df[order(-df$usedby.count),];
rownames(df) = 1:length(rows);

save.plot.open(outfile, w=10, h=10);

i = 1;
for (i in 1:nrow(df)) {
  grid.newpage();
  grid.table(df[i,]);
}

save.plot.close();
