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
     * Instantiate the Sound.
     */
    public EngineSound2() {
        super.addAssetPath("/Audio/engine-2d2.ogg", 12.6f);
        super.addAssetPath("/Audio/engine-2.ogg", 25.25f);
        super.addAssetPath("/Audio/engine-2x2.ogg", 50.5f);
        super.addAssetPath("/Audio/engine-2x4.ogg", 101f);
    }
}
