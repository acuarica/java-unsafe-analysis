--select sum(size)/(1024*1024*1024) from art where sat ='NA' and  ext in ( 'jar' ,  'ejb', 'war', 'ear' )
--select t.ext, t.c from (select ext, count(*) c from art group by ext) t order by t.c desc
--select * from (select gid, aid, max(cver), count(ver) c from art where sat = 'NA' group by gid, aid) t order by c desc
--select gid, aid, max(cver), count(ver) from art where sat ='NA' and  ext in ( 'jar' ,  'ejb', 'war', 'ear' ) group by gid, aid

--select distinct ver, length(ver) from art order by length(ver) desc
-- jar war aar (android) car ear kar xar nar sar mar har ejb (no ejb files)

--select * from art limit 1000
select * from art where ext != is0