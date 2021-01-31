package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.GearBox;
import jme3utilities.Validate;

/**
 * A CartoucheState to display the name of the engaged gear.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class GearNameState extends CartoucheState {
    // *************************************************************************
    // fields

    /**
     * GearBox of the selected Vehicle
     */
    private GearBox gearBox;
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
    // new methods exposed

    /**
     * Alter which Vehicle is associated with this indicator.
     *
     * @param newVehicle the Vehicle to associate (not null)
     */
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");
        this.gearBox = newVehicle.getGearBox();
    }
    // *************************************************************************
    // CartoucheState methods

    /**
     * Compare the enaged gear name to the displayed text and repopulate the
     * Node if they differ.
     */
    @Override
    protected void updateNode() {
        String gearName;
        if (gearBox == null) {
            gearName = "";
        } else {
            Gear activeGear = gearBox.getEngagedGear();
            gearName = activeGear.name();
        }

        displayText(gearName);
    }
}
