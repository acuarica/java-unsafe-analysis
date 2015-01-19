
library(ggplot2)
library(reshape2)
library(tools)

printf <- function(format, ...) print(sprintf(format, ...))

save <- function(p, d, s, w=12, h=8) {
  path <- sprintf('%s-chart-%s.pdf', d, s)
  printf("Saving plot %s to %s", s, path)
  pdf(file=path, paper='special', width=w, height=h, pointsize=12)
  print(p)
  null <- dev.off()
}

csvfilename <- 'unsafe.boa-7751.out.csv'
path <- file_path_sans_ext(csvfilename)
csv <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
colnames(csv) <- c('url', 'file', 'method', 'use');

uses <- nrow(csv)

countsTotal <- 699331
countsJava <- 50692
countsUnsafe <- nrow(dcast(csv, url~use, value.var='use', fun.aggregate = length))

keys <- c("Non-Java projects", "Java projects", "Java projects w/ Unsafe")
values <- c(countsTotal-countsJava, countsJava-countsUnsafe, countsUnsafe)

pct <- round(values/countsTotal * 100, 2)
keys <- paste(keys,"\n")
keys <- paste(keys, pct) # add percents to labels 
keys <- paste(keys,"%",sep="") # ad % to labels 
keys <- paste(keys, values, sep=' (')
keys <- paste(keys,")",sep="")

df <- data.frame(keys, values)
p <- ggplot(df, aes(x=1, y=values, fill=keys)) + geom_bar(stat="identity", color='gray87') +
  guides(fill=guide_legend(override.aes=list(colour=NA))) + coord_polar(theta='y') +
  theme(axis.ticks=element_blank(), 
        axis.title=element_blank(), 
        axis.text.y=element_blank(), 
        axis.text.x=element_text(color='black'),
        legend.position="none",panel.background = element_rect(fill = "white")) +
  scale_y_continuous(breaks=cumsum(df$values) - df$values/1.65, labels=df$keys)
save(p, path, 'pie', w=8, h=8)

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

df <- dcast(csv, use~., value.var='use', fun.aggregate = length)
colnames(df) <- c('use', 'count')
df$group <- df$use
levels(df$group) <- factor(groups)

p <- ggplot(df, aes(x=use, y=count, fill=group))
p <- p + facet_grid(. ~ group, space='free_x', scales="free_x")
p <- p + geom_bar(stat="identity")
p <- p + theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="none")
p <- p + labs(x="sun.misc.Unsafe Method", y = "# call sites")
p
save(p, path, "usage", h=6)
