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
     * Instantiate a suspension with stiffness=25 and maxForce=10000. TODO
     * default to kCompress=0.2, kRelax=0.3
     *
     * @param vehicleWheel the physics object to manage (not null, alias
     * created)
     * @param kCompress the desired damping ratio for compression (0=undamped,
     * 1=critically damped)
     * @param kRelax the desired damping ratio for relaxation (0=undamped,
     * 1=critically damped)
     */
    public Suspension(VehicleWheel vehicleWheel, float kCompress,
            float kRelax) {
        this.vehicleWheel = vehicleWheel;

        setCompressDamping(kCompress);
        setMaxForce(10_000f);
        setRelaxDamping(kRelax);
        setStiffness(25f);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the damping for compression.
     *
     * @param dampingRatio the desired damping ratio (0=undamped, 1=critically
     * damped)
     */
    public void setCompressDamping(float dampingRatio) {
        kCompress = dampingRatio;

        float stiffness = vehicleWheel.getSuspensionStiffness();
        float damp = 2 * dampingRatio * FastMath.sqrt(stiffness);
        vehicleWheel.setWheelsDampingCompression(damp);
    }

    /**
     * Alter the maximum force exerted by this suspension.
     * <p>
     * Increase this if the suspension cannot handle the weight of your vehicle.
     *
     * @param maxForce the desired maximum force
     */
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

    /**
     * Alter the damping for relaxation.
     *
     * @param dampingRatio the desired damping ratio (0=undamped, 1=critically
     * damped)
     */
    public void setRelaxDamping(float dampingRatio) {
        kRelax = dampingRatio;

        float stiffness = vehicleWheel.getSuspensionStiffness();
        float damp = 2 * dampingRatio * FastMath.sqrt(stiffness);
        vehicleWheel.setWheelsDampingRelaxation(damp);
    }

    public void setRestLength(float restLength) {
        vehicleWheel.setRestLength(restLength);
    }

    /**
     * Alter the stiffness of this suspension.
     *
     * @param stiffness the desired stiffness constant (10&rarr;off-road buggy,
     * 50&rarr;sports car, 200&rarr;Formula-1 race car, default=5.88)
     */
    public void setStiffness(float stiffness) {
        vehicleWheel.setSuspensionStiffness(stiffness);

        setCompressDamping(kCompress);
        setRelaxDamping(kRelax);
    }
}
