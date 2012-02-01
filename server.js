
var http = require('http');
var https = require('https');
var path = require('path');
var fs = require('fs');
var os = require('os');
var legalURI=/[\w\.]/;

http.createServer(function (req, res) {

    var p = req.url;
    console.log(p);
    if (p == '/') { p = 'index.html'; }

    if (p == '/news') { // Handle a request for news stories
	var dbhost = 'api.cloudmine.me'
	var dbpath = '/v1/app/ae6ae133296c4a92bb1b55a85dd098dd'
	var dbheaders = {
	    'Content-Type':'application/json',
	    'X-CloudMine-ApiKey':'b5a3d4cdf27541008627342cd2272571'
	};

	var results = '';

	https.get({
	    'host':dbhost,
	    'path':dbpath+'/text',
	    'headers':dbheaders
	}, function(response) {
	    response.on('data', function(chunk) {
		results += chunk;
	    });

	    response.on('end', function() {
		results = JSON.parse(results);
		if ('success' in results) {
		    res.writeHead(200, {'Content-type': 'application/json'});
		    res.write(JSON.stringify(results['success']));
		    res.end();
		} else {
		    res.writeHead(500);
		    res.end();
		}
	    });

	}).on('error', function (e) {
	    console.log(e.message);
	    res.writeHead(500);
	    res.end();
	});
    } else { // handle request for arbitrary resource
        p=p.replace(/^\//, '');
        if (legalURI.test(p)) { // make sure it is valid uri
	    fs.readFile(p, 'utf-8', function (err, data) {
	        if (err) {
		    res.writeHead(404);
		    res.end();
	        } else {
		    res.writeHead(200, {'Content-type': 'text/html'});
		    res.end(data, 'utf-8');
	        }
	    });
        } else {
            res.writeHead(400);
            res.end();
        }
    }
}).listen(1337, os.hostname());

console.log('Server running at '+os.hostname());
