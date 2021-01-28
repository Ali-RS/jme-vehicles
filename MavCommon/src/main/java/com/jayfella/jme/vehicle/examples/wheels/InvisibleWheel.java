package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel for an invisible wheel.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class InvisibleWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(InvisibleWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public InvisibleWheel(float diameter) {
        super(diameter);
    }
    // *************************************************************************
    // WheelModel methods

    /**
     * Load this WheelModel from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        Spatial cgmRoot = new Node("wheel");
        super.setSpatial(cgmRoot);
    }
}
