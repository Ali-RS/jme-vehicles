package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

/**
 * Encapsulate a single wheel from Daniel Zhabotinsky's "Modern Hatchback - Low
 * Poly" model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HatchbackWheel extends WheelModel {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a wheel with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public HatchbackWheel(float diameter) {
        super(diameter);

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "/Models/modern_hatchback/wheel.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);
        super.setSpatial(cgmRoot);
        super.getWheelNode().setLocalScale(diameter);
    }
}
