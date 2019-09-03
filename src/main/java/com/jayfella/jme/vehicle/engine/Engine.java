package com.jayfella.jme.vehicle.engine;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;

public abstract class Engine {

    private final String name;

    private AudioNode engineAudio;

    // the total power of the engine. This will be distributed to the propellant(s).
    private float power;

    private float revs; // revolutions in a 0 - 1 range.
    private float maxRevs; // max revs - e.g. 7000 - used as a VISUAL multiplier.

    // the amount of engine braking when coasting.
    // this can be manipulated to simulate damage.
    private float braking;

    private boolean started;

    /**
     * Defines an engine
     * @param name    the name of the engine.
     * @param power   the power it can produce.
     * @param maxRevs the maximum revolutions the engine can work at.
     * @param braking the amount of engine braking when coasting.
     */
    public Engine(String name, float power, float maxRevs, float braking) {
        this.name = name;
        this.power = power;
        this.maxRevs = maxRevs;
        this.braking = braking;
    }

    public String getName() {
        return this.name;
    }

    public void setEngineAudio(AssetManager assetManager, String audioFile) {
        this.engineAudio = new AudioNode(assetManager, audioFile, AudioData.DataType.Buffer);
        this.engineAudio.setLooping(true);
        this.engineAudio.setPositional(true);
        this.engineAudio.setDirectional(false);
    }

    public AudioNode getEngineAudio() {
        return engineAudio;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public float getRevs() {
        return revs;
    }

    public void setRevs(float revs) {
        // this.revs = FastMath.clamp(revs, 0, 1);
        // we're not going to clamp it because it exposes bad math if it's set wrong.
        this.revs = revs;
    }

    public float getMaxRevs() {
        return maxRevs;
    }

    public void setMaxRevs(float maxRevs) {
        this.maxRevs = maxRevs;
    }


    /**
     * Gets the power output at the current RPM.
     * This is essentially the "power graph" of the engine.
     * @return the power of the engine at the current RPM.
     */
    public float getPowerOutputAtRevs() {
        float revs = getRevs() * getMaxRevs();
        revs = FastMath.clamp(revs, 0, getMaxRevs() - 0.01f);
        float power = evaluateSpline(revs);
        return power * getPower();
    }

    protected float getTorqueAtSpeed(Vehicle vehicle) {
        // the maximum this vehicle can go is 135mph or 216kmh.

        // float engineMaxSpeed = 192.0f;
        float engineMaxSpeed = vehicle.getGearBox().getMaxSpeed(Vehicle.SpeedUnit.KMH);
        float speedUnit = vehicle.getSpeed(Vehicle.SpeedUnit.KMH) / engineMaxSpeed;
        return 1.0f - FastMath.interpolateLinear(speedUnit, 0, speedUnit);
    }

    /**
     * Get the amount of torque the engine produces at speed.
     * This is a kind of speed limiter that slows down acceleration as the vehicle increases in speed.
     * @return the amount of torque applied at the current speed.
     */
    public abstract float getTorqueAtSpeed();

    /**
     * Evaluate the power graph
     * @param range a value from 0-maxRevs
     * @return the power at this rev-range, from 0 to getPower().
     */
    public abstract float evaluateSpline(float range);

    public float getBraking() {
        return braking;
    }

    public void setBraking(float braking) {
        this.braking = braking;
    }

    public float evaluateSpline(Spline powerGraph, float range) {

        int index = powerGraph.getControlPoints().size() - 1;

        Vector3f point = powerGraph.getControlPoints().get(index);

        while (point.x > range) {
            index -= 1;
            point = powerGraph.getControlPoints().get(index);
        }

        //System.out.println("index: " + index + " - range: " + range);

        float start = point.x;
        float end = powerGraph.getControlPoints().get(index + 1).x;

        float interp = map(range, start, end, 0, 1);

        return powerGraph.interpolate(interp, index, null).y;
    }

    private float map(float value, float oldMin, float oldMax, float newMin, float newMax) {
        return (((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }

}
