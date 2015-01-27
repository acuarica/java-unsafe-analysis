
library(ggplot2)
library(reshape2)
library(tools)
library(scales)
library(xtable)

printf <- function(format, ...) {
  print(sprintf(format, ...));
}

formatd <- function(days) {
  days <- as.numeric(days);
  y <- trunc(days/365);
  m <- trunc((days %% 365) / 30);
  d <- trunc((days %% 365) %% 30);

  if (y == 0) y <- sprintf('', y);
  if (y == 1) y <- sprintf(' %s year', y);
  if (y >= 2) y <- sprintf( '%s years', y);

  if (m == 0) m <- sprintf('', m);
  if (m == 1) m <- sprintf(' %s month', m);
  if (m >= 2) m <- sprintf(' %s months', m);

  if (d == 0) d <- sprintf('', d);
  if (d == 1) d <- sprintf(' %s day', d);
  if (d >= 2) d <- sprintf(' %s days', d);
  
  sprintf('%s%s%s', y, m, d)
}

save.plot <- function(p, d, s, w=12, h=8) {
  path <- sprintf('%s-%s.pdf', d, s)
  printf("Saving plot %s to %s", s, path)
  pdf(file=path, paper='special', width=w, height=h, pointsize=12)
  print(p)
  null <- dev.off()
}

save.table <- function(df, d, s, caption, label) {
  path <- sprintf('%s-%s.tex', d, s)
  printf("Saving table %s to %s", s, path)
  print.xtable(xtable(df, caption=caption, label=label), file=path)
}

if (interactive()) {
  csvfilename <- 'build/unsafe.csv'
} else {
  csvfilename <- commandArgs(trailingOnly = TRUE)[1]
}

path <- file_path_sans_ext(csvfilename)
csv <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
colnames(csv) <- c('kind', 'id', 'name', 'description', 'url', 'file', 'method', 'use', 'revs', 'start', 'end', 'asts', 'value');
csv$start <- as.POSIXct(csv$start/1000000, origin="1970-01-01");
csv$end <- as.POSIXct(csv$end/1000000, origin="1970-01-01");
csv$lifetime <- as.numeric(csv$end-csv$start, units = "days");
csv$formatd <- csv$lifetime;

for (i in 3:nrow(csv) ) {
  csv$formatd[i] <- formatd(csv$formatd[i]);
}

csv$lifetimey <- trunc(csv$lifetime/365);
csv$lifetimem <- trunc((csv$lifetime %% 365) / 30);
csv$lifetimed <- trunc((csv$lifetime %% 365) %% 30);

formatd(csv$lifetime)

1 & adtools & Amiga Development Tools (adtools) \\
2 & amino & Concurrent Building Block \\ 
3 & amock & Java Mock libarary for static method \\ 
4 & android & Android on PXA270 \\ 
5 & aojunit & An aspect-oriented extension to JUnit \\ 
6 & archaiosjava & Scalable and fast libraries for Java \\ 
7 & beanlib & Java Bean Library \\ 
8 & caloriecount & Track what you eat \\ 
9 & cegcc & CeGCC - Cross development for Pocket PC \\ 
10 & cgnu & CGNU (Clean GNU) \\ 
11 & classreach & Identifies unused Java classes and methods \\ 
12 & clipc & Library for IPC \\ 
13 & concutest & Tools to test concurrent Java programs easier \\ 
14 & ec & ec-gin Europe China Grid InterNetworking  \\ 
15 & essence & Essence Java Framework  \\ 
16 & essentialbudget & Essential Budget \\ 
17 & glassbox & Troubleshooting and monitoring agent \\ 
18 & grinder & Load testing framework \\ 
19 & high & Highly Scalable Java  \\ 
20 & ikvm & JVM for .NET Framework and Mono \\ 
21 & jadoth & abstraction utils and frameworks \\ 
22 & janetdev & Ja.NET - Java Development Tools for .NET \\ 
23 & janux & Java directly on the Linux Kernel \\ 
24 & java & Lightweight Java Game Library \\ 
25 & javapayload & Payloads to be used for post-exploitation \\ 
26 & jaxlib & Platform independent Java library \\ 
27 & jikesrvm & The Jikes Research Virtual Machine (RVM) \\ 
28 & jnode & JNode: new Java Operating System \\ 
29 & jon & Java Object Notation \\ 
30 & jprovocateur & RAD for Ajax applications in Java \\ 
31 & junitrecorder & Record test cases \\ 
32 & katta & Lucene in the cloud \\ 
33 & l2next & L2 Private Server code  \\ 
34 & neurogrid & P2P Bookmark Organiser \\ 
35 & osfree & osFree operating system \\ 
36 & ps2toolchain & Toolchain for the Playstation 2's \\ 
  37 & simulaeco & Semester project \\ 
  38 & snarej & Snare's Not A Risc os Emulator in Java \\ 
