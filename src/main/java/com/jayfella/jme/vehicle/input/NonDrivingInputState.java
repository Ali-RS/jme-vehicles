package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.view.CameraController;
import com.jayfella.jme.vehicle.view.CameraSignal;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;
import jme3utilities.minie.FilterAll;

/**
 * An InputMode to manage camera controllers. TODO rename CameraMode
 */
public class NonDrivingInputState extends InputMode {
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
    final public static Logger logger
            = Logger.getLogger(NonDrivingInputState.class.getName());
    // *************************************************************************
    // fields

    final private CameraController activeCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode.
     */
    public NonDrivingInputState() {
        super("Camera Mode", F_CAMERA_RESET_FOV, F_CAMERA_RESET_OFFSET,
                F_CAMVIEW);

        Camera cam = Main.getApplication().getCamera();
        SignalMode signalMode = Main.findAppState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();

        float rearBias = 0f;
        FilterAll filter = new FilterAll(true);
        activeCam = new ChaseCamera(cam, signalTracker, ChaseOption.FreeOrbit,
                rearBias, filter);
        for (CameraSignal function : CameraSignal.values()) {
            String signalName = function.toString();
            activeCam.setSignalName(function, signalName);
        }
        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                MyCamera.setYTangent(cam, 1f);
            }
        }, F_CAMERA_RESET_FOV);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                resetCameraOffset();
            }
        }, F_CAMERA_RESET_OFFSET);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter which Vehicle is associated with the camera.
     *
     * @param newVehicle the vehicle to associate (not null)
     */
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");

        Camera cam = getApplication().getCamera();
        MyCamera.setYTangent(cam, 1f);
        Main.getWorld().resetCameraPosition();
        activeCam.setVehicle(newVehicle);
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
        activeCam.detach();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        Camera cam = getApplication().getCamera();
        MyCamera.setYTangent(cam, 1f);
        Main.getWorld().resetCameraPosition();

        Vehicle vehicle = Main.getVehicle();
        activeCam.setVehicle(vehicle);

        activeCam.attach();
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
        activeCam.update(tpf);
    }
    // *************************************************************************
    // private methods

    private void resetCameraOffset() {
        if (activeCam instanceof ChaseCamera) {
            /*
             * Locate the camera 20 wu behind and 5 wu above the target vehicle.
             */
            Vector3f offset = Main.getVehicle().forwardDirection(null);
            offset.multLocal(-20f);
            offset.y += 5f;

            ChaseCamera orbitCam = (ChaseCamera) activeCam;
            orbitCam.setOffset(offset);
        }
    }
}
