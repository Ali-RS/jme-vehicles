package com.jayfella.jme.vehicle.skid;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.objects.VehicleWheel;

public class WheelSkid {

    final float SKID_FX_SPEED = 0.25f; // Min side slip speed in m/s to start showing a skid
    int lastSkid = -1; // Array index for the skidmarks controller. Index of last skidmark piece this wheel used
    SkidMarkManager manager;
    // VehicleControl vehicleControl;
    VehicleWheel wheel;

    public WheelSkid(VehicleWheel wheel, AssetManager assetManager, float tireWidth) {
        // this.vehicleControl = vehicle.getVehicleControl();
        this.wheel = wheel;
        this.manager = new SkidMarkManager(assetManager, tireWidth);
    }

    public SkidMarkManager getManager() {
        return manager;
    }

    public void update(float tpf) {
        if (wheel.getSkidInfo() < 1) {
            float wheelspin = 1.0f - wheel.getSkidInfo();

            if (wheelspin > SKID_FX_SPEED) {
                wheelspin = smoothstep(SKID_FX_SPEED, 1.0f, wheelspin);
                lastSkid = manager.addSection(wheel.getCollisionLocation(), wheel.getCollisionNormal(), wheelspin, lastSkid);
            } else {
                lastSkid = -1;
            }

        } else {
            lastSkid = -1;
        }
    }

    private float smoothstep(final float a, final float b, final float x) {
        if (x < a) {
            return 0;
        } else if (x > b) {
            return 1;
        }
        float xx = (x - a) / (b - a);
        return xx * xx * (3 - 2 * xx);
    }
}
