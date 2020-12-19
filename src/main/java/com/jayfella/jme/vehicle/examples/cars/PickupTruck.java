package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.BasicAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An sample Car based around mauro.zampaoli's "Ford Ranger" model.
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
        super(Main.getApplication(), "Pickup Truck");
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
        }
        /*
         * Load the C-G model with everything except the wheels.
         * Bullet refers to this as the "chassis".
         */
        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "Models/ford_ranger/pickup.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        float mass = 1_550f; // in kilos
        setChassis(chassis, mass);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float wheelScale = 0.8f;
        WheelModel wheel_fl = new BasicAlloyWheel(assetManager, wheelScale);

        WheelModel wheel_fr = new BasicAlloyWheel(assetManager, wheelScale);
        wheel_fr.getSpatial().rotate(0f, FastMath.PI, 0f);

        WheelModel wheel_rl = new BasicAlloyWheel(assetManager, wheelScale);

        WheelModel wheel_rr = new BasicAlloyWheel(assetManager, wheelScale);
        wheel_rr.getSpatial().rotate(0f, FastMath.PI, 0f);
        /*
         * Add wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.75f; // half of the wheelbase
        float axleY = 0.7f; // height of axle relative to vehicle's CoG
        float frontZ = 1.76f;
        float rearZ = -1.42f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float brakeForce = 90f;
        addWheel(wheel_fl.getWheelNode(),
                new Vector3f(+wheelX, axleY, frontZ), front, steeringFlipped,
                new Brake(brakeForce));
        addWheel(wheel_fr.getWheelNode(),
                new Vector3f(-wheelX, axleY, frontZ), front, steeringFlipped,
                new Brake(brakeForce));
        addWheel(wheel_rl.getWheelNode(),
                new Vector3f(+wheelX, axleY, rearZ), rear, steeringFlipped,
                new Brake(brakeForce));
        addWheel(wheel_rr.getWheelNode(),
                new Vector3f(-wheelX, axleY, rearZ), rear, steeringFlipped,
                new Brake(brakeForce));
        /*
         * Configure the suspension.
         * This vehicle applies the same settings to each wheel,
         * but you don't have to.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Wheel w = getWheel(wheelIndex);

            // the rest-length or "height" of the suspension
            w.getSuspension().setRestLength(0.51f);
            w.getVehicleWheel().setMaxSuspensionTravelCm(1_000f);

            // how much force the suspension can take before it bottoms out
            // setting this too low will make the wheels sink into the ground
            w.getSuspension().setMaxForce(20_000f);

            // the stiffness of the suspension
            // setting this too soft can cause odd behavior.
            w.getSuspension().setStiffness(20f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            w.getSuspension().setCompression(0.2f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            w.getSuspension().setDamping(0.3f);
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
        getWheel(0).setAccelerationForce(1f);
        getWheel(1).setAccelerationForce(1f);
        getWheel(2).setAccelerationForce(1f);
        getWheel(3).setAccelerationForce(1f);
        /*
         * Define the speed range for each gearbox.
         * Successive gears should overlap.
         * The "end" value of the last gear should determine the top speed.
         */
        Gear[] gearArray = new Gear[]{
            new Gear(0f, 19f),
            new Gear(15f, 48f),
            new Gear(35f, 112f),
            new Gear(100f, 192f),
            new Gear(180f, 254f)
        };
        GearBox gearBox = new GearBox(gearArray);
        setGearBox(gearBox);

        Engine engine = new Engine450HP(this);
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
