package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.input.DrivingInputState;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MainMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        Main application = Main.getApplication();
        AppStateManager stateManager = getStateManager();
        List<Button> result = new ArrayList<>(5);

        Button button = new Button("Change Environment");
        button.addClickCommands(source -> {
            stateManager.attach(new EnvironmentMenu());
            stateManager.detach(this);
        });
        result.add(button);

        button = new Button("Change Vehicle");
        button.addClickCommands(source -> {
            stateManager.attach(new CarMenu());
            stateManager.detach(this);
        });
        result.add(button);

        button = new Button("Change Wheels");
        button.addClickCommands(source -> {
            stateManager.attach(new WheelMenu());
            stateManager.detach(this);
        });
        result.add(button);

        button = new Button("Drive");
        button.addClickCommands(source -> drive());
        result.add(button);

        button = new Button("Exit Game");
        button.addClickCommands(source -> application.stop());
        result.add(button);

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Drive the selected vehicle in the selected environment.
     */
    private void drive() {
        AppStateManager stateManager = getStateManager();
        NonDrivingInputState orbit
                = stateManager.getState(NonDrivingInputState.class);
        stateManager.detach(orbit);

        Car vehicle = (Car) Main.getVehicle();
        vehicle.getEngine().setRunning(true);
        DriverHud hud = Main.findAppState(DriverHud.class);
        hud.setCar(vehicle);
        hud.setEnabled(true);

        // handle keyboard/mouse inputs
        DrivingInputState inputState = new DrivingInputState(vehicle);
        stateManager.attach(inputState);

        // engine graph GUI for viewing torque/power @ revs
        EnginePowerGraphState enginePowerGraphState = new EnginePowerGraphState(vehicle);
        enginePowerGraphState.setEnabled(false);
        stateManager.attach(enginePowerGraphState);

        // tire data GUI for viewing how much grip each tire has according to the Pacejka formula
        TireDataState tireDataState = new TireDataState(vehicle);
        tireDataState.setEnabled(false);
        stateManager.attach(tireDataState);

        // the main vehicle editor to modify aspects of the vehicle in real time
        VehicleEditorState vehicleEditorState = new VehicleEditorState(vehicle);
        stateManager.attach(vehicleEditorState);

        // vehicle debug add-on to enable/disable debug screens
        DebugTabState debugTabState = new DebugTabState();
        stateManager.attach(debugTabState);

        stateManager.detach(this);
    }
}
