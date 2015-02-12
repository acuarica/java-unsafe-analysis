
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

df.methods <- (function() {
  csv.groups <- read.csv('unsafe-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.methods <- read.csv('unsafe-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
  df <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df$gid <- NULL;
  df;
})();

csvfilename <- if (interactive()) 'build/unsafe.csv' else commandArgs(trailingOnly = TRUE)[1];
path <- file_path_sans_ext(csvfilename)

csv.boa <- (function() {
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
  
  csv.boa <- read.csv(csvfilename, strip.white=TRUE, sep=',', header=FALSE);
  colnames(csv.boa) <- c('kind', 'repo', 'rev', 'id', 'name', 'description', 'url', 'file', 'nsname', 'clsname', 'method', 'use', 'revs', 'start', 'end', 'asts', 'value');
  csv.boa$start <- as.POSIXct(csv.boa$start/1000000, origin="1970-01-01");
  csv.boa$end <- as.POSIXct(csv.boa$end/1000000, origin="1970-01-01");
  csv.boa$lifetime <- as.numeric(csv.boa$end-csv.boa$start, units = "days");
  csv.boa$formatd <- csv.boa$lifetime;
  csv.boa$astsk <- paste(trunc(csv.boa$asts / 1000), 'k');
  
  for (i in 3:nrow(csv.boa) ) {
    csv.boa$formatd[i] <- formatd(csv.boa$formatd[i]);
  }
  
  csv.boa$description <- NULL
  csv.boa$url <- NULL
  csv.boa$start <- NULL
  csv.boa$end <- NULL
  #csv.boa$lifetime <- NULL
  
  csv.projects <- read.csv('unsafe-projects.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.boa$name <- NULL;
  csv.boa <- merge(csv.boa, csv.projects, by.x='id', by.y='id', all.x=TRUE);
  csv.boa;
})();

df.summary <- (function() {
  counts.total <- subset(csv.boa, kind=='countsTotal')[1,'value']
  counts.java <- subset(csv.boa, kind=='countsJava')[1,'value']
  counts.unsafe <- nrow(dcast(subset(csv.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral'), id~use, value.var='use', fun.aggregate=length))
  
  label <- c('nprojects', 'njavaprojects', 'nunsafeprojects');
  total <- c(counts.total, counts.java, counts.unsafe);
  perc <- c(100, round((counts.java/counts.total)*100, 2), round((counts.unsafe/counts.java)*100, 2))
  data.frame(label, total, perc);
})();

save.csv(df.summary, path, 'summary');  

# usage plot

(function() {
  csv.so <- read.csv('stackoverflow/method-usages.csv', strip.white=TRUE, sep=',', header=TRUE);
  df.so <- merge(csv.so, df.methods, by.x="method", by.y="method", all.x=TRUE);
  df.so <- melt(df.so, id.vars = c('method', 'group'));
  levels(df.so$variable) <- c('Usages in Questions only ', 'Usage in Answers only', 'Usages in both');
  
  p <- ggplot(df.so, aes(x=method, y=value, fill=variable))+
    facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="identity")+
    theme(axis.text.x=element_text(angle=45, hjust=1), 
          legend.box="horizontal", legend.position="top", legend.title=element_blank(),
          strip.text.x=element_text(angle=45))+
    labs(x="sun.misc.Unsafe methods", y = "# matches")
  save.plot(p, path, "plot-usage-so", h=6)
})();

df <- subset(csv.boa, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');
df <- dcast(df, kind+id+name+asts+revs+formatd+file+nsname+clsname+method+astsk+lifetime~use, value.var='use', fun.aggregate=length, fill=-1)
df$file <- NULL
df <- df[!duplicated(df),]
df <- melt(df, id.vars=c('kind', 'id', 'name', 'asts', 'revs', 'formatd', 'nsname', 'clsname', 'method', 'astsk', 'lifetime'), variable.name='use')
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

df <- merge(df, df.methods, by.x = "use", by.y = "method");

p <- ggplot(subset(df, kind=='projectsWithUnsafe') , aes(x=use, fill=package))+
  facet_grid(.~group, space='free_x', scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top",
        strip.text.x=element_text(angle=45))+
  labs(x="sun.misc.Unsafe methods", y = "# call sites")
save.plot(p, path, "plot-usage", h=6)

# cluster methods by project

g1 <- c('adtools', 'cegcc', 'cgnu', 'janetdev', 'ps2toolchain', 'takatuka', 'android', 'jikesrvm');
g2 <- c('caloriecount', 'classreach', 'clipc', 'ec', 'essentialbudget', 'jprovocateur', 'simulaeco', 'timelord', 'vcb', 'xbeedriver', 'statewalker', 'beanlib');
g3 <- c('osfree', 'snarej'); 
g4 <- c('aojunit', 'glassbox', 'jon', 'junitrecorder', 'neurogrid');
g5 <- c('concutest', 'high', 'katta');
g6 <- c('amino', 'amock', 'archaiosjava', 'essence', 'grinder', 'janux', 'java', 'javapayload', 'jaxlib', 'l2next');
g7 <- c('jadoth');
g8 <- c('x10', 'jnode', 'ikvm');
g9 <- c('ucl', 'lockss', 'jigcell', 'javapathfinder', 'hlv');
df.project <- df;
projectLevels <- c(g1, g2, g3, g4, g5, g6, g7, g8, g9);
df.project$id <- factor(df.project$id, levels=c(projectLevels));

gs <- list(g1, g2, g3, g4, g5, g6, g7, g8, g9);
cs <-    c( 4,  3,  2,  5,  3,  2,  1,  5,  5);
ws <-    c( 6,  6,  6,  6,  6,  6,  6,  8,  4);
hs <-    c( 4,  8,  4,  3,  3, 10,  4,  4,  4);

p <- ggplot(subset(df.project, kind=='projectsWithUnsafe'|kind=='projectsWithUnsafeLiteral'), 
            aes(x=group, fill=package))+
  facet_wrap(~id, scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top")+
  labs(x="sun.misc.Unsafe functional groups", y = "# call sites")
save.plot(p, path, "plot-usage-by-project", h=16)

for (i in 1:length(gs) ) {
p <- ggplot(subset(df.project, (kind=='projectsWithUnsafe'|kind=='projectsWithUnsafeLiteral') & id %in% gs[[i]]), aes(x=group, fill=package))+
  facet_wrap(~id, ncol=cs[i], scales="free_x")+geom_bar(stat="bin")+
  theme(axis.text.x=element_text(angle=45, hjust=1), legend.box="horizontal", legend.position="top")+
  labs(x="sun.misc.Unsafe functional groups", y = "# call sites")
save.plot(p, path, sprintf("plot-usage-by-project%s", i), w=ws[i], h=hs[i])
}


# Project table
#df <- subset(csv, kind=='projectsWithUnsafe' | kind=='projectsWithUnsafeLiteral');

df.table <- df;
df.table[df.table$kind=='projectsWithUnsafe','use'] <- 'allocateMemory';
df.table <- dcast(df.table, id+name+astsk+revs+formatd+asts+lifetime~use, value.var='use', fun.aggregate=length)

df.table.plot <- df.table;
df.table.plot$astsk <- NULL;
df.table.plot$formatd <- NULL;
df.table.plot$asts <- df.table.plot$asts / 1000;
colnames(df.table.plot) <- c('id', 'name', '# Revs', '# AST Nodes', 'Lifetime', '# call sites', '# smu Literal');

p <- ggplot(melt(df.table.plot, id.vars=c('id', 'name')), aes(x=id, y=value, fill=id))+
  facet_grid(variable~., scales='free')+geom_bar(stat="identity")+
  theme(axis.text.x=element_text(angle=45, hjust=1), 
        legend.box="horizontal", legend.position="none"#,
        #strip.text.x=element_text(angle=45)
        )+
  labs(x="Projects", y = "")
save.plot(p, path, "plot-projects-summary")


df.table <- df.table[with(df.table, order(id)), ]
df.table$asts <- NULL;
df.table$lifetime <- NULL;

#df.table$n <- row.names(df.table)
#df.table <- df.table[c(8,1,2,3,4,5,6,7)]

#df.table$name <- as.character(df.table$name)
#colnames(df.table) <- c('#', 'Name', 'Description', '# AST Nodes', '# Revisions', 'Lifetime', '# smU Call Sites', '# smU String Literal')
colnames(df.table) <- c('Name', 'Description', '# AST Nodes', '# Revisions', 'Lifetime', '# smU Call Sites', '# smU String Literal')

p <- sprintf('%s-%s.tex', path, 'table-projects')
printf("Saving table %s to %s", 'table-projects', p)
print(xtable(df.table, caption='Java Projects using \\smu{}', label='table:projects', align='r|l|l|r|r|r|Y|Y|'), 
             file=p, floating.environment='table*', table.placement='htb', tabular.environment='tabularx',
             caption.placement='top', include.rownames=FALSE,width="\\textwidth")

