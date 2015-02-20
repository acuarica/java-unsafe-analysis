
library(ggplot2)
library(reshape2)
library(tools)
library(scales)
library(xtable)
suppressMessages(library(gdata))

printf <- function(format, ...) print(sprintf(format, ...));

save.plot <- function(plot, d, s, w=12, h=8) {
  path <- sprintf('%s-%s.pdf', d, s)
  printf("Saving plot %s to %s", s, path)
  pdf(file=path, paper='special', width=w, height=h, pointsize=12)
  print(plot)
  null <- dev.off()
}

save.csv <- function(csv, prefix, name) {
  file <- sprintf('%s-%s.csv', prefix, name);
  printf("Saving csv %s to %s", name, file);
  write.csv(csv, file=file);
}

formatd <- function(days) {
  days <- as.numeric(days);
  y <- trunc(days/365);
  m <- trunc((days %% 365) / 30);
  d <- trunc((days %% 365) %% 30);
  
  if (d > 15) m <- m + 1;
  if (m > 6) y <- y + 1;
  
  if (y == 0) { 
    if (m == 0) {
      if (d == 0 || d == 1) return ('1 day');
      if (d >= 2) return (sprintf('%s days', d));
    }
    if (m == 1) return ('1 month');
    if (m >= 2) return (sprintf('%s months', m));
  }
  if (y == 1) return ('1 year');
  if (y >= 2) return (sprintf('%s years', y));
}

df.methods <- (function() {
  csv.groups <- read.csv('unsafe-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.methods <- read.csv('unsafe-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
  
  df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df.methods$gid <- NULL; 
  
  df.methods
})();

csvfilename <- if (interactive()) 'build/unsafe-maven.csv' else commandArgs(trailingOnly = TRUE)[1];
path <- file_path_sans_ext(csvfilename);

df.maven <- (function() {
  csv.maven <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=TRUE);
  df.maven <- merge(csv.maven, df.methods, by.x="name", by.y="method", all.x=TRUE);
  df.maven$name <- factor(df.maven$name, levels=levels(df.methods$method));
  df.maven
})();

save.plot(ggplot(df.maven, aes(x=name))+geom_bar(stat="bin")+scale_x_discrete( drop=TRUE)+
            facet_grid(.~group, space='free_x', scales="free_x",drop=TRUE)+
            theme(axis.text.x=element_text(angle=45, hjust=1), 
                  legend.box="horizontal", legend.position="top", legend.title=element_blank(),
  strip.text.x=element_text(angle=90))+
  labs(x="sun.misc.Unsafe methods", y = "# call sites"), path, "plot-usage", h=5);




csv.boa <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
csv.so <- read.csv('stackoverflow/method-usages.csv', strip.white=TRUE, sep=',', header=TRUE);
csv.projects <- read.csv('unsafe-projects.csv', strip.white=TRUE, sep=',', header=TRUE);

df.boa <- csv.boa;
colnames(df.boa) <- c('kind', 'repo', 'rev', 'id', 'name', 'description', 'url', 'file', 'nsname', 'clsname', 'method', 'use', 'revs', 'start', 'end', 'asts', 'value');
df.boa <- subset(df.boa, id != 'neurogrid');
df.boa$start <- as.POSIXct(df.boa$start/1000000, origin="1970-01-01");
df.boa$end <- as.POSIXct(df.boa$end/1000000, origin="1970-01-01");
df.boa$lifetime <- as.numeric(df.boa$end-df.boa$start, units = "days");
df.boa$formatd <- df.boa$lifetime;
df.boa$astsk <- paste(trunc(df.boa$asts / 1000), 'k');
for (i in 3:nrow(df.boa)) df.boa$formatd[i] <- formatd(df.boa$formatd[i]);
df.boa$description <- NULL; df.boa$url <- NULL; df.boa$start <- NULL; df.boa$end <- NULL; df.boa$name <- NULL;
df.boa <- merge(df.boa, csv.projects, by.x='id', by.y='id', all.x=TRUE);
other.text <- 'application'
df.boa$package <- factor(other.text, levels=c("java.io", "java.lang", "java.nio",  "java.security", "java.util.concurrent", "sun.nio", other.text));
df.boa[startsWith(df.boa$nsname,'java.io') ,]$package <- 'java.io'
df.boa[startsWith(df.boa$nsname,'java.lang') ,]$package <- 'java.lang'
df.boa[startsWith(df.boa$nsname,'java.nio') ,]$package <- 'java.nio'
df.boa[startsWith(df.boa$nsname,'java.security') ,]$package <- 'java.security'
df.boa[startsWith(df.boa$nsname,'java.util.concurrent') ,]$package <- 'java.util.concurrent'
df.boa[startsWith(df.boa$nsname,'sun.nio') ,]$package <- 'sun.nio'


