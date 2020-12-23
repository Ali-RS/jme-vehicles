package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.CruiserWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sample Car, built around Thomas Glenn Thorne's "Opel GT Retopo" model.
 */
public class GrandTourer extends Car {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(GrandTourer.class.getName());
    // *************************************************************************
    // constructors

    public GrandTourer() {
        super(Main.getApplication(), "Grand Tourer");
    }
    // *************************************************************************
    // Car methods

    /**
     * Determine the offset of the Grand Tourer's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    public Vector3f dashCamOffset() {
        return new Vector3f(0f, 1.5f, 0.5f);
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
        String assetPath = "Models/GT/scene.gltf.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        chassis.setLocalScale(0.2f); // TODO eliminate this step
        float mass = 1_525f; // in kilos
        setChassis(chassis, mass);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float wheelScale = 0.85f;
        WheelModel wheel_fl = new CruiserWheel(wheelScale);

        WheelModel wheel_fr = new CruiserWheel(wheelScale);
        wheel_fr.getSpatial().rotate(0f, FastMath.PI, 0f);

        WheelModel wheel_rl = new CruiserWheel(wheelScale);

        WheelModel wheel_rr = new CruiserWheel(wheelScale);
        wheel_rr.getSpatial().rotate(0f, FastMath.PI, 0f);
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.85f; // half of the axle track
        float frontY = 0.35f; // height of front axle relative to vehicle's CoG
        float rearY = 0.45f; // height of rear axle relative to vehicle's CoG
        float frontZ = 1.6f;
        float rearZ = -1.6f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        float brakeForce = 700f; // This vehicle has brakes only in front.
        addWheel(wheel_fl.getWheelNode(),
                new Vector3f(+wheelX, frontY, frontZ), front, steeringFlipped,
                new Brake(brakeForce));
        addWheel(wheel_fr.getWheelNode(),
                new Vector3f(-wheelX, frontY, frontZ), front, steeringFlipped,
                new Brake(brakeForce));
        addWheel(wheel_rl.getWheelNode(),
                new Vector3f(+wheelX, rearY, rearZ), rear, steeringFlipped,
                new Brake(0f));
        addWheel(wheel_rr.getWheelNode(),
                new Vector3f(-wheelX, rearY, rearZ), rear, steeringFlipped,
                new Brake(0f));
        /*
         * Configure the suspension.
         * This vehicle applies the same settings to each wheel,
         * but you don't have to.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Suspension suspension = getWheel(wheelIndex).getSuspension();

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(8_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(10f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.33f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.45f);
        }
        // the rest-length or "height" of the suspension
        getWheel(0).getSuspension().setRestLength(0.225f);
        getWheel(1).getSuspension().setRestLength(0.225f);
        getWheel(2).getSuspension().setRestLength(0.285f);
        getWheel(3).getSuspension().setRestLength(0.285f);
        /*
         * Give each wheel a tire with friction.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Wheel w = getWheel(wheelIndex);
            w.setTireModel(new Tire_01());
            w.setFriction(1.6f);
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
        GearBox gearBox = new GearBox(5);
        gearBox.setGear(0, 0f, 15f);
        gearBox.setGear(1, 5f, 40f);
        gearBox.setGear(2, 25f, 75f);
        gearBox.setGear(3, 55f, 130f);
        gearBox.setGear(4, 120f, 190f);

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
     * Determine the offset of the Grand Tourer's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 0.8f, -2.74f);
    }
}
