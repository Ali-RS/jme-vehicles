package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.gui.MainMenu;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.view.CameraController;
import com.jayfella.jme.vehicle.view.CameraSignal;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jayfella.jme.vehicle.view.DashCamera;
import com.jayfella.jme.vehicle.view.VehicleCamView;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.minie.FilterAll;

/**
 * An InputMode that's enabled while the user is driving a Vehicle. It uses 2
 * camera modes: one for the dash camera and one for the chase camera.
 */
public class DrivingInputMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_FORWARD
            = new FunctionId("Vehicle Move");
    final public static FunctionId F_HORN
            = new FunctionId("Vehicle Horn");
    final public static FunctionId F_MAIN_BRAKE
            = new FunctionId("Vehicle Footbrake");
    final public static FunctionId F_PARKING_BRAKE
            = new FunctionId("Vehicle Parking Brake");
    final public static FunctionId F_RESET
            = new FunctionId("Vehicle Reset");
    final public static FunctionId F_RETURN
            = new FunctionId("Return to Main Menu");
    final public static FunctionId F_REVERSE
            = new FunctionId("Vehicle reverse");
    final public static FunctionId F_START_ENGINE
            = new FunctionId("Vehicle Start Engine");
    final public static FunctionId F_TURN_LEFT
            = new FunctionId("Vehicle Turn Left");
    final public static FunctionId F_TURN_RIGHT
            = new FunctionId("Vehicle Turn Right");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(DrivingInputMode.class.getName());
    // *************************************************************************
    // fields

    final private float maxSteeringAngle = 1f; // radians
    final private float returnRate = 2f; // radians per second
    final private float turnRate = 0.5f; // radians per second

    private boolean accelerating, mainBrake, parkingBrake;
    private boolean turningLeft, turningRight;

    private float steeringAngle = 0f;

    private VehicleCamView cameraMode = VehicleCamView.ChaseCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode to drive the selected Vehicle.
     */
    public DrivingInputMode() {
        super("Driving Mode", F_FORWARD, F_MAIN_BRAKE, F_PARKING_BRAKE, F_RESET,
                F_RETURN, F_REVERSE, F_START_ENGINE, F_TURN_LEFT, F_TURN_RIGHT);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                accelerating = true;
            } else {
                accelerating = false;
            }
        }, F_FORWARD);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                mainBrake = true;
            } else {
                mainBrake = false;
            }
        }, F_MAIN_BRAKE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                parkingBrake = true;
            } else {
                parkingBrake = false;
            }
        }, F_PARKING_BRAKE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                resetVehicle();
            }
        }, F_RESET);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                returnToMainMenu();
            }
        }, F_RETURN);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            GearBox gearBox = Main.getVehicle().getGearBox();
            if (inputState == InputState.Positive) {
                gearBox.setReversing(true);
            } else {
                gearBox.setReversing(false);
            }
        }, F_REVERSE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                Main.findAppState(DriverHud.class)
                        .toggleEngineStarted();
            }
        }, F_START_ENGINE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                turningLeft = true;
            } else {
                turningLeft = false;
            }
        }, F_TURN_LEFT);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                turningRight = true;
            } else {
                turningRight = false;
            }
        }, F_TURN_RIGHT);
    }
    // *************************************************************************
    // public methods

    /**
     * Advance to the next camera mode.
     */
    public void nextCameraMode() {
        cameraMode = cameraMode.next();
        setCamera(cameraMode);
    }

    /**
     * Stop driving, reload the vehicle, and return to the main menu.
     */
    public void returnToMainMenu() {
        AppStateManager stateManager = getStateManager();
        setEnabled(false);

        EnginePowerGraphState enginePowerGraphState
                = Main.findAppState(EnginePowerGraphState.class);
        stateManager.detach(enginePowerGraphState);

        TireDataState tireDataState = Main.findAppState(TireDataState.class);
        stateManager.detach(tireDataState);

        VehicleEditorState vehicleEditorState
                = Main.findAppState(VehicleEditorState.class);
        stateManager.detach(vehicleEditorState);

        DebugTabState debugTabState = Main.findAppState(DebugTabState.class);
        stateManager.detach(debugTabState);

        DriverHud hud = Main.findAppState(DriverHud.class);
        hud.setEnabled(false);

        Vehicle vehicle = Main.getVehicle();
        Vehicle newVehicle;
        try {
            newVehicle = vehicle.getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new RuntimeException(exception);
        }
        newVehicle.load();
        Main.getApplication().setVehicle(newVehicle);

        stateManager.attach(new MainMenu());
        NonDrivingInputState cameraState
                = Main.findAppState(NonDrivingInputState.class);
        cameraState.orbit();
    }
    // *************************************************************************
    // InputMode methods

    /**
     * Callback invoked whenever this InputMode becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();
        setCamera(cameraMode);
    }

    /**
     * Callback to update this InputMode, invoked once per frame when the mode
     * is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        updateTurn(tpf);
        updateBrakeAndAccelerate();

        SignalMode signalMode = Main.findAppState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();
        boolean requested = signalTracker.test(SignalMode.F_HORN1.getId());
        Main.getVehicle().setHornStatus(requested);
    }
    // *************************************************************************
    // private methods

    private void resetVehicle() {
        // TODO make this more reliable

        Vehicle vehicle = Main.getVehicle();
        VehicleControl control = vehicle.getVehicleControl();
        float[] angles = new float[3];
        control.getPhysicsRotation().toAngles(angles);

        Quaternion newRotation = new Quaternion();
        newRotation.fromAngles(0f, angles[1], 0f);
        control.setPhysicsRotation(newRotation);

        control.setAngularVelocity(Vector3f.ZERO);
        control.setLinearVelocity(Vector3f.ZERO);
    }

    private void setCamera(VehicleCamView camView) { // TODO rename setCameraMode, rename arg
        NonDrivingInputState cameraInputMode
                = Main.findAppState(NonDrivingInputState.class);
        CameraController activeCam = cameraInputMode.getActiveCamera(); // TODO rename activeCamera

        Camera cam = getApplication().getCamera(); // TODO rename camera
        MyCamera.setYTangent(cam, 1f);

        SignalMode signalMode = Main.findAppState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();

        switch (camView) {
            case ChaseCam:
                float rearBias = 1f;
                FilterAll obstructionFilter = new FilterAll(true);
                ChaseCamera chaseCam = new ChaseCamera(cam, signalTracker,
                        ChaseOption.StrictChase, rearBias, obstructionFilter);
                activeCam = chaseCam;
                for (CameraSignal function : CameraSignal.values()) {
                    String signalName = function.toString();
                    chaseCam.setSignalName(function, signalName);
                }
                break;

            case DashCam:
                Vehicle vehicle = Main.getVehicle();
                DashCamera dashCam
                        = new DashCamera(vehicle, cam, signalTracker);
                activeCam = dashCam;
                dashCam.setSignalName(CameraSignal.ZoomIn,
                        CameraSignal.ZoomIn.toString());
                dashCam.setSignalName(CameraSignal.ZoomOut,
                        CameraSignal.ZoomOut.toString());
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown Camera View: " + camView);
        }

        cameraInputMode.setActiveCamera(activeCam);
    }

    private void updateBrakeAndAccelerate() {
        Vehicle vehicle = Main.getVehicle();
        /*
         * Update the brake control signals.
         */
        float main = mainBrake ? 1f : 0f;
        float parking = parkingBrake ? 1f : 0f;
        vehicle.setBrakeSignals(main, parking);
        /*
         * Update the "accelerate" control signal.
         */
        float kph = vehicle.getSpeed(SpeedUnit.KPH);
        GearBox gearBox = vehicle.getGearBox();

        float acceleration = 0f;
        boolean isEngineRunning = vehicle.getEngine().isRunning();
        if (isEngineRunning && accelerating) {
            float maxKph = gearBox.maxForwardSpeed(SpeedUnit.KPH);
            if (kph < maxKph) {
                acceleration = 1f;
            }
        }

        if (isEngineRunning && gearBox.isInReverse()) {
            float maxKph = gearBox.maxReverseSpeed(SpeedUnit.KPH);
            if (kph > maxKph) {
                acceleration = -1f;
            } else {
                acceleration = 0f;
            }
        }
        vehicle.setAccelerateSignal(acceleration);
    }

    private void updateTurn(float tpf) {
        if (turningLeft && !turningRight && steeringAngle >= 0f) {
            // turn more to the left
            steeringAngle += turnRate * tpf;
            steeringAngle = Math.min(steeringAngle, maxSteeringAngle);

        } else if (turningRight && !turningLeft && steeringAngle <= 0f) {
            // turn more to the right
            steeringAngle -= turnRate * tpf;
            steeringAngle = Math.max(steeringAngle, -maxSteeringAngle);

        } else if (steeringAngle > 0f) {
            // return from turning left
            steeringAngle -= returnRate * tpf;
            steeringAngle = Math.max(steeringAngle, 0f);

        } else if (steeringAngle < 0f) {
            // return from turning right
            steeringAngle += returnRate * tpf;
            steeringAngle = Math.min(steeringAngle, 0f);
        }

        Main.getVehicle().steer(steeringAngle);
    }
}
