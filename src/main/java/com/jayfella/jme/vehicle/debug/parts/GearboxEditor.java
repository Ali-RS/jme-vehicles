package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Vehicle;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class GearboxEditor extends VehicleEditor {

    final private Vehicle vehicle;

    public GearboxEditor(Vehicle vehicle) {
        this.vehicle = vehicle;
        addChild(createGearBoxRollup());
    }

    private RollupPanel createGearBoxRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 0; i < vehicle.getGearBox().getGearCount(); i++) {

            // propertyPanel.addFloatProperty("Wheel " + i, vehicle.getWheel(i).getBrake(), "strength", 0, 200, 0.1f);
            propertyPanel.addFloatProperty("Gear Start " + i, vehicle.getGearBox().getGear(i), "start", 0, 250, 0.1f);
            propertyPanel.addFloatProperty("Gear End " + i, vehicle.getGearBox().getGear(i), "end", 0, 250, 0.1f);

        }

        return new RollupPanel("Ratios", propertyPanel, "glass");
    }

    @Override
    public void update(float tpf) {

    }

}
