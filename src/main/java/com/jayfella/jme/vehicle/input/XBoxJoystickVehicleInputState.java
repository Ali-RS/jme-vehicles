package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.DashCamera;
import com.jayfella.jme.vehicle.view.VehicleCamView;
import com.jayfella.jme.vehicle.view.VehicleCamera;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import jme3utilities.SignalTracker;
import jme3utilities.minie.FilterAll;

public class XBoxJoystickVehicleInputState extends BaseAppState implements StateFunctionListener, AnalogFunctionListener {

    public static final String G_VEHICLE = "GROUP_VEHICLE";

    private static final FunctionId F_START_ENGINE = new FunctionId(G_VEHICLE, "Vehicle Start Engine");

    private static final FunctionId F_MOVE_ANALOG = new FunctionId(G_VEHICLE, "Vehicle Move Analog");
    private static final FunctionId F_TURN_ANALOG = new FunctionId(G_VEHICLE, "Vehicle Turn Analog");

    private static final FunctionId F_REVERSE = new FunctionId(G_VEHICLE, "Vehicle reverse");
    private static final FunctionId F_HANDBRAKE = new FunctionId(G_VEHICLE, "Vehicle Handbrake");

    private static final FunctionId F_RESET = new FunctionId(G_VEHICLE, "Vehicle Reset");

    private static final FunctionId F_CAMVIEW = new FunctionId(G_VEHICLE, "Camera View");

    private static final FunctionId F_HORN = new FunctionId(G_VEHICLE, "Vehicle Horn");

    private final Vehicle vehicle;

    private InputMapper inputMapper;
    final private SignalTracker signalTracker;

    // private VehicleFirstPersonCamera firstPersonCam;
    private VehicleCamera activeCam;
    private VehicleCamView currentCam = VehicleCamView.DashCam;

    public XBoxJoystickVehicleInputState(Vehicle vehicle) {
        this.vehicle = vehicle;

        signalTracker = new SignalTracker();
        signalTracker.add("horn");
    }

