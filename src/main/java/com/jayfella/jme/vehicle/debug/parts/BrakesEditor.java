package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Car;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class BrakesEditor extends VehicleEditor {

    private final Car vehicle;

    public BrakesEditor(Car vehicle) {
        super();

        this.vehicle = vehicle;

        addChild(createBrakesRollup());
    }

    private RollupPanel createBrakesRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.getNumWheels(); i++) {
            propertyPanel.addFloatProperty("Wheel " + i, vehicle.getWheel(i).getBrake(), "strength", 0, 1000, 0.1f);
        }

        return new RollupPanel("Strength", propertyPanel, "glass");
    }


    @Override
    public void update(float tpf) {
    }
}
