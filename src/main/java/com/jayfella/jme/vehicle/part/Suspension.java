package com.jayfella.jme.vehicle.part;

import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;

public class Suspension {

    private float stiffness;
    private float compression;
    private float dampness;
    private float maxForce;
    private float restLength;

    private final VehicleWheel vehicleWheel;

    public Suspension(VehicleWheel vehicleWheel, float compression, float dampness) {
        this.vehicleWheel = vehicleWheel;

        // we can't get compression and dampness because they use a formula.
        this.compression = compression;
        this.dampness = dampness;

        this.stiffness = 25; // vehicleWheel.getSuspensionStiffness();
        this.maxForce = 10000;
        this.restLength = 0.2f;

        setStiffness(this.stiffness);
        setCompression(this.compression);
        setDampness(this.dampness);
        setMaxForce(10000);
    }

    public float getStiffness() {
        return stiffness;
    }

    public void setStiffness(float stiffness) {
        this.stiffness = stiffness;
        this.vehicleWheel.setSuspensionStiffness(stiffness);

        setCompression(compression);
        setDampness(dampness);
    }

    public float getCompression() {
        return compression;
    }

    public void setCompression(float compression) {
        this.compression = compression;
        this.vehicleWheel.setWheelsDampingCompression(this.compression * 2.0f * FastMath.sqrt(stiffness));
    }

    public float getDampness() {
        return dampness;
    }

    public void setDampness(float dampness) {
        this.dampness = dampness;
        this.vehicleWheel.setWheelsDampingRelaxation(this.dampness * 2.0f * FastMath.sqrt(stiffness));
    }

    public float getMaxForce() {
        return maxForce;
    }

    public void setMaxForce(float maxForce) {
        this.maxForce = maxForce;
        vehicleWheel.setMaxSuspensionForce(this.maxForce);

    }

    public float getRestLength() {
        return restLength;
    }

    public void setRestLength(float restLength) {
        this.restLength = restLength;
        vehicleWheel.setRestLength(restLength);
        // vehicleWheel.setMaxSuspensionTravelCm(this.restLength);
    }
}
