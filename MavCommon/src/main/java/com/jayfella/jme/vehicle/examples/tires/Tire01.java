package com.jayfella.jme.vehicle.examples.tires;

import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jayfella.jme.vehicle.tire.TireSettings;

public class Tire01 extends PacejkaTireModel {

    public Tire01() {
        super("Tire 01",
                new TireSettings(
                        1.54f,
                        18.86f,
                        0.27f,
                        2.0f,
                        0.000058f
                ),
                new TireSettings(
                        1.52f,
                        30.0f,
                        -1.6f,
                        2.14f,
                        0.000055f
                ),
                new TireSettings(
                        2.13f,
                        9.96f,
                        -2.0f,
                        2.65f,
                        0.000110f
                ),
                10000
        );
    }
}
