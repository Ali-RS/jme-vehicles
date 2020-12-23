package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class CruiserWheel extends WheelModel {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public CruiserWheel(float diameter) {
        super(diameter);

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "Models/GT/cruiser_wheel.gltf.j3o";
        Spatial wheelSpatial = assetManager.loadModel(assetPath);

        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(diameter);
    }
}
