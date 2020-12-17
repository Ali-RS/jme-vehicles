package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Car;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;

/**
 * Appstate to manage a car's active skidmarks, one for each wheel.
 */
public class SkidMarksState extends BaseAppState {
    // *************************************************************************
    // fields

    private boolean skidmarkEnabled = true;
    final private Car vehicle;
    private WheelSkid[] skids;
    private int numWheels;
    final private float tireWidth;
    // *************************************************************************
    // constructors

    public SkidMarksState(Car vehicle, float tireWidth) {
        this.vehicle = vehicle;
        this.tireWidth = tireWidth;
    }

    @Override
    protected void initialize(Application app) {
        numWheels = vehicle.getNumWheels();
        skids = new WheelSkid[numWheels];
        AssetManager assetManager = app.getAssetManager();

        for (int i = 0; i < numWheels; ++i) {
            skids[i] = new WheelSkid(vehicle, i, assetManager, tireWidth);
        }
    }

    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    @Override
    protected void onEnable() {
        // do nothing
    }

    @Override
    protected void onDisable() {
        for (int i = 0; i < numWheels; ++i) {
            WheelSkid skid = skids[i];
            Geometry geometry = skid.getManager().getGeometry();

            if (geometry != null) {
                geometry.removeFromParent();
            }
        }
    }

    @Override
    public void update(float tpf) {
        for (int i = 0; i < numWheels; ++i) {
            WheelSkid skid = skids[i];
            Geometry geometry = skid.getManager().getGeometry();

            // kind of annoying, but we can't attach a geometry that doesn't exist if the car hasn't skidded yet.
            if (geometry != null && geometry.getParent() == null) {
                vehicle.getNode().getParent().attachChild(geometry);
            }

            if (skidmarkEnabled) {
                skid.update(tpf);
            }
        }
    }

    /**
     * Stop adding more skidmarks.
     *
     * @param enabled whether or not to show more skidmarks.
     */
    public void setSkidmarkEnabled(boolean enabled) {
        skidmarkEnabled = enabled;
    }
}
