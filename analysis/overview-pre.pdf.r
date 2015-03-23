
library(reshape2)
library(ggplot2)

source('utils.r')

replacefield = function(df, fname, toname) {
    df[df$name == fname,]$name = toname;
    df;
}

csv = load.csv('build/cs.csv');
csv = merge(csv, df.methods, by.x='name', by.y='method', all.x=TRUE, all.y=TRUE);

plotoverview = function(df, outfile) {
  df = dcast(df, name+group~'cs', value.var='cs', fun.aggregate=sum);
  #df = merge(df, df.methods, by.x='name', by.y='method', all.x=TRUE, all.y=TRUE);
  df[is.na(df$cs),]$cs = 0;

  p = ggplot(df, aes(x=name, y=cs))+
    geom_bar(stat="identity")+
    facet_grid(.~group, space='free_x', scales="free_x")+
    theme(axis.text.x=element_text(size=10, angle=90, hjust=1, vjust=0.2),
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
    labs(x="sun.misc.Unsafe members", y = "# call sites/reads");
  
  save.plot(p, outfile, w=15.47, h=6);
}

df = csv;
plotoverview(df, suffixfile(outfile, 'all'));

df = csv;
df = df[df$access=='public' & df$name != 'getUnsafe',];
plotoverview(df, outfile);

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
plotoverview(df, suffixfile(outfile, 'nofields'));
