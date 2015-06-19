
library(reshape2)
library(ggplot2)
library(grid)

source('analysis/utils/utils.r')

replacename = function(df, name, offheapdesc) {
  df[df$method == sprintf('%s (Heap)', name),]$method = name;
  df[df$method == sprintf('%s (Off-Heap)', name),]$method = name;  
  df;
}

df = df.methods;
df$method = as.character(df$method);
df = replacename(df, 'copyMemory', '(JJJ)V');
df = replacename(df, 'setMemory', '(JJB)V');
df = replacename(df, 'getByte', '(J)B');
df = replacename(df, 'getChar', '(J)C');
df = replacename(df, 'getDouble', '(J)D');
df = replacename(df, 'getFloat', '(J)F');
df = replacename(df, 'getInt', '(J)I');
df = replacename(df, 'getLong', '(J)J');
df = replacename(df, 'getShort', '(J)S');
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
colnames(so) = c('method','Only in Questions', 'Only in Answers', 'Both');
so = merge(so, df, by.x='method', by.y='method', all.x=TRUE, all.y=FALSE);
so = melt(so, id=c('method', 'access', 'group', 'member'));
so = so[so$value > 0, ];

p = ggplot(so, aes(x=value, y=method, fill=variable))+
  geom_bar_horz(stat="identity", position="identity")+
  facet_grid(group~., space='free_y', scales="free_y")+
  theme(
    text=element_text(size=16),
    plot.margin = unit(c(0,0,0,0), "cm"), 
    panel.margin = unit(0.05, "cm"),
    legend.position="top",
    legend.text=element_text(size=12),
    legend.title=element_blank(),
    strip.text.y=element_text(angle=0)
  )+
  labs(x="# matches", y = "sun.misc.Unsafe members")

save.plot(p, outfile, w=6, h=14);
