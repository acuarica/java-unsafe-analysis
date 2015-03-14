
library(ggplot2)
#library(ggdendro)
#library(reshape2)
#library(grid)
#library(gridExtra)

source('utils.r')

df.maven <- read.csv('build/cs.csv', strip.white=TRUE, sep=',', header=TRUE);

p = ggplot(df.maven, aes(x=name))+
  geom_bar(stat="bin")+
  facet_grid(.~group, space='free_x', scales="free_x")+
  theme(axis.text.x=element_text(angle=90, hjust=1), 
        axis.text.y=element_text(angle=90, hjust=1), 
        axis.title.x=element_text(angle=180),
        legend.box="horizontal", 
        legend.position="top", 
        legend.title=element_blank(),
        strip.text.x=element_text(angle=90))+
  labs(x="sun.misc.Unsafe methods", y = "# call sites");

save.plot(p, outfile, w=15, h=6);
