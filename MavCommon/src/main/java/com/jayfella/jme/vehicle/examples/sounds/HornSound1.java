package com.jayfella.jme.vehicle.examples.sounds;

import com.jayfella.jme.vehicle.Sound;

/**
 * A Sound built around "horn-1.ogg".
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HornSound1 extends Sound {
    // *************************************************************************
    // constructors

    /**
     * Instantiate the Sound.
     */
    public HornSound1() {
        super.addAssetPath("/Audio/horn-1.ogg", 823f);
    }
}
