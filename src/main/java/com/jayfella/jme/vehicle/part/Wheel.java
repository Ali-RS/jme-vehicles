package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;

/**
 * A single wheel of a Car.
 */
public class Wheel {
    // *************************************************************************
    // fields

    private boolean isSteering;
    /**
     * steer with a rear wheel by flipping the direction
     */
    private boolean isSteeringFlipped;
    /**
     * fraction of the total drive power to apply to this wheel (&ge;0, &le;1)
     */
    private float powerFraction = 0f;

    final private VehicleControl vehicleControl;
    final private int wheelIndex;
    final private VehicleWheel vehicleWheel;

    private float maxSteerAngle = FastMath.QUARTER_PI;
    private float steeringAngle = 0f;

    final private Suspension suspension;

    final private Brake brake;

    private PacejkaTireModel tireModel;

    private float rotationDelta;

    // grip degradation: 1 = full grip the tire allows, 0 = dead tire
    private float grip = 1f;

    // the amount of braking strength being applied. Between 0 and 1
    private float brakeStrength = 0f;
    // *************************************************************************
    // constructors

    public Wheel(VehicleControl vehicleControl, int wheelIndex,
            boolean isSteering, boolean steeringFlipped, Suspension suspension,
            Brake brake) {
        this.vehicleControl = vehicleControl;

        this.wheelIndex = wheelIndex;
        this.vehicleWheel = vehicleControl.getWheel(wheelIndex);

        this.isSteering = isSteering;
        this.isSteeringFlipped = steeringFlipped;

        this.suspension = suspension;
        this.brake = brake;

        setFriction(1f);
    }
    // *************************************************************************
    // new methods exposed

    public PacejkaTireModel getTireModel() {
        return tireModel;
    }

    public void setTireModel(PacejkaTireModel tireModel) {
        this.tireModel = tireModel;
    }

    public float getGrip() {
        return grip;
    }

    public void setGrip(float grip) {
        this.grip = grip;
    }

    public float getFriction() {
        return vehicleWheel.getFrictionSlip();
    }

    public void setFriction(float friction) {
        vehicleWheel.setFrictionSlip(friction);
    }

    public boolean isSteering() {
        return isSteering;
    }

    public void setSteering(boolean steering, boolean flipped) {
        setSteering(steering);
        setSteeringFlipped(flipped);
    }

    public void setSteering(boolean steering) {
        this.isSteering = steering;
        vehicleWheel.setFrontWheel(steering);
    }

    public boolean isSteeringFlipped() {
        return isSteeringFlipped;
    }

    public void setSteeringFlipped(boolean steeringFlipped) {
        this.isSteeringFlipped = steeringFlipped;
    }

    /**
     * Determine the fraction of the total drive power to apply to this wheel.
     *
     * @return the power fraction (&ge;0, &le;1)
     */
    public float getPowerFraction() {
        return powerFraction;
    }

    /**
     * Alter the fraction of the total drive power that gets applied to this
     * wheel.
     *
     * @param fraction the desired power fraction (&ge;0, &le;1)
     */
    public void setPowerFraction(float fraction) {
        Validate.fraction(fraction, "fraction");
        powerFraction = fraction;
    }

    /**
     * Update the drive force applied via this wheel.
     *
     * @param force the amount of drive force (negative if reversing)
     */
    public void accelerate(float force) {
        vehicleControl.accelerate(wheelIndex, force);
    }

    /**
     * Causes the wheel to slow down.
     *
     * @param strength the strength of the braking force from 0 - 1.
     */
    public void brake(float strength) {
        brakeStrength = strength;
        vehicleControl.brake(wheelIndex, brake.getStrength() * strength);
    }

    public float getBrakeStrength() {
        return brakeStrength;
    }

    /**
     * Causes the wheel to slow down. This method is usually used for a
     * handbrake. It overrides the specified brake strength.
     *
     * @param strength the force of the brake from 0 - 1
     * @param brakeStrength the strength of the brake force at 1 (100%).
     */
    public void brake(float strength, float brakeStrength) {
        vehicleControl.brake(wheelIndex, brakeStrength * strength);
    }

    public Brake getBrake() {
        return brake;
    }

    public void steer(float strength) {
        if (isSteering()) {
            if (isSteeringFlipped) {
                steeringAngle = getMaxSteerAngle() * -strength;
            } else {
                steeringAngle = getMaxSteerAngle() * strength;
            }

            vehicleControl.steer(wheelIndex, steeringAngle);
        }
    }

    public float getSteeringAngle() {
        return steeringAngle;
    }

    public float getMaxSteerAngle() {
        return maxSteerAngle;
    }

    public void setMaxSteerAngle(float maxSteerAngle) {
        this.maxSteerAngle = maxSteerAngle;
    }

    public float getSize() {
        return vehicleWheel.getWheelSpatial().getLocalScale().y; // they should all be the same.
    }

    public void setSize(float diameter) {
        vehicleWheel.getWheelSpatial().setLocalScale(diameter);
        vehicleWheel.setRadius(diameter / 2);
    }

    public Suspension getSuspension() {
        return suspension;
    }

    public VehicleWheel getVehicleWheel() {
        return vehicleWheel;
    }

    // Pacejka
    // LATERAL
    // the slip angle is the angle between the direction in which a wheel is pointing
    // and the direction in which the vehicle is traveling.
    public float calculateLateralSlipAngle() {
        Quaternion wheelRot = vehicleControl.getPhysicsRotation().mult(
                new Quaternion().fromAngles(0f, getSteeringAngle(), 0f));

        Vector3f wheelDir = wheelRot.getRotationColumn(2);
        Vector3f vehicleTravel;
        if (vehicleControl.getCurrentVehicleSpeedKmHour() < 5f) {
            vehicleTravel = vehicleControl.getPhysicsRotation().getRotationColumn(2);
        } else {
            vehicleTravel = vehicleControl.getLinearVelocity().normalizeLocal();
            vehicleTravel.setY(0f);
        }

        float minAngle = 0.1f;
        float angle = minAngle + wheelDir.angleBetween(vehicleTravel);
        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + angle * FastMath.RAD_TO_DEG);

        angle = FastMath.clamp(angle, 0f, FastMath.QUARTER_PI);

        return angle;
    }

    // the slip angle for this is how much force is being applied to the tire (acceleration force).
    // how much rotation has been applied as a result of acceleration.
    public float calculateLongitudinalSlipAngle() {
        // the rotation of the wheel as if it were just following a moving vehicle.
        // that is to say a wheel that is rolling without slip.
        float normalRot = vehicleWheel.getDeltaRotation();

        // the rotation applied via wheelspin
        float wheelSpinRot = getRotationDelta();

        // combined rotation of normal roll + wheelspin
        float rot = wheelSpinRot + normalRot;

        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + rot);
        float vel = vehicleControl.getLinearVelocity().length();

        float minAngle = 0.1f;

        float angle = rot / vel;
        angle *= 10f;
        //angle += minAngle;

        //result = FastMath.QUARTER_PI - result;
        // return result + 0.1f;
        // float slip = 1.0f - vehicleWheel.getSkidInfo();
        // slip *= FastMath.QUARTER_PI;
        //return slip;
        angle = FastMath.clamp(angle, 0f, FastMath.TWO_PI);
        return angle;
    }

    public float getRotationDelta() {
        return rotationDelta;
    }

    public void setRotationDelta(float rotationDelta) {
        this.rotationDelta = rotationDelta;
    }
}
