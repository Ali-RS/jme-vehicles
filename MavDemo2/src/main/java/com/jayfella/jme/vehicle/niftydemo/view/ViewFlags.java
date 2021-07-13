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
     * collision shapes for props
     */
    PropShapes,
    /**
     * swept spheres for props
     */
    PropSpheres,
    /**
     * shadows
     */
    Shadows,
    /**
     * collision shapes for vehicles
     */
    VehicleShapes,
    /**
     * swept spheres for vehicles
     */
    VehicleSpheres,
    /**
     * collision shapes for the World
     */
    WorldShapes;
}
