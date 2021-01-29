package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.engines.Engine180HP;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound5;
import com.jayfella.jme.vehicle.examples.sounds.HornSound1;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.BuggyFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.BuggyRearWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sample Vehicle, built around oakar258's "HCR2 Buggy" model.
 */
public class DuneBuggy extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(DuneBuggy.class.getName());
    // *************************************************************************
    // constructors

    public DuneBuggy() {
        super("Dune Buggy");
    }
    // *************************************************************************
    // Vehicle methods

    /**
     * Load this Vehicle from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        if (getVehicleControl() != null) {
            logger.log(Level.SEVERE, "The model is already loaded.");
            return;
        }
        /*
         * Load the C-G model with everything except the wheels.
         * Bullet refers to this as the "chassis".
         */
        float mass = 525f; // in kilos
        float linearDamping = 0.02f;
        setChassis("hcr2_buggy", "dune-buggy", assetManager, mass,
                linearDamping);

        float rearDiameter = 0.944f;
        float frontDiameter = 0.77f;
        WheelModel wheel_fl = new BuggyFrontWheel(frontDiameter);
        WheelModel wheel_fr = new BuggyFrontWheel(frontDiameter);
        WheelModel wheel_rl = new BuggyRearWheel(rearDiameter);
        WheelModel wheel_rr = new BuggyRearWheel(rearDiameter);
        wheel_fl.load(assetManager);
        wheel_fr.load(assetManager);
        wheel_rl.load(assetManager);
        wheel_rr.load(assetManager);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        wheel_fr.flip();
        wheel_rr.flip();
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.9f; // half of the axle track
        float frontY = 0.48f; // height of front axle relative to vehicle's CoG
        float rearY = 0.6f; // height of rear axle relative to vehicle's CoG
        float frontZ = 1.12f;
        float rearZ = -1.33f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float mainBrake = 3_000f; // in front only
        float parkingBrake = 3_000f; // in front only
        float damping = 0.09f; // extra linear damping
        addWheel(wheel_fl, new Vector3f(+wheelX, frontY, frontZ), front,
                steeringFlipped, mainBrake, parkingBrake, damping);
        addWheel(wheel_fr, new Vector3f(-wheelX, frontY, frontZ), front,
                steeringFlipped, mainBrake, parkingBrake, damping);
        addWheel(wheel_rl, new Vector3f(+wheelX, rearY, rearZ), rear,
                steeringFlipped, 0f, 0f, damping);
        addWheel(wheel_rr, new Vector3f(-wheelX, rearY, rearZ), rear,
                steeringFlipped, 0f, 0f, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(12_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(24f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.5f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.65f);
        }
        /*
         * Give each wheel a tire with friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire_01());
            wheel.setFriction(1.3f);
        }
        /*
         * Distribute drive power across the wheels:
         *  0 = no power, 1 = all of the power
         *
         * This vehicle has rear-wheel drive.
         *
         * 4-wheel drive would be problematic here because
         * the diameters of the front wheels differ from those of the rear ones.
         */
        getWheel(0).setPowerFraction(0f);
        getWheel(1).setPowerFraction(0f);
        getWheel(2).setPowerFraction(0.4f);
        getWheel(3).setPowerFraction(0.4f);
        /*
         * Specify the name and speed range for each gear.
         * The min-max speeds of successive gears should overlap.
         * The "min" speed of low gear should be zero.
         * The "max" speed of high gear determines the top speed.
         * The "red" speed of each gear is used to calculate its ratio.
         */
        GearBox gearBox = new GearBox(4, 1);
        gearBox.getGear(-1).setName("reverse").setMinMaxRedKph(0f, -40f, -40f);
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 15f, 20f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(5f, 30f, 35f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(25f, 50f, 60f);
        gearBox.getGear(4).setName("high").setMinMaxRedKph(45f, 90f, 90f);
        setGearBox(gearBox);

        Engine engine = new Engine180HP();
        setEngine(engine);

        Sound engineSound = new EngineSound5();
        engineSound.load(assetManager);
        setEngineSound(engineSound);

        Sound hornSound = new HornSound1();
        hornSound.load(assetManager);
        setHornSound(hornSound);
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the dune buggy's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.4f, -0.4f);
    }

    /**
     * Determine the offset of the dune buggy's ChaseCamera target in scaled
     * shape coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 1.18f, -1.67f);
    }
}
