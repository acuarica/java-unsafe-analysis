
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

csv.comments = load.csv('csv/comments.csv');

patterns = unique(strsplit(paste(levels(csv.comments$patterns), collapse='&'), split='&')[[1]]);
patterns = patterns[2:length(patterns)];

df.maven = load.csv('build/cs.csv');
df.deps = load.csv('build/deps-prod.csv');
df.cu = dcast(df.maven, id+package+classunit~name, value.var='cs', fun.aggregate=sum);
df.clones = dcast(df.cu, classunit~'clones', value.var='id', fun.aggregate=length);

#levs = function(col) {
#  levels(factor(col));
#}

depsbyarts = function(arts) {
  length(unique(df.deps[df.deps$depId %in% arts,]$depCount)) + length(arts);
}

mostdeps = function(arts) {
  df = df.deps[df.deps$depId %in% arts,];
  df = dcast(df, depId~'value', value.var='depCount', fun.aggregate=length);
  df = df[order(-df$value),];
  
  df$depId[1:10];
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
  gs = c('Fence', 'Volatile Get', 'Volatile Put');
  ms = as.character(subset(df.methods, group %in% gs)$method);
  arts = unique(subset(df.maven, name %in% ms)$id);
  list('fence', arts);
})();

garts[[length(garts)+1]] = (function() {
  arts = unique(subset(df.maven, name %in% c('park', 'unpark'))$id);
  list('park2', arts);
})();

garts[[length(garts)+1]] = (function() {
  arts = unique(subset(df.maven, name %in% c('monitorEnter', 'monitorExit'))$id);
  list('monitor2', arts);
})();

rows = list();
txt = '';

for (i in 1:length(garts)) {
  pattern = garts[[i]][[1]];
  arts = garts[[i]][[2]];

  len = length(arts);
  deps = depsbyarts(arts);
  most = mostdeps(arts);
  
  mosttext = as.character(paste(most, collapse='\n') );
  rows[[ length(rows)+1 ]] = list(pattern, len, deps, mosttext);
  
  txt = sprintf('%s\\newcommand{\\%slen}{%s}\n', txt, pattern, len);
  txt = sprintf('%s\\newcommand{\\%sdeps}{%s}\n', txt, pattern, deps);
  txt = sprintf('%s\\newcommand{\\%smost}{%s}\n', txt, pattern, mosttext);
  txt = sprintf('%s\n', txt);
}

f = file('build/patterns-stats.tex');
writeLines(txt, f);
close(f);

df = data.frame(matrix(unlist(rows), ncol=4, byrow=T));
colnames(df) = c('pattern', 'artifact.count', 'usedby.count', 'most.deps');
df$usedby.count = as.numeric(as.character(df$usedby.count));
df = df[order(-df$usedby.count),];
rownames(df) = 1:length(rows);

save.plot.open(outfile, w=10, h=length(rows)*2.5);
grid.newpage();
grid.table(df);
save.plot.close();
