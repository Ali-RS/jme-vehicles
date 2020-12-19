package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.VehicleAudioState;
import com.jayfella.jme.vehicle.input.DrivingInputState;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Quaternion;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.simsilica.lemur.event.MouseEventControl;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.SignalTracker;
import jme3utilities.mesh.DiscMesh;
import jme3utilities.mesh.RectangleMesh;

/**
 * Heads-up display (HUD) for driving a vehicle. This AppState should be
 * instantiated once and then enabled/disabled as needed. It directly manages
 * portions of the graphical user interface, namely:
 * <ul>
 * <li>the exit button</li>
 * <li>the horn button</li>
 * <li>the mute button</li>
 * <li>the pause button</li>
 * <li>the power button</li>
 * <li>the single-step button</li>
 * <li>the steering-wheel indicator</li>
 * </ul>
 * It indirectly manages:
 * <ul>
 * <li>the automatic-transmision mode indicator</li>
 * <li>the camera-name indicator</li>
 * <li>the speedometer</li>
 * <li>the compass</li>
 * <li>the tachometer</li>
 * </ul>
 */
public class DriverHud extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * Z-coordinate for most GUI geometries
     */
    final private static float guiZ = 1f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DriverHud.class.getName());
    // *************************************************************************
    // fields

    /**
     * Appstate to manage the automatic-transmission mode indicator
     */
    final private AtmiState atmiState = new AtmiState();
    /**
     * Appstate to manage the camera-name indicator
     */
    final private CameraNameState cameraNameState = new CameraNameState();
    private Car car;
    final private CompassState compassState = new CompassState();
    /**
     * dimensions of the GUI viewport (in pixels)
     */
    private float viewPortHeight, viewPortWidth;

    private Geometry exitButton;
    private Geometry hornButton;
    private Geometry muteButton;
    private Geometry pauseButton;
    private Geometry powerButton;
    private Geometry singleStepButton;
    private Geometry steering;
    /**
     * pre-loaded materials for iconic buttons
     */
    private Material exitMaterial;
    private Material hornSilentMaterial, hornSoundMaterial;
    private Material muteMaterial, pauseMaterial;
    private Material powerOffMaterial, powerOnMaterial, runMaterial;
    private Material soundMaterial;
    /**
     * appstates to manage the dial indicators
     */
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
        atmiState.setCar(car);
    }

    /**
     * Display the horn button.
     *
     * @param sounding true &rarr; show it in the "sounding" state, false &rarr;
     * show it in the "silent" state
     */
    public void showHornButton(boolean sounding) {
        if (hornButton.getParent() == null) {
            attachToGui(hornButton);
        }

        if (sounding) {
            hornButton.setMaterial(hornSoundMaterial);
        } else {
            hornButton.setMaterial(hornSilentMaterial);
        }
        /*
         * Position the button in the viewport.
         */
        float x = 0.5f * viewPortWidth;
        float y = 0.18f * viewPortHeight;
        hornButton.setLocalTranslation(x, y, guiZ);
    }

    /**
     * Single-step the physics simulation.
     */
    public void singleStepPhysics() {
        BulletAppState bas = Main.findAppState(BulletAppState.class);
        float physicsSpeed = bas.getSpeed();
        assert physicsSpeed == 0f : physicsSpeed;

        PhysicsSpace space = bas.getPhysicsSpace();
        float timeStep = space.getAccuracy();
        space.update(timeStep, 0);
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
        BulletAppState bas = Main.findAppState(BulletAppState.class);
        float physicsSpeed = bas.getSpeed();
        if (physicsSpeed > 0f) { // was running
            bas.setSpeed(0f);
            showPauseButton(true);
            showSingleStepButton();
        } else {
            bas.setSpeed(1f);
            showPauseButton(false);
            hideSingleStepButton();
        }
    }

    /**
     * Toggle the sound between enabled and muted.
     */
    public void toggleSoundMuted() {
        VehicleAudioState.toggleMuted();

        boolean isMuted = VehicleAudioState.isMuted();
        showMuteButton(isMuted);
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
        AppStateManager stateManager = getApplication().getStateManager();
        stateManager.detach(atmiState);
        stateManager.detach(cameraNameState);
        stateManager.detach(compassState);
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        Camera camera = app.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
        /*
         * pre-load unshaded materials for buttons
         */
        AssetManager manager = app.getAssetManager();
        Texture texture = manager.loadTexture("Textures/exit.png");
        exitMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/horn-silent.png");
        hornSilentMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/horn-sound.png");
        hornSoundMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/mute.png");
        muteMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/pause.png");
        pauseMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/run.png");
        runMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/power-off.png");
        powerOffMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/power-on.png");
        powerOnMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/sound.png");
        soundMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        AppStateManager stateManager = getApplication().getStateManager();
        stateManager.attach(atmiState);
        stateManager.attach(cameraNameState);
        stateManager.attach(compassState);
        /*
         * Construct a Geometry for the horn button.
         */
        float radius = 0.05f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        hornButton = new Geometry("horn button", mesh);
        /*
         * Add an Expander to toggle the horn sounding/silent.
         */
        Expander listener = new Expander(hornButton) {
            @Override
            public void onClick(boolean isPressed) {
                DrivingInputState inputState
                        = Main.findAppState(DrivingInputState.class);
                SignalTracker signalTracker = inputState.getSignalTracker();
                signalTracker.setActive("horn", 999, isPressed);
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        hornButton.addControl(control);
        /*
         * Construct a Geometry for the single-step button.
         */
        radius = 0.025f * viewPortHeight;
        numVertices = 25;
        mesh = new DiscMesh(radius, numVertices);
        singleStepButton = new Geometry("single-step button", mesh);
        texture = manager.loadTexture("Textures/single-step.png");
        Material material = MyAsset.createUnshadedMaterial(manager, texture);
        singleStepButton.setMaterial(material);
        /*
         * Add an Expander to single-step the simulation.
         */
        listener = new Expander(singleStepButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    singleStepPhysics();
                }
            }
        };
        control = new MouseEventControl(listener);
        singleStepButton.addControl(control);
        /*
         * Construct a Geometry for the steering-wheel indicator.
         */
        radius = 120f;
        mesh = new RectangleMesh(-radius, +radius, -radius, +radius, +1f);
        steering = new Geometry("steering wheel", mesh);

        texture = manager.loadTexture("Textures/steering.png");
        material = MyAsset.createUnshadedMaterial(manager, texture);
        RenderState ars = material.getAdditionalRenderState();
        ars.setBlendMode(RenderState.BlendMode.Alpha);
        steering.setMaterial(material);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        atmiState.setEnabled(false);
        cameraNameState.setEnabled(false);
        compassState.setEnabled(false);
        hideExitButton();
        hideHornButton();
        hideMuteButton();
        hidePauseButton();
        hidePowerButton();
        hideSingleStepButton();
        hideSpeedometer();
        hideSteering();
        hideTachometer();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        boolean isMuted = VehicleAudioState.isMuted();
        showMuteButton(isMuted);

        BulletAppState bas = Main.findAppState(BulletAppState.class);
        boolean isPaused = (bas.getSpeed() == 0f);
        showPauseButton(isPaused);
        if (isPaused) {
            showSingleStepButton();
        }

        atmiState.setEnabled(true);
        cameraNameState.setEnabled(true);
        compassState.setEnabled(true);
        showExitButton();
        showHornButton(false);

        boolean isEngineRunning = car.getEngine().isStarted();
        showPowerButton(isEngineRunning);

        showSpeedo(Vehicle.SpeedUnit.MPH);
        showSteeringWheel();
        showTacho();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        /*
         * Re-orient the horn button and the steering-wheel indicator.
         */
        float angle = car.steeringWheelAngle();
        Quaternion orientation = new Quaternion();
        orientation.fromAngles(0f, 0f, angle);
        hornButton.setLocalRotation(orientation);
        steering.setLocalRotation(orientation);
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        Node guiNode = Main.getApplication().getGuiNode();
        guiNode.attachChild(spatial);
    }

    /**
     * Hide the exit button.
     */
    private void hideExitButton() {
        if (exitButton != null) {
            exitButton.removeFromParent();
            exitButton = null;
        }
    }

    /**
     * Hide the horn button.
     */
    private void hideHornButton() {
        if (hornButton.getParent() != null) {
            hornButton.removeFromParent();
        }
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
     * Hide the mute button.
     */
    private void hideMuteButton() {
        if (muteButton != null) {
            muteButton.removeFromParent();
            muteButton = null;
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
     * Hide the single-step button.
     */
    private void hideSingleStepButton() {
        singleStepButton.removeFromParent();
    }

    /**
     * Hide the steering-wheel indicator.
     */
    private void hideSteering() {
        steering.removeFromParent();
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
     * Exit the car and return to the main menu.
     */
    private void returnToMainMenu() {
        DrivingInputState inputState
                = Main.findAppState(DrivingInputState.class);
        inputState.returnToMainMenu();
    }

    /**
     * Display the exit button.
     */
    private void showExitButton() {
        hideExitButton();

        float radius = 0.035f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        exitButton = new Geometry("exit button", mesh);
        attachToGui(exitButton);

        exitButton.setMaterial(exitMaterial);
        /*
         * Position the button in the viewport.
         */
        float x = 0.975f * viewPortWidth;
        float y = 0.955f * viewPortHeight;
        exitButton.setLocalTranslation(x, y, guiZ);
        /*
         * Add an Expander to return to the main menu.
         */
        Expander listener = new Expander(exitButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    returnToMainMenu();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        exitButton.addControl(control);
    }

    /**
     * Display the mute/sound button.
     *
     * @param muted true &rarr; show it in the "muted" state, false &rarr; show
     * it in the "enabled" state
     */
    private void showMuteButton(boolean muted) {
        hideMuteButton();

        float radius = 0.025f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        muteButton = new Geometry("mute button", mesh);
        attachToGui(muteButton);

        if (muted) {
            muteButton.setMaterial(muteMaterial);
        } else {
            muteButton.setMaterial(soundMaterial);
        }
        /*
         * Position the button in the viewport.
         */
        float x = 0.76f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        muteButton.setLocalTranslation(x, y, guiZ);
        /*
         * Add an Expander to toggle the sound on/muted.
         */
        Expander listener = new Expander(muteButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    toggleSoundMuted();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        muteButton.addControl(control);
    }

    /**
     * Display the pause/run button.
     *
     * @param paused true &rarr; show it in the "paused" state, false &rarr;
     * show it in the "running" state
     */
    private void showPauseButton(boolean paused) {
        hidePauseButton();

        float radius = 0.025f * viewPortHeight;
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
        float x = 0.8f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        pauseButton.setLocalTranslation(x, y, guiZ);
        /*
         * Add an Expander to toggle the simulation running/paused.
         */
        Expander listener = new Expander(pauseButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    togglePhysicsPaused();
                }
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

        float radius = 0.035f * viewPortHeight;
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
        float x = 0.625f * viewPortWidth;
        float y = 0.07f * viewPortHeight;
        powerButton.setLocalTranslation(x, y, guiZ);
        /*
         * Add an Expander to toggle the engine on/off.
         */
        Expander listener = new Expander(powerButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    toggleEngineStarted();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        powerButton.addControl(control);
    }

    /**
     * Display the single-step button.
     */
    private void showSingleStepButton() {
        if (singleStepButton.getParent() == null) {
            attachToGui(singleStepButton);
        }
        /*
         * Position the button in the viewport.
         */
        float x = 0.84f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        singleStepButton.setLocalTranslation(x, y, guiZ);
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
     * Display the steering-wheel indicator.
     */
    private void showSteeringWheel() {
        attachToGui(steering);
        /*
         * Position the indicator in the viewport.
         */
        float x = 0.5f * viewPortWidth;
        float y = 0.18f * viewPortHeight;
        steering.setLocalTranslation(x, y, 0.9f);
    }

    /**
     * Display the tachometer.
     */
    private void showTacho() {
        hideTachometer();

        tachometer = new TachometerState(car);
        getStateManager().attach(tachometer);
    }
}
