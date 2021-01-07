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
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import java.util.Set;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.minie.FilterAll;

/**
 * An AppState to handle input while driving a Vehicle. There are 2 camera
 * modes: one for the dash camera and one for the orbit camera.
 */
public class DrivingInputState
        extends BaseAppState
        implements StateFunctionListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DrivingInputState.class.getName());

    final public static String G_CAMERA = "GROUP_CAMERA";
    final public static String G_VEHICLE = "GROUP_VEHICLE";
    /**
     * Vehicle function IDs
     */
    final private static FunctionId F_CAMERA_RESET_FOV
            = new FunctionId(G_VEHICLE, "Camera Reset FOV");
    final private static FunctionId F_CAMVIEW
            = new FunctionId(G_VEHICLE, "Camera View");
    final private static FunctionId F_MAIN_BRAKE
            = new FunctionId(G_VEHICLE, "Vehicle Footbrake");
    final private static FunctionId F_PARKING_BRAKE
            = new FunctionId(G_VEHICLE, "Vehicle Parking Brake");
    final private static FunctionId F_HORN
            = new FunctionId(G_VEHICLE, "Vehicle Horn");
    final private static FunctionId F_FORWARD
            = new FunctionId(G_VEHICLE, "Vehicle Move");
    final private static FunctionId F_RESET
            = new FunctionId(G_VEHICLE, "Vehicle Reset");
    final private static FunctionId F_RETURN
            = new FunctionId(G_VEHICLE, "Return to Main Menu");
    final private static FunctionId F_REVERSE
            = new FunctionId(G_VEHICLE, "Vehicle reverse");
    final private static FunctionId F_START_ENGINE
            = new FunctionId(G_VEHICLE, "Vehicle Start Engine");
    final private static FunctionId F_TURN_LEFT
            = new FunctionId(G_VEHICLE, "Vehicle Turn Left");
    final private static FunctionId F_TURN_RIGHT
            = new FunctionId(G_VEHICLE, "Vehicle Turn Right");
    /**
     * ChaseCamera function IDs
     */
    final private static FunctionId F_CAMERA_RESET_OFFSET
            = new FunctionId(G_CAMERA, "Camera Reset Offset");
    // *************************************************************************
    // fields

    private CameraController activeCam;

    final private float maxSteeringAngle = 1f; // radians
    final private float returnRate = 2f; // radians per second
    final private float turnRate = 0.5f; // radians per second

    private boolean accelerating, mainBrake, parkingBrake;
    private boolean turningLeft, turningRight;

    private float steeringAngle = 0f;

    private InputMapper inputMapper;
    private VehicleCamView cameraMode = VehicleCamView.ChaseCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled input state to drive the selected Vehicle.
     */
    public DrivingInputState() {
        setEnabled(false);
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
        cameraState.setEnabled(true);
    }
    // *************************************************************************
    // BaseAppState methods

    @Override
    protected void cleanup(Application app) {
        /*
         * Remove all input mappings/listeners in G_CAMERA and G_VEHICLE.
         */
        Set<FunctionId> functions = inputMapper.getFunctionIds();
        for (FunctionId function : functions) {
            String group = function.getGroup();
            switch (group) {
                case G_CAMERA:
                case G_VEHICLE:
                    Set<InputMapper.Mapping> mappings
                            = inputMapper.getMappings(function);
                    for (InputMapper.Mapping mp : mappings) {
                        inputMapper.removeMapping(mp);
                    }
                    inputMapper.removeStateListener(this, function);
            }
        }
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.map(F_START_ENGINE, KeyInput.KEY_Y);

        inputMapper.map(F_FORWARD, KeyInput.KEY_W);
        inputMapper.map(F_MAIN_BRAKE, KeyInput.KEY_S);

        inputMapper.map(F_TURN_LEFT, KeyInput.KEY_A);
        inputMapper.map(F_TURN_RIGHT, KeyInput.KEY_D);

        inputMapper.map(F_REVERSE, KeyInput.KEY_E);
        inputMapper.map(F_PARKING_BRAKE, KeyInput.KEY_SPACE);
        inputMapper.map(F_RESET, KeyInput.KEY_R);
        inputMapper.map(F_CAMVIEW, KeyInput.KEY_F5);
        inputMapper.map(F_HORN, KeyInput.KEY_H);

        inputMapper.map(F_CAMERA_RESET_FOV, KeyInput.KEY_NUMPAD6);
        inputMapper.map(F_CAMERA_RESET_OFFSET, Button.MOUSE_BUTTON2);
        inputMapper.map(F_CAMERA_RESET_OFFSET, KeyInput.KEY_NUMPAD5);

        inputMapper.map(F_RETURN, KeyInput.KEY_ESCAPE);
        /*
         * Add listeners for all functions in G_CAMERA and G_VEHICLE.
         */
        Set<FunctionId> functions = inputMapper.getFunctionIds();
        for (FunctionId function : functions) {
            String group = function.getGroup();
            switch (group) {
                case G_CAMERA:
                case G_VEHICLE:
                    inputMapper.addStateListener(this, function);
            }
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        inputMapper.deactivateGroup(G_VEHICLE);
        activeCam.detach();
        activeCam = null;
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        inputMapper.activateGroup(G_VEHICLE);
        setCamera(cameraMode);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        updateTurn(tpf);
        updateBrakeAndAccelerate();

        activeCam.update(tpf);

        SignalMode signalMode = Main.findAppState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();
        boolean requested = signalTracker.test(SignalMode.F_HORN1.getId());
        Main.getVehicle().setHornStatus(requested);
    }
    // *************************************************************************
    // StateFunctionListener methods

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {
        boolean pressed = (value == InputState.Positive);
        DriverHud driverHud = Main.findAppState(DriverHud.class);
        Vehicle vehicle = Main.getVehicle();

        if (func == F_START_ENGINE && !pressed) {
            driverHud.toggleEngineStarted();

        } else if (func == F_FORWARD) {
            accelerating = pressed;

        } else if (func == F_MAIN_BRAKE) {
            mainBrake = pressed;

        } else if (func == F_REVERSE) {
            vehicle.getGearBox().setReversing(pressed);

        } else if (func == F_PARKING_BRAKE) {
            parkingBrake = pressed;

        } else if (func == F_TURN_LEFT) {
            turningLeft = pressed;

        } else if (func == F_TURN_RIGHT) {
            turningRight = pressed;

        } else if (func == F_RESET && pressed) { // TODO make this more reliable
            float[] angles = new float[3];
            vehicle.getVehicleControl().getPhysicsRotation().toAngles(angles);

            Quaternion newRotation = new Quaternion();
            newRotation.fromAngles(0f, angles[1], 0f);
            vehicle.getVehicleControl().setPhysicsRotation(newRotation);

            vehicle.getVehicleControl().setAngularVelocity(Vector3f.ZERO);
            vehicle.getVehicleControl().setLinearVelocity(Vector3f.ZERO);

        } else if (func == F_CAMERA_RESET_OFFSET && pressed) {
            resetCameraOffset();

        } else if (func == F_CAMERA_RESET_FOV && pressed) {
            Camera cam = getApplication().getCamera();
            MyCamera.setYTangent(cam, 1f);

        } else if (func == F_RETURN && !pressed) {
            // can't use InputState.Positive for this purpose
            returnToMainMenu();

        } else if (func == F_CAMVIEW && pressed) {
            nextCameraMode();
        }
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

            ChaseCamera chaseCam = (ChaseCamera) activeCam;
            chaseCam.setOffset(offset);
        }
    }

    private void setCamera(VehicleCamView camView) {
        if (activeCam != null) {
            activeCam.detach();
        }

        Camera cam = getApplication().getCamera();
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

        activeCam.attach();
        resetCameraOffset();
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
