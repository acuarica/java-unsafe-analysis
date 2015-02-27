#!/usr/bin/env python

def main():
    import sys

    from pprint import pprint

    sg = set()
    with open('subgraph.csv', 'r') as f:
        for line in f:
            node, count, rank = [cell.strip().replace('"', '') for cell in line.split(',')]
            sg.add(node)

    text = """
digraph depgraph {
    rankdir=LR;
"""

    sys.stdout.write(text)

    for line in sys.stdin:
        row = [cell.strip() for cell in line.split(',')]
        if (len(row) == 6):
            groupid, artifactid, depgroupid, departifactid, depversion, depscope = row

            key = '%s:%s' % (groupid, artifactid)
            depkey = '%s:%s' % (depgroupid, departifactid)

            #if key in sg or depkey in sg:
            if key in sg:
                sys.stdout.write('  "%s" -> "%s;"\n' % (key, depkey))

    sys.stdout.write("}\n")
    
if __name__ == '__main__':
    main()
