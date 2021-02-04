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
     * Instantiate the Sound.
     */
    public EngineSound1() {
        super.addAssetPath("/Audio/engine-1d2.ogg", 13.4f);
        super.addAssetPath("/Audio/engine-1.ogg", 26.75f);
        super.addAssetPath("/Audio/engine-1x2.ogg", 53.5f);
        super.addAssetPath("/Audio/engine-1x4.ogg", 107f);
    }
}
