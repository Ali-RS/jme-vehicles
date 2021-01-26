package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.shadow.ShadowUtil;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.debug.PointVisualizer;

/**
 * AppState to visualize key points on the selected vehicle, for debugging. Each
 * new instance is disabled by default.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class VehiclePointsState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(VehiclePointsState.class.getName());
    // *************************************************************************
    // fields

    final private Camera tmpCamera;
    /**
     * visualize the dash-camera frustum
     */
    final private Geometry dcFrustum;
    /**
     * visualize the chase-camera target
     */
    final private PointVisualizer cameraTarget;
    /**
     * visualize the vehicle's center of mass
     */
    final private PointVisualizer centerOfMass;
    /**
     * visualize the dash-camera location
     */
    final private PointVisualizer dashCamera;
    /**
     * reusable Quaternion
     */
    final private static Quaternion tmpOrientation = new Quaternion();
    /**
     * reusable vector
     */
    final private static Vector3f tmpLocation = new Vector3f();
    /**
     * vertex locations in the dash-camera frustum
     */
    final private Vector3f[] dcFrustumVertices = new Vector3f[8];
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled set of points.
     */
    public VehiclePointsState() {
        super("Vehicle Points");

        AssetManager assetManager = Main.getApplication().getAssetManager();
        int indicatorSize = 15; // in pixels

        cameraTarget = new PointVisualizer(assetManager, indicatorSize,
                ColorRGBA.Yellow, "ring");
        centerOfMass = new PointVisualizer(assetManager, indicatorSize,
                ColorRGBA.White, "saltire");
        dashCamera = new PointVisualizer(assetManager, indicatorSize,
                ColorRGBA.Red, "square");

        for (int vertexIndex = 0; vertexIndex < 8; vertexIndex++) {
            dcFrustumVertices[vertexIndex] = new Vector3f();
        }

        WireFrustum dcFrustumMesh = new WireFrustum(dcFrustumVertices);
        dcFrustum = new Geometry("dash camera frustum", dcFrustumMesh);
        Material dcfMaterial
                = MyAsset.createUnshadedMaterial(assetManager, ColorRGBA.Red);
        dcFrustum.setMaterial(dcfMaterial);
        dcFrustum.setShadowMode(RenderQueue.ShadowMode.Off);

        tmpCamera = Main.getApplication().getCamera().clone();

        super.setEnabled(false);
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        cameraTarget.removeFromParent();
        centerOfMass.removeFromParent();
        dashCamera.removeFromParent();
        dcFrustum.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        Node rootNode = Main.getApplication().getRootNode();
        rootNode.attachChild(cameraTarget);
        rootNode.attachChild(centerOfMass);
        rootNode.attachChild(dashCamera);
        rootNode.attachChild(dcFrustum);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        Vehicle vehicle = Main.getVehicle();
        vehicle.locateTarget(1f, tmpLocation);
        cameraTarget.setLocalTranslation(tmpLocation);

        RigidBodyMotionState motionState
                = vehicle.getVehicleControl().getMotionState();
        motionState.getLocation(tmpLocation);
        centerOfMass.setLocalTranslation(tmpLocation);

        Vector3f offset = new Vector3f(); // TODO garbage
        vehicle.locateDashCam(offset);
        motionState.getOrientation(tmpOrientation);
        tmpOrientation.mult(offset, offset);
        tmpLocation.addLocal(offset);
        dashCamera.setLocalTranslation(tmpLocation);

        Camera defaultCamera = Main.getApplication().getCamera();
        tmpCamera.copyFrom(defaultCamera);
        tmpCamera.setLocation(tmpLocation);
        tmpCamera.setRotation(tmpOrientation);
        ShadowUtil.updateFrustumPoints2(tmpCamera, dcFrustumVertices);

        WireFrustum frustumMesh = (WireFrustum) dcFrustum.getMesh();
        frustumMesh.update(dcFrustumVertices);
        dcFrustum.setMesh(frustumMesh);
    }
}
