#!/usr/bin/env python

def progress(gen, step, out, text='.'):
    i = 0

    for item in gen:
        yield item
        i += 1

        if (i % step == 0):
            out(text)

    out('\n')

class IndexFile:
    def __init__(self, indexfile):
        self.indexfile = indexfile

        with open(indexfile, 'rb') as f:
            import struct
            from datetime import datetime
        
            f.read(1)

            self.indexdate = datetime.fromtimestamp(long(struct.unpack('>Q', f.read(8))[0]) / 1000)

    def parse(self):
        with open(self.indexfile, 'rb') as f:
            import struct
            from datetime import datetime

            f.read(1)

            indexdate = datetime.fromtimestamp(long(struct.unpack('>Q', f.read(8))[0]) / 1000)

            while True:
                buf = f.read(4)
                if not buf:
                    break

                fieldcount = struct.unpack('>I', buf)[0]

                nr = {}

                for i in range(fieldcount):
                    f.read(1)

                    keylen = struct.unpack('>H', f.read(2))[0]
                    key = struct.unpack(str(keylen) + 's', f.read(keylen))[0]

                    valuelen = struct.unpack('>I', f.read(4))[0]
                    value = struct.unpack(str(valuelen) + 's', f.read(valuelen))[0]

                    nr[key] = value

                yield nr

    def check(self):
        import re, sys, datetime

        def issha1(one):
            return one != None and re.match("[0-9a-f]{40}", one.lower())

        self.recordCount = 0
        self.rg = None
        self.mmindate = datetime.datetime.max
        self.mmaxdate = datetime.datetime.min

        for nr in progress(self.parse(), 50000, lambda text: sys.stderr.write(text) or sys.stderr.flush()):
            self.recordCount += 1

            allGroups = nr.get('allGroups')
            allGroupsList = nr.get("allGroupsList")
            rootGroups = nr.get("rootGroups")
            rootGroupsList = nr.get("rootGroupsList")
            descriptor = nr.get("DESCRIPTOR")
            idxinfo = nr.get("IDXINFO")
            one = nr.get("1")
            m = nr.get("m")
            i = nr.get("i")
            u = nr.get("u")
            n = nr.get("n")
            d = nr.get("d")
            delete = nr.get('del')

            assert (allGroups == None) == (allGroupsList == None), "Invalid allGroups: %s" % nr
            assert allGroups == None or allGroups == 'allGroups', 'allGroups => "allGroups": %s' % nr

            assert (rootGroups == None) == (rootGroupsList == None), "Invalid rootGroups: %s" % nr
            assert rootGroups == None or rootGroups == "rootGroups", "rootGroups => 'rootGroups': %s" % nr
            assert rootGroups == None or self.rg == None, "rootGroupsList already set";

            assert (descriptor == None) == (idxinfo == None), "Invalid DESCRIPTOR/IDXINFO: %s" % nr
            assert descriptor == None or descriptor == 'NexusIndex', "Invalid DESCRIPTOR/NexusIndex: %s" % nr

            assert ((allGroups == None) and (rootGroups == None) and (descriptor == None)) == (m != None), "Invalid m: %s" % nr

            assert (one == None) or (m != None), "one => m: %s" % nr
            assert (i == None) or (m != None), "i => m: %s" % nr
            assert (u == None) or (m != None), "u => m: %s" % nr
            assert (delete == None) or (m != None), "del => m: %s" % nr

            assert (u != None) == (i != None), "u <=> i: %s" % nr
            assert (m == None) or ((u != None) == (delete == None)), "m: u <=> !del: %s" % nr
            assert m == None or re.match("[0-9]{13}", m), "m is not a valid date: %s" % nr

            if m != None:
                mdate = datetime.datetime.fromtimestamp(long(m) / 1000)
                self.mmindate = min(self.mmindate, mdate)
                self.mmaxdate = max(self.mmaxdate, mdate)

            if i != None:
                ifs = i.split("|")
                assert len(ifs) == 7, "Invalid i: %s" % nr

                itype, idatetext, isizetext, iv1, iv2, iv3, iext = ifs
                
                assert re.match("[0-9]{13}", idatetext), "Invalid i.date: %s" % nr
                assert re.match("-?[0-9]+", isizetext), "Invalid i.size: %s" % nr
                assert re.match("[0-9]", iv1), "Invalid i.v1: %s" % nr
                assert re.match("[0-9]", iv2), "Invalid i.v2: %s" % nr
                assert re.match("[0-9]", iv2), "Invalid i.v3: %s" % nr

                ufs = u.split("|")
                assert len(ufs) == 4 or len(ufs) == 5, "Invalid value for u field: %s" % nr
                
                one = one if issha1(one) else "<nosha1>"
                yield ufs, one, iext
                    
            if rootGroups != None:
                self.rg = rootGroupsList.split('|')

def pg():
    import psycopg2

    try:
        conn = psycopg2.connect("dbname='maven' user='luigi' host='localhost' password=''")
    except:
        print "I am unable to connect to the database"

    cur = conn.cursor()
    cur.execute("drop table arts")
    cur.execute("""
        CREATE TABLE arts (
        gid  varchar(255) NOT NULL,
        aid  varchar(255) NOT NULL,
        version varchar(255),
        sat varchar(255), 
        ext varchar(64), 
        one varchar(64), 
        path varchar(512),
        inrepo boolean
    )
    """)

    cur.execute("drop table deps")
    cur.execute("""
        CREATE TABLE deps (
        gid  varchar(255) NOT NULL,
        aid  varchar(255) NOT NULL,
        ver varchar(255),
        dgid  varchar(255) ,
        daid  varchar(255) ,
        dver varchar(255),
        dscope varchar(255)
    )
    """)

    conn.commit()

    #print "Holaaa!!!"

def inrepo(artpath, one):
    import os.path
    relpath = 'db/repo/' + artpath
    return os.path.exists(relpath)

def extractdeps(path):
    import xml.dom.minidom

    def getvalue(elemname):
        elem = dep.getElementsByTagName(elemname)
        return elem[0].firstChild.nodeValue if len(elem) > 0 and elem[0].firstChild != None else None

    try:
        proj = xml.dom.minidom.parse(path)
        deps = proj.getElementsByTagName('dependencies')

        if len(deps) > 0:
            for dep in deps[0].getElementsByTagName('dependency'):
                dgid = getvalue('groupId')
                daid = getvalue('artifactId')
                dver = getvalue('version')
                scope = getvalue('scope')
                
                yield (dgid, daid, dver, scope)
    except Exception as err:
        print path, err

def main():
    def buildindex(indexfile):
        import psycopg2

        i = IndexFile(indexfile)

        conn = psycopg2.connect("dbname='maven' user='luigi' host='localhost' password=''")
        cur = conn.cursor()
        
        j = 0

        with open('db/index.list', 'w') as f:
            for ufs, one, iext in i.check():
                if len(ufs) == 4:
                    groupid, artifactid, version, kind = ufs

                    path = '{0}/{1}/{2}/{1}-{2}.{3}'.format(groupid.replace('.', '/'), artifactid, version, iext)
                    line = path + ' ' + one
                    f.write(line + '\n')                    
                    cur.execute("INSERT INTO arts (gid, aid, version, sat, ext, one, path, inrepo) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", (groupid, artifactid, version, '', iext, one, path, inrepo(path, one) ) )

                    path = '{0}/{1}/{2}/{1}-{2}.{3}'.format(groupid.replace('.', '/'), artifactid, version, 'pom')
                    line = path + ' ' + one
                    f.write(line + '\n')
                    ir = inrepo(path, one)
                    cur.execute("INSERT INTO arts (gid, aid, version, sat, ext, one, path, inrepo) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", (groupid, artifactid, version, '', 'pom', one, path, ir) )
                    if ir:
                        for (dgid, daid, dver, scope) in extractdeps('db/repo/' + path):
                            cur.execute("INSERT INTO deps (gid, aid, ver, dgid, daid, dver, dscope) VALUES (%s, %s, %s, %s, %s, %s, %s)", (groupid, artifactid, version, dgid, daid, dver, scope) )

                    #   j += 1
                    #if j == 2000:
                     #   break

                if len(ufs) == 5:
                    groupid, artifactid, version, satellite, ext = ufs
 
                    path = '{0}/{1}/{2}/{1}-{2}-{3}.{4}'.format(groupid.replace('.', '/'), artifactid, version, satellite, ext)
                    line = path + ' ' + one
                    f.write(line + '\n')
                    cur.execute("INSERT INTO arts (gid, aid, version, sat, ext, one, path, inrepo) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", (groupid, artifactid, version, satellite, ext, one, path, inrepo(path, one)) )

        conn.commit()

        with open('db/stats.txt', 'w') as f:
            f.write('Nexus Index Timestamp: %s\n' % i.indexdate)
            f.write('Record count: %d\n' % i.recordCount)
            f.write('mmindate: %s\n' % i.mmindate)
            f.write('mmaxdate: %s\n' % i.mmaxdate)
            f.write('mmaxdate: %s\n' % i.rg)

    pg()
    buildindex('db/nexus-maven-repository-index')

if __name__ == '__main__':
    main()
