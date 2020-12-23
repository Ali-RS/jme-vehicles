package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class BasicAlloyWheel extends WheelModel {
    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public BasicAlloyWheel(float diameter) {
        super(diameter);

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "Models/Vehicles/Wheel/Wheel_1/wheel.j3o";
        Spatial wheelSpatial = assetManager.loadModel(assetPath);
        super.setSpatial(wheelSpatial);

        String materialAssetPath = "Materials/Vehicles/Wheel_1.j3m";
        wheelSpatial.setMaterial(assetManager.loadMaterial(materialAssetPath));
    }
}
