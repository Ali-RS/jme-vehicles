package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.WheelModel;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel built around the left front wheel from oakar258's "HCR2 Buggy"
 * model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class BuggyFrontWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(BuggyFrontWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public BuggyFrontWheel(float diameter) {
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
        String assetPath = "/Models/hcr2_buggy/front-wheel.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);
        super.setSpatial(cgmRoot);
    }
}
