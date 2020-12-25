package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A vehicle with a single Engine and a single GearBox.
 */
abstract public class Vehicle {
    // *************************************************************************
    // constants and loggers

    final public static float KMH_TO_MPH = 0.62137f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Vehicle.class.getName());
    // *************************************************************************
    // fields

    private AudioNode hornAudio;
    private AutomaticGearboxState gearboxState;
    private boolean parkingBrakeApplied;
    private Engine engine;
    /**
     * control signal for acceleration, ranging from -1 (full-throttle reverse)
     * to +1 = full-throttle forward
     */
    private float accelerateSignal;
    private GearBox gearBox;
    final private Node node;
    private Spatial chassis;
    final private String name;
    private VehicleAudioState vehicleAudioState;
    private VehicleControl vehicleControl;
    // *************************************************************************
    // constructors

    public Vehicle(String name) {
        this.name = name;
        node = new Node("Vehicle: " + name);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the "accelerate" control signal. TODO rename setAccelerateSignal()
     *
     * @param value the desired signal value, between -1 (full-throttle reverse)
     * and +1 (full-throttle forward) inclusive
     */
    public void accelerate(float value) {
        // TODO awkward interface - controls both the gearbox and the throttle
        Validate.inRange(value, "value", -1f, 1f);
        accelerateSignal = value;
    }

    abstract public void applyEngineBraking();

    public void attachToScene(Node parent) {
        warpToStart();
        getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        enable();

        parent.attachChild(node);

        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        physicsSpace.add(vehicleControl);
    }

    /**
     * Apply the vehicle brakes at the given strength.
     *
     * @param strength a unit value between 0.0 - 1.0.
     */
    abstract public void brake(float strength);

    /**
     * Determine the offset of the vehicle's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    abstract public Vector3f dashCamOffset();

    public void detachFromScene() {
        disable();
        vehicleControl.setPhysicsSpace(null);
        node.removeFromParent();
    }

    /**
     * Determine the forward direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in physics-space coordinates (either storeResult or
     * a new instance)
     */
    public Vector3f forwardDirection(Vector3f storeResult) {
        Vector3f result = vehicleControl.getForwardVector(storeResult);
        return result;
    }

    /**
     * Evaluate the "accelerate" control signal. TODO rename
     *
     * @return a value between -1 and +1 inclusive
     */
    public float getAccelerationForce() {
        return accelerateSignal;
    }

    public Spatial getChassis() {
        return chassis;
    }

    public Engine getEngine() {
        return engine;
    }

    public GearBox getGearBox() {
        return gearBox;
    }

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    public Quaternion getRotation() {
        return node.getLocalRotation();
    }

    /**
     * Determine the forward component of this vehicle's airspeed as of the
     * previous time step.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (may be negative)
     */
    public float getSpeed(SpeedUnit speedUnit) {
        float kph = vehicleControl.getCurrentVehicleSpeedKmHour();
        switch (speedUnit) {
            case KMH:
                return kph;
            case MPH:
                return kph * KMH_TO_MPH;
            default:
                throw new IllegalArgumentException("speedUnit = " + speedUnit);
        }
    }

    public VehicleControl getVehicleControl() {
        return vehicleControl;
    }

    abstract public void handbrake(float strength);

    /**
     * Load this Vehicle from assets.
     */
    abstract public void load();

    abstract public void removeEngineBraking();

    /**
     * Update the status of the horn.
     *
     * @param isRequested true &rarr; requested, false &rarr; not requested
     */
    public void setHornStatus(boolean isRequested) {
        DriverHud hud = Main.findAppState(DriverHud.class);
        hud.showHornButton(isRequested);

        AudioSource.Status status = hornAudio.getStatus();
        boolean isSounding = (status == AudioSource.Status.Playing);
        if (VehicleAudioState.isMuted()) {
            isRequested = false;
        }
        if (isSounding && !isRequested) {
            hornAudio.stop();
        } else if (isRequested && !isSounding) {
            hornAudio.play();
        }
    }

    public void setParkingBrakeApplied(boolean applied) {
        parkingBrakeApplied = applied;
    }

    public void startEngine() {
        if (!engine.isRunning()) {
            engine.setRunning(true);
            vehicleAudioState.playEngineSound();
        }
    }

    abstract public void steer(float strength);

    public void stopEngine() {
        if (engine.isRunning()) {
            engine.setRunning(false);
            vehicleAudioState.stopEngineSound();
        }
    }

    /**
     * Determine the location of the ChaseCamera target.
     *
     * @param bias how much to displace the target toward the rear (0=center of
     * mass, 1=back bumper)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f targetLocation(float bias, Vector3f storeResult) {
        Vector3f offset = targetOffset();
        offset.z *= bias;
        Matrix3f orientation = vehicleControl.getPhysicsRotationMatrix(null);
        orientation.mult(offset, offset);

        Vector3f result = vehicleControl.getPhysicsLocation(storeResult);
        result.addLocal(offset);

        return result;
    }

    /**
     * Warp this vehicle to a suitable start position.
     */
    public void warpToStart() {
        /*
         * Cast a physics ray downward from the drop location.
         */
        Vector3f dropLocation = Main.getEnvironment().dropLocation();
        Vector3f endLocation = dropLocation.add(0f, -999f, 0f);
        BulletAppState bas = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        List<PhysicsRayTestResult> rayTest
                = physicsSpace.rayTestRaw(dropLocation, endLocation);
        /*
         * Find the closest contact with another collision object,
         * typically the pavement.
         */
        float closestFraction = 9f;
        for (PhysicsRayTestResult hit : rayTest) {
            if (hit.getCollisionObject() != vehicleControl) {
                float hitFraction = hit.getHitFraction();
                if (hitFraction < closestFraction) {
                    closestFraction = hitFraction;
                }
            }
        }
        Vector3f contactLocation = MyVector3f.lerp(closestFraction,
                dropLocation, endLocation, null);
        /*
         * Estimate the minimum chassis Y offset to keep
         * the undercarriage off the pavement.
         */
        CollisionShape shape = vehicleControl.getCollisionShape();
        BoundingBox aabb
                = shape.boundingBox(Vector3f.ZERO, Matrix3f.IDENTITY, null);
        float minYOffset = aabb.getYExtent() - aabb.getCenter().y;
        /*
         * Estimate chassis Y offset based on an unloaded axle.
         * TODO adjust for the vehicle's weight
         */
        VehicleWheel w0 = vehicleControl.getWheel(0);
        float suspensionLength = w0.getRestLength();
        float yOffset
                = w0.getRadius() + suspensionLength - w0.getLocation(null).y;
        /*
         * Calculate and apply an appropriate start location.
         */
        if (yOffset < minYOffset) {
            yOffset = minYOffset;
        }
        Vector3f startLocation = contactLocation.add(0f, yOffset, 0f);
        vehicleControl.setPhysicsLocation(startLocation);
        float yRotation = Main.getEnvironment().dropYRotation();
        Quaternion orient = new Quaternion().fromAngles(0f, yRotation, 0f);
        vehicleControl.setPhysicsRotation(orient);

        vehicleControl.setAngularVelocity(Vector3f.ZERO);
        vehicleControl.setLinearVelocity(Vector3f.ZERO);
    }
    // *************************************************************************
    // new protected methods

    /**
     * Should be invoked last, after all parts have been configured and added.
     */
    protected void build() {
        gearboxState = new AutomaticGearboxState(this);
        vehicleAudioState = new VehicleAudioState(this);
    }

    protected void disable() {
        AppStateManager stateManager = Main.getApplication().getStateManager();
        stateManager.detach(gearboxState);
        stateManager.detach(vehicleAudioState);
    }

    protected void enable() {
        AppStateManager stateManager = Main.getApplication().getStateManager();
        stateManager.attach(gearboxState);
        stateManager.attach(vehicleAudioState);
    }

    protected void setChassis(String folderName, Spatial chassis, float mass) {
        this.chassis = chassis;

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath
                = "/Models/" + folderName + "/shapes/chassis-shape.j3o";
        CollisionShape shape;
        try {
            shape = (CollisionShape) assetManager.loadAsset(assetPath);
            Vector3f scale = chassis.getWorldScale();
            shape.setScale(scale);
        } catch (AssetNotFoundException exception) {
            shape = CollisionShapeFactory.createDynamicMeshShape(chassis);
        }
        vehicleControl = new VehicleControl(shape, mass);
        /*
         * Configure continuous collision detection (CCD) for the chassis.
         */
        float radius = shape.maxRadius();
        vehicleControl.setCcdMotionThreshold(radius);
        vehicleControl.setCcdSweptSphereRadius(radius);

        node.addControl(vehicleControl);
        node.attachChild(chassis);
    }

    protected void setEngine(Engine engine) {
        this.engine = engine;
        node.attachChild(this.engine.getEngineAudio());
    }

    protected void setGearBox(GearBox gearBox) {
        this.gearBox = gearBox;
    }

    /**
     * Create and attach an audio node for the vehicle's horn.
     *
     * @param assetPath the path to the OGG asset (not null, not empty)
     */
    protected void setHornAudio(String assetPath) {
        AssetManager assetManager = Main.getApplication().getAssetManager();
        hornAudio = new AudioNode(assetManager, assetPath,
                AudioData.DataType.Stream);
        hornAudio.setLooping(true);
        hornAudio.setPositional(true);
        hornAudio.setDirectional(false);
        node.attachChild(hornAudio);
    }

    /**
     * Determine the offset of the vehicle's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    abstract protected Vector3f targetOffset();
}
