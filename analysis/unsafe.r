
library(ggplot2)
library(tools)
library(reshape2)
library(grid)
library(gridExtra)

printf <- function(format, ...) {
  print(sprintf(format, ...));
}

outfile <- function(origin, name, ext) {
  file <- sprintf('build/%s-%s.%s', origin, name, ext);
  printf("Saving %s", file);
  file;
}

save.csv <- function(df, origin, name) {
  write.csv(df, file=outfile(origin, name, 'csv'));
}

save.plot.open <- function(origin, name, w=12, h=8) {
  pdf(file=outfile(file_path_sans_ext(origin), name, 'pdf'), paper='special', width=w, height=h, pointsize=12);
}

save.plot.close <- function() {
  null <- dev.off();
}

save.plot <- function(plot, d, s, w=12, h=8) { 
  save.plot.open(d, s); 
  print(plot); 
  save.plot.close();
}

csvfilename <- if (interactive()) 'unsafe-maven.csv' else commandArgs(trailingOnly = TRUE)[1];

df.methods <- (function() {
  csv.groups <- read.csv('unsafe-def-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.methods <- read.csv('unsafe-def-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
  
  df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df.methods$gid <- NULL; 
  
  df.methods
})();

df.maven <- (function() {
  csv.maven <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=TRUE);
  df.maven <- merge(csv.maven, df.methods, by.x="name", by.y="method", all.x=FALSE);
  df.maven$id <- factor(paste(df.maven$groupId, df.maven$artifactId, sep=':'));

  df.maven$package <- NA;
  df.maven$class = NA;
  for (i in 1:nrow(df.maven)) {
    className <- as.character(df.maven$className[i]);
    a <- strsplit(className, '/')[[1]]
    df.maven$package[i] <- paste(a[1:length(a)-1], collapse='/');
    df.maven$class[i] <- a[length(a)];
  }
  df.maven$class <- factor(df.maven$class);

  df.maven
})();

df.most.used.artifacts.w.unsafe <- (function() {

  invdeps <- function(f) {
    csv.invdeps <- read.csv(f, strip.white=TRUE, sep=',', header=TRUE);
    df.invdeps <- csv.invdeps[with(csv.invdeps, order(-depCount) ), ]
    df.invdeps$rank <- c(1:nrow(df.invdeps))
    df <- dcast(df.maven, id+groupId~., value.var='name', fun.aggregate=length);
    df <- merge(df, df.invdeps, by.x='id', by.y='depId', all.x=TRUE);
    df <- df[with(df, order(rank) ), ]
    
    print(ggplot(subset(df, !is.na(depCount)), aes(x=rank))+geom_histogram(binwidth=1000)+
            theme()+labs(x="Ranking", y = "Artifacts"));
    
    grid.newpage();
    grid.table(df.invdeps[1:20,], show.rownames=FALSE);
    
    grid.newpage();
    grid.table(df[1:20,c('id', 'depCount', 'rank')], show.rownames=FALSE);
    
    df;
  };
  
  save.plot.open(csvfilename, 'summary');
  
  noartifacts <- nrow(dcast(df.maven, groupId+artifactId~name, value.var='name', fun.aggregate=length));
  nogroups <- nrow(dcast(df.maven, groupId~name, value.var='name', fun.aggregate=length));
  
  grid.newpage();
  grid.table(data.frame(
    description=c("# of call sites to Unsafe", "# of artifacts using Unsafe", "# of groups using Unsafe"),
    total=c(nrow(df.maven), noartifacts, nogroups)));
  
  invdeps('maven-invdeps-production.csv');
  df <- invdeps('maven-invdeps-all.csv');
  
  save.plot.close();
  
  df;
})();

cluster.sort <- (function() {
  df <- dcast(df.maven, id~name, value.var='name', fun.aggregate=length);
  rownames(df) <- df$id
  df$id <- NULL
  
  d <- dist(df, method = "euclidean");
  fit <- hclust(d, method="ward.D");
  
  save.plot.open(csvfilename, 'cluster', w=64, h=64);
  plot(fit);
  groups <- cutree(fit, k=5);
  rect.hclust(fit, k=4, border="red");
  save.plot.close();
  
  row.names(df[fit$order,]);
})();

(function() {
  save.plot(ggplot(df.maven, aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
              theme(axis.text.x=element_text(angle=62, hjust=1), legend.box="horizontal", legend.position="top", 
                    legend.title=element_blank(),strip.text.x=element_text(angle=90))+
              labs(x="sun.misc.Unsafe methods", y = "# call sites"), csvfilename, 'cs-all-horizontal', w=15, h=8);
  
  save.plot(ggplot(df.maven, aes(x=name))+geom_bar(stat="bin")+facet_grid(.~group, space='free_x', scales="free_x")+
              theme(axis.text.x=element_text(angle=62, hjust=1), axis.text.y=element_text(angle=90, hjust=1), 
                    axis.title.x=element_text(angle=180),
                    legend.box="horizontal", legend.position="top", 
                    legend.title=element_blank(),strip.text.x=element_text(angle=90))+
              labs(x="sun.misc.Unsafe methods", y = "# call sites"), csvfilename, 'cs-all-vertical-pre', w=15, h=6);  
})();

df <- dcast(df.maven, class+className~., fun.aggregate=length);
df <- dcast(df, class~., fun.aggregate=length);
df <- df[with(df, order(-.) ), ]
save.plot.open(csvfilename, 'cs-classes');
i <- 'Striped64';
for (i in df$class) {
  printf('Processing class %s...', i);
  print(ggplot(subset(df.maven, class==i), aes(x=name, fill=className))+geom_bar(stat="bin")+
          facet_grid(.~group, space='free_x', scales="free_x")+
          theme(axis.text.x=element_text(angle=45, hjust=1))+
          labs(x="sun.misc.Unsafe methods", y = "# call sites"));
}
save.plot.close();

df <- dcast(subset(df.most.used.artifacts.w.unsafe, !is.na(rank)), groupId~., value.var='rank', fun.aggregate=min)
df <- df[with(df, order(.) ), ]
i <- 'ai.h2o'
save.plot.open(csvfilename, 'cs-groups-by-dep');
for (i in df$groupId) {
  printf('Processing group %s...', i);
  print(ggplot(subset(df.maven, groupId==i), aes(x=name, fill=className))+geom_bar(stat="bin")+
    facet_grid(artifactId~group, space='free_x', scales="free_x")+
    theme(axis.text.x=element_text(angle=45, hjust=1))+
    labs(x="sun.misc.Unsafe methods", y = sprintf("# call sites in %s", i) ));
}
save.plot.close();

save.plot.open(csvfilename, 'cs-artifacts');
for (i in cluster.sort) {
  printf('Processing artifact %s...', i);
  print(ggplot(subset(df.maven, id==i), aes(x=name, fill=className))+geom_bar(stat="bin")+
      facet_grid(.~group, space='free_x', scales="free_x")+
      theme(axis.text.x=element_text(angle=45, hjust=1))+
      labs(x="sun.misc.Unsafe methods", y = "# call sites"));
}
save.plot.close();
