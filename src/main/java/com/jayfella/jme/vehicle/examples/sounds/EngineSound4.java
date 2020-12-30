package com.jayfella.jme.vehicle.examples.sounds;

import com.jayfella.jme.vehicle.Sound;

/**
 * A Sound built around "engine-4.ogg".
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class EngineSound4 extends Sound {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the Sound and load its assets.
     */
    public EngineSound4() {
        super.addOgg("engine-4d8", 10.7f);
        super.addOgg("engine-4d4", 21.4f);
        super.addOgg("engine-4d2", 42.9f);
        super.addOgg("engine-4", 85.75f);
        super.addOgg("engine-4x2", 171.5f);
    }
}
