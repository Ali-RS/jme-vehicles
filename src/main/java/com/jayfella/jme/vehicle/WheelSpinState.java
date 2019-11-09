package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class WheelSpinState extends BaseAppState {

    private final Car car;
    private int wheelCount;

    private Quaternion[] rot;
    private float[][] angles;// = new float[3];

    public WheelSpinState(Car car) {
        this.car = car;
    }

    @Override
    protected void initialize(Application app) {
        this.wheelCount = car.getVehicleControl().getNumWheels();
        this.angles = new float[wheelCount][3];

         rot = new Quaternion[wheelCount];

         for (int i = 0; i < rot.length; i++) {
             rot[i] = new Quaternion();
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

    }

    // returns how many revolutions per second a wheel should be spinning at
    // based on the gear and revs of the engine.
    private float calcWheelspin(Wheel wheel) {

        // https://sciencing.com/calculate-wheel-speed-7448165.html

        Gear gear  = car.getGearBox().getActiveGear();

        // should be between 0 and 1.
        float revs = car.getEngine().getRevs();

        // should give us the speed the vehicle should travel at these revs.
        float mphRevs = FastMath.interpolateLinear(revs, gear.getStart(), gear.getEnd());

        // convert mph to meters per minute
        float metersPerHour = mphRevs * 1609;
        float metersPerMin = metersPerHour / 60;

        // calculate the circumference of the wheel.
        float c = (wheel.getSize() * 0.5f) * FastMath.PI * 2;

        // calc the wheel speed in revs per min
        float revsPerMin = metersPerMin / c;
        float revsPerSec = revsPerMin / 60;

        return revsPerSec;
    }

    @Override
    public void update(float tpf) {

        for (int i = 0; i < wheelCount; i++) {

            /*
            Wheel wheel = car.getWheel(i);

            // rotation since last physics step
            float currentRot = wheel.getVehicleWheel().getDeltaRotation();

            // how many times this wheel "should" rotate per second.
            float potentialRevsPerSec = calcWheelspin(wheel);

            // multiply that by TWO_PI to get radians.
            float potentialRot = potentialRevsPerSec / FastMath.TWO_PI;

            // should give us a rotation in radians for "extre" rotation as a result
            // of slip.
            float diff = potentialRot - currentRot;

            wheel.setRotationDelta(diff);

            System.out.println(wheel.getVehicleWheel().getWheelSpatial().getName() + ": " + currentRot + " / " + potentialRot + " / " + diff);

             */

            Wheel wheel = car.getWheel(i);

            // the acceleration force this wheel can apply. 0 = it doesnt give power, 1 = it gives full power.
            float wheelforce = wheel.getAccelerationForce();

            // the acceleration force of the accelerator pedal in 0-1 range.
            float acceleration = car.getAccelerationForce();

            // how much this wheel is "skidding".
            float skid = 1.0f - wheel.getVehicleWheel().getSkidInfo();

            // would equal at most 57 degrees in one frame (one radian).
            float skidForce = (acceleration * wheelforce) * skid;

            //System.out.println(wheel.getVehicleWheel().getWheelSpatial().getName() + ": " + skidForce);

            // set this before we do any "scene" modifications to make it look better.
            wheel.setRotationDelta(skidForce);

            // the numbers below alter the scene only. they have no relation to any calculations.
            // These calculations will add an additional rotation to the wheel to simulate wheelspin.

            // so if we mult this by say 10(?) for 570 degrees and then mult it by tpf, we should be about right.
            // this means if we are slipping 100% it will add ( 57 * x ) degrees per second.

            // actually we can work this out. get the max revs.

            skidForce *= 10;
            skidForce *= tpf;

            // the angle is negative due to the way the wheels rotate. negative is forward.
            angles[i][0] += skidForce;
            //angles[i][1] = 0.2f;

            // around and around we go...
            if (angles[i][0] < -FastMath.PI) {
                angles[i][0] += FastMath.PI;
            }

            Node wheelNode = (Node) wheel.getVehicleWheel().getWheelSpatial();
            Spatial wheelGeom = wheelNode.getChild("wheel");

            float[] existingAngles = wheelGeom.getLocalRotation().toAngles(null);

            angles[i][1] = existingAngles[1];
            angles[i][2] = existingAngles[2];

            rot[i].fromAngles(angles[i]);

            wheelGeom.setLocalRotation(rot[i]);


        }


    }

}
