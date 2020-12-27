package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine180HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_02;
import com.jayfella.jme.vehicle.examples.wheels.HatchbackWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sample Car.
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
     * Determine the offset of the hatchback's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    public Vector3f dashCamOffset() {
        return new Vector3f(0f, 1.2f, 0.7f);
    }

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
        float linearDamping = 0f;
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
        float brakeForce = 80f; // This vehicle has brakes only in front.
        addWheel(wheel_fl, new Vector3f(+wheelX, axleY, frontZ), front,
                steeringFlipped, brakeForce);
        addWheel(wheel_fr, new Vector3f(-wheelX, axleY, frontZ), front,
                steeringFlipped, brakeForce);
        addWheel(wheel_rl, new Vector3f(+wheelX, axleY, rearZ), rear,
                steeringFlipped, 0f);
        addWheel(wheel_rr, new Vector3f(-wheelX, axleY, rearZ), rear,
                steeringFlipped, 0f);
        /*
         * Configure the suspension.
         * This vehicle applies the same settings to each wheel,
         * but you don't have to.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Suspension suspension = getWheel(wheelIndex).getSuspension();

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
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Wheel w = getWheel(wheelIndex);
            w.setTireModel(new Tire_02());
            w.setFriction(0.9f);
        }
        /*
         * Distribute drive power across the wheels:
         *  0 = no power, 1 = full power
         *
         * This vehicle has 4-wheel drive.
         */
        getWheel(0).setPowerFraction(1f);
        getWheel(1).setPowerFraction(1f);
        getWheel(2).setPowerFraction(1f);
        getWheel(3).setPowerFraction(1f);
        /*
         * Define the speed range for each gear.
         * Successive gears should overlap.
         * The "end" value of the last gear should determine the top speed.
         */
        GearBox gearBox = new GearBox(6);
        gearBox.setGear(0, 0f, 20f);
        gearBox.setGear(1, 20f, 40f);
        gearBox.setGear(2, 40f, 75f);
        gearBox.setGear(3, 75f, 110f);
        gearBox.setGear(4, 110f, 140f);
        gearBox.setGear(5, 140f, 200f);
        setGearBox(gearBox);

        Engine engine = new Engine180HP();
        engine.setEngineAudio(assetManager, "Audio/engine-4.ogg");
        setEngine(engine);

        super.setHornAudio("Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the hatchback's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 0.52f, -1.7f);
    }
}
