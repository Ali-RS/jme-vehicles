package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TyreDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.input.KeyboardVehicleInputState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;

public class ReturnToMenuClickCommand implements Command<Button> {

    private final Car vehicle;

    public ReturnToMenuClickCommand(Car vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public void execute(Button source) {

        AppStateManager stateManager = vehicle.getApplication().getStateManager();

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

        vehicle.removeTacho();
        vehicle.removeSpeedo();
        vehicle.detachFromScene();

        stateManager.attach(new MainMenuState());
        vehicle.removeRtmmButton();

        vehicle.getApplication().getCamera().setLocation(new Vector3f(-200, 50, -200));
        vehicle.getApplication().getCamera().lookAt(new Vector3f(100, 10, 150), Vector3f.UNIT_Y);
    }
}
