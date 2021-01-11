package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;

/**
 * AppState to manage a car's active skidmarks, one skidmark for each wheel.
 */
public class SkidMarksState extends BaseAppState {
    // *************************************************************************
    // fields

    private boolean skidmarkEnabled = true;
    final private Car vehicle;
    final private float tireWidth;
    private int numWheels;
    private WheelSkid[] skids;
    // *************************************************************************
    // constructors

    public SkidMarksState(Car vehicle, float tireWidth) {
        this.vehicle = vehicle;
        this.tireWidth = tireWidth;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Stop adding more skidmarks.
     *
     * @param enabled whether or not to show more skidmarks.
     */
    public void setSkidmarkEnabled(boolean enabled) {
        skidmarkEnabled = enabled;
    }
    // *************************************************************************
    // BaseAppState methods

    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        numWheels = vehicle.countWheels();
        skids = new WheelSkid[numWheels];
        AssetManager assetManager = app.getAssetManager();

        for (int i = 0; i < numWheels; ++i) {
            Wheel wheel = vehicle.getWheel(i);
            skids[i] = new WheelSkid(wheel, assetManager, tireWidth);
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
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

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        // do nothing
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
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
}
