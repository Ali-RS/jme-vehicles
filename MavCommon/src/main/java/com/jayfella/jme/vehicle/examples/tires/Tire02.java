package com.jayfella.jme.vehicle.examples.tires;

import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jayfella.jme.vehicle.tire.TireSettings;
import java.util.logging.Logger;

/**
 * An example of a Pacejka lateral-force model for a tire.
 */
public class Tire02 extends PacejkaTireModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(Tire02.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a tire model of this type.
     */
    public Tire02() {
        super("Tire 02",
                new TireSettings(
                        2.49f,
                        11.93f,
                        0.88f,
                        2.00f,
                        0.000055f
                ),
                new TireSettings(
                        1.41f,
                        30.0f,
                        -1.6f,
                        2.0f,
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
