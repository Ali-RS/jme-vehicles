package com.jayfella.jme.vehicle.examples.engines;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;

public class Engine250HP extends Engine {

    final private Vehicle vehicle;
    final private Spline powerGraph;

    public Engine250HP(Vehicle vehicle) {
        super("Basic 250", 2000f, 600f, 5500f, 10f);
        this.vehicle = vehicle;

        Vector3f[] points = new Vector3f[] {

                new Vector3f(0, 0.25f, 0),
                new Vector3f(1000, 0.35f, 0),
                new Vector3f(2000, 0.4f, 0),
                new Vector3f(3000, 0.65f, 0),
                new Vector3f(4000, 0.75f, 0),
                new Vector3f(5000, 0.99f, 0),
                new Vector3f(6000, 0.95f, 0),
                new Vector3f(7000, 0.70f, 0),
                new Vector3f(7500, 0.60f, 0),
        };

        powerGraph = new Spline(Spline.SplineType.Linear, points, 0.1f, false);
    }

    @Override
    public float getTorqueAtSpeed() {
        return getTorqueAtSpeed(vehicle);
    }

    @Override
    public float evaluateSpline(float range) {
        return evaluateSpline(powerGraph, range);
    }

}
