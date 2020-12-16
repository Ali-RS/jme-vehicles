package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class DarkAlloyWheel extends WheelModel {

    public DarkAlloyWheel(AssetManager assetManager, float size) {

        Spatial wheelSpatial = assetManager.loadModel("Models/gtr_nismo/dark_alloy.gltf.j3o");

        setSpatial(wheelSpatial);
        getWheelNode().setLocalScale(size);
    }

}
