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
import java.util.ArrayList;
import java.util.List;
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
    protected List<Button> createItems() {
        AppStateManager stateManager = getStateManager();
        List<Button> result = new ArrayList<>(6);

        Button button = new Button("Grand Tourer");
        button.addClickCommands(source -> setVehicle(new GrandTourer()));
        result.add(button);

        button = new Button("GTR Nismo");
        button.addClickCommands(source -> setVehicle(new GTRNismo()));
        result.add(button);

        button = new Button("Pickup Truck");
        button.addClickCommands(source -> setVehicle(new PickupTruck()));
        result.add(button);

        button = new Button("Hatchback");
        button.addClickCommands(source -> setVehicle(new HatchBack()));
        result.add(button);

        button = new Button("Dune Buggy");
        button.addClickCommands(source -> setVehicle(new DuneBuggy()));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> {
            stateManager.attach(new MainMenu());
            stateManager.detach(this);
        });
        result.add(button);

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
