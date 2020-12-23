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
            float brakeStrength) {
        VehicleControl vehicleControl = getVehicleControl();
        Node wheelNode = wheelModel.getWheelNode();
        Vector3f suspensionDirection = new Vector3f(0f, -1f, 0f);
        Vector3f axleDirection = new Vector3f(-1f, 0f, 0f);
        float restLength = 0.2f;
        float radius = ((BoundingBox) wheelNode.getWorldBound()).getZExtent(); // TODO
        VehicleWheel vehicleWheel = vehicleControl.addWheel(wheelNode,
                connectionLocation, suspensionDirection, axleDirection,
                restLength, radius, isSteering);

        int wheelIndex = wheels.size();
        Suspension suspension = new Suspension(vehicleWheel);
        Brake brake = new Brake(brakeStrength);
        Wheel wheel = new Wheel(vehicleControl, wheelIndex, isSteering,
                isSteeringFlipped, suspension, brake);
        wheels.add(wheel);

        getNode().attachChild(wheelNode);

        return wheel;
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

    @Override
    public void accelerate(float strength) {
        super.accelerate(strength);

        if (getEngine().isRunning()) {
            for (Wheel wheel : wheels) {
                if (wheel.getAccelerationForce() > 0f) {
                    /*
                     * Reduce power by up to 75% to simulate air resistance.
                     */
                    float currentKph = getSpeed(SpeedUnit.KMH);
                    float maxKph = getGearBox().getMaxSpeed(SpeedUnit.KMH);
                    float speedRatio = currentKph / maxKph;
                    float powerFactor = Math.max(0.25f, 1f - speedRatio);

                    float power = strength * getEngine().getPowerOutputAtRevs();
                    power *= powerFactor;
                    wheel.accelerate(power);

                } else {
                    // we always set this because the wheel could be "broken down" over time.
                    wheel.accelerate(0f);
                }
            }
        }
    }

    @Override
    public void applyEngineBraking() {
        for (int i = 0; i < getNumWheels(); ++i) {
            Wheel wheel = getWheel(i);

            // if the wheel is not "connected" to the engine, don't slow the wheel down using engine braking.
            // so if the wheel has 1 acceleration force, apply full engine braking.
            // but if the wheel has 0 acceleration force, it's not "connected" to the engine.
            float brakingForce = getEngine().getBraking() * wheel.getAccelerationForce();
            // System.out.println(brakingForce);
            // wheel.brake(brakingForce);
            getVehicleControl().brake(i, brakingForce);
        }
    }

    @Override
    public void brake(float strength) {
        for (Wheel wheel : wheels) {
            wheel.brake(strength);
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
    public void handbrake(float strength) {
        // just apply the brakes to the rear wheels.
        wheels.get(2).brake(strength, 100);
        wheels.get(3).brake(strength, 100);
    }

    @Override
    public void removeEngineBraking() {
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            getVehicleControl().brake(wheelIndex, 0);
        }
    }

    @Override
    public void setParkingBrakeApplied(boolean applied) {
        super.setParkingBrakeApplied(applied);

        if (applied) {
            wheels.get(2).brake(1);
            wheels.get(3).brake(1);
        } else {
            wheels.get(2).brake(0);
            wheels.get(3).brake(0);
        }
    }

    @Override
    public void steer(float strength) {
        for (Wheel wheel : wheels) {
            wheel.steer(strength);
        }
    }
}
