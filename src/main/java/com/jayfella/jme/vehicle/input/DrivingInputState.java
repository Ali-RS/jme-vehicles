package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.gui.MainMenu;
import com.jayfella.jme.vehicle.view.CameraSignal;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jayfella.jme.vehicle.view.DashCamera;
import com.jayfella.jme.vehicle.view.VehicleCamView;
import com.jayfella.jme.vehicle.view.VehicleCamera;
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

    public static final String G_CAMERA = "GROUP_CAMERA";
    public static final String G_VEHICLE = "GROUP_VEHICLE";
    /**
     * Vehicle function IDs
     */
    private static final FunctionId F_CAMERA_RESET_FOV
            = new FunctionId(G_VEHICLE, "Camera Reset FOV");
    private static final FunctionId F_CAMERA_ZOOM_IN1
            = new FunctionId(G_VEHICLE, CameraSignal.ZoomIn.toString());
    private static final FunctionId F_CAMERA_ZOOM_OUT1
            = new FunctionId(G_VEHICLE, CameraSignal.ZoomOut.toString());
    private static final FunctionId F_CAMVIEW
            = new FunctionId(G_VEHICLE, "Camera View");
    private static final FunctionId F_FOOTBRAKE
            = new FunctionId(G_VEHICLE, "Vehicle Footbrake");
    private static final FunctionId F_HANDBRAKE
            = new FunctionId(G_VEHICLE, "Vehicle Handbrake");
    private static final FunctionId F_HORN
            = new FunctionId(G_VEHICLE, "Vehicle Horn");
    private static final FunctionId F_FORWARD
            = new FunctionId(G_VEHICLE, "Vehicle Move");
    private static final FunctionId F_PAUSE
            = new FunctionId(G_VEHICLE, "Pause Simulation");
    private static final FunctionId F_RESET
            = new FunctionId(G_VEHICLE, "Vehicle Reset");
    private static final FunctionId F_RETURN
            = new FunctionId(G_VEHICLE, "Return to Main Menu");
    private static final FunctionId F_REVERSE
            = new FunctionId(G_VEHICLE, "Vehicle reverse");
    private static final FunctionId F_SCREEN_SHOT
            = new FunctionId(G_VEHICLE, "ScreenShot");
    private static final FunctionId F_START_ENGINE
            = new FunctionId(G_VEHICLE, "Vehicle Start Engine");
    private static final FunctionId F_TURN_LEFT
            = new FunctionId(G_VEHICLE, "Vehicle Turn Left");
    private static final FunctionId F_TURN_RIGHT
            = new FunctionId(G_VEHICLE, "Vehicle Turn Right");
    /**
     * ChaseCamera function IDs
     */
    private static final FunctionId F_CAMERA_BACK1
            = new FunctionId(G_CAMERA, CameraSignal.Back.toString());
    private static final FunctionId F_CAMERA_DOWN1
            = new FunctionId(G_CAMERA, CameraSignal.OrbitDown.toString());
    private static final FunctionId F_CAMERA_DRAG_TO_ORBIT1
            = new FunctionId(G_CAMERA, CameraSignal.DragToOrbit.toString());
    private static final FunctionId F_CAMERA_FORWARD1
            = new FunctionId(G_CAMERA, CameraSignal.Forward.toString());
    private static final FunctionId F_CAMERA_RESET_OFFSET
            = new FunctionId(G_CAMERA, "Camera Reset Offset");
    private static final FunctionId F_CAMERA_UP1
            = new FunctionId(G_CAMERA, CameraSignal.OrbitUp.toString());
    private static final FunctionId F_CAMERA_XRAY1
            = new FunctionId(G_CAMERA, CameraSignal.Xray.toString());
    // *************************************************************************
    // fields

    final private float maxSteeringAngle = 1f; // radians
    final private float returnRate = 2f; // radians per second
    final private float turnRate = 0.5f; // radians per second

    private boolean accelerating, braking;
    private boolean turningLeft, turningRight;

    private float steeringAngle = 0f;

    private InputMapper inputMapper;
    final private SignalTracker signalTracker;
    private final Vehicle vehicle;
    private VehicleCamera activeCam;
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
        if (enginePowerGraphState != null) {
            stateManager.detach(enginePowerGraphState);
        }

        TireDataState tireDataState = Main.findAppState(TireDataState.class);
        if (tireDataState != null) {
            stateManager.detach(tireDataState);
        }

        VehicleEditorState vehicleEditorState
                = Main.findAppState(VehicleEditorState.class);
        if (vehicleEditorState != null) {
            stateManager.detach(vehicleEditorState);
        }

        DebugTabState debugTabState = Main.findAppState(DebugTabState.class);
        if (debugTabState != null) {
            stateManager.detach(debugTabState);
        }

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
        inputMapper.map(F_FOOTBRAKE, KeyInput.KEY_S);

        inputMapper.map(F_TURN_LEFT, KeyInput.KEY_A);
        inputMapper.map(F_TURN_RIGHT, KeyInput.KEY_D);

        inputMapper.map(F_REVERSE, KeyInput.KEY_E);
        inputMapper.map(F_HANDBRAKE, KeyInput.KEY_Q);
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

    @Override
    public void update(float tpf) {
        updateTurn(tpf);
        updateMovement(tpf);

        activeCam.update(tpf);

        boolean hornIsRequested = signalTracker.test("horn");
        vehicle.setHornStatus(hornIsRequested);

        //if (vehicle.getDriver() != null) {
        //vehicle.getDriver().getPlayerNode().setLocalTranslation(vehicle.getLocation());
        //}
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

        } else if (func == F_FOOTBRAKE) {
            braking = pressed;

        } else if (func == F_REVERSE) {
            vehicle.getGearBox().setReversing(pressed);

        } else if (func == F_HANDBRAKE) {
            // if (pressed) {
            // vehicle.setParkingBrakeApplied(!vehicle.isParkingBrakeApplied());
            // }
            // vehicle.setParkingBrakeApplied(pressed);
            vehicle.handbrake(pressed ? 1f : 0f);

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
            driverHud.togglePhysicsPaused();

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
                FilterAll obstructionFilter = new FilterAll(true);
                ChaseCamera chaseCam = new ChaseCamera(cam, signalTracker,
                        ChaseOption.StrictChase, obstructionFilter);
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

    private void updateMovement(float tpf) {
        // do braking first so it doesn't override engineBraking.
        if (braking) {
            vehicle.brake(1f);
        } else {
            vehicle.brake(0f);
        }

        if (accelerating) {
            vehicle.removeEngineBraking();

            float kph = vehicle.getSpeed(Vehicle.SpeedUnit.KMH);
            float maxKph
                    = vehicle.getGearBox().getMaxSpeed(Vehicle.SpeedUnit.KMH);
            if (kph < maxKph) {
                vehicle.accelerate(1f);
            } else {
                vehicle.accelerate(0f);
            }

        } else {
            if (!braking) {
                vehicle.applyEngineBraking();
            }

            vehicle.accelerate(0f);
        }

        if (vehicle.getGearBox().isReversing()) {
            if (vehicle.getSpeed(Vehicle.SpeedUnit.KMH) > -40f) {
                vehicle.accelerate(-1f);
            } else {
                vehicle.accelerate(0f);
            }
        }
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
