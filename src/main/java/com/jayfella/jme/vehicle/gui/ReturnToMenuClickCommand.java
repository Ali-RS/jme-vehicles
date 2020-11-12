package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TyreDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.input.KeyboardVehicleInputState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

public class ReturnToMenuClickCommand implements Command<Button> {

    private final Car vehicle;

    public ReturnToMenuClickCommand(Car vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public void execute(Button source) {
        returnToMenu(vehicle);
    }

    public static void returnToMenu(Vehicle v) {
        AppStateManager stateManager = v.getApplication().getStateManager();

        KeyboardVehicleInputState inputState = stateManager.getState(KeyboardVehicleInputState.class);
        // XBoxJoystickVehicleInputState inputState = getState(XBoxJoystickVehicleInputState.class);
        if (inputState != null) {
            stateManager.detach(inputState);
        }

        EnginePowerGraphState enginePowerGraphState = stateManager.getState(EnginePowerGraphState.class);
        if (enginePowerGraphState != null) {
            stateManager.detach(enginePowerGraphState);
        }

        TyreDataState tyreDataState = stateManager.getState(TyreDataState.class);
        if (tyreDataState != null) {
            stateManager.detach(tyreDataState);
        }

        VehicleEditorState vehicleEditorState = stateManager.getState(VehicleEditorState.class);
        if (vehicleEditorState != null) {
            stateManager.detach(vehicleEditorState);
        }

        DebugTabState debugTabState = stateManager.getState(DebugTabState.class);
        if (debugTabState != null) {
            stateManager.detach(debugTabState);
        }

        v.removeTacho();
        v.removeSpeedo();
        v.detachFromScene();

        stateManager.attach(new MainMenuState());
        v.removeRtmmButton();

        Camera camera = v.getApplication().getCamera();
        camera.setLocation(new Vector3f(-200f, 50f, -200f));
        camera.lookAt(new Vector3f(100f, 10f, 150f), Vector3f.UNIT_Y);
    }
}
