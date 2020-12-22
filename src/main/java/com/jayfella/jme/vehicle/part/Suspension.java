package com.jayfella.jme.vehicle.part;

import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;

/**
 * Manage a wheel's suspension. If you use this class, you should avoid directly
 * invoking the corresponding physics setters.
 */
public class Suspension {
    // *************************************************************************
    // fields

    private float stiffness;
    private float compression;
    private float damping;
    private float maxForce;
    private float restLength;
    /**
     * physics object being configured
     */
    final private VehicleWheel vehicleWheel;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a suspension with stiffness=25. TODO default to
     * kCompress=0.2, kRelax=0.3
     *
     * @param vehicleWheel the corresponding physics object (not null, alias
     * created)
     * @param compression the desired damping ratio for compression (0=undamped,
     * 1=critically damped)
     * @param damping the desired damping ratio for relaxation (0=undamped,
     * 1=critically damped)
     */
    public Suspension(VehicleWheel vehicleWheel, float compression, float damping) {
        this.vehicleWheel = vehicleWheel;

        // we can't get compression and damping because they use a formula.
        this.compression = compression;
        this.damping = damping;

        this.stiffness = 25; // vehicleWheel.getSuspensionStiffness();
        this.maxForce = 10000;
        this.restLength = 0.2f;

        setStiffness(this.stiffness);
        setCompression(this.compression);
        setDamping(this.damping);
        setMaxForce(10000);
    }
    // *************************************************************************
    // new methods exposed

    public float getStiffness() {
        return stiffness;
    }

    /**
     * Alter the stiffness of this suspension.
     *
     * @param stiffness the desired stiffness constant (10&rarr;off-road buggy,
     * 50&rarr;sports car, 200&rarr;Formula-1 race car, default=5.88)
     */
    public void setStiffness(float stiffness) {
        this.stiffness = stiffness;
        this.vehicleWheel.setSuspensionStiffness(stiffness);

        setCompression(compression);
        setDamping(damping);
    }

    public float getCompression() {
        return compression;
    }

    /**
     * Alter the damping for compression
     *
     * @param compression the desired damping ratio (0=undamped, 1=critically
     * damped)
     */
    public void setCompression(float compression) {
        this.compression = compression;
        this.vehicleWheel.setWheelsDampingCompression(this.compression * 2.0f * FastMath.sqrt(stiffness));
    }

    public float getDamping() {
        return damping;
    }

    /**
     * Alter the damping for relaxation. TODO rename
     *
     * @param damping the desired damping ratio (0=undamped, 1=critically
     * damped)
     */
    public void setDamping(float damping) {
        this.damping = damping;
        this.vehicleWheel.setWheelsDampingRelaxation(this.damping * 2.0f * FastMath.sqrt(stiffness));
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
