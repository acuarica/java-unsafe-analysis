
library(reshape2)
library(ggplot2)

source('utils.r')

df = load.csv('build/cs.csv');
df = dcast(df, name~'cs', value.var='cs', fun.aggregate=sum);
df = merge(df, df.methods, by.x='name', by.y='method', all.y=TRUE);
df[is.na(df$cs),]$cs = 0;

p = ggplot(df, aes(x=name, y=cs))+
  geom_bar(stat="identity")+
  facet_grid(.~group, space='free_x', scales="free_x")+
  theme(axis.text.x=element_text(angle=90, hjust=1, vjust=0.2),
        axis.text.y=element_text(angle=90, hjust=1),
        axis.title.x=element_text(angle=180),
        legend.box="horizontal", 
        legend.position="top", 
        legend.title=element_blank(),
        strip.text.x=element_text(angle=90))+
  labs(x="sun.misc.Unsafe methods", y = "# call sites");

save.plot(p, outfile, w=15, h=6);
