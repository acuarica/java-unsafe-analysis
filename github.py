#!/usr/bin/env python

import os
import requests
import json
from pprint import pprint
import argparse

def ascii(text):
	if text == None:
		return text
	else:
		return text.encode('ascii', 'ignore')

def getReps():
	filename = 'reps.json'

	if not os.path.isfile(filename):
		r = requests.get('https://api.github.com/search/repositories?q=language:java&sort=stars&order=desc&per_page=100')

		reps = r.json()
		with open(filename, 'w') as f:
			json.dump(reps, f)

	with open(filename, 'r') as f:
		reps = json.load(f)

	return reps

def downloadFile(url, filename):
    r = requests.get(url, stream=True)
    with open(filename, 'wb') as f:
        for chunk in r.iter_content(chunk_size=1024):
            if chunk:
                f.write(chunk)
                f.flush()
    return filename

def downloadRepos(repocount):
	reps = getReps()

	print reps['total_count']
	print reps['incomplete_results']

	i = 0
	for rep in reps['items']:
		i += 1
		fullname = rep['full_name']
		url = rep['html_url']
		desc = rep['description']
		print i, fullname, url

		url = 'https://github.com/' + fullname
		zipurl = url + '/archive/master.zip'

		filename = 'build/' + fullname.replace('/', '-') + '-master.zip'

		downloadFile(zipurl, filename)

		if i == repocount:
			break

def main():
	def parseargs():
		parser = argparse.ArgumentParser(description='Download repos.')
		parser.add_argument('repocount', metavar='repocount', default=5, type=int, nargs='?', help='How many repos to download.')
		return parser.parse_args()

	args = parseargs()

	downloadRepos(args.repocount)

if __name__ == '__main__':
	main()
