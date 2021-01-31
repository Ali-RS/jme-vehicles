package com.jayfella.jme.vehicle.part;

import jme3utilities.Validate;

/**
 * Model a single brake, such a disc brake on a Wheel.
 *
 * Derived from the Brake class in the Advanced Vehicles project.
 */
public class Brake {
    // *************************************************************************
    // fields - TODO model failures due to wear and overheating

    /**
     * peak force (in Newtons, &ge;0)
     */
    private float peakForce;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Brake with the specified peak force.
     *
     * @param force the desired peak force (in Newtons, &ge;0)
     */
    public Brake(float force) {
        Validate.nonNegative(force, "force");
        this.peakForce = force;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the peak force.
     *
     * @return the peak force (in Newtons, &ge;0)
     */
    public float getPeakForce() {
        return peakForce;
    }

    /**
     * Alter the peak force.
     *
     * @param force the desired peak force (in Newtons, &ge;0)
     */
    public void setPeakForce(float force) {
        Validate.nonNegative(force, "force");
        peakForce = force;
    }
}
