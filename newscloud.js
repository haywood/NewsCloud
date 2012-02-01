var map;
function initialize() {
    var latlng = new google.maps.LatLng(40.000, -73.644);
    var myOptions = {
        zoom: 3,
        center: latlng,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

    var xh = new XMLHttpRequest();
    xh.onreadystatechange= function() {
        if (xh.readyState == 4 && xh.status == 200) {
            var data = JSON.parse(xh.responseText);
            for (k in data) {
                if (true && data[k]['coordinates'].length > 0) {
                    var l = data[k]['coordinates'][0];
                    var latlng = new google.maps.LatLng(l['lat']+jitter(), l['lng']+jitter());
                    var marker = new google.maps.Marker({
                        title: data[k]['title'],
                        map: map,
                        position: latlng
                    });
                    marker.story = data[k];
                    google.maps.event.addListener(marker, 'click', function() {
                        window.open(this.story['url']+'?pagewanted=all');
                    });
                }
            }
        }
    }
    xh.open('GET', '/news', true);
    xh.send();
}

function jitter() {
    var epsilon = 0.01;
    return -epsilon + Math.random()*2*epsilon;
}
