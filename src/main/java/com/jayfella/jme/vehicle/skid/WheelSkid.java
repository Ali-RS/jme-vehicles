package com.jayfella.jme.vehicle.skid;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.FastMath;

public class WheelSkid {

    // VehicleControl vehicleControl;
    VehicleWheel wheel;
    SkidMarkManager manager;

    public WheelSkid(VehicleWheel wheel, AssetManager assetManager, int maxDistance, float tyreWidth) {
        // this.vehicleControl = vehicle.getVehicleControl();
        this.wheel = wheel;
        this.manager = new SkidMarkManager(assetManager, maxDistance, tyreWidth);
    }

	final float SKID_FX_SPEED = 0.25f; // Min side slip speed in m/s to start showing a skid
    int lastSkid = -1; // Array index for the skidmarks controller. Index of last skidmark piece this wheel used

    public SkidMarkManager getManager() {
        return manager;
    }

    public void update(float tpf) {

        if (wheel.getSkidInfo() < 1) {

            float wheelspin = 1.0f - wheel.getSkidInfo();

            if (wheelspin > SKID_FX_SPEED) {

                wheelspin = smoothstep(SKID_FX_SPEED, 1.0f, wheelspin);


                lastSkid = manager.AddSkidMark(wheel.getCollisionLocation(), wheel.getCollisionNormal(), wheelspin, lastSkid);
            } else {
                lastSkid = -1;
            }

        } else {
            lastSkid = -1;
        }


        manager.update();
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
