package com.example.sceneviewtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.InstructionsController;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sceneview extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnSessionConfigurationListener,
        Runnable {

    private final String TAG = "NOTICE";
    private final Map<String, Float> directionAngles = new HashMap<>();
    private final List<Vector3> oldPositions = new ArrayList<>();

    private final Map<String, Boolean> imageDetectionStatus = new HashMap<>();
    private final Map<String, Bitmap> bitmapImages = new HashMap<>();
    private AugmentedImageDatabase database;
    private String detectedImage = null;

    private ArrayList<Location> locations;
    private Conversion conversions;
    private Path currentPosition;
    private String currentDirection;
    private ArrayList<Path> generatedPath;

    private TextView arrowCoordinates;

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    private Vector3 vector;
    private AnchorNode parentNode;
    private TransformableNode node;
    private Camera camera;

    private int counter = 0;
    private int endLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneview);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("LOCATIONS");
        generatedPath = new ArrayList<>();

        locations = (ArrayList<Location>) args.getSerializable("LOCATIONS_ARRAY");
        endLocation = intent.getIntExtra("END_LOCATION", 0);

        Thread thread = new Thread(this);
        thread.start();

        vector = new Vector3();

        getSupportFragmentManager().addFragmentOnAttachListener(this);
        arrowCoordinates = findViewById(R.id.arrowPosition);

        startSceneform(savedInstanceState);
    }

    @Override
    public void run() {
        try {
            URL url;
            for (Location location: locations) {
                String filename = location.getPath();
                Log.d(TAG, Constants.baseURL + Constants.markerLocation + filename);
                url = new URL(Constants.baseURL + Constants.markerLocation + filename);
                Bitmap image = BitmapFactory.decodeStream(url.openStream());
                filename = filename.replace(".png", "");
                bitmapImages.put(filename, image);
                imageDetectionStatus.put(filename, false);
            }
        } catch(IOException e) {
            System.out.println(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        camera = arFragment.getArSceneView().getScene().getCamera();
    }

    private void populateDirectionAngles() {
        directionAngles.put("north", 180.0f);
        directionAngles.put("south", 0.0f);
        directionAngles.put("west", 90.0f);
        directionAngles.put("east", 270.0f);
    }

    private void adjustDirectionAngles() {
        if (directionAngles != null && !currentDirection.equals("")) {
            switch (currentDirection) {
                case "north":
                    populateDirectionAngles();
                    break;
                case "south":
                    directionAngles.put("south", 180.0f);
                    directionAngles.put("north", 0.0f);
                    directionAngles.put("west", 270.0f);
                    directionAngles.put("east", 90.0f);
                    break;
                case "east":
                    directionAngles.put("south", 270.0f);
                    directionAngles.put("north", 90.0f);
                    directionAngles.put("west", 0.0f);
                    directionAngles.put("east", 180.0f);
                    break;
                case "west":
                    directionAngles.put("south", 90.0f);
                    directionAngles.put("north", 270.0f);
                    directionAngles.put("west", 180.0f);
                    directionAngles.put("east", 0.0f);
                    break;
            }
        }
    }

    private void createNewArrow(Vector3 oldNodePosition) {
        float[] steps = computeStep();
        float newXPosition = oldNodePosition.x - steps[0];
        float newZPosition = oldNodePosition.z - steps[1];

//        vector.set(oldNodePosition.x, -1f, oldNodePosition.z - 1);
        vector.set(newXPosition, -1f, newZPosition);

        parentNode = new AnchorNode();
        parentNode.setParent(arFragment.getArSceneView().getScene());

        node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(this.model);
        node.getScaleController().setMinScale(0.0f);
        node.getScaleController().setMaxScale(3.0f);
        node.setLocalScale(new Vector3(0.02f, 0.02f, 0.02f));

        Float currentAngle = directionAngles.get(currentDirection);
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), currentAngle == null ? 180.0f : currentAngle));

        node.setParent(parentNode);

        node.setWorldPosition(vector);
    }

    private float[] computeStep() {
        float xDifference = 0.0f;
        if (currentPosition.getX_coord() > generatedPath.get(0).getX_coord()) {
            xDifference = currentPosition.getX_coord() - generatedPath.get(0).getX_coord();
        } else {
            xDifference = generatedPath.get(0).getX_coord() - currentPosition.getX_coord();
        }

        float yDifference = 0.0f;
        if (currentPosition.getY_coord() > generatedPath.get(0).getY_coord()) {
            yDifference = currentPosition.getY_coord() - generatedPath.get(0).getY_coord();
        } else {
            yDifference = generatedPath.get(0).getY_coord() - currentPosition.getY_coord();
        }

        xDifference = conversions.getValue_two() * xDifference;
        yDifference = conversions.getValue_two() * yDifference;

        float[] steps = {xDifference, yDifference};
        if (currentDirection.equals("east") || currentDirection.equals("west"))
            steps = new float[]{yDifference, xDifference};

        return steps;
    }

    private void updateCurrentDirection() {
        // Note: North = forward, South = backward, west = left, east = right

        if (currentPosition.getX_coord() > generatedPath.get(0).getX_coord()) {
            currentDirection = "west";
        } else if (currentPosition.getX_coord() < generatedPath.get(0).getX_coord()) {
            currentDirection = "east";
        } else if (currentPosition.getY_coord() > generatedPath.get(0).getY_coord()) {
            currentDirection = "north";
        } else if (currentPosition.getY_coord() < generatedPath.get(0).getY_coord()) {
            currentDirection = "south";
        }
    }

    private void startSceneform(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        loadModels();
    }

    private void removeOldArrow() {
        // Remove old arrow
        arFragment.getArSceneView().getScene().removeChild(parentNode);
        parentNode.removeChild(node);
        parentNode.setParent(null);
        parentNode.setRenderable(null);
    }

    private boolean hasMoved(Vector3 oldPosition, Vector3 newPosition, Vector3 arrowPosition) {
        Vector3 expectedCoordinates = new Vector3(oldPosition.x + arrowPosition.x, oldPosition.y + arrowPosition.y, oldPosition.z + arrowPosition.z);

        double x_difference = Math.abs(expectedCoordinates.x - newPosition.x);
        double z_difference = Math.abs(expectedCoordinates.z - newPosition.z);

        return (x_difference < 0.2 && z_difference < 0.2);
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }

        // Disable plane detection
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);

        database = new AugmentedImageDatabase(session);


        // Every image has to have its own unique String identifier
        for (String key: bitmapImages.keySet()) {
            database.addImage(key, bitmapImages.get(key));
        }

        config.setAugmentedImageDatabase(database);

        // Check for image detection
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate);
    }

    public void onAugmentedImageTrackingUpdate(AugmentedImage augmentedImage) {
        // If there are both images already detected, for better CPU usage we do not need scan for them
        counter++;
        if (detectedImage != null && node != null && counter % 10 == 0) {
            arrowCoordinates.setText(node.getWorldPosition().toString());

            if (hasMoved(oldPositions.get(oldPositions.size()-1), camera.getLocalPosition(), node.getWorldPosition())) {
                if (generatedPath.size() > 0) {
                    currentPosition = generatedPath.get(0);
                    generatedPath.remove(0);
                    updateCurrentDirection();
                    adjustDirectionAngles();

                    Vector3 oldNodePosition = node.getWorldPosition();
                    removeOldArrow();
                    createNewArrow(oldNodePosition);
                } else {
                    Log.d(TAG, "Arrived!! Hooray!!");
                }
            }
        }

        if (detectedImage == null && bitmapImages != null && augmentedImage.getTrackingState() == TrackingState.TRACKING
                && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {

            // Setting anchor to the center of Augmented Image
            AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));

            for (String key: bitmapImages.keySet()) {
                if (!imageDetectionStatus.get(key) && augmentedImage.getName().equals(key)) {

                    imageDetectionStatus.put(key, true);
                    detectedImage = key;

                    String[] locationArray = key.split("_");
                    getFinalPath(locationArray[1], String.valueOf(endLocation), anchorNode, key);
                }
            }
        }
        Log.d(TAG, detectedImage == null ? "null": detectedImage);
        if (detectedImage != null) {
            arFragment.getInstructionsController().setEnabled(
                    InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false);
        }
    }

    public void getFinalPath(String start_location, String end_location, AnchorNode anchorNode, String key) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("start_location", start_location);
        parameters.put("end_location", end_location);

        volleyAPI("getPaths", parameters, anchorNode, key);
    }

    private void getPaths(HashMap allData, AnchorNode anchorNode, String key) throws Exception {
        ArrayList paths = (ArrayList) allData.get("paths");
        conversions = new Gson().fromJson(allData.get("conversions").toString(), Conversion.class);

        if (paths != null) {
            for (Object coordinate: paths) {
                Path path = new Gson().fromJson(coordinate.toString(), Path.class);
                generatedPath.add(path);
            }

            if (generatedPath.size() > 0) {
                currentPosition = generatedPath.get(0);
                generatedPath.remove(0);

                populateDirectionAngles();
                updateCurrentDirection();
                adjustDirectionAngles();
                oldPositions.add(camera.getLocalPosition());
            }

            displayFirstArrow(anchorNode, key);
        } else {
            Log.d(TAG, "null");
            Toast.makeText(Sceneview.this, "No generatedPath possible from source to destination.", Toast.LENGTH_LONG).show();
        }
    }

    private void displayFirstArrow(AnchorNode anchorNode, String key) {
        Log.d(TAG, "displayFirstArrow");
        Toast.makeText(this, key + " tag detected", Toast.LENGTH_LONG).show();

        anchorNode.setWorldScale(new Vector3(0.01f, 0.01f, 0.01f));
        arFragment.getArSceneView().getScene().addChild(anchorNode);

        node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(this.model);
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), directionAngles.get(currentDirection)));
        anchorNode.addChild(node);
    }

    private void volleyAPI(String type, Map<String, String> parameters, AnchorNode anchorNode, String key) {
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

                            getPaths(allData, anchorNode, key);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
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

    public void loadModels() {
        WeakReference<Sceneview> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, R.raw.scene)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    Sceneview activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.activity_sceneview)
                .build()
                .thenAccept(viewRenderable -> {
                    Sceneview activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }
}