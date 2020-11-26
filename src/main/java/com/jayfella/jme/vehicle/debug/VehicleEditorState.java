package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.debug.parts.BrakesEditor;
import com.jayfella.jme.vehicle.debug.parts.ChassisEditor;
import com.jayfella.jme.vehicle.debug.parts.EngineEditor;
import com.jayfella.jme.vehicle.debug.parts.GearboxEditor;
import com.jayfella.jme.vehicle.debug.parts.SuspensionEditor;
import com.jayfella.jme.vehicle.debug.parts.VehicleEditor;
import com.jayfella.jme.vehicle.debug.parts.WheelsEditor;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.component.SpringGridLayout;

public class VehicleEditorState extends BaseAppState {

    private final Car vehicle;
    private final TabbedPanel tabbedPanel;

    public static final SpringGridLayout Layout = new SpringGridLayout(Axis.Y, Axis.X, FillMode.Even, FillMode.Last);
    public static final int Width = 350;

    private VehicleEditor chassis;
    private VehicleEditor engine;
    private VehicleEditor gearbox;
    private VehicleEditor suspension;
    private VehicleEditor brakes;
    private VehicleEditor wheels;

    public VehicleEditorState(Car vehicle) {

        this.vehicle = vehicle;
        this.tabbedPanel = new TabbedPanel();

    }

    public TabbedPanel getTabbedPanel() {
        return tabbedPanel;
    }

    @Override
    protected void initialize(Application app) {

        chassis = tabbedPanel.addTab("Chassis", new ChassisEditor(vehicle));
        engine = tabbedPanel.addTab("Engine", new EngineEditor(vehicle));
        gearbox = tabbedPanel.addTab("Gearbox", new GearboxEditor(vehicle));
        suspension = tabbedPanel.addTab("Suspension", new SuspensionEditor(vehicle));
        brakes = tabbedPanel.addTab("Brakes", new BrakesEditor(vehicle));
        wheels = tabbedPanel.addTab("Wheels", new WheelsEditor(vehicle));

        tabbedPanel.setLocalTranslation(
                app.getCamera().getWidth() - 370,
                app.getCamera().getHeight() - 20,
                0
        );
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        ((SimpleApplication)getApplication()).getGuiNode().attachChild(this.tabbedPanel);
    }

    @Override
    protected void onDisable() {
        this.tabbedPanel.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        chassis.update(tpf);
        engine.update(tpf);
        gearbox.update(tpf);
        suspension.update(tpf);
        brakes.update(tpf);
        wheels.update(tpf);

    }

}
