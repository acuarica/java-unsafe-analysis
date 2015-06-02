
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('analysis/utils.r')

df.maven <- load.csv('out/cs.csv');

f = 'out/maven-invdeps-production.csv';
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


total.arts = 75405;
total.arts.wdeps = 40622;

df.prod = load.csv('out/deps-prod.csv');
unsart.prod = length(unique(df.prod$depCount))
porc.prod =  (unsart.prod / total.arts) * 100;
deps.prod =  (unsart.prod / total.arts.wdeps) * 100;

df.all = load.csv('out/deps-all.csv');
unsart.all = length(unique(df.all$depCount));
porc.all =  (unsart.all / total.arts) * 100;
deps.all =  (unsart.all / total.arts.wdeps) * 100;


#(17296 / total.arts.wdeps) * 100;

df = dcast(df.maven, id~'cs', value.var='cs', fun.aggregate=sum);
nocs = dcast(df, .~'cs', value.var='cs', fun.aggregate=sum)$cs[1];
noarts = nrow(df);
df = data.frame(
  desc=c("# of call sites to Unsafe", "# of artifacts using Unsafe", "% prod", "% all", "% prod w/ deps", "% all w/ deps", "# unarts prod", "# unarts all"),
  total=c(nocs, noarts, porc.prod, porc.all, deps.prod, deps.all, unsart.prod, unsart.all));

save.plot.open(outfile, w=12, h=16);

grid.newpage();
grid.table(df);

pp = invdeps('out/maven-invdeps-production.csv', 1000);
pa = invdeps('out/maven-invdeps-all.csv', 1000);

save.plot.close();

save.plot(pp, suffixfile(outfile, 'prod'), w=6, h=3);
save.plot(pa, suffixfile(outfile, 'all'), w=6, h=3);
