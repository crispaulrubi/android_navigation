package com.example.sceneviewtest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "NOTICE: ";
    private ArrayList<Path> generatedPath;
    private Conversion conversions;

    private ArrayList<Location> locations;

    private ArrayAdapter<Location> adapter;
    private Spinner endLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locations = new ArrayList<>();
        generatedPath = new ArrayList<>();
        endLocations = findViewById(R.id.end_locations_spinner);

        volleyAPI("getLocations");
    }

    public void getFinalPath(View view) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("start_location", "1");
        parameters.put("end_location", "3");

        volleyAPI("getPaths", parameters);
    }

    private void initializeUI() {
        if (locations != null) {
            adapter = new ArrayAdapter<Location>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, locations);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            endLocations.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            endLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Location location = (Location) adapterView.getItemAtPosition(i);
                    Log.d(TAG, location.toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.d(TAG, "Nothing");
                }
            });
        }
    }

    private void getLocations(HashMap allData) {
        ArrayList unformattedLocations = (ArrayList) allData.get("locations");

        if (unformattedLocations != null) {
            for (Object locationDetails: unformattedLocations) {
                Location location = new Gson().fromJson(locationDetails.toString(), Location.class);
                locations.add(location);
            }
            initializeUI();
        } else {
            Toast.makeText(MainActivity.this, "No locations found.", Toast.LENGTH_LONG).show();
        }
    }

    private void getPaths(HashMap allData) {
        ArrayList paths = (ArrayList) allData.get("paths");
        conversions = new Gson().fromJson(allData.get("conversions").toString(), Conversion.class);

        if (paths != null) {
            for (Object coordinate: paths) {
                Path path = new Gson().fromJson(coordinate.toString(), Path.class);
                generatedPath.add(path);
            }
            startSceneform();
        } else {
            Toast.makeText(MainActivity.this, "No paths possible from source to destination.", Toast.LENGTH_LONG).show();
        }
    }

    private void startSceneform() {
        Intent intent = new Intent(MainActivity.this, Sceneview.class);
        Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST", (Serializable) generatedPath);
        intent.putExtra("PATHS", args);

        if (conversions != null) {
            intent.putExtra("CONVERSIONS", conversions);
        }

        startActivity(intent);
    }

    private void volleyAPI(String type, Map<String, String> parameters) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_LINKS.get(type);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject result = null;
                        try {
                            result = new JSONObject(response);
                            HashMap allData = new Gson().fromJson(result.toString(), HashMap.class);

                            switch (type) {
                                case "getPaths":
                                    getPaths(allData);
                                    break;
                                case "getLocations":
                                    getLocations(allData);
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Failed with error msg:\t" + error.getMessage());
                Log.d(TAG, "Error StackTrace: \t" + error.getStackTrace());
                try {
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e(TAG, new String(htmlBodyBytes), error);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }) {@Override
        public Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();

            for (String key: parameters.keySet()) {
                params.put(key, parameters.get(key));
            }
            return params;
        }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void volleyAPI(String type) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_LINKS.get(type);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject result = null;
                        try {
                            result = new JSONObject(response);
                            HashMap allData = new Gson().fromJson(result.toString(), HashMap.class);

                            switch (type) {
                                case "getPaths":
                                    getPaths(allData);
                                    break;
                                case "getLocations":
                                    getLocations(allData);
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Failed with error msg:\t" + error.getMessage());
                Log.d(TAG, "Error StackTrace: \t" + error.getStackTrace());
                try {
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e(TAG, new String(htmlBodyBytes), error);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}