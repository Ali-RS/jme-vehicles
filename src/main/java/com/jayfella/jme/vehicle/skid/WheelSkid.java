package com.jayfella.jme.vehicle.skid;

import com.jayfella.jme.vehicle.Car;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.Vector3f;

class WheelSkid {

    final private static float SKID_FX_SPEED = 0.25f; // Min side slip speed in m/s to start showing a skid
    private int lastSkid = -1; // Array index for the skidmarks controller. Index of last skidmark piece this wheel used
    final private SkidMarkManager manager;
    final private VehicleControl vehicleControl;
    final private VehicleWheel wheel;

    WheelSkid(Car car, int wheelIndex, AssetManager assetManager,
            float tireWidth) {
        vehicleControl = car.getVehicleControl();
        wheel = car.getWheel(wheelIndex).getVehicleWheel();
        manager = new SkidMarkManager(assetManager, tireWidth);
    }

    SkidMarkManager getManager() {
        return manager;
    }

    void update(float tpf) {
        int wheelIndex = wheel.getIndex();
        float distance = vehicleControl.castRay(wheelIndex); // TODO Wheel.traction() method
        if (distance < 0f) {
            /*
             * There's nothing supporting the wheel.
             */
            lastSkid = -1;
            return;
        }

        float traction = wheel.getSkidInfo();
        if (traction >= 1f) {
            /*
             * The tire has full traction.
             */
            lastSkid = -1;
            return;
        }

        float wheelspin = 1f - traction;
        if (wheelspin > SKID_FX_SPEED) {
            wheelspin = smoothstep(SKID_FX_SPEED, 1f, wheelspin);
            Vector3f normal = wheel.getCollisionNormal();
            assert normal.isUnitVector() : normal;
            Vector3f location = wheel.getCollisionLocation();
            lastSkid = manager.addSection(location, normal, wheelspin,
                    lastSkid);
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
