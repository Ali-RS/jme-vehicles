package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.Vector3f;

class WheelSkid {

    final private static float SKID_FX_SPEED = 0.25f; // Min side slip speed in m/s to start showing a skid
    private int lastSkid = -1; // Array index for the skidmarks controller. Index of last skidmark piece this wheel used
    final private SkidMarkManager manager;
    final private Wheel wheel;

    WheelSkid(Wheel wheel, AssetManager assetManager, float tireWidth) {
        this.wheel = wheel;
        manager = new SkidMarkManager(assetManager, tireWidth);
    }

    SkidMarkManager getManager() {
        return manager;
    }

    void update(float tpf) {
        float skidFraction = wheel.skidFraction();
        if (skidFraction < SKID_FX_SPEED) {
            lastSkid = -1;
        } else {
            skidFraction = smoothstep(SKID_FX_SPEED, 1f, skidFraction);
            VehicleWheel vehicleWheel = wheel.getVehicleWheel();
            Vector3f normal = vehicleWheel.getCollisionNormal();
            assert normal.isUnitVector() : normal;
            Vector3f location = vehicleWheel.getCollisionLocation();
            lastSkid = manager.addSection(location, normal, skidFraction,
                    lastSkid);
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
