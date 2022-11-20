package com.jayfella.jme.vehicle.niftydemo.view;

import com.github.stephengold.garrett.AffixedCamera;
import com.github.stephengold.garrett.CameraSignal;
import com.github.stephengold.garrett.OrbitCamera;
import com.github.stephengold.garrett.Target;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.Vehicles;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
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
     * controller for Dash mode
     */
    private static AffixedCamera dash;
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
     * Configure the camera controllers and main camera during startup.
     */
    public static void configure() {
        MavDemo2 app = MavDemo2.getApplication();
        FlyByCamera flyCam = app.getFlyByCamera();
        flyCam.setEnabled(false);

        AppStateManager stateManager = app.getStateManager();
        Camera camera = app.getCamera();
        SignalTracker tracker = app.getSignals();
        ObstructionFilter obstructionFilter = new ObstructionFilter();

        chase = new OrbitCamera("chase", camera, tracker);
        float lagSeconds = 0.5f;
        chase.setAzimuthTau(lagSeconds);
        chase.setObstructionFilter(obstructionFilter);
        float lowerAngle = 0.2f;
        float upperAngle = 1.7f;
        chase.setPoleExclusionAngles(upperAngle, lowerAngle);

        chase.setSignalName(CameraSignal.Back, "FLYCAM_Backward");
        chase.setSignalName(CameraSignal.DragToOrbit, "cameraDrag");
        chase.setSignalName(CameraSignal.Forward, "FLYCAM_Forward");
        chase.setSignalName(CameraSignal.OrbitDown, "FLYCAM_Lower");
        chase.setSignalName(CameraSignal.OrbitUp, "FLYCAM_Rise");
        chase.setSignalName(CameraSignal.Xray, "cameraXray");
        chase.setSignalName(CameraSignal.ZoomIn, "cameraZoomIn");
        chase.setSignalName(CameraSignal.ZoomOut, "cameraZoomOut");
        boolean success = stateManager.attach(chase);
        assert success;

        dash = new AffixedCamera("dash", camera, tracker);
        dash.setSignalName(CameraSignal.ZoomIn, "cameraZoomIn");
        dash.setSignalName(CameraSignal.ZoomOut, "cameraZoomOut");
        success = stateManager.attach(dash);
        assert success;

        orbit = new OrbitCamera("orbit", camera, tracker);
        orbit.setObstructionFilter(obstructionFilter);
        orbit.setSignalName(CameraSignal.Back, "FLYCAM_Backward");
        orbit.setSignalName(CameraSignal.DragToOrbit, "cameraDrag");
        orbit.setSignalName(CameraSignal.Forward, "FLYCAM_Forward");
        orbit.setSignalName(CameraSignal.OrbitCcw, "cameraOrbitCcw");
        orbit.setSignalName(CameraSignal.OrbitCw, "cameraOrbitCw");
        orbit.setSignalName(CameraSignal.OrbitDown, "FLYCAM_Lower");
        orbit.setSignalName(CameraSignal.OrbitUp, "FLYCAM_Rise");
        orbit.setSignalName(CameraSignal.Xray, "cameraXray");
        orbit.setSignalName(CameraSignal.ZoomIn, "cameraZoomIn");
        orbit.setSignalName(CameraSignal.ZoomOut, "cameraZoomOut");
        success = stateManager.attach(orbit);
        assert success;

        setDesiredMode(CameraMode.Orbit);
    }

    /**
     * Disable all camera controllers.
     */
    public static void disableAll() {
        chase.setEnabled(false);
        dash.setEnabled(false);
        orbit.setEnabled(false);
    }

    /**
     * Determine the target for ChaseCam/OrbitCam.
     *
     * @return the pre-existing instance, or null if none
     */
    public static Vehicle findTarget() {
        DemoState demoState = MavDemo2.getDemoState();
        Vehicles vehicles = demoState.getVehicles();
        Vehicle result = vehicles.getSelected();

        return result;
    }

    /**
     * Determine the CameraMode to apply during the next update.
     *
     * @return an enum value
     */
    public static CameraMode getMode() {
        return desiredMode;
    }

    /**
     * Switch to the next applicable camera controller.
     */
    public static void next() {
        switch (desiredMode) {
            case Chase:
                desiredMode = CameraMode.Orbit;
                break;
            case Dash:
                desiredMode = CameraMode.Chase;
                break;
            case Orbit:
                desiredMode = CameraMode.Dash;
                break;
            default:
                throw new IllegalStateException("mode = " + desiredMode);
        }
    }

    /**
     * Set the camera's field of view to the default.
     */
    public static void resetFov() {
        Camera camera = MavDemo2.getApplication().getCamera();
        MyCamera.setYTangent(camera, 1f);
    }

    /**
     * Move the camera to a convenient position.
     */
    public static void resetOffset() {
        DemoState demoState = MavDemo2.getDemoState();
        Vehicle vehicle = demoState.getVehicles().getSelected();
        if (activeController == chase) {
            /*
             * Locate the camera 20 wu behind and 5 wu above
             * the selected vehicle.
             */
            Vector3f offset = vehicle.forwardDirection(null);
            offset.multLocal(-20f);
            offset.y += 5f;
            chase.setOffset(offset);

        } else if (activeController == orbit) {
            World world = demoState.getWorld();
            world.resetCameraPosition();

            orbit.setPreferredRange(5f);
        }
    }

    /**
     * Alter the current mode, with changes taking effect during the next
     * update.
     *
     * @param mode the desired mode
     */
    public static void setDesiredMode(CameraMode mode) {
        Validate.nonNull(mode, "mode");
        desiredMode = mode;
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
            case Dash:
                newController = dash;
                break;
            case Orbit:
                newController = orbit;
                break;
            default:
                throw new IllegalStateException("mode = " + desiredMode);
        }

        Vehicle newVehicle = findTarget();
        if (newController instanceof AffixedCamera) {
            AffixedCamera affixedCamera = (AffixedCamera) newController;
            PhysicsRigidBody body = affixedCamera.getRigidBody();
            PhysicsRigidBody newBody = newVehicle.getVehicleControl();
            if (body != newBody) {
                affixedCamera.setRigidBody(newBody);

                Vector3f offset = new Vector3f();
                newVehicle.locateDashCam(offset);
                affixedCamera.setOffset(offset);
            }
        } else if (newController instanceof OrbitCamera) {
            OrbitCamera orbitCamera = (OrbitCamera) newController;
            Object targetEntity = getTarget(orbitCamera);
            if (targetEntity != newVehicle) {
                retarget(orbitCamera, newVehicle);
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
     * Access the target for chasing/orbiting behavior.
     *
     * @param orbitCam the camera controller (not null)
     * @return the pre-existing instance, or null if none
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
     * Reconfigure the specified camera controller for the specified Vehicle.
     *
     * @param orbitCamera the camera control to reconfigure (not null)
     * @param targetVehicle the desired target (not null)
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
