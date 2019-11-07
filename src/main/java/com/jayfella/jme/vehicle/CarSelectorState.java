package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TyreDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.examples.cars.*;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;

public class CarSelectorState extends BaseAppState {

    private final Node scene;
    private final PhysicsSpace physicsSpace;

    private Container container;
    private Car vehicle = null;

    public CarSelectorState(Node scene, PhysicsSpace physicsSpace) {
        this.scene = scene;
        this.physicsSpace = physicsSpace;
    }

    @Override
    protected void initialize(Application app) {

        this.container = new Container();

        Button gtTourer = container.addChild(new Button("Grand Tourer"));
        gtTourer.addClickCommands(source -> setVehicle(new GrandTourer(getApplication())));

        Button gtrNismo = container.addChild(new Button("GTR Nismo"));
        gtrNismo.addClickCommands(source -> setVehicle(new GTRNismo(getApplication())));

        Button pickup = container.addChild(new Button("Pickup Truck"));
        pickup.addClickCommands(source -> setVehicle(new PickupTruck(getApplication())));

        Button hatchback = container.addChild(new Button("Hatchback"));
        hatchback.addClickCommands(source -> setVehicle(new HatchBack(getApplication())));

        Button dunebuggy = container.addChild(new Button("Dune Buggy"));
        dunebuggy.addClickCommands(source -> setVehicle(new DuneBuggy(getApplication())));

        container.setLocalTranslation(
                (getApplication().getCamera().getWidth() * 0.5f) - (container.getPreferredSize().x * 0.5f),
                getApplication().getCamera().getHeight() - 10,
                1);
    }

    public void setVehicle(Car newVehicle) {

        if (vehicle != null) {

            BasicVehicleInputState basicVehicleInputState = getState(BasicVehicleInputState.class);
            if (basicVehicleInputState != null) {
                getStateManager().detach(basicVehicleInputState);
            }

            EnginePowerGraphState enginePowerGraphState = getState(EnginePowerGraphState.class);
            if (enginePowerGraphState != null) {
                getStateManager().detach(enginePowerGraphState);
            }

            TyreDataState tyreDataState = getState(TyreDataState.class);
            if (tyreDataState != null) {
                getStateManager().detach(tyreDataState);
            }

            VehicleEditorState vehicleEditorState = getState(VehicleEditorState.class);
            if (vehicleEditorState != null) {
                getStateManager().detach(vehicleEditorState);
            }

            DebugTabState debugTabState = getState(DebugTabState.class);
            if (debugTabState != null) {
                getStateManager().detach(debugTabState);
            }

            vehicle.removeTacho();
            vehicle.removeSpeedo();
            vehicle.detachFromScene();
        }

        vehicle = newVehicle;

        vehicle.showSpeedo(Vehicle.SpeedUnit.MPH);
        vehicle.showTacho();
        vehicle.attachToScene(scene, physicsSpace);

        vehicle.getVehicleControl().setPhysicsLocation(new Vector3f(0, 6, 0));

        // add some controls
        BasicVehicleInputState basicVehicleInputState = new BasicVehicleInputState(vehicle);
        getStateManager().attach(basicVehicleInputState);

        // engine debugger
        EnginePowerGraphState enginePowerGraphState = new EnginePowerGraphState(vehicle);
        enginePowerGraphState.setEnabled(false);
        getStateManager().attach(enginePowerGraphState);

        // tyre debugger
        TyreDataState tyreDataState = new TyreDataState(vehicle);
        tyreDataState.setEnabled(false);
        getStateManager().attach(tyreDataState);

        // the vehicle debug.
        VehicleEditorState vehicleEditorState = new VehicleEditorState(vehicle);
        getStateManager().attach(vehicleEditorState);

        // vehicle debug add-on to enable/disable debug screens.
        DebugTabState debugTabState = new DebugTabState();
        getStateManager().attach(debugTabState);

        MagicFormulaState magicFormulaState = new MagicFormulaState(vehicle);
        getStateManager().attach(magicFormulaState);

        vehicle.getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        setShowVehicleSelector(true);
    }

    @Override
    protected void onDisable() {
        setShowVehicleSelector(false);
    }

    public boolean getShowVehicleSelector() {
        return container.getParent() != null;
    }

    public void setShowVehicleSelector(boolean show) {

        if (show) {
            ((SimpleApplication)getApplication()).getGuiNode().attachChild(container);
        }
        else {
            container.removeFromParent();
        }
    }

}
