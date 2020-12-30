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
abstract public class Vehicle implements Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * factor to convert km/hr to miles per hour
     */
    final public static float KPH_TO_MPH = 0.62137f;
    /**
     * factor to convert km/hr to wu/sec
     */
    final public static float KPH_TO_WUPS = 0.277778f;
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
    /**
     * engine model
     */
    private Engine engine;
    /**
     * control signal for acceleration, ranging from -1 (full-throttle reverse)
     * to +1 (full-throttle forward)
     */
    private float accelerateSignal;
    private GearBox gearBox;
    final private Node node;
    /**
     * sound produced when the Engine is running, or null for silence
     */
    private Sound engineSound;
    /**
     * computer-graphics (C-G) model to visualize the whole Vehicle except for
     * its wheels
     */
    private Spatial chassis;
    /**
     * descriptive name (not null)
     */
    final private String name;
    private VehicleAudioState vehicleAudioState;
    private VehicleControl vehicleControl;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Vehicle with the specified name.
     *
     * @param name the desired name (not null)
     */
    public Vehicle(String name) {
        this.name = name;
        node = new Node("Vehicle: " + name);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the "accelerate" control signal.
     *
     * @param strength the desired strength of the "accelerate" control signal:
     * between -1 (full-throttle reverse) and +1 (full-throttle forward)
     * inclusive
     */
    public void setAccelerateSignal(float strength) {
        // TODO awkward interface - controls both the gearbox and the throttle
        Validate.inRange(strength, "strength", -1f, 1f);
        accelerateSignal = strength;
    }

    /**
     * Add this Vehicle to the specified scene-graph node.
     *
     * @param parent where to attach (not null)
     */
    public void attachToScene(Node parent) {
        if (vehicleControl == null) {
            load();
        }
        parent.attachChild(node);

        warpToStart();
        getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        enable();

        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        physicsSpace.add(vehicleControl);
    }

    /**
     * Access the engine sound.
     *
     * @return the pre-existing Sound, or null for silence
     */
    public Sound getEngineSound() {
        return engineSound;
    }

    /**
     * Alter the values of the brake control signals.
     *
     * @param mainStrength the desired strength of the main-brake control
     * signal, between 0 (not applied) and 1 (applied as strongly as possible)
     * @param parkingStrength the desired strength of the parking-brake control
     * signal, between 0 (not applied) and 1 (applied as strongly as possible)
     */
    abstract public void setBrakeSignals(float mainStrength,
            float parkingStrength);

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
     * Evaluate the "accelerate" control signal.
     *
     * @return a value between -1 and +1 inclusive
     */
    public float accelerateSignal() {
        return accelerateSignal;
    }

    /**
     * Access the computer-graphics (C-G) model for visualization.
     *
     * @return the pre-existing instance
     */
    public Spatial getChassis() {
        return chassis;
    }

    /**
     * Access the Engine.
     *
     * @return the pre-existing instance
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Access the GearBox.
     *
     * @return the pre-existing instance
     */
    public GearBox getGearBox() {
        return gearBox;
    }

    /**
     * Determine this vehicle's name.
     *
     * @return the descriptive name (not null)
     */
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

        float result;
        switch (speedUnit) {
            case KPH:
                result = kph;
                break;
            case MPH:
                result = kph * Vehicle.KPH_TO_MPH;
                break;
            case WUPS:
                result = kph * Vehicle.KPH_TO_WUPS;
                break;
            default:
                throw new RuntimeException("speedUnit = " + speedUnit);
        }

        return result;
    }

    /**
     * Access the PhysicsControl.
     *
     * @return the pre-existing instance
     */
    public VehicleControl getVehicleControl() {
        return vehicleControl;
    }

    /**
     * Alter the engine sound.
     *
     * @param sound the desired sound, or null for silence
     */
    public void setEngineSound(Sound sound) {
        this.engineSound = sound;
    }

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

    /**
     * Alter whether the parking brake is applied.
     *
     * @param applied the desired state (true&rarr;applied, false&rarr; not
     * applied)
     */
    public void setParkingBrakeApplied(boolean applied) {
        parkingBrakeApplied = applied;
    }

    public void startEngine() {
        if (!engine.isRunning()) {
            engine.setRunning(true);
        }
    }

    abstract public void steer(float strength);

    public void stopEngine() {
        if (engine.isRunning()) {
            engine.setRunning(false);
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
        Vector3f dropLocation = Main.getWorld().dropLocation();
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
        float yRotation = Main.getWorld().dropYRotation();
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

    protected void setChassis(String folderName, Spatial chassis, float mass,
            float linearDamping) {
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
         * Configure damping for the chassis,
         * to simulate drag due to air resistance.
         */
        vehicleControl.setLinearDamping(linearDamping);
        /*
         * Configure continuous collision detection (CCD) for the chassis.
         */
        float radius = shape.maxRadius();
        vehicleControl.setCcdMotionThreshold(radius);
        vehicleControl.setCcdSweptSphereRadius(radius);

        node.addControl(vehicleControl);
        node.attachChild(chassis);
    }

    protected void setEngine(Engine desiredEngine) {
        engine = desiredEngine;
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
        hornAudio.setPositional(false);
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
