package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine180HP;
import com.jayfella.jme.vehicle.examples.tires.Tire_02;
import com.jayfella.jme.vehicle.examples.wheels.BasicAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        super(Main.getApplication(), "HatchBack");
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
        return new Vector3f(0f, 1.6f, 0.3f);
    }

    /**
     * Load this Vehicle from assets.
     */
    @Override
    public void load() {
        if (getVehicleControl() != null) {
            logger.log(Level.SEVERE, "The model is already loaded.");
        }

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Spatial chassis = assetManager.loadModel("Models/Vehicles/Chassis/Hatchback/hatchback.j3o");
        Material chassisMaterial = assetManager.loadMaterial("Materials/Vehicles/Hatchback.j3m");
        chassis.setMaterial(chassisMaterial);
        setChassis(chassis, 1140);

        WheelModel wheel_fl = new BasicAlloyWheel(assetManager, 0.8f);

        WheelModel wheel_fr = new BasicAlloyWheel(assetManager, 0.8f);
        wheel_fr.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_rl = new BasicAlloyWheel(assetManager, 0.8f);

        WheelModel wheel_rr = new BasicAlloyWheel(assetManager, 0.8f);
        wheel_rr.getSpatial().rotate(0, FastMath.PI, 0);

        addWheel(wheel_fl.getWheelNode(), new Vector3f(0.75f, 0, 1.3f), true, false, new Brake(80));
        addWheel(wheel_fr.getWheelNode(), new Vector3f(-0.75f, 0, 1.3f), true, false, new Brake(80));

        addWheel(wheel_rl.getWheelNode(), new Vector3f(0.75f, 0, -1.3f), false, false, new Brake(0));
        addWheel(wheel_rr.getWheelNode(), new Vector3f(-0.75f, 0, -1.3f), false, false, new Brake(0));

        for (int i = 0; i < getNumWheels(); i++) {
            getWheel(i).getSuspension().setRestLength(0.01f);
            getWheel(i).getSuspension().setStiffness(20);
            getWheel(i).getSuspension().setCompression(0.6f);
            getWheel(i).getSuspension().setDamping(0.8f);
            getWheel(i).setFriction(0.9f);
        }

        // give each wheel a tire.
        getWheel(0).setTireModel(new Tire_02());
        getWheel(1).setTireModel(new Tire_02());
        getWheel(2).setTireModel(new Tire_02());
        getWheel(3).setTireModel(new Tire_02());

        getWheel(0).setAccelerationForce(1);
        getWheel(1).setAccelerationForce(1);
        getWheel(2).setAccelerationForce(0);
        getWheel(3).setAccelerationForce(0);

        //vehicle.setMaxSpeedMph(130);
        GearBox gearBox = new GearBox(6);
        gearBox.setGear(0, 0, 20);
        gearBox.setGear(1, 20, 40);
        gearBox.setGear(2, 40, 75);
        gearBox.setGear(3, 75, 110);
        gearBox.setGear(4, 110, 140);
        gearBox.setGear(5, 140, 200);

        setGearBox(gearBox);

        Engine engine = new Engine180HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-4.ogg");
        setEngine(engine);

        super.setHornAudio("Audio/horn-1.ogg");

        build();
    }

    /**
     * Determine the offset of the hatchback's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 0.52f, -2.112f);
    }
}
