
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('analysis/utils/utils.r')

df.maven = load.csv('out/analysis/cs.csv')

df.stats = load.csv('out/stats-maven.csv')
df.depstats = load.csv('out/analysis/deps-stats.csv')

f = 'out/maven-invdeps-production.csv';
invdeps <- function(f) {
  csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
  df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
  df.invdeps$rank <- c(1:nrow(df.invdeps))
  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(rank) ), ]

  grid.newpage();
  grid.table(df.invdeps[1:30,], show.rownames=FALSE);
  
  grid.newpage();
  grid.table(df[1:30,c('id', 'depCount', 'rank')], show.rownames=FALSE);  
};

# Stats on Maven

#total.arts = 75405;
#total.arts.withpominfo = 47127;

total.arts = as.numeric(as.character(df.stats[df.stats$variable=='uniqueJarsArtifactsCount','value']))
total.arts.withpominfo = as.numeric(as.character(df.depstats[df.depstats$variable=='artswithpominfo','value']))

save.plot.open(outfile, w=7, h=9);

summary = function(tags) {
  tagssuffix = paste(tags, collapse='-')
  df.prod = load.csv(sprintf('out/analysis/deps-prod-%s.csv', tagssuffix))
  unsart.prod = length(unique(df.prod$depCount))
  porc.prod =  (unsart.prod / total.arts) * 100
  deps.prod =  (unsart.prod / total.arts.withpominfo) * 100
  
  df.all = load.csv(sprintf('out/analysis/deps-all-%s.csv', tagssuffix))
  unsart.all = length(unique(df.all$depCount))
  porc.all =  (unsart.all / total.arts) * 100
  deps.all =  (unsart.all / total.arts.withpominfo) * 100

  df = merge(df.maven, df.methods, by.x=c('name'), by.y=c('method'), all.x=TRUE, all.y=TRUE)
  df = df[df$tag %in% tags,]
  
  #df = dcast(df, id~'cs', value.var='cs', fun.aggregate=sum)
  #nousage = dcast(df, .~'cs', value.var='cs', fun.aggregate=sum)$cs[1]
  nousage = sum(df[df$member!='literal',]$cs)
  nocs = sum(df[df$member=='method',]$cs)
  nofields = sum(df[df$member=='field',]$cs)
  noliteral = sum(df[df$member=='literal',]$cs)
  noarts = length(unique(df$id))
  #noarts = nrow(df)
  df = data.frame(
    variable=c('tags', "Number of call sites/field reads to Unsafe", "Number of call sites", "Number of field reads", "Number of literal", "Number of artifacts using Unsafe", "% dependencies on prod", "% dependencies on all", "% dependencies on prod with POM info", "% dependencies on all with POM info", "Numbers of Unsafe artifacts on prod", "Number of Unsafe artifacts on all"),
    value=c(tagssuffix, nousage, nocs, nofields, noliteral, noarts, porc.prod, porc.all, deps.prod, deps.all, unsart.prod, unsart.all))

  grid.newpage();
  grid.table(df);
}

grid.newpage();
grid.table(df.stats);

grid.newpage();
grid.table(df.depstats);

summary(c('app', 'lang'))
summary(c('app'))
summary(c('lang'))

invdeps('out/maven-invdeps-production.csv');
invdeps('out/maven-invdeps-all.csv');

save.plot.close();
