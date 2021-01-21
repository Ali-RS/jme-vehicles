package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * AppState to manage a vehicle's active skidmarks, one skidmark for each Wheel.
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
     * Vehicle producing the skidmarks (not null)
     */
    final private Vehicle vehicle;
    /**
     * width of the skidmark for each wheel (in world units, &gt;0)
     */
    final private float[] skidWidths;
    /**
     * active skidmark for each wheel
     */
    private WheelSkid[] skids;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState.
     *
     * @param vehicle the Vehicle that will produce skidmarks (not null)
     */
    public SkidMarksState(Vehicle vehicle) {
        this.vehicle = vehicle;

        int numWheels = vehicle.countWheels();
        this.skidWidths = new float[numWheels];
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            Wheel wheel = vehicle.getWheel(wheelIndex);
            Spatial spatial = wheel.getVehicleWheel().getWheelSpatial();
            BoundingBox bounds = (BoundingBox) spatial.getWorldBound();
            float skidWidth = 0.75f * bounds.getZExtent();
            skidWidths[wheelIndex] = skidWidth;
        }
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

    /**
     * Translate all skidmarks by the specified offset.
     *
     * @param offset the desired offset (in world coordinates, not null,
     * unaffected)
     */
    public void translateAll(Vector3f offset) {
        for (WheelSkid skid : skids) {
            skid.translateAll(offset);
        }
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        int numWheels = vehicle.countWheels();
        skids = new WheelSkid[numWheels];
        AssetManager assetManager = application.getAssetManager();

        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            Wheel wheel = vehicle.getWheel(wheelIndex);
            float width = skidWidths[wheelIndex];
            skids[wheelIndex] = new WheelSkid(wheel, assetManager, width);
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
        super.update(tpf);

        for (WheelSkid skid : skids) {
            Geometry geometry = skid.getGeometry();

            // kind of annoying, but we can't attach a geometry that doesn't exist if the Vehicle hasn't skidded yet.
            if (geometry != null && geometry.getParent() == null) {
                vehicle.getNode().getParent().attachChild(geometry);
            }

            if (skidmarkEnabled) {
                skid.update(tpf);
            }
        }
    }
}
