
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

df.maven <- load.csv('build/cs.csv');

f = 'csv/maven-invdeps-production.csv';
dc = 500;
bw = 0.1;
invdeps <- function(f, bw, dc) {
  csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
  df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
  df.invdeps$rank <- c(1:nrow(df.invdeps))
  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(rank) ), ]

  #grid.newpage();
  #grid.table(df.invdeps[1:20,], show.rownames=FALSE);
  
  #grid.newpage();
  #grid.table(df[1:20,c('id', 'depCount', 'rank')], show.rownames=FALSE);

  df.all = df;
  df.all$unsafe = 'Unsafe';
  df.all$depCount = NULL;
  df.all$rank = NULL;
  df.all = merge(df.invdeps, df.all, by.x='depId', by.y='id', all.x=TRUE, all.y=TRUE);
  #hola=df.all[!is.na(df.all$unsafe) & df.all$unsafe == TRUE,]
  df.all[is.na(df.all$unsafe),]$unsafe = 'Safe';
  
  df.invdeps$depCount = df.invdeps$depCount / df.invdeps$depCount[1];
  df$depCount = df$depCount / df$depCount[1];
  
  p = ggplot(subset(df.all, !is.na(depCount) & depCount > dc), aes(x=rank, y=depCount, fill=unsafe))+
    geom_bar(stat='identity')+
    theme(legend.title=element_blank())+
    labs(x="Dependencies", y = "# Artifacts", title='a');
  #print(p);

  p = ggplot(data=subset(df.invdeps, !is.na(depCount) & depCount > 0.1), aes(x=rank, y=depCount))+
    geom_bar(stat='identity')+
    labs(x="Dependencies", y = "# Artifacts", title='a');
  #print(p);

  p = ggplot(data=subset(df, !is.na(depCount) & depCount > 0.1), aes(x=rank, y=depCount))+
    geom_bar(stat='identity')+
    labs(x="Dependencies", y = "# Artifacts", title='a');
  #print(p);
  
  p = ggplot(subset(df.all, !is.na(depCount)), aes(x=depCount))+
    geom_histogram(binwidth=bw, aes(y=cumsum(..count..)))+
    facet_grid(unsafe~.)+
    labs(x="Dependencies", y = "# Artifacts");
  #print(p);
  
  p = ggplot(subset(df.invdeps, !is.na(depCount)), aes(x=depCount))+
    geom_histogram(binwidth=0.01, aes(y=cumsum(..count..)))+
    #geom_density()+
    theme(legend.title=element_blank())+
    labs(x="Dependencies upon (normalized)", y = "# Artifacts", title='Cumulative histogram');
  print(p);
  
  p = ggplot(subset(df, !is.na(depCount) ), aes(x=depCount))+
    geom_histogram(binwidth=0.01, aes(y=cumsum(..count..)))+
    #geom_density()+
    theme(legend.title=element_blank())+
    labs(x="Dependencies upon (normalized)", y = "# Artifacts", title='Cumulative histogram');
  print(p);
  
  p;
  
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

pp = invdeps('csv/maven-invdeps-production.csv', 500, 500);
pa = invdeps('csv/maven-invdeps-all.csv', 500, 500);

save.plot.close();

save.plot(pp, suffixfile(outfile, 'prod'), w=6, h=3);
save.plot(pa, suffixfile(outfile, 'all'), w=6, h=3);
