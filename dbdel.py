import requests
import json

if __name__ == '__main__':
	url = 'https://api.cloudmine.me/v1/app/ae6ae133296c4a92bb1b55a85dd098dd'
	headers = {
					'Content-Type': 'application/json',
					'X-CloudMine-ApiKey': 'b5a3d4cdf27541008627342cd2272571'
					}
	print 'delete', requests.delete(url+'/data?', headers=headers).content
