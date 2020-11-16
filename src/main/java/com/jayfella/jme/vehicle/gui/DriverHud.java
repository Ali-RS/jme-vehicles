package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.SpeedometerState;
import com.jayfella.jme.vehicle.TachometerState;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.MouseListener;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.mesh.DiscMesh;

/**
 * Heads-up display (HUD) for driving a vehicle. This AppState should be
 * instantiated once and then enabled/disabled as needed. It manages portions of
 * the graphical user interface, namely:
 * <ul>
 * <li>the "Return to Main Menu" button</li>
 * <li>the pause button</li>
 * <li>the power button</li>
 * <li>the speedometer</li>
 * <li>the tachometer</li>
 * </ul>
 */
public class DriverHud extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DriverHud.class.getName());
    // *************************************************************************
    // fields

    private Button returnButton;
    private Car car;
    private Geometry pauseButton;
    private Geometry powerButton;
    /**
     * pre-loaded materials for buttons
     */
    private Material pauseMaterial, powerOffMaterial, powerOnMaterial;
    private Material runMaterial;
    private SpeedometerState speedometer;
    private TachometerState tachometer;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled HUD.
     */
    public DriverHud() {
        super("Driver HUD");
        super.setEnabled(false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Associate a Car with this HUD prior to enabling it.
     *
     * @param car the Car to use (or null for none)
     */
    public void setCar(Car car) {
        this.car = car;
    }

    /**
     * Toggle the engine between the started and stopped states.
     */
    public void toggleEngineStarted() {
        boolean wasStarted = car.getEngine().isStarted();
        if (wasStarted) {
            car.stopEngine();
            showPowerButton(false);
        } else {
            car.startEngine();
            showPowerButton(true);
        }
    }

    /**
     * Toggle the physics simulation between running and paused.
     */
    public void togglePhysicsPaused() {
        BulletAppState bas = getStateManager().getState(BulletAppState.class);
        float physicsSpeed = bas.getSpeed();
        if (physicsSpeed > 0f) { // was running
            bas.setSpeed(0f);
            showPauseButton(true);
        } else {
            bas.setSpeed(1f);
            showPauseButton(false);
        }
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked during initialization once this AppState is attached but
     * before onEnable() is called.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        /*
         * pre-load materials for buttons
         */
        AssetManager manager = app.getAssetManager();
        Texture texture = manager.loadTexture("Textures/pause.png");
        pauseMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/run.png");
        runMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/power-off.png");
        powerOffMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/power-on.png");
        powerOnMaterial = MyAsset.createUnshadedMaterial(manager, texture);
    }

    /**
     * Callback invoked when this AppState was previously enabled but is now
     * disabled either because setEnabled(false) was called or the state is
     * being cleaned up.
     */
    @Override
    protected void onDisable() {
        hidePauseButton();
        hidePowerButton();
        hideReturnButton();
        hideSpeedometer();
        hideTachometer();
    }

    /**
     * Callback invoked when this AppState is fully enabled, ie: is attached and
     * isEnabled() is true or when the setEnabled() status changes after the
     * state is attached.
     */
    @Override
    protected void onEnable() {
        AppStateManager stateManager = getStateManager();
        BulletAppState bas = stateManager.getState(BulletAppState.class);
        boolean isPaused = (bas.getSpeed() == 0f);
        showPauseButton(isPaused);

        showPowerButton(false);
        showReturnButton();
        showSpeedo(Vehicle.SpeedUnit.MPH);
        showTacho();
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        Node guiNode = simpleApp.getGuiNode();
        guiNode.attachChild(spatial);
    }

    /**
     * Hide the pause button.
     */
    private void hidePauseButton() {
        if (pauseButton != null) {
            pauseButton.removeFromParent();
            pauseButton = null;
        }
    }

    /**
     * Hide the power button.
     */
    private void hidePowerButton() {
        if (powerButton != null) {
            powerButton.removeFromParent();
            powerButton = null;
        }
    }

    /**
     * Hide the "Return to Main Menu" button.
     */
    private void hideReturnButton() {
        if (returnButton != null) {
            returnButton.removeFromParent();
            returnButton = null;
        }
    }

    /**
     * Hide the speedometer.
     */
    private void hideSpeedometer() {
        if (speedometer != null) {
            getStateManager().detach(speedometer);
            speedometer = null;
        }
    }

    /**
     * Hide the tachometer.
     */
    private void hideTachometer() {
        if (tachometer != null) {
            getStateManager().detach(tachometer);
            tachometer = null;
        }
    }

    /**
     * Display the pause/run button.
     *
     * @param paused true &rarr; show it in the "paused" state, false &rarr;
     * show it in the "running" state
     */
    private void showPauseButton(boolean paused) {
        hidePauseButton();

        float radius = 0.025f * viewPortHeight();
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        pauseButton = new Geometry("pause button", mesh);
        attachToGui(pauseButton);

        if (paused) {
            pauseButton.setMaterial(pauseMaterial);
        } else {
            pauseButton.setMaterial(runMaterial);
        }
        /*
         * Position the button in the viewport.
         */
        float x = 0.8f * viewPortWidth();
        float y = 0.95f * viewPortHeight();
        float z = 1f;
        pauseButton.setLocalTranslation(x, y, z);
        /*
         * Add a MouseListener to toggle the simulation running/paused.
         */
        MouseListener listener = new DefaultMouseListener() {
            @Override
            public void mouseButtonEvent(MouseButtonEvent event, Spatial s1,
                    Spatial s2) {
                if (event.isPressed()) {
                    togglePhysicsPaused();
                }
                event.setConsumed();
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        pauseButton.addControl(control);
    }

    /**
     * Display the power button.
     *
     * @param on true &rarr; show it in the "on" state, false &rarr; show it in
     * the "off" state
     */
    private void showPowerButton(boolean on) {
        hidePowerButton();

        float radius = 0.05f * viewPortHeight();
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        powerButton = new Geometry("power button", mesh);
        attachToGui(powerButton);

        if (on) {
            powerButton.setMaterial(powerOnMaterial);
        } else {
            powerButton.setMaterial(powerOffMaterial);
        }
        /*
         * Position the button in the viewport.
         */
        float x = 0.63f * viewPortWidth();
        float y = 0.07f * viewPortHeight();
        float z = 1f;
        powerButton.setLocalTranslation(x, y, z);
        /*
         * Add a MouseListener to toggle the engine on/off.
         */
        MouseListener listener = new DefaultMouseListener() {
            @Override
            public void mouseButtonEvent(MouseButtonEvent event, Spatial s1,
                    Spatial s2) {
                if (event.isPressed()) {
                    toggleEngineStarted();
                }
                event.setConsumed();
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        powerButton.addControl(control);
    }

    /**
     * Display the "Return to Main Menu" button.
     */
    private void showReturnButton() {
        hideReturnButton();

        returnButton = new Button("Return to Main Menu");
        attachToGui(returnButton);

        returnButton.setFontSize(16f);
        GuiComponent background = returnButton.getBackground();
        ((TbtQuadBackgroundComponent) background).setMargin(10f, 5f);
        Command<Button> returnCmd = new ReturnToMenuClickCommand(car);
        returnButton.addClickCommands(returnCmd);
        /*
         * Position the button in the viewport.
         */
        float x = viewPortWidth() - returnButton.getPreferredSize().x - 40f;
        float y = viewPortHeight() - 20f;
        float z = 1f;
        returnButton.setLocalTranslation(x, y, z);
    }

    /**
     * Display the speedometer using the specified speed unit.
     *
     * @param speedUnit (not null)
     */
    private void showSpeedo(Vehicle.SpeedUnit speedUnit) {
        hideSpeedometer();

        speedometer = new SpeedometerState(car, speedUnit);
        getStateManager().attach(speedometer);
    }

    /**
     * Display the tachometer.
     */
    private void showTacho() {
        hideTachometer();

        tachometer = new TachometerState(car);
        getStateManager().attach(tachometer);
    }

    /**
     * Determine the viewport height.
     *
     * @return the height (in pixels, &gt;0)
     */
    private float viewPortHeight() {
        Application application = getApplication();
        Camera camera = application.getCamera();
        float result = camera.getHeight();

        assert result > 0f : result;
        return result;
    }

    /**
     * Determine the viewport width.
     *
     * @return the width (in pixels, &gt;0)
     */
    private float viewPortWidth() {
        Application application = getApplication();
        Camera camera = application.getCamera();
        float result = camera.getWidth();

        assert result > 0f : result;
        return result;
    }
}
