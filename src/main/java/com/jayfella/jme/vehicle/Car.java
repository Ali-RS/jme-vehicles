package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

public class Car extends Vehicle {

    // wheel-related stuff. This isn't really "vehicle" related since a vehicle can be a boat or a helicopter.
    private final List<Wheel> wheels = new ArrayList<>();
    /**
     * all available modes in the automatic transmission
     */
    final private String[] atModes = new String[]{"R", "D"};
    private TireSmokeEmitter smokeEmitter;
    private SkidMarksState skidmarks;
    private WheelSpinState wheelSpinState;
    private MagicFormulaState magicFormulaState;

    public Car(Application app, String name) {
        super(app, name);
    }

    public int getNumWheels() {
        return wheels.size();
    }

    public Wheel getWheel(int index) {
        return wheels.get(index);
    }

    public Wheel addWheel(Spatial model, Vector3f connectionPoint, boolean isSteering, boolean steeringFlipped, Brake brake) {

        Vector3f direction = new Vector3f(0, -1, 0);
        Vector3f axle = new Vector3f(-1, 0, 0);

        float restlength = 0.2f;
        float radius = ((BoundingBox)model.getWorldBound()).getZExtent();

        // boolean isFrontWheel = false;

        VehicleWheel vehicleWheel = getVehicleControl().addWheel(
                model,
                connectionPoint,
                direction,
                axle,
                restlength,
                radius,
                isSteering);


        int index = getVehicleControl().getNumWheels() - 1;

        Suspension suspension = new Suspension(vehicleWheel, 0.2f, 0.3f);

        Wheel wheel = new Wheel(getVehicleControl(), index, isSteering, steeringFlipped, suspension, brake);

        wheels.add(wheel);
        getNode().attachChild(model);

        return wheel;
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
        this.wheels.remove(index);
    }

    @Override
    public void accelerate(float strength) {
        super.accelerate(strength);

        if (getEngine().isStarted()) {
            for (Wheel wheel : wheels) {
                if (wheel.getAccelerationForce() > 0) {

                    float power = (getEngine().getPowerOutputAtRevs() * strength);

                    // so the faster we go, the less force the vehicle can apply.
                    // this simulates making it harder to accelerate at higher speeds
                    // realistically this makes it difficult to achieve the max speed.
                    float speedRatio = 1.0f - (getSpeed(SpeedUnit.KMH) / getGearBox().getMaxSpeed(SpeedUnit.KMH));
                    speedRatio = Math.max(0.25f, speedRatio);

                    wheel.accelerate(power * speedRatio);
                }
                else {

                    // we always set this because the wheel could be "broken down" over time.
                    wheel.accelerate(0);
                }
            }
        }
    }

    @Override
    public void brake(float strength) {

        for (Wheel wheel : wheels) {
            wheel.brake(strength);
        }
    }

    @Override
    public void handbrake(float strength) {
        // just apply the brakes to the rear wheels.
        wheels.get(2).brake(strength, 100);
        wheels.get(3).brake(strength, 100);
    }

    @Override
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

    public void setTireSmokeEnabled(boolean enabled) {
        this.smokeEmitter.setEnabled(enabled);
    }

    public void setTireSkidMarksVisible(boolean enabled) {
        this.skidmarks.setEnabled(enabled);
    }

    public void setTireSkidMarksEnabled(boolean enabled) {
        this.skidmarks.setSkidmarkEnabled(enabled);
    }

    @Override
    public void setParkingBrakeApplied(boolean applied) {
        super.setParkingBrakeApplied(applied);

        if (applied) {
            wheels.get(2).brake(1);
            wheels.get(3).brake(1);
        }
        else {
            wheels.get(2).brake(0);
            wheels.get(3).brake(0);
        }
    }

    @Override
    public void build() {
        super.build();
        this.smokeEmitter = new TireSmokeEmitter(this);

        Spatial wheelSpatial = getWheel(0).getVehicleWheel().getWheelSpatial();
        BoundingBox bounds = (BoundingBox) wheelSpatial.getWorldBound();
        float markWidth = 0.75f * bounds.getZExtent();
        skidmarks = new SkidMarksState(this, markWidth);

        this.magicFormulaState = new MagicFormulaState(this);
        this.wheelSpinState = new WheelSpinState(this);
    }

    @Override
    protected void enable() {
        super.enable();

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(smokeEmitter);
        manager.attach(skidmarks);
        manager.attach(magicFormulaState);
        manager.attach(wheelSpinState);
    }

    @Override
    protected void disable() {
        super.disable();

        AppStateManager manager = getApplication().getStateManager();
        manager.detach(smokeEmitter);
        manager.detach(skidmarks);
        manager.detach(magicFormulaState);
        manager.detach(wheelSpinState);
    }

    @Override
    public void applyEngineBraking() {

        for (int i = 0; i < getNumWheels(); i++) {
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
    public void removeEngineBraking() {
        for (int i = 0; i < getNumWheels(); i++) {
            getVehicleControl().brake(i, 0);
        }
    }


}
