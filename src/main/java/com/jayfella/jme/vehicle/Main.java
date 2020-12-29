package com.jayfella.jme.vehicle;

import com.atr.jme.font.asset.TrueTypeLoader;
import com.jayfella.jme.vehicle.examples.cars.*;
import com.jayfella.jme.vehicle.examples.environments.*;
import com.jayfella.jme.vehicle.examples.skies.*;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.input.DumpInputState;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickConnectionListener;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.style.BaseStyles;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.SignalTracker;

public class Main extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Main.class.getName());
    /**
     * filesystem path to the directory where screenshots will be written
     */
    final private static String screenshotDirectory = "./";
    /**
     * filename prefix for numbered screen shots
     */
    final private static String screenshotPrefix = "screen_shot";
    // *************************************************************************
    // fields

    /**
     * selected game environment/world (not null)
     */
    private static Environment environment = new Racetrack();
    /**
     * application instance
     */
    private static Main application;
    /**
     * selected sky, including lights and post-processing (not null)
     */
    private static Sky sky = new QuarrySky();
    /**
     * selected Vehicle (not null)
     */
    private static Vehicle vehicle = new GrandTourer();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a SimpleApplication without FlyCam or debug keys.
     */
    private Main() {
        super(
                new AudioListenerState(),
                new ConstantVerifierState(),
                new DriverHud(),
                new DumpInputState(),
                new LoadingState(),
                new ScreenshotAppState(screenshotDirectory, screenshotPrefix),
                new StatsAppState()
        );
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Attach the selected environment, sky, and vehicle to the scene.
     */
    public void attachAllToScene() {
        sky.attachToScene(rootNode);

        environment.resetCameraPosition();
        environment.attachToScene(rootNode);

        vehicle.attachToScene(rootNode);
    }

    /**
     * Find the first attached AppState that's an instance of the specified
     * class.
     *
     * @param <T> the kind of AppState
     * @param subclass the kind of AppState to search for (not null)
     * @return the pre-existing instance (not null)
     */
    public static <T extends AppState> T findAppState(Class<T> subclass) {
        AppStateManager manager = application.getStateManager();
        T result = manager.getState(subclass);

        assert result != null;
        return result;
    }

    /**
     * Access the application instance from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Main getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Access the environment from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Environment getEnvironment() {
        assert environment != null;
        return environment;
    }

    /**
     * Access the selected vehicle from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Vehicle getVehicle() {
        assert vehicle != null;
        return vehicle;
    }

    /**
     * Main entry point for the More Advanced Vehicles application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String... args) {
        /*
         * Mute the chatty loggers found in some imported packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        boolean forceDialog = false;
        /*
         * Process any command-line arguments.
         */
        for (String arg : args) {
            switch (arg) {
                case "-f":
                case "--forceDialog":
                    forceDialog = true;
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }
        SignalTracker.logger.setLevel(Level.WARNING);

        AppSettings appSettings = new AppSettings(true);
        appSettings.setResolution(1280, 720);
        appSettings.setTitle("More Advanced Vehicles");
        appSettings.setUseJoysticks(true);
        appSettings.setVSync(true);

        application = new Main();
        application.setDisplayStatView(false);
        application.setDisplayFps(false);
        application.setSettings(appSettings);
        application.setShowSettings(forceDialog);
        application.start();
    }

    /**
     * Replace the current Environment with a new one.
     *
     * @param newEnvironment the desired environment (not null, loaded)
     */
    public void setEnvironment(Environment newEnvironment) {
        sky.detachFromScene();
        vehicle.detachFromScene();
        environment.detachFromScene();

        environment = newEnvironment;
        attachAllToScene();
        /*
         * Re-use the existing input state with the new Vehicle instance.
         */
        NonDrivingInputState inputState
                = Main.findAppState(NonDrivingInputState.class);
        inputState.setVehicle(vehicle);
    }

    /**
     * Replace the current Sky with a new one.
     *
     * @param newSky the desired Sky (not null, loaded)
     */
    public void setSky(Sky newSky) {
        sky.detachFromScene();
        sky = newSky;
        sky.attachToScene(rootNode);
    }

    /**
     * Replace the current Vehicle with a new one.
     *
     * @param newVehicle the desired Vehicle (not null, loaded)
     */
    public void setVehicle(Vehicle newVehicle) {
        vehicle.detachFromScene();
        vehicle = newVehicle;
        vehicle.attachToScene(rootNode);
    }
    // *************************************************************************
    // SimpleApplication methods

    @Override
    public void simpleInitApp() {
        NativeLibrary.setStartupMessageEnabled(false);
        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        renderer.setDefaultAnisotropicFilter(4);

        inputManager.addJoystickConnectionListener(new JoystickConnectionListener() {
            @Override
            public void onConnected(Joystick joystick) {
                System.out.println("Joystick connected: " + joystick);
            }

            @Override
            public void onDisconnected(Joystick joystick) {
                System.out.println("Joystick disconnected: " + joystick);
            }
        });

        inputManager.clearMappings();
        inputManager.clearRawInputListeners();
        /*
         * The dash camera sits close to the bodywork, so set its near clipping
         * plane accordingly.
         */
        float near = 0.1f;
        float far = 1800f;
        MyCamera.setNearFar(cam, near, far);

        // initialize Lemur with the "glass" style
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // initialize physics with debug disabled
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        stateManager.attach(bulletAppState);

        Sky.initialize();
        environment.resetCameraPosition();

        // this consumes joystick input. I'll have to investigate why.
        stateManager.getState(FocusNavigationState.class).setEnabled(false);
    }
}
