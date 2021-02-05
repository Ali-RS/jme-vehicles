package com.jayfella.jme.vehicle.simpledemo;

import com.github.stephengold.garrett.ChaseOption;
import com.github.stephengold.garrett.OrbitCamera;
import com.github.stephengold.garrett.Target;
import com.jayfella.jme.vehicle.ChunkManager;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.skies.AnimatedDaySky;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound2;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.gui.CompassState;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.gui.TachometerState;
import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import java.util.logging.Level;
import jme3utilities.Heart;
import jme3utilities.SignalTracker;

/**
 * A single-class example of More Advanced Vehicles.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloMav extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * steering-control parameters
     */
    final private static float maxSteerAngle = 1f;
    final private static float turnRate = 0.5f;
    /**
     * names for the 3 input signals used to control the Vehicle
     */
    final private static String forwardSignalName = "forward";
    final private static String leftSignalName = "left";
    final private static String rightSignalName = "right";
    // *************************************************************************
    // fields

    /**
     * steer angle from the previous update
     */
    private float steerAngle = 0f;
    /**
     * track which of the named input signals are active
     */
    final private SignalTracker signalTracker = new SignalTracker();
    /**
     * calculate camera velocity
     */
    final private Vector3f cameraVelocity = new Vector3f();
    final private Vector3f oldCameraLocation = new Vector3f();
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
        stateManager.attachAll(
                new BulletAppState(),
                new ChunkManager(),
                new CompassState()
        );

        World world = new Mountains();

        BulletAppState bulletAppState
                = getStateManager().getState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        world.attach(this, rootNode, physicsSpace);

        vehicle.addToWorld(world, () -> {
            return 1f;
        });
        Engine engine = vehicle.getEngine();
        engine.setRunning(true);
        stateManager.attach(new SpeedometerState(vehicle, SpeedUnit.MPH));
        stateManager.attach(new TachometerState(engine));

        Sound engineSound = new EngineSound2();
        engineSound.load(assetManager);
        engine.setSound(engineSound);

        Sky.setApplication(this);
        Sky.initialize();
        new AnimatedDaySky().addToWorld(world);

        initCamera();

        mapKeyToSignal(KeyInput.KEY_W, forwardSignalName);
        mapKeyToSignal(KeyInput.KEY_A, leftSignalName);
        mapKeyToSignal(KeyInput.KEY_D, rightSignalName);
    }

    /**
     * Callback invoked once per frame, to convert input signals to
     * vehicle-control signals.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        float strength;
        if (signalTracker.test(forwardSignalName)) {
            strength = 1f;
        } else {
            strength = 0f;
        }
        vehicle.setAccelerateSignal(strength);

        updateTurn(tpf);
        /*
         * For 3-D audio, move the Listener with the default Camera.
         */
        listener.setRotation(cam.getRotation());
        Vector3f newLocation = cam.getLocation(); // alias
        listener.setLocation(newLocation);
        if (tpf > 0f) {
            newLocation.subtract(oldCameraLocation, cameraVelocity);
            cameraVelocity.divide(tpf);
            listener.setVelocity(cameraVelocity);
            oldCameraLocation.set(newLocation);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Initialize the default Camera and its controller.
     */
    private void initCamera() {
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

    /**
     * Add an input mapping that causes the SignalTracker to track the specified
     * key.
     *
     * @param key the key to be tracked
     * @param signalName name for the input signal (not null)
     */
    private void mapKeyToSignal(int key, String signalName) {
        signalTracker.add(signalName);

        ActionListener rightListener = (action, keyPressed, tpf) -> {
            signalTracker.setActive(signalName, 0, keyPressed);
        };
        String action = "signal " + signalName;
        inputManager.addListener(rightListener, action);

        KeyTrigger trigger = new KeyTrigger(key);
        inputManager.addMapping(action, trigger);
    }

    /**
     * Implement progressive steering, for better control and more fun.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    private void updateTurn(float tpf) {
        if (signalTracker.test(leftSignalName)) {
            steerAngle += tpf * turnRate;
            steerAngle = Math.min(steerAngle, maxSteerAngle);

        } else if (signalTracker.test(rightSignalName)) {
            steerAngle -= tpf * turnRate;
            steerAngle = Math.max(steerAngle, -maxSteerAngle);

        } else {
            steerAngle = 0f;
        }

        vehicle.steer(steerAngle);
    }
}
