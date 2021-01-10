package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.examples.wheels.BasicAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.BuggyFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.BuggyRearWheel;
import com.jayfella.jme.vehicle.examples.wheels.CruiserWheel;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.HatchbackWheel;
import com.jayfella.jme.vehicle.examples.wheels.RangerWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available wheel models.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class WheelMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(WheelMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        AppStateManager stateManager = getStateManager();
        List<Button> result = new ArrayList<>(8);

        Button button = new Button("Basic Alloy");
        button.addClickCommands(source -> setModel(BasicAlloyWheel.class));
        result.add(button);

        button = new Button("Buggy Front");
        button.addClickCommands(source -> setModel(BuggyFrontWheel.class));
        result.add(button);

        button = new Button("Buggy Rear");
        button.addClickCommands(source -> setModel(BuggyRearWheel.class));
        result.add(button);

        button = new Button("Cruiser");
        button.addClickCommands(source -> setModel(CruiserWheel.class));
        result.add(button);

        button = new Button("Dark Alloy");
        button.addClickCommands(source -> setModel(DarkAlloyWheel.class));
        result.add(button);

        button = new Button("Hatchback");
        button.addClickCommands(source -> setModel(HatchbackWheel.class));
        result.add(button);

        button = new Button("Ranger");
        button.addClickCommands(source -> setModel(RangerWheel.class));
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

    private void setModel(Class<? extends WheelModel> wheelModelClass) {
        Car vehicle = (Car) Main.getVehicle();
        int numWheels = vehicle.getVehicleControl().getNumWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            vehicle.setWheelModel(wheelIndex, wheelModelClass);
        }
    }
}
