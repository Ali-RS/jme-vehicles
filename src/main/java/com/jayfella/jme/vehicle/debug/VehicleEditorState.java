package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.debug.parts.BrakesEditor;
import com.jayfella.jme.vehicle.debug.parts.ChassisEditor;
import com.jayfella.jme.vehicle.debug.parts.EngineEditor;
import com.jayfella.jme.vehicle.debug.parts.GearboxEditor;
import com.jayfella.jme.vehicle.debug.parts.SuspensionEditor;
import com.jayfella.jme.vehicle.debug.parts.WheelsEditor;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.component.SpringGridLayout;

public class VehicleEditorState extends BaseAppState {
    // *************************************************************************
    // fields

    final public static int Width = 350;
    final public static SpringGridLayout Layout = new SpringGridLayout(Axis.Y,
            Axis.X, FillMode.Even, FillMode.Last);
    final private TabbedPanel tabbedPanel;
    final private Vehicle vehicle;
    // *************************************************************************
    // constructor

    public VehicleEditorState(Vehicle vehicle) {
        this.vehicle = vehicle;
        tabbedPanel = new TabbedPanel();
    }
    // *************************************************************************
    // new methods exposed

    public TabbedPanel getTabbedPanel() {
        return tabbedPanel;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        tabbedPanel.addTab("Chassis", new ChassisEditor(vehicle));
        tabbedPanel.addTab("Engine", new EngineEditor(vehicle));
        tabbedPanel.addTab("Gearbox", new GearboxEditor(vehicle));
        tabbedPanel.addTab("Suspension", new SuspensionEditor(vehicle));
        tabbedPanel.addTab("Brakes", new BrakesEditor(vehicle));
        tabbedPanel.addTab("Wheels", new WheelsEditor(vehicle));

        tabbedPanel.setLocalTranslation(
                application.getCamera().getWidth() - 370,
                application.getCamera().getHeight() - 20,
                0
        );
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        tabbedPanel.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        Main.getApplication().getGuiNode().attachChild(tabbedPanel);
    }
}
