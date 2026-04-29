package com.skm.guideme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** @noinspection ALL*/
public class PlaceDetailActivity extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private GeoPoint destinationPoint;
    private GeoPoint currentLocation;
    private TextView drivingTextView;

    private static final int REQUEST_PERMISSIONS = 1001;
    private static final String TAG = "PlaceDetailActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        PlaceDatabaseHelper dbHelper = new PlaceDatabaseHelper(this);
        dbHelper.insertHotelData(this);
        dbHelper.insertHospitalData(this);
        dbHelper.insertBankData(this);
        dbHelper.insertTouristAttractionData(this);

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        TextView titleText = findViewById(R.id.placeTitle);
        TextView descriptionText = findViewById(R.id.placeDescription);
        drivingTextView = findViewById(R.id.drivingTextView);
        mapView = findViewById(R.id.detailMap);
        ImageView placeImageView = findViewById(R.id.placeImageView);

        mapView.setMultiTouchControls(true);

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lon = getIntent().getDoubleExtra("lon", 0);
        String placeName = getIntent().getStringExtra("place_name");

        titleText.setText(placeName);
        destinationPoint = new GeoPoint(lat, lon);

        // 🔍 Get place from DB
        Place place = dbHelper.getPlaceDetails(placeName);

        if (place != null) {
            Log.d(TAG, "✅ Place found: " + place.getName());
            Log.d(TAG, "📄 Description: " + place.getDescription());
            Log.d(TAG, "🖼 Image Name: " + place.getImageName());

            descriptionText.setText(place.getDescription());

            if (place.getImageName() != null && !place.getImageName().isEmpty()) {
                String imagePath = "file:///android_asset/" + place.getImageName();
                Log.d(TAG, "📷 Loading image path: " + imagePath);
                Glide.with(this)
                        .load(imagePath)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(placeImageView);
            } else {
                Log.d(TAG, "⚠️ Image name empty, using placeholder.");
                placeImageView.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            Log.d(TAG, "❌ Place not found for: " + placeName);
            descriptionText.setText("No description available.");
            placeImageView.setImageResource(R.drawable.placeholder_image);
        }

        requestLocationPermissions();
    }

    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS);
        } else {
            setupMapAndLocation();
        }
    }

    private void setupMapAndLocation() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        locationOverlay.enableMyLocation();

        locationOverlay.runOnFirstFix(() -> {
            if (locationOverlay.getMyLocation() != null) {
                currentLocation = new GeoPoint(
                        locationOverlay.getMyLocation().getLatitude(),
                        locationOverlay.getMyLocation().getLongitude()
                );

                runOnUiThread(() -> {
                    IMapController controller = mapView.getController();
                    controller.setZoom(15.0);
                    controller.setCenter(currentLocation);

                    addMarkers();
                    fetchDrivingRouteFromOSRM();
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_LONG).show()
                );
            }
        });

        mapView.getOverlays().add(locationOverlay);
    }

    private void addMarkers() {
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(currentLocation);
        startMarker.setTitle("Your Location");
        mapView.getOverlays().add(startMarker);

        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(destinationPoint);
        endMarker.setTitle("Destination");
        mapView.getOverlays().add(endMarker);
    }

    private void fetchDrivingRouteFromOSRM() {
        String url = String.format(Locale.ENGLISH,
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                currentLocation.getLongitude(), currentLocation.getLatitude(),
                destinationPoint.getLongitude(), destinationPoint.getLatitude());

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PlaceDetailActivity.this, "Failed to fetch driving route", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject route = routes.getJSONObject(0);

                        double distance = route.getDouble("distance");
                        double duration = route.getDouble("duration");

                        String distanceKm = String.format(Locale.ENGLISH, "%.2f km", distance / 1000);
                        String durationMin = String.format(Locale.ENGLISH, "%.0f min", duration / 60);

                        JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");
                        List<GeoPoint> geoPoints = new ArrayList<>();
                        for (int i = 0; i < coords.length(); i++) {
                            JSONArray point = coords.getJSONArray(i);
                            double lon = point.getDouble(0);
                            double lat = point.getDouble(1);
                            geoPoints.add(new GeoPoint(lat, lon));
                        }

                        runOnUiThread(() -> {
                            Polyline polyline = new Polyline();
                            polyline.setPoints(geoPoints);
                            polyline.setWidth(8f);
                            mapView.getOverlays().add(polyline);
                            mapView.invalidate();

                            drivingTextView.setText("Driving: " + distanceKm + ", " + durationMin);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMapAndLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
