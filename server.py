import tornado.ioloop
import tornado.web
import requests
import socket
import math
import json
import sys

url = 'https://api.cloudmine.me/v1/app/ae6ae133296c4a92bb1b55a85dd098dd'
headers = {
				'Content-Type': 'application/json',
				'X-CloudMine-ApiKey': 'b5a3d4cdf27541008627342cd2272571'
			}

def distance(p1, p2):
	return math.hypot(p1[0] - p2[0], p1[1] - p2[1])

def getCloseStories(lat, lon, prec):
	pos = (float(lat), float(lon))
	prec = float(prec)
	result = json.loads(requests.get(url+'/text?', headers=headers).content)
	ret={'stories':[]}
	for r in result['success']['stories']:
		geo=r['geo_facet']
		if geo: 
			close = False
			for l in r['locations']:
				if distance(pos, (l['lat'], l['lon'])) < prec:
					close = True
					break
			if close: ret['stories'].append(r)
	return {'success':ret}

class Handler(tornado.web.RequestHandler):
	def get(self):
		try:
			lat = self.get_argument('lat')
			lon = self.get_argument('lon')
			prec = self.get_argument('prec', default=1.1)
			response=getCloseStories(lat, lon, prec)
			self.write(json.dumps(response))
		except ValueError as e:
			print e

app=tornado.web.Application([(r'/', Handler)])

if __name__ == '__main__':

	app.listen(8888, socket.gethostbyname(socket.gethostname()))
	print 'running on {0}'.format(socket.gethostbyname(socket.gethostname()))
	tornado.ioloop.IOLoop.instance().start()
