package com.jayfella.jme.vehicle.niftydemo.view;

/**
 * Enumerate the flags supported by View.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum ViewFlags {
    // *************************************************************************
    // values

    /**
     * physics debug for joints
     */
    PhysicsJoints,
    /**
     * physics shapes for props
     */
    PropPhysics,
    /**
     * swept spheres for props
     */
    PropSpheres,
    /**
     * shadows
     */
    Shadows,
    /**
     * physics shapes for vehicles
     */
    VehiclePhysics,
    /**
     * swept spheres for vehicles
     */
    VehicleSpheres,
    /**
     * physics shape for the World
     */
    WorldPhysics;
}
