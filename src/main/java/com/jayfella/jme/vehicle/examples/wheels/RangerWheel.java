package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import jme3utilities.Validate;

/**
 * A single wheel from mauro.zampaoli's "Ford Ranger" model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class RangerWheel extends WheelModel {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a wheel with the specified diameter.
     *
     * @param diameter (in world units, &gt;0)
     */
    public RangerWheel(float diameter) {
        Validate.positive(diameter, "diameter");

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Spatial cgmRoot
                = assetManager.loadModel("/Models/ford_ranger/wheel.j3o");
        super.setSpatial(cgmRoot);
        super.getWheelNode().setLocalScale(diameter);
    }
}
