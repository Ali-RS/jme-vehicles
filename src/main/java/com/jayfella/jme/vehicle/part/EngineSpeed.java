package com.jayfella.jme.vehicle.part;

/**
 * Interface to the speed (and idle/redline speeds) of an Engine, for use in
 * tachometers.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface EngineSpeed {
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the idle speed as a fraction of the redline.
     *
     * @return the fraction (&ge;0)
     */
    float idleFraction();

    /**
     * Determine the idle speed of the Engine.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &ge;0,
     * &le;redlineRpm)
     */
    float idleRpm();

    /**
     * Determine the redline speed of the Engine.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &gt;0,
     * &ge;idleRpm)
     */
    float redlineRpm();

    /**
     * Determine the current engine speed.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &ge;0)
     */
    float rpm();

    /**
     * Determine the current engine speed as a fraction of the redline.
     *
     * @return the fraction (&ge;0, &le;1)
     */
    float rpmFraction();
}
