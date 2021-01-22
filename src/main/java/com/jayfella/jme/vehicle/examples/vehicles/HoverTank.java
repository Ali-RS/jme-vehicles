package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.engines.Engine600HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.InvisibleWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sample Vehicle, built around Rémy Bouquet's "Enhanced HoverTank" model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HoverTank extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(HoverTank.class.getName());
    // *************************************************************************
    // fields

    /**
     * control signal for steering
     */
    private float steeringValue;
    /**
     * reusable temporary vectors
     */
    final private Vector3f tmpForce = new Vector3f();
    final private Vector3f tmpInvInertia = new Vector3f();
    final private Vector3f tmpTorque = new Vector3f();
    final private Vector3f tmpVelocity = new Vector3f();
    // *************************************************************************
    // constructors

    public HoverTank() {
        super("HoverTank");
    }
    // *************************************************************************
    // Vehicle methods

    /**
     * Load this Vehicle from assets.
     */
    @Override
    public void load() {
        if (getVehicleControl() != null) {
            logger.log(Level.SEVERE, "The model is already loaded.");
            return;
        }

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Spatial chassis = assetManager.loadModel("/Models/Tank/chassis.j3o");

        float mass = 10_000f; // in kilos
        float linearDamping = 0.25f;
        setChassis("Tank", chassis, mass, linearDamping);
        getVehicleControl().setAngularDamping(0.4f);

        float wheelDiameter = 1.5f;
        WheelModel wheel_fl = new InvisibleWheel(wheelDiameter);
        WheelModel wheel_fr = new InvisibleWheel(wheelDiameter).flip();
        WheelModel wheel_rl = new InvisibleWheel(wheelDiameter);
        WheelModel wheel_rr = new InvisibleWheel(wheelDiameter).flip();
        /*
         * Add the (invisible) wheels to the Vehicle.
         */
        float frontX = 0.9f; // half of the axle track
        float rearX = 2.4f;
        float axleY = 0f; // height of axles relative to vehicle's CoG
        float frontZ = 2.7f;
        float rearZ = -1.8f;
        boolean steering = false; // Wheels do not steer.
        boolean steeringFlipped = false;
        float mainBrake = 0f;
        float parkingBrake = 0f;
        float extraDamping = 0f;
        addWheel(wheel_fl, new Vector3f(+frontX, axleY, frontZ), steering,
                steeringFlipped, mainBrake, parkingBrake, extraDamping);
        addWheel(wheel_fr, new Vector3f(-frontX, axleY, frontZ), steering,
                steeringFlipped, mainBrake, parkingBrake, extraDamping);
        addWheel(wheel_rl, new Vector3f(+rearX, axleY, rearZ), steering,
                steeringFlipped, mainBrake, parkingBrake, extraDamping);
        addWheel(wheel_rr, new Vector3f(-rearX, axleY, rearZ), steering,
                steeringFlipped, mainBrake, parkingBrake, extraDamping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // the rest-length or "height" of the suspension
            suspension.setRestLength(0.6f);

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(50_000f);
        }
        /*
         * Give each wheel a tire with very little friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setFriction(0.001f);
            wheel.setPowerFraction(0f);
            wheel.setTireModel(new Tire_01());
        }
        /*
         * Specify the name and speed range for each gear.
         * The min-max speeds of successive gears should overlap.
         * The "min" speed of low gear should be zero.
         * The "max" speed of high gear determines the top speed.
         * The "red" speed of each gear is used to calculate its ratio.
         */
        GearBox gearBox = new GearBox(1, 0);
        gearBox.getGear(1).setName("forward").setMinMaxRedKph(0f, 200f, 200f);
        setGearBox(gearBox);

        Engine engine = new Engine600HP();
        setEngine(engine);

        setEngineSound(null);
        setHornAudio("/Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the tank's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 2f, 0f);
    }

    /**
     * Determine the offset of the tank's ChaseCamera target in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.9f, -3.3f);
    }

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        PhysicsVehicle vehicleControl = getVehicleControl();

        if (steeringValue != 0f) {
            vehicleControl.getAngularVelocity(tmpVelocity);
            float turnRate = tmpVelocity.y;
            if (FastMath.sqr(turnRate) < 1f) {
                vehicleControl.getInverseInertiaLocal(tmpInvInertia);
                float torqueMagnitude = 2f * steeringValue / tmpInvInertia.y;
                tmpTorque.set(0f, torqueMagnitude, 0f);
                vehicleControl.applyTorque(tmpTorque);
            }
        }

        float accelerationValue = accelerateSignal();
        if (accelerationValue > 0f) {
            float maxSpeed = getGearBox().maxForwardSpeed(SpeedUnit.MPH);
            float speed = getSpeed(SpeedUnit.MPH);
            float speedFraction = speed / maxSpeed;
            if (speedFraction <= 1f) {
                if (speedFraction > 0.8f) {
                    accelerationValue *= (1f - speedFraction) / 0.2f;
                }
                float mass = vehicleControl.getMass();
                float forceMagnitude = 10f * mass * accelerationValue;

                vehicleControl.getForwardVector(tmpForce);
                tmpForce.multLocal(1f, 0f, 1f);
                tmpForce.normalizeLocal();
                tmpForce.multLocal(forceMagnitude);
                vehicleControl.applyCentralForce(tmpForce);
            }
        }

        super.prePhysicsTick(space, timeStep);
    }

    @Override
    public void steer(float strength) {
        this.steeringValue = strength;
    }
}
