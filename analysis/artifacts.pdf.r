
library(ggplot2)
library(reshape2)

source('analysis/utils/utils.r')

df.maven = load.csv('out/analysis/cs.csv');

df = df.maven;
df = merge(df, df.methods, by.x='name', by.y='method', all.x=TRUE);

df.artsbyrank <- (function() {
  csv.invdeps <- load.csv('out/maven-invdeps-production.csv');

  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, csv.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(-depCount) ), ]

  df;
})();

save.plot.open(outfile, w=6, h=6);

i = 1;
#for (i in (1:nrow(df.artsbyrank)) ) {
for (i in (1:6) ) {
  art = df.artsbyrank[i,]$id;
  deps = df.artsbyrank[i,]$depCount;
  
  printf('Processing artifact %s', art);
  
  p = ggplot(df[df$id %in% art,], aes(x=name, y=cs))+
    geom_bar(stat="identity")+
    facet_grid(className~group, space='free_x', scales="free_x")+
    theme(axis.text.x=element_text(angle=60, hjust=1),
          strip.text.x=element_text(angle=90),
          strip.text.y=element_text(angle=0))+
    #labs(x='Methods', y = '# call sites', title=sprintf('%s (%s)', art, deps));
    labs(x='Methods', y = '# call sites', title=art);
  print(p);
}

save.plot.close();
