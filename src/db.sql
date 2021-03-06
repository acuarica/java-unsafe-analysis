
drop table if exists allgroups;

create table allgroups (
	value	varchar(64)	not null
);

drop table if exists rootgroups;

create table rootgroups (
	value	varchar(64)	not null
);

drop table if exists prop;

create table prop (
	key		varchar(255)	not null,
	value	text			not null
);

drop table if exists art;

create table art (
	gid 	varchar(255)	not null,	/* u[0] */
	aid 	varchar(255)	not null,	/* u[1] */
	ver		varchar(128)	not null,	/* u[2] */
	sat		varchar(255)	not null,	/* u[3] */
	is0		varchar(255)	not null,	/* i[0] == u[4] if not null */
	idate	date			not null,	/* i[1] */
	size	integer			not null,	/* i[2] */
	is3		integer			not null,	/* i[3] */
	is4		integer			not null,	/* i[4] */
	is5		integer			not null,	/* i[5] */
	ext		varchar(64)		not null,	/* i[6] */
	mdate	date			not null,	/* m */
	sha		varchar(64),				/* 1 */	
	gdesc	text,						/* n */
	adesc	text,						/* d */
	path	varchar(512)	not null,
	inrepo	boolean			not null
);

drop table if exists dep;

create table dep (
	gid 	varchar(255)	not null,
	aid 	varchar(255)	not null,
	ver		varchar(128)	not null,
	dgid 	varchar(255)	,
	daid 	varchar(255)	,
	dver	varchar(128),
	dscope	varchar(32)
);

drop table if exists class;

create table class (
	gid 	varchar(255)	not null,
	aid 	varchar(255)	not null,
	ver		varchar(128)	not null,
	clsname varchar(255)	not null,
	supername varchar(255)	not null
);

drop table if exists method;

create table method (
	gid 	varchar(255)	not null,
	aid 	varchar(255)	not null,
	ver		varchar(128)	not null,
	clsname varchar(255)	not null,
	methodname varchar(255)	not null,
	methoddesc varchar(255)	not null
);

drop table if exists callsite;

create table callsite (
	gid 	varchar(255)	not null,
	aid 	varchar(255)	not null,
	ver		varchar(128)	not null,
	clsname varchar(255)	not null,
	methodname varchar(255)	not null,
	methoddesc varchar(255)	not null,
	targetclass varchar(255)	not null,
	targetmethod varchar(255)	not null,
	targetdesc varchar(255)	not null
);

drop table if exists fieldaccess;

create table fieldaccess (
	gid 	varchar(255)	not null,
	aid 	varchar(255)	not null,
	ver		varchar(128)	not null,
	clsname varchar(255)	not null,
	methodname varchar(255)	not null,
	methoddesc varchar(255)	not null,
	targetclass varchar(255)	not null,
	targetfield varchar(255)	not null,
	targetdesc varchar(255)	not null
);

drop table if exists literal;

create table literal (
	gid 	varchar(255)	not null,
	aid 	varchar(255)	not null,
	ver		varchar(128)	not null,
	clsname varchar(255)	not null,
	methodname varchar(255)	not null,
	methoddesc varchar(255)	not null,
	literal text	not null
);
