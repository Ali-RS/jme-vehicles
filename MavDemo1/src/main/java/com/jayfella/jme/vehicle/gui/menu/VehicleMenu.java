package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.vehicles.DuneBuggy;
import com.jayfella.jme.vehicle.examples.vehicles.GTRNismo;
import com.jayfella.jme.vehicle.examples.vehicles.GrandTourer;
import com.jayfella.jme.vehicle.examples.vehicles.HatchBack;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.vehicles.PickupTruck;
import com.jayfella.jme.vehicle.examples.vehicles.Rotator;
import com.jayfella.jme.vehicle.input.CameraInputMode;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.asset.AssetManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available vehicles.
 *
 * Derived from the CarSelectorState class in the Advanced Vehicles project.
 */
class VehicleMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(VehicleMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(8);

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

        button = new Button("Hover Tank");
        button.addClickCommands(source -> setVehicle(new HoverTank()));
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
        MavDemo1 main = MavDemo1.getApplication();
        AssetManager assetManager = main.getAssetManager();
        vehicle.load(assetManager);
        main.setVehicle(vehicle);

        getState(CameraInputMode.class).setVehicle(vehicle);
    }
}
