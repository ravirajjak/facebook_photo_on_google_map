package com.jatayu.main.map.look;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jatayu.main.map.gpstracker.ConnectionDetector;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.maps.model.MarkerOptions;
import com.jatayu.main.map.glideutils.GlideCircleTransform;
import com.jatayu.main.map.gpstracker.GPSTracker;
import com.jatayu.main.map.http.NetworkIp;
import com.jatayu.main.map.look.R;
import com.jatayu.main.map.sqlite.DBHandler;
import com.jatayu.main.map.sqlite.PassKey.TableInfo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by ravi on 12-07-2015.
 */


public class Tab1 extends Fragment {
    protected LocationManager locationManager;
    MapView mapView;
    private GoogleMap map;
    ImageButton btnPlan;
    protected Context context;
    private Bitmap theBitmap = null;
    AutoCompleteTextView text;
    ProgressDialog pDialog;
    MultiAutoCompleteTextView text1;
   ArrayList<BitmapDescriptor> bitmapDescAL;
    RequestParams params;
    Boolean isInternetPresent = false;
   // Connection detector class
    ConnectionDetector  cd;
    ArrayList<Tab1Item> tb ;
/*
    LinkedHashMap<String, String> hm;
  
    List<LinkedHashMap<String, String>> containerList;*/
    //ArrayList <String> facebookIdAL,usernameAL,userstatusAL,userTime,userDate;
    //ArrayList<LatLng> latlng;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab1, container, false);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            Log.e("Address Map", "Could not initialize google play", e);
        }

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
            case ConnectionResult.SUCCESS:
                // Getting Map for the SupportMapFragment
               

        		cd = new ConnectionDetector(getActivity());
        	    // get Internet status
                isInternetPresent = cd.isConnectingToInternet();
            	mapView = (MapView) v.findViewById(R.id.map);
                mapView.onCreate(savedInstanceState);
                if (isInternetPresent) {
                	// Gets to GoogleMap from the MapView and does initialization stuff
                if(mapView!=null)
                {  map = mapView.getMap();
                    map.getUiSettings().setMyLocationButtonEnabled(true);
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().setZoomControlsEnabled(false);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(19.129980, 72.834795), 10);
                    map.animateCamera(cameraUpdate);
            		Log.e("","Check 1");
            		params 				= new RequestParams();
            		pDialog		= new ProgressDialog(getContext());
            		/*
            		hm			 				= new  LinkedHashMap<String, String>();
            		containerList	= new  ArrayList<LinkedHashMap<String, String>>();*/
            	/*	facebookIdAL   = new ArrayList<String>();
            		usernameAL		= new ArrayList<String>();
            		userstatusAL	= new ArrayList<String>();
            		userTime			= new ArrayList<String>();
            		userDate				= new ArrayList<String>();
            		latlng					= new ArrayList<LatLng>();
            		*/
            		bitmapDescAL= new ArrayList<BitmapDescriptor>();
            		tb 							= new ArrayList<Tab1Item>();
            		showAllFriendsOnMap();
            		
            		
                    final FloatingActionButton updatePosition = (FloatingActionButton) v.findViewById(R.id.currentLoc);
                    updatePosition.setOnClickListener(new OnClickListener() {
                      @Override
                      public void onClick(View view) {
                      //  menuMultipleActions.setEnabled(!menuMultipleActions.isEnabled());
                       //Toast.makeText(getContext(), "Time Set", Toast.LENGTH_SHORT).show();
                      
                    		String mitId=  	getUserMitId();
	                    	updateUserLocation(mitId);
	            
                      }
                    });
                    
            		final FloatingActionButton timerSet = (FloatingActionButton) v.findViewById(R.id.timeSet);
                    timerSet.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        //  menuMultipleActions.setEnabled(!menuMultipleActions.isEnabled());

                          showAllFriendsOnMap();
                        }
                    });
                }
                } else {
                    Toast.makeText(getContext(), "No Internet", Toast.LENGTH_LONG).show();
                }
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(getActivity(), "SERVICE MISSING", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(getActivity(), "UPDATE REQUIRED", Toast.LENGTH_SHORT).show();
                break;
            default: Toast.makeText(getActivity(), GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()), Toast.LENGTH_SHORT).show();
        }

        return v;
    }



	private String  getUserMitId()
	{
		DBHandler dbh = new DBHandler(getContext());
      String showMitID=null;
      
      Cursor cursor = dbh.readAllRow(dbh);
		while(cursor.moveToNext()){
			 showMitID 	= cursor.getString(0);
			 Log.e("","MITID DISPLAYED "+showMitID);
		    // Log.e("","GDT "+google_distance_time+" wait "+wait_time);
		 }
		cursor.close(); 
		return showMitID;
		
	}
	
	private void updateUserLocation(String showMitID)
	{
		cd = new ConnectionDetector(getActivity());
		
	    isInternetPresent = cd.isConnectingToInternet();
	    
	  if (isInternetPresent) {
	  
		GPSTracker gps= new GPSTracker(getContext());
      double lat=gps.getLatitude();
      double lng=gps.getLongitude();

  	AsyncHttpClient client = new AsyncHttpClient();
		Log.e("","YES !!!");
		params.put("parseMitID", showMitID);
		params.put("parseLatitude", String.valueOf(lat));
		params.put("parseLongitude", String.valueOf(lng));
		Log.e("","LAt "+lat+" Lng "+lng);
		client.post(NetworkIp.INSERT_LOCATION, params,
	            new AsyncHttpResponseHandler() {
	                // When the response returned by REST has Http
	                // response code '200'
	                @Override
	                public void onSuccess(String response) {
	                    // Hide Progress Dialog
	                	Log.e("","Response "+response);
	                	//   Toast.makeText(getApplicationContext(), "Success Saev", Toast.LENGTH_LONG).show();
	                	 try{
	                	   
	                		JSONObject mainJson 	= 	new JSONObject(response);
	                	   JSONArray jsonArray		= 	mainJson.getJSONArray("data");
	                       Log.e("Point","Yes 2");
	                       JSONObject objJson = new JSONObject(response);
	                       
	                       for (int i 	= 0; i < jsonArray.length(); i++) {
         					objJson = jsonArray.getJSONObject(i);
         					String success =objJson.getString("success");
         					if(success.equals("1")){
         						Toast.makeText(getContext(), "Location  updated", Toast.LENGTH_SHORT).show();
         					}
         					else{
         						Toast.makeText(getContext(), "Location not updated", Toast.LENGTH_SHORT).show();
         						}																																																																																																																																																									
	                       	}
	                       }
	                         catch(Exception e)
	                     {e.printStackTrace();}
	                }   			 
	    	
	        


					// When the response returned by REST has Http
	                // response code other than '200' such as '404',
                // '500' or '403' etc
	                @Override
	                public void onFailure(int statusCode, Throwable error,String content) {
	                    if (statusCode == 404) {
	                        Toast.makeText(getContext(),
	                                "Requested resource not found",
	                                Toast.LENGTH_LONG).show();
	                    }
	                    // When Http response code is '500'
	                    else if (statusCode == 500) {
	                        Toast.makeText(getContext(),
	                                "Something went wrong at server end",
	                                Toast.LENGTH_LONG).show();
	                    }
	                    // When Http response code other than 404, 500
	                    else {
	                        Toast.makeText(
	                                getContext(),
	                                "Unexpected Error occcured! [Most common Error: Device might "
	                                        + "not be connected to Internet or remote server is not up and running], check for other errors as well",
	                                Toast.LENGTH_LONG).show();
	                    }
	                }
	            });
	  }
	  else{}
	}

    private void showAllFriendsOnMap() {
		// TODO Auto-generated method stub
    	pDialog = new ProgressDialog(getContext());
		pDialog.setMessage("Please wait...");
		//pDialog.setCancelable(false);
		pDialog.show();
    	final DBHandler dbh	= new DBHandler(getContext());
		Cursor cursor 		= dbh.readMitId(dbh);	
		String showMitID= null;
		while(cursor.moveToNext()){
			 showMitID 	= cursor.getString(0);
			 Log.e("","MITID DISPLAYED "+showMitID);
		    // Log.e("","GDT "+google_distance_time+" wait "+wait_time);
		 }
		cursor.close();
    
    	AsyncHttpClient client = new AsyncHttpClient();
		Log.e("","YES !!!");
		params.put("parseMitID", showMitID);
		params.put("parseStatus","1");
	    client.post(NetworkIp.DISPLAY_FRIENDS, params,
	            new AsyncHttpResponseHandler() {
	                // When the response returned by REST has Http
	                // response code '200'
	                @Override
	                public void onSuccess(String response) {
	                    // Hide Progress Dialog
	                //	Log.e("","Response "+response);
	                	//   Toast.makeText(getApplicationContext(), "Success Saev", Toast.LENGTH_LONG).show();
	                	 try{
	                		
	                    		
                 
                     			map.clear();
	                		 
                     			JSONObject mainJson 	= 	new JSONObject(response);
                     			JSONArray jsonArray		= 	mainJson.getJSONArray("data");
                     			Log.e("Point","Yes 2");
	                       JSONObject objJson = new JSONObject(response);
	                       Log.e("","Respo" +response) ;
	                       String 		success 		= null;
	                       String 		userName 	= null;
	                       String 		fbID 				= null;
	                       String 		mitOtherFrndID=null;
	                       String 		latitude		= null;
	                       String		longitude 	= null;
	                       String 	   	userStatus	= null;
	                       String 	   	setDate		= null;
	                       String 	   	setTime		= null;
	                        
	                       // ArrayList<String> userNameAL = new ArrayList<String>();
	                       for (int i 	= 0; i < jsonArray.length(); i++) {
	                    	   Log.e("JSON AL ",""+jsonArray.length());
	                           		objJson = jsonArray.getJSONObject(i);
	                           		
	                           		//success		= 	objJson.getString("success");
	                           		
	                           		
	                           		mitOtherFrndID	=	objJson.getString("mit_user_id");
	                           	
	                           		
	                           		Tab1Item ti = new Tab1Item();
	                           	
	                           		fbID				=	objJson.getString("facebook_id");
	                           		userName	= 	objJson.getString("user_name");
	                           		latitude		=	objJson.getString("latitude");
	                        		longitude	=	objJson.getString("longitude");
	                        		userStatus =	objJson.getString("user_status");
	                        		setTime		=	objJson.getString("set_time");
	                        		setDate 		=	objJson.getString("set_date");
	                      /*  		ti.setFacebookIdAL(fbID);
	                        		ti.setUsernameAL(userName);
	                        		ti.setLatlng(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)));
	                        		ti.setUserstatusAL(userStatus);
	                        		ti.setUserTime(setTime);
	                        		ti.setUserDate(setDate);
	                        		tb.add(ti);
	                      */  		dbh.putInformation(dbh, userName, fbID, userStatus, latitude, longitude, setTime, setDate);		
	                        		//Log.e("Executed "," 1");
	                        	//	plotFriendsOnMap();
	                       

	                        		
	                       }
	                       
	                       readFriends();
	                     
	               		
	               		
	               		
	               		
	               		
	                       }catch(Exception e)
	                     {e.printStackTrace();}
	                }   			 
	    	
	                private void readFriends() {
						// TODO Auto-generated method stub 
	                		tb= new ArrayList<Tab1Item>();
	                		DBHandler dbh = new  DBHandler(getContext());
	                		Cursor cursor = dbh.readAllFriends(dbh);
	               			while(cursor.moveToNext()){
	               				Tab1Item ti = new Tab1Item();
	               				ti.setFacebookIdAL(cursor.getString(cursor.getColumnIndex(TableInfo.F_FACEBOOK_ID)));
                        		ti.setUsernameAL(cursor.getString(cursor.getColumnIndex(TableInfo.F_USER_NAME)));
                        		ti.setLatlng(new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(TableInfo.F_LAT))),Double.parseDouble(cursor.getString(cursor.getColumnIndex(TableInfo.F_LNG)))));
                        		ti.setUserstatusAL(cursor.getString(cursor.getColumnIndex(TableInfo.F_USER_STATUS)));
                        		ti.setUserTime(cursor.getString(cursor.getColumnIndex(TableInfo.F_TIME)));
                        		ti.setUserDate(cursor.getString(cursor.getColumnIndex(TableInfo.F_DATE)));
                        		tb.add(ti);
                        	
	               			}
	               			cursor.close(); 

	            	     	
	               			LoadImage li = new LoadImage();
	               			li.execute();
					}

					class LoadImage extends AsyncTask<Void, Void, Void> {
	                 		BitmapDescriptor iconBitmap;     
                 	 			@Override
                 	 			protected Void doInBackground(Void... urls) {
	                 			Log.e("Facebook AL Size", " "+tb.size());
	                 			int sizeOfTB=tb.size();	
	                 			for(int i=0;i<sizeOfTB;i++){
	                 						Bitmap theBitmap = null;
	                 					//	Log.e("","Wow It has to work");
	                 						try {
	                 								//Animation anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
	                 							//	Log.e("",			facebookIdAL.get(i));
	                 								theBitmap = Glide.with(getActivity())
	     		            								         						.load("https://graph.facebook.com/"+tb.get(i).getFacebookIdAL()+"/picture?type=large")
	     		            								         						.asBitmap()
	     		            								         						.transform(new GlideCircleTransform(getContext()))
	     		            							//	         						.animate(anim)
	     		            								         						.into(100, 100) // Width and height
	     		            								         						.get();
	                 								
	     		            								iconBitmap = BitmapDescriptorFactory.fromBitmap(theBitmap);
	     		            								bitmapDescAL.add(iconBitmap);
	     		            								
	     		            							} catch (InterruptedException | ExecutionException e) {
	     		            								// TODO Auto-generated catch block
	     		            	 							e.printStackTrace();
	     		            							}
	                 							}			
	                 			            return null;
	                 			         }
	                 			         @Override
	                 			         protected void onPostExecute(Void result) {
	                 			       
	                 			        	for(int i=0;i<tb.size();i++){        	
	                 			        	 map.addMarker(new MarkerOptions()
	           			                    .icon(bitmapDescAL.get(i))
	           			                    .title(tb.get(i).getUsernameAL()+tb.get(i).getUserTime()+ " "+tb.get(i).getUserDate())
	           			                    .snippet(tb.get(i).getUserstatusAL() )
	           			                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
	           			                    .position(tb.get(i).getLatlng()));
	    	                 				}
	                 			        	if(pDialog.isShowing())
	                 			        	{
	                 			        		pDialog.dismiss();
	                 			        	}
	                 			         }
					}



					// When the response returned by REST has Http
	                // response code other than '200' such as '404',
	                // '500' or '403' etc
	                @Override
	                public void onFailure(int statusCode, Throwable error,String content) {
	                    // Hide Progress Dialog
	                	/*if(showProgress.isShowing())
               		 		{
               			 		showProgress.dismiss();
               		 		}*/
	                    // When Http response code is '404'
	                    if (statusCode == 404) {
	                        Toast.makeText(getContext(),
	                                "Requested resource not found",
	                                Toast.LENGTH_LONG).show();
	                    }
	                    // When Http response code is '500'
	                    else if (statusCode == 500) {
	                        Toast.makeText(getContext(),
	                                "Something went wrong at server end",
	                                Toast.LENGTH_LONG).show();
	                    }
	                    // When Http response code other than 404, 500
	                    else {
	                        Toast.makeText(
	                                getContext(),
	                                "Unexpected Error occcured! [Most common Error: Device might "
	                                        + "not be connected to Internet or remote server is not up and running], check for other errors as well",
	                                Toast.LENGTH_LONG).show();
	                    }
	                }
	            });

}





	/*
    private static final class CustomDialog extends AlertDialog {

        private CustomDialog(Context context) {
            super(context);
        }

        *//**
         * {@inheritDoc}
         *//*
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final Resources res = getContext().getResources();
            final int id = res.getIdentifier("titleDivider", "id", "android");
            final View titleDivider = findViewById(id);
            if (titleDivider != null) {
                titleDivider.setBackgroundColor(Color.RED);
            }
        }
    }*/



    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}

