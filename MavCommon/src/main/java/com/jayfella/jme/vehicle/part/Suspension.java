package com.jayfella.jme.vehicle.part;

import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;

/**
 * Manage a wheel's suspension.
 * <p>
 * If you use this class, you should avoid directly invoking certain physics
 * setters, notably:
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
     * Instantiate a suspension with reasonable parameter settings.
     *
     * @param vehicleWheel the physics object to manage (not null, alias
     * created)
     */
    public Suspension(VehicleWheel vehicleWheel) {
        this.vehicleWheel = vehicleWheel;

        setCompressDamping(0.2f);
        setMaxForce(10_000f);
        setRelaxDamping(0.3f);
        setStiffness(25f);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the damping for compression.
     *
     * @return the damping ratio (0=undamped, 1=critically damped)
     */
    public float getCompressDamping() {
        return kCompress;
    }

    /**
     * Determine the maximum force exerted by this suspension.
     *
     * @return the maximum force (in Newtons)
     */
    public float getMaxForce() {
        float result = vehicleWheel.getMaxSuspensionForce();
        return result;
    }

    /**
     * Determine the damping for relaxation.
     *
     * @return the damping ratio (0=undamped, 1=critically damped)
     */
    public float getRelaxDamping() {
        return kRelax;
    }

    /**
     * Determine the rest length of this suspension.
     *
     * @return the length (in physics-space units)
     */
    public float getRestLength() {
        float result = vehicleWheel.getRestLength();
        return result;
    }

    /**
     * Determine the stiffness of this suspension.
     *
     * @return the stiffness constant (10&rarr;off-road buggy, 50&rarr;sports
     * car, 200&rarr;Formula-1 race car)
     */
    public float getStiffness() {
        float result = vehicleWheel.getSuspensionStiffness();
        return result;
    }

    /**
     * Alter the damping for compression.
     *
     * @param dampingRatio the desired damping ratio (0=undamped, 1=critically
     * damped, default=0.2)
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
     * @param maxForce the desired maximum force (default=10_000)
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
     * physics-space unit, default=500)
     */
    public void setMaxTravelCm(float travelCm) {
        vehicleWheel.setMaxSuspensionTravelCm(travelCm);
    }

    /**
     * Alter the damping for relaxation.
     *
     * @param dampingRatio the desired damping ratio (0=undamped, 1=critically
     * damped, default=0.3)
     */
    public void setRelaxDamping(float dampingRatio) {
        kRelax = dampingRatio;

        float stiffness = vehicleWheel.getSuspensionStiffness();
        float damp = 2 * dampingRatio * FastMath.sqrt(stiffness);
        vehicleWheel.setWheelsDampingRelaxation(damp);
    }

    /**
     * Alter the length of this suspension. Bullet updates the length on every
     * physics tick. TODO so remove this?
     *
     * @param restLength the desired length (in physics-space units)
     */
    public void setRestLength(float restLength) {
        vehicleWheel.setRestLength(restLength);
    }

    /**
     * Alter the stiffness of this suspension.
     *
     * @param stiffness the desired stiffness constant (10&rarr;off-road buggy,
     * 50&rarr;sports car, 200&rarr;Formula-1 race car, default=25)
     */
    public void setStiffness(float stiffness) {
        vehicleWheel.setSuspensionStiffness(stiffness);

        setCompressDamping(kCompress);
        setRelaxDamping(kRelax);
    }
}
