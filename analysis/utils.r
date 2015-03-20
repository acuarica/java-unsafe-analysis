
printf = function(format, ...) {
  print(sprintf(format, ...));
}

load.csv = function(file) {
  read.csv(file, strip.white=TRUE, comment.char='#');
}

save.csv = function(df, file, quote=FALSE, row.names=FALSE) {
  printf("Saving %s", file);
  write.csv(df, file=file, quote=quote, row.names=row.names);
}

save.plot.open = function(file, w=12, h=8) {
  printf("Saving %s", file);
  pdf(file=file, paper='special', width=w, height=h, pointsize=12);
}

save.plot.close = function() {
  null = dev.off();
}

save.plot = function(plot, file, w=12, h=8) { 
  save.plot.open(file, w=w, h=h);
  print(plot);
  save.plot.close();
}

suffixfile = function(outfile, suffix, sep='-') {
  library(tools);
  
  sprintf('%s%s%s.%s', file_path_sans_ext(outfile), sep, suffix, file_ext(outfile));
}

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

outfile <- commandArgs(trailingOnly = TRUE)[1];

df.methods <- (function() {
  csv.groups <- load.csv('csv/unsafe-def-groups.csv');
  csv.methods <- load.csv('csv/unsafe-def-methods.csv');
  
  df.methods <- merge(csv.methods, csv.groups, by='gid', all.x=TRUE, all.y=TRUE);
  df.methods$gid <- NULL; 
  
  df.methods
})();
