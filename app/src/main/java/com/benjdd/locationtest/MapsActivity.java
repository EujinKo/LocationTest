package com.benjdd.locationtest;

import androidx.fragment.app.FragmentActivity;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    ArrayList<Location> locationsFromFile = new ArrayList<>();

    private static final int LAT_INDICATOR = 0123;
    private static final int LNG_INDICATOR = 0456;

    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng uofa = new LatLng(32.230,-110.951);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uofa, 15.5F));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        try {
            addPolygonsFromFileId(R.raw.polygons);
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawLocations();
        drawPointsAndSearchPoints();
    }

    public void updateLocation(View v) {
        mMap.clear();
        drawLocations();
        drawPointsAndSearchPoints();
    }

    private void addPolygonsFromFileId(int id) throws Exception {
        Resources r = getResources();
        InputStream stream = r.openRawResource(id);
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = br.readLine()) != null) {
            Location location = new Location(line);
            locationsFromFile.add(location);
        }
    }

    private void drawLocations() {
        for (Location location : locationsFromFile) {
            /** TODO
             * Add the polygon, for each location.
             * (A) Create a PolygonOptions object
             * (B) Add polygon to the map
             * (C) Set the fill color
             */
            PolygonOptions polygonOptions = getPolygonOptionsForLocation(location);
            Polygon polygon = mMap.addPolygon(polygonOptions);
            polygon.setClickable(true);
            polygon.setFillColor(Integer.decode(location.locationColor));

        }
    }

    private void drawPointsAndSearchPoints() {
        for (Location location : locationsFromFile) {
            double avgLat = getAverage(location, LAT_INDICATOR);
            double avgLng = getAverage(location, LNG_INDICATOR);
            LatLng markerLocation = new LatLng(avgLat,avgLng);
            Marker m = mMap.addMarker(new MarkerOptions().position(markerLocation).title(location.locationName));
            m.setVisible(true);
            m.showInfoWindow();
            new DownloadTask().execute(avgLat + "," + avgLng);
        }
    }

    private PolygonOptions getPolygonOptionsForLocation(Location location) {
        PolygonOptions polygonOptions = new PolygonOptions().clickable(true);
        for (int i = 0; i < location.locations.size(); i++) {
            double lat = location.locations.get(i)[0];
            double lng = location.locations.get(i)[1];
            polygonOptions.add(new LatLng(lat, lng));
        }
        return polygonOptions;
    }

    private double getAverage(Location location, int latOrLong) {
        /** TODO
         * Get the average lattitude or longitude from the location object.
         */
        return 0.0; // replace
    }

    private class DownloadTask extends AsyncTask<String, Void, JSONObject> {

        private String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?&radius=1000&key=AIzaSyDAnrCI3RZzD6r8yxvFs79tuoMWlRXqM_Q";
        private String locComponent = "&location=";
        private String keyComponent = "&keyword=";
        String inputKeyword;

        @Override
        protected void onPreExecute() {
            inputKeyword = ((EditText)findViewById(R.id.keyword)).getText().toString();
        }

        @Override
        protected JSONObject doInBackground(String[] s) {
            try {
                String textJson = "";
                String line;
                String textUrl = baseUrl + locComponent + s[0] + keyComponent + inputKeyword;
                URL url = new URL(textUrl);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = in.readLine()) != null) {
                    textJson += line;
                }
                in.close();
                JSONObject json = new JSONObject(textJson);
                return json;
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            try {

                JSONArray results = json.getJSONArray("results");
                for (int i = 0 ; i < results.length(); i++) {
                    /** TODO
                     * Assume that the json variable will be a JSONObject populates with results from a places search.
                     * You should put a Marker on the map for each resulting location, with a corresponding label.
                     */
                    JSONObject loc = results.getJSONObject(i)
                            .getJSONObject("geometry")
                            .getJSONObject("location");
                    double lat = loc.getDouble("lat");
                    double lng = loc.getDouble("lng");
                    String label = results.getJSONObject(i)
                            .getString("name");

                    LatLng location = new LatLng(lat,lng);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
