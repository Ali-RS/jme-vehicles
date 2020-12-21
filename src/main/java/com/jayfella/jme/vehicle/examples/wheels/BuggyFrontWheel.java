package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import jme3utilities.Validate;

/**
 * A WheelModel derived from the left front wheel of oakar258's "HCR2 Buggy"
 * model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class BuggyFrontWheel extends WheelModel {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a wheel with the specified diameter.
     *
     * @param diameter the desired diameter (in world units, &gt;0)
     */
    public BuggyFrontWheel(float diameter) {
        Validate.positive(diameter, "diameter");

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Spatial cgmRoot
                = assetManager.loadModel("/Models/hcr2_buggy/front-wheel.j3o");
        super.setSpatial(cgmRoot);
        super.getWheelNode().setLocalScale(diameter);
    }
}
