
library(reshape2)

source('utils.r')

df = load.csv('csv/unsafe-maven.csv');
unsarts = levels(factor(paste(df$groupId, df$artifactId, sep=':')));

filtercsv = function(infile, outfile) {
  csv = load.csv(infile);
  df = csv;
  df = subset(df, depId %in% unsarts);
  
  save.csv(df, outfile);  
}

save.csv(NULL, outfile);
filtercsv('csv/maven-invdeps-all-list.csv', suffixfile(outfile, 'all'));
filtercsv('csv/maven-invdeps-production-list.csv', suffixfile(outfile, 'prod'));
