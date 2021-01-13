package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import java.util.logging.Logger;

/**
 * AppState to manage a car's active skidmarks, one skidmark for each wheel.
 */
public class SkidMarksState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(SkidMarksState.class.getName());
    // *************************************************************************
    // fields

    private boolean skidmarkEnabled = true;
    /**
     * Car that produces the skidmarks (not null)
     */
    final private Car vehicle;
    /**
     * width of the tire (in world units, &gt;0)
     */
    final private float tireWidth;
    private int numWheels;
    /**
     * active skid mark for each wheel
     */
    private WheelSkid[] skids;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState.
     *
     * @param vehicle the Car that will produce skidmarks (not null)
     */
    public SkidMarksState(Car vehicle, float tireWidth) {
        this.vehicle = vehicle;
        this.tireWidth = tireWidth;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Enable or disable adding skidmarks.
     *
     * @param enabled true&rarr;keep adding skidmarks, false&rarr;stop adding
     */
    public void setSkidmarkEnabled(boolean enabled) {
        skidmarkEnabled = enabled;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        numWheels = vehicle.countWheels();
        skids = new WheelSkid[numWheels];
        AssetManager assetManager = application.getAssetManager();

        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            Wheel wheel = vehicle.getWheel(wheelIndex);
            skids[wheelIndex] = new WheelSkid(wheel, assetManager, tireWidth);
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        for (WheelSkid skid : skids) {
            Geometry geometry = skid.getGeometry();
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
        for (WheelSkid skid : skids) {
            Geometry geometry = skid.getGeometry();

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
