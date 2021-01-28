package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel built around a wheel from iSteven's "Nissan GT-R" model.
 */
public class DarkAlloyWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(DarkAlloyWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public DarkAlloyWheel(float diameter) {
        super(diameter);
    }
    // *************************************************************************
    // WheelModel methods

    /**
     * Load this WheelModel from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        String assetPath = "Models/gtr_nismo/dark_alloy.gltf.j3o";
        Spatial wheelSpatial = assetManager.loadModel(assetPath);
        setSpatial(wheelSpatial);
    }
}
