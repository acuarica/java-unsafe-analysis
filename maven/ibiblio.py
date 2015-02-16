#!/usr/bin/env python

from __future__ import print_function

def fetchdirlist(dirurl, retries, log):
	"""Fetches directory list from url, parsing HTML table."""

	def request(dirurl, retries, log):
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

		return r.text

	xmltext = request(dirurl, retries, log)

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
			yield s

def fetchdirlistrec(rootdirurl, retries, log):
	"""Fetches directory list recursively starting from rootdirurl, parsing HTML table."""

	lsfname = 'build/' + rootdirurl.replace(':', '_c_').replace('/', '_') + '.list'

	import os
	
	if os.path.isfile(lsfname):
		log('Directory ' + rootdirurl + ' already in database index.')
		return

	ls = []
	dl = fetchdirlist(rootdirurl, retries, log)
	for href in dl:
		if href.endswith('/'):
			fetchdirlistrec(rootdirurl + href, retries, log)
		else:
			ls.append(rootdirurl + href)

	with open(lsfname, 'w') as lsf:
		for href in ls:
			lsf.write(href + '\n')

def main():
	def parseargs(root, retries):
		import argparse

		parser = argparse.ArgumentParser(description='Fetches directory list recursively.')
		parser.add_argument('root', metavar='root', default=root, nargs='?', help='root to start fetching directory list.')
		parser.add_argument('-r', '--retries', type=int, default=retries, help='maximum number of retries.')
		return parser.parse_args()

	def log(message):
		import sys
		sys.stderr.write('[ ')
		sys.stderr.write(message)
		sys.stderr.write(' ]\n')

	root = 'http://mirrors.ibiblio.org/maven2/'
	retries = 5

	args = parseargs(root, retries)

	fetchdirlistrec(args.root, args.retries, log)

if __name__ == '__main__':
	main()
