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
     * Instantiate the Sound.
     */
    public EngineSound4() {
        super.addAssetPath("/Audio/engine-4d8.ogg", 10.7f);
        super.addAssetPath("/Audio/engine-4d4.ogg", 21.4f);
        super.addAssetPath("/Audio/engine-4d2.ogg", 42.9f);
        super.addAssetPath("/Audio/engine-4.ogg", 85.75f);
        super.addAssetPath("/Audio/engine-4x2.ogg", 171.5f);
    }
}
