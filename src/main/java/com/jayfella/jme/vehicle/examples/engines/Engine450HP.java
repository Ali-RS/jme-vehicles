package com.jayfella.jme.vehicle.examples.engines;

import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A 450-horsepower engine with a 600-RPM idle and a 7,500-RPM redline.
 */
public class Engine450HP extends Engine {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Engine450HP.class.getName());
    /**
     * points defining the power curve: x=RPMs, y=powerFraction
     */
    final private static Vector3f[] points = new Vector3f[]{
        new Vector3f(0f, 0.25f, 0f),
        new Vector3f(1_000f, 0.35f, 0f),
        new Vector3f(2_000f, 0.4f, 0f),
        new Vector3f(3_000f, 0.5f, 0f),
        new Vector3f(4_000f, 0.7f, 0f),
        new Vector3f(5_000f, 0.95f, 0f),
        new Vector3f(6_000f, 0.99f, 0f),
        new Vector3f(7_000f, 0.85f, 0f),
        new Vector3f(7_500f, 0.75f, 0f)
    };
    // *************************************************************************
    // fields

    /**
     * power curve
     */
    final private Spline spline;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a 450-horsepower engine.
     */
    public Engine450HP() {
        super("Basic 450", 450f * HP_TO_W, 600f, 7_500f);
        spline = new Spline(Spline.SplineType.Linear, points, 0.1f, false);
    }
    // *************************************************************************
    // Engine methods

    /**
     * Determine the fractional power output at the specified speed.
     *
     * @param rpm the angular speed of the crankshaft (in RPMs, &ge;0)
     * @return the power as a fraction of the maximum (&ge;0, &le;1)
     */
    @Override
    public float powerFraction(float rpm) {
        Validate.nonNegative(rpm, "rpm");
        float result = evaluateSpline(spline, rpm);
        return result;
    }
}
