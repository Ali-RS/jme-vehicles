package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
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
import jme3utilities.math.MyVector3f;

/**
 * A vehicle with a single Engine and a single GearBox.
 */
public abstract class Vehicle {

    public enum SpeedUnit {
        KMH, MPH
    }
    // *************************************************************************
    // constants and loggers

    public static final float KMH_TO_MPH = 0.62137f;
    // *************************************************************************
    // fields

    final private Application app;
    private AudioNode hornAudio;
    private AutomaticGearboxState gearboxState;
    private boolean parkingBrakeApplied;
    private Engine engine;
    private float accelerationForce;
    private GearBox gearBox;
    final private Node node;
    private Spatial chassis;
    private String name;
    private VehicleAudioState vehicleAudioState;
    private VehicleControl vehicleControl;
    // *************************************************************************
    // constructors

    public Vehicle(Application app, String name) {
        this.app = app;
        this.name = name;
        node = new Node("Vehicle: " + name);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Accelerate the vehicle with the given power.
     *
     * @param strength a unit value between 0.0 - 1.0. Essentially how "hard"
     * you want to accelerate.
     */
    public void accelerate(float strength) {
        accelerationForce = strength;
    }

    public abstract void applyEngineBraking();

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
    public abstract void brake(float strength);

    /**
     * Determine the offset of the vehicle's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    abstract public Vector3f dashCamOffset();

    public void detachFromScene() {
        disable();

        node.removeFromParent();
        vehicleControl.getPhysicsSpace().remove(vehicleControl);
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

    public float getAccelerationForce() {
        return accelerationForce;
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

    public abstract void handbrake(float strength);

    /**
     * Load this Vehicle from assets.
     */
    abstract public void load();

    public abstract void removeEngineBraking();

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
        if (!engine.isStarted()) {
            engine.setStarted(true);
            vehicleAudioState.playEngineSound();
        }
    }

    public abstract void steer(float strength);

    public void stopEngine() {
        if (engine.isStarted()) {
            engine.setStarted(false);
            vehicleAudioState.stopEngineSound();
        }
    }

    /**
     * Determine the location of the ChaseCamera target.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f targetLocation(Vector3f storeResult) {
        Vector3f offset = targetOffset();
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
        app.getStateManager().detach(gearboxState);
        app.getStateManager().detach(vehicleAudioState);
    }

    protected void enable() {
        app.getStateManager().attach(gearboxState);
        app.getStateManager().attach(vehicleAudioState);
    }

    protected void setChassis(Spatial chassis, float mass) {
        this.chassis = chassis;
        CollisionShape chassisCollisionShape
                = CollisionShapeFactory.createDynamicMeshShape(chassis);
        vehicleControl = new VehicleControl(chassisCollisionShape, mass);
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
        AssetManager assetManager = app.getAssetManager();
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
