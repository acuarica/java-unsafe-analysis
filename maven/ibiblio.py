#!/usr/bin/env python

from __future__ import print_function

def fetchdirlist(dirurl, retries, log):
	"""Fetches directory list from url, parsing HTML table."""

	import requests

	log("Fetching directory list from '" + dirurl + "'")

	for i in range(retries):
		r = requests.get(dirurl)

		if (r.status_code == 200):
			break

		secs = 5 * (i+1)
		log("**** Retry no. {0} due to status code {1} on '{2}' (waiting {3} secs) ****".format(i+1, r.status_code, dirurl, secs))
		import time
		time.sleep(secs)

	xmltext = r.text

	b = xmltext.index("<table summary='Directory Listing'")
	e = xmltext.index("</table>")
	xmltext = xmltext[b:e+8]

	xmltext = '<?xml version="1.1" ?><!DOCTYPE naughtyxml [<!ENTITY nbsp "&#0160;"><!ENTITY copy "&#0169;">]>' + xmltext

	from xml.etree import ElementTree as ET

	xml = ET.XML(xmltext)
	links = xml.findall('./tbody/tr/td/a')

	for link in links:
		s = link.attrib['href']
		if s != '../':
			yield (dirurl, s)

def fetchdirlistrec(rootdirurl, retries, log):
	"""Fetches directory list recursively starting from rootdirurl, parsing HTML table."""

	dl = fetchdirlist(rootdirurl, retries, log)
	for f in dl:
		if f[1].endswith('/'):
			for g in fetchdirlistrec(f[0] + f[1], retries, log):
				yield g
		else:
			yield f

def main():
	def parseargs(root, dl, dlrec, retries):
		import argparse

		parser = argparse.ArgumentParser(description='Fetches directory list.')
		parser.add_argument('root', metavar='root', default=root, nargs='?', help='root to start fetching directory list.')
		parser.add_argument('-r', '--recursive', dest='fetchdl', action='store_const', default=dl, const=dlrec, help='fetches directory list recursively.')
		parser.add_argument('-t', dest='retries', metavar='retries', type=int, default=retries, help='maximum number of retries.')
		return parser.parse_args()

	def log(message):
		import sys
		sys.stderr.write('[ ')
		sys.stderr.write(message)
		sys.stderr.write(' ]\n')

	root = 'http://mirrors.ibiblio.org/maven2/'
	retries = 5

	args = parseargs(root, fetchdirlist, fetchdirlistrec, retries)

	dl = args.fetchdl(args.root, args.retries, log)

	for f in dl:
		print(f[0] + f[1])

if __name__ == '__main__':
	main()
