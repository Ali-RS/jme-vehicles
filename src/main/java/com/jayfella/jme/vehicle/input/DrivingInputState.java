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
import com.jayfella.jme.vehicle.gui.PhysicsHud;
import com.jayfella.jme.vehicle.view.CameraController;
import com.jayfella.jme.vehicle.view.CameraSignal;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jayfella.jme.vehicle.view.DashCamera;
import com.jayfella.jme.vehicle.view.VehicleCamView;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.ScreenshotAppState;
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
    final private static FunctionId F_CAMERA_ZOOM_IN1
            = new FunctionId(G_VEHICLE, CameraSignal.ZoomIn.toString());
    final private static FunctionId F_CAMERA_ZOOM_OUT1
            = new FunctionId(G_VEHICLE, CameraSignal.ZoomOut.toString());
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
    final private static FunctionId F_PAUSE
            = new FunctionId(G_VEHICLE, "Pause Simulation");
    final private static FunctionId F_RESET
            = new FunctionId(G_VEHICLE, "Vehicle Reset");
    final private static FunctionId F_RETURN
            = new FunctionId(G_VEHICLE, "Return to Main Menu");
    final private static FunctionId F_REVERSE
            = new FunctionId(G_VEHICLE, "Vehicle reverse");
    final private static FunctionId F_SCREEN_SHOT
            = new FunctionId(G_VEHICLE, "ScreenShot");
    final private static FunctionId F_START_ENGINE
            = new FunctionId(G_VEHICLE, "Vehicle Start Engine");
    final private static FunctionId F_TURN_LEFT
            = new FunctionId(G_VEHICLE, "Vehicle Turn Left");
    final private static FunctionId F_TURN_RIGHT
            = new FunctionId(G_VEHICLE, "Vehicle Turn Right");
    /**
     * ChaseCamera function IDs
     */
    final private static FunctionId F_CAMERA_BACK1
            = new FunctionId(G_CAMERA, CameraSignal.Back.toString());
    final private static FunctionId F_CAMERA_DOWN1
            = new FunctionId(G_CAMERA, CameraSignal.OrbitDown.toString());
    final private static FunctionId F_CAMERA_DRAG_TO_ORBIT1
            = new FunctionId(G_CAMERA, CameraSignal.DragToOrbit.toString());
    final private static FunctionId F_CAMERA_FORWARD1
            = new FunctionId(G_CAMERA, CameraSignal.Forward.toString());
    final private static FunctionId F_CAMERA_RESET_OFFSET
            = new FunctionId(G_CAMERA, "Camera Reset Offset");
    final private static FunctionId F_CAMERA_UP1
            = new FunctionId(G_CAMERA, CameraSignal.OrbitUp.toString());
    final private static FunctionId F_CAMERA_XRAY1
            = new FunctionId(G_CAMERA, CameraSignal.Xray.toString());
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
    final private SignalTracker signalTracker;
    final private Vehicle vehicle;
    private VehicleCamView cameraMode = VehicleCamView.ChaseCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an input state to drive the specified Vehicle.
     *
     * @param vehicle the Vehicle to drive (not null)
     */
    public DrivingInputState(Vehicle vehicle) {
        this.vehicle = vehicle;

        signalTracker = new SignalTracker();
        signalTracker.add("horn");
        for (CameraSignal function : CameraSignal.values()) {
            String signalName = function.toString();
            signalTracker.add(signalName);
        }
    }
    // *************************************************************************
    // public methods

    /**
     * Access the SignalTracker.
     *
     * @return the pre-existing instance (not null)
     */
    public SignalTracker getSignalTracker() {
        return signalTracker;
    }

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
        stateManager.detach(this);

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

        Vehicle newVehicle;
        try {
            newVehicle = vehicle.getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException exception) {
            throw new RuntimeException(exception);
        }
        newVehicle.load();
        Main.getApplication().setVehicle(newVehicle);

        stateManager.attach(new MainMenu());
        stateManager.attach(new NonDrivingInputState());

        Main.getEnvironment().resetCameraPosition();
    }
    // *************************************************************************
    // BaseAppState methods

    @Override
    protected void cleanup(Application app) {
        activeCam.detach();
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

        inputMapper.map(F_CAMERA_BACK1, KeyInput.KEY_NUMPAD1);
        inputMapper.map(F_CAMERA_DOWN1, KeyInput.KEY_NUMPAD2);
        inputMapper.map(F_CAMERA_DRAG_TO_ORBIT1, Button.MOUSE_BUTTON3);
        inputMapper.map(F_CAMERA_FORWARD1, KeyInput.KEY_NUMPAD7);
        inputMapper.map(F_CAMERA_RESET_FOV, KeyInput.KEY_NUMPAD6);
        inputMapper.map(F_CAMERA_RESET_OFFSET, Button.MOUSE_BUTTON2);
        inputMapper.map(F_CAMERA_RESET_OFFSET, KeyInput.KEY_NUMPAD5);
        inputMapper.map(F_CAMERA_UP1, KeyInput.KEY_NUMPAD8);
        inputMapper.map(F_CAMERA_XRAY1, KeyInput.KEY_NUMPAD0);
        inputMapper.map(F_CAMERA_ZOOM_IN1, KeyInput.KEY_NUMPAD9);
        inputMapper.map(F_CAMERA_ZOOM_OUT1, KeyInput.KEY_NUMPAD3);

        inputMapper.map(F_PAUSE, KeyInput.KEY_PAUSE);
        inputMapper.map(F_PAUSE, KeyInput.KEY_PERIOD);
        inputMapper.map(F_RETURN, KeyInput.KEY_ESCAPE);
        // Some Linux window managers block SYSRQ/PrtSc, so we map F12 instead.
        inputMapper.map(F_SCREEN_SHOT, KeyInput.KEY_F12);
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

        setCamera(cameraMode);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        inputMapper.deactivateGroup(G_VEHICLE);
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        inputMapper.activateGroup(G_VEHICLE);
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

        boolean hornIsRequested = signalTracker.test("horn");
        vehicle.setHornStatus(hornIsRequested);
    }
    // *************************************************************************
    // StateFunctionListener methods

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {
        boolean pressed = (value == InputState.Positive);
        DriverHud driverHud = Main.findAppState(DriverHud.class);

        if (func == F_HORN) {
            signalTracker.setActive("horn", KeyInput.KEY_H, pressed);

        } else if (func == F_START_ENGINE && !pressed) {
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

        } else if (func == F_RESET && pressed) {
            float[] angles = new float[3];
            vehicle.getVehicleControl().getPhysicsRotation().toAngles(angles);

            Quaternion newRotation = new Quaternion();
            newRotation.fromAngles(0f, angles[1], 0f);
            vehicle.getVehicleControl().setPhysicsRotation(newRotation);

            vehicle.getVehicleControl().setAngularVelocity(Vector3f.ZERO);
            vehicle.getVehicleControl().setLinearVelocity(Vector3f.ZERO);

        } else if (func == F_CAMERA_BACK1) {
            signalTracker.setActive(CameraSignal.Back.toString(), 1, pressed);

        } else if (func == F_CAMERA_DOWN1) {
            signalTracker.setActive(CameraSignal.OrbitDown.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_DRAG_TO_ORBIT1) {
            signalTracker.setActive(CameraSignal.DragToOrbit.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_FORWARD1) {
            signalTracker.setActive(CameraSignal.Forward.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_RESET_OFFSET && pressed) {
            resetCameraOffset();

        } else if (func == F_CAMERA_RESET_FOV && pressed) {
            Camera cam = getApplication().getCamera();
            MyCamera.setYTangent(cam, 1f);

        } else if (func == F_CAMERA_UP1) {
            signalTracker.setActive(CameraSignal.OrbitUp.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_XRAY1) {
            signalTracker.setActive(CameraSignal.Xray.toString(), 1, pressed);

        } else if (func == F_CAMERA_ZOOM_IN1) {
            signalTracker.setActive(CameraSignal.ZoomIn.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_ZOOM_OUT1) {
            signalTracker.setActive(CameraSignal.ZoomOut.toString(),
                    1, pressed);

        } else if (func == F_PAUSE && pressed) {
            PhysicsHud physicsState = Main.findAppState(PhysicsHud.class);
            physicsState.togglePhysicsPaused();

        } else if (func == F_RETURN && !pressed) {
            // can't use InputState.Positive for this purpose
            returnToMainMenu();

        } else if (func == F_SCREEN_SHOT && pressed) {
            ScreenshotAppState screenshotAppState
                    = Main.findAppState(ScreenshotAppState.class);
            screenshotAppState.takeScreenshot();

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
            Vector3f offset = vehicle.forwardDirection(null);
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
        /*
         * Update the brake control signals.
         */
        float main = mainBrake ? 1f : 0f;
        float parking = parkingBrake ? 1f : 0f;
        vehicle.setBrakeSignals(main, parking);
        /*
         * Update the "accelerate" control signal.
         */
        boolean isEngineRunning = vehicle.getEngine().isRunning();
        float kph = vehicle.getSpeed(SpeedUnit.KPH);
        float acceleration = 0f;
        if (isEngineRunning && accelerating) {
            float maxKph = vehicle.getGearBox().getMaxSpeed(SpeedUnit.KPH);
            if (kph < maxKph) {
                acceleration = 1f;
            }
        }

        if (isEngineRunning && vehicle.getGearBox().isInReverse()) {
            if (kph > -40f) { // TODO maxKph based on engine and gearbox
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

        vehicle.steer(steeringAngle);
    }
}
