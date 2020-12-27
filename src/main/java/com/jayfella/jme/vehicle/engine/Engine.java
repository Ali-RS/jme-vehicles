package com.jayfella.jme.vehicle.engine;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import jme3utilities.Validate;

/**
 * Model a single engine in a Vehicle.
 */
abstract public class Engine {
    // *************************************************************************
    // constants and loggers

    /**
     * approximate factor to convert horsepower to Watts
     */
    final public static float HP_TO_W = 745f;
    // *************************************************************************
    // fields

    private AudioNode audioNode;
    /*
     * true when on/running, false when off/stopped
     */
    private boolean isRunning;
    /*
     * crankshaft revolutions per minute (RPMs) when idling (&ge;0)
     */
    private float idleRpm;
    /*
     * maximum power output (total across all axles, &gt;0)
     */
    private float maxPower;
    /*
     * revolutions per minute (RPMs) at the redline (&ge;idleRpm)
     */
    private float redline;
    /*
     * current RPMs as a fraction of the redline (&ge;0)
     */
    private float rpmFraction;
    /*
     * descriptive name
     */
    final private String name;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an Engine in the "off" state, without audio.
     *
     * @param name a descriptive name
     * @param power the desired maximum power output (&gt;0)
     * @param idleSpeed the desired idle speed (in RPMs, &ge;0)
     * @param maxRevs the desired RPMs at the redline (&gt;0)
     */
    public Engine(String name, float power, float idleSpeed, float maxRevs) {
        Validate.positive(maxRevs, "redline");
        Validate.inRange(idleSpeed, "idle speed", 0f, maxRevs);

        this.name = name;
        this.maxPower = power;
        this.idleRpm = idleSpeed;
        this.redline = maxRevs;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Evaluate this engine's power graph.
     *
     * @param range a value from 0-maxRevs
     * @return the power at this rev-range, from 0 to getPower().
     */
    abstract public float evaluateSpline(float range);

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

    /**
     * Access the sound of this Engine.
     *
     * @return the pre-existing AudioNode
     */
    public AudioNode getEngineAudio() {
        return audioNode;
    }

    /**
     * Determine this engine's idle speed.
     *
     * @return the rotational speed (in RPMs, &ge;0)
     */
    public float getIdleRpm() {
        assert idleRpm >= 0f : idleRpm;
        return idleRpm;
    }

    /**
     * Determine this engine's redline.
     *
     * @return the rate (in revolutions per minute, &gt;0)
     */
    public float getMaxRevs() {
        return redline;
    }

    /**
     * Access this engine's descriptive name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Determine this engine's maximum power output. TODO units?
     *
     * @return the amount of power (&gt;0)
     */
    public float getPower() {
        return maxPower;
    }

    /**
     * Determine the power output at the current RPM. This determines the
     * Engine's "power graph".
     *
     * @return the amount of power (&gt;0)
     */
    public float getPowerOutputAtRevs() {
        float revs = getRpmFraction() * getMaxRevs();
        revs = FastMath.clamp(revs, 0, getMaxRevs() - 0.01f);
        float power = evaluateSpline(revs);

        return power * getPower();
    }

    /**
     * Determine the current RPMs as a fraction of the redline.
     *
     * @return the fraction (&ge;0)
     */
    public float getRpmFraction() {
        return rpmFraction;
    }

    /**
     * Test whether this Engine is running.
     *
     * @return true if running, otherwise false
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Alter the sound of this Engine.
     *
     * @param assetManager (not null)
     * @param audioFile an asset path to the desired sound (not null)
     */
    public void setEngineAudio(AssetManager assetManager, String audioFile) {
        audioNode = new AudioNode(assetManager, audioFile,
                AudioData.DataType.Buffer);
        audioNode.setLooping(true);
        audioNode.setPositional(true);
        audioNode.setDirectional(false);
    }

    /**
     * Alter this engine's redline.
     *
     * @param maxRevs the desired speed (in RPMs, &ge;idleRpm)
     */
    public void setMaxRevs(float maxRevs) {
        Validate.inRange(maxRevs, "max revs", idleRpm, Float.MAX_VALUE);
        redline = maxRevs;
    }

    /**
     * Alter this engine's maximum power output.
     *
     * @param power the desired amount of power (&gt;0)
     */
    public void setPower(float power) {
        maxPower = power;
    }

    /**
     * Alter the current RPMs as a fraction of the redline.
     *
     * @param revs the desired fraction (&ge;0)
     */
    public void setRevs(float revs) {
        rpmFraction = revs;
    }

    /**
     * Alter whether this Engine is running.
     *
     * @param newState the desired state (default=false)
     */
    public void setRunning(boolean newState) {
        isRunning = newState;
    }
    // *************************************************************************
    // new protected methods

    protected float getTorqueAtSpeed(Vehicle vehicle) {
        float engineMaxSpeed = vehicle.getGearBox().getMaxSpeed(SpeedUnit.KPH);
        float speedUnit = vehicle.getSpeed(SpeedUnit.KPH) / engineMaxSpeed;
        return 1.0f - FastMath.interpolateLinear(speedUnit, 0, speedUnit);
    }
    // *************************************************************************
    // private methods

    private float map(float value, float oldMin, float oldMax, float newMin,
            float newMax) {
        return (((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }
}
