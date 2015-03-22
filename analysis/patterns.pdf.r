
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

levs = function(col) {
  levels(factor(col));
}

depsbyarts = function(arts) {
  length(levs(df.deps[df.deps$depId %in% arts,]$depCount));
}

deps = function(classes) {
  unsart = levs(df.maven[df.maven$classunit %in% classes, ]$id);
  count = length(levs(df.deps[df.deps$depId %in% unsart,]$depCount));
  count;
}

clones = function(classes) {
  df = df.clones[df.clones$classunit %in% classes, ];
  sum(df$clones);
}

mostdeps = function(arts) {
  df = df.deps[df.deps$depId %in% arts,];
  df = dcast(df, depId~'value', value.var='depCount', fun.aggregate=length);
  df = df[order(-df$value),];
  as.character(df$depId[1]);
}

row = function(name, classes) {
  list(name, length(classes), clones(classes), deps(classes));
}

rows = list();

i = 1;
rid = 1;
for (i in 1:length(patterns)) {
  pattern = patterns[i];
  
  if (substr(pattern, 1, 1) != '.') {
    arts = csv.comments[grepl(pattern, csv.comments$pattern),]$id
    rows[[rid]] = list(pattern, length(arts), depsbyarts(arts), mostdeps(arts));
    rid = rid + 1;
  }
}

df = data.frame(matrix(unlist(rows), ncol=4, byrow=T));
colnames(df) = c('pattern', 'artifact.count', 'usedby.count', 'most.deps');
df$usedby.count = as.numeric(as.character(df$usedby.count));
df = df[order(-df$usedby.count),];
rownames(df) = 1:(rid-1);

save.plot.open(outfile, w=7, h=5);
grid.newpage();
grid.table(df);
save.plot.close();
