package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.examples.cars.DuneBuggy;
import com.jayfella.jme.vehicle.examples.cars.GTRNismo;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.examples.cars.HatchBack;
import com.jayfella.jme.vehicle.examples.cars.PickupTruck;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available vehicles.
 */
class CarMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CarMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    @Override
    protected Button[] createItems() {
        AppStateManager stateManager = getStateManager();

        Button gtButton = new Button("Grand Tourer");
        gtButton.addClickCommands(source -> setVehicle(new GrandTourer()));

        Button nismoButton = new Button("GTR Nismo");
        nismoButton.addClickCommands(source -> setVehicle(new GTRNismo()));

        Button pickupButton = new Button("Pickup Truck");
        pickupButton.addClickCommands(source -> setVehicle(new PickupTruck()));

        Button hatchbackButton = new Button("Hatchback");
        hatchbackButton.addClickCommands(source -> setVehicle(new HatchBack()));

        Button buggyButton = new Button("Dune Buggy");
        buggyButton.addClickCommands(source -> setVehicle(new DuneBuggy()));

        Button backButton = new Button("<< Back");
        backButton.addClickCommands(source -> {
            stateManager.attach(new MainMenu());
            stateManager.detach(this);
        });

        Button[] result = new Button[]{
            gtButton,
            nismoButton,
            pickupButton,
            hatchbackButton,
            buggyButton,
            backButton
        };
        return result;
    }
    // *************************************************************************
    // private methods

    private void setVehicle(Car vehicle) {
        vehicle.load();
        Main.getApplication().setVehicle(vehicle);

        NonDrivingInputState orbitCam
                = Main.findAppState(NonDrivingInputState.class);
        orbitCam.setVehicle(vehicle);
    }
}
