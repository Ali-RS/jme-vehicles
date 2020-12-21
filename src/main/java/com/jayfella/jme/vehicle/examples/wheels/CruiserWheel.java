package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class CruiserWheel extends WheelModel {

    public CruiserWheel(float size) {

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Spatial wheelSpatial = assetManager.loadModel("Models/GT/cruiser_wheel.gltf.j3o");

        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(size);
    }

}
