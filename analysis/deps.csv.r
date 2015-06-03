
library(reshape2);

source('analysis/utils.r');

df = load.csv('out/unsafe-maven.csv');
df = filterlang(df);

unsarts = levels(factor(paste(df$groupId, df$artifactId, sep=':')));

filtercsv = function(infile, outfile) {
  csv = load.csv(infile);
  df = csv;
  df = subset(df, depId %in% unsarts);
  
  save.csv(df, outfile);  
}

save.csv(NULL, outfile);
filtercsv('out/maven-invdeps-all-list.csv', suffixfile(outfile, 'all'));
filtercsv('out/maven-invdeps-production-list.csv', suffixfile(outfile, 'prod'));
