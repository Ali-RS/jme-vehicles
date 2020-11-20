package com.jayfella.jme.vehicle.gui;

import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.shape.TrueTypeNode;
import com.atr.jme.font.util.Style;
import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
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
import jme3utilities.mesh.RectangleMesh;
import jme3utilities.mesh.RectangleOutlineMesh;
import jme3utilities.mesh.RoundedRectangle;

/**
 * Heads-up display (HUD) for driving a vehicle. This AppState should be
 * instantiated once and then enabled/disabled as needed. It manages portions of
 * the graphical user interface, namely:
 * <ul>
 * <li>the "Return to Main Menu" button</li>
 * <li>the pause button</li>
 * <li>the automatic-transmision mode indicator</li>
 * <li>the horn button</li>
 * <li>the power button</li>
 * <li>the steering-wheel indicator</li>
 * <li>the speedometer</li>
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

    private Button returnButton;
    private Car car;
    /**
     * vertical spacing between ATM indicator positions (in pixels)
     */
    private float atmiSpacingY;
    /**
     * dimensions of the GUI viewport (in pixels)
     */
    private float viewPortHeight, viewPortWidth;

    private Geometry atmiIndicatorGeometry;
    private Geometry hornButton;
    private Geometry pauseButton;
    private Geometry powerButton;
    private Geometry steering;
    /**
     * pre-loaded materials for the ATM indicator
     */
    private Material atmiBackgroundMaterial, atmiIndicatorMaterial;
    /**
     * pre-loaded materials for buttons
     */
    private Material hornSilentMaterial, hornSoundMaterial, pauseMaterial;
    private Material powerOffMaterial, powerOnMaterial, runMaterial;
    /**
     * Node that represents the ATM indicator
     */
    private Node atmiNode;
    /**
     * appstates for indicators
     */
    private SpeedometerState speedometer;
    private TachometerState tachometer;

    private TrueTypeMesh droidFont;
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
     * Display the horn button.
     *
     * @param sounding true &rarr; show it in the "sounding" state, false &rarr;
     * show it in the "silent" state
     */
    public void showHornButton(boolean sounding) {
        hideHornButton();

        float radius = 0.05f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        hornButton = new Geometry("horn button", mesh);
        attachToGui(hornButton);

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
        /*
         * Add a MouseListener to toggle the horn sounding/silent.
         */
        MouseListener listener = new DefaultMouseListener() {
            @Override
            public void mouseButtonEvent(MouseButtonEvent event, Spatial s1,
                    Spatial s2) {
                boolean pressed = event.isPressed();
                car.setHornInput(2, pressed);
                event.setConsumed();
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        hornButton.addControl(control);
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
        Camera camera = app.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
        /*
         * pre-load unshaded materials for buttons
         */
        AssetManager manager = app.getAssetManager();
        Texture texture = manager.loadTexture("Textures/horn-silent.png");
        hornSilentMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/horn-sound.png");
        hornSoundMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/pause.png");
        pauseMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/run.png");
        runMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/power-off.png");
        powerOffMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("Textures/power-on.png");
        powerOnMaterial = MyAsset.createUnshadedMaterial(manager, texture);
        /*
         * Construct a Geometry for the steering-wheel indicator.
         */
        float radius = 120f;
        Mesh mesh = new RectangleMesh(-radius, +radius, -radius, +radius, +1f);
        steering = new Geometry("steering wheel", mesh);

        texture = manager.loadTexture("Textures/steering.png");
        Material material = MyAsset.createUnshadedMaterial(manager, texture);
        RenderState ars = material.getAdditionalRenderState();
        ars.setBlendMode(RenderState.BlendMode.Alpha);
        steering.setMaterial(material);
        /*
         * pre-load the Droid font
         */
        AssetKey<TrueTypeMesh> assetKey = new TrueTypeKeyMesh(
                "Interface/Fonts/DroidSerifBold-aMPE.ttf", Style.Plain, 18);
        droidFont = manager.loadAsset(assetKey);
        /*
         * pre-load unshaded materials for the mode indicator
         */
        atmiBackgroundMaterial
                = MyAsset.createUnshadedMaterial(manager, ColorRGBA.Black);
        atmiIndicatorMaterial
                = MyAsset.createUnshadedMaterial(manager, ColorRGBA.Green);
    }

    /**
     * Callback invoked when this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        hideAtmi();
        hideHornButton();
        hidePauseButton();
        hidePowerButton();
        hideReturnButton();
        hideSpeedometer();
        hideSteering();
        hideTachometer();
    }

    /**
     * Callback invoked when this AppState becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        AppStateManager stateManager = getStateManager();
        BulletAppState bas = stateManager.getState(BulletAppState.class);
        boolean isPaused = (bas.getSpeed() == 0f);
        showPauseButton(isPaused);

        String[] atModes = car.listAtModes();
        showAtmi(atModes);
        showHornButton(false);
        showPowerButton(false);
        showReturnButton();
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

        // Indicate the mode of the automatic transmission.
        String mode;
        if (car.getGearBox().isReversing()) {
            mode = "R";
        } else {
            mode = "D";
        }
        String[] atModes = car.listAtModes();
        setAtmi(atModes, mode);
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
     * Hide the automatic-transmission mode indicator.
     */
    private void hideAtmi() {
        if (atmiNode != null) {
            atmiNode.removeFromParent();
            atmiNode = null;
        }
    }

    /**
     * Hide the horn button.
     */
    private void hideHornButton() {
        if (hornButton != null) {
            hornButton.removeFromParent();
            hornButton = null;
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
     * Indicate the specified automatic-transmission mode.
     *
     * @param modes the array of modes, in top-to-bottom order
     * @param selectedMode which mode to indicate (not null)
     */
    private void setAtmi(String modes[], String selectedMode) {
        for (int i = 0; i < modes.length; ++i) {
            if (selectedMode.equals(modes[i])) {
                float y = (modes.length - i) * atmiSpacingY;
                atmiIndicatorGeometry.setLocalTranslation(0f, y, 0f);
                atmiIndicatorGeometry.setCullHint(Spatial.CullHint.Never);
                return;
            }
        }

        atmiIndicatorGeometry.setCullHint(Spatial.CullHint.Always);
    }

    /**
     * Display the automatic-transmission mode indicator.
     *
     * @param modes an array of modes, in top-to-bottom order
     */
    private void showAtmi(String[] modes) {
        hideAtmi();

        atmiNode = new Node("Automatic-Transmission Mode Indicator");
        attachToGui(atmiNode);
        float centerX = 0.625f * viewPortWidth;
        float bottomY = 0.15f * viewPortHeight;
        atmiNode.setLocalTranslation(centerX, bottomY, guiZ);

        atmiSpacingY = 0.032f * viewPortHeight;
        float yModeCenter = modes.length * atmiSpacingY;
        float maxWidth = 0f;
        int kerning = 0;
        ColorRGBA textColor = ColorRGBA.White.clone();
        /*
         * Attach a TrueTypeNode for each mode.
         */
        for (String mode : modes) {
            TrueTypeNode ttNode = droidFont.getText(mode, kerning, textColor);
            atmiNode.attachChild(ttNode);
            float width = ttNode.getWidth();
            float x = -width / 2f;
            float height = ttNode.getHeight();
            float y = yModeCenter + height / 2f;
            ttNode.setLocalTranslation(x, y, guiZ);

            if (width > maxWidth) {
                maxWidth = width;
            }
            yModeCenter -= atmiSpacingY;
        }
        /*
         * Attach a rounded-rectangle Geometry for the background.
         */
        float bgHeight = (modes.length + 1) * atmiSpacingY;
        float cornerRadius = 0.01f * viewPortWidth;
        float bgWidth = maxWidth + 2f * cornerRadius;
        Mesh bgMesh = new RoundedRectangle(-bgWidth / 2f, bgWidth / 2f, 0f,
                bgHeight, cornerRadius, 1f);
        Geometry bgGeometry = new Geometry("bg", bgMesh);
        atmiNode.attachChild(bgGeometry);
        bgGeometry.setLocalTranslation(0f, 0f, -0.1f);
        bgGeometry.setMaterial(atmiBackgroundMaterial);
        /*
         * Attach a rectangular outline Geometry for the indicator.
         */
        float indWidth = maxWidth + cornerRadius;
        float x1 = -indWidth / 2f;
        float x2 = indWidth / 2f;
        float y1 = -atmiSpacingY / 2f;
        float y2 = atmiSpacingY / 2f;
        Mesh indMesh = new RectangleOutlineMesh(x1, x2, y1, y2);
        atmiIndicatorGeometry = new Geometry("ind", indMesh);
        atmiNode.attachChild(atmiIndicatorGeometry);
        atmiIndicatorGeometry.setCullHint(Spatial.CullHint.Always);
        atmiIndicatorGeometry.setMaterial(atmiIndicatorMaterial);
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
        float x = viewPortWidth - returnButton.getPreferredSize().x - 40f;
        float y = viewPortHeight - 20f;
        returnButton.setLocalTranslation(x, y, guiZ);
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
