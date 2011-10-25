var http = require('http');
var https = require('https');
var os = require('os');

http.createServer(function (req, res) {

    var dbhost = 'api.cloudmine.me'
    var dbpath = '/v1/app/ae6ae133296c4a92bb1b55a85dd098dd'
    var dbheaders = {
	'Content-Type':'application/json',
	'X-CloudMine-ApiKey':'b5a3d4cdf27541008627342cd2272571'
    };
    var results = ''

    https.get({
	'host':dbhost,
	'path':dbpath+'/text',
	'headers':dbheaders

    }, function(response) {
	response.on('data', function(chunk) {
	    results += chunk;
	});

	response.on('end', function() {
	    results = JSON.parse(results)['success']
	    var html = '';
	    html += '<head><title>News!</title></head>';
	    html += '<h1>News!</h1><ol>';
	    for (k in results) {
		html += '<li><a href="'+results[k]['url']+'">'+results[k]['title']+'</a><ol>';
		html += '<li>'+results[k]['abstract']+'</li>';
		if (results[k]['multimedia']) {
		    results[k]['multimedia'].forEach(function (media) {
			if (media['type'] == 'image') {
			    html += '<li><img src="'+media['url']+'"/></li>';
			}
		    });
		}
		html += '<li>Loation: '+results[k]['geo_facet'].join(', ')+'</li>';
		html += '</ol></li>';
	    }
	    html += '</ol>';

	    res.writeHead(200, {'Content-type': 'text/html'});
	    res.write(html);
	    res.end();
	});

    }).on('error', function (e) {
	console.log(e.message);
	res.writeHead(500);
 	res.end();
    });

}).listen(1337, os.hostname());

console.log('Server running at '+os.hostname());
