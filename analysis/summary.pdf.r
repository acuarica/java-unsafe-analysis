
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

df.maven <- read.csv('build/cs.csv', strip.white=TRUE, sep=',', header=TRUE);

invdeps <- function(f) {
  csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
  df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
  df.invdeps$rank <- c(1:nrow(df.invdeps))
  #df <- dcast(df.maven, id+groupId~., value.var='name', fun.aggregate=length);
  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(rank) ), ]
  
  p = ggplot(subset(df, !is.na(depCount)), aes(x=rank))+
    geom_histogram(binwidth=1000)+
    theme()+
    labs(x="Ranking", y = "Artifacts");
  print(p);
  
  grid.newpage();
  grid.table(df.invdeps[1:20,], show.rownames=FALSE);
  
  grid.newpage();
  grid.table(df[1:20,c('id', 'depCount', 'rank')], show.rownames=FALSE);
  
  invisible(df);
};

save.plot.open(outfile);

noarts <- nrow(dcast(df.maven, id~name, value.var='name', fun.aggregate=length));

grid.newpage();
grid.table(data.frame(
  desc=c("# of call sites to Unsafe", "# of artifacts using Unsafe"),
  total=c(nrow(df.maven), noarts)));

invdeps('csv/maven-invdeps-production.csv');
invdeps('csv/maven-invdeps-all.csv');

save.plot.close();
