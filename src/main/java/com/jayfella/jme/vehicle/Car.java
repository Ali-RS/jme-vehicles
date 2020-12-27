package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.control.VehicleControl;
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
    final private static Logger logger = Logger.getLogger(Car.class.getName());
    // *************************************************************************
    // fields

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

    public Wheel addWheel(WheelModel wheelModel, Vector3f connectionLocation,
            boolean isSteering, boolean isSteeringFlipped,
            float mainBrakePeakForce, float parkingBrakePeakForce) {
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
                isSteeringFlipped, suspension, mainBrake, parkingBrake);
        wheels.add(result);

        getNode().attachChild(wheelNode);

        return result;
    }

    public int getNumWheels() {
        return wheels.size();
    }

    public Wheel getWheel(int index) {
        return wheels.get(index);
    }

    /**
     * Enumerate all available modes in the automatic transmission.
     *
     * @return the pre-existing array (not null)
     */
    public String[] listAtModes() {
        return atModes;
    }

    public void removeWheel(int index) {
        getVehicleControl().removeWheel(index);
        wheels.remove(index);
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

        Spatial oldSpatial = vehicleWheel.getWheelSpatial();
        assert oldSpatial.getParent() == getNode();
        oldSpatial.removeFromParent();

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
        Quaternion localRotation = oldSpatial.getLocalRotation();
        wheelModel.getSpatial().setLocalRotation(localRotation);

        Node wheelNode = wheelModel.getWheelNode();
        vehicleWheel.setWheelSpatial(wheelNode);

        getNode().attachChild(wheelNode);
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
        float maxWatts = getEngine().getPowerOutputAtRevs();
        //System.out.println("speed = " + speed + " rpm = " + getEngine().getRpm() + " maxWatts = " + maxWatts);
        float totalWatts = strength * maxWatts; // signed so that <0 means reverse
        for (Wheel wheel : wheels) {
            float powerFraction = wheel.getPowerFraction();
            float wheelPower = powerFraction * totalWatts;
            float wheelForce = wheelPower / speed;
            wheel.accelerate(wheelForce);
        }
    }

    @Override
    public void setBrakeSignal(float mainStrength, float parkingStrength) {
        for (Wheel wheel : wheels) {
            wheel.brake(mainStrength, parkingStrength);
        }
    }

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

    @Override
    public void steer(float strength) {
        for (Wheel wheel : wheels) {
            wheel.steer(strength);
        }
    }
}
