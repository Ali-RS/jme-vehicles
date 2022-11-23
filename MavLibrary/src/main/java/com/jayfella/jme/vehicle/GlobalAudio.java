package com.jayfella.jme.vehicle;

/**
 * Interface to the global audio controls.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface GlobalAudio {
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the effective global audio volume.
     *
     * @return the volume (linear scale, &ge;0, &le;1)
     */
    float effectiveVolume();
}
