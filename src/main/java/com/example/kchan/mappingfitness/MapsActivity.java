package com.example.kchan.mappingfitness;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener {


    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }

    private static final String TAG = "MapActivity";
    TextView text;
    long it;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    HashMap<String,List<LatLng>> hashMap= new HashMap<>();
    HashMap<String, Double> calmap=new HashMap<>();
    HashMap<String, Double> distmap= new HashMap<>();
    HashMap<Date,ArrayList> datemap = new HashMap<>();
    ArrayList<List<LatLng>> innerlist = new ArrayList<>();
    HashMap<String,List> eachmap= new HashMap<>();

    private ArrayList<LatLng> hp;
    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<LatLng> points; //added
    Polyline line; //added
    LatLng mcurrent;
    Marker marker;
    int calorie=0;
    BroadcastReceiver broadcastReceiver;
    String ActivityTT="start";
    float bmap;
    private Polyline mPolyline;
    ArrayList<LatLng> te= new ArrayList<>();
    Double calo;
    int age=23;
    int weight=70;
    int heartrate=0;
    LocationManager locationManager;
    String prev=" ";
    String FinalDistance=" "; String FinalCalories= " " ;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        points = new ArrayList<LatLng>();
        text=(TextView)findViewById(R.id.textView);

        startTracking();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };
        startTracking();
      //  startCalCount();
        getLocationPermission();
        FloatingActionButton fb= (FloatingActionButton)findViewById(R.id.floatingActionButton);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MapsActivity.this,DetailsActivity.class);
                i.putExtra("distance",FinalDistance);
                i.putExtra("calories",FinalCalories);
                startActivity(i);

            }
        });

    }



    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, (LocationListener) this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }
    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);
        //  int icon = R.drawable.ic_still;
        Log.e(TAG, "test");


        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                heartrate=80;
                it= SystemClock.currentThreadTimeMillis();
              //  bmap= BitmapDescriptorFactory.HUE_AZURE;
                //  icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                heartrate=110;
                it= SystemClock.currentThreadTimeMillis();
                //  bmap= BitmapDescriptorFactory.HUE_BLUE;
                //   icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                heartrate=80;
                label = getString(R.string.activity_on_foot);
                it= SystemClock.currentThreadTimeMillis();
              //  bmap= BitmapDescriptorFactory.HUE_CYAN;
                //   icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                heartrate=100;
                label = getString(R.string.activity_running);
                it= SystemClock.currentThreadTimeMillis();
              //  bmap= BitmapDescriptorFactory.HUE_GREEN;
                //  icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                heartrate=80;
                label = getString(R.string.activity_still);
                it= SystemClock.currentThreadTimeMillis();
              //  bmap= BitmapDescriptorFactory.HUE_MAGENTA;
                break;
            }
            case DetectedActivity.TILTING: {
                heartrate=95;
                label = getString(R.string.activity_tilting);
                it= SystemClock.currentThreadTimeMillis();
              //  bmap= BitmapDescriptorFactory.HUE_ORANGE;
                //  icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                heartrate=90;
                label = getString(R.string.activity_walking);
                it= SystemClock.currentThreadTimeMillis();
              //  bmap= BitmapDescriptorFactory.HUE_RED;
                //  icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                it= SystemClock.currentThreadTimeMillis();
               // bmap= BitmapDescriptorFactory.HUE_VIOLET;
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

        if (confidence > constants.CONFIDENCE) {
            Log.e(TAG, "test");
            ActivityTT=label;
            switch (type) {
                case DetectedActivity.IN_VEHICLE: {

                    bmap= BitmapDescriptorFactory.HUE_AZURE;
                    //  icon = R.drawable.ic_driving;
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {

                    bmap= BitmapDescriptorFactory.HUE_BLUE;
                    //   icon = R.drawable.ic_on_bicycle;
                    break;
                }
                case DetectedActivity.ON_FOOT: {

                    bmap= BitmapDescriptorFactory.HUE_CYAN;
                    //   icon = R.drawable.ic_walking;
                    break;
                }
                case DetectedActivity.RUNNING: {

                    bmap= BitmapDescriptorFactory.HUE_GREEN;
                    //  icon = R.drawable.ic_running;
                    break;
                }
                case DetectedActivity.STILL: {

                    bmap= BitmapDescriptorFactory.HUE_MAGENTA;
                    break;
                }
                case DetectedActivity.TILTING: {

                    bmap= BitmapDescriptorFactory.HUE_ORANGE;
                    //  icon = R.drawable.ic_tilting;
                    break;
                }
                case DetectedActivity.WALKING: {

                    bmap= BitmapDescriptorFactory.HUE_RED;
                    //  icon = R.drawable.ic_walking;
                    break;
                }
                case DetectedActivity.UNKNOWN: {

                    bmap= BitmapDescriptorFactory.HUE_VIOLET;
                    break;
                }
            }
            /*txtActivity.setText(label);
            txtConfidence.setText("Confidence: " + confidence);
            *///  imgActivity.setImageResource(icon);
        }

    }




    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(constants.BROADCAST_DETECTED_ACTIVITY));
          }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startTracking() {
        Log.e(TAG, "test STARTTRACKING");
        Intent intent1 = new Intent(MapsActivity.this, BackgroundDetectedActivityService.class);
        startService(intent1);
    }

    private void stopTracking() {
        Intent intent = new Intent(MapsActivity.this, BackgroundDetectedActivityService.class);
        stopService(intent);
    }
    @Override
    public void onLocationChanged(Location location) {
        it= Math.abs((it- SystemClock.currentThreadTimeMillis()));
       if(ActivityTT=="Still" || ActivityTT== "In a vehicle" || ActivityTT== "Unknown activity") calo=0.0;
       else  calo= calorieCount(it);
        calorie+=calo;
        FinalCalories= String.valueOf(calorie);
        Log.e(ActivityTT, String.valueOf(bmap));
        Toast.makeText(this, "Calories: "+calorie , Toast.LENGTH_SHORT).show();

        if (calmap.containsKey(ActivityTT)) {
            Double temp= calmap.get(ActivityTT);
            temp+=calo;
            calmap.put(ActivityTT,temp);
            Log.e("Total Calories for "+ActivityTT, String.valueOf(calmap.get(ActivityTT)));
        } else {
            Double temp= 0.0;
            temp+=calo;
            calmap.put(ActivityTT,temp);
            Log.e("Total Calories for "+ActivityTT, String.valueOf(calmap.get(ActivityTT)));
        }



        if(prev.equalsIgnoreCase(ActivityTT)){
            te.add(new LatLng(location.getLatitude(), location.getLongitude()));
            prev=ActivityTT;
        }else{
            innerlist.add(te);

            if (eachmap.containsKey(ActivityTT)) {
                List<List> temp= eachmap.get(ActivityTT);
                temp.add(innerlist);
                eachmap.put(ActivityTT,temp);

                if (distmap.containsKey(ActivityTT)) {
                    Double ret= retDistance(te);
                    Double tempi= calmap.get(ActivityTT);
                    tempi+=ret;

                    distmap.put(ActivityTT,tempi);

                    Log.e("Total distance for "+ActivityTT, String.valueOf(distmap.get(ActivityTT)));

                } else {
                    Double ret= retDistance(te);
                    Double tempi= 0.0;
                    tempi+=ret;

                    distmap.put(ActivityTT,tempi);

                    Log.e("Total Distance for "+ActivityTT, String.valueOf(distmap.get(ActivityTT)));

                }
            } else {
                eachmap.put(ActivityTT,innerlist);
            }
            te.clear();
            te.add(new LatLng(location.getLatitude(), location.getLongitude()));
            prev=ActivityTT;
        }


        if (hashMap.containsKey(ActivityTT)) {

            List<LatLng> temp= hashMap.get(ActivityTT);
            temp.add(new LatLng(location.getLatitude(), location.getLongitude()));
            hashMap.put(ActivityTT,temp);

        } else {
            ArrayList<LatLng> temp=new ArrayList<>();
            temp.add(new LatLng(location.getLatitude(), location.getLongitude()));
            hashMap.put(ActivityTT,temp);


        }

        Log.e("HASH KEY", hashMap.keySet().toString());
        Log.e("HASH VALUE of"+ActivityTT+" :" ,hashMap.get(ActivityTT).toString());
      //  Toast.makeText(this,  hashMap.keySet().toString(), Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, hashMap.get(ActivityTT).toString(), Toast.LENGTH_SHORT).show();
            points.add(new LatLng(location.getLatitude(), location.getLongitude()));
            showDistance(points);
           // Toast.makeText(this, "Current Location: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            addMarker(location);


        }


        private double calorieCount(long t){

        Double cal= (Double) (((age * 0.2017) - (weight * 0.09036) + (heartrate * 0.6309) - 55.0969) * (t / 4.184));

        return cal;
        }


    private void addMarker(Location location){

        Log.e(TAG,ActivityTT);
       // Toast.makeText(this, ActivityTT, Toast.LENGTH_SHORT).show();
        marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title(ActivityTT)
                .icon(BitmapDescriptorFactory.defaultMarker(bmap)));

        addLine(te,ActivityTT);



    }

    private void addLine(List<LatLng> point,String activityTT) {


        switch (activityTT) {
            case "On a bicycle": {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#0000FF"))
                        .geodesic(true) );
                break;
            }
            case "On foot": {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#00FFFF"))
                        .geodesic(true) );
                break;
            }
            case "Running": {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#008000"))
                        .geodesic(true) );
                break;
            }
            case "Still": {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#FF00FF"))
                        .geodesic(true) );
                break;
            }
            case "Tilting": {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#FFA500"))
                        .geodesic(true) );
                break;
            }
            case "Unknown activity": {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#EE82EE"))
                        .geodesic(true) );
                break;
            }
            case "In a vehicle": {

                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#F0FFFF"))
                        .geodesic(true) );
                break;
            }
            case "Walking": {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(point)

                        .width(12)
                        .color(Color.parseColor("#FF0000"))
                        .geodesic(true) );
                break;
            }
        }


    }


    private Double retDistance(ArrayList<LatLng> points){
        Double distance = SphericalUtil.computeLength(points);
        distance=distance*0.000621371;// converting to miles

        Toast.makeText(this, "Distance covered: "+distance.toString() , Toast.LENGTH_SHORT).show();
        Log.e("DISTANCE COVERED",distance.toString());
        return distance;

    }


    private void showDistance(ArrayList<LatLng> points){
        Double distance = SphericalUtil.computeLength(points);
        distance=distance*0.000621371;// converting to miles

        Toast.makeText(this, "Distance covered: "+distance.toString() , Toast.LENGTH_SHORT).show();
        Log.e("DISTANCE COVERED",distance.toString());
      FinalDistance= String.valueOf(distance);


    }
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MapsActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
      //  Toast.makeText(MapsActivity.this, "GPS and Internet is now Available", Toast.LENGTH_SHORT).show();

    }    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }


    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }


}

