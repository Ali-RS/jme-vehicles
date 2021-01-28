package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.GearBox;

/**
 * A CartoucheState to display the name of the engaged gear.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class GearNameState extends CartoucheState {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled AppState.
     */
    public GearNameState() {
        super("Gear Name", 0.821f, 0.04f);
        setEnabled(false);
    }
    // *************************************************************************
    // CartoucheState methods

    /**
     * Compare the enaged gear name to the displayed text and repopulate the
     * Node if they differ.
     */
    @Override
    protected void updateNode() {
        GearBox gearBox = Main.getVehicle().getGearBox();
        Gear activeGear = gearBox.getEngagedGear();
        String gearName = activeGear.name();
        displayText(gearName);
    }
}
