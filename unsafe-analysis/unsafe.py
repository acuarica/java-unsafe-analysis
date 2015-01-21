#!/usr/bin/env python

class Rec:
    def __init__(self):
        self.kind = ''
        self.id = ''
        self.name = ''
        self.description = ''
        self.project = ''
        self.file = ''
        self.method = ''
        self.use = ''
        self.value = 0

def parseBoa(filename):
    def parseLine(line):
        import re
    
        r = Rec()

        if line.startswith('counts'):
            m = re.search('(\w+)\[\] = (.+)', line)
            
            r.kind = m.group(1)
            r.id = ''
            r.name = ''
            r.description = ''
            r.project = ''
            r.file = ''
            r.method = ''
            r.use = ''
            r.value = m.group(2)
        else:
            def getid(url):
                m = re.search('http://sourceforge.net/projects/(\w+)', url)
                return m.group(1)
            
            m = re.search('(\w+)\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\] = 1', line)
            
            url = m.group(4)
            
            r.kind = m.group(1)
            r.id = getid(url)
            r.name = m.group(2)
            r.description = m.group(3)
            r.project = url
            r.file = m.group(5)
            r.method = m.group(6)
            r.use = m.group(7)
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
            w.writerow([r.kind, r.id, r.name, r.description, r.project, r.file, r.method, r.use, r.value])

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
