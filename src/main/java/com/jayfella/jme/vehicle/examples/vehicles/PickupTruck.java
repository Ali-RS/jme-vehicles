package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound1;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.RangerWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sample Vehicle, built around mauro.zampaoli's "Ford Ranger" model.
 */
public class PickupTruck extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(PickupTruck.class.getName());
    // *************************************************************************
    // constructors

    public PickupTruck() {
        super("Pickup Truck");
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
        /*
         * Load the C-G model with everything except the wheels.
         * Bullet refers to this as the "chassis".
         */
        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "Models/ford_ranger/pickup.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        float mass = 1_550f; // in kilos
        float linearDamping = 0.01f;
        setChassis("ford_ranger", chassis, mass, linearDamping);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float diameter = 0.8f;
        WheelModel wheel_fl = new RangerWheel(diameter);
        WheelModel wheel_fr = new RangerWheel(diameter).flip();
        WheelModel wheel_rl = new RangerWheel(diameter);
        WheelModel wheel_rr = new RangerWheel(diameter).flip();
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.75f; // half of the axle track
        float axleY = 0.7f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.76f;
        float rearZ = -1.42f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float mainBrake = 4_000f; // all 4 wheels
        float parkingBrake = 25_000f; // in rear only
        float damping = 0.04f; // extra linear damping
        addWheel(wheel_fl, new Vector3f(+wheelX, axleY, frontZ), front,
                steeringFlipped, mainBrake, 0f, damping);
        addWheel(wheel_fr, new Vector3f(-wheelX, axleY, frontZ), front,
                steeringFlipped, mainBrake, 0f, damping);
        addWheel(wheel_rl, new Vector3f(+wheelX, axleY, rearZ), rear,
                steeringFlipped, mainBrake, parkingBrake, damping);
        addWheel(wheel_rr, new Vector3f(-wheelX, axleY, rearZ), rear,
                steeringFlipped, mainBrake, parkingBrake, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // the rest-length or "height" of the suspension
            suspension.setRestLength(0.51f);
            suspension.setMaxTravelCm(1_000f);

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(20_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(20f);
        }
        /*
         * Give each wheel a tire with friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire_01());
            wheel.setFriction(1f);
        }
        /*
         * Distribute drive power across the wheels:
         *  0 = no power, 1 = all of the power
         *
         * This vehicle has 4-wheel drive.
         */
        getWheel(0).setPowerFraction(0.2f);
        getWheel(1).setPowerFraction(0.2f);
        getWheel(2).setPowerFraction(0.2f);
        getWheel(3).setPowerFraction(0.2f);
        /*
         * Specify the name and speed range for each gear.
         * The min-max speeds of successive gears should overlap.
         * The "min" speed of low gear should be zero.
         * The "max" speed of high gear determines the top speed.
         * The "red" speed of each gear is used to calculate its ratio.
         */
        GearBox gearBox = new GearBox(4, 1);
        gearBox.getGear(-1).setName("reverse").setMinMaxRedKph(0f, -40f, -40f);
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 19f, 25f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(12f, 50f, 60f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(40f, 80f, 90f);
        gearBox.getGear(4).setName("high").setMinMaxRedKph(70f, 110f, 110f);
        setGearBox(gearBox);

        Engine engine = new Engine450HP();
        setEngine(engine);

        Sound engineSound = new EngineSound1();
        setEngineSound(engineSound);

        setHornAudio("/Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the truck's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.5f, 1.1f);
    }

    /**
     * Determine the offset of the truck's ChaseCamera target target in scaled
     * shape coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.91f, -2.75f);
    }
}
