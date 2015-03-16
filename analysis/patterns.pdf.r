
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

levs = function(col) {
  levels(factor(col));
}

df.maven = load.csv('build/cs.csv');
df.deps = load.csv('build/deps-prod.csv');
df.cu = dcast(df.maven, id+package+classunit~name, value.var='cs', fun.aggregate=sum);
df.clones = dcast(df.cu, classunit~'clones', value.var='id', fun.aggregate=length);

deps = function(classes) {
  unsart = levs(df.maven[df.maven$classunit %in% classes, ]$id);
  count = length(levs(df.deps[df.deps$depId %in% unsart,]$depCount));
  count;
}

clones = function(classes) {
  df = df.clones[df.clones$classunit %in% classes, ];
  sum(df$clones);
}

row = function(name, classes) {
  list(name, length(classes), clones(classes), deps(classes));
}

rows = list();

classes = c('UnsignedBytes*');
rows[[1]] = row('Process byte arrays', classes);

classes = levs(df.cu[df.cu$compareAndSwapInt > 0 | df.cu$compareAndSwapLong > 0 | df.cu$compareAndSwapObject > 0,]$classunit);
rows[[2]] = row('Lock-free data structures/sync', classes);

classes = c();
rows[[3]] = row('Foreign data access/Marshalling', classes);

classes = c();
rows[[4]] = row('Serialization/Deserialization', classes);

classes = c();
rows[[5]] = row('Update final fields', classes);

classes = c('BitLargeArray*', 'ByteLargeArray*', 'StringLargeArray*', 'ShortLargeArray*', 'DoubleLargeArray*', 'FloatLargeArray*', 'IntLargeArray*', 'LongLargeArray*');
rows[[6]] = row('Large Arrays', classes);

classes = levs(df.cu[df.cu$defineClass > 0,]$classunit);
rows[[7]] = row('Define class without security checks', classes);

classes = levs(df.cu[df.cu$monitorEnter > 0 & df.cu$monitorExit > 0,]$classunit);
rows[[8]] = row('Monitor', classes);

classes = levs(df.cu[df.cu$throwException > 0,]$classunit);
rows[[9]] = row('Throw checked exceptions', classes);

df = data.frame(matrix(unlist(rows), ncol=4, byrow=T));
colnames(df) = c('Pattern', '# classes', '# clones', '# depend on');

save.plot.open(outfile, w=6, h=3);
grid.newpage();
grid.table(df);
save.plot.close();
