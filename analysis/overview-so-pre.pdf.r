
library(reshape2)
library(ggplot2)

source('analysis/utils.r')

replacename = function(df, name, offheapdesc) {
  df[df$method == sprintf('%s (Heap)', name),]$method = name;
  df[df$method == sprintf('%s (Off-Heap)', name),]$method = name;  
  df;
}

df = df.methods;
df$method = as.character(df$method);
df = replacename(df, 'copyMemory', '(JJJ)V');
df = replacename(df, 'setMemory', '(JJB)V');
#df = replacename(df, 'getBoolean', '(J)B');
df = replacename(df, 'getByte', '(J)B');
df = replacename(df, 'getChar', '(J)C');
df = replacename(df, 'getDouble', '(J)D');
df = replacename(df, 'getFloat', '(J)F');
df = replacename(df, 'getInt', '(J)I');
df = replacename(df, 'getLong', '(J)J');
df = replacename(df, 'getShort', '(J)S');
#df = replacename(df, 'putBoolean', '(JB)V');
df = replacename(df, 'putByte', '(JB)V');
df = replacename(df, 'putChar', '(JC)V');
df = replacename(df, 'putDouble', '(JD)V');
df = replacename(df, 'putFloat', '(JF)V');
df = replacename(df, 'putInt', '(JI)V');
df = replacename(df, 'putLong', '(JJ)V');
df = replacename(df, 'putShort', '(JS)V');

df$group = as.character(df$group);
df[df$group == 'Heap Get',]$group = 'Get';
df[df$group == 'Off-Heap Get',]$group = 'Get';
df[df$group == 'Heap Put',]$group = 'Put';
df[df$group == 'Off-Heap Put',]$group = 'Put';

so = load.csv('stackoverflow/results/method-usages.csv');

colnames(so) = c('method','Usages in Questions only', 'Usages in Answers only', 'Usages in both');

so = merge(so, df, by.x='method', by.y='method', all.x=TRUE, all.y=FALSE);
so = melt(so, id=c('method', 'access', 'group', 'member'));

p = ggplot(so, aes(x=method, y=value, fill=variable))+
  geom_bar(stat="identity")+
  facet_grid(.~group, space='free_x', scales="free_x")+
  theme(axis.text.x=element_text(size=10, angle=90, hjust=1, vjust=0.2),
        axis.text.y=element_text(angle=90, hjust=1),
        #axis.title.x=element_text(angle=180),
        axis.title.x=element_blank(),
        legend.box="horizontal", 
        legend.position="none", 
        #legend.text =element_text(angle=90, vjust=0),
        #legend.text.align=
        legend.title=element_blank(),
        strip.text.x=element_text(size=11, angle=90),
        
        #panel.background=element_rect(fill='white'),
        #plot.background=element_rect(color='green'),
        #panel.border=element_rect(color='black'),
        
        panel.grid.major=element_line(),
        panel.grid.minor=element_blank()
        #axis.ticks=element_blank()
  )+
  labs(x="sun.misc.Unsafe members", y = "# matches");

#outfile = 'build/overview-pre.pdf';
save.plot(p, outfile, w=14, h=6);
