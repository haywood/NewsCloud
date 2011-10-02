package com.hackny2011.newscloud;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NewsCloud extends MapActivity {
	MapView mapView;
	TextView locationText;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getCurrentLocation();
        Location currentLocation = getCurrentLocation();
    	locationText = (TextView)findViewById(R.id.locationtext);
    	mapView = (MapView)findViewById(R.id.currentMap);
        
        String testMessage;
        if(currentLocation == null){
        	testMessage = "It's null.";
        }
        else{
        	testMessage = currentLocation.toString();
        	locationText.setText("Lat: " + currentLocation.getLatitude() + " Lon: " + currentLocation.getLongitude());
        }
        
        String jsonString = getFromServer(currentLocation.getLatitude(), currentLocation.getLongitude());
        JSONArray stories = extractStories(jsonString);
        addPins(stories);
    }
    
    private Location getCurrentLocation() {
	    LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
	    List<String> providers = lm.getProviders(true);

	    /*
	     * Loop over the array backwards, and if you get an accurate location,
	     * then break out the loop
	     */
	    Location l = null;

	    for (int i = providers.size() - 1; i >= 0; i--) {
		    Log.d("Are they enabled?", Boolean.valueOf(lm.isProviderEnabled(providers.get(i))).toString());
	        l = lm.getLastKnownLocation(providers.get(i));
	        if (l != null)
	            break;
	    }
	    return l;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	

	private void addPin(double dLat, double dLon, String title, String snippet){
		int lat = (int)(dLat * 1E6);
		int lon = (int)(dLon * 1E6);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_mappin);
		ItemOverlays itemizedoverlay = new ItemOverlays(drawable);
		
		GeoPoint point = new GeoPoint(lat, lon);
		OverlayItem overlayitem = new OverlayItem(point, title, snippet);
		
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
	}
	
	private void addPins(JSONArray stories){
		if(stories != null){
			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = this.getResources().getDrawable(R.drawable.ic_mappin);
			List<JSONObject> storiesList = new ArrayList<JSONObject>();
			ItemOverlays itemizedoverlay = new ItemOverlays(drawable, this, this);
			
			try{
				for(int i = 0; i < stories.length(); i++){
					JSONObject currentStory = stories.getJSONObject(i);
					JSONArray currentStoryLocationArray = currentStory.getJSONArray("locations");
					JSONObject currentStoryLocation;
					if(currentStoryLocationArray.length() > 0){
						currentStoryLocation = currentStoryLocationArray.getJSONObject(0);
						storiesList.add(currentStory);
						Log.d("Story #" + i + " is ", currentStoryLocation.getInt("lat") + ", " + currentStoryLocation.getInt("lon"));
						GeoPoint point = new GeoPoint(new Double(currentStoryLocation.getDouble("lat") * 1E6).intValue(), new Double(currentStoryLocation.getInt("lon") * 1E6).intValue());
						OverlayItem overlayitem = new OverlayItem(point, currentStory.getString("title"), currentStory.getString("abstract"));
						itemizedoverlay.addOverlay(overlayitem);
					}
				}
				itemizedoverlay.setStoriesList(storiesList);
			}
			catch(JSONException e){Log.d("JSONException", "in addPins()");}
			mapOverlays.add(itemizedoverlay);
		}
	}
    
	
	private String getFromServer(double lat, double lon){
		URI url = null;
		String jsonString = null;
		
		try{
			//url = new URI("http://nosslapi.cloudmine.me/v1/app/ae6ae133296c4a92bb1b55a85dd098dd/text?");
			url = new URI("http://174.143.145.57:8888/?lat=" + lat + "&lon=" + lon + "&prec=1.1");
		}
		catch(URISyntaxException e){Log.d("URI", "Problem");}
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		//httpGet.addHeader(new BasicHeader("Content-Type","application/json"));
		//httpGet.addHeader(new BasicHeader("X-CloudMine-ApiKey", "b5a3d4cdf27541008627342cd2272571"));
		
		HttpResponse response = null;
		try{
			response = httpClient.execute(httpGet);
			//locationText.setText(response.toString());
			
		}
		catch(ClientProtocolException e){Log.d("ClientProtocol", "Problem");}
		catch(IOException e){Log.d("IO", e.toString());}
		
		if(response != null){
			HttpEntity httpEntity = response.getEntity();
			if(httpEntity != null){
				try{
					jsonString = EntityUtils.toString(httpEntity);
				}
				catch(IOException e){}
			}
		}
		return jsonString;
	}
	
	private JSONArray extractStories(String jsonString){
		JSONObject jsonResponse = null;
		JSONArray stories = null;
		
		try{
			 jsonResponse = new JSONObject(jsonString);
			 stories = jsonResponse.getJSONObject("success").getJSONArray("stories");
		}
		catch(JSONException e){}

		 return stories;
		//locationText.setText(stories.toString());
	}
	
	
}