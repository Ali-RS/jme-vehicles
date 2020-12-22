package com.jayfella.jme.vehicle.part;

import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;

/**
 * Manage a wheel's suspension.
 * <p>
 * If you use this class, you should avoid directly invoking certain physics
 * setters, namely:
 * <ul>
 * <li>setWheelsDampingCompression(),</li>
 * <li>setWheelsDampingRelaxation(), and</li>
 * <li>setSuspensionStiffness().</li>
 * </ul>
 */
public class Suspension {
    // *************************************************************************
    // fields

    /**
     * damping ratio for compression (0=undamped, 1=critically damped)
     */
    private float kCompress;
    /**
     * damping ratio for relaxation (0=undamped, 1=critically damped)
     */
    private float kRelax;
    /**
     * physics object being managed
     */
    final private VehicleWheel vehicleWheel;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a suspension with stiffness=25. TODO default to
     * kCompress=0.2, kRelax=0.3
     *
     * @param vehicleWheel the physics object to manage (not null, alias
     * created)
     * @param compression the desired damping ratio for compression (0=undamped,
     * 1=critically damped)
     * @param damping the desired damping ratio for relaxation (0=undamped,
     * 1=critically damped)
     */
    public Suspension(VehicleWheel vehicleWheel, float compression, float damping) {
        this.vehicleWheel = vehicleWheel;

        // we can't get compression and damping because they use a formula.
        this.kCompress = compression;
        this.kRelax = damping;

        setStiffness(25f);
        setCompression(this.kCompress);
        setDamping(this.kRelax);
        setMaxForce(10_000f);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the stiffness of this suspension.
     *
     * @param stiffness the desired stiffness constant (10&rarr;off-road buggy,
     * 50&rarr;sports car, 200&rarr;Formula-1 race car, default=5.88)
     */
    public void setStiffness(float stiffness) {
        this.vehicleWheel.setSuspensionStiffness(stiffness);

        setCompression(kCompress);
        setDamping(kRelax);
    }

    /**
     * Alter the damping for compression
     *
     * @param compression the desired damping ratio (0=undamped, 1=critically
     * damped)
     */
    public void setCompression(float compression) {
        this.kCompress = compression;
        float stiffness = vehicleWheel.getSuspensionStiffness();
        this.vehicleWheel.setWheelsDampingCompression(this.kCompress * 2.0f * FastMath.sqrt(stiffness));
    }

    /**
     * Alter the damping for relaxation. TODO rename
     *
     * @param damping the desired damping ratio (0=undamped, 1=critically
     * damped)
     */
    public void setDamping(float damping) {
        this.kRelax = damping;
        float stiffness = vehicleWheel.getSuspensionStiffness();
        this.vehicleWheel.setWheelsDampingRelaxation(this.kRelax * 2.0f * FastMath.sqrt(stiffness));
    }

    public void setMaxForce(float maxForce) {
        vehicleWheel.setMaxSuspensionForce(maxForce);
    }

    /**
     * Alter the maximum travel distance.
     *
     * Note that the units are centimeters ONLY if the physics-space unit is
     * exactly one meter.
     *
     * @param travelCm the desired maximum amount the suspension can be
     * compressed or expanded, relative to its rest length (in hundredths of a
     * physics-space unit)
     */
    public void setMaxTravelCm(float travelCm) {
        vehicleWheel.setMaxSuspensionTravelCm(travelCm);
    }

    public void setRestLength(float restLength) {
        vehicleWheel.setRestLength(restLength);
    }
}
