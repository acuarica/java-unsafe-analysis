
printf <- function(format, ...) {
  print(sprintf(format, ...));
}

outpath <- function(file, ext) {
  path <- sprintf('build/%s.%s', file, ext);
  printf("Saving %s", path);
  path;
}

save.csv <- function(df, file) {
  write.csv(df, file=outpath(file, 'csv'));
}

save.plot.open <- function(file, w=12, h=8) {
  pdf(file=outpath(file, 'pdf'), paper='special', width=w, height=h, pointsize=12);
}

save.plot.close <- function() {
  null <- dev.off();
}

save.plot <- function(plot, file, w=12, h=8) { 
  save.plot.open(file, w=w, h=h);
  print(plot);
  save.plot.close();
}

df.methods <- (function() {
  csv.groups <- read.csv('unsafe-def-groups.csv', strip.white=TRUE, sep=',', header=TRUE);
  csv.methods <- read.csv('unsafe-def-methods.csv', strip.white=TRUE, sep=',', header=TRUE);
  
  df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df.methods$gid <- NULL; 
  
  df.methods
})();


# Multiple plot function
#
# ggplot objects can be passed in ..., or to plotlist (as a list of ggplot objects)
# - cols:   Number of columns in layout
# - layout: A matrix specifying the layout. If present, 'cols' is ignored.
#
# If the layout is something like matrix(c(1,2,3,3), nrow=2, byrow=TRUE),
# then plot 1 will go in the upper left, 2 will go in the upper right, and
# 3 will go all the way across the bottom.
#
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  require(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

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
