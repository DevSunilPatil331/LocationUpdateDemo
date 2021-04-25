package com.android.locationdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 1244;
    private String TAG="MAinActivity";
    private static final int RQUEST_PERMISSION_CODE = 5421;
    TextView txt_location_updates,txt_last_location;
    String[] permissions=new String[]{  Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION};
    private  FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient=new FusedLocationProviderClient(this);

        initViews();
        clickEvents();
    }

    private void clickEvents() {
        txt_location_updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkForPermissions()){
                    //do your work
                    //getLastLocation();
                    getCurrentLocation();
                }else{
                    requestPermissions(permissions,RQUEST_PERMISSION_CODE);
                }
            }
        });
    }
    LocationRequest locationRequest;
    private void getCurrentLocation(){
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setMaxWaitTime(60000*5);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder(); // new builder

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this,new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                Log.e(TAG, "LocationSettingsResponse " );
                if (locationSettingsResponse!=null){
                    LocationSettingsStates states=locationSettingsResponse.getLocationSettingsStates();
                    boolean is_location_present=states.isLocationPresent();
                    boolean is_network_location_present=states.isNetworkLocationPresent();
                    boolean is_gps_present=states.isGpsPresent();
                    Log.e(TAG, "onSuccess: location_present="+is_location_present+"\nis_network_location_present="+is_network_location_present+
                            "\nis_gps_present="+is_gps_present);
                    getLocationUpdates();
                }

            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void getLocationUpdates() {

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e(TAG, "onLocationResult: location="+locationResult.toString() );
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        },getMainLooper());
    }

    private void getLastLocation() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.e(TAG, "onSuccess: Location= "+location );
                if (location!=null){
                    Toast.makeText(MainActivity.this,"Lat="+location.getLatitude()+"\nLon="+location.getLongitude(),Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Location Not Found", Toast.LENGTH_SHORT).show();

                }
            }

        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to get Location\nError: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this,permissions[0])== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,permissions[1])== PackageManager.PERMISSION_GRANTED ){
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RQUEST_PERMISSION_CODE:

                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void initViews() {
        txt_location_updates=findViewById(R.id.txt_location_updates);
        txt_last_location=findViewById(R.id.txt_last_location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CHECK_SETTINGS){
            getLocationUpdates();
        }
    }
}