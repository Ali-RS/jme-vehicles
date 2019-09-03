package com.jayfella.jme.vehicle.view;

public interface VehicleCamera {

    void enableInputMappings();
    void disableInputMappings();

    void attach();
    void detach();

    void update(float tpf);

}
