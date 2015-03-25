
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

df.maven <- load.csv('build/cs.csv');

f = 'csv/maven-invdeps-production.csv';
invdeps <- function(f, bw) {
  csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
  df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
  df.invdeps$rank <- c(1:nrow(df.invdeps))
  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(rank) ), ]

  grid.newpage();
  grid.table(df.invdeps[1:50,], show.rownames=FALSE);
  
  grid.newpage();
  grid.table(df[1:50,c('id', 'depCount', 'rank')], show.rownames=FALSE);
  
  ggplot(subset(df, !is.na(depCount)), aes(x=rank))+
    geom_histogram(binwidth=bw)+
    theme(legend.title=element_blank())+
    labs(x="Ranking by Impact", y = "# Artifacts");
};

df = dcast(df.maven, id~'cs', value.var='cs', fun.aggregate=sum);
nocs = dcast(df, .~'cs', value.var='cs', fun.aggregate=sum)$cs[1];
noarts = nrow(df);
df = data.frame(
  desc=c("# of call sites to Unsafe", "# of artifacts using Unsafe"),
  total=c(nocs, noarts));

save.plot.open(outfile, w=12, h=16);

grid.newpage();
grid.table(df);

pp = invdeps('csv/maven-invdeps-production.csv', 1000);
pa = invdeps('csv/maven-invdeps-all.csv', 1000);

save.plot.close();

save.plot(pp, suffixfile(outfile, 'prod'), w=6, h=3);
save.plot(pa, suffixfile(outfile, 'all'), w=6, h=3);
