package com.busenamli.travelbook;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this::onMapLongClick);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) {

        /*LatLng sydney = new LatLng(-34,151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));}}*/

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(@NonNull Location location) {

                    //Lokasyon değişmeden gezinti yapmak
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.busenamli.mytravelbook", MODE_PRIVATE);
                    boolean firstTimeCheck = sharedPreferences.getBoolean("notFirstTime", true);

                    if (!firstTimeCheck) {

                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        //System.out.println("location: " + location);
                        sharedPreferences.edit().putBoolean("notFirstTime", true).apply();

                    }

                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    //System.out.println("location: " + location);

                }
            };

            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, locationListener);

                    mMap.clear();
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation != null) {

                        //LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        FirstTimeCheck(lastLocation);
                    }
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, locationListener);

                mMap.clear();
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {

                    //LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                    FirstTimeCheck(lastLocation);
                }

            }

        }else{
            mMap.clear();
            int position = intent.getIntExtra("position",0);
            LatLng location = new LatLng(MainActivity.locations.get(position).latitude, MainActivity.locations.get(position).longitude);
            String placeName = MainActivity.names.get(position);

            mMap.addMarker(new MarkerOptions().title(placeName).position(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(grantResults.length > 0)
            if(requestCode == 1){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, locationListener);

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");

                    if(info.matches("new")){

                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if(lastLocation != null){

                            //LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                            FirstTimeCheck(lastLocation);
                        }

                    }else{
                        mMap.clear();
                        int position = intent.getIntExtra("position",0);
                        LatLng location = new LatLng(MainActivity.locations.get(position).latitude, MainActivity.locations.get(position).longitude);
                        String placeName = MainActivity.names.get(position);

                        mMap.addMarker(new MarkerOptions().title(placeName).position(location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
                    }

                }

            }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void FirstTimeCheck(Location lastLocation){
        LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
    }

    public void onMapLongClick(LatLng latLng){

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String adress = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

            if(addressList != null && addressList.size()>0){
                if(addressList.get(0).getThoroughfare() !=null){
                    adress += addressList.get(0).getThoroughfare();
                    if (addressList.get(0).getSubThoroughfare() != null){
                        adress += "" + addressList.get(0).getSubThoroughfare();
                    }
                }
            }else {
                adress = "New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().title(adress).position(latLng));
        Toast.makeText(getApplicationContext(),"New Place Created!", Toast.LENGTH_SHORT).show();

        MainActivity.names.add(adress);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        try {

            Double l1 = latLng.latitude;
            Double l2 = latLng.longitude;

            String coor1 = l1.toString();
            String coor2 = l2.toString();

            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places(name VARCHAR, latitude VARCHAR, longitude VARCHAR)");

            String toCompile = "INSERT INTO places (name,latitude,longitude) VALUES (?,?,?)";

            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);

            sqLiteStatement.bindString(1,adress);
            sqLiteStatement.bindString(2,coor1);
            sqLiteStatement.bindString(3,coor2);

            sqLiteStatement.execute();


        }catch (Exception e){

        }

    }
}