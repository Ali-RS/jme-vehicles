package com.jayfella.jme.vehicle;

import com.atr.jme.font.asset.TrueTypeLoader;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.examples.worlds.Racetrack;
import com.jayfella.jme.vehicle.gui.CameraNameState;
import com.jayfella.jme.vehicle.gui.CompassState;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.gui.GearNameState;
import com.jayfella.jme.vehicle.gui.MainMenu;
import com.jayfella.jme.vehicle.gui.PhysicsHud;
import com.jayfella.jme.vehicle.input.DrivingInputState;
import com.jayfella.jme.vehicle.input.DumpInputState;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jayfella.jme.vehicle.input.PhysicsMode;
import com.jayfella.jme.vehicle.input.ScreenshotMode;
import com.jayfella.jme.vehicle.input.SignalMode;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickConnectionListener;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.input.Button;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyCamera;
import jme3utilities.MyString;

public class Main extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Main.class.getName());
    // *************************************************************************
    // fields

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
    /**
     * selected World (not null)
     */
    private static World world = new Racetrack();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a SimpleApplication without FlyCam or debug keys.
     */
    private Main() {
        super(
                new AudioListenerState(),
                new ConstantVerifierState(),
                new LoadingState(),
                new StatsAppState()
        );
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Callback from LoadingState when it has finished warming up the AssetCache
     * and initializing Lemur.
     */
    void doneLoading() {
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        Sky.initialize();
        attachAllToScene();

        stateManager.attachAll(
                new CameraNameState(),
                new CompassState(),
                new DriverHud(),
                new GearNameState(),
                new MainMenu(),
                new PhysicsHud()
        );
        //stateManager.attach(new VehiclePointsState());
        /*
         * Attach input modes.
         */
        activateDumpMode();
        activatePhysicsMode();
        activateScreenshotMode();
        activateSignalMode();
        activateCameraMode();
        attachDrivingMode(); // needs the SignalTracker of the SignalMode
        /*
         * The dash camera sits close to the bodywork, so set the near clipping
         * plane accordingly.
         */
        float near = 0.1f;
        float far = 1_800f;
        MyCamera.setNearFar(cam, near, far);

        MyCamera.setYTangent(cam, 1f);
        world.resetCameraPosition();
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
     * Access the selected Vehicle from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Vehicle getVehicle() {
        assert vehicle != null;
        return vehicle;
    }

    /**
     * Access the selected World from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static World getWorld() {
        assert world != null;
        return world;
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

    /**
     * Replace the current World with a new one.
     *
     * @param newWorld the desired world (not null, loaded)
     */
    public void setWorld(World newWorld) {
        sky.detachFromScene();
        vehicle.detachFromScene();
        world.detachFromScene();

        world = newWorld;
        attachAllToScene();
        /*
         * Re-use the existing input state with the new Vehicle instance.
         */
        NonDrivingInputState inputState
                = Main.findAppState(NonDrivingInputState.class);
        inputState.setVehicle(vehicle);
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
    }
    // *************************************************************************
    // private methods

    /**
     * Configure, attach, and enable a new camera InputMode.
     */
    private static void activateCameraMode() {
        NonDrivingInputState mode = new NonDrivingInputState();
        mode.assign(NonDrivingInputState.F_CAMERA_RESET_FOV, KeyInput.KEY_NUMPAD6);
        mode.assign(NonDrivingInputState.F_CAMERA_RESET_OFFSET, Button.MOUSE_BUTTON2);
        mode.assign(NonDrivingInputState.F_CAMERA_RESET_OFFSET, KeyInput.KEY_NUMPAD5);
        mode.assign(NonDrivingInputState.F_CAMVIEW, KeyInput.KEY_F5);

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(mode);
        mode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new dump InputMode.
     */
    private static void activateDumpMode() {
        DumpInputState mode = new DumpInputState();
        mode.assign(DumpInputState.F_DUMP_CAMERA, KeyInput.KEY_C);
        mode.assign(DumpInputState.F_DUMP_PHYSICS, KeyInput.KEY_O);
        mode.assign(DumpInputState.F_DUMP_VIEWPORT, KeyInput.KEY_P);

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(mode);
        mode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new physics InputMode.
     */
    private static void activatePhysicsMode() {
        PhysicsMode mode = new PhysicsMode();
        mode.assign(PhysicsMode.F_PAUSE,
                KeyInput.KEY_PAUSE, KeyInput.KEY_PERIOD);
        mode.assign(PhysicsMode.F_SINGLE_STEP, KeyInput.KEY_COMMA);

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(mode);
        mode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new screenshot InputMode.
     */
    private static void activateScreenshotMode() {
        ScreenshotMode mode = new ScreenshotMode();

        // Some Linux window managers block SYSRQ/PrtSc, so map F12 instead.
        mode.assign(ScreenshotMode.F_SCREEN_SHOT, KeyInput.KEY_F12);

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(mode);
        mode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new signal InputMode.
     */
    private static void activateSignalMode() {
        SignalMode mode = new SignalMode();
        mode.assign(SignalMode.F_CAMERA_BACK1, KeyInput.KEY_NUMPAD1);
        mode.assign(SignalMode.F_CAMERA_DOWN1, KeyInput.KEY_NUMPAD2);
        mode.assign(SignalMode.F_CAMERA_DRAG_TO_ORBIT1, Button.MOUSE_BUTTON3);
        mode.assign(SignalMode.F_CAMERA_FORWARD1, KeyInput.KEY_NUMPAD7);
        mode.assign(SignalMode.F_CAMERA_UP1, KeyInput.KEY_NUMPAD8);
        mode.assign(SignalMode.F_CAMERA_XRAY1, KeyInput.KEY_NUMPAD0);
        mode.assign(SignalMode.F_CAMERA_ZOOM_IN1, KeyInput.KEY_NUMPAD9);
        mode.assign(SignalMode.F_CAMERA_ZOOM_OUT1, KeyInput.KEY_NUMPAD3);
        mode.assign(SignalMode.F_HORN1, KeyInput.KEY_H);

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(mode);
        mode.setEnabled(true);
    }

    /**
     * Attach the selected Sky, World, and Vehicle to the scene.
     */
    private void attachAllToScene() {
        sky.attachToScene(rootNode);
        world.attachToScene(rootNode);
        vehicle.attachToScene(rootNode);
    }

    /**
     * Configure and attach a new (disabled) driving InputMode.
     */
    private static void attachDrivingMode() {
        DrivingInputState mode = new DrivingInputState();
        mode.assign(NonDrivingInputState.F_CAMERA_RESET_FOV, KeyInput.KEY_NUMPAD6);
        mode.assign(NonDrivingInputState.F_CAMERA_RESET_OFFSET, Button.MOUSE_BUTTON2);
        mode.assign(NonDrivingInputState.F_CAMERA_RESET_OFFSET, KeyInput.KEY_NUMPAD5);
        mode.assign(NonDrivingInputState.F_CAMVIEW, KeyInput.KEY_F5);
        mode.assign(DrivingInputState.F_FORWARD, KeyInput.KEY_W);
        mode.assign(DrivingInputState.F_MAIN_BRAKE, KeyInput.KEY_S);
        mode.assign(DrivingInputState.F_PARKING_BRAKE, KeyInput.KEY_SPACE);
        mode.assign(DrivingInputState.F_RESET, KeyInput.KEY_R);
        mode.assign(DrivingInputState.F_RETURN, KeyInput.KEY_ESCAPE);
        mode.assign(DrivingInputState.F_REVERSE, KeyInput.KEY_E);
        mode.assign(DrivingInputState.F_START_ENGINE, KeyInput.KEY_Y);
        mode.assign(DrivingInputState.F_TURN_LEFT, KeyInput.KEY_A);
        mode.assign(DrivingInputState.F_TURN_RIGHT, KeyInput.KEY_D);

        AppStateManager manager = getApplication().getStateManager();
        manager.attach(mode);
    }
}
