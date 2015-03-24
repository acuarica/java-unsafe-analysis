
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

df.maven <- read.csv('build/cs.csv', strip.white=TRUE, sep=',', header=TRUE);

f = 'csv/maven-invdeps-production.csv';
invdeps <- function(f) {
  csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
  df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
  df.invdeps$rank <- c(1:nrow(df.invdeps))
  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(rank) ), ]

  grid.newpage();
  grid.table(df.invdeps[1:20,], show.rownames=FALSE);
  
  grid.newpage();
  grid.table(df[1:20,c('id', 'depCount', 'rank')], show.rownames=FALSE);

  df$unsafe = TRUE;
  df$depCount = NULL;
  df.all = merge(df.invdeps, df, by.x='depId', by.y='id', all.x=TRUE, all.y=TRUE);
  #hola=df.all[!is.na(df.all$unsafe) & df.all$unsafe == TRUE,]
  df.all[is.na(df.all$unsafe),]$unsafe = FALSE;
  
  ggplot(subset(df.all, !is.na(depCount) & depCount > 1000), aes(x=depCount, fill=unsafe))+
    geom_histogram(binwidth=500)+
    #geom_density()+
    theme()+
    labs(x="Ranking", y = "Artifacts");
};

df = dcast(df.maven, id~'cs', value.var='cs', fun.aggregate=sum);
nocs = dcast(df, .~'cs', value.var='cs', fun.aggregate=sum)$cs[1];
noarts = nrow(df);
df = data.frame(
  desc=c("# of call sites to Unsafe", "# of artifacts using Unsafe"),
  total=c(nocs, noarts));

save.plot.open(outfile);

grid.newpage();
grid.table(df);

pp = invdeps('csv/maven-invdeps-production.csv');
pa = invdeps('csv/maven-invdeps-all.csv');

save.plot.close();

save.plot(pp, suffixfile(outfile, 'prod'), w=6, h=3);
save.plot(pa, suffixfile(outfile, 'all'), w=6, h=3);
