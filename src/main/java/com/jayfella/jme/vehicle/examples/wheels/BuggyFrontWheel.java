package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import jme3utilities.Validate;

/**
 * Encapsulate the left front wheel from oakar258's "HCR2 Buggy" model.
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
        String assetPath = "/Models/hcr2_buggy/front-wheel.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);
        super.setSpatial(cgmRoot);
        super.getWheelNode().setLocalScale(diameter);
    }
}
