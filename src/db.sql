
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
	mdate	date			not null,	/* m */
	sha		varchar(64),				/* 1 */
	gid 	varchar(255)	not null,	/* u[0] */
	aid 	varchar(255)	not null,	/* u[1] */
	ver		varchar(128)	not null,	/* u[2] */
	sat		varchar(255)	not null,	/* u[3] */
	is0		varchar(255)	not null,	/* i[0] */
	idate	date			not null,	/* i[1] */
	size	integer			not null,	/* i[2] */
	is3		integer			not null,	/* i[3] */
	is4		integer			not null,	/* i[4] */
	is5		integer			not null,	/* i[5] */
	ext		varchar(64)		not null,	/* i[6] */
	gdesc	text,						/* n */
	adesc	text,						/* d */
	path	varchar(512)	not null,
	inrepo	boolean			not null
);

--create table dep {
--	gid 	varchar(255)	not null,
--	aid 	varchar(255)	not null,
--	ver		varchar(128)	not null,
--};
