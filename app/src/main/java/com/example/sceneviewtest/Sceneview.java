package com.example.sceneviewtest;

import android.content.Intent;
import android.net.Uri;
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
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Sceneview extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    private RequestQueue queue;
    private ArrayList<Path> paths;
    private Conversion conversions;
    Path currentPosition;

    private TextView positionText;
    private TextView arrowCoordinates;

    private Button sceneviewStarter;

    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    private Vector3 vector;
    private AnchorNode parentNode;
    private TransformableNode node;
    private Camera camera;

    private final List<Vector3> oldPositions = new ArrayList<>();

    private int tigerCount = 0;

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

        conversions = (Conversion) getIntent().getSerializableExtra("CONVERSIONS");

        queue = Volley.newRequestQueue(this);
        vector = new Vector3();

        getSupportFragmentManager().addFragmentOnAttachListener(this);
        positionText = findViewById(R.id.positionDetails);
        arrowCoordinates = findViewById(R.id.arrowPosition);
        sceneviewStarter = findViewById(R.id.start_sceneform);

        startSceneform(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        vector.set(0, -1f,-1f);

        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (tigerCount == 0) {
                parentNode = new AnchorNode();
                parentNode.setParent(arFragment.getArSceneView().getScene());
                parentNode.setWorldPosition(new Vector3(0, 0, 0));

                node = new TransformableNode(arFragment.getTransformationSystem());
                node.setRenderable(this.model);
                node.getScaleController().setMinScale(0.0f);
                node.getScaleController().setMaxScale(3.0f);
                node.setLocalScale(new Vector3(0.02f, 0.02f, 0.02f));
                node.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), 90f));
                node.setParent(parentNode);
                node.setWorldPosition(vector);

                oldPositions.add(camera.getLocalPosition());
            }

            if (tigerCount % 10 == 0) {
                arrowCoordinates.setText(node.getWorldPosition().toString());
//                positionText.setText("Positions: " + camera.getLocalPosition().toString());

                if (hasMoved(oldPositions.get(oldPositions.size()-1), camera.getLocalPosition(), node.getWorldPosition())) {
//                    positionText.setText("Arrived!!!! Hooray!!!");
                    Vector3 oldNodePosition = node.getWorldPosition();
                    removeOldArrow();
                    createNewArrow(oldNodePosition);
                }
            }
            tigerCount++;
        });

        camera = arFragment.getArSceneView().getScene().getCamera();
    }

    private void createNewArrow(Vector3 oldNodePosition) {
        // TODO: Please check nya ni balik ang conversion, basin sayop ni haha
        float xDifference = currentPosition.getxCoord() - paths.get(0).getxCoord();
        float yDifference = currentPosition.getyCoord() - paths.get(0).getyCoord();
        xDifference = conversions.getValueTwo() * xDifference;
        yDifference = conversions.getValueTwo() * yDifference;
        float newXPosition = oldNodePosition.x - xDifference;
        float newZPosition = oldNodePosition.z - yDifference;

//        vector.set(oldNodePosition.x, -1f, oldNodePosition.z - 1);
        vector.set(newXPosition, -1f, newZPosition);

        parentNode = new AnchorNode();
        parentNode.setParent(arFragment.getArSceneView().getScene());

        node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(this.model);
        node.getScaleController().setMinScale(0.0f);
        node.getScaleController().setMaxScale(3.0f);
        node.setLocalScale(new Vector3(0.02f, 0.02f, 0.02f));
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), 90f));
        node.setParent(parentNode);

        node.setWorldPosition(vector);
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
                .setSource(this, R.raw.scene_glb)
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