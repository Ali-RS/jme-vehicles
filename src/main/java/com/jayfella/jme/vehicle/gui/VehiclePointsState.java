package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import java.util.logging.Logger;
import jme3utilities.debug.PointVisualizer;

/**
 * AppState to visualize key points on the selected vehicle, for debugging.
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

    /**
     * visualize the camera target
     */
    final private PointVisualizer cameraTarget;
    /**
     * visualize the vehicle's center of mass
     */
    final private PointVisualizer centerOfMass;
    /**
     * visualize the dash camera
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
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled set of points.
     */
    public VehiclePointsState() {
        super("Vehicle Points");

        AssetManager assetManager = Main.getApplication().getAssetManager();
        int indicatorSize = 15; // in pixels

        cameraTarget = new PointVisualizer(assetManager, indicatorSize,
                ColorRGBA.Yellow, "ring");
        cameraTarget.setQueueBucket(RenderQueue.Bucket.Translucent);

        centerOfMass = new PointVisualizer(assetManager, indicatorSize,
                ColorRGBA.Black, "saltire");
        centerOfMass.setQueueBucket(RenderQueue.Bucket.Translucent);

        dashCamera = new PointVisualizer(assetManager, indicatorSize,
                ColorRGBA.Red, "square");
        dashCamera.setQueueBucket(RenderQueue.Bucket.Translucent);

        setEnabled(true);
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
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
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        Vehicle vehicle = Main.getVehicle();
        vehicle.targetLocation(1f, tmpLocation);
        cameraTarget.setLocalTranslation(tmpLocation);

        vehicle.getVehicleControl().getPhysicsLocation(tmpLocation);
        centerOfMass.setLocalTranslation(tmpLocation);

        Vector3f offset = vehicle.dashCamOffset();
        vehicle.getVehicleControl().getPhysicsRotation(tmpOrientation);
        tmpOrientation.mult(offset, offset);
        tmpLocation.addLocal(offset);
        dashCamera.setLocalTranslation(tmpLocation);
    }
}
