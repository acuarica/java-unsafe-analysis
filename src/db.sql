
DROP TABLE IF EXISTS arts;

CREATE TABLE arts (
	gid  varchar(255) NOT NULL,
	aid  varchar(255) NOT NULL,
	version varchar(255),
	sat varchar(255), 
	ext varchar(64), 
	one varchar(64), 
	path varchar(512),
	inrepo boolean
);
