package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel derived from the left rear wheel of oakar258's "HCR2 Buggy"
 * model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class BuggyRearWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(BuggyRearWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public BuggyRearWheel(float diameter) {
        super(diameter);

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "/Models/hcr2_buggy/rear-wheel.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);
        super.setSpatial(cgmRoot);
    }
}
