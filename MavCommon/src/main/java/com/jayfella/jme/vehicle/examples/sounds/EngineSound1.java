package com.jayfella.jme.vehicle.examples.sounds;

import com.jayfella.jme.vehicle.Sound;

/**
 * A Sound built around "engine-1.ogg".
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class EngineSound1 extends Sound {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the Sound and load its assets.
     */
    public EngineSound1() {
        super.addOgg("engine-1d2", 13.4f);
        super.addOgg("engine-1", 26.75f);
        super.addOgg("engine-1x2", 53.5f);
        super.addOgg("engine-1x4", 107f);
    }
}