    public InputMapper getInputMapper() {
        return inputMapper;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public VehicleCamera getActiveCam() {
        return activeCam;
    }

    @Override
    protected void initialize(Application app) {

        inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.map( F_START_ENGINE, KeyInput.KEY_Y );

        inputMapper.map( F_MOVE_ANALOG, Axis.JOYSTICK_RIGHT_TRIGGER);
        // inputMapper.map( F_MOVE_ANALOG, InputState.Negative, Axis.JOYSTICK_LEFT_TRIGGER );

        inputMapper.map( F_TURN_ANALOG, InputState.Negative, Axis.JOYSTICK_HAT_X );

        inputMapper.map( F_REVERSE, KeyInput.KEY_E );

        inputMapper.map( F_HANDBRAKE, KeyInput.KEY_Q );

        inputMapper.map( F_RESET, KeyInput.KEY_R );

        inputMapper.map( F_CAMVIEW, KeyInput.KEY_F5);

        inputMapper.map( F_HORN, KeyInput.KEY_H );

        inputMapper.addStateListener(this,
                F_START_ENGINE, F_REVERSE, F_HANDBRAKE, F_RESET,
                F_HORN,
                F_CAMVIEW
        );

        inputMapper.addAnalogListener(this, F_TURN_ANALOG, F_MOVE_ANALOG);

        // activeCam = new VehicleFirstPersonCamera(vehicle, app.getCamera());

        setCamera(currentCam);

    }

    @Override
    protected void cleanup(Application app) {

        inputMapper.removeMapping( F_START_ENGINE, KeyInput.KEY_Y );

        inputMapper.removeMapping( F_MOVE_ANALOG, InputState.Negative, Axis.JOYSTICK_HAT_Y);

        inputMapper.removeMapping( F_TURN_ANALOG, InputState.Negative, Axis.JOYSTICK_HAT_X );

        inputMapper.removeMapping( F_REVERSE, KeyInput.KEY_E );

        inputMapper.removeMapping( F_HANDBRAKE, KeyInput.KEY_SPACE );

        inputMapper.removeMapping( F_RESET, KeyInput.KEY_R );

        inputMapper.removeMapping( F_CAMVIEW, KeyInput.KEY_F5);

        inputMapper.removeMapping( F_HORN, KeyInput.KEY_H );

        inputMapper.removeStateListener(this,
                F_START_ENGINE, F_REVERSE, F_HANDBRAKE, F_RESET,
                F_HORN,
                F_CAMVIEW
        );

        inputMapper.removeAnalogListener(this, F_TURN_ANALOG, F_MOVE_ANALOG);
    }

    @Override
    protected void onEnable() {
        inputMapper.activateGroup( G_VEHICLE );
    }

    @Override
    protected void onDisable() {
        inputMapper.deactivateGroup( G_VEHICLE );
    }

    @Override
    public void update(float tpf) {
        //updateTurn(tpf);
        //updateMovement(tpf);

        boolean hornIsRequested = signalTracker.test("horn");
        vehicle.setHornStatus(hornIsRequested);

        activeCam.update(tpf);

    }

    boolean turningLeft, turningRight;

    private float maxSteerForce = 1.0f;
    private float steeringForce = 0;
    private float turnSpeed = 0.5f;

    boolean accelerating, braking, reversing;

    private void updateTurn(float tpf) {

        if (turningLeft) {
            steeringForce = Math.min(steeringForce + (tpf * turnSpeed), maxSteerForce);
            // vehicle.getVehicleControl().steer(steeringValue);
            //vehicle.steer(steeringForce);
            vehicle.steer(steeringForce);
        }
        else if (turningRight) {
            steeringForce = Math.max(steeringForce - (tpf * turnSpeed), -maxSteerForce);
            //vehicle.getVehicleControl().steer(steeringForce);
            vehicle.steer(steeringForce);
        }
        else {
            //steeringForce = 0;
            //vehicle.steer(steeringForce);
        }
        /*
        else {
            if (steeringForce < 0) {
                steeringForce = Math.min(steeringForce + tpf, 0);
            }
            else if (steeringForce > 0) {
                steeringForce = Math.max(steeringForce - tpf, 0);
            }
        }
        */


    }



    public void updateMovement(float tpf) {

        // do braking first so it doesn't override engineBraking.
        if (braking) {
            vehicle.brake(1);
        }
        else {
            vehicle.brake(0);
        }

        if (accelerating) {

            vehicle.removeEngineBraking();

            if (vehicle.getSpeed(Vehicle.SpeedUnit.KMH) < vehicle.getGearBox().getMaxSpeed(Vehicle.SpeedUnit.KMH)) {
                vehicle.accelerate(1);
            }
            else {
                vehicle.accelerate(0);
            }
        }
        else {
            if (!braking) {
                vehicle.applyEngineBraking();
            }

            //vehicle.accelerate(0);
        }

        if (reversing) {

            if (vehicle.getSpeed(Vehicle.SpeedUnit.KMH) > -40) {
                vehicle.accelerate(-1);
            }
            else {
                vehicle.accelerate(0);
            }

        }



    }

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {

        boolean pressed = value == InputState.Positive;

        if (func == F_HORN) {
            signalTracker.setActive("horn", KeyInput.KEY_H, pressed);
        }

        else if (func == F_START_ENGINE) {

            if (!pressed) {
                DriverHud hud = Main.findAppState(DriverHud.class);
                hud.toggleEngineStarted();
            }
        }

        else if (func == F_REVERSE) {
            reversing = value == InputState.Positive;
        }

        else if (func == F_HANDBRAKE) {

            // if (pressed) {
                // vehicle.setParkingBrakeApplied(!vehicle.isParkingBrakeApplied());
            // }
            // vehicle.setParkingBrakeApplied(pressed);
            vehicle.handbrake(pressed ? 1 : 0);

        }

        else if (func == F_RESET) {

            if (!pressed) {
                return;
            }

            float[] angles = new float[3];
            vehicle.getVehicleControl().getPhysicsRotation().toAngles(angles);

            Quaternion newRotation = new Quaternion().fromAngles(0, angles[1], 0);
            vehicle.getVehicleControl().setPhysicsRotation(newRotation);

            vehicle.getVehicleControl().setAngularVelocity(new Vector3f());
            vehicle.getVehicleControl().setLinearVelocity(new Vector3f());
        }


        if (func == F_CAMVIEW && value == InputState.Positive) {

            currentCam = currentCam.next();
            setCamera(currentCam);
        }

    }

    private float deadzone_x = 0.1f;
    private float deadzone_y = 0.1f;

    @Override
    public void valueActive(FunctionId func, double value, double tpf) {

        float val = (float) value;

        if (func == F_TURN_ANALOG) {
            vehicle.steer(val);
        }

        if (func == F_MOVE_ANALOG) {
            if (val > deadzone_x) {
                if (vehicle.getSpeed(Vehicle.SpeedUnit.KMH) < vehicle.getGearBox().getMaxSpeed(Vehicle.SpeedUnit.KMH)) {
                    vehicle.accelerate(val);
                    vehicle.brake(0);
                }
            }
            else if (val < -deadzone_y) {
                vehicle.accelerate(0);
                float brakeStrength = val * -1;
                vehicle.brake(brakeStrength);
            }
            else {
                vehicle.applyEngineBraking();
            }
        }

    }

    private void setCamera(VehicleCamView camView) {

        if (activeCam != null) {
            activeCam.detach();
        }

        Camera cam = getApplication().getCamera();
        switch (camView) {

            case DashCam: {
                activeCam = new DashCamera(vehicle,
                        getApplication().getCamera(), signalTracker);
                break;
            }
            case ChaseCam: {
                FilterAll obstructionFilter = new FilterAll(true);
                ChaseCamera oc = new ChaseCamera(vehicle, cam, signalTracker,
                        obstructionFilter);
                activeCam = oc;
                break;
            }

            default: throw new IllegalArgumentException("Unknown Camera View: " + camView);
        }

        activeCam.attach();

    }



}
