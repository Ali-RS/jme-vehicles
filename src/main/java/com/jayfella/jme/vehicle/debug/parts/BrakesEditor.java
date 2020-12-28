package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.part.Brake;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class BrakesEditor extends VehicleEditor {

    final private Car vehicle;

    public BrakesEditor(Car vehicle) {
        super();

        this.vehicle = vehicle;

        addChild(createBrakesRollup());
    }

    private RollupPanel createBrakesRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.getNumWheels(); i++) {
            Brake mainBrake = vehicle.getWheel(i).getMainBrake();
            propertyPanel.addFloatProperty("Wheel " + i, mainBrake, "peakForce",
                    0f, 1_000f, 0.1f);
        }

        return new RollupPanel("Strength", propertyPanel, "glass");
    }


    @Override
    public void update(float tpf) {
    }
}
