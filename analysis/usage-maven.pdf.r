
library(reshape2)
library(ggplot2)
library(grid)

source('analysis/utils/utils.r')

df.memxtag = merge(df.methods, data.frame(tag=c('app', 'lang')))

df = load.csv('out/analysis/cs.csv')
df = merge(df, df.memxtag, by.x=c('name', 'tag'), by.y=c('method', 'tag'), all.x=TRUE, all.y=TRUE)
df = df[df$access=='public' & df$name != 'getUnsafe',]
df = dcast(df, name+member+group+tag~'cs', value.var='cs', fun.aggregate=sum)
df[is.na(df$cs),]$cs = 0

df.text = dcast(df, name+member+group~tag, value.var='cs', fun.aggregate=sum)
df.text$text = paste(df.text$app, '/', df.text$lang)
df.text$app = NULL
df.text$lang = NULL

df = merge(df, df.text, by=c('name', 'member', 'group'))

df$tag = factor(df$tag, levels=c('app', 'lang'), labels=c('Application  / ', 'Language'))

plotoverview = function(df, outfile, xlabel, ylabel, h, l) {
  p = ggplot(df, aes(x=cs, y=name, fill=tag))+
    facet_grid(group~., scales="free_y", space = "free_y")+
    geom_bar_horz(stat="identity", position="identity")+
    scale_x_continuous(limits = c(0,l))+
    scale_fill_grey(start = 0.6, end = 0.0)+
    geom_text(data=df[df$tag=='Application  / ',], aes(label=text, hjust=-0.05), size=3.75)+
    theme_bw()+
    theme(
      text=element_text(size=15),
      strip.text.y=element_text(angle=0), 
      legend.position="top",
      legend.title=element_blank(),
      plot.margin = unit(c(0,0,0,0), "cm"), 
      panel.margin = unit(0.05, "cm")
    )+
    labs(x=xlabel, y=sprintf("sun.misc.Unsafe %s", ylabel))
  
  save.plot(p, outfile, w=6, h=h);
}

save.plot(NULL, outfile, w=1, h=1);
plotoverview(df[df$member=='method',], suffixfile(outfile, 'methods'), '# Call Sites', 'methods', 15.47, 7000)
plotoverview(df[df$member=='field',], suffixfile(outfile, 'fields'), '# Reads', 'fields', 6, 85)
