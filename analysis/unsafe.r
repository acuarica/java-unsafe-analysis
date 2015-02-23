
library(ggplot2)
library(tools)
library(reshape2)

printf <- function(format, ...) print(sprintf(format, ...));

save.plot <- function(plot, d, s, w=12, h=8) {
  path <- sprintf('build/%s-plot-%s.pdf', d, s)
  printf("Saving plot %s to %s", s, path)
  pdf(file=path, paper='special', width=w, height=h, pointsize=12)
  print(plot)
  null <- dev.off()
}

df.methods <- (function() {
  csv.groups <- read.csv('unsafe-def-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.methods <- read.csv('unsafe-def-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
  
  df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df.methods$gid <- NULL; 
  
  df.methods
})();

csvfilename <- if (interactive()) 'unsafe-maven.csv' else commandArgs(trailingOnly = TRUE)[1];
path <- file_path_sans_ext(csvfilename);

df.maven <- (function() {
  csv.maven <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=TRUE);
  df.maven <- merge(csv.maven, df.methods, by.x="name", by.y="method", all.x=FALSE);
  df.maven$id <- factor(paste(df.maven$groupId, df.maven$artifactId, sep=':'));

  noartifacts <- nrow(dcast(df.maven, groupId+artifactId~name, value.var='name', fun.aggregate=length));
  nogroups <- nrow(dcast(df.maven, groupId~name, value.var='name', fun.aggregate=length));
  printf("# of call sites to Unsafe: %s", nrow(df.maven));
  printf("# of artifacts using Unsafe: %s", noartifacts);
  printf("# of groups using Unsafe: %s", nogroups);
  
  df.maven
})();

save.plot(ggplot(df.maven, aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
            theme(axis.text.x=element_text(angle=62, hjust=1), axis.text.y=element_text(angle=90, hjust=1), 
                  axis.title.x=element_text(angle=180),
                  legend.box="horizontal", legend.position="top", 
                  legend.title=element_blank(),strip.text.x=element_text(angle=90))+
            labs(x="sun.misc.Unsafe methods", y = "# call sites"), path, 'cs', w=15, h=6);

gaid <- 'org.python_jython'
for (gaid in head(levels(df.maven$id), 5)) {
  save.plot(ggplot(subset(df.maven, id==gaid), aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
            theme(axis.text.x=element_text(angle=45, hjust=1))+ 
            labs(x="sun.misc.Unsafe methods", y = "# call sites"), path, paste('cs', gaid, sep='-'));
}


csv.invdeps <- read.csv('maven-invdeps.csv', strip.white=TRUE, sep=',', header=TRUE);
df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
df.invdeps$rank <- c(1:nrow(df.invdeps))
df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
df <- df[with(df, order(rank) ), ]

save.plot(ggplot(df, aes(x=rank, y='Ranking'))+
            geom_vline(aes(xintercept=rank, x=rank, y='Ranking'))+
            theme(axis.text.y=element_blank())+labs(x="Ranking", y = "Artifacts"), 
          path, 'ranking', h=2, w=8);
