package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Car;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public class SkidMarksState extends BaseAppState {

    private boolean skidmarkEnabled = true;
    private Car vehicle;
    private WheelSkid[] skids;
    private int numWheels;
    private final float tireWidth;

    public SkidMarksState(Car vehicle, float tireWidth) {
        this.vehicle = vehicle;
        this.tireWidth = tireWidth;
    }

    @Override
    protected void initialize(Application app) {
        numWheels = vehicle.getNumWheels();
        this.skids = new WheelSkid[numWheels];

        for (int i = 0; i < numWheels; i++) {
            skids[i] = new WheelSkid(vehicle, i, app.getAssetManager(),
                    tireWidth);
        }
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        for (int i = 0; i < numWheels; i++) {

            WheelSkid skid = skids[i];

            if (skid.getManager().getGeometry() != null) {
                skid.getManager().getGeometry().removeFromParent();
            }
        }
    }

    @Override
    public void update(float tpf) {
        for (int i = 0; i < numWheels; i++) {

            WheelSkid skid = skids[i];

            // kind of annoying, but we can't attach a geometry that doesn't exist if the car hasn't skidded yet.
            if (skid.getManager().getGeometry() != null && skid.getManager().getGeometry().getParent() == null) {
                // ((SimpleApplication) getApplication()).getRootNode().attachChild(skid.getManager().getGeometry());
                vehicle.getNode().getParent().attachChild(skid.getManager().getGeometry());
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
