
library(ggplot2)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

df <- load.csv('csv/cassandra-maven.csv');
df$id = factor(paste(df$groupId, df$artifactId, sep=':'));

arts = data.frame(id=unique(df$id));
save.csv(arts, 'cassandra-arts');
