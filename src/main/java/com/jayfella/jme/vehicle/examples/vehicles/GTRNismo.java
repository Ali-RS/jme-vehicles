package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.engines.Engine600HP;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound2;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.lemurdemo.Main;
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
 * A sample Vehicle, built around iSteven's "Nissan GT-R" model.
 */
public class GTRNismo extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(GTRNismo.class.getName());
    // *************************************************************************
    // constructors

    public GTRNismo() {
        super("GTR Nismo");
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
        String assetPath = "Models/gtr_nismo/scene.gltf.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        float mass = 1_525f; // in kilos
        float linearDamping = 0.002f;
        setChassis("gtr_nismo", chassis, mass, linearDamping);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float diameter = 0.74f;
        WheelModel wheel_fl = new DarkAlloyWheel(diameter);
        WheelModel wheel_fr = new DarkAlloyWheel(diameter).flip();
        WheelModel wheel_rl = new DarkAlloyWheel(diameter);
        WheelModel wheel_rr = new DarkAlloyWheel(diameter).flip();
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.8f; // half of the axle track
        float axleY = 0.32f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.42f;
        float rearZ = -1.36f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float parkingBrake = 25_000f; // in rear only
        float damping = 0.009f; // extra linear damping
        addWheel(wheel_fl, new Vector3f(+wheelX, axleY, frontZ), front,
                steeringFlipped, 6_750f, 0f, damping);
        addWheel(wheel_fr, new Vector3f(-wheelX, axleY, frontZ), front,
                steeringFlipped, 6_750f, 0f, damping);
        addWheel(wheel_rl, new Vector3f(+wheelX, axleY, rearZ), rear,
                steeringFlipped, 3_000f, parkingBrake, damping);
        addWheel(wheel_rr, new Vector3f(-wheelX, axleY, rearZ), rear,
                steeringFlipped, 3_000f, parkingBrake, damping);
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
            suspension.setMaxForce(7_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(12.5f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.3f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.4f);
        }
        /*
         * Give each wheel a tire with friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire_01());
            wheel.setFriction(1.6f);
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
        GearBox gearBox = new GearBox(6, 1);
        gearBox.getGear(-1).setName("reverse").setMinMaxRedKph(0f, -40f, -40f);
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 30f, 35f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(15f, 70f, 75f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(50f, 130f, 140f);
        gearBox.getGear(4).setName("4th").setMinMaxRedKph(120f, 190f, 200f);
        gearBox.getGear(5).setName("5th").setMinMaxRedKph(180f, 255f, 275f);
        gearBox.getGear(6).setName("high").setMinMaxRedKph(250f, 320f, 320f);
        setGearBox(gearBox);

        Engine engine = new Engine600HP();
        setEngine(engine);

        Sound engineSound = new EngineSound2();
        setEngineSound(engineSound);

        setHornAudio("/Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the Nismo's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.5f, 0.5f);
    }

    /**
     * Determine the offset of the Nismo's ChaseCamera target in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.6f, -2.31f);
    }
}
