package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.examples.engines.Engine250HP;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound4;
import com.jayfella.jme.vehicle.examples.tires.Tire_02;
import com.jayfella.jme.vehicle.examples.wheels.HatchbackWheel;
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
 * A sample Car, built around Daniel Zhabotinsky's "Modern Hatchback - Low Poly"
 * model.
 */
public class HatchBack extends Car {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(HatchBack.class.getName());
    // *************************************************************************
    // constructors

    public HatchBack() {
        super("HatchBack");
    }
    // *************************************************************************
    // Car methods

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
        String assetPath = "Models/modern_hatchback/hatchback.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        float mass = 1_140f; // in kilos
        float linearDamping = 0.004f;
        setChassis("modern_hatchback", chassis, mass, linearDamping);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float diameter = 0.65f;
        WheelModel wheel_fl = new HatchbackWheel(diameter);
        WheelModel wheel_fr = new HatchbackWheel(diameter).flip();
        WheelModel wheel_rl = new HatchbackWheel(diameter);
        WheelModel wheel_rr = new HatchbackWheel(diameter).flip();
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.66f; // half of the axle track
        float axleY = -0.02f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.2f;
        float rearZ = -1.19f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float mainBrake = 5_000f; // all 4 wheels
        float parkingBrake = 25_000f; // in front only
        float damping = 0.025f; // extra linear damping
        addWheel(wheel_fl, new Vector3f(+wheelX, axleY, frontZ), front,
                steeringFlipped, mainBrake, parkingBrake, damping);
        addWheel(wheel_fr, new Vector3f(-wheelX, axleY, frontZ), front,
                steeringFlipped, mainBrake, parkingBrake, damping);
        addWheel(wheel_rl, new Vector3f(+wheelX, axleY, rearZ), rear,
                steeringFlipped, mainBrake, 0f, damping);
        addWheel(wheel_rr, new Vector3f(-wheelX, axleY, rearZ), rear,
                steeringFlipped, mainBrake, 0f, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // the rest-length or "height" of the suspension
            suspension.setRestLength(0.01f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(20f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.6f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.8f);
        }
        /*
         * Give each wheel a tire with friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire_02());
            wheel.setFriction(0.9f);
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
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 30f, 40f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(20f, 60f, 70f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(50f, 100f, 120f);
        gearBox.getGear(4).setName("high").setMinMaxRedKph(80f, 140f, 140f);
        setGearBox(gearBox);

        Engine engine = new Engine250HP();
        setEngine(engine);

        Sound engineSound = new EngineSound4();
        setEngineSound(engineSound);

        setHornAudio("/Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the hatchback's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.2f, 0.7f);
    }

    /**
     * Determine the offset of the hatchback's ChaseCamera target in scaled
     * shape coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.52f, -1.7f);
    }
}
