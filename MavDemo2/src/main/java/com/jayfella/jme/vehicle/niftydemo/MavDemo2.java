package com.jayfella.jme.vehicle.niftydemo;

import com.jayfella.jme.vehicle.ChunkManager;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.vehicles.GrandTourer;
import com.jayfella.jme.vehicle.gui.CompassState;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.gui.SteeringWheelState;
import com.jayfella.jme.vehicle.gui.TachometerState;
import com.jayfella.jme.vehicle.niftydemo.action.Action;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.app.BasicProfilerState;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeVersion;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.debug.PerformanceAppState;
import jme3utilities.minie.MinieVersion;
import jme3utilities.minie.PhysicsDumper;
import jme3utilities.nifty.GuiApplication;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.nifty.displaysettings.DsScreen;
import jme3utilities.ui.DisplaySettings;
import jme3utilities.ui.DisplaySizeLimits;

/**
 * An application with a Nifty GUI to demonstrate the MaVehicles library. The
 * application's main entry point is in this class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MavDemo2 extends GuiApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MavDemo2.class.getName());
    // *************************************************************************
    // fields

    /**
     * true once {@link #startup1()} has completed, until then false
     */
    private boolean didStartup1 = false;
    /**
     * demo's "game state"
     */
    private static DemoState demoState;
    /**
     * Nifty screen for editing display settings
     */
    private static DsScreen displaySettingsScreen;
    /**
     * application instance
     */
    private static MavDemo2 application;
    /**
     * dump state for debugging
     */
    final public static PhysicsDumper dumper = new PhysicsDumper();
    // *************************************************************************
    // new methods exposed

    /**
     * Find the first attached AppState that's an instance of the specified
     * class.
     *
     * @param <T> type of subclass
     * @param subclass the kind of AppState to search for (not null)
     * @return the pre-existing instance (not null)
     */
    public static <T extends AppState> T findAppState(Class<T> subclass) {
        AppStateManager manager = application.getStateManager();
        T appState = manager.getState(subclass);

        assert appState != null;
        return appState;
    }

    /**
     * Access the application instance from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static MavDemo2 getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Access the live DemoState.
     *
     * @return the pre-existing instance (not null)
     */
    public static DemoState getDemoState() {
        assert demoState != null;
        return demoState;
    }

    /**
     * Main entry point for the MavDemo2 application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        /*
         * Mute the chatty loggers found in some imported packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        boolean forceDialog = false;
        /*
         * Process any command-line arguments.
         */
        for (String arg : arguments) {
            switch (arg) {
                case "-f":
                case "--forceDialog":
                    forceDialog = true;
                    break;

                case "-v":
                case "--verbose":
                    Heart.setLoggingLevels(Level.INFO);
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }

        mainStartup(forceDialog);
    }
    // *************************************************************************
    // GuiApplication methods

    /**
     * Initialize the MavDemo2 application.
     */
    @Override
    public void guiInitializeApplication() {
        logger.info("");

        if (!Heart.areAssertionsEnabled()) {
            logger.warning("Assertions are disabled!");
        }
        /*
         * Log version strings.
         */
        logger.log(Level.INFO, "jme3-core version is {0}",
                MyString.quote(JmeVersion.FULL_NAME));
        logger.log(Level.INFO, "Heart version is {0}",
                MyString.quote(Heart.versionShort()));
        logger.log(Level.INFO, "Minie version is {0}",
                MyString.quote(MinieVersion.versionShort()));

        renderer.setDefaultAnisotropicFilter(8);

        dumper.setDumpBucket(true)
                .setDumpCull(true)
                .setDumpShadow(true)
                .setDumpTransform(true);
        /*
         * Detach a couple app states created by SimpleApplication.
         */
        DebugKeysAppState debugKeys = findAppState(DebugKeysAppState.class);
        stateManager.detach(debugKeys);
        AppState flyByCam = findAppState(FlyCamAppState.class);
        stateManager.detach(flyByCam);

        attachAppStates();
    }

    /**
     * Process an action from the GUI or keyboard that wasn't handled by the
     * active InputMode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        logger.log(Level.INFO, "Got action {0} ongoing={1}", new Object[]{
            MyString.quote(actionString), ongoing
        });

        boolean handled;
        if (ongoing) {
            handled = Action.processOngoing(actionString);
        } else {
            handled = Action.processNotOngoing(actionString);
        }

        if (!handled) {
            /*
             * Forward unhandled action to the superclass.
             */
            super.onAction(actionString, ongoing, tpf);
        }
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);

        if (!didStartup1) {
            startup1();
            didStartup1 = true;
        } else {
            Cameras.update();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Attach app states during initialization.
     */
    private void attachAppStates() {
        boolean success;

        BasicProfilerState profiler = new BasicProfilerState();
        success = stateManager.attach(profiler);
        assert success;

        BindScreen bindScreen = new BindScreen();
        success = stateManager.attach(bindScreen);
        assert success;

        stateManager.attachAll(
                new ChunkManager(),
                new CompassState()
        );

        PhysicsSpace physicsSpace = configurePhysics();

        Cameras.configure();
        View view = new View();
        success = stateManager.attach(view);
        assert success;

        demoState = new DemoState(physicsSpace);
        Vehicle vehicle = new GrandTourer();
        vehicle.load(assetManager);
        demoState.addVehicle(vehicle);

        success = stateManager.attach(displaySettingsScreen);
        assert success;
        /*
         * Create and attach a screen controller for the heads-up display (HUD)
         * and link it to the DefaultInputMode.
         */
        MainHud hud = new MainHud();
        success = stateManager.attach(hud);
        assert success;

        PerformanceAppState pas = new PerformanceAppState();
        success = stateManager.attach(pas);
        assert success;

        String directory = "./";
        String filenamePrefix = "screen_shot";
        ScreenshotAppState screenshotAppState
                = new ScreenshotAppState(directory, filenamePrefix);
        success = stateManager.attach(screenshotAppState);
        assert success;

        SpeedometerState speedometer
                = new SpeedometerState(vehicle, SpeedUnit.MPH);
        success = stateManager.attach(speedometer);
        assert success;

        float radius = 120f; // pixels
        float x = 0.5f * cam.getWidth();
        float y = 0.18f * cam.getHeight();
        float z = 1f;
        SteeringWheelState steeringWheel
                = new SteeringWheelState(radius, new Vector3f(x, y, z));
        steeringWheel.setVehicle(vehicle);
        steeringWheel.setEnabled(true);
        success = stateManager.attach(steeringWheel);
        assert success;

        Engine engine = vehicle.getEngine();
        TachometerState tachometer = new TachometerState(engine);
        success = stateManager.attach(tachometer);
        assert success;
    }

    /**
     * Configure physics during startup.
     */
    private PhysicsSpace configurePhysics() {
        BulletAppState bulletAppState = new BulletAppState();
        boolean success = stateManager.attach(bulletAppState);
        assert success;

        PhysicsSpace result = bulletAppState.getPhysicsSpace();
        result.setAccuracy(0.008f);
        result.setGravity(new Vector3f(0f, -9.8f, 0f));
        result.setSolverNumIterations(15);

        return result;
    }

    /**
     * Initialization performed immediately after parsing the command-line
     * arguments.
     *
     * @param forceDialog true&rarr;force startup to show the JME settings
     * dialog, false&rarr; show the dialog only if persistent settings are
     * missing
     */
    private static void mainStartup(boolean forceDialog) {
        /*
         * Instantiate the application.
         */
        application = new MavDemo2();
        /*
         * Instantiate the display-settings screen.
         */
        String applicationName = "MavDemo2";
        DisplaySizeLimits dsl = new DisplaySizeLimits(
                1_280, 720, // min width, height
                2_048, 1_080 // max width, height
        );
        DisplaySettings displaySettings
                = new DisplaySettings(application, applicationName, dsl) {
            @Override
            protected void applyOverrides(AppSettings settings) {
                super.applyOverrides(settings);

                setForceDialog(forceDialog);
                settings.setGammaCorrection(true);
                settings.setVSync(true);
            }
        };
        displaySettingsScreen = new DsScreen(displaySettings);

        AppSettings appSettings = displaySettings.initialize();
        if (appSettings != null) {
            application.setSettings(appSettings);
            /*
             * Don't pause on lost focus.  This simplifies debugging by
             * permitting the application to run while minimized.
             */
            application.setPauseOnLostFocus(false);
            /*
             * If the settings dialog should be shown, it has already been shown
             * by DisplaySettings.initialize().
             */
            application.setShowSettings(false);

            if (displaySettings.isGammaCorrection()) {
                application.setNiftyColorsAsSrgb();
            }
            application.start();
            /*
             * ... and onward to MavDemo2.guiInitializeApplication()!
             */
        }
    }

    /**
     * Initialization performed during the first invocation of
     * {@link #simpleUpdate(float)}.
     */
    private void startup1() {
        logger.info("");
        /*
         * Disable the JME statistic displays.
         * These can be re-enabled by pressing the F5 hotkey.
         */
        setDisplayFps(false);
        setDisplayStatView(false);

        MyCamera.setYTangent(cam, 2f);
    }
}
