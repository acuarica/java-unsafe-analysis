#!/usr/bin/env python

import os
import requests
import json
from pprint import pprint

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

def downloadAll():
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

		if i == 5:
			break

def main():
	downloadAll()

if __name__ == '__main__':
	main()
