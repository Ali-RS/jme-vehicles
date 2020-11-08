package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.view.*;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.*;

public class KeyboardVehicleInputState extends BaseAppState implements StateFunctionListener {

    public static final String G_VEHICLE = "GROUP_VEHICLE";

    private static final FunctionId F_START_ENGINE = new FunctionId(G_VEHICLE, "Vehicle Start Engine");

    private static final FunctionId F_MOVE = new FunctionId(G_VEHICLE, "Vehicle Move");
    private static final FunctionId F_TURN = new FunctionId(G_VEHICLE, "Vehicle Turn");

    private static final FunctionId F_REVERSE = new FunctionId(G_VEHICLE, "Vehicle reverse");
    private static final FunctionId F_HANDBRAKE = new FunctionId(G_VEHICLE, "Vehicle Handbrake");

    private static final FunctionId F_RESET = new FunctionId(G_VEHICLE, "Vehicle Reset");

    private static final FunctionId F_CAMVIEW = new FunctionId(G_VEHICLE, "Camera View");

    private static final FunctionId F_HORN = new FunctionId(G_VEHICLE, "Vehicle Horn");

    private final Vehicle vehicle;

    private InputMapper inputMapper;

    // private VehicleFirstPersonCamera firstPersonCam;
    private VehicleCamera activeCam;
    private VehicleCamView currentCam = VehicleCamView.FirstPerson;

    public KeyboardVehicleInputState(Vehicle vehicle) {
        this.vehicle = vehicle;
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

        inputMapper.map( F_MOVE, KeyInput.KEY_W );
        inputMapper.map( F_MOVE, InputState.Negative, KeyInput.KEY_S );

        inputMapper.map( F_TURN, KeyInput.KEY_A );
        inputMapper.map( F_TURN, InputState.Negative, KeyInput.KEY_D );

        inputMapper.map( F_REVERSE, KeyInput.KEY_E );

        inputMapper.map( F_HANDBRAKE, KeyInput.KEY_Q );

        inputMapper.map( F_RESET, KeyInput.KEY_R );

        inputMapper.map( F_CAMVIEW, KeyInput.KEY_F5);

        inputMapper.map( F_HORN, KeyInput.KEY_H );

        inputMapper.addStateListener(this,
                F_START_ENGINE, F_MOVE, F_TURN, F_REVERSE, F_HANDBRAKE, F_RESET,
                F_HORN,
                F_CAMVIEW
        );

        // activeCam = new VehicleFirstPersonCamera(vehicle, app.getCamera());

        setCamera(currentCam);

    }

    @Override
    protected void cleanup(Application app) {

        inputMapper.removeMapping( F_START_ENGINE, KeyInput.KEY_Y );

        inputMapper.removeMapping( F_MOVE, KeyInput.KEY_W );
        inputMapper.removeMapping( F_MOVE, InputState.Negative, KeyInput.KEY_S );

        inputMapper.removeMapping( F_TURN, KeyInput.KEY_A );
        inputMapper.removeMapping( F_TURN, InputState.Negative, KeyInput.KEY_D );

        inputMapper.removeMapping( F_REVERSE, KeyInput.KEY_E );

        inputMapper.removeMapping( F_HANDBRAKE, KeyInput.KEY_SPACE );

        inputMapper.removeMapping( F_RESET, KeyInput.KEY_R );

        inputMapper.removeMapping( F_CAMVIEW, KeyInput.KEY_F5);

        inputMapper.removeMapping( F_HORN, KeyInput.KEY_H );

        inputMapper.removeStateListener(this,
                F_START_ENGINE, F_MOVE, F_TURN, F_REVERSE, F_HANDBRAKE, F_RESET,
                F_HORN,
                F_CAMVIEW
        );

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
        updateTurn(tpf);
        updateMovement(tpf);

        activeCam.update(tpf);

        //if (vehicle.getDriver() != null) {
            //vehicle.getDriver().getPlayerNode().setLocalTranslation(vehicle.getLocation());
        //}

    }

    boolean turningLeft, turningRight;

    private float maxSteerForce = 1.0f;
    private float steeringForce = 0;
    private float turnSpeed = 0.5f;

    boolean accelerating, braking, reversing;

    private void updateTurn(float tpf) {

        if (turningLeft) {
            steeringForce = Math.min(steeringForce + (tpf * turnSpeed), maxSteerForce);
            vehicle.steer(steeringForce);
        }
        else if (turningRight) {
            steeringForce = Math.max(steeringForce - (tpf * turnSpeed), -maxSteerForce);
            vehicle.steer(steeringForce);
        }
        else {
            steeringForce = 0;
            vehicle.steer(steeringForce);
        }
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

            vehicle.accelerate(0);
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

            if (pressed) {
                // vehicle.setEngineStarted(!vehicle.getEngine().isStarted());
                vehicle.pressHorn();
            }
        }

        else if (func == F_START_ENGINE) {

            if (!pressed) {
                vehicle.setEngineStarted(!vehicle.getEngine().isStarted());
            }
        }

        else if (func == F_MOVE) {

            if (value == InputState.Positive) {
                accelerating = true;
            }
            else if (value == InputState.Negative) {
                braking = true;
            }
            else {
                accelerating = false;
                braking = false;

                // we are coasting. engine braking comes into force.
                // braking applies to the wheels that are connected to the engine.

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

        else if (func == F_TURN) {

            if (value == InputState.Positive) {
                turningLeft = true;
                turningRight = false;

            } else if (value == InputState.Negative) {
                turningRight = true;
                turningLeft = false;
            } else {

                turningLeft = false;
                turningRight = false;

            }
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

    private void setCamera(VehicleCamView camView) {

        if (activeCam != null) {
            activeCam.detach();
        }

        switch (camView) {

            case FirstPerson: {
                activeCam = new VehicleFirstPersonCamera(vehicle, getApplication().getCamera());
                break;
            }
            case ThirdPerson: {
                activeCam = new VehicleThirdPersonCamera(vehicle, getApplication().getCamera());
                break;
            }

            default: throw new IllegalArgumentException("Unknown Camera View: " + camView);
        }

        activeCam.attach();

    }



}
