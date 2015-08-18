--select count(*) from (select distinct gid, aid, ver from dep )
/*
WITH RECURSIVE d(gid, aid, depth
--, path, cycle
) AS (
    values ('de.berlios.jsunit', 'jsunit', 0--, 'de.berlios.jsunit' || ':' || 'jsunit', 0
    )
  UNION 
	select dep.dgid, dep.daid, d.depth + 1 --, 
--		d.path || ',' || (dep.dgid || ':' || dep.daid),
--		instr(path, dep.dgid || ':' || dep.daid)
	from dep, d 
	where dep.gid=d.gid and dep.aid=d.aid --and not cycle
)
SELECT * FROM d*/



--select sum(size)/(1024*1024*1024) from art where sat ='NA' and  ext in ( 'jar' ,  'ejb', 'war', 'ear' )
--select t.ext, t.c from (select ext, count(*) c from art group by ext) t order by t.c desc
--select * from (select gid, aid, max(cver), count(ver) c from art where sat = 'NA' group by gid, aid) t order by c desc
--select gid, aid, max(cver), count(ver) from art where sat ='NA' and  ext in ( 'jar' ,  'ejb', 'war', 'ear' ) group by gid, aid

--select distinct ver, length(ver) from art order by length(ver) desc
-- jar war aar (android) car ear kar xar nar sar mar har ejb (no ejb files)

--select * from art limit 1000
--select * from art where ext != is0

--select count(*) from (select distinct gid, aid, ver from callsite)
--select count(*) from callsite

--select * from class limit 1000

--select * from method limit 1000
--select count(*) from art



/*WITH RECURSIVE d(gid, aid, depth --, path, cycle
) AS (
    values ('de.berlios.jsunit', 'jsunit', 0--, 'de.berlios.jsunit' || ':' || 'jsunit', 0
    )
  UNION 
	select dep.dgid, dep.daid, d.depth + 1 --, 
--		d.path || ',' || (dep.dgid || ':' || dep.daid),
--		instr(path, dep.dgid || ':' || dep.daid)
	from dep, d 
	where dep.gid=d.gid and dep.aid=d.aid --and not cycle
)
SELECT * FROM d*/

/*
WITH RECURSIVE dept(gid, aid, level, path, cycle) AS (
select "br.com.objectos", "sitebricks", 0, "root", 0
UNION 
select dep.dgid, dep.daid, dept.level+1, 
dept.path || ' -> '|| dep.dgid || ':'|| dep.daid, 
instr(dept.path, dep.dgid || ':' || dep.daid)
from dep, dept where dep.gid=dept.gid and dep.aid=dept.aid and cycle=0
order by dept.level+1 desc
)
SELECT * FROM dept;
*/
--select * from dep where gid="ch.qos.logback" and aid="logback-classic" and ver="1.1.2"
--create index dep_id on dep (gid, aid);



--select * from class where supername='java/lang/Throwable'
/*WITH RECURSIVE d(cls, level, path, cycle) AS (
select "java/lang/Object", 0, "root", 0
UNION 
select class.clsname, d.level+1, d.path || ': '|| d.cls, instr(d.path, class.clsname)
from class, d where class.supername=d.cls and cycle=0
order by d.level+1 desc
)
SELECT * FROM d;*/

--select distinct supername from class

WITH RECURSIVE d(root, cls) AS (
--select "java/lang/Object", "java/lang/Object"
select distinct supername, supername from class
UNION 
select root, class.clsname from class, d where class.supername=d.cls )
SELECT root, count(*) FROM d group by root order by count(*) desc;

--create index class_super on class (supername);