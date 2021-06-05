package com.tamer.alna99.watertabdriver;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tapadoo.alerter.Alerter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private final int REQUEST_LOCATION_PERMISSION = 100;
    private final int REQUEST_LOCATION_SETTINGS = 10;
    private String email, username, phone, password, longitude, latitude;
    private TextInputEditText et_email, et_username, et_phone, et_password;
    private NetworkUtils networkUtils;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        networkUtils = NetworkUtils.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Get Location
                checkSettingsAndRequestLocation();
            } else {
                Toast.makeText(this, "location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_SETTINGS && resultCode == RESULT_OK) {
            requestLocation();
        } else {
            Toast.makeText(this, "location service is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_username = findViewById(R.id.et_username);
        et_phone = findViewById(R.id.et_phone);
    }

    private boolean checkFields() {
        if (!TextUtils.isEmpty(et_email.getText().toString())) {
            if (!TextUtils.isEmpty(et_username.getText().toString())) {
                if (!TextUtils.isEmpty(et_phone.getText().toString())) {
                    if (!TextUtils.isEmpty(et_password.getText().toString())) {
                        email = et_email.getText().toString();
                        username = et_username.getText().toString();
                        phone = et_phone.getText().toString();
                        password = et_password.getText().toString();
                        return true;
                    } else {
                        et_password.setError(getString(R.string.password_empty));
                        return false;
                    }
                } else {
                    et_phone.setError(getString(R.string.phone_empty));
                    return false;
                }
            } else {
                et_username.setError(getString(R.string.username_empty));
                return false;
            }
        } else {
            et_email.setError(getString(R.string.email_empty));
            return false;
        }
    }

    public void register(View view) {
        if (checkFields()) {
            checkSettingsAndRequestLocation();

            Call<ResponseBody> responseBodyCall = networkUtils.getApiInterface().register(username, email, longitude, latitude, password, phone);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    try {
                        if (response.body() != null) {
                            JsonObject root = new JsonParser().parse(response.body().string()).getAsJsonObject();
                            boolean success = root.get("success").getAsBoolean();
                            if (success) {
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                String message = root.get("success").getAsString();
                                showAlerter(message);
                            }
                        } else {
                            showAlerter(getString(R.string.email_is_used));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {

                }
            });
        }
    }

    public void login(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    public void back(View view) {
        onBackPressed();
        finish();
    }

    private void showAlerter(String message) {
        Alerter.create(this)
                .setText(message)
                .setDuration(3000)
                .setBackgroundColorRes(R.color.teal_200)
                .show();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
//        locationRequest.setInterval(5);
//        locationRequest.setFastestInterval(3);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                longitude = String.valueOf(location.getLongitude());
                latitude = String.valueOf(location.getLatitude());
                removeLocationUpdates();
            }
        };
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void checkSettingsAndRequestLocation() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Create location settings request
            LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest).build();
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);

            // Success
            task.addOnSuccessListener(locationSettingsResponse -> {
                // request location updates
                requestLocation();
            });

            // Failure
            task.addOnFailureListener(e -> {
                if (e instanceof ResolvableApiException) {
                    // if resolvable, ask the user  to enable location settings
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(RegisterActivity.this, REQUEST_LOCATION_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                        // Location is not available in this device
                        Toast.makeText(RegisterActivity.this, "Location Service unavailable", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Location is not available in this device
                    Toast.makeText(RegisterActivity.this, "Location Service unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}