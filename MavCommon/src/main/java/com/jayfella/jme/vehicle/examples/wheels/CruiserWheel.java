package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel built around a wheel from Thomas Glenn Thorne's "Opel GT Retopo"
 * model.
 */
public class CruiserWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(CruiserWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public CruiserWheel(float diameter) {
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
        String assetPath = "Models/GT/cruiser_wheel.gltf.j3o";
        Spatial wheelSpatial = assetManager.loadModel(assetPath);
        setSpatial(wheelSpatial);
    }
}
