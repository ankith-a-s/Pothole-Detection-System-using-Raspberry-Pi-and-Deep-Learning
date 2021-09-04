package sdev.com.map;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,SensorEventListener,LocationListener {

    private GoogleMap mMap;
    private SensorManager sensorManager;
    private Sensor accelerometer,gyro;
    LocationManager locationManager;
    String mprovider;
    private TextView gps1,gps2,speed;
    Switch s1;
    Button refresh;
    boolean rekam=false;
    public static float ax[] = new float[10];
    public static float ay[] = new float[10];
    public static float az[] = new float[10];
    public static float gx[] = new float[10];
    public static float gy[] = new float[10];
    public static float gz[] = new float[10];
    public static float tm[] = new float[10];
    int c = 0;
    String t1;

    private static final int PERMISSION_REQUEST_CODE = 1;
    ArrayList<String> value ;
    boolean doubleBackToExitPressedOnce = false;
    ProgressDialog progressDialog;
    Location location;
    public static int count = 0;
    private String mJSONURLString = "https://pure-island-91062.herokuapp.com/location";

    // private String mJSONURLString = "https://roads.googleapis.com/v1/nearestRoads?points=12.88199316,77.64131948|12.84335033,77.66300198|12.88125856,77.63850992|12.88224405,77.64028189|12.88125546,77.64133571&key=AIzaSyD7ALReFekmEYSVqrNfytp20lqQIgo-zUI";
    ArrayList<HashMap<String, String>> googlelist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        toolbar.setTitle("Pothole Map");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        if (!checkPermission()) {
            requestPermission();
        }

        googlelist = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        value = new ArrayList<>();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        gps1=(TextView) findViewById(R.id.gps1);
        gps2=(TextView) findViewById(R.id.gps2);
        speed=(TextView) findViewById(R.id.speed);
        s1=(Switch) findViewById(R.id.switch1);
        refresh=(Button) findViewById(R.id.refresh) ;
        progressDialog = new ProgressDialog(this);
        gps1.setText("0.0");
        gps2.setText("0.0");
        speed.setText("0.0");

        mprovider = locationManager.getBestProvider(criteria, false);

        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("haha","not done");
                return;
            }
            location = getLastKnownLocation();
            locationManager.requestLocationUpdates(mprovider, 50, (float)0.01, this);

            if (location != null)
                onLocationChanged(location);
            else {

                Toast.makeText(getBaseContext(), "Turn your GPS ON", Toast.LENGTH_LONG).show();

            }
        }

    //switch code
        s1.setChecked(false);
        s1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(getBaseContext(), "Data sending ON", Toast.LENGTH_SHORT).show();
                     rekam=true;
                }else{
                    Toast.makeText(getBaseContext(), "Data sending OFF", Toast.LENGTH_SHORT).show();
                    rekam=false;
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // Toast.makeText(getBaseContext(), "Updating Data", Toast.LENGTH_SHORT).show();
                getRoadLocation();
            }
        });








    }

    private Location getLastKnownLocation() {


        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
// permission checker
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }
    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            Toast.makeText(getApplicationContext(),"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getBaseContext(), "Permission Granted, Please refresh", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Permission Denied, You cannot access location data", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Changing map type
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
         //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        // Showing / hiding your current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        // Enable / Disable zooming controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable / Disable my location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Enable / Disable Compass icon
        googleMap.getUiSettings().setCompassEnabled(true);

        // Enable / Disable Rotate gesture
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        // Enable / Disable zooming functionality
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        if (location != null) {
            float zoomLevel = (float) 10.0; //This goes up to 21
            LatLng sydney2 = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney2, zoomLevel));
        }

        getRoadLocation();



    }



    public void getRoadLocation(){
        // Initialize a new RequestQueue instance
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Initialize a new JsonObjectRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mJSONURLString, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with response
                        //mTextView.setText(response.toString());

                        // Process the JSON
                        try{
                            // Get the JSON array
                            JSONArray array = response.getJSONArray("location");

                            // Loop through the array elements
                            for(int i=0;i<array.length();i++){
                               // if (i%2==1) {

                                    // Get current json object
                                    JSONObject location = array.getJSONObject(i);
                                   // JSONObject location = student.getJSONObject("location");
                                    // Get the current student (json object) data
                                    String latitude = location.getString("latitude");
                                    String longitude = location.getString("longitude");

                                    HashMap<String, String> plocation = new HashMap<>();
                                    plocation.put("latitude", latitude);
                                    plocation.put("longitude", longitude);
                                    googlelist.add(plocation);
                                }
                           // }
                            Log.d("location",""+googlelist);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        markOnMap();
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred

                        Toast.makeText(getApplicationContext(), "Error Retrieving data", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                }
        );
        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
       //initialize the progress dialog and show it
        progressDialog.setMessage("Fetching The Data...");
        progressDialog.show();

    }

    public void markOnMap() {

        Log.d("mark", ""+googlelist.size());
        for (int i = 0; i < googlelist.size(); i++) {
           // Log.d("i", ""+i);
            Log.d("mark", googlelist.get(i).get("longitude"));
            LatLng sydney = new LatLng(Double.parseDouble(googlelist.get(i).get("longitude")), Double.parseDouble(googlelist.get(i).get("latitude")));
            MarkerOptions marker = new MarkerOptions();
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            mMap.addMarker(marker.position(sydney).title("pothole"));

            //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }

    }





    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        sensorManager.unregisterListener(this, gyro);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(c == 0){
            value = new ArrayList<>();
        }



        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
//                currentX.setText(Float.toString(event.values[0]));
//                currentY.setText(Float.toString(event.values[1]));
//                currentZ.setText(Float.toString(event.values[2]));

                if(rekam==true)
                {


                        float curx=0,cury=0,curz=0,curt=0;
                        if(count < 10){
                            ax[count] = event.values[0];
                            ay[count] = event.values[1];
                            az[count] = event.values[2];
                            tm[count] = System.currentTimeMillis()/10;
                            //count++;
                        }
                        else if(count==10){
                            for(int j = 0;j<ax.length;j++){
                                curx += ax[j];
                                cury+=ay[j];
                                curz+=az[j];
                                curt=tm[j];

                            }
                            curx/=10;
                            cury/=10;
                            curz/=10;
                            curt/=10;
                            if(c < 50){
                                t1 = System.currentTimeMillis()/10+"," + Float.toString(curx) + "," + Float.toString(cury) + "," + Float.toString(curz);
                                Log.d("sensor", t1);
                                Log.d("c", c + "");
//                                value.add(t1);
//                                c++;
                            }

                            //count = 0;


                        }

                        //out.append((System.currentTimeMillis()/10)+",Accl|" + Float.toString(event.values[0]) + "\t" + Float.toString(event.values[1]) + "\t" + Float.toString(event.values[2]) + "\n");





                }
                break;

            case Sensor.TYPE_GYROSCOPE:
//                gyrox.setText(Float.toString(event.values[0]));
//                gyroy.setText(Float.toString(event.values[1]));
//                gyroz.setText(Float.toString(event.values[2]));
                if(rekam==true)
                {

                        float curx=0,cury=0,curz=0,curt=0;
                        if(count < 10){
                            gx[count] = event.values[0];
                            gy[count] = event.values[1];
                            gz[count] = event.values[2];
                            tm[count] = System.currentTimeMillis()/10;
                            count++;
                        }
                        else if(count==10){
                            for(int j = 0;j<ax.length;j++){
                                curx += gx[j];
                                cury+=gy[j];
                                curz+=gz[j];
                                curt=tm[j];

                            }
                            curx/=10;
                            cury/=10;
                            curz/=10;
                            //curt/=10;
                            //out.append(Float.toString(curx) + "," + Float.toString(cury) + "," + Float.toString(curz) + ","+ gps2.getText()+"," + gps1.getText() +","+speed.getText()+ "\n");
                            if(c < 50){
                                t1 = t1 + "," + Float.toString(curx) + "," + Float.toString(cury) + "," + Float.toString(curz) + ","+ gps2.getText()+"," + gps1.getText() +","+speed.getText();
                                Log.d("sensor", t1);
                                Log.d("c", c + "");
                                value.add(t1);
                                c++;
                            }
                            else if(c == 50){
                                c = 0;
                                sendData();
                                value = null;
                            }
                            count = 0;

                        }



                }
                break;

        }

        //ctime=System.currentTimeMillis()/10;
    }



    @Override
    public void onLocationChanged(Location location) {

        gps1.setText(""+(location.getLongitude()));
        gps2.setText(""+(location.getLatitude()));
        speed.setText(""+(location.getSpeed()));
   //     LatLng current = new LatLng(location.getLongitude(),location.getLatitude());
    //    mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        //Log.d("speed : ", float.toString(location.getSpeed()));
        // Log.d("location-long : ", float.toString(location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //function to send data
    public void sendData(){

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        //this is the url where you want to send the request
        //TODO: replace with your own url to send request, as I am using my own localhost for this tutorial
        String url = "https://pure-island-91062.herokuapp.com/data";
        JSONObject jsonBody = new JSONObject();
        try {
            Toast.makeText(getBaseContext(), "Data sent", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < value.size(); i++) {
                jsonBody.put("data" + i , value.get(i));
                //Log.d("sdata", value.get(i)+"|||");
            }
            Log.d("jsonbody_len", " "+jsonBody.length());
            Log.d("value_size", " "+value.size());

            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = String.valueOf(response.statusCode);

                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            queue.add(stringRequest);

        }
        catch(JSONException e) {
            e.printStackTrace();

        }
        // Request a string response from the provided URL.

        // Add the request to the RequestQueue.
        //queue.add(stringRequest);
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}


