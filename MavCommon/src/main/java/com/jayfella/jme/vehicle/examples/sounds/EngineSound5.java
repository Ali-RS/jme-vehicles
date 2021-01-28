package com.jayfella.jme.vehicle.examples.sounds;

import com.jayfella.jme.vehicle.Sound;

/**
 * A Sound built around "engine-5.ogg".
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class EngineSound5 extends Sound {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the Sound and load its assets.
     */
    public EngineSound5() {
        super.addOgg("engine-5d4", 14.4f);
        super.addOgg("engine-5d2", 28.9f);
        super.addOgg("engine-5", 57.75f);
        super.addOgg("engine-5x2", 115.5f);
    }
}
