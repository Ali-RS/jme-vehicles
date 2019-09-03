package com.jayfella.jme.vehicle.view;

import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;

public class VehicleFirstPersonCamera implements VehicleCamera {

    private final Vehicle vehicle;
    //private final Camera camera;

    private final CameraNode cameraNode;

    public VehicleFirstPersonCamera(Vehicle vehicle, Camera camera) {

        this.vehicle = vehicle;
        //this.camera = camera;

        this.cameraNode = new CameraNode("Vehicle Camera Node", camera);

    }

    @Override
    public void enableInputMappings() {

    }

    @Override
    public void disableInputMappings() {

    }

    @Override
    public void attach() {

        cameraNode.setLocalTranslation(vehicle.getHoodCamLocation());
        vehicle.getNode().attachChild(cameraNode);

        enableInputMappings();
    }

    @Override
    public void detach() {
        cameraNode.removeFromParent();
        disableInputMappings();
    }

    @Override
    public void update(float tpf) {

    }


}
