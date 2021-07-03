package com.tamer.alna99.watertabdriver.view.fragments;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
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
import com.tamer.alna99.watertabdriver.R;
import com.tamer.alna99.watertabdriver.model.AnswerInterface;
import com.tamer.alna99.watertabdriver.model.MySocket;
import com.tamer.alna99.watertabdriver.model.Result;
import com.tamer.alna99.watertabdriver.model.SharedPrefs;
import com.tamer.alna99.watertabdriver.viewmodel.MapViewModel;

import org.json.JSONException;
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
    private final Emitter.Listener newOrderListener = args -> {
        Log.d("dddd", "Listener on");
        JSONObject data = (JSONObject) args[0];
        Log.d("ddd", data.toString());

        BottomSheetFragment sheetFragment = new BottomSheetFragment(new AnswerInterface() {
            @Override
            public void answer(int answer) {
                JSONObject data2 = new JSONObject();
                try {
                    data2.put("answer", answer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("driverAnswer", data2);

                JSONObject data3 = new JSONObject();
                try {
                    data3.put("orderFinish", "finish");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("orderFinish", data3);
            }
        });
        sheetFragment.show(getChildFragmentManager(), "Tag");
    };
    private ProgressBar progressBar;
    private Group group;
    private String id;
    private MapViewModel mapViewModel;
    private Socket socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        id = SharedPrefs.getUserId(requireContext());
        mapViewModel = new MapViewModel();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        checkSettingsAndRequestLocationUpdates();
        createLocationRequest();
        createLocationCallback();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        group = view.findViewById(R.id.group);
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        socket = MySocket.getInstance();
        socket.connect();

        JSONObject data = new JSONObject();
        try {
            data.put("id", id);
            data.put("isDriver", "true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("join", data);

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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Create location settings request
            LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest).build();
            SettingsClient settingsClient = LocationServices.getSettingsClient(requireActivity());
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
                        resolvableApiException.startResolutionForResult(requireActivity(), REQUEST_LOCATION_SETTINGS);
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
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
//        mapViewModel.updateResult().addObserver((observable, o) -> {
//            Result result = (Result) o;
//            switch (result.status){
//                case SUCCESS:
//                    Location location = (Location) result.data;
//                    supportMapFragment.getMapAsync(MapFragment.this);
//                    removeLocationUpdates();
//                    progressBar.setVisibility(View.GONE);
//                    group.setVisibility(View.VISIBLE);
//                    double [] locations = {location.getLongitude(), location.getLatitude()};
//
//                    mapViewModel.updateResult().addObserver((observable2, o2) -> {
//                        Result result2 = (Result) o2;
//                        switch (result2.status){
//                            case SUCCESS:
//                                Log.d("dddd", String.valueOf(result2.data));
//                                break;
//                            case ERROR:
//                                Log.d("dddd", getString(R.string.error));
//                                break;
//                        }
//                    });
//
//                    mapViewModel.updateLocation(id, locations);
//                    break;
//                case ERROR:
//                    Log.d("dddd", getString(R.string.error));
//                    break;
//            }
//        });
//
//        mapViewModel.requestLocation(locationCallback);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                supportMapFragment.getMapAsync(MapFragment.this);
                removeLocationUpdates();
                progressBar.setVisibility(View.GONE);
                group.setVisibility(View.VISIBLE);
                double[] locations = {location.getLongitude(), location.getLatitude()};

                mapViewModel.updateResult().addObserver((observable, o) -> {
                    Result result = (Result) o;
                    switch (result.status) {
                        case SUCCESS:
                            Log.d("dddd", String.valueOf(result.data));
                            break;
                        case ERROR:
                            Log.d("dddd", getString(R.string.error));
                            break;
                    }
                });

                mapViewModel.updateLocation(id, locations);
            }
        };
    }

    private void removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}