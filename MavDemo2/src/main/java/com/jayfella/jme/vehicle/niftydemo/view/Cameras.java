package com.jayfella.jme.vehicle.niftydemo.view;

import com.github.stephengold.garrett.CameraSignal;
import com.github.stephengold.garrett.ChaseOption;
import com.github.stephengold.garrett.OrbitCamera;
import com.github.stephengold.garrett.Target;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.Vehicles;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.logging.Logger;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;

/**
 * Utility class to manage camera controllers for the MavDemo2 application. All
 * methods should be static.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Cameras {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Cameras.class.getName());
    // *************************************************************************
    // fields

    /**
     * active controller, or null if none
     */
    private static AppState activeController;
    /**
     * mode to apply during the next update
     */
    private static CameraMode desiredMode;
    /**
     * controller for Chase mode
     */
    private static OrbitCamera chase;
    /**
     * controller for Orbit mode
     */
    private static OrbitCamera orbit;
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Cameras() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Configure the cameras during startup.
     */
    public static void configure() {
        MavDemo2 app = MavDemo2.getApplication();
        FlyByCamera flyCam = app.getFlyByCamera();
        flyCam.setEnabled(false);

        AppStateManager stateManager = app.getStateManager();
        Camera camera = app.getCamera();
        SignalTracker tracker = app.getSignals();
        ObstructionFilter obstructionFilter = new ObstructionFilter();

        chase = new OrbitCamera(camera, tracker);
        chase.setChaseOption(ChaseOption.StrictFollow);
        chase.setObstructionFilter(obstructionFilter);
        chase.setSignalName(CameraSignal.Back, "FLYCAM_Backward");
        chase.setSignalName(CameraSignal.DragToOrbit, "cameraDrag");
        chase.setSignalName(CameraSignal.Forward, "FLYCAM_Forward");
        chase.setSignalName(CameraSignal.OrbitDown, "FLYCAM_Lower");
        chase.setSignalName(CameraSignal.OrbitUp, "FLYCAM_Rise");
        chase.setSignalName(CameraSignal.Xray, "cameraXray");
        boolean success = stateManager.attach(chase);
        assert success;

        orbit = new OrbitCamera(camera, tracker);
        orbit.setChaseOption(ChaseOption.FreeOrbit);
        orbit.setObstructionFilter(obstructionFilter);
        orbit.setSignalName(CameraSignal.Back, "FLYCAM_Backward");
        orbit.setSignalName(CameraSignal.DragToOrbit, "cameraDrag");
        orbit.setSignalName(CameraSignal.Forward, "FLYCAM_Forward");
        orbit.setSignalName(CameraSignal.OrbitDown, "FLYCAM_Lower");
        orbit.setSignalName(CameraSignal.OrbitUp, "FLYCAM_Rise");
        orbit.setSignalName(CameraSignal.Xray, "cameraXray");
        success = stateManager.attach(orbit);
        assert success;

        setNextMode(CameraMode.Orbit);
    }

    /**
     * Disable all camera controllers.
     */
    public static void disableAll() {
        chase.setEnabled(false);
        orbit.setEnabled(false);
    }

    /**
     * Determine the target entity for ChaseCam/OrbitCam.
     *
     * @return the pre-existing instance, or null if none
     */
    public static Vehicle findTarget() {
        DemoState gameState = MavDemo2.getDemoState();
        Vehicles vehicles = gameState.getVehicles();
        Vehicle result = vehicles.getSelected();

        return result;
    }

    /**
     * TODO
     *
     * @param nextMode
     */
    public static void setNextMode(CameraMode nextMode) {
        Validate.nonNull(nextMode, "next mode");
        desiredMode = nextMode;
    }

    /**
     * TODO
     */
    public static void toggle() {
        // TODO
    }

    /**
     * Ensure that an appropriate camera controller is enabled.
     */
    public static void update() {
        AppState newController;
        switch (desiredMode) {
            case Chase:
                newController = chase;
                break;
            case Orbit:
                newController = orbit;
                break;
            default:
                throw new IllegalStateException("mode = " + desiredMode);
        }

        if (newController instanceof OrbitCamera) {
            OrbitCamera orbitCamera = (OrbitCamera) newController;
            Object targetEntity = getTarget(orbitCamera);
            Vehicle newTargetVehicle = findTarget();
            if (targetEntity != newTargetVehicle) {
                retarget(orbitCamera, newTargetVehicle);
            }
        }

        if (newController != activeController) {
            if (activeController != null) {
                activeController.setEnabled(false);
            }
            newController.setEnabled(true);
            activeController = newController;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * TODO
     *
     * @param orbitCam
     * @return
     */
    private static Object getTarget(OrbitCamera orbitCam) {
        Object result = null;

        Target target = orbitCam.getTarget();
        if (target != null) {
            PhysicsCollisionObject targetPco = target.getTargetPco();
            result = targetPco.getApplicationData();
        }

        return result;
    }

    /**
     * TODO
     *
     * @param orbitCamera
     * @param targetVehicle
     */
    private static void retarget(OrbitCamera orbitCamera,
            Vehicle targetVehicle) {
        float rearBias;
        if (orbitCamera == chase) {
            rearBias = 1f;
        } else if (orbitCamera == orbit) {
            rearBias = 0f;
        } else {
            throw new IllegalArgumentException();
        }

        Target target = new Target() {
            @Override
            public Vector3f forwardDirection(Vector3f storeResult) {
                Vector3f result = targetVehicle.forwardDirection(storeResult);
                return result;
            }

            @Override
            public PhysicsCollisionObject getTargetPco() {
                VehicleControl result = targetVehicle.getVehicleControl();
                return result;
            }

            @Override
            public Vector3f locateTarget(Vector3f storeResult) {
                Vector3f result
                        = targetVehicle.locateTarget(rearBias, storeResult);
                return result;
            }
        };
        orbitCamera.setTarget(target);
    }
}
