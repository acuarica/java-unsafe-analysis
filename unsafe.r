
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

df.so <- merge(csv.so, df.methods, by.x="method", by.y="method", all.x=TRUE);
df.so <- melt(df.so, id.vars = c('method', 'group'));
levels(df.so$variable) <- c('Usages in Questions only ', 'Usage in Answers only', 'Usages in both');

df.usage <- subset(df.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');
df.usage <- dcast(df.usage, kind+id+name+asts+revs+formatd+file+nsname+clsname+method+astsk+lifetime+package~use, value.var='use', fun.aggregate=length, fill=-1)
df.usage$file <- NULL
df.usage <- df.usage[!duplicated(df.usage),]
df.usage <- melt(df.usage, id.vars=c('kind', 'id', 'name', 'asts', 'revs', 'formatd', 'nsname', 'clsname', 'method', 'astsk', 'lifetime', 'package'), variable.name='use')
df.usage <- subset(df.usage, value>0)
df.usage <- merge(df.usage, df.methods, by.x = "use", by.y = "method");

# Outputs

save.plot(ggplot(df.so, aes(x=method, y=value, fill=variable))+
  facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="identity")+
  theme(axis.text.x=element_text(angle=45, hjust=1), 
    legend.box="horizontal", legend.position="top", legend.title=element_blank(),
    strip.text.x=element_text(angle=90))+
  labs(x="sun.misc.Unsafe methods", y = "# matches"), path, "plot-usage-so", h=6);

save.plot(ggplot(subset(df.usage, kind=='projectsWithUnsafe') , aes(x=use, fill=package))+
  facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top",
    strip.text.x=element_text(angle=90))+
    labs(x="sun.misc.Unsafe methods", y = "# call sites"), path, "plot-usage-boa", h=6);

# cluster methods by project

#gs <- list(g1, g2, g3, g4, g5, g6, g7, g8, g9);
#cs <-    c( 4,  3,  2,  5,  3,  2,  1,  5,  5);
#ws <-    c( 6,  6,  6,  6,  6,  6,  6,  8,  4);
#hs <-    c( 4,  8,  4,  3,  3, 10,  4,  4,  4);
#labels <- paste(df.project$id, ' (', df.project$astsk, ')\n', df.project$revs, ' revs in ', df.project$formatd, sep='');
#df.project$id <- factor(df.project$id, levels=c(projectLevels));
#levels(df.project$projlabel)[order(projectLevels)]
#order(levels(df.project$projlabel))
#projectLevels[]
#order(projectLevels)

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

#for (i in 1:length(gs) ) {
#save.plot(ggplot(subset(df.project, (kind=='projectsWithUnsafe'|kind=='projectsWithUnsafeLiteral') & id %in% gs[[i]]), aes(x=group, fill=package))+
#            facet_wrap(~id, ncol=cs[i], scales="free_x")+geom_bar(stat="bin")+
#            theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top")+
#            labs(x="sun.misc.Unsafe functional groups", y = "# call sites"), path, sprintf("plot-usage-by-project%s", i), w=ws[i], h=hs[i])
#}

# Project table
#df <- subset(csv, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');

#df.table <- df.usage;
#df.table[df.table$kind=='projectsWithUnsafe','use'] <- 'allocateMemory';
#df.table <- dcast(df.table, id+name+astsk+revs+formatd+asts+lifetime~use, value.var='use', fun.aggregate=length)

#df.table.plot <- df.table;
#df.table.plot$astsk <- NULL;
#df.table.plot$formatd <- NULL;
#df.table.plot$asts <- df.table.plot$asts / 1000;
#colnames(df.table.plot) <- c('id', 'name', '# Revs', '# AST Nodes', 'Lifetime', '# call sites', '# smu Literal');

#p <- ggplot(melt(df.table.plot, id.vars=c('id', 'name')), aes(x=id, y=value, fill=id))+
 # facet_grid(variable~., scales='free')+geom_bar(stat="identity")+
  #theme(axis.text.x=element_text(angle=45, hjust=1), 
   #     legend.box="horizontal", legend.position="none"#,
       # #strip.text.x=element_text(angle=45)
    #    )+
#  labs(x="Projects", y = "")
#save.plot(p, path, "plot-projects-summary")


#df.table <- df.table[with(df.table, order(id)), ]
#df.table$asts <- NULL;
#df.table$lifetime <- NULL;

#df.table$n <- row.names(df.table)
#df.table <- df.table[c(8,1,2,3,4,5,6,7)]

#df.table$name <- as.character(df.table$name)
#colnames(df.table) <- c('#', 'Name', 'Description', '# AST Nodes', '# Revisions', 'Lifetime', '# smU Call Sites', '# smU String Literal')

#colnames(df.table) <- c('Name', 'Description', '# AST Nodes', '# Revisions', 'Lifetime', '# smU Call Sites', '# smU String Literal')

#p <- sprintf('%s-%s.tex', path, 'table-projects')
#printf("Saving table %s to %s", 'table-projects', p)
#print(xtable(df.table, caption='Java Projects using \\smu{}', label='table:projects', align='r|l|l|r|r|r|Y|Y|'), 
 #            file=p, floating.environment='table*', table.placement='htb', tabular.environment='tabularx',
  #           caption.placement='top', include.rownames=FALSE,width="\\textwidth")