39 & statewalker & Graph traversing library \\ 
40 & takatuka & TakaTuka Java Virtual Machine \\ 
41 & timelord & A tool for estimating and tracking time \\ 
42 & vcb & Component Based Development tool \\ 
43 & x10 & Experimental language for DARPA/HPCS \\ 
44 & xbeedriver & Driver for the ZigBee network \\ 



countsTotal <- subset(csv, kind=='countsTotal')[1,'value']
countsJava <- subset(csv, kind=='countsJava')[1,'value']

projectsWithUnsafe <- subset(csv, kind=='projectsWithUnsafe');
projectsWithUnsafe$use <- factor(projectsWithUnsafe$use)

projectsWithUnsafeLiteral <- subset(csv, kind=='projectsWithUnsafeLiteral');

# java-over-total plot
keys <- c("Non-Java projects", "Java projects")
values <- c(countsTotal-countsJava, countsJava)

df <- data.frame(keys, values)
p <- ggplot(df, aes(x=keys, y=values, fill=keys)) + geom_bar(stat='identity')
p <- p + theme(legend.position="none") + labs(x='', y = "# projects")
p <- p + scale_y_continuous(labels=comma)
save.plot(p, path, 'plot-java-over-total', w=4, h=4)

# unsafe-over-java plot
countsUnsafe <- nrow(dcast(projectsWithUnsafe, url~use, value.var='use', fun.aggregate=length))

keys <- c("Java projects not using Unsafe", "Java projects using Unsafe")
values <- c(countsJava-countsUnsafe, countsUnsafe)

df <- data.frame(keys, values)
p <- ggplot(df, aes(x=keys, y=values, fill=keys)) + geom_bar(stat='identity')
p <- p + theme(legend.position="none") + labs(x='', y = "# projects")
p <- p + scale_y_continuous(labels=comma)
save.plot(p, path, 'plot-unsafe-over-java', w=6, h=6)


# usage plot

g.array <- 'Array'
g.memory <- 'Memory'
g.park <- 'Park'
g.cas <- 'CAS'
g.single <- 'Single'
g.class <- 'Class'
g.get <- 'Get'
g.put <- 'Put'
g.offset <- 'Offset'

groups <- c(g.memory, g.single, g.memory, g.array, g.array, g.cas, g.cas, g.cas,
            g.memory, g.class, g.class, g.offset, g.memory, g.memory, 
            g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.get, g.get, 
            g.offset, g.memory, g.park, g.memory,
            g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, g.put, 
            g.memory, g.memory, g.offset, g.offset, g.single, g.park)

df <- dcast(projectsWithUnsafe, use~., value.var='use', fun.aggregate = length)
colnames(df) <- c('use', 'count')
df$group <- df$use
levels(df$group) <- factor(groups)

p <- ggplot(df, aes(x=use, y=count, fill=group))
p <- p + facet_grid(. ~ group, space='free_x', scales="free_x")
p <- p + geom_bar(stat="identity")
p <- p + theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="none")
p <- p + labs(x="sun.misc.Unsafe Method", y = "# call sites")
save.plot(p, path, "plot-usage", h=6)



# tex table
df <- dcast(projectsWithUnsafe, id+name+description+revs+start+end+lifetime+asts~., value.var='use', fun.aggregate = length)
save.table(df, path, 'projects', 'Projects using Unsafe', 'table:projects')

# tex literal table 
df <- dcast(projectsWithUnsafeLiteral, id+name+description~., value.var='use', fun.aggregate = length)
