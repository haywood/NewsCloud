from googlemaps import GoogleMaps
from multiprocessing import *
import requests
import json
import sys
import re

dburl='https://api.cloudmine.me/v1/app/ae6ae133296c4a92bb1b55a85dd098dd'
dbheaders = {
				'Content-Type': 'application/json',
				'X-CloudMine-ApiKey': 'b5a3d4cdf27541008627342cd2272571'
				}
tkill=re.compile(r"<!?--.*?-->|<(style|(no)?script).*?>.*?</\1>|<.*?>|\s+", flags=re.S).sub
ekill=re.compile(r"&[a-z0-9]+?;").sub
gm = GoogleMaps('ABQIAAAA8CkQgGb_7Lc2cieBIb8J3hQdj4Co_YrXzqfb9ptRo07MTIpfrxQQUSS77Xh18ULRlZARcXk10z6KSQ')

def isnyc(story):
	return (re.match('N.Y.', story['section']) 
				and story['geo_facet']
				and any([re.search(r'NYC', g) for g in story['geo_facet']]))

def addstory(story, stories):
	try:
		story['locations'] = []
		geo = story['geo_facet']
		for g in geo:
			g = re.sub(r' \(([a-zA-Z]+)\)', r', \1', g)
			lat, lon = gm.address_to_latlng(g)
			story['locations'].append({'lat':lat, 'lon':lon})
		if story['locations']: stories.append(story)

	except Exception as e:
		print e

if __name__ == '__main__':

	goal = 500
	if sys.argv[1:]:
		goal = int(sys.argv[1])
	if goal > 500: goal = 500
	limit=20
	mul = 1 + goal/limit
	i = n = 0
	j = 1

	pool=Pool(processes=20)
	lastcount = 0
	manager = Manager()

	submit = manager.list()

	while n < mul*limit:
		url = ('http://api.nytimes.com/svc/news/v3/content/nyt/all?'
				+'api-key=d9e3489453c073a12e6e5140ac8071da:3:64029825'
				+'&time-period=720&offset={0}&limit={1}'.format(i, limit))
		try:
			stories = json.loads(requests.get(url).content)['results']
			post = {}
			for story in stories:
				post[str(j)]=story
				j+=1
			if post: 
				requests.post(dburl+'/text', json.dumps(post), headers=dbheaders)
				stories = json.loads(requests.get(dburl+'/search?q=[section="N.Y. ?/ ?Region"]', headers=dbheaders).content)
				for s in stories['success']:
					if re.match('^\d+$', s):
						if isnyc(stories['success'][s]):
							pool.apply_async(addstory, (stories['success'][s], submit))
							n+=1
							if len(submit) > lastcount:
								lastcount = len(submit)
								print lastcount
		except ValueError as e:
			print e
		i += limit

	pool.close()
	pool.join()

	print requests.post(dburl+'/text', json.dumps({'stories':list(submit)}), headers=dbheaders).content
