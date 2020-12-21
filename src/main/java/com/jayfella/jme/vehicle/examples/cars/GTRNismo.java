package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine600HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
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
 * A sample Car, built around iSteven's "Nissan GT-R" model.
 */
public class GTRNismo extends Car {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(GTRNismo.class.getName());
    // *************************************************************************
    // constructors

    public GTRNismo() {
        super(Main.getApplication(), "GTR Nismo");
    }
    // *************************************************************************
    // Car methods

    /**
     * Determine the offset of the Nismo's DashCamera.
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
        String assetPath = "Models/gtr_nismo/scene.gltf.j3o";
        Spatial chassis = assetManager.loadModel(assetPath);
        chassis.setLocalScale(0.01f); // TODO eliminate this step
        float mass = 1_525f; // in kilos
        setChassis(chassis, mass);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        float wheelScale = 0.75f;
        WheelModel wheel_fl = new DarkAlloyWheel(assetManager, wheelScale);
        wheel_fl.getSpatial().rotate(0f, FastMath.PI, 0f); // ???

        WheelModel wheel_fr = new DarkAlloyWheel(assetManager, wheelScale);

        WheelModel wheel_rl = new DarkAlloyWheel(assetManager, wheelScale);
        wheel_rl.getSpatial().rotate(0f, FastMath.PI, 0f); // ???

        WheelModel wheel_rr = new DarkAlloyWheel(assetManager, wheelScale);
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.8f; // half of the wheelbase
        float axleY = 0.1f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.4f;
        float rearZ = -1.4f;
        boolean front = true; // Front wheels are for steering.
        boolean rear = false; // Rear wheels do not steer.
        boolean steeringFlipped = false;
        addWheel(wheel_fl.getWheelNode(),
                new Vector3f(+wheelX, axleY, frontZ), front, steeringFlipped,
                new Brake(675f));
        addWheel(wheel_fr.getWheelNode(),
                new Vector3f(-wheelX, axleY, frontZ), front, steeringFlipped,
                new Brake(675f));
        addWheel(wheel_rl.getWheelNode(),
                new Vector3f(+wheelX, axleY, rearZ), rear, steeringFlipped,
                new Brake(300f));
        addWheel(wheel_rr.getWheelNode(),
                new Vector3f(-wheelX, axleY, rearZ), rear, steeringFlipped,
                new Brake(300f));
        /*
         * Configure the suspension.
         * This vehicle applies the same settings to each wheel,
         * but you don't have to.
         */
        for (int wheelIndex = 0; wheelIndex < getNumWheels(); ++wheelIndex) {
            Suspension suspension = getWheel(wheelIndex).getSuspension();

            // the rest-length or "height" of the suspension
            suspension.setRestLength(0.01f);

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(7_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(12.5f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompression(0.3f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setDamping(0.4f);
        }
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
         * This vehicle has 4-wheel drive.
         */
        getWheel(0).setAccelerationForce(1f);
        getWheel(1).setAccelerationForce(1f);
        getWheel(2).setAccelerationForce(1f);
        getWheel(3).setAccelerationForce(1f);
        /*
         * Define the speed range for each gear.
         * Successive gears should overlap.
         * The "end" value of the last gear should determine the top speed.
         */
        GearBox gearBox = new GearBox(6);
        gearBox.setGear(0, 0f, 30f);
        gearBox.setGear(1, 15f, 70f);
        gearBox.setGear(2, 50f, 130f);
        gearBox.setGear(3, 120f, 190f);
        gearBox.setGear(4, 180f, 255f);
        gearBox.setGear(5, 250f, 320f);

        setGearBox(gearBox);

        Engine engine = new Engine600HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-2.ogg");
        setEngine(engine);

        super.setHornAudio("Audio/horn-1.ogg");
        /*
         * build() must be invoked last, to complete the Vehicle
         */
        build();
    }

    /**
     * Determine the offset of the Nismo's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 0.6f, -2.478f);
    }
}
