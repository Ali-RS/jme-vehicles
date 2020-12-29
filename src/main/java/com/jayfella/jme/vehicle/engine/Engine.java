package com.jayfella.jme.vehicle.engine;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.FastMath;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import java.util.List;
import java.util.logging.Logger;
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
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Engine.class.getName());
    // *************************************************************************
    // fields

    /**
     * sound produced by this Engine
     */
    private AudioNode audioNode;
    /**
     * true when on/running, false when off/stopped
     */
    private boolean isRunning;
    /**
     * crankshaft rotation rate while idling (in revolutions per minute, &ge;0)
     */
    private float idleRpm;
    /**
     * maximum power output (in Watts, &gt;0)
     */
    private float maxOutputWatts;
    /**
     * crankshaft rotation rate at the redline (in revolutions per minute,
     * &ge;idleRpm)
     */
    private float redlineRpm;
    /**
     * rotational speed as a fraction of the redline (&ge;0)
     */
    private float rpmFraction;
    /**
     * descriptive name (not null)
     */
    final private String name;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an Engine in the "off" state, without audio.
     *
     * @param name the desired name (not null)
     * @param maxWatts the desired maximum power output (in Watts, &gt;0)
     * @param idleRpm the desired idle speed (in RPMs, &ge;0, &lt;redlineRpm)
     * @param redlineRpm the desired redline speed (&gt;0)
     */
    public Engine(String name, float maxWatts, float idleRpm,
            float redlineRpm) {
        Validate.positive(maxWatts, "max Watts");
        Validate.inRange(idleRpm, "idle RPM", 0f, redlineRpm);
        Validate.positive(redlineRpm, "redline RPM");

        this.name = name;
        this.maxOutputWatts = maxWatts;
        this.idleRpm = idleRpm;
        this.redlineRpm = redlineRpm;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the power output for the specified speed.
     *
     * @param rpm the crankshaft rotation rate (in revolutions per minute,
     * &ge;0)
     * @return the power output (in Watts, between 0 and getPower())
     */
    abstract public float powerFraction(float rpm);

    /**
     * Determine the power output for the specified speed, using a Spline.
     *
     * @param powerCurve the spline to use (not null, unaffected)
     * @param rpm the crankshaft rotation rate (in revolutions per minute)
     * @return the power output (in Watts)
     */
    protected static float evaluateSpline(Spline powerCurve, float rpm) {
        List<Vector3f> points = powerCurve.getControlPoints();
        int lastIndex = points.size() - 1;
        for (int lowIndex = 0; lowIndex < lastIndex; ++lowIndex) {
            float lowRpm = points.get(lowIndex).x;
            float highRpm = points.get(lowIndex + 1).x;
            if (rpm >= lowRpm && rpm <= highRpm) {
                float t = FastMath.unInterpolateLinear(rpm, lowRpm, highRpm);
                Vector3f interpolatedPoint
                        = powerCurve.interpolate(t, lowIndex, null);
                assert FastMath.approximateEquals(interpolatedPoint.x, rpm);
                float result = interpolatedPoint.y;

                return result;
            }
        }
        throw new IllegalArgumentException("rpm = " + rpm);
    }

    /**
     * Access this engine's sound.
     *
     * @return the pre-existing AudioNode
     */
    public AudioNode getEngineAudio() {
        return audioNode;
    }

    /**
     * Determine the idle speed.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &ge;0,
     * &le;redlineRpm)
     */
    public float getIdleRpm() {
        assert idleRpm >= 0f && idleRpm <= redlineRpm : idleRpm;
        return idleRpm;
    }

    /**
     * Determine the redline speed.
     *
     * @return the crankshaft rotation rate (in revolutions per minute,
     * &gt;idlRpm)
     */
    public float getRedlineRpm() {
        assert redlineRpm > 0f : redlineRpm;
        return redlineRpm;
    }

    /**
     * Determine this engine's name.
     *
     * @return the descriptive name (not null)
     */
    public String getName() {
        assert name != null;
        return name;
    }

    /**
     * Determine the maximum power output.
     *
     * @return the power (in Watts, &gt;0)
     */
    public float getMaxOutputWatts() {
        assert maxOutputWatts > 0f : maxOutputWatts;
        return maxOutputWatts;
    }

    /**
     * Determine the current power output.
     *
     * @return the power (in Watts, &gt;0, &le;maxOutputWatts)
     */
    public float outputWatts() {
        float revs = getRpm();
        revs = FastMath.clamp(revs, 0, getRedlineRpm() - 0.01f);
        float powerFraction = powerFraction(revs);
        float result = powerFraction * getMaxOutputWatts();

        assert result >= 0f && result <= getMaxOutputWatts() : result;
        return result;
    }

    /**
     * Determine the current speed.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &ge;0)
     */
    public float getRpm() {
        return rpmFraction * redlineRpm;
    }

    /**
     * Determine the current speed as a fraction of the redline.
     *
     * @return the fraction (&ge;0)
     */
    public float getRpmFraction() {
        assert rpmFraction >= 0f && rpmFraction <= 1f : rpmFraction;
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
     * @param assetPath an asset path to the desired sound (not null)
     */
    public void setEngineAudio(String assetPath) {
        Validate.nonEmpty(assetPath, "asset path");

        AssetManager assetManager = Main.getApplication().getAssetManager();
        audioNode = new AudioNode(assetManager, assetPath,
                AudioData.DataType.Buffer);
        audioNode.setLooping(true);
        audioNode.setPositional(false);
        audioNode.setDirectional(false);
    }

    /**
     * Alter this engine's redline speed.
     *
     * @param redlineRpm the desired crankshaft rotation rate (in revolutions
     * per minute, &ge;idleRpm)
     */
    public void setMaxRevs(float redlineRpm) {
        Validate.inRange(redlineRpm, "redline RPM", idleRpm, Float.MAX_VALUE);
        this.redlineRpm = redlineRpm;
    }

    /**
     * Alter this engine's maximum power output.
     *
     * @param watts the desired amount of power (&gt;0)
     */
    public void setMaxOutputWatts(float watts) {
        Validate.positive(watts, "watts");
        maxOutputWatts = watts;
    }

    /**
     * Alter the engine's speed as a fraction of the redline.
     *
     * @param revs the desired fraction (&ge;0)
     */
    public void setRpmFraction(float revs) {
        Validate.fraction(revs, "revs");
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
}
