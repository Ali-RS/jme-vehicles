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
     * Instantiate the Sound.
     */
    public EngineSound5() {
        super.addAssetPath("/Audio/engine-5d4.ogg", 14.4f);
        super.addAssetPath("/Audio/engine-5d2.ogg", 28.9f);
        super.addAssetPath("/Audio/engine-5.ogg", 57.75f);
        super.addAssetPath("/Audio/engine-5x2.ogg", 115.5f);
    }
}
