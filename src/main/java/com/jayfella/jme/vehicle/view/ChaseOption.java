package com.jayfella.jme.vehicle.view;

/**
 * Enumerate chase-behavior options for ChaseCamera.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum ChaseOption {
    // *************************************************************************
    // values

    /**
     * Ignore the target vehicle's forward direction.
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
    StrictFollow;
}
