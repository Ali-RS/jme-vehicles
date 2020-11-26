package com.jayfella.jme.vehicle.view;

import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;

public class DashCamera implements VehicleCamera {

    private final Vehicle vehicle;
    //private final Camera camera;

    private final CameraNode cameraNode;

    public DashCamera(Vehicle vehicle, Camera camera) {

        this.vehicle = vehicle;
        //this.camera = camera;

        this.cameraNode = new CameraNode("Vehicle Camera Node", camera);

    }

    @Override
    public void attach() {
        cameraNode.setLocalTranslation(vehicle.getHoodCamLocation(null));
        vehicle.getNode().attachChild(cameraNode);
    }

    @Override
    public void detach() {
        cameraNode.removeFromParent();
    }

    @Override
    public void update(float tpf) {

    }


}
