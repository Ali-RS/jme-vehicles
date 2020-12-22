package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class BasicAlloyWheel extends WheelModel {

    public BasicAlloyWheel(float size) {

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "Models/Vehicles/Wheel/Wheel_1/wheel.j3o";
        Spatial wheelSpatial = assetManager.loadModel(assetPath);
        String materialAssetPath = "Materials/Vehicles/Wheel_1.j3m";
        wheelSpatial.setMaterial(assetManager.loadMaterial(materialAssetPath));
        // wheelSpatial.setLocalScale(0.75f); // this makes it 1.0 wu in radius
        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(size);
    }

}
