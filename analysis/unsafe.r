
library(ggplot2)
library(tools)
library(reshape2)
suppressMessages(library(gridExtra))

printf <- function(format, ...) print(sprintf(format, ...));

save.plot <- function(plot, d, s, w=12, h=8) {
  d <- file_path_sans_ext(d);
  filename <- sprintf('build/%s-plot-%s.pdf', d, s)
  printf("Saving plot %s to %s", s, filename)
  pdf(file=filename, paper='special', width=w, height=h, pointsize=12)
  print(plot)
  null <- dev.off()
}

save.grid <- function(df, d, name, w=8, h=12) {
  d <- file_path_sans_ext(d);
  filename <- sprintf('build/%s-%s.pdf', d, name);
  printf("Saving grid %s to %s", name, filename);
  pdf(file=filename, paper='special', width=w, height=h, pointsize=12);
  grid.table(df, show.rownames=FALSE )
  null <- dev.off()
}

save.csv <- function(df, d, name) {
  d <- file_path_sans_ext(d);
  filename <- sprintf('build/%s-%s.csv', d, name);
  printf("Saving csv %s to %s", name, filename);
  write.csv(df, file=filename, row.names=FALSE);
}

df.methods <- (function() {
  csv.groups <- read.csv('unsafe-def-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.methods <- read.csv('unsafe-def-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
  
  df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df.methods$gid <- NULL; 
  
  df.methods
})();

csvfilename <- if (interactive()) 'unsafe-maven.csv' else commandArgs(trailingOnly = TRUE)[1];

df.maven <- (function() {
  csv.maven <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=TRUE);
  df.maven <- merge(csv.maven, df.methods, by.x="name", by.y="method", all.x=FALSE);
  df.maven$id <- factor(paste(df.maven$groupId, df.maven$artifactId, sep=':'));

  noartifacts <- nrow(dcast(df.maven, groupId+artifactId~name, value.var='name', fun.aggregate=length));
  nogroups <- nrow(dcast(df.maven, groupId~name, value.var='name', fun.aggregate=length));
  #printf("# of call sites to Unsafe: %s", nrow(df.maven));
  #printf("# of artifacts using Unsafe: %s", noartifacts);
  #printf("# of groups using Unsafe: %s", nogroups);
  
  save.grid(data.frame(
    description=c("# of call sites to Unsafe", "# of artifacts using Unsafe", "# of groups using Unsafe"),
    total=c(nrow(df.maven), noartifacts, nogroups)), csvfilename, 'summary');
  
  df.maven
})();

invdeps <- function(f) {
  csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
  df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
  df.invdeps$rank <- c(1:nrow(df.invdeps))
  df <- dcast(df.maven, id~., value.var='name', fun.aggregate=length);
  df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
  df <- df[with(df, order(rank) ), ]
  
  save.plot(ggplot(subset(df, !is.na(depCount)), aes(x=rank, y='Ranking'))+geom_point(position='jitter')+
              theme(axis.text.y=element_blank())+labs(x="Ranking", y = "Artifacts"), 
            f, 'ranking', h=2, w=8);
  
  #save.csv(df.invdeps[1:20,], f, 'requested-top20-all');
  #save.csv(df[1:20,c('id', 'depCount', 'rank')], f, 'requested-top20-unsafe');
  
  save.grid(df.invdeps[1:20,], f, 'requested-top20-all');
  save.grid(df[1:20,c('id', 'depCount', 'rank')], f, 'requested-top20-unsafe');
};

invdeps('maven-invdeps-production.csv')
invdeps('maven-invdeps-all.csv')

save.plot(ggplot(df.maven, aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
            theme(axis.text.x=element_text(angle=62, hjust=1), legend.box="horizontal", legend.position="top", 
                  legend.title=element_blank(),strip.text.x=element_text(angle=90))+
            labs(x="sun.misc.Unsafe methods", y = "# call sites"), csvfilename, 'cs-horizontal', w=15, h=8);

save.plot(ggplot(df.maven, aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
            theme(axis.text.x=element_text(angle=62, hjust=1), axis.text.y=element_text(angle=90, hjust=1), 
                  axis.title.x=element_text(angle=180),
                  legend.box="horizontal", legend.position="top", 
                  legend.title=element_blank(),strip.text.x=element_text(angle=90))+
            labs(x="sun.misc.Unsafe methods", y = "# call sites"), csvfilename, 'cs', w=15, h=6);

gaid <- 'org.python_jython'
#h <- 1
h <- length(levels(df.maven$id))
for (gaid in head(levels(df.maven$id), h)) {
  save.plot(ggplot(subset(df.maven, id==gaid), aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
            theme(axis.text.x=element_text(angle=45, hjust=1))+ 
            labs(x="sun.misc.Unsafe methods", y = "# call sites"), csvfilename, paste('cs', gaid, sep='-'));
}
