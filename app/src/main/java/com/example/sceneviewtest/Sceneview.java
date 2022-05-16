package com.example.sceneviewtest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sceneview extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private final String TAG = "NOTICE: ";
    private final Map<String, Float> directionAngles = new HashMap<>();

    private ArrayList<Path> paths;
    private Conversion conversions;
    private Path currentPosition;
    private String currentDirection;

    private TextView arrowCoordinates;

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    private Vector3 vector;
    private AnchorNode parentNode;
    private TransformableNode node;
    private Camera camera;

    private final List<Vector3> oldPositions = new ArrayList<>();

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneview);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("PATHS");
        paths = (ArrayList<Path>) args.getSerializable("ARRAYLIST");
        if (paths.size() > 0) {
            currentPosition = paths.get(0);
            paths.remove(0);
        }

        populateDirectionAngles();
        updateCurrentDirection();
        adjustDirectionAngles();
        conversions = (Conversion) getIntent().getSerializableExtra("CONVERSIONS");

        vector = new Vector3();

        getSupportFragmentManager().addFragmentOnAttachListener(this);
        arrowCoordinates = findViewById(R.id.arrowPosition);

        startSceneform(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        vector.set(0, -1f,-1f);

        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (counter == 0) {
                parentNode = new AnchorNode();
                parentNode.setParent(arFragment.getArSceneView().getScene());
                parentNode.setWorldPosition(new Vector3(0, 0, 0));

                node = new TransformableNode(arFragment.getTransformationSystem());
                node.setRenderable(this.model);
                node.getScaleController().setMinScale(0.0f);
                node.getScaleController().setMaxScale(3.0f);
                node.setLocalScale(new Vector3(0.05f, 0.05f, 0.05f));

                Float currentAngle = directionAngles.get(currentDirection);
                node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), currentAngle == null ? 180.0f : currentAngle));

                node.setParent(parentNode);
                node.setWorldPosition(vector);

                oldPositions.add(camera.getLocalPosition());
            }

            if (counter % 10 == 0) {
                arrowCoordinates.setText(node.getWorldPosition().toString());

                if (hasMoved(oldPositions.get(oldPositions.size()-1), camera.getLocalPosition(), node.getWorldPosition())) {
                    if (paths.size() > 0) {
                        currentPosition = paths.get(0);
                        paths.remove(0);
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
            counter++;
        });

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
        if (currentPosition.getX_coord() > paths.get(0).getX_coord()) {
            xDifference = currentPosition.getX_coord() - paths.get(0).getX_coord();
        } else {
            xDifference = paths.get(0).getX_coord() - currentPosition.getX_coord();
        }

        float yDifference = 0.0f;
        if (currentPosition.getY_coord() > paths.get(0).getY_coord()) {
            yDifference = currentPosition.getY_coord() - paths.get(0).getY_coord();
        } else {
            yDifference = paths.get(0).getY_coord() - currentPosition.getY_coord();
        }

        xDifference = conversions.getValue_two() * xDifference;
        yDifference = conversions.getValue_two() * yDifference;
        Log.d(TAG, "xDiff: " + xDifference);
        Log.d(TAG, "yDiff: " + yDifference);

        float[] steps = {xDifference, yDifference};
        if (currentDirection.equals("east") || currentDirection.equals("west"))
            steps = new float[]{yDifference, xDifference};

        return steps;
    }

    private void updateCurrentDirection() {
        // Note: North = forward, South = backward, west = left, east = right

        if (currentPosition.getX_coord() > paths.get(0).getX_coord()) {
            currentDirection = "west";
        } else if (currentPosition.getX_coord() < paths.get(0).getX_coord()) {
            currentDirection = "east";
        } else if (currentPosition.getY_coord() > paths.get(0).getY_coord()) {
            currentDirection = "north";
        } else if (currentPosition.getY_coord() < paths.get(0).getY_coord()) {
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
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
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

        Log.d("MYAPP3: ", this.model == null ? "YES" : "NO");
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null || viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model)
                .animate(true).start();
        model.setWorldPosition(vector);
        model.select();
    }
}