# plot-usage-so

df.so <- merge(csv.so, df.methods, by.x="method", by.y="method", all.x=TRUE);
df.so <- melt(df.so, id.vars = c('method', 'group'));
levels(df.so$variable) <- c('Usages in Questions only ', 'Usage in Answers only', 'Usages in both');

save.plot(ggplot(df.so, aes(x=method, y=value, fill=variable))+
            facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="identity")+
            theme(axis.text.x=element_text(angle=45, hjust=1), 
                  legend.box="horizontal", legend.position="top", legend.title=element_blank(),
                  strip.text.x=element_text(angle=90))+
            labs(x="sun.misc.Unsafe methods", y = "# matches"), path, "plot-usage-so", h=5);

df.usage <- subset(df.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');
df.usage <- dcast(df.usage, kind+id+name+asts+revs+formatd+file+nsname+clsname+method+astsk+lifetime+package~use, value.var='use', fun.aggregate=length, fill=-1)
df.usage$file <- NULL
df.usage <- df.usage[!duplicated(df.usage),]
df.usage <- melt(df.usage, id.vars=c('kind', 'id', 'name', 'asts', 'revs', 'formatd', 'nsname', 'clsname', 'method', 'astsk', 'lifetime', 'package'), variable.name='use')
df.usage <- subset(df.usage, value>0)
df.usage <- merge(df.usage, df.methods, by.x = "use", by.y = "method");

# Outputs

save.plot(ggplot(subset(df.usage, kind=='projectsWithUnsafe'), aes(x=use, fill=package))+geom_bar(stat="bin")+
    facet_grid(.~group, space='free', scales="free")+
  theme(axis.text.x=element_text(angle=50, hjust=1), axis.text.y=element_text(angle=0, hjust=1), 
        axis.title.x=element_text(angle=0),
        legend.box="horizontal", legend.position="top",
        #legend.text=element_text(angle=180,vjust=1),legend.text.align=-1,
        #legend.direction='horizontal',
        #legend.title=element_text(angle=180),legend.title.align=0.5,
        #legend.justification='center',
        #legend.direction='vertical',
    strip.text.x=element_text(angle=90))+
    labs(x="sun.misc.Unsafe methods", y = "# call sites"), path, "plot-usage-boa", h=5);

# cluster methods by project

gs <- list(
  c('adtools', 'cegcc', 'cgnu', 'ps2toolchain', 'takatuka'), # java.util.concurrent
  c('janetdev', 'android'), # partial java.util.concurrent
  
  c('jikesrvm', 'x10', 'jnode', 'ikvm'), # partial jdk
  c('jadoth', 'jaxlib', 'javapayload'), # use several things
  
  c('amock', 'essence'), 
  c('amino', 'concutest', 'high', 'katta', 'l2next'),
  
  c('archaiosjava', 'grinder', 'janux', 'java'),
  
  c('ec', 'essentialbudget', 'jprovocateur', 'simulaeco', 'xbeedriver', 'statewalker'), # literal/offset/put
  c('caloriecount', 'classreach', 'clipc', 'timelord', 'vcb'), # offset/put
  c('osfree', 'snarej'),
  
  c('beanlib'),
  c('aojunit', 'glassbox', 'junitrecorder'),
  
  c('jon'),
  c('hlv', 'javapathfinder', 'jigcell', 'lockss', 'ucl')
);

projectLevels <- unlist(gs);

df.project <- df.usage;
df.project$projlabel <- factor(paste(df.project$id, ' (', df.project$astsk, ')\n', df.project$revs, ' revs in ', df.project$formatd, sep=''));
df.project$projlabel <- factor(df.project$projlabel, levels=levels(df.project$projlabel)[match(projectLevels, projectLevels[order(projectLevels)])]);

g <- geom_bar(stat="bin", aes(x=group, fill=package));
f <- function(ncol) facet_wrap(~projlabel, scales="free_x", ncol=ncol);
t <- function(a) theme(axis.text.x=element_text(angle=a, hjust=1), legend.box="horizontal", legend.position="top");
l <- labs(x="sun.misc.Unsafe functional groups", y = "# call sites");

save.plot(
  ggplot(df.project)+
    f(7)+g+t(60)+l, path, "plot-usage-boa-by-project", h=15);

# 10 projects
save.plot(
  ggplot(subset(df.project, id %in% c('adtools', 'cegcc', 'cgnu', 'ps2toolchain', 'takatuka', 
                                      'janetdev', 'android', 'jikesrvm', 'x10', 'jnode')))+
    f(2)+g+t(45)+l, path, "plot-usage-boa-by-project-juc", w=6, h=10);

# 3 projects
save.plot(
  ggplot(subset(df.project, id %in% c('ikvm', 'jadoth', 'jaxlib')))+
    f(3)+g+t(45)+l, path, "plot-usage-boa-by-project-lot", w=6, h=3);

# 4 projects
save.plot(
  ggplot(subset(df.project, id %in% c('osfree', 'snarej', 'janux', 'javapayload')))+
    f(4)+g+t(45)+l, path, "plot-usage-boa-by-project-natadd", w=6, h=3);

# 13 projects
save.plot(
  ggplot(subset(df.project, id %in% c('ec', 'essentialbudget', 'jprovocateur', 'simulaeco', 
    'xbeedriver', 'statewalker', 'caloriecount', 'classreach', 'clipc', 'timelord', 'vcb', 'lockss', 'jon')))+
    f(4)+g+t(45)+l+scale_y_continuous(breaks=c(0,5,10), limits=c(0,10)), path, "plot-usage-boa-by-project-ser", w=7, h=6);

# 6 projects
save.plot(
  ggplot(subset(df.project, id %in% c('amino', 'concutest', 'high', 'katta', 
                                      'javapathfinder', 'l2next')))+
    f(3)+g+t(45)+l, path, "plot-usage-boa-by-project-cas", w=6, h=4);

# 2 projects
save.plot(
  ggplot(subset(df.project, id %in% c('beanlib', 'grinder')))+
    f(2)+g+t(45)+l+scale_y_continuous(breaks=c(0,1,2), limits=c(0,2)), path, "plot-usage-boa-by-project-volatile", w=6, h=2.5);

# 5 projects
save.plot(
  ggplot(subset(df.project, id %in% c('amock', 'aojunit', 'glassbox', 'junitrecorder', 'ucl')))+
    f(3)+g+t(45)+l, path, "plot-usage-boa-by-project-test", w=6, h=4);

# 4 projects
save.plot(
  ggplot(subset(df.project, id %in% c('java', 'jigcell', 'essence', 'hlv')))+
    f(2)+g+t(45)+l, path, "plot-usage-boa-by-project-misc", w=6, h=4);

# 1 Project
save.plot(
  ggplot(subset(df.project, id %in% c('archaiosjava')))+
    f(1)+g+t(45)+l, path, "plot-usage-boa-by-project-offheap", w=6, h=3);



# real uses
df.callers <- df.usage;
df.callers <- dcast(df.callers, nsname+clsname+method+package~use, value.var='use', fun.aggregate=length, fill=-1)
df.callers$package <- NULL;
#df.callers <- df.callers[!duplicated(df.callers),]

df.callers <- melt(df.callers, id.vars=c('nsname', 'clsname', 'method'), variable.name='use')
df.callers <- subset(df.callers, value>0)
df.callers <- merge(df.callers, df.methods, by.x = "use", by.y = "method");

#save.plot(ggplot(df.callers, aes(x=group))+geom_bar(stat="bin")+
#            facet_wrap(~nsname, scales="free_x")+
#            theme(axis.text.x=element_text(angle=60, hjust=1), legend.box="horizontal", legend.position="top",
#                  strip.text.x=element_text(angle=30))+
#            labs(x="sun.misc.Unsafe functional groups", y = "# call sites"), path, "plot-usage-boa-by-package", h=16);



counts.total <- subset(df.boa, kind=='countsTotal')[1,'value'];
counts.java <- subset(df.boa, kind=='countsJava')[1,'value'];
counts.unsafe <- nrow(dcast(subset(df.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral'), id~use, value.var='use', fun.aggregate=length));

printf("# of projects in SourceForge: %s (%s %%)", counts.total, 100);
printf("# of Java projects: %s (%s %%)", counts.java, round((counts.java/counts.total)*100, 2));
printf("# of Unsafe projects: %s (%s %%)", counts.unsafe, round((counts.unsafe/counts.java)*100, 2));
printf("# of call sites to Unsafe (include branches): %s", nrow(df.boa)-2);
printf("# of call sites to Unsafe (exclude branches): %s", nrow(df.usage));
printf("# of call sites to Unsafe (exclude branches and duplicates between projects): %s", nrow(df.callers));
