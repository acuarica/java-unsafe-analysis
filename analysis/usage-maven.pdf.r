
library(reshape2)
library(ggplot2)
library(grid)

source('analysis/utils/utils.r')

df.memxtag = merge(df.methods, data.frame(tag=c('app', 'lang')))

df = load.csv('out/analysis/cs.csv')
df = merge(df, df.memxtag, by.x=c('name', 'tag'), by.y=c('method', 'tag'), all.x=TRUE, all.y=TRUE)
df = df[df$access=='public' & df$name != 'getUnsafe',]
df = dcast(df, name+member+group+tag~'cs', value.var='cs', fun.aggregate=sum)
df[is.na(df$cs),]$cs = 0;
df$tag = factor(df$tag, levels=c('app', 'lang'), labels=c('Application', 'Language'))

plotoverview = function(df, outfile, xlabel, ylabel, h) {
  p = ggplot(df, aes(y=name))+
    facet_grid(group~tag, scales="free_y", space = "free_y")+
    geom_bar_horz(aes(x=cs), stat="identity", position="identity", fill='#aaaaaa')+
    geom_text(aes(label=cs, x=0, hjust=0), size=4.2)+
    theme(
      text=element_text(size=15),
      strip.text.y=element_text(angle=0), 
      plot.margin = unit(c(0,0,0,0), "cm"), 
      panel.margin = unit(0.05, "cm")
    )+
    labs(x=xlabel, y=sprintf("sun.misc.Unsafe %s", ylabel))
  
  save.plot(p, outfile, w=6, h=h);
}

save.plot(NULL, outfile, w=1, h=1);
plotoverview(df[df$member=='method',], suffixfile(outfile, 'methods'), '# Call Sites', 'methods', 15.47)
plotoverview(df[df$member=='field',], suffixfile(outfile, 'fields'), '# Reads', 'fields', 4)
