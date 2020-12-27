package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.RangerWheel;
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
 * A sample Car, built around mauro.zampaoli's "Ford Ranger" model.
 */
public class PickupTruck extends Car {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(PickupTruck.class.getName());
    // *************************************************************************
    // constructors

    public PickupTruck() {
        super("Pickup Truck");
    }
    // *************************************************************************
    // Car methods

    /**
     * Determine the offset of the truck's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    public Vector3f dashCamOffset() {
        return new Vector3f(0f, 1.5f, 1.1f);
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
        String assetPath = "Models/ford_ranger/pickup.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        float mass = 1_550f; // in kilos
        float linearDamping = 0f;
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
        float brakeForce = 90f;
        addWheel(wheel_fl, new Vector3f(+wheelX, axleY, frontZ), front,
                steeringFlipped, brakeForce);
        addWheel(wheel_fr, new Vector3f(-wheelX, axleY, frontZ), front,
                steeringFlipped, brakeForce);
        addWheel(wheel_rl, new Vector3f(+wheelX, axleY, rearZ), rear,
                steeringFlipped, brakeForce);
        addWheel(wheel_rr, new Vector3f(-wheelX, axleY, rearZ), rear,
                steeringFlipped, brakeForce);
        /*
         * Configure the suspension.
         * This vehicle applies the same settings to each wheel,
         * but you don't have to.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Suspension suspension = getWheel(wheelIndex).getSuspension();

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
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Wheel w = getWheel(wheelIndex);
            w.setTireModel(new Tire_01());
            w.setFriction(1f);
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
        GearBox gearBox = new GearBox(5);
        gearBox.setGear(0, 0f, 19f);
        gearBox.setGear(1, 15f, 48f);
        gearBox.setGear(2, 35f, 112f);
        gearBox.setGear(3, 100f, 192f);
        gearBox.setGear(4, 180f, 254f);
        setGearBox(gearBox);

        Engine engine = new Engine450HP();
        engine.setEngineAudio(assetManager, "Audio/engine-1.ogg");
        setEngine(engine);

        super.setHornAudio("Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the truck's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 0.91f, -2.75f);
    }
}
