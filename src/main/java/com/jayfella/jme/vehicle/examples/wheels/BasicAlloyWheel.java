package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class BasicAlloyWheel extends WheelModel {

    public BasicAlloyWheel(AssetManager assetManager, float size) {

        Spatial wheelSpatial = assetManager.loadModel("Models/Vehicles/Wheel/Wheel_1/wheel.j3o");
        wheelSpatial.setMaterial(assetManager.loadMaterial("Materials/Vehicles/Wheel_1.j3m"));
        // wheelSpatial.setLocalScale(0.75f); // this makes it 1.0 wu in radius
        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(size);
    }

}
