package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available speedometers.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SpeedometerMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SpeedometerMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(5);

        addButton(result, "Kilometers per hour",
                source -> setUnits(SpeedUnit.KPH));
        addButton(result, "Miles per hour",
                source -> setUnits(SpeedUnit.MPH));
        addButton(result, "World units per second",
                source -> setUnits(SpeedUnit.WUPS));
        addButton(result, "None",
                source -> setUnits(null));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new CustomizationMenu())));

        return result;
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        removeOldSpeedometer();
        super.onDisable();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();
        addNewSpeedometer();
    }
    // *************************************************************************
    // private methods

    private void addNewSpeedometer() {
        Vehicle vehicle = MavDemo1.getVehicle();
        SpeedUnit units = vehicle.getSpeedometerUnits();
        if (units != null) {
            SpeedometerState speedometer = new SpeedometerState(vehicle, units);
            getStateManager().attach(speedometer);
        }
    }

    private void removeOldSpeedometer() {
        AppStateManager stateManager = getStateManager();
        SpeedometerState old = getState(SpeedometerState.class);
        if (old != null) {
            stateManager.detach(old);
        }
    }

    private void setUnits(SpeedUnit units) {
        removeOldSpeedometer();
        MavDemo1.getVehicle().setSpeedometerUnits(units);
        addNewSpeedometer();
    }
}
