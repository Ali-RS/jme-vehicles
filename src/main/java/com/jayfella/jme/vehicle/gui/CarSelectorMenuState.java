package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.examples.cars.DuneBuggy;
import com.jayfella.jme.vehicle.examples.cars.GTRNismo;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.examples.cars.HatchBack;
import com.jayfella.jme.vehicle.examples.cars.PickupTruck;
import com.jayfella.jme.vehicle.input.KeyboardVehicleInputState;
import com.jme3.app.Application;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;

public class CarSelectorMenuState extends AnimatedMenuState {

    private final Node scene;
    private final PhysicsSpace physicsSpace;

    public CarSelectorMenuState(Node scene, PhysicsSpace physicsSpace) {
        this.scene = scene;
        this.physicsSpace = physicsSpace;
    }

    @Override
    protected Button[] createItems() {
        Button[] buttons = new Button[]{
            new Button("Grand Tourer"),
            new Button("GTR Nismo"),
            new Button("Pickup Truck"),
            new Button("Hatchback"),
            new Button("Dune Buggy"),
            new Button("<< Back")
        };

        Application app = getApplication();
        buttons[0].addClickCommands(source -> setVehicle(new GrandTourer(app)));
        buttons[1].addClickCommands(source -> setVehicle(new GTRNismo(app)));
        buttons[2].addClickCommands(source -> setVehicle(new PickupTruck(app)));
        buttons[3].addClickCommands(source -> setVehicle(new HatchBack(app)));
        buttons[4].addClickCommands(source -> setVehicle(new DuneBuggy(app)));

        buttons[5].addClickCommands(source -> {
            animateOut(() -> {
                getStateManager().attach(new MainMenuState());
                getStateManager().detach(this);
            });
        });

        return buttons;
    }

    private void addVehicle(Car vehicle) {
        DriverHud hud = getStateManager().getState(DriverHud.class);
        hud.setCar(vehicle);
        hud.setEnabled(true);
        vehicle.attachToScene(scene, physicsSpace);

        vehicle.getVehicleControl().setPhysicsLocation(new Vector3f(0, 6, 0));

        // add some controls
        KeyboardVehicleInputState inputState = new KeyboardVehicleInputState(vehicle);
        // XBoxJoystickVehicleInputState inputState = new XBoxJoystickVehicleInputState(vehicle);
        getStateManager().attach(inputState);

        // engine graph GUI for viewing torque/power @ revs
        EnginePowerGraphState enginePowerGraphState = new EnginePowerGraphState(vehicle);
        enginePowerGraphState.setEnabled(false);
        getStateManager().attach(enginePowerGraphState);

        // tire data GUI for viewing how much grip each tire has according to the Pacejka formula
        TireDataState tireDataState = new TireDataState(vehicle);
        tireDataState.setEnabled(false);
        getStateManager().attach(tireDataState);

        // the main vehicle editor to modify aspects of the vehicle in real time
        VehicleEditorState vehicleEditorState = new VehicleEditorState(vehicle);
        getStateManager().attach(vehicleEditorState);

        // vehicle debug add-on to enable/disable debug screens
        DebugTabState debugTabState = new DebugTabState();
        getStateManager().attach(debugTabState);

        vehicle.getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
    }

    private void setVehicle(Car newVehicle) {
        addVehicle(newVehicle);
        getStateManager().detach(this);
    }
}
