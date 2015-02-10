
library(ggplot2)
library(reshape2)
library(tools)
library(scales)
library(xtable)
suppressMessages(library(gdata))

printf <- function(format, ...) {
  print(sprintf(format, ...));
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

save.plot <- function(p, d, s, w=12, h=8) {
  path <- sprintf('%s-%s.pdf', d, s)
  printf("Saving plot %s to %s", s, path)
  pdf(file=path, paper='special', width=w, height=h, pointsize=12)
  print(p)
  null <- dev.off()
}

csv.so <- read.csv('method-usages-so.csv', strip.white=TRUE, sep=',', header=TRUE);

if (interactive()) {
  csvfilename <- 'build/unsafe.csv'
} else {
  csvfilename <- commandArgs(trailingOnly = TRUE)[1]
}

path <- file_path_sans_ext(csvfilename)
csv <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
colnames(csv) <- c('kind', 'repo', 'rev', 'id', 'name', 'description', 'url', 'file', 'nsname', 'clsname', 'method', 'use', 'revs', 'start', 'end', 'asts', 'value');
csv$start <- as.POSIXct(csv$start/1000000, origin="1970-01-01");
csv$end <- as.POSIXct(csv$end/1000000, origin="1970-01-01");
csv$lifetime <- as.numeric(csv$end-csv$start, units = "days");
csv$formatd <- csv$lifetime;
csv$asts <- paste(trunc(csv$asts / 1000), 'k');

for (i in 3:nrow(csv) ) {
  csv$formatd[i] <- formatd(csv$formatd[i]);
}

csv$description <- NULL
csv$url <- NULL
csv$start <- NULL
csv$end <- NULL
csv$lifetime <- NULL

# Print summary
countsTotal <- subset(csv, kind=='countsTotal')[1,'value']
countsJava <- subset(csv, kind=='countsJava')[1,'value']
countsUnsafe <- nrow(dcast(subset(csv, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral'), id~use, value.var='use', fun.aggregate=length))

printf("Total number of projects: %d", countsTotal);
printf("Total number of Java projects: %d (%s%%)", countsJava, round((countsJava/countsTotal)*100, 2) );
printf("Total number of projects: %d (%s%%)", countsUnsafe, round((countsUnsafe/countsJava)*100, 2) );

# usage plot

g.array <- 'Array'
g.memory <- 'Off-Heap'
g.park <- 'Park'
g.cas <- 'CAS'
g.single <- 'Misc'
g.class <- 'Class'
g.get <- 'Get'
g.put <- 'Put'
g.offset <- 'Offset'

g.monitor <- 'Monitor'

methods <- data.frame(
  dcast(subset(csv, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral'), use~., value.var='use', fun.aggregate=length)$use,
  c(g.memory, g.single, g.memory, g.array, g.array, g.cas, g.cas, g.cas,
            g.memory, g.class, g.class, g.offset, g.memory, g.memory, 
            g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.single, g.get, g.get, g.get, g.get, g.get, 
            g.offset, g.memory, g.park, g.memory,
            g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, 
            g.memory, g.memory, g.offset, g.offset, 'Literal', g.single, g.park)
);
colnames(methods) <- c('method', 'group');

csv.so <- merge(csv.so, methods, by.x = "method", by.y = "method", all.x=TRUE);
csv.so$group <- as.character(csv.so$group)
csv.so[csv.so$method=='defineAnonymousClass','group'] <- g.class;
csv.so[csv.so$method=='getBooleanVolatile','group'] <- g.class;
csv.so[csv.so$method=='getByteVolatile','group'] <- g.get;
csv.so[csv.so$method=='getCharVolatile','group'] <- g.get;
csv.so[csv.so$method=='getDoubleVolatile','group'] <- g.get;
csv.so[csv.so$method=='getFloatVolatile','group'] <- g.get;
csv.so[csv.so$method=='getShortVolatile','group'] <- g.get;
csv.so[csv.so$method=='getUnsafe','group'] <- g.single;
csv.so[csv.so$method=='monitorEnter','group'] <- g.monitor;
csv.so[csv.so$method=='monitorExit','group'] <- g.monitor;
csv.so[csv.so$method=='putBooleanVolatile','group'] <- g.put;
csv.so[csv.so$method=='putByteVolatile','group'] <- g.put;
csv.so[csv.so$method=='putCharVolatile','group'] <- g.put;
csv.so[csv.so$method=='putDoubleVolatile','group'] <- g.put;
csv.so[csv.so$method=='putFloatVolatile','group'] <- g.put;
csv.so[csv.so$method=='putShortVolatile','group'] <- g.put;
csv.so[csv.so$method=='tryMonitorEnter','group'] <- g.monitor;

csv.so <- melt(csv.so, id.vars = c('method', 'group'));

levels(csv.so$variable) <- c('Usages in Questions only ', 'Usage in Answers only', 'Usages in both');

p <- ggplot(csv.so, aes(x=method, y=value, fill=variable))+
  facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="identity")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top", legend.title=element_blank())+
  labs(x="sun.misc.Unsafe methods", y = "# matches")
save.plot(p, path, "plot-usage-so", h=6)


df <- subset(csv, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');
df <- dcast(df, kind+id+name+asts+revs+formatd+file+nsname+clsname+method~use, value.var='use', fun.aggregate=length, fill=-1)
df$file <- NULL
df <- df[!duplicated(df),]
df <- melt(df, id.vars=c('kind', 'id', 'name', 'asts', 'revs', 'formatd', 'nsname', 'clsname', 'method'), variable.name='use')
df <- subset(df, value>0)

other.text <- 'application'

df$use <- factor(df$use)
df$package <- as.character(other.text)
df[startsWith(df$nsname,'java.io') ,]$package <- 'java.io'
df[startsWith(df$nsname,'java.lang') ,]$package <- 'java.lang'
df[startsWith(df$nsname,'java.nio') ,]$package <- 'java.nio'
df[startsWith(df$nsname,'java.security') ,]$package <- 'java.security'
df[startsWith(df$nsname,'java.util.concurrent') ,]$package <- 'java.util.concurrent'
df[startsWith(df$nsname,'sun.nio') ,]$package <- 'sun.nio'
df$package <- factor(df$package)
df$package <- factor(df$package, levels=c("java.io", "java.lang", "java.nio", 
                                          "java.security", "java.util.concurrent", "sun.nio", other.text));

df <- merge(df, methods, by.x = "use", by.y = "method")

p <- ggplot(subset(df, kind=='projectsWithUnsafe') , aes(x=use, fill=package))+
  facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top")+
  labs(x="sun.misc.Unsafe methods", y = "# call sites")
save.plot(p, path, "plot-usage", h=8)

# cluster methods by project

g1 <- c('adtools', 'cegcc', 'cgnu', 'janetdev', 'ps2toolchain', 'takatuka', 'android', 'jikesrvm');
g2 <- c('caloriecount', 'classreach', 'clipc', 'ec', 'essentialbudget', 'jprovocateur', 'simulaeco', 'timelord', 'vcb', 'xbeedriver', 'beanlib', 'statewalker');
g3 <- c('osfree', 'snarej'); 
g4 <- c('aojunit', 'glassbox', 'jon', 'junitrecorder', 'neurogrid');
g5 <- c('concutest', 'high', 'katta');
g6 <- c('amino', 'amock', 'archaiosjava', 'essence', 'grinder', 'janux', 'java', 'javapayload', 'jaxlib', 'l2next');
g7 <- c('jadoth');
g8 <- c('x10', 'jnode', 'ikvm');

df.project <- df;
projectLevels <- c(g1, g2, g3, g4, g5, g6, g7, g8);
df.project$id <- factor(df.project$id, levels=projectLevels);

gs <- list(g1, g2, g3, g4, g5, g6, g7, g8);
cs <-    c( 4,  3,  2,  5,  3,  2,  1,  5);
ws <-    c( 6,  6,  6,  6,  6,  6,  6,  8);
hs <-    c( 4,  8,  4,  3,  3, 10,  4,  4);

p <- ggplot(subset(df.project, kind=='projectsWithUnsafe'), aes(x=group, fill=package))+
  facet_wrap(~id, scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top")+
  labs(x="sun.misc.Unsafe functional groups", y = "# call sites")
save.plot(p, path, "plot-usage-by-project", h=16)

for (i in 1:length(gs) ) {
p <- ggplot(subset(df.project, kind=='projectsWithUnsafe' & id %in% gs[[i]]), aes(x=group, fill=package))+
  facet_wrap(~id, ncol=cs[i], scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top")+
  labs(x="sun.misc.Unsafe functional groups", y = "# call sites")
save.plot(p, path, sprintf("plot-usage-by-project%s", i), w=ws[i], h=hs[i])
}


# Project table
#df <- subset(csv, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');
df[df$kind=='projectsWithUnsafe','use'] <- 'allocateMemory';
df <- dcast(df, id+name+asts+revs+formatd~use, value.var='use', fun.aggregate=length)
df$n <- row.names(df)
df <- df[c(8,1,2,3,4,5,6,7)]

df$name <- as.character(df$name)

df[df$id=='adtools','name'] <- 'Amiga Development Tools (adtools)'
df[df$id=='amino','name'] <- 'Concurrent Building Block'
df[df$id=='amock','name'] <- 'Java Mock library for static method'
df[df$id=='android','name'] <- 'Android on PXA270'
df[df$id=='aojunit','name'] <- 'An aspect-oriented extension to JUnit'
df[df$id=='archaiosjava','name'] <- 'Scalable and fast libraries for Java'
df[df$id=='beanlib','name'] <- 'Java Bean Library'
df[df$id=='caloriecount','name'] <- 'Track what you eat'
df[df$id=='cegcc','name'] <- 'CeGCC - Cross development for Pocket PC'
df[df$id=='cgnu','name'] <- 'CGNU (Clean GNU)'
df[df$id=='classreach','name'] <- 'Identifies unused Java classes and methods'
df[df$id=='clipc','name'] <- 'Library for IPC'
df[df$id=='concutest','name'] <- 'Tools to test concurrent Java programs'
df[df$id=='ec','name'] <- 'ec-gin Europe China Grid InterNetworking'
df[df$id=='essence','name'] <- 'Essence Java Framework'
df[df$id=='essentialbudget','name'] <- 'Essential Budget'
df[df$id=='glassbox','name'] <- 'Troubleshooting and monitoring agent'
df[df$id=='grinder','name'] <- 'Load testing framework'
df[df$id=='high','name'] <- 'Highly Scalable Java'
df[df$id=='hlv','name'] <- 'Collection of high level view plugins for eclipse'
df[df$id=='ikvm','name'] <- 'JVM for .NET Framework and Mono'
df[df$id=='jadoth','name'] <- 'abstraction utils and frameworks'
df[df$id=='janetdev','name'] <- 'Ja.NET - Java Development Tools for .NET'
df[df$id=='janux','name'] <- 'Java directly on the Linux Kernel'
df[df$id=='java','name'] <- 'Lightweight Java Game Library'
df[df$id=='javapathfinder','name'] <- 'Verifies Java bytecode programs'
df[df$id=='javapayload','name'] <- 'Payloads to be used for post-exploitation'
df[df$id=='jaxlib','name'] <- 'Platform independent Java library'
df[df$id=='jigcell','name'] <- 'Computational biology problem solving'
df[df$id=='jikesrvm','name'] <- 'The Jikes Research Virtual Machine (RVM)'
df[df$id=='jnode','name'] <- 'JNode: new Java Operating System'
df[df$id=='jon','name'] <- 'Java Object Notation'
df[df$id=='jprovocateur','name'] <- 'RAD for Ajax applications in Java'
df[df$id=='junitrecorder','name'] <- 'Record test cases'
df[df$id=='katta','name'] <- 'Lucene in the cloud'
df[df$id=='l2next','name'] <- 'L2 Private Server code'
df[df$id=='lockss','name'] <- 'Lots of Copies Keep Stuff Safe'
df[df$id=='neurogrid','name'] <- 'P2P Bookmark Organiser'
df[df$id=='osfree','name'] <- 'osFree operating system'
df[df$id=='ps2toolchain','name'] <- "Toolchain for the Playstation 2's"
df[df$id=='simulaeco','name'] <- 'Semester project'
df[df$id=='snarej','name'] <- "Snare's Not A Risc OS Emulator in Java"
df[df$id=='statewalker','name'] <- 'Graph traversing library'
df[df$id=='takatuka','name'] <- 'TakaTuka Java Virtual Machine'
df[df$id=='timelord','name'] <- 'A tool for estimating and tracking time'
df[df$id=='ucl','name'] <- 'A final year project by UCL students'
df[df$id=='vcb','name'] <- 'Component Based Development tool'
df[df$id=='x10','name'] <- 'Experimental language for DARPA/HPCS'
df[df$id=='xbeedriver','name'] <- 'Driver for the ZigBee network'

colnames(df) <- c('#', 'Name', 'Description', '# AST Nodes', '# Revisions', 'Lifetime', '# smU Call Sites', '# smU String Literal')

p <- sprintf('%s-%s.tex', path, 'table-projects')
printf("Saving table %s to %s", 'table-projects', p)
print(xtable(df, caption='Java Projects using \\smu{}', label='table:projects', align='l|r|l|l|r|r|r|Y|Y|'), 
             file=p, floating.environment='table*', table.placement='htb', tabular.environment='tabularx',
             caption.placement='top', include.rownames=FALSE,width="\\textwidth")
