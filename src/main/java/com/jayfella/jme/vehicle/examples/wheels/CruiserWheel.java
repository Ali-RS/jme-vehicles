package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class CruiserWheel extends WheelModel {

    public CruiserWheel(AssetManager assetManager, float size) {

        Spatial wheelSpatial = assetManager.loadModel("Models/cruiser_wheel/cruiser_wheel.gltf.j3o");

        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(size);
    }

}
