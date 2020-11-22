package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Car;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.Vector3f;

public class WheelSkid {

    final float SKID_FX_SPEED = 0.25f; // Min side slip speed in m/s to start showing a skid
    int lastSkid = -1; // Array index for the skidmarks controller. Index of last skidmark piece this wheel used
    SkidMarkManager manager;
    final private VehicleControl vehicleControl;
    VehicleWheel wheel;

    public WheelSkid(Car car, int wheelIndex, AssetManager assetManager,
            float tireWidth) {
        vehicleControl = car.getVehicleControl();
        wheel = car.getWheel(wheelIndex).getVehicleWheel();
        this.manager = new SkidMarkManager(assetManager, tireWidth);
    }

    public SkidMarkManager getManager() {
        return manager;
    }

    public void update(float tpf) {
        float distance = vehicleControl.castRay(wheel.getIndex());
        if (distance < 0f) {
            lastSkid = -1;  // The tire isn't touching pavement!

        } else if (wheel.getSkidInfo() < 1f) {
            float wheelspin = 1f - wheel.getSkidInfo();

            if (wheelspin > SKID_FX_SPEED) {
                wheelspin = smoothstep(SKID_FX_SPEED, 1.0f, wheelspin);
                Vector3f normal = wheel.getCollisionNormal();
                assert normal.isUnitVector() : normal;
                lastSkid = manager.addSection(wheel.getCollisionLocation(),
                        normal, wheelspin, lastSkid);
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
