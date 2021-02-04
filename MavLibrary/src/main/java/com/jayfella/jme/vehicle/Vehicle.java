package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A vehicle based on Bullet's btRaycastVehicle, with a single Engine and a
 * single GearBox.
 *
 * Derived from the Car and Vehicle classes in the Advanced Vehicles project.
 */
abstract public class Vehicle
        implements Loadable, PhysicsTickListener, VehicleSpeed {
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

    private AutomaticGearboxState gearboxState;
    /**
     * for testing TireSmokeEmitter
     */
    private boolean isBurningRubber = false;
    /**
     * true when driver is sounding the horn, otherwise false
     */
    private boolean isHornRequested = false;
    /**
     * source of motive power
     */
    private Engine engine;
    /**
     * control signal for acceleration, ranging from -1 (full-throttle reverse)
     * to +1 (full-throttle forward)
     */
    private float accelerateSignal;
    /**
     * damping to due air resistance on the chassis (&ge;0, &lt;1)
     */
    private float chassisDamping;
    private GearBox gearBox;
    final private List<Wheel> wheels = new ArrayList<>(4);
    /**
     * temporary storage for the vehicle's orientation
     */
    final private static Matrix3f tmpOrientation = new Matrix3f();
    final private Node node;
    private SkidMarksState skidmarks;
    /**
     * sound produced when the horn is sounding, or null for silence
     */
    private Sound hornSound;
    /**
     * computer-graphics (C-G) model to visualize the whole Vehicle except for
     * its wheels
     */
    private Spatial chassis;
    /**
     * units to use in the speedometer, or null for no speedometer
     */
    private SpeedUnit speedometerUnits = SpeedUnit.MPH;
    /**
     * descriptive name (not null)
     */
    final private String name;

    private TireSmokeEmitter smokeEmitter;
    private VehicleAudioState vehicleAudioState;
    private VehicleControl vehicleControl;
    /**
     * VehicleWorld that contains this Vehicle, or null if none
     */
    private VehicleWorld world;

    private WheelSpinState wheelSpinState;
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
     * Evaluate the "accelerate" control signal.
     *
     * @return a value between -1 and +1 inclusive
     */
    public float accelerateSignal() {
        return accelerateSignal;
    }

    /**
     * Add this Vehicle to the specified world.
     *
     * @param world where to attach (not null, alias created)
     * @param globalAudio the global audio controls (not null, alias created)
     */
    public void addToWorld(VehicleWorld world, GlobalAudio globalAudio) {
        this.world = world;

        if (vehicleControl == null) {
            AssetManager assetManager = world.getAssetManager();
            load(assetManager);
        }

        Node parentNode = world.getParentNode();
        parentNode.attachChild(node);

        warpToStart();
        getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        vehicleAudioState.setGlobalAudio(globalAudio);
        enable();

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        vehicleControl.setPhysicsSpace(physicsSpace);
        physicsSpace.addTickListener(this);
    }

    /**
     * Add a single Wheel to this Vehicle.
     *
     * @param wheelModel the desired WheelModel (not null)
     * @param connectionLocation the location where the suspension connects to
     * the chassis (in chassis coordinates, not null, unaffected)
     * @param isSteering true if used for steering, otherwise false
     * @param isSteeringFlipped true for rear-wheel steering, otherwise false
     * @param mainBrakePeakForce (in Newtons, &ge;0)
     * @param parkingBrakePeakForce (in Newtons, &ge;0)
     * @param extraDamping (&ge;0, &lt;1)
     * @return the new Wheel
     */
    public Wheel addWheel(WheelModel wheelModel, Vector3f connectionLocation,
            boolean isSteering, boolean isSteeringFlipped,
            float mainBrakePeakForce, float parkingBrakePeakForce,
            float extraDamping) {
        Node wheelNode = wheelModel.getWheelNode();
        Vector3f suspensionDirection = new Vector3f(0f, -1f, 0f);
        Vector3f axleDirection = new Vector3f(-1f, 0f, 0f);
        float restLength = 0.2f;
        float radius = wheelModel.radius();
        VehicleWheel vehicleWheel = vehicleControl.addWheel(wheelNode,
                connectionLocation, suspensionDirection, axleDirection,
                restLength, radius, isSteering);

        int wheelIndex = wheels.size();
        Suspension suspension = new Suspension(vehicleWheel);
        Brake mainBrake = new Brake(mainBrakePeakForce);
        Brake parkingBrake = new Brake(parkingBrakePeakForce);
        Wheel result = new Wheel(this, wheelIndex, isSteering,
                isSteeringFlipped, suspension, mainBrake, parkingBrake,
                extraDamping);
        wheels.add(result);

        getNode().attachChild(wheelNode);

        return result;
    }

    /**
     * Determine the linear damping due to air resistance.
     *
     * @return a fraction (&ge;0, &lt;1)
     */
    public float chassisDamping() {
        assert chassisDamping >= 0f && chassisDamping < 1f : chassisDamping;
        return chassisDamping;
    }

    /**
     * Count how many wheels this Vehicle has.
     *
     * @return the count (&ge;0)
     */
    public int countWheels() {
        return wheels.size();
    }

    /**
     * Determine the circumference of the first drive wheel. (It's assumed
     * they're all the same size.) Used to convert between axle angular speed
     * and tread speed.
     *
     * @return the circumference (in world units, &gt;0)
     */
    public float driveWheelCircumference() {
        for (Wheel wheel : wheels) {
            if (wheel.isPowered()) {
                float diameter = wheel.getDiameter();
                float circumference = FastMath.PI * diameter;

                assert circumference > 0f : circumference;
                return circumference;
            }
        }

        throw new IllegalStateException("No drive wheel found!");
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
     * Access the horn sound.
     *
     * @return the pre-existing instance, or null for silence
     */
    public Sound getHornSound() {
        return hornSound;
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
     * Determine which units to use in the speedometer.
     *
     * @return an enum value, or null for no speedometer
     */
    public SpeedUnit getSpeedometerUnits() {
        return speedometerUnits;
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
     * Access the indexed Wheel.
     *
     * @param index which Wheel to access (&ge;0)
     * @return the pre-existing instance
     */
    public Wheel getWheel(int index) {
        return wheels.get(index);
    }

    /**
     * Access the world that contains this Vehicle.
     *
     * @return the pre-existing instance, or null if none
     */
    public VehicleWorld getWorld() {
        return world;
    }

    /**
     * Test whether the tires are forced to emit smoke.
     *
     * @return true if forced, otherwise false
     */
    public boolean isBurningRubber() {
        return isBurningRubber;
    }

    /**
     * Test whether the driver is sounding the horn.
     *
     * @return true if sounding, otherwise false
     */
    public boolean isHornRequested() {
        return isHornRequested;
    }

    /**
     * Enumerate all wheels.
     *
     * @return a new array (not null)
     */
    public Wheel[] listWheels() {
        int numWheels = countWheels();
        Wheel[] result = new Wheel[numWheels];
        wheels.toArray(result);

        return result;
    }

    /**
     * Determine the offset of the vehicle's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    abstract public void locateDashCam(Vector3f storeResult);

    /**
     * Determine the location of the ChaseCamera target.
     *
     * @param bias how much to displace the target toward the rear (0=center of
     * mass, 1=back bumper)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f locateTarget(float bias, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        RigidBodyMotionState motion = vehicleControl.getMotionState();

        motion.getOrientation(tmpOrientation);
        Vector3f offset = new Vector3f(); // TODO garbage
        locateTarget(offset);
        offset.z *= bias;
        tmpOrientation.mult(offset, offset);

        motion.getLocation(result);
        result.addLocal(offset);

        return result;
    }

    /**
     * Remove this Vehicle from the world to which it was added.
     */
    public void removeFromWorld() {
        disable();
        PhysicsSpace physicsSpace = vehicleControl.getPhysicsSpace();
        physicsSpace.removeTickListener(this);
        vehicleControl.setPhysicsSpace(null);
        node.removeFromParent();
        this.world = null;
    }

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
        this.accelerateSignal = strength;
        /*
         * Determine unsigned speed in world units per second.
         */
        float speed = forwardSpeed(SpeedUnit.WUPS);
        speed = FastMath.abs(speed);
        if (speed < 0.1f) {
            speed = 0.1f; // avoid division by zero
        }
        /*
         * Distribute the total engine power across the wheels in accordance
         * with their configured power fractions.
         */
        float maxWatts = getEngine().outputWatts();
        //System.out.println("speed = " + speed + " rpm = " + getEngine().getRpm() + " maxWatts = " + maxWatts);
        float totalWatts = strength * maxWatts; // signed so that <0 means reverse
        for (Wheel wheel : wheels) {
            float powerFraction = wheel.getPowerFraction();
            float wheelPower = powerFraction * totalWatts;
            float wheelForce = wheelPower / speed;
            wheel.updateAccelerate(wheelForce);
        }
    }

    /**
     * Alter the values of the brake control signals.
     *
     * @param mainStrength the desired strength of the main-brake control
     * signal, between 0 (not applied) and 1 (applied as strongly as possible)
     * @param parkingStrength the desired strength of the parking-brake control
     * signal, between 0 (not applied) and 1 (applied as strongly as possible)
     */
    public void setBrakeSignals(float mainStrength,
            float parkingStrength) {
        for (Wheel wheel : wheels) {
            wheel.updateBrakes(mainStrength, parkingStrength);
        }
    }

    /**
     * Alter whether the tires are forced to emit smoke.
     *
     * @param setting true&rarr;forced, false&rarr;not forced
     */
    public void setBurningRubber(boolean setting) {
        this.isBurningRubber = setting;
    }

    /**
     * Update the status of the horn.
     *
     * @param isRequested true &rarr; requested, false &rarr; not requested
     */
    public void setHornStatus(boolean isRequested) {
        this.isHornRequested = isRequested;
    }

    /**
     * Alter which units to use in the speedometer.
     *
     * @param units an enum value, or null for no speedometer
     */
    public void setSpeedometerUnits(SpeedUnit units) {
        this.speedometerUnits = units;
    }

    public void setTireSkidMarksEnabled(boolean enabled) {
        skidmarks.setSkidmarkEnabled(enabled);
    }

    public void setTireSkidMarksVisible(boolean enabled) {
        skidmarks.setEnabled(enabled);
    }

    public void setTireSmokeEnabled(boolean enabled) {
        smokeEmitter.setEnabled(enabled);
    }

    /**
     * Replace the WheelModel of the indexed wheel with a new instance of the
     * specified class.
     *
     * @param wheelIndex which wheel to replace (&ge;0, &lt;numWheels-1)
     * @param modelClass the desired type of wheel (not null)
     */
    public void setWheelModel(int wheelIndex,
            Class<? extends WheelModel> modelClass) {
        Wheel wheel = wheels.get(wheelIndex);
        VehicleWheel vehicleWheel = wheel.getVehicleWheel();

        Node oldNode = (Node) vehicleWheel.getWheelSpatial();
        oldNode.removeFromParent();

        Constructor<? extends WheelModel>[] constructors
                = (Constructor<? extends WheelModel>[]) modelClass
                        .getConstructors();
        assert constructors.length == 1 : constructors.length;
        Constructor<? extends WheelModel> constructor = constructors[0];
        // assuming a single constructor that takes a single float argument

        float radius = vehicleWheel.getRadius();
        float diameter = 2 * radius;
        WheelModel wheelModel;
        try {
            wheelModel = constructor.newInstance(diameter);
        } catch (IllegalAccessException
                | InstantiationException
                | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
        AssetManager assetManager = world.getAssetManager();
        wheelModel.load(assetManager);
        /*
         * Copy the local rotation of the old Spatial.
         */
        int numChildren = oldNode.getChildren().size();
        assert numChildren == 1 : numChildren;
        Spatial oldSpatial = oldNode.getChild(0);
        Spatial newSpatial = wheelModel.getSpatial();
        Quaternion localRotation = oldSpatial.getLocalRotation();
        newSpatial.setLocalRotation(localRotation);

        Node newNode = wheelModel.getWheelNode();
        vehicleWheel.setWheelSpatial(newNode);
        getNode().attachChild(newNode);

        AppStateManager stateManager = world.getStateManager();
        stateManager.detach(skidmarks);
        skidmarks = new SkidMarksState(this);
        stateManager.attach(skidmarks);
    }

    public void startEngine() {
        if (!engine.isRunning()) {
            engine.setRunning(true);
        }
    }

    public void steer(float strength) {
        for (Wheel wheel : wheels) {
            wheel.steer(strength);
        }
    }

    /**
     * Determine the rotation angle for the steering wheel.
     *
     * @return the angle (in radians, negative = left, 0 = neutral, positive =
     * right)
     */
    public float steeringWheelAngle() {
        float steeringAngle = wheels.get(0).getSteeringAngle();
        float result = 2f * steeringAngle;

        return result;
    }

    public void stopEngine() {
        if (engine.isRunning()) {
            engine.setRunning(false);
        }
    }

    /**
     * Warp this vehicle to a suitable start position.
     */
    public void warpToStart() {
        /*
         * Cast a physics ray downward from the drop location.
         */
        Vector3f dropLocation = new Vector3f();
        world.locateDrop(dropLocation);
        Vector3f endLocation = dropLocation.add(0f, -999f, 0f);
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
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
        float yRotation = world.dropYRotation();
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
        skidmarks = new SkidMarksState(this);
        smokeEmitter = new TireSmokeEmitter(this);
        vehicleAudioState = new VehicleAudioState(this);
        wheelSpinState = new WheelSpinState(this);
    }

    /**
     * Determine the offset of the vehicle's ChaseCamera target in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    abstract protected void locateTarget(Vector3f storeResult);

    /**
     * Configure the "chassis": the entire Vehicle except for any wheels.
     *
     * @param folderName the name of the folder containing the C-G model asset
     * (not null, not empty)
     * @param cgmBaseFileName the base filename of the C-G model asset (not
     * null, not empty)
     * @param assetManager to load assets (not null)
     * @param mass in (in kilos, &gt;0)
     * @param damping the drag on the chassis due to air resistance (&ge;0,
     * &lt;1)
     */
    protected void setChassis(String folderName, String cgmBaseFileName,
            AssetManager assetManager, float mass, float damping) {
        Validate.nonEmpty(folderName, "folder name");
        Validate.nonEmpty(cgmBaseFileName, "base filename");
        Validate.nonNull(assetManager, "asset manager");
        Validate.positive(mass, "mass");
        Validate.fraction(damping, "damping");

        this.chassisDamping = damping;

        String assetPath
                = "/Models/" + folderName + "/" + cgmBaseFileName + ".j3o";
        chassis = assetManager.loadModel(assetPath);

        assetPath = "/Models/" + folderName + "/shapes/chassis-shape.j3o";
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
        vehicleControl.setLinearDamping(damping);
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
     * Alter the horn sound.
     *
     * @param sound the desired Sound (loaded), or null for silence
     */
    protected void setHornSound(Sound sound) {
        if (hornSound != null) {
            hornSound.detach();
        }
        this.hornSound = sound;
        if (sound != null) {
            sound.attachTo(node);
        }
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load the assets of this Vehicle.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        // do nothing
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        /*
         * Update the linear damping of the chassis.
         */
        float linearDamping = chassisDamping();
        for (Wheel wheel : wheels) {
            linearDamping += wheel.linearDamping();
        }
        //System.out.println("linearDamping = " + linearDamping);
        PhysicsVehicle physicsVehicle = getVehicleControl();
        physicsVehicle.setLinearDamping(linearDamping);
    }

    /**
     * Callback from Bullet, invoked just after the physics has been stepped.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }
    // *************************************************************************
    // VehicleSpeed methods

    /**
     * Determine the forward component of the vehicle's inertial velocity.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (in the specified units, may be negative)
     */
    @Override
    public float forwardSpeed(SpeedUnit speedUnit) {
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
     * Estimate the vehicle's maximum forward speed.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (in the specified units, &ge;0)
     */
    @Override
    public float maxForwardSpeed(SpeedUnit speedUnit) {
        float result = gearBox.maxForwardSpeed(speedUnit);
        assert result >= 0f : result;
        return result;
    }

    /**
     * Estimate the vehicle's maximum reverse speed.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (in the specified units, &le;0)
     */
    @Override
    public float maxReverseSpeed(SpeedUnit speedUnit) {
        float result = gearBox.maxForwardSpeed(speedUnit);
        assert result <= 0f : result;
        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Detach the AppStates associated with this Vehicle.
     */
    private void disable() {
        AppStateManager stateManager = world.getStateManager();
        stateManager.detach(gearboxState);
        stateManager.detach(skidmarks);
        stateManager.detach(smokeEmitter);
        stateManager.detach(vehicleAudioState);
        stateManager.detach(wheelSpinState);
    }

    /**
     * Attach the AppStates associated with this Vehicle.
     */
    private void enable() {
        AppStateManager stateManager = world.getStateManager();
        stateManager.attach(gearboxState);
        stateManager.attach(skidmarks);
        stateManager.attach(smokeEmitter);
        stateManager.attach(vehicleAudioState);
        stateManager.attach(wheelSpinState);
    }
}
