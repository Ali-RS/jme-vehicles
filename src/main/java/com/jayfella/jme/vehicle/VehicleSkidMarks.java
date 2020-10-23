package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.skid.WheelSkid;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public class VehicleSkidMarks extends BaseAppState {

    private Car vehicle;

    private WheelSkid[] skids;
    private int numWheels;

    private final int maxDistance;
    private final float tyreWidth;

    public VehicleSkidMarks(Car vehicle, int maxDistance, float tyreWidth) {
        this.vehicle = vehicle;

        this.maxDistance = maxDistance;
        this.tyreWidth = tyreWidth;
    }

    @Override
    protected void initialize(Application app) {

        numWheels = vehicle.getNumWheels();
        this.skids = new WheelSkid[numWheels];

        for (int i = 0; i < numWheels; i++) {
            skids[i] = new WheelSkid(vehicle.getWheel(i).getVehicleWheel(), app.getAssetManager(), maxDistance, tyreWidth);
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

    private boolean skidmarkEnabled = true;

    /**
     * Stop adding more skidmarks.
     * @param enabled whether or not to show more skidmarks.
     */
    public void setSkidmarkEnabled(boolean enabled) {
        skidmarkEnabled = enabled;
    }

}
