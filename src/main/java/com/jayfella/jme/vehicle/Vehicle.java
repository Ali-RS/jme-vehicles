package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * A vehicle with a single Engine and a single GearBox.
 */
public abstract class Vehicle {

    public enum SpeedUnit {
        KMH, MPH
    }

    public static final float KMH_TO_MPH = 0.62137f;
    public static final float MPH_TO_KMH = 1.60934f;

    private final Application app;

    private AudioNode hornAudio;
    private final Node node;
    private VehicleControl vehicleControl;

    private String name;

    private Spatial chassis;
    private Engine engine;
    private GearBox gearBox;

    private AutomaticGearboxState gearboxState;
    private VehicleAudioState vehicleAudioState;

    final private Vector3f dashCamLocation = new Vector3f();

    private boolean parkingBrakeApplied;

    public Vehicle(Application app, String name) {
        this.app = app;
        this.name = name;
        node = new Node("Vehicle: " + name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        node.setName("Vehicle: " + name);
    }

    /**
     * Create and attach an audio node for the vehicle's horn.
     *
     * @param assetPath the path to the OGG asset (not null, not empty)
     */
    public void setHornAudio(String assetPath) {
        AssetManager assetManager = app.getAssetManager();
        hornAudio = new AudioNode(assetManager, assetPath,
                AudioData.DataType.Stream);
        hornAudio.setLooping(true);
        hornAudio.setPositional(true);
        hornAudio.setDirectional(false);
        node.attachChild(hornAudio);
    }

    /**
     * Update the status of the horn.
     *
     * @param isRequested true &rarr; requested, false &rarr; not requested
     */
    public void setHornStatus(boolean isRequested) {
        AudioSource.Status status = hornAudio.getStatus();
        boolean isSounding = (status == AudioSource.Status.Playing);

        DriverHud hud = Main.findAppState(DriverHud.class);
        if (isSounding && !isRequested) {
            hornAudio.stop();
            hud.showHornButton(false);
        } else if (isRequested && !isSounding) {
            hornAudio.play();
            hud.showHornButton(true);
        }
    }

    public Spatial getChassis() {
        return chassis;
    }

    public void setChassis(Spatial chassis, float mass) {
        this.chassis = chassis;
        CollisionShape chassisCollisionShape
                = CollisionShapeFactory.createDynamicMeshShape(chassis);
        vehicleControl = new VehicleControl(chassisCollisionShape, mass);
        node.addControl(vehicleControl);
        node.attachChild(chassis);
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
        node.attachChild(this.engine.getEngineAudio());
    }

    public void startEngine() {
        if (!engine.isStarted()) {
            engine.setStarted(true);
            vehicleAudioState.playEngineSound();
        }
    }

    public void stopEngine() {
        if (engine.isStarted()) {
            engine.setStarted(false);
            vehicleAudioState.stopEngineSound();
        }
    }

    public GearBox getGearBox() {
        return gearBox;
    }

    public void setGearBox(GearBox gearBox) {
        this.gearBox = gearBox;
    }

    private float accelerationForce;

    /**
     * Accelerate the vehicle with the given power.
     *
     * @param strength a unit value between 0.0 - 1.0. Essentially how "hard"
     * you want to accelerate.
     */
    public void accelerate(float strength) {
        accelerationForce = strength;
    }

    public float getAccelerationForce() {
        return accelerationForce;
    }

    /**
     * Apply the vehicle brakes at the given strength.
     *
     * @param strength a unit value between 0.0 - 1.0.
     */
    public abstract void brake(float strength);

    public abstract void handbrake(float strength);

    public abstract void steer(float strength);

    public Vector3f getLocation() {
        return node.getLocalTranslation();
    }

    public void setLocation(Vector3f loc) {
        vehicleControl.setPhysicsLocation(loc);
    }

    public Quaternion getRotation() {
        return node.getLocalRotation();
    }

    public void setRotation(Quaternion rotation) {
        vehicleControl.setPhysicsRotation(rotation);
    }

    public Node getNode() {
        return node;
    }

    public VehicleControl getVehicleControl() {
        return vehicleControl;
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

    /**
     * Determine the location of the dash camera.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in local coordinates (either storeResult or a
     * new instance)
     */
    public Vector3f getDashCamLocation(Vector3f storeResult) {
        if (storeResult == null) {
            return dashCamLocation.clone();
        } else {
            return storeResult.set(dashCamLocation);
        }
    }

    protected void setDashCamLocation(Vector3f loc) {
        dashCamLocation.set(loc);
    }

    public boolean isParkingBrakeApplied() {
        return parkingBrakeApplied;
    }

    public void setParkingBrakeApplied(boolean applied) {
        parkingBrakeApplied = applied;
    }

    public void attachToScene(Node parent, PhysicsSpace physicsSpace) {
        enable();

        parent.attachChild(node);
        physicsSpace.add(vehicleControl);
    }

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

    /**
     * Determine the location of the ChaseCamera target.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f targetLocation(Vector3f storeResult) {
        Vector3f offset = new Vector3f(0f, 1f, -3f); // TODO tune
        Matrix3f orientation = vehicleControl.getPhysicsRotationMatrix(null);
        orientation.mult(offset, offset);
        Vector3f result = vehicleControl.getPhysicsLocation(storeResult);
        result.addLocal(offset);

        return result;
    }

    /**
     * Should be called last when all vehicle parts have been built and added.
     */
    protected void build() {
        gearboxState = new AutomaticGearboxState(this);
        vehicleAudioState = new VehicleAudioState(this);

        app.getStateManager().attach(gearboxState);
        app.getStateManager().attach(vehicleAudioState);
    }

    protected void enable() {
        app.getStateManager().attach(gearboxState);
        app.getStateManager().attach(vehicleAudioState);
    }

    protected void disable() {
        app.getStateManager().detach(gearboxState);
        app.getStateManager().detach(vehicleAudioState);
    }

    public abstract void applyEngineBraking();

    public abstract void removeEngineBraking();
}
