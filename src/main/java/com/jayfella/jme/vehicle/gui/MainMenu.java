package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.input.KeyboardVehicleInputState;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
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
    protected Button[] createItems() {
        Main application = Main.getApplication();
        AppStateManager stateManager = getStateManager();

        Button envButton = new Button("Change Environment");
        envButton.addClickCommands(source -> {
            stateManager.attach(new EnvironmentMenu());
            stateManager.detach(this);
        });

        Button vehicleButton = new Button("Change Vehicle");
        vehicleButton.addClickCommands(source -> {
            stateManager.attach(new CarMenu());
            stateManager.detach(this);
        });

        Button driveButton = new Button("Drive");
        driveButton.addClickCommands(source -> drive());

        Button exitButton = new Button("Exit Game");
        exitButton.addClickCommands(source -> application.stop());

        Button[] result = new Button[]{
            envButton,
            vehicleButton,
            driveButton,
            exitButton
        };
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
        DriverHud hud = Main.findAppState(DriverHud.class);
        hud.setCar(vehicle);
        hud.setEnabled(true);

        // handle keyboard/mouse inputs
        KeyboardVehicleInputState inputState = new KeyboardVehicleInputState(vehicle);
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
