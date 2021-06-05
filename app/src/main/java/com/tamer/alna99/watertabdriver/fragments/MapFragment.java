package com.tamer.alna99.watertabdriver.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.CameraPosition;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.tamer.alna99.watertabdriver.MySocket;
import com.tamer.alna99.watertabdriver.R;

import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final int REQUEST_LOCATION_PERMISSION = 100;
    private final int REQUEST_LOCATION_SETTINGS = 10;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location location;
    private SupportMapFragment supportMapFragment;
    private final Emitter.Listener newOrderListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("ddd", data.toString());
        }
    };
    private Socket socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        checkSettingsAndRequestLocationUpdates();
        createLocationRequest();
        createLocationCallback();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        socket = MySocket.getInstance();
        socket.connect();
        socket.on("newOrder", newOrderListener);


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.off("newOrder", newOrderListener);
        socket.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng);
        googleMap.addMarker(markerOptions);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .zoom(18)
                .bearing(30)
                .target(latLng)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_SETTINGS && resultCode == Activity.RESULT_OK) {
            requestLocationUpdates();
        } else {
            Toast.makeText(getContext(), "location service is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Get Location
                checkSettingsAndRequestLocationUpdates();
                createLocationRequest();
                createLocationCallback();
            } else {
                Toast.makeText(getContext(), "location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkSettingsAndRequestLocationUpdates() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Create location settings request
            LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest).build();
            SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);

            // Success
            task.addOnSuccessListener(locationSettingsResponse -> {
                // request location updates
                requestLocationUpdates();
            });

            // Failure
            task.addOnFailureListener(e -> {
                if (e instanceof ResolvableApiException) {
                    // if resolvable, ask the user  to enable location settings
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(getActivity(), REQUEST_LOCATION_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                        // Location is not available in this device
                        Toast.makeText(getContext(), "Location Service unavailable", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Location is not available in this device
                    Toast.makeText(getContext(), "Location Service unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(3);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d("dddd", "Longitude: " + location.getLongitude());
                    Log.d("dddd", "Latitude: " + location.getLatitude());
                    removeLocationUpdates();
                    supportMapFragment.getMapAsync(MapFragment.this);
                }

            }
        };
    }

    private void removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


}