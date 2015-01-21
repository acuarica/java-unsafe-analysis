
library(ggplot2)
library(reshape2)
library(tools)
library(scales)

printf <- function(format, ...) print(sprintf(format, ...));

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
  csvfilename <- 'unsafe-large.csv'
} else {
  csvfilename <- commandArgs(trailingOnly = TRUE)[1]
}

path <- file_path_sans_ext(csvfilename)
csv <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
colnames(csv) <- c('kind', 'id', 'name', 'description', 'url', 'file', 'method', 'use', 'value');

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
