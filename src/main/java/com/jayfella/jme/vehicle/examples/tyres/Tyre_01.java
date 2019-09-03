package com.jayfella.jme.vehicle.examples.tyres;

import com.jayfella.jme.vehicle.tire.PajeckaTireModel;
import com.jayfella.jme.vehicle.tire.TyreSettings;

public class Tyre_01 extends PajeckaTireModel {

    public Tyre_01() {
        super("Tyre 01",
                new TyreSettings(
                        2.50f,
                        7.34f,
                        1.05f,
                        2.0f,
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
