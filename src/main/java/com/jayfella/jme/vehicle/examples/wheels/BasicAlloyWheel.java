package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class BasicAlloyWheel extends WheelModel {

    public BasicAlloyWheel(float size) {

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Spatial wheelSpatial = assetManager.loadModel("Models/Vehicles/Wheel/Wheel_1/wheel.j3o");
        wheelSpatial.setMaterial(assetManager.loadMaterial("Materials/Vehicles/Wheel_1.j3m"));
        // wheelSpatial.setLocalScale(0.75f); // this makes it 1.0 wu in radius
        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(size);
    }

}
