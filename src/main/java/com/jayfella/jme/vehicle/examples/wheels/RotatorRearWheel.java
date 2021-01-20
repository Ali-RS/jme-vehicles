package com.jayfella.jme.vehicle.examples.wheels;

import com.jayfella.jme.vehicle.Main;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A WheelModel built around the left rear wheel of oakar258's "HCR2 Rotator"
 * model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class RotatorRearWheel extends WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger2
            = Logger.getLogger(RotatorRearWheel.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    public RotatorRearWheel(float diameter) {
        super(diameter);

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "/Models/hcr2_rotator/rear-wheel.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);
        super.setSpatial(cgmRoot);
    }
}
