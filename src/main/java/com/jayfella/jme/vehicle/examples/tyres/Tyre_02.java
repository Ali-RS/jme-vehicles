package com.jayfella.jme.vehicle.examples.tyres;

import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jayfella.jme.vehicle.tire.TyreSettings;

public class Tyre_02 extends PacejkaTireModel {

    public Tyre_02() {
        super("Tyre 02",
                new TyreSettings(
                        2.49f,
                        11.93f,
                        0.88f,
                        2.00f,
                        0.000055f
                ),
                new TyreSettings(
                        1.41f,
                        30.0f,
                        -1.6f,
                        2.0f,
                        0.000055f
                ),
                new TyreSettings(
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
