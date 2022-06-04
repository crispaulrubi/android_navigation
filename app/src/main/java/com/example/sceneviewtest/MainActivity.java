package com.example.sceneviewtest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "NOTICE";

    private ArrayList<Location> locations;
    private ArrayList<Role> roles;

    private ArrayAdapter<Location> adapter;
    private ArrayAdapter<Role> roleAdapter;

    private Spinner endLocations;
    private Spinner roleSpinner;

    private int endLocation = 0;
    private int roleID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locations = new ArrayList<>();
        roles = new ArrayList<>();
        endLocations = findViewById(R.id.end_location_spinner);
        roleSpinner = findViewById(R.id.role_spinner);

        volleyAPI("getLocations");
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
                    endLocation = location.getId();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.d(TAG, "Nothing");
                }
            });
        }

        if (roles != null) {
            roleAdapter = new ArrayAdapter<Role>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, roles);
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            roleSpinner.setAdapter(roleAdapter);
            roleAdapter.notifyDataSetChanged();
            roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Role role = (Role) adapterView.getItemAtPosition(i);
                    roleID = role.getId();
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
        ArrayList unformattedRoles = (ArrayList) allData.get("roles");

        if (unformattedRoles != null) {
            for (Object roleDetails: unformattedRoles) {
                Role role = new Gson().fromJson(roleDetails.toString(), Role.class);
                roles.add(role);
            }
        } else {
            Toast.makeText(MainActivity.this, "No roles found.", Toast.LENGTH_LONG).show();
        }

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

    public void startSceneform(View view) {
        if (endLocation != 0 && roleID != 0) {
            Intent intent = new Intent(MainActivity.this, Sceneview.class);
            Bundle args = new Bundle();
            args.putSerializable("LOCATIONS_ARRAY", (Serializable) locations);
            intent.putExtra("LOCATIONS", args);
            intent.putExtra("END_LOCATION", endLocation);
            intent.putExtra("ROLE_ID", roleID);

            startActivity(intent);
        }
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
                            getLocations(allData);

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