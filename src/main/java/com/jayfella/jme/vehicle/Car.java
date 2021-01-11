package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A Vehicle with wheels.
 */
abstract public class Car extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2 = Logger.getLogger(Car.class.getName());
    // *************************************************************************
    // fields

    /**
     * for testing TireSmokeEmitter
     */
    private boolean isBurningRubber = false;

    // wheel-related stuff. This isn't really "vehicle" related since a vehicle can be a boat or a helicopter.
    final private List<Wheel> wheels = new ArrayList<>(4);
    private MagicFormulaState magicFormulaState;
    private SkidMarksState skidmarks;
    /**
     * all available modes in the automatic transmission
     */
    final private String[] atModes = new String[]{"R", "D"};
    private TireSmokeEmitter smokeEmitter;
    private WheelSpinState wheelSpinState;
    // *************************************************************************
    // constructors

    public Car(String name) {
        super(name);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a single Wheel to this Car.
     *
     * @param wheelModel
     * @param connectionLocation
     * @param isSteering
     * @param isSteeringFlipped
     * @param mainBrakePeakForce (in Newtons, &ge;0)
     * @param parkingBrakePeakForce (in Newtons, &ge;0)
     * @param extraDamping (&ge;0, &lt;1)
     * @return the new Wheel
     */
    public Wheel addWheel(WheelModel wheelModel, Vector3f connectionLocation,
            boolean isSteering, boolean isSteeringFlipped,
            float mainBrakePeakForce, float parkingBrakePeakForce,
            float extraDamping) {
        VehicleControl vehicleControl = getVehicleControl();
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
        Wheel result = new Wheel(vehicleControl, wheelIndex, isSteering,
                isSteeringFlipped, suspension, mainBrake, parkingBrake,
                extraDamping);
        wheels.add(result);

        getNode().attachChild(wheelNode);

        return result;
    }

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

    public Wheel getWheel(int index) {
        return wheels.get(index);
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
     * Enumerate all available modes in the automatic transmission.
     *
     * @return the pre-existing array (not null)
     */
    public String[] listAtModes() {
        return atModes;
    }

    /**
     * Alter whether the tires are forced to emit smoke.
     *
     * @param setting true&rarr;forced, false&rarr;not forced
     */
    public void setBurningRubber(boolean setting) {
        this.isBurningRubber = setting;
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
    // *************************************************************************
    // Vehicle methods

    @Override
    public void build() {
        super.build();
        smokeEmitter = new TireSmokeEmitter(this);

        Spatial wheelSpatial = getWheel(0).getVehicleWheel().getWheelSpatial();
        BoundingBox bounds = (BoundingBox) wheelSpatial.getWorldBound();
        float markWidth = 0.75f * bounds.getZExtent();
        skidmarks = new SkidMarksState(this, markWidth);

        magicFormulaState = new MagicFormulaState(this);
        wheelSpinState = new WheelSpinState(this);
    }

    @Override
    protected void disable() {
        super.disable();

        AppStateManager manager = Main.getApplication().getStateManager();
        manager.detach(smokeEmitter);
        manager.detach(skidmarks);
        manager.detach(magicFormulaState);
        manager.detach(wheelSpinState);
    }

    @Override
    protected void enable() {
        super.enable();

        AppStateManager manager = Main.getApplication().getStateManager();
        manager.attach(smokeEmitter);
        manager.attach(skidmarks);
        manager.attach(magicFormulaState);
        manager.attach(wheelSpinState);
    }

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

    /**
     * Alter the value of the "accelerate" control signal and update the drive
     * force applied to each wheel.
     *
     * @param strength the "accelerate" control-signal value: between -1
     * (full-throttle reverse) and +1 (full-throttle forward) inclusive
     */
    @Override
    public void setAccelerateSignal(float strength) {
        super.setAccelerateSignal(strength);
        /*
         * Determine unsigned speed in world units per second.
         */
        float speed = getSpeed(SpeedUnit.WUPS);
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
     * Alter the values of the brake control signals and update the brake
     * impulses applied to each wheel.
     *
     * @param mainStrength the strength of the main-brake control signal,
     * between 0 (not applied) and 1 (applied as strongly as possible)
     * @param parkingStrength the strength of the parking-brake control signal,
     * between 0 (not applied) and 1 (applied as strongly as possible)
     */
    @Override
    public void setBrakeSignals(float mainStrength, float parkingStrength) {
        for (Wheel wheel : wheels) {
            wheel.updateBrakes(mainStrength, parkingStrength);
        }
    }

    @Override
    public void steer(float strength) {
        for (Wheel wheel : wheels) {
            wheel.steer(strength);
        }
    }
}
