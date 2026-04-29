package com.skm.guideme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;



/** @noinspection ALL*/
public class HomeActivity extends AppCompatActivity {

    TextView tvLocation;
    FusedLocationProviderClient fusedLocationClient;
    double latitude = 27.6644, longitude = 85.3188; // Default to Lalitpur center

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final int GPS_ENABLE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvLocation = findViewById(R.id.tvLocation);
        Button btnHotels = findViewById(R.id.btnHotels);
        Button btnHospitals = findViewById(R.id.btnHospitals);
        Button btnBanks = findViewById(R.id.btnBanks);
        Button btnTourist = findViewById(R.id.btnTourist);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Logout from Firebase
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close HomeActivity
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationSettings(); // Prompt for GPS and permissions

        View.OnClickListener listener = v -> {
            String category = "";

            int id = v.getId();
            if (id == R.id.btnHotels) {
                category = "hotels";
            } else if (id == R.id.btnHospitals) {
                category = "hospitals";
            } else if (id == R.id.btnBanks) {
                category = "banks";
            } else if (id == R.id.btnTourist) {
                category = "tourism";
            }
            Intent intent = new Intent(HomeActivity.this, MapActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("lat", latitude);
            intent.putExtra("lng", longitude);
            startActivity(intent);


        };

        btnHotels.setOnClickListener(listener);
        btnHospitals.setOnClickListener(listener);
        btnBanks.setOnClickListener(listener);
        btnTourist.setOnClickListener(listener);
    }

    @SuppressLint("SetTextI18n")
    private void checkLocationSettings() {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10_000)
                .setFastestInterval(5_000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // Settings are satisfied, proceed to get location
            getLocation();
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(HomeActivity.this, GPS_ENABLE_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    tvLocation.setText("Failed to prompt GPS enable.");
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                tvLocation.setText("Your Location: " + latitude + ", " + longitude);
            } else {
                tvLocation.setText("Unable to get location. Showing default location.");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            tvLocation.setText("Location permission denied. Using default.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_ENABLE_REQUEST) {
            checkLocationSettings();
        }
    }
}
