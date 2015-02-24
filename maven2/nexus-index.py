#!/usr/bin/env python

def progress(gen, step, out, text='.'):
    i = 0

    for item in gen:
        yield item
        i += 1

        if (i % step == 0):
            out(text)

    out('\n')

def parseindex(indexfile):
    import struct
    from datetime import datetime

    with open(indexfile, 'rb') as f:
        f.read(1)

        indexdate = datetime.fromtimestamp(long(struct.unpack('>Q', f.read(8))[0]) / 1000)

        #print 'Nexus Index Timestamp:', indexdate, ' ',

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

def printindex(indexfile):
    for nr in parseindex(indexfile):
        print nr

def checkindex(indexfile):
    import re, sys, datetime

    def issha1(one):
        return one != None and re.match("[0-9a-f]{40}", one.lower())

    recordCount = 0
    rg = None
    mmindate = datetime.datetime.max
    mmaxdate = datetime.datetime.min

    for nr in progress(parseindex(indexfile), 20000, lambda text: sys.stderr.write(text) or sys.stderr.flush()):
        recordCount += 1

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
        assert rootGroups == None or rg == None, "rootGroupsList already set";

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
            mmindate = min(mmindate, mdate)
            mmaxdate = max(mmaxdate, mdate)

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
            
            if len(ufs) == 4 and iext == 'jar' and issha1(one):               
                groupid, artifactid, version, kind = ufs
                path = '{0}/{1}/{2}/{1}-{2}.{3} {4}'.format(groupid.replace('.', '/'), artifactid, version, iext, one)
                print path 
                
                #'http://mirrors.ibiblio.org/maven2/

        if rootGroups != None:
            rg = rootGroupsList.split('|')

   # print 'Record count: %d' % recordCount
   # print 'mmindate: %s' % mmindate
   # print 'mmaxdate: %s' % mmaxdate
   # print 'rootGroupsList: %s' % rg

def buildindex(indexfile):
    pass

def main():
    def parseargs(indexfile):
        import argparse
        
        parser = argparse.ArgumentParser(description='Nexus index checker and parser')
        group = parser.add_mutually_exclusive_group(required=False)
        group.add_argument('--build', dest='build', action='store_const', const=buildindex, default=checkindex, help='build index filter (default: only check)')
        group.add_argument('--print', dest='build', action='store_const', const=printindex, default=checkindex, help='print index filter (default: only check)')
        parser.add_argument('indexfile', metavar='indexfile', nargs='?', default=indexfile, help='index file name (default: %s)' % indexfile)
        
        return parser.parse_args()
    
    args = parseargs('db/index/nexus-maven-repository-index')

    args.build(args.indexfile)
    
if __name__ == '__main__':
    main()
