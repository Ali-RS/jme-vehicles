package com.jayfella.jme.vehicle.view;

/**
 * Enumerate the ChaseCamera functions that can be controlled by signals.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum CcFunctions {
    // *************************************************************************
    // values

    /**
     * translate backward (camera's -Z direction)
     */
    Back,
    /**
     * activate drag-to-orbit
     */
    DragToOrbit,
    /**
     * translate forward (camera's look or +Z direction)
     */
    Forward,
    /**
     * orbit in the camera's actual "down" direction
     */
    OrbitDown,
    /**
     * orbit in the camera's actual "up" direction
     */
    OrbitUp,
    /**
     * use X-ray vision (no line-of-sight constraint)
     */
    Xray,
    /**
     * magnify the scene
     */
    ZoomIn,
    /**
     * de-magnify the scene
     */
    ZoomOut;
}
