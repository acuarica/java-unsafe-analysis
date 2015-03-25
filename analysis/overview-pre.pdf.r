
library(reshape2)
library(ggplot2)

source('utils.r')

replacefield = function(df, fname, toname) {
    df[df$name == fname,]$name = toname;
    df;
}

csv = load.csv('build/cs.csv');
csv = merge(csv, df.methods, by.x='name', by.y='method', all.x=TRUE, all.y=TRUE);

plotoverview = function(df, outfile, ylabel='# Call Sites', w=15.47) {
  df = dcast(df, name+group~'cs', value.var='cs', fun.aggregate=sum);
  #df = merge(df, df.methods, by.x='name', by.y='method', all.x=TRUE, all.y=TRUE);
  df[is.na(df$cs),]$cs = 0;

  df$hjust = ifelse(df$cs > 4000, ifelse(df$cs > 4032, 0.7, 0.25), -0.2);
  df$vjust = ifelse(df$cs > 4000, 1.6,  0.5);
  
  if (nrow(df[df$name == 'ARRAY_BYTE_BASE_OFFSET',]) > 0) {
    df[df$name == 'ARRAY_BYTE_BASE_OFFSET',]$hjust = 0.8;
    df[df$name == 'ARRAY_BYTE_BASE_OFFSET',]$vjust = 1.8;
  }
  
  p = ggplot(df, aes(x=name, y=cs, label=cs))+
    geom_bar(stat="identity")+
    facet_grid(.~group, space='free_x', scales="free_x")+
    geom_text(aes(hjust=hjust, vjust=vjust), angle=90, color='#444444', size=4)+
    theme(axis.text.x=element_text(size=12, angle=90, hjust=1, vjust=0.2),
          axis.text.y=element_text(angle=90, hjust=1),
          #axis.title.x=element_text(angle=180),
          axis.title.x=element_blank(),
          legend.box="horizontal", 
          legend.position="top", 
          legend.title=element_blank(),
          strip.text.x=element_text(size=11, angle=90),
          
          #panel.background=element_rect(fill='white'),
            #plot.background=element_rect(color='green'),
            #panel.border=element_rect(color='black'),
          
          panel.grid.major=element_line(),
          panel.grid.minor=element_blank()
          #axis.ticks=element_blank()
          )+
    labs(x="sun.misc.Unsafe members", y=ylabel);
  
  save.plot(p, outfile, w=w, h=6);
}

df = csv;
#plotoverview(df, suffixfile(outfile, 'all'));

df = csv;
df = df[df$access=='public' & df$name != 'getUnsafe',];
plotoverview(df[df$member=='method',], outfile);
plotoverview(df[df$member=='field',], suffixfile(outfile, 'field'), ylabel='# Reads', w=4);


df = csv;
df$name = as.character(df$name);
df = replacefield(df, 'ADDRESS_SIZE', 'addressSize');
df = replacefield(df, 'ARRAY_BOOLEAN_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_BOOLEAN_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_BYTE_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_BYTE_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_CHAR_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_CHAR_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_DOUBLE_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_DOUBLE_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_FLOAT_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_FLOAT_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_INT_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_INT_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_LONG_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_LONG_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_OBJECT_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_OBJECT_INDEX_SCALE', 'arrayIndexScale');
df = replacefield(df, 'ARRAY_SHORT_BASE_OFFSET', 'arrayBaseOffset');
df = replacefield(df, 'ARRAY_SHORT_INDEX_SCALE', 'arrayIndexScale');
df$name = factor(df$name);

exclude = c(
  'getUnsafe', 
'ADDRESS_SIZE', 
'ARRAY_BOOLEAN_BASE_OFFSET', 
'ARRAY_BOOLEAN_INDEX_SCALE', 
'ARRAY_BYTE_BASE_OFFSET', 
'ARRAY_BYTE_INDEX_SCALE', 
'ARRAY_CHAR_BASE_OFFSET', 
'ARRAY_CHAR_INDEX_SCALE', 
'ARRAY_DOUBLE_BASE_OFFSET', 
'ARRAY_DOUBLE_INDEX_SCALE', 
'ARRAY_FLOAT_BASE_OFFSET', 
'ARRAY_FLOAT_INDEX_SCALE', 
'ARRAY_INT_BASE_OFFSET', 
'ARRAY_INT_INDEX_SCALE', 
'ARRAY_LONG_BASE_OFFSET', 
'ARRAY_LONG_INDEX_SCALE', 
'ARRAY_OBJECT_BASE_OFFSET', 
'ARRAY_OBJECT_INDEX_SCALE', 
'ARRAY_SHORT_BASE_OFFSET', 
'ARRAY_SHORT_INDEX_SCALE'
);

df = df[df$access=='public' & !(df$name %in% exclude),];
#plotoverview(df, suffixfile(outfile, 'nofields'));
