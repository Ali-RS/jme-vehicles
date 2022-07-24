package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.Gear;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class GearboxEditor extends Container {
    // *************************************************************************
    // fields

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public GearboxEditor(Vehicle vehicle) {
        this.vehicle = vehicle;
        addChild(createGearBoxRollup());
    }
    // *************************************************************************
    // private methods

    private RollupPanel createGearBoxRollup() {

        PropertyPanel propertyPanel = new PropertyPanel("glass");

        for (int i = 1; i <= vehicle.getGearBox().countForwardGears(); ++i) {
            Gear gear = vehicle.getGearBox().getGear(i);
            propertyPanel.addFloatProperty("Gear Start " + i, gear, "minKph",
                    0f, 250f, 0.1f);
            propertyPanel.addFloatProperty("Gear End " + i, gear, "maxKph",
                    0f, 250f, 0.1f);
        }

        return new RollupPanel("Ratios", propertyPanel, "glass");
    }
}
