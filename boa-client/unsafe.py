#!/usr/bin/env python

import os
import requests
import json
import collections
from pprint import pprint
import sqlite3

class Rec:
    def __init__(self):
        self.project = ""
        self.file = ""
        self.method = ""
        self.use = ""

def parseBoa(filename):
    
    def parseLine(line):
        import re
    
        m = re.search('projectsWithUnsafe\[(.+)\]\[(.+)\]\[(.+)\]\[(.+)\] = 1', line)
        
        r = Rec()
        
        r.project = m.group(1)
        r.file = m.group(2)
        r.method = m.group(3)
        r.use = m.group(4)
        
        return r
    
    with open(filename, 'r') as f:
        for line in f:
            r = parseLine(line)
            
            yield r

def buildDb(filename):
    db = sqlite3.connect(':memory:')
    cur = db.cursor()
    db.execute("CREATE TABLE boa (project text, file text, method text, use text)")
    
    for r in parseBoa(filename):
        db.execute("INSERT INTO boa VALUES (?, ?, ?, ?)", (r.project, r.file, r.method, r.use))

    db.commit()

    return db

def select(query, db):
    for row in db.execute(query):
        print row

def main():
    def parseargs():
        import argparse
        parser = argparse.ArgumentParser(description='Download repos.')
        parser.add_argument('repocount', metavar='repocount', default=5, type=int, nargs='?', help='How many repos to download.')
        return parser.parse_args()
    
    filename = 'unsafe.boa-7751.out'
    
    #args = parseargs()

    db = buildDb(filename)
    
    print '# uses'
    select('SELECT count(*) FROM boa', db)
    
    print '# uses per Unsafe method'
    select('SELECT use, count(*) FROM boa group BY use ORDER BY count(*) DESC', db)
    
    print 'How many projects use Unsafe'        
    select('SELECT count(*) FROM (SELECT project FROM boa group BY project)', db)
    
    print '# uses per project'        
    select('SELECT project, count(*) FROM boa group BY project ORDER BY count(*) DESC', db)        

if __name__ == '__main__':
    main()
