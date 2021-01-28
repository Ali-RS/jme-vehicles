package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.lemurdemo.Main;
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

        Button button = new Button("Kilometers per hour");
        button.addClickCommands(source -> setUnits(SpeedUnit.KPH));
        result.add(button);

        button = new Button("Miles per hour");
        button.addClickCommands(source -> setUnits(SpeedUnit.MPH));
        result.add(button);

        button = new Button("World units per second");
        button.addClickCommands(source -> setUnits(SpeedUnit.WUPS));
        result.add(button);

        button = new Button("None");
        button.addClickCommands(source -> setUnits(null));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new CustomizationMenu())
        ));
        result.add(button);

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
        Vehicle vehicle = Main.getVehicle();
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
        Main.getVehicle().setSpeedometerUnits(units);
        addNewSpeedometer();
    }
}
