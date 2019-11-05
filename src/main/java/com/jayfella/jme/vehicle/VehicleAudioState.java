package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Gear;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;

public class VehicleAudioState extends BaseAppState {

    private final Vehicle vehicle;

    public VehicleAudioState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void playEngineSound() {
        vehicle.getEngine().getEngineAudio().play();
    }

    public void stopEngineSound() {
        vehicle.getEngine().getEngineAudio().stop();
    }

    public void playHornSound() {
        if (vehicle.getHornAudio() != null) {
            vehicle.getHornAudio().play();
        }
    }

    @Override
    protected void initialize(Application app) {

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        // vehicle.getEngine().getEngineAudio().play();
    }

    @Override
    protected void onDisable() {
        stopEngineSound();
    }

    private float lastValue;

    @Override
    public void update(float tpf) {

        float speed = vehicle.getSpeed(Vehicle.SpeedUnit.KMH);
        Gear gear = vehicle.getGearBox().getActiveGear();

        // float value = unInterpolateLinear(speed, gear.getStart(), gear.getEnd());
        float value = vehicle.getEngine().getRevs();

        // add a bit of interpolation for when we change gears.
        // this effectively stops the sound from jumping from full revs to low revs and vice versa.
        // maybe a smoothstep or some kind of exponent would work better here.
        value = FastMath.interpolateLinear(tpf * 5.0f, lastValue, value);

        float pitch = FastMath.clamp(value + 1.0f, 1.0f, 2.0f);
        vehicle.getEngine().getEngineAudio().setPitch(pitch);

        lastValue = value;
    }

    private float unInterpolateLinear(float value, float min, float max) {
        return (value - min) / (max - min);
    }


}
