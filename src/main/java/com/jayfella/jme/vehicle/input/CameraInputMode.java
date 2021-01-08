package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.view.CameraController;
import com.jayfella.jme.vehicle.view.CameraSignal;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;
import jme3utilities.minie.FilterAll;

/**
 * An InputMode to manage the Camera.
 */
public class CameraInputMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_CAMERA_RESET_FOV
            = new FunctionId("Camera Reset FOV");
    final public static FunctionId F_CAMERA_RESET_OFFSET
            = new FunctionId("Camera Reset Offset");
    final public static FunctionId F_CAMVIEW
            = new FunctionId("Camera View");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(CameraInputMode.class.getName());
    // *************************************************************************
    // fields

    /**
     * active controller
     */
    private CameraController activeController;
    /**
     * orbit controller (active when not driving a Vehicle)
     */
    final private ChaseCamera orbitCamera;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode.
     */
    public CameraInputMode() {
        super("Camera Mode", F_CAMERA_RESET_FOV, F_CAMERA_RESET_OFFSET,
                F_CAMVIEW);

        Camera camera = Main.getApplication().getCamera();
        SignalMode signalMode = Main.findAppState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();

        float rearBias = 0f;
        FilterAll filter = new FilterAll(true);
        orbitCamera = new ChaseCamera(camera, signalTracker,
                ChaseOption.FreeOrbit, rearBias, filter);
        for (CameraSignal function : CameraSignal.values()) {
            String signalName = function.toString();
            orbitCamera.setSignalName(function, signalName);
        }
        activeController = orbitCamera;

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                MyCamera.setYTangent(camera, 1f);
            }
        }, F_CAMERA_RESET_FOV);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                resetCameraOffset();
            }
        }, F_CAMERA_RESET_OFFSET);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                nextCameraMode();
            }
        }, F_CAMVIEW);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the active CameraController.
     *
     * @return the pre-existing instance (not null)
     */
    public CameraController getActiveCamera() {
        return activeController;
    }

    /**
     * Switch to the built-in orbit camera.
     */
    public void orbit() {
        setActiveCamera(orbitCamera);
    }

    /**
     * Alter which CameraController is active.
     *
     * @param newActive (not null, alias created)
     */
    public void setActiveCamera(CameraController newActive) {
        activeController.detach();
        activeController = newActive;
        activeController.attach();

        Camera camera = getApplication().getCamera();
        MyCamera.setYTangent(camera, 1f);

        resetCameraOffset();
    }

    /**
     * Alter which Vehicle is associated with the active camera.
     *
     * @param newVehicle the vehicle to associate (not null)
     */
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");

        Camera camera = getApplication().getCamera();
        MyCamera.setYTangent(camera, 1f);
        Main.getWorld().resetCameraPosition();
        activeController.setVehicle(newVehicle);
    }
    // *************************************************************************
    // InputMode methods

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        super.onDisable();
        activeController.detach();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        activeController.attach();
        super.onEnable();
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);
        activeController.update(tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Cycle through the available camera modes.
     */
    private void nextCameraMode() {
        DrivingInputMode driving = Main.findAppState(DrivingInputMode.class);
        if (driving.isEnabled()) {
            driving.nextCameraMode();
        }
    }

    private void resetCameraOffset() {
        if (activeController instanceof ChaseCamera) {
            ChaseCamera chaseCam = (ChaseCamera) activeController;
            if (chaseCam.getChaseOption() == ChaseOption.StrictChase) {
                /*
                 * Locate the camera 20 wu behind and 5 wu above
                 * the target vehicle.
                 */
                Vector3f offset = Main.getVehicle().forwardDirection(null);
                offset.multLocal(-20f);
                offset.y += 5f;
                chaseCam.setOffset(offset);

            } else { // orbiting
                World world = Main.getWorld();
                world.resetCameraPosition();

                chaseCam.setPreferredRange(5f);

                Vehicle vehicle = Main.getVehicle();
                chaseCam.setVehicle(vehicle);
            }
        }
    }
}
