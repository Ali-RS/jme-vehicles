package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine250HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.BuggyFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.BuggyRearWheel;
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
 * A sample Car, built around oakar258's "HCR2 Buggy" model.
 */
public class DuneBuggy extends Car {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DuneBuggy.class.getName());
    // *************************************************************************
    // constructors

    public DuneBuggy() {
        super("Dune Buggy");
    }
    // *************************************************************************
    // Car methods

    /**
     * Determine the offset of the dune buggy's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    public Vector3f dashCamOffset() {
        return new Vector3f(0f, 1.4f, -0.4f);
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
        String assetPath = "Models/hcr2_buggy/dune-buggy.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        float mass = 525f; // in kilos
        float linearDamping = 0f;
        setChassis("hcr2_buggy", chassis, mass, linearDamping);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float rearDiameter = 0.944f;
        float frontDiameter = 0.77f;
        WheelModel wheel_fl = new BuggyFrontWheel(frontDiameter);
        WheelModel wheel_fr = new BuggyFrontWheel(frontDiameter).flip();
        WheelModel wheel_rl = new BuggyRearWheel(rearDiameter);
        WheelModel wheel_rr = new BuggyRearWheel(rearDiameter).flip();
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.92f; // half of the axle track
        float frontY = 0.53f; // height of front axle relative to vehicle's CoG
        float rearY = 0.63f; // height of rear axle relative to vehicle's CoG
        float frontZ = 1.12f;
        float rearZ = -1.33f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float brakeForce = 80f; // This vehicle has brakes only in front.
        addWheel(wheel_fl, new Vector3f(+wheelX, frontY, frontZ), front,
                steeringFlipped, brakeForce);
        addWheel(wheel_fr, new Vector3f(-wheelX, frontY, frontZ), front,
                steeringFlipped, brakeForce);
        addWheel(wheel_rl, new Vector3f(+wheelX, rearY, rearZ), rear,
                steeringFlipped, 0f);
        addWheel(wheel_rr, new Vector3f(-wheelX, rearY, rearZ), rear,
                steeringFlipped, 0f);
        /*
         * Configure the suspension.
         * This vehicle applies the same settings to each wheel,
         * but you don't have to.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Suspension suspension = getWheel(wheelIndex).getSuspension();

            // the rest-length or "height" of the suspension
            suspension.setRestLength(0.25f);

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
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Wheel w = getWheel(wheelIndex);
            w.setTireModel(new Tire_01());
            w.setFriction(1.3f);
        }
        /*
         * Distribute drive power across the wheels:
         *  0 = no power, 1 = full power
         *
         * This vehicle has rear-wheel drive only.
         */
        getWheel(0).setAccelerationForce(0f);
        getWheel(1).setAccelerationForce(0f);
        getWheel(2).setAccelerationForce(1f);
        getWheel(3).setAccelerationForce(1f);
        /*
         * Define the speed range for each gear.
         * Successive gears should overlap.
         * The "end" value of the last gear should determine the top speed.
         */
        GearBox gearBox = new GearBox(6);
        gearBox.setGear(0, 0f, 15f);
        gearBox.setGear(1, 15f, 30f);
        gearBox.setGear(2, 30f, 45f);
        gearBox.setGear(3, 45f, 80f);
        gearBox.setGear(4, 80f, 140f);
        gearBox.setGear(5, 140f, 220f);
        setGearBox(gearBox);

        Engine engine = new Engine250HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-5.ogg");
        setEngine(engine);

        super.setHornAudio("Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the dune buggy's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 1.18f, -1.67f);
    }
}
