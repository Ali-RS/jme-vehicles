package com.jayfella.jme.vehicle;

/**
 * An object that contains loadable assets.
 * 
 * @author Stephen Gold sgold@sonic.net
 */
public interface Loadable {
    // *************************************************************************
    // new methods exposed

    /**
     * Load the assets of this Loadable without attaching them to any scene.
     */
    void load();
}
