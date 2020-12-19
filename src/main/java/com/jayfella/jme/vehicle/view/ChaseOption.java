package com.jayfella.jme.vehicle.view;

/**
 * Enumerate chase options for camera controllers.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum ChaseOption {
    // *************************************************************************
    // values

    /**
     * Ignore the direction the target vehicle is facing.
     *
     * Freely orbit the target, including left/right, based on user input.
     *
     * If the target moves, maintain a constant offset from it.
     */
    FreeOrbit,
    /**
     * Stay directly behind the target vehicle.
     *
     * Orbit the target based on user input, but only up/down.
     *
     * If the target moves, maintain a constant distance and elevation from it.
     */
    StrictChase;
}
