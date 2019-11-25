package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TyreDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.examples.cars.*;
import com.jayfella.jme.vehicle.gui.LoadingState;
import com.jayfella.jme.vehicle.input.XBoxJoystickVehicleInputState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

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
        // gtTourer.addClickCommands(source -> setVehicleAsync(GrandTourer.class));

        Button gtrNismo = container.addChild(new Button("GTR Nismo"));
        gtrNismo.addClickCommands(source -> setVehicle(new GTRNismo(getApplication())));

        Button pickup = container.addChild(new Button("Pickup Truck"));
        pickup.addClickCommands(source -> setVehicle(new PickupTruck(getApplication())));
        // pickup.addClickCommands(source -> setVehicleAsync(PickupTruck.class));

        Button hatchback = container.addChild(new Button("Hatchback"));
        hatchback.addClickCommands(source -> setVehicle(new HatchBack(getApplication())));

        Button dunebuggy = container.addChild(new Button("Dune Buggy"));
        dunebuggy.addClickCommands(source -> setVehicle(new DuneBuggy(getApplication())));

        container.setLocalTranslation(
                (getApplication().getCamera().getWidth() * 0.5f) - (container.getPreferredSize().x * 0.5f),
                getApplication().getCamera().getHeight() - 10,
                1);
    }

    public void addVehicle(Car newVehicle) {

        vehicle = newVehicle;

        vehicle.showSpeedo(Vehicle.SpeedUnit.MPH);
        vehicle.showTacho();
        vehicle.attachToScene(scene, physicsSpace);

        vehicle.getVehicleControl().setPhysicsLocation(new Vector3f(0, 6, 0));

        // add some controls
        // KeyboardVehicleInputState inputState = new KeyboardVehicleInputState(vehicle);
        XBoxJoystickVehicleInputState inputState = new XBoxJoystickVehicleInputState(vehicle);
        getStateManager().attach(inputState);

        // engine graph GUI for viewing torqe/power @ revs
        EnginePowerGraphState enginePowerGraphState = new EnginePowerGraphState(vehicle);
        enginePowerGraphState.setEnabled(false);
        getStateManager().attach(enginePowerGraphState);

        // tyre data GUI for viewing how much grip each tyre has according to the pajecka formula.
        TyreDataState tyreDataState = new TyreDataState(vehicle);
        tyreDataState.setEnabled(false);
        getStateManager().attach(tyreDataState);

        // the main vehicle editor to modify all areas of the vehicle real-time.
        VehicleEditorState vehicleEditorState = new VehicleEditorState(vehicle);
        getStateManager().attach(vehicleEditorState);

        // vehicle debug add-on to enable/disable debug screens.
        DebugTabState debugTabState = new DebugTabState();
        getStateManager().attach(debugTabState);



        vehicle.getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

    }

    public void removeCurrentVehicle() {
        if (vehicle != null) {

            // KeyboardVehicleInputState inputState = getState(KeyboardVehicleInputState.class);
            XBoxJoystickVehicleInputState inputState = getState(XBoxJoystickVehicleInputState.class);
            if (inputState != null) {
                getStateManager().detach(inputState);
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
    }

    public void setVehicleAsync(Class<? extends Car> clazz) {

        getApplication().getStateManager().getState(LoadingState.class).setEnabled(true);
        removeCurrentVehicle();

        CompletableFuture
                .supplyAsync(() -> {

                    try {

                        Constructor constructor = null;

                        if (clazz.isAssignableFrom(GrandTourer.class)) {
                            constructor = GrandTourer.class.getConstructor(Application.class);
                        }
                        else if (clazz.isAssignableFrom(PickupTruck.class)) {
                            constructor = PickupTruck.class.getConstructor(Application.class);
                        }

                        Car car = (Car) constructor.newInstance(getApplication());
                        return car;
                    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .whenComplete((car, ex) -> {
                    if (car != null) {
                        getApplication().enqueue(() -> {
                            addVehicle(car);
                            getApplication().getStateManager().getState(LoadingState.class).setEnabled(false);
                        });

                    }
                });


    }

    public void setVehicle(Car newVehicle) {
        removeCurrentVehicle();
        addVehicle(newVehicle);
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
