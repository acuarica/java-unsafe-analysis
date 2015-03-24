
df.prod = load.csv('build/deps-prod.csv');
#length(unique(df.prod$depId))
unsart = length(unique(df.prod$depCount))

#df.all = load.csv('build/deps-all.csv');
#length(unique(df.all$depId))
#unsart = length(unique(df.all$depCount));

(unsart / 75405) * 100
