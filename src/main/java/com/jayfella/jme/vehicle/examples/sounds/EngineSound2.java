package com.jayfella.jme.vehicle.examples.sounds;

import com.jayfella.jme.vehicle.Sound;

/**
 * A Sound built around "engine-2.ogg".
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class EngineSound2 extends Sound {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the Sound and load its assets.
     */
    public EngineSound2() {
        super.addOgg("engine-2d2", 12.6f);
        super.addOgg("engine-2", 25.25f);
        super.addOgg("engine-2x2", 50.5f);
        super.addOgg("engine-2x4", 101f);
    }
}
