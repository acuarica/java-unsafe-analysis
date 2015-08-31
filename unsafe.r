
library ("RSQLite")
library ("ggplot2")

db = dbConnect(SQLite(), "unsafe-stage.sqlite3")
dbDisconnect(db)

dbListTables(db)

dbGetQuery(db, "
select * from unsafe
");

dbSendQuery(db, "attach '../mavends/out/mavenindex.sqlite3' as mi");

df = dbGetQuery(db, "select count(*) from artifact_jar")
df = dbGetQuery(db, "select u.coorid, a.idate, a.mdate from (select distinct coorid from unsafe) u inner join mi.artifact_jar a on a.coorid = u.coorid")
df = dbGetQuery(db, "select a.coorid, a.idate, a.mdate from artifact_jar a order by idate asc limit 100000")
df$a.idate = as.Date( df$a.idate )
df$a.mdate = as.Date( df$a.mdate )

ggplot(df, aes(x=a.mdate))+geom_bar()
ggplot(df, aes(x=a.idate))+geom_bar()

ggplot(df)+geom_bar(aes(x=a.idate, fill='idate'))+geom_bar(aes(x=a.mdate, fill='mdate'))


dbGetQuery(db, "
with 
  unscoords as (select distinct coorid from unsafe),
  unsarts as (select distinct a.groupid || ':' ||a.artifactid from unscoords t inner join mi.artifact_jar a on a.coorid = t.coorid)
--  select * from unsarts;
  select coorid in unscoords, * from mi.artifact_jar a where groupid || ':' || artifactid in unsarts order by groupid, artifactid, version;           
")


dbGetQuery(db, "
with 
  unscoords as (select distinct coorid from unsafe),
  unsarts as (select distinct a.groupid || ':' ||a.artifactid from unscoords t inner join artifact_jar a on a.coorid = t.coorid)
--  select * from unsarts;
  select coorid in unscoords, * from artifact_jar a where groupid || ':' || artifactid in unsarts order by groupid, artifactid, version;           
")



