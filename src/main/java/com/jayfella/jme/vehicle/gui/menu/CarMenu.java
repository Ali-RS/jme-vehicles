package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.cars.DuneBuggy;
import com.jayfella.jme.vehicle.examples.cars.GTRNismo;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.examples.cars.HatchBack;
import com.jayfella.jme.vehicle.examples.cars.PickupTruck;
import com.jayfella.jme.vehicle.examples.cars.Rotator;
import com.jayfella.jme.vehicle.input.CameraInputMode;
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
        List<Button> result = new ArrayList<>(7);

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

        button = new Button("Rotator");
        button.addClickCommands(source -> setVehicle(new Rotator()));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new MainMenu())
        ));
        result.add(button);

        return result;
    }
    // *************************************************************************
    // private methods

    private void setVehicle(Vehicle vehicle) {
        vehicle.load();
        Main.getApplication().setVehicle(vehicle);

        getState(CameraInputMode.class).setVehicle(vehicle);
    }
}
