#!/usr/bin/env python
from fileinput import filename

class Rec:
    def __init__(self):
        self.kind = ''
        self.repo = ''
        self.rev = ''
        self.id = ''
        self.name = ''
        self.description = ''
        self.project = ''
        self.file = ''
        self.nsname = ''
        self.clsname = ''
        self.method = ''
        self.use = ''
        self.revs = 0
        self.start = None
        self.end = None
        self.asts = 0
        self.value = 0

def parseBoa(filename):
    def parseLine(line):
        import re
    
        r = Rec()

        if line.startswith('counts'):
            m = re.search('(\w+)\[\] = (.+)', line)
            
            r.kind = m.group(1)
            r.repo = ''
            r.rev = ''
            r.id = ''
            r.name = ''
            r.description = ''
            r.project = ''
            r.file = ''
            r.nsname = ''
            r.clsname = ''
            r.method = ''
            r.use = ''
            r.revs = 0
            r.start = None
            r.end = None
            r.asts = 0
            r.value = m.group(2)
        else:
            def getid(url):
                m = re.search('http://sourceforge.net/projects/(\w+)', url)
                return m.group(1)
            
            def getclassname(filepath):
                import os, re
                
                filename = os.path.basename(filepath)
                                    
                m = re.search('(\w+)\.java', filename)
                clsname = m.group(1)
                
                return clsname
            
            m = re.search('(\w+)\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.*)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\] = 1', line)
            
            url = m.group(6)
            
            r.kind = m.group(1)
            r.repo = m.group(2)
            r.rev = m.group(3)
            r.id = getid(url)
            r.name = m.group(4)
            r.description = m.group(5)
            r.project = url
            r.file = m.group(7)
            r.nsname = m.group(8)
            r.clsname = getclassname(r.file)
            r.method = m.group(9)
            r.use = m.group(10)
            r.revs = m.group(11)
            r.start = m.group(12)
            r.end = m.group(13)
            r.asts = m.group(14)
            r.value = 1
        
        return r
    
    with open(filename, 'r') as f:
        for line in f:
            r = parseLine(line)
            
            yield r

def buildCsv(input, output):        
    import csv

    with open(output, 'wb') as csvfile:
        w = csv.writer(csvfile, delimiter=',', quotechar='"', quoting=csv.QUOTE_NONNUMERIC)
        for r in parseBoa(input):
            w.writerow([r.kind, r.repo, r.rev, r.id, r.name, r.description, r.project, r.file, r.nsname, r.clsname, r.method, r.use, r.revs, r.start, r.end, r.asts, r.value])

def main():
    def parseargs():
        import argparse
        parser = argparse.ArgumentParser(description='Parse BOA output.')
        parser.add_argument('input', metavar='input', help='input file name.')
        parser.add_argument('output', metavar='output', help='output file name.')
        return parser.parse_args()
    
    args = parseargs()

    buildCsv(args.input, args.output)
    
if __name__ == '__main__':
    main()