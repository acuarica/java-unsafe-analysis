
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

csvfilename <- if (interactive()) 'build/unsafe.csv' else commandArgs(trailingOnly = TRUE)[1];
path <- file_path_sans_ext(csvfilename)

csv.boa <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
csv.so <- read.csv('stackoverflow/method-usages.csv', strip.white=TRUE, sep=',', header=TRUE);
csv.groups <- read.csv('unsafe-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
csv.methods <- read.csv('unsafe-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
csv.projects <- read.csv('unsafe-projects.csv', strip.white=TRUE, sep=',', header=TRUE);

df.boa <- csv.boa;
colnames(df.boa) <- c('kind', 'repo', 'rev', 'id', 'name', 'description', 'url', 'file', 'nsname', 'clsname', 'method', 'use', 'revs', 'start', 'end', 'asts', 'value');
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

counts.total <- subset(df.boa, kind=='countsTotal')[1,'value']
counts.java <- subset(df.boa, kind=='countsJava')[1,'value']
counts.unsafe <- nrow(dcast(subset(df.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral'), id~use, value.var='use', fun.aggregate=length))

df.summary <- data.frame(
  label=c('nprojects', 'njavaprojects', 'nunsafeprojects'), 
  total=c(counts.total, counts.java, counts.unsafe),
  perc=c(100, round((counts.java/counts.total)*100, 2), round((counts.unsafe/counts.java)*100, 2)));

print(df.summary);

df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
df.methods$gid <- NULL;

# plot-usage-so

df.so <- merge(csv.so, df.methods, by.x="method", by.y="method", all.x=TRUE);
df.so <- melt(df.so, id.vars = c('method', 'group'));
levels(df.so$variable) <- c('Usages in Questions only ', 'Usage in Answers only', 'Usages in both');

save.plot(ggplot(df.so, aes(x=method, y=value, fill=variable))+
            facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="identity")+
            theme(axis.text.x=element_text(angle=45, hjust=1), 
                  legend.box="horizontal", legend.position="top", legend.title=element_blank(),
                  strip.text.x=element_text(angle=90))+
            labs(x="sun.misc.Unsafe methods", y = "# matches"), path, "plot-usage-so", h=6);

df.usage <- subset(df.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');
df.usage <- dcast(df.usage, kind+id+name+asts+revs+formatd+file+nsname+clsname+method+astsk+lifetime+package~use, value.var='use', fun.aggregate=length, fill=-1)
df.usage$file <- NULL
df.usage <- df.usage[!duplicated(df.usage),]
df.usage <- melt(df.usage, id.vars=c('kind', 'id', 'name', 'asts', 'revs', 'formatd', 'nsname', 'clsname', 'method', 'astsk', 'lifetime', 'package'), variable.name='use')
df.usage <- subset(df.usage, value>0)
df.usage <- merge(df.usage, df.methods, by.x = "use", by.y = "method");

# Outputs

save.plot(ggplot(subset(df.usage, kind=='projectsWithUnsafe') , aes(x=use, fill=package))+
  facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top",
    strip.text.x=element_text(angle=90))+
    labs(x="sun.misc.Unsafe methods", y = "# call sites"), path, "plot-usage-boa", h=6);

# cluster methods by project

gs <- list(
  c('adtools', 'cegcc', 'cgnu', 'ps2toolchain', 'takatuka'), # java.util.concurrent
  c('janetdev', 'android'), # partial java.util.concurrent
  
  c('jikesrvm', 'x10', 'jnode', 'ikvm'), # partial jdk
  c('jadoth', 'jaxlib', 'javapayload'), # use several things
  
  c('ec', 'essentialbudget', 'jprovocateur', 'simulaeco', 'xbeedriver', 'statewalker'), # literal/offset/put
  c('caloriecount', 'classreach', 'clipc', 'timelord', 'vcb'), # offset/put
  c('osfree', 'snarej'),
  
  c('amino', 'amock', 'archaiosjava', 'essence', 'grinder', 'janux', 'java', 'l2next'),
  
  c('concutest', 'high', 'katta'),
  c('beanlib'),
  c('aojunit', 'glassbox', 'junitrecorder'),
  
  c('jon', 'neurogrid'),
  c('ucl', 'lockss', 'jigcell', 'javapathfinder', 'hlv')
);

projectLevels <- unlist(gs);

df.project <- df.usage;
df.project$projlabel <- factor(paste(df.project$id, ' (', df.project$astsk, ')\n', df.project$revs, ' revs in ', df.project$formatd, sep=''));
df.project$projlabel <- factor(df.project$projlabel, levels=levels(df.project$projlabel)[match(projectLevels, projectLevels[order(projectLevels)])]);

save.plot(ggplot(df.project, aes(x=group, fill=package))+facet_wrap(~projlabel, scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=60, hjust=1), legend.box="horizontal", legend.position="top")+
  labs(x="sun.misc.Unsafe functional groups", y = "# call sites"), path, "plot-usage-boa-by-project", h=16);


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
