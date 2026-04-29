package com.skm.guideme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** @noinspection ALL*/
public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private MapView map;
    private MyLocationNewOverlay locationOverlay;
    private DBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        dbHelper = new DBHelper(this);

        String category = getIntent().getStringExtra("category");
        double lat = getIntent().getDoubleExtra("lat", 27.6644);
        double lng = getIntent().getDoubleExtra("lng", 85.3188);

        if (category == null || category.isEmpty()) {
            Toast.makeText(this, "No category selected. Returning to Home.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        map = findViewById(R.id.detailMap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        mapController.setCenter(new GeoPoint(lat, lng));

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        String overpassUrl = buildOverpassUrl(category, lat, lng);
        fetchPlaces(overpassUrl, this::addMarkersFromJson);
    }

    private String buildOverpassUrl(String category, double lat, double lng) {
        String filter;
        switch (category) {
            case "hotels":
                filter = "\"tourism\"=\"hotel\"";
                break;
            case "hospitals":
                filter = "\"amenity\"=\"hospital\"";
                break;
            case "banks":
                filter = "\"amenity\"=\"bank\"";
                break;
            case "tourism":
                filter = "\"tourism\"=\"attraction\"";
                break;
            default:
                filter = "";
                Toast.makeText(this, "Unknown category", Toast.LENGTH_SHORT).show();
        }

        return "https://overpass-api.de/api/interpreter?data=[out:json];(" +
                "node[" + filter + "](around:2000," + lat + "," + lng + ");" +
                ");out;";
    }

    private void fetchPlaces(String url, OnPlacesLoadedListener listener) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MapActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    runOnUiThread(() -> listener.onLoaded(jsonData));
                }
            }
        });
    }

    private void addMarkersFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray elements = obj.getJSONArray("elements");

            for (int i = 0; i < elements.length(); i++) {
                JSONObject el = elements.getJSONObject(i);

                if (!el.has("lat") || !el.has("lon") || !el.has("tags")) continue;

                JSONObject tags = el.getJSONObject("tags");
                if (!tags.has("name")) continue; // Skip unnamed

                String name = tags.getString("name");
                double lat = el.getDouble("lat");
                double lon = el.getDouble("lon");

                String description = dbHelper.getSummary(name);
                if (description == null) {
                    WikipediaFetcher fetcher = new WikipediaFetcher(this);
                    fetcher.getPlaceSummary(name, name, null);
                    description = "No description available.";
                }

                GeoPoint point = new GeoPoint(lat, lon);
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setTitle(name);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                String finalName = name;
                String finalDescription = description;

                marker.setOnMarkerClickListener((m, mapView) -> {
                    try {
                        Log.d("MARKER_CLICK", "Clicked: " + finalName);
                        Intent intent = new Intent(MapActivity.this, PlaceDetailActivity.class);
                        intent.putExtra("place_name", finalName);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lon", lon);
                        intent.putExtra("description", finalDescription);

                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MapActivity.this, "Error opening place detail", Toast.LENGTH_SHORT).show();
                        Log.e("MARKER_CLICK", "Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                });

                map.getOverlays().add(marker);
            }

            map.invalidate();
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
            Log.e("JSON_ERROR", e.getMessage());
            e.printStackTrace();
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (map != null) {
                locationOverlay.enableMyLocation();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    interface OnPlacesLoadedListener {
        void onLoaded(String json);
    }
}
