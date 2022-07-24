package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.WheelModel;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel built around the "Wheel_1" C-G model.
 */
public class BasicAlloyWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(BasicAlloyWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public BasicAlloyWheel(float diameter) {
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
        String assetPath = "Models/Vehicles/Wheel/Wheel_1/wheel.j3o";
        Spatial wheelSpatial = assetManager.loadModel(assetPath);
        super.setSpatial(wheelSpatial);

        String materialAssetPath = "Materials/Vehicles/Wheel_1.j3m";
        wheelSpatial.setMaterial(assetManager.loadMaterial(materialAssetPath));
    }
}
