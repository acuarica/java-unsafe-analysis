
library(ggplot2)
library(ggdendro)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

df.maven <- read.csv('build/cs.csv', strip.white=TRUE, sep=',', header=TRUE);

do.hc <- function(classes=NULL) {
  df <- dcast(df.maven, id+package+classunit~name, value.var='name', fun.aggregate=length);
  df <- melt(df, id=c('id', 'package', 'classunit'));
  df[df$value > 0, 'value'] <- 1;
  df <- dcast(df, classunit~variable, value.var='value', fill=-1000, fun.aggregate=max);
  if (!is.null(classes)) {
    df <- df[df$classunit %in% classes,];
  }
  
  rownames(df) <- df$classunit
  df$classunit <- NULL
  
  d <- dist(df, method = "euclidean");
  hc <- hclust(d, method="ward.D");
  hc;
};

groups <- (function() {
  hc <- do.hc();
  #save.plot(ggdendrogram(hc), 'unsafe-maven-cluster', w=64, h=32);
  
  groups = groups.hclust(hc, h=8);
  
  for (i in 1:length(groups)) {
    groups[[i]] = hc$labels[ hc$order[hc$order %in% groups[[i]]] ]
  }
  
  groups;
})();


df <- dcast(df.maven, id+package+classunit~name, value.var='name', fun.aggregate=length);
df <- melt(df, id=c('id', 'package', 'classunit'));
df <- dcast(df, classunit~variable, value.var='value', fill=-1000, fun.aggregate=max);
df <- melt(df, id=c('classunit'), fun.aggregate=length);
df <- subset(df, value > 0);
df <- merge(df, df.methods, by.x='variable', by.y='method');

save.plot.open(outfile, w=12, h=10);
for (classes in groups[1:1]) {
  printf('Processing %s', paste(classes, collapse=' '));
  
  df.classes = df;
  df.classes$classunit = factor(df.classes$classunit, levels=classes);
  p = ggplot(subset(df.classes, classunit %in% classes), aes(x=variable, y=value))+
    geom_bar(stat="identity")+facet_grid(classunit~group, space='free_x', scales="free_x")+
    theme(axis.text.x=element_text(angle=45, hjust=1),
          strip.text.x=element_text(angle=90),
          strip.text.y=element_text(angle=0))+
    labs(x="sun.misc.Unsafe methods", y = "# call sites");
  
  hc <- do.hc(classes);
  multiplot(ggdendrogram(hc), p, layout=matrix(c(1,2,2,2), nrow=1, byrow=TRUE));
}

save.plot.close();

#df <- dcast(subset(df.most.used.artifacts.w.unsafe, !is.na(rank)), groupId~., value.var='rank',
#fun.aggregate=min)
#df <- df[with(df, order(.) ), ]
#i <- 'ai.h2o'
#save.plot.open(csvfilename, 'cs-groups-by-dep');
#for (i in df$groupId) {
#  printf('Processing group %s...', i);
#  print(ggplot(subset(df.maven, groupId==i), aes(x=name, fill=className))+geom_bar(stat="bin")+
#    facet_grid(artifactId~group, space='free_x', scales="free_x")+
#    theme(axis.text.x=element_text(angle=45, hjust=1))+
#    labs(x="sun.misc.Unsafe methods", y = sprintf("# call sites in %s", i) ));
#}
#save.plot.close();
