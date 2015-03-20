
library(ggplot2)
library(ggdendro)
library(reshape2)
library(grid)
library(gridExtra)

source('utils.r')

groups.hclust = function (tree, k= NULL, which=NULL, x=NULL, h=NULL, border=2, cluster=NULL) {
  if (length(h) > 1L | length(k) > 1L) 
    stop("'k' and 'h' must be a scalar")
  if (!is.null(h)) {
    if (!is.null(k)) 
      stop("specify exactly one of 'k' and 'h'")
    k <- min(which(rev(tree$height) < h))
    k <- max(k, 2)
  }
  else if (is.null(k)) 
    stop("specify exactly one of 'k' and 'h'")
  if (k < 2 | k > length(tree$height)) 
    stop(gettextf("k must be between 2 and %d", length(tree$height)), 
         domain = NA)
  if (is.null(cluster)) 
    cluster <- cutree(tree, k = k)
  clustab <- table(cluster)[unique(cluster[tree$order])]
  m <- c(0, cumsum(clustab))
  if (!is.null(x)) {
    if (!is.null(which)) 
      stop("specify exactly one of 'which' and 'x'")
    which <- x
    for (n in seq_along(x)) which[n] <- max(which(m < x[n]))
  }
  else if (is.null(which)) 
    which <- 1L:k
  if (any(which > k)) 
    stop(gettextf("all elements of 'which' must be between 1 and %d", 
                  k), domain = NA)
  border <- rep_len(border, length(which))
  retval <- list()
  for (n in seq_along(which)) {
    retval[[n]] <- which(cluster == as.integer(names(clustab)[which[n]]))
  }
  
  retval
}

df.maven = load.csv('build/cs.csv');

dupcols = function(df, cols) {
  for (col in cols) {
    if(col %in% colnames(df)) {
      df[paste(col, 'W', sep='')] = df[col];
    }
  }
  
  df;
}

do.hc = function(classes=NULL) {
  df = dcast(df.maven, id+package+classunit~name, value.var='name', fun.aggregate=length);
  df = melt(df, id=c('id', 'package', 'classunit'));
  df[df$value > 0, 'value'] <- 1;
  df <- dcast(df, classunit~variable, value.var='value', fill=-1000, fun.aggregate=max);
  if (!is.null(classes)) {
    df <- df[df$classunit %in% classes,];
  }
  
  get.methods = function(groupname) subset(df.methods, group==groupname)$method;
  
  df = dupcols(df, get.methods('CAS'));
  df = dupcols(df, get.methods('Monitor'));
  df = dupcols(df, get.methods('Class'));  
  
  rownames(df) = df$classunit;
  df$classunit = NULL;
  
  d <- dist(df, method = "euclidean");
  hc <- hclust(d, method="ward.D");
  hc;
};

tree <- do.hc();
groups = groups.hclust(tree, h=10);

for (i in 1:length(groups)) {
  groups[[i]] = tree$labels[ tree$order[tree$order %in% groups[[i]]] ]
}

df <- dcast(df.maven, id+package+classunit~name, value.var='cs', fun.aggregate=sum);
df <- melt(df, id=c('id', 'package', 'classunit'));
df <- dcast(df, classunit~variable, value.var='value', fill=-1000, fun.aggregate=max);
df <- melt(df, id=c('classunit'), fun.aggregate=length);
df <- subset(df, value > 0);
df <- merge(df, df.methods, by.x='variable', by.y='method');
df$classunit = factor(df$classunit, levels=tree$labels[tree$order]);

save.plot.open(outfile, w=12, h=10);

for (classes in groups) {
  printf('Processing %s', paste(classes, collapse=' '));
  
  p = ggplot(subset(df, classunit %in% classes), aes(x=variable, y=value))+
    geom_bar(stat="identity")+facet_grid(classunit~group, space='free_x', scales="free_x")+
    theme(axis.text.x=element_text(angle=45, hjust=1),
          strip.text.x=element_text(angle=90),
          strip.text.y=element_text(angle=0))+
    labs(x="sun.misc.Unsafe members", y = "# call sites");
  
  hc <- do.hc(classes);
  multiplot(ggdendrogram(hc), p, layout=matrix(c(1,2,2,2), nrow=1, byrow=TRUE));
}

save.plot.close();
