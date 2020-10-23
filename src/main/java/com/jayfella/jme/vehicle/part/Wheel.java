package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.tire.PajeckaTireModel;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class Wheel {

    private final VehicleControl vehicleControl;
    private final int wheelIndex;
    private final VehicleWheel vehicleWheel;

    // allows us to steer with rear wheels by flipping the direction and power.
    private boolean steering;
    private boolean steeringFlipped;

    // determines whether this wheel provides acceleration in a 0..1 range.
    // this can be used for settings FWD, RWD, 60/40 distribution, etc...
    private float accelerationForce = 0;

    private float maxSteerAngle = FastMath.QUARTER_PI;
    // private float maxSteerAngle = 30 * FastMath.DEG_TO_RAD;

    private final Suspension suspension;

    private final Brake brake;

    private PajeckaTireModel tireModel;

    private float rotationDelta;

    // simulates degradation. 1.0 = full grip the tyre allows, 0.0 = the tyre is dead.
    private float grip = 1.0f;

    // the amount of braking strength being applied. Between 0 and 1
    private float brakeStrength = 0;

    public Wheel(VehicleControl vehicleControl, int wheelIndex, boolean isSteering, boolean steeringFlipped, Suspension suspension, Brake brake) {

        this.vehicleControl = vehicleControl;

        this.wheelIndex = wheelIndex;
        this.vehicleWheel = vehicleControl.getWheel(wheelIndex);

        this.steering = isSteering;
        this.steeringFlipped = steeringFlipped;

        this.suspension = suspension;
        this.brake = brake;

        setFriction(1f);
    }

    public PajeckaTireModel getTireModel() {
        return tireModel;
    }

    public void setTireModel(PajeckaTireModel tireModel) {
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
        this.vehicleWheel.setFrictionSlip(friction);
    }

    public boolean isSteering() {
        return steering;
    }

    public void setSteering(boolean steering, boolean flipped) {
        setSteering(steering);
        setSteeringFlipped(flipped);
    }

    public void setSteering(boolean steering) {
        this.steering = steering;
        this.vehicleWheel.setFrontWheel(steering);
    }

    public boolean isSteeringFlipped() { return steeringFlipped; }
    public void setSteeringFlipped(boolean steeringFlipped) { this.steeringFlipped = steeringFlipped; }

    public float getAccelerationForce() { return accelerationForce; }

    /**
     * The amount of force applied to this wheel when the vehicle is accelerating
     * Value is in the 0 to 1 range. 0 = no force, 1 = full force.
     * This acts as a multiplier for the engine power to this wheel.
     * @param accelerationForce the amount of force to apply to this wheel when accelerating.
     */
    public void setAccelerationForce(float accelerationForce) { this.accelerationForce = accelerationForce; }

    // public float getBrakeForce() { return brakeForce; }
    // public void setBrakeForce(float brakeForce) { this.brakeForce = brakeForce; }

    public void accelerate(float strength) {
        vehicleControl.accelerate(wheelIndex, accelerationForce * strength);
    }

    /**
     * Causes the wheel to slow down.
     * @param strength the strength of the braking force from 0 - 1.
     */
    public void brake(float strength) {
        this.brakeStrength = strength;
        vehicleControl.brake(wheelIndex, brake.getStrength() * strength);
    }

    public float getBrakeStrength() {
        return this.brakeStrength;
    }


    /**
     * Causes the wheel to slow down. This method is usually used for a handbrake. It overrides the specified brake strength.
     * @param strength      the force of the brake from 0 - 1
     * @param brakeStrength the strength of the brake force at 1 (100%).
     */
    public void brake(float strength, float brakeStrength) {
        vehicleControl.brake(wheelIndex, brakeStrength * strength);
    }

    public Brake getBrake() {
        return this.brake;
    }

    private float steeringAngle = 0;

    public void steer(float strength) {
        if (isSteering()) {
            if (steeringFlipped) {
                steeringAngle = getMaxSteerAngle() * -strength;
            }
            else {
                steeringAngle = getMaxSteerAngle() * strength;
            }

            this.vehicleControl.steer(wheelIndex, steeringAngle);
        }
    }

    public float getSteeringAngle() {
        return this.steeringAngle;
    }

    public float getMaxSteerAngle() {

        //float speed = 1.0f - (vehicleControl.getCurrentVehicleSpeedKmHour() / 200);

        return maxSteerAngle;// * speed;
    }

    public void setMaxSteerAngle(float maxSteerAngle) {
        this.maxSteerAngle = maxSteerAngle;
    }

    public float getSize() {
        return vehicleWheel.getWheelSpatial().getLocalScale().y; // they should all be the same.
    }

    public void setSize(float scale) {
        vehicleWheel.getWheelSpatial().setLocalScale(scale);
        // Vector3f bounds = ((BoundingBox)vehicleWheel.getWheelSpatial().getWorldBound()).getExtent(null);
        vehicleWheel.setRadius(scale * 0.5f);
    }

    public Suspension getSuspension() { return suspension; }

    public VehicleWheel getVehicleWheel() {
        return this.vehicleWheel;
    }


    // Pacejka
    // LATERAL
    // the slip angle is the angle between the direction in which a wheel is pointing
    // and the direction in which the vehicle is traveling.
    public float calculateLateralSlipAngle() {

        Quaternion wheelRot = vehicleControl.getPhysicsRotation().mult(
                new Quaternion().fromAngles(new float[]{0, getSteeringAngle(), 0}));

        Vector3f wheelDir = wheelRot.getRotationColumn(2);

        Vector3f vehicleTravel;

        if (vehicleControl.getCurrentVehicleSpeedKmHour() < 5) {
            vehicleTravel = vehicleControl.getPhysicsRotation().getRotationColumn(2);
        }
        else {
            vehicleTravel = vehicleControl.getLinearVelocity().normalizeLocal();
            vehicleTravel.setY(0);
        }

        float minAngle = 0.1f;

        float angle = minAngle + wheelDir.angleBetween(vehicleTravel);
        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + angle * FastMath.RAD_TO_DEG);

        // angle = Math.max(0.1f, angle);
        angle = FastMath.clamp(angle, 0, FastMath.QUARTER_PI);

        return angle;

    }

    // the slip angle for this is how much force is being applied to the tyre (acceleration force).
    // how much rotation has been applied as a result of acceleration.
    public float calculateLongitudinalSlipAngle() {

        // the rotation of the wheel as if it were just following a moving vehicle.
        // that is to say a wheel that is rolling without slip.
        float normalRot = vehicleWheel.getDeltaRotation();// * 0.5f;

        // the rotation applied via wheelspin
        float wheelSpinRot = getRotationDelta();// * 1.5f;

        // combined rotation of normal roll + wheelspin
        float rot = wheelSpinRot + normalRot;

        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + rot);


        float vel = vehicleControl.getLinearVelocity().length();

        float minAngle = 0.1f;

        float angle = rot / vel;
        angle *= 10;
        //angle += minAngle;

        //result = FastMath.QUARTER_PI - result;
        // return result + 0.1f;
        // float slip = 1.0f - vehicleWheel.getSkidInfo();
        // slip *= FastMath.QUARTER_PI;
        //return slip;

        angle = FastMath.clamp(angle, 0, FastMath.TWO_PI);
        return angle;
    }

    public float getRotationDelta() {
        return rotationDelta;
    }

    public void setRotationDelta(float rotationDelta) {
        this.rotationDelta = rotationDelta;
    }


}
