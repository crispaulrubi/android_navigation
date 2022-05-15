package com.example.sceneviewtest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "NOTICE: ";
    private ArrayList<Path> generatedPath;
    private Conversion conversions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generatedPath = new ArrayList<>();
    }

    public void getFinalPath(View view) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.12/navigation_api/api/get_path";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject result = null;
                        try {
                            result = new JSONObject(response);
                            HashMap allData = new Gson().fromJson(result.toString(), HashMap.class);
                            ArrayList paths = (ArrayList) allData.get("paths");
                            conversions = (Conversion) allData.get("conversions");

                            for (Object coordinate: paths) {
                                Path path = new Gson().fromJson(coordinate.toString(), Path.class);
                                generatedPath.add(path);
                            }

                            if (generatedPath != null)
                                startSceneform();
                            else
                                Toast.makeText(MainActivity.this, "No paths possible from source to destination.", Toast.LENGTH_LONG).show();
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
                params.put("start_location", "1");
                params.put("end_location", "3");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
}