
library(reshape2)

source('analysis/utils/utils.r')

df.all = load.csv('out/maven-invdeps-all-list.csv')
df.prod = load.csv('out/maven-invdeps-production-list.csv')

filtercsv = function(df, outfile, unsarts) {
  df = subset(df, depId %in% unsarts)
  save.csv(df, outfile)
}

df = load.csv('out/analysis/cs.csv')

bytag = function(tags) {
  df = df[df$tag %in% tags,]
  unsarts = unique(df$id)
  
  tagssuffix = paste(tags, collapse='-')
  filtercsv(df.all, suffixfile(outfile, sprintf('all-%s', tagssuffix)), unsarts)
  filtercsv(df.prod, suffixfile(outfile, sprintf('prod-%s', tagssuffix)), unsarts)
}

bytag(c('app', 'lang'))
bytag(c('app'))
bytag(c('lang'))

df.depstats = data.frame(variable=c('artswithpominfo'), value=c(length(unique(df.all$depCount))))
save.csv(df.depstats, suffixfile(outfile, 'stats'))

save.csv(NULL, outfile)
