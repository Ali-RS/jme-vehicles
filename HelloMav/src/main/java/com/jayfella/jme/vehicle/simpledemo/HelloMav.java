package com.jayfella.jme.vehicle.simpledemo;

import com.github.stephengold.garrett.ChaseOption;
import com.github.stephengold.garrett.OrbitCamera;
import com.github.stephengold.garrett.Target;
import com.jayfella.jme.vehicle.ChunkManager;
import com.jayfella.jme.vehicle.GlobalAudio;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.skies.AnimatedDaySky;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.SignalTracker;

/**
 * A SimpleApplication to illustrate some basic features of More Advanced
 * Vehicles.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloMav extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(HelloMav.class.getName());
    /**
     * names for the 3 input signals used to control the Vehicle
     */
    final private static String forwardSignalName = "forward";
    final private static String leftSignalName = "left";
    final private static String rightSignalName = "right";
    // *************************************************************************
    // fields

    /**
     * dummy control for the global audio volume
     */
    final private GlobalAudio globalAudio = new GlobalAudio() {
        @Override
        public float effectiveVolume() {
            return 0.1f;
        }
    };
    /**
     * track which of the named input signals are active
     */
    final private SignalTracker signalTracker = new SignalTracker();
    /**
     * Vehicle that's being driven
     */
    final private Vehicle vehicle = new HoverTank();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloMav application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String... args) {
        /*
         * Mute the chatty loggers found in some imported packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        AppSettings appSettings = new AppSettings(true);
        appSettings.setResolution(1280, 720);
        appSettings.setVSync(true);

        HelloMav application = new HelloMav();
        application.setSettings(appSettings);
        application.setShowSettings(false);
        application.start();
    }
    // *************************************************************************
    // SimpleApplication methods

    /**
     * Initialize the HelloMav application.
     */
    @Override
    public void simpleInitApp() {
        Sky.setApplication(this);
        Sky.initialize();

        stateManager.attach(new BulletAppState());
        stateManager.attach(new ChunkManager());

        World world = new Mountains();
        world.load(assetManager);
        world.attach(this, rootNode);

        Sky sky = new AnimatedDaySky();
        sky.load(assetManager);
        sky.addToWorld(world);

        vehicle.load(assetManager);
        vehicle.addToWorld(world, globalAudio);

        setupCamera();
        setupInput();
    }

    /**
     * Callback invoked once per frame, to convert input signals to
     * vehicle-control signals.
     *
     * @param unused the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float unused) {
        float strength;
        if (signalTracker.test(forwardSignalName)) {
            strength = 1f;
        } else {
            strength = 0f;
        }
        vehicle.setAccelerateSignal(strength);

        float steerAngle = 0f;
        if (signalTracker.test(leftSignalName)) {
            steerAngle += 0.5f;
        }
        if (signalTracker.test(rightSignalName)) {
            steerAngle -= 0.5f;
        }
        vehicle.steer(steerAngle);
    }
    // *************************************************************************
    // private methods

    /**
     * Initialize the default Camera and its controller.
     */
    private void setupCamera() {
        cam.setLocation(new Vector3f(291f, 12f, 2_075f));
        cam.setRotation(new Quaternion(0f, 0.9987554f, -0.05f, 0f));
        flyCam.setEnabled(false);

        OrbitCamera cameraController = new OrbitCamera(cam, signalTracker);
        cameraController.setChaseOption(ChaseOption.StrictFollow);
        Target cameraTarget = new Target() {
            @Override
            public Vector3f forwardDirection(Vector3f storeResult) {
                Vector3f result = vehicle.forwardDirection(storeResult);
                return result;
            }

            @Override
            public PhysicsCollisionObject getTargetPco() {
                VehicleControl result = vehicle.getVehicleControl();
                return result;
            }

            @Override
            public Vector3f locateTarget(Vector3f storeResult) {
                Vector3f result = vehicle.locateTarget(1f, storeResult);
                return result;
            }
        };
        cameraController.setTarget(cameraTarget);
        cameraController.setEnabled(true);
        stateManager.attach(cameraController);
    }

    private void setupInput() {
        /*
         * Initialize the 3 input signals.
         */
        signalTracker.add(forwardSignalName);
        signalTracker.add(leftSignalName);
        signalTracker.add(rightSignalName);
        /*
         * The "W" key activates the "forward" input signal.
         */
        ActionListener forwardListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean keyPressed, float tpf) {
                signalTracker.setActive(forwardSignalName, 0, keyPressed);
            }
        };
        String forwardAction = "Forward";
        inputManager.addListener(forwardListener, forwardAction);
        KeyTrigger trigger = new KeyTrigger(KeyInput.KEY_W);
        inputManager.addMapping(forwardAction, trigger);
        /*
         * The "A" key activates the "left" input signal.
         */
        ActionListener leftListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean keyPressed, float tpf) {
                signalTracker.setActive(leftSignalName, 0, keyPressed);
            }
        };
        String leftAction = "Left";
        inputManager.addListener(leftListener, leftAction);
        trigger = new KeyTrigger(KeyInput.KEY_A);
        inputManager.addMapping(leftAction, trigger);
        /*
         * The "D" key activates the "right" input signal.
         */
        ActionListener rightListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean keyPressed, float tpf) {
                signalTracker.setActive(rightSignalName, 0, keyPressed);
            }
        };
        String rightAction = "Right";
        inputManager.addListener(rightListener, rightAction);
        trigger = new KeyTrigger(KeyInput.KEY_D);
        inputManager.addMapping(rightAction, trigger);
    }
}
