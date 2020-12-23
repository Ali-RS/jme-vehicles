package com.jayfella.jme.vehicle.gui;

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
    protected Button[] createItems() {
        AppStateManager stateManager = getStateManager();

        Button basicAlloyButton = new Button("Basic Alloy");
        basicAlloyButton.addClickCommands(source
                -> setModel(BasicAlloyWheel.class));

        Button buggyFrontButton = new Button("Buggy Front");
        buggyFrontButton.addClickCommands(source
                -> setModel(BuggyFrontWheel.class));

        Button buggyRearButton = new Button("Buggy Rear");
        buggyRearButton.addClickCommands(source
                -> setModel(BuggyRearWheel.class));

        Button cruiserButton = new Button("Cruiser");
        cruiserButton.addClickCommands(source
                -> setModel(CruiserWheel.class));

        Button darkAlloyButton = new Button("Dark Alloy");
        darkAlloyButton.addClickCommands(source
                -> setModel(DarkAlloyWheel.class));

        Button hatchbackButton = new Button("Hatchback");
        hatchbackButton.addClickCommands(source
                -> setModel(HatchbackWheel.class));

        Button rangerButton = new Button("Ranger");
        rangerButton.addClickCommands(source
                -> setModel(RangerWheel.class));

        Button backButton = new Button("<< Back");
        backButton.addClickCommands(source -> {
            stateManager.attach(new MainMenu());
            stateManager.detach(this);
        });

        Button[] result = new Button[]{
            basicAlloyButton,
            buggyFrontButton,
            buggyRearButton,
            cruiserButton,
            darkAlloyButton,
            hatchbackButton,
            rangerButton,
            backButton
        };
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
