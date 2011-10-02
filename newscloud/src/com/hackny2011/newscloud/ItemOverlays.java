package com.hackny2011.newscloud;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ItemOverlays extends ItemizedOverlay{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	Activity mActivity;
	List<JSONObject> storiesList;

	public ItemOverlays(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	public ItemOverlays(Drawable defaultMarker, Context context, Activity activity) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		  mActivity = activity;
		}
	
	public void setStoriesList(List<JSONObject> sl){
		storiesList = sl;
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	protected boolean onTap(int index) {
		  OverlayItem item = mOverlays.get(index);
		  final JSONObject story = storiesList.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.setCancelable(false)
	       .setPositiveButton("Visit Article", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
					//String uri = "http://nytimes.com/";
	        	   String uri = null;
	        	   try{
	        		   uri = story.getString("url");
	        	   }
	        	   catch(JSONException e){}
					mActivity.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
	           }
	       })
	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	           }
	       });
		  dialog.show();
		  return true;
		}
	
}
