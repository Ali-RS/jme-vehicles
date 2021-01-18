package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.MagicFormulaState;
import com.jme3.app.Application;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class DebugTabState extends BaseAppState {

    private boolean displayFps = false;
    private boolean displayStats = false;
    // *************************************************************************
    // new methods exposed

    public boolean isDisplayFps() {
        return displayFps;
    }

    public boolean isDisplayStats() {
        return displayStats;
    }

    public void setDisplayFps(boolean displayFps) {
        this.displayFps = displayFps;
        getState(StatsAppState.class).setDisplayFps(displayFps);
    }

    public void setDisplayStats(boolean displayStats) {
        this.displayStats = displayStats;
        getState(StatsAppState.class).setDisplayStatView(displayStats);
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
        Container container = new Container();

        BulletAppState bulletAppState = getState(BulletAppState.class);

        VehicleEditorState editorState = getState(VehicleEditorState.class);
        TabbedPanel tabbedPanel = editorState.getTabbedPanel();

        EnginePowerGraphState enginePowerGraphState = getState(EnginePowerGraphState.class);
        TireDataState tireDataState = getState(TireDataState.class);
        MagicFormulaState magicFormulaState = getState(MagicFormulaState.class);

        PropertyPanel propertyPanel = container.addChild(new PropertyPanel("glass"));
        propertyPanel.addBooleanProperty("Display FPS", this, "displayFps");
        propertyPanel.addBooleanProperty("Display Stats", this, "displayStats");
        propertyPanel.addBooleanProperty("Bullet Debug", bulletAppState, "debugEnabled");
        propertyPanel.addBooleanProperty("Engine Graph", enginePowerGraphState, "enabled");
        propertyPanel.addBooleanProperty("Tire Graph", tireDataState, "enabled");
        propertyPanel.addBooleanProperty("Tire Data", magicFormulaState, "vehicleDataEnabled");
        propertyPanel.addBooleanProperty("Center of Gravity", magicFormulaState, "centerOfGravityEnabled");

        tabbedPanel.addTab("Debug", container);

        tabbedPanel.setLocalTranslation(
                application.getCamera().getWidth() - 420,
                application.getCamera().getHeight() - 60,
                0
        );
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        // do nothing
    }
}
