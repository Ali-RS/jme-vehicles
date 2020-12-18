package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.examples.cars.DuneBuggy;
import com.jayfella.jme.vehicle.examples.cars.GTRNismo;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.examples.cars.HatchBack;
import com.jayfella.jme.vehicle.examples.cars.PickupTruck;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import java.util.logging.Logger;

/**
 * An animated menu to choose among the available vehicles.
 */
class CarSelectorMenuState extends AnimatedMenuState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CarSelectorMenuState.class.getName());
    // *************************************************************************
    // fields

    private final Node scene;
    // *************************************************************************
    // constructors

    public CarSelectorMenuState(Node scene) {
        this.scene = scene;
    }
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
            stateManager.attach(new MainMenuState());
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
    }
}
