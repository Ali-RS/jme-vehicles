package com.jayfella.jme.vehicle;

/**
 * Interface to the steering system of a vehicle.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface VehicleSteering {
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the rotation angle of the steering wheel, handlebars, or
     * tiller.
     *
     * @return the angle (in radians, negative = left, 0 = neutral, positive =
     * right)
     */
    float steeringWheelAngle();
}
