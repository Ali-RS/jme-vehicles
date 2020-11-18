package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.gui.ReturnToMenuClickCommand;
import com.jayfella.jme.vehicle.view.*;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.*;
import java.util.logging.Logger;
import jme3utilities.debug.Dumper;
import jme3utilities.minie.PhysicsDumper;

public class KeyboardVehicleInputState
        extends BaseAppState
        implements StateFunctionListener {
    // *************************************************************************
    // constants and loggers

    final private float maxSteeringAngle = 1f; // radians
    final private float returnRate = 2f; // radians per second
    final private float turnRate = 0.5f; // radians per second

    public static final String G_VEHICLE = "GROUP_VEHICLE";
    /**
     * keyboard function IDs
     */
    private static final FunctionId F_CAMVIEW
            = new FunctionId(G_VEHICLE, "Camera View");
    private static final FunctionId F_DUMP_PHYSICS
            = new FunctionId(G_VEHICLE, "Dump Physics");
    private static final FunctionId F_DUMP_VIEWPORT
            = new FunctionId(G_VEHICLE, "Dump Viewport");
    private static final FunctionId F_HANDBRAKE
            = new FunctionId(G_VEHICLE, "Vehicle Handbrake");
    private static final FunctionId F_HORN
            = new FunctionId(G_VEHICLE, "Vehicle Horn");
    private static final FunctionId F_MOVE
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
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(KeyboardVehicleInputState.class.getName());
    // *************************************************************************
    // fields

    private boolean accelerating, braking, reversing;
    private boolean turningLeft, turningRight;

    private float steeringAngle = 0f;

    private InputMapper inputMapper;
    private final Vehicle vehicle;
    private VehicleCamera activeCam;
    private VehicleCamView currentCam = VehicleCamView.FirstPerson;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an input state to control the specified Vehicle.
     *
     * @param vehicle the Vehicle to control (not null)
     */
    public KeyboardVehicleInputState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    // *************************************************************************
    // BaseAppState methods

    @Override
    protected void cleanup(Application app) {
        /*
         * Select the 1st person camera, since it doesn't have any keyboard
         * mappings to be cleaned up.
         */
        setCamera(VehicleCamView.FirstPerson);

        inputMapper.removeMapping(F_START_ENGINE, KeyInput.KEY_Y);

        inputMapper.removeMapping(F_MOVE, KeyInput.KEY_W);
        inputMapper.removeMapping(F_MOVE, InputState.Negative, KeyInput.KEY_S);

        inputMapper.removeMapping(F_TURN_LEFT, KeyInput.KEY_A);
        inputMapper.removeMapping(F_TURN_RIGHT, KeyInput.KEY_D);

        inputMapper.removeMapping(F_REVERSE, KeyInput.KEY_E);
        inputMapper.removeMapping(F_HANDBRAKE, KeyInput.KEY_SPACE);
        inputMapper.removeMapping(F_RESET, KeyInput.KEY_R);
        inputMapper.removeMapping(F_CAMVIEW, KeyInput.KEY_F5);
        inputMapper.removeMapping(F_HORN, KeyInput.KEY_H);

        inputMapper.removeMapping(F_DUMP_PHYSICS, KeyInput.KEY_O);
        inputMapper.removeMapping(F_DUMP_VIEWPORT, KeyInput.KEY_P);
        inputMapper.removeMapping(F_PAUSE, KeyInput.KEY_PAUSE);
        inputMapper.removeMapping(F_PAUSE, KeyInput.KEY_PERIOD);
        inputMapper.removeMapping(F_RETURN, KeyInput.KEY_ESCAPE);
        inputMapper.removeMapping(F_SCREEN_SHOT, KeyInput.KEY_F12);

        inputMapper.removeStateListener(this,
                F_START_ENGINE, F_MOVE, F_TURN_LEFT, F_TURN_RIGHT,
                F_REVERSE, F_HANDBRAKE, F_RESET,
                F_HORN, F_CAMVIEW, F_DUMP_PHYSICS, F_DUMP_VIEWPORT, F_PAUSE,
                F_RETURN, F_SCREEN_SHOT
        );
    }

    @Override
    protected void initialize(Application app) {
        inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.map(F_START_ENGINE, KeyInput.KEY_Y);

        inputMapper.map(F_MOVE, KeyInput.KEY_W);
        inputMapper.map(F_MOVE, InputState.Negative, KeyInput.KEY_S);

        inputMapper.map(F_TURN_LEFT, KeyInput.KEY_A);
        inputMapper.map(F_TURN_RIGHT, KeyInput.KEY_D);

        inputMapper.map(F_REVERSE, KeyInput.KEY_E);
        inputMapper.map(F_HANDBRAKE, KeyInput.KEY_Q);
        inputMapper.map(F_RESET, KeyInput.KEY_R);
        inputMapper.map(F_CAMVIEW, KeyInput.KEY_F5);
        inputMapper.map(F_HORN, KeyInput.KEY_H);

        inputMapper.map(F_DUMP_PHYSICS, KeyInput.KEY_O);
        inputMapper.map(F_DUMP_VIEWPORT, KeyInput.KEY_P);
        inputMapper.map(F_PAUSE, KeyInput.KEY_PAUSE);
        inputMapper.map(F_PAUSE, KeyInput.KEY_PERIOD);
        inputMapper.map(F_RETURN, KeyInput.KEY_ESCAPE);
        // Some Linux window managers block SYSRQ/PrtSc, so we map F12 instead.
        inputMapper.map(F_SCREEN_SHOT, KeyInput.KEY_F12);

        inputMapper.addStateListener(this,
                F_START_ENGINE, F_MOVE, F_TURN_LEFT, F_TURN_RIGHT,
                F_REVERSE, F_HANDBRAKE, F_RESET,
                F_HORN, F_CAMVIEW, F_DUMP_PHYSICS, F_DUMP_VIEWPORT, F_PAUSE,
                F_RETURN, F_SCREEN_SHOT
        );

        setCamera(currentCam);
    }

    @Override
    protected void onDisable() {
        inputMapper.deactivateGroup(G_VEHICLE);
    }

    @Override
    protected void onEnable() {
        inputMapper.activateGroup(G_VEHICLE);
    }

    @Override
    public void update(float tpf) {
        updateTurn(tpf);
        updateMovement(tpf);

        activeCam.update(tpf);

        //if (vehicle.getDriver() != null) {
        //vehicle.getDriver().getPlayerNode().setLocalTranslation(vehicle.getLocation());
        //}
    }
    // *************************************************************************
    // StateFunctionListener methods

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {
        boolean pressed = (value == InputState.Positive);
        DriverHud driverHud = getStateManager().getState(DriverHud.class);

        if (func == F_HORN) {
            vehicle.setHornInput(0, pressed);

        } else if (func == F_START_ENGINE && !pressed) {
            driverHud.toggleEngineStarted();

        } else if (func == F_MOVE) {
            if (value == InputState.Positive) {
                accelerating = true;
            } else if (value == InputState.Negative) {
                braking = true;
            } else {
                accelerating = false;
                braking = false;

                // we are coasting. engine braking comes into force.
                // braking applies to the wheels that are connected to the engine.
            }

        } else if (func == F_REVERSE) {
            reversing = pressed;

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

        } else if (func == F_DUMP_PHYSICS && pressed) {
            BulletAppState bas
                    = getStateManager().getState(BulletAppState.class);
            new PhysicsDumper().dump(bas);

        } else if (func == F_DUMP_VIEWPORT && pressed) {
            ViewPort vp = getApplication().getViewPort();
            new Dumper().setDumpShadow(true).dump(vp);

        } else if (func == F_PAUSE && pressed) {
            driverHud.togglePhysicsPaused();

        } else if (func == F_RETURN && !pressed) {
            // can't use InputState.Positive for this purpose
            ReturnToMenuClickCommand.returnToMenu(vehicle);

        } else if (func == F_SCREEN_SHOT && pressed) {
            ScreenshotAppState screenshotAppState
                    = getStateManager().getState(ScreenshotAppState.class);
            screenshotAppState.takeScreenshot();

        } else if (func == F_CAMVIEW && pressed) {
            currentCam = currentCam.next();
            setCamera(currentCam);
        }
    }
    // *************************************************************************
    // private methods

    private void setCamera(VehicleCamView camView) {
        if (activeCam != null) {
            activeCam.detach();
        }

        Camera cam = getApplication().getCamera();
        switch (camView) {
            case FirstPerson:
                activeCam = new VehicleFirstPersonCamera(vehicle, cam);
                break;

            case ThirdPerson:
                activeCam = new VehicleThirdPersonCamera(vehicle, cam);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown Camera View: " + camView);
        }

        activeCam.attach();
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

        if (reversing) {
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
