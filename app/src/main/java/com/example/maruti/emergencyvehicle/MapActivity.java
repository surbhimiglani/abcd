package com.example.maruti.emergencyvehicle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity  extends FragmentActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback, LocationListener {

    ImageView imageView, imageView12;
    private static final String LOG_TAG = "Activity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient, client;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    SharedPreferences sf3;
    private static String preference = "pref3";
    private static String saveIt = "Savekey3";
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private Marker currentLocation, marker;
    public Location lastLocation;
    private static final int REQUEST_PERMISION_CODE = 99;
    Geocoder geocoder;
    LatLng latLng, latLng2;
    LoadMap lm;
    android.os.Handler handler;
    ScrollView scrolPob;
    GestureDetectorCompat gestureDetector;
    TextView skip;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        geocoder = new Geocoder(this);
        client = new GoogleApiClient.Builder(MapActivity.this)                   // client declared for the autocomplete textview
                .addApi(Places.GEO_DATA_API)
                .build();
        // .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
        handler=new Handler();

        sf3 = getSharedPreferences(preference, Context.MODE_PRIVATE);

        lm=new LoadMap();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()          //   map initialized to fragment
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        else
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if(mMap!=null){
                    mMap.setMyLocationEnabled(true);}            }
        }


        Bundle extras= getIntent().getExtras();

        address = extras.getString("destination");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permisision denied", Toast.LENGTH_LONG).show();
                }
        }
        return;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "Google Places API connected.");

        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISION_CODE);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        Intent ab = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(ab);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();                   // initialize the client to show current and selected location on map
                mMap.setMyLocationEnabled(true);}                     // current location enabled
                savedPosition();                                     // on resuming this activity show the location entered in the textview on the map

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        if (currentLocation != null) {
            currentLocation.remove();
        }

        lm=new LoadMap();
        lm.execute();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    Runnable updateMarker = new Runnable() {
        @Override
        public void run() {
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng2));

            handler.postDelayed(this, 2000);

        }
    };

    public void savedPosition() {
        mMap.clear();
        List<Address> addressList = null;
        MarkerOptions mo = new MarkerOptions();
        if(!address.equals("")) {
            try {
                addressList = geocoder.getFromLocationName(address, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (addressList != null) {
            for(int i = 0; i < addressList.size(); i++) {
                Address myAddress = addressList.get(i);
                latLng2 = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                mo.position(latLng2);
                mo.title("Your entered location");
                mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                marker=mMap.addMarker(mo);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 8));
                handler.postDelayed(updateMarker, 2000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(updateMarker);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }

    class LoadMap extends AsyncTask<Void, Void, LatLng> {


        @Override
        protected LatLng doInBackground(Void... objects) {

            latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());            // current location in the map
            return latLng;
        }

        @Override
        protected void onPostExecute(LatLng location) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(location);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            currentLocation = mMap.addMarker(markerOptions);

            List<Address> addressList = null;
            MarkerOptions mo = new MarkerOptions();
            if (!address.equals("")) {
                try {
                    addressList = geocoder.getFromLocationName(address, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (addressList != null) {
                for (int i = 0; i < addressList.size(); i++) {
                    Address myAddress = addressList.get(i);
                    LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                    mo.position(latLng);
                    mo.title("Your entered location");
                    mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    mMap.addMarker(mo);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
                }

            }

        }
    }

    private boolean isNetworkAvailable() {                           // check if the network is available
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
