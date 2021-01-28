package com.jayfella.jme.vehicle.debug.parts;

import com.jayfella.jme.vehicle.Vehicle;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class ChassisEditor extends Container {
    // *************************************************************************
    // fields

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public ChassisEditor(Vehicle vehicle) {
        super();

        this.vehicle = vehicle;
        addChild(createWeightRollup());
    }
    // *************************************************************************
    // private methods

    private RollupPanel createWeightRollup() {
        PropertyPanel propertyPanel = new PropertyPanel("glass");
        propertyPanel.addFloatProperty("Weight", vehicle.getVehicleControl(), "mass", 1, 5000, 0.1f);

        return new RollupPanel("Weight", propertyPanel, "glass");
    }
}
