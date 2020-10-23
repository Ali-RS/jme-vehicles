package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.examples.engines.Engine250HP;
import com.jayfella.jme.vehicle.examples.tyres.Tyre_01;
import com.jayfella.jme.vehicle.examples.wheels.BasicAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class DuneBuggy extends Car {

    public DuneBuggy(Application app) {
        super(app, "Dune Buggy");

        AssetManager assetManager = app.getAssetManager();

        Spatial chassis = assetManager.loadModel("Models/Vehicles/Chassis/DuneBuggy/dune-buggy.j3o");
        Material chassisMaterial = assetManager.loadMaterial("Materials/Vehicles/DuneBuggy.j3m");
        chassis.setMaterial(chassisMaterial);
        setChassis(chassis, 525);

        // rotate the right-sided wheels 180 degrees.

        WheelModel wheel_fl = new BasicAlloyWheel(assetManager, 0.75f);
        wheel_fl.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_fr = new BasicAlloyWheel(assetManager, 0.75f);

        WheelModel wheel_rl = new BasicAlloyWheel(assetManager, 0.75f);
        wheel_rl.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_rr = new BasicAlloyWheel(assetManager, 0.75f);


        addWheel(wheel_fl.getWheelNode(), new Vector3f(0.75f, .25f, 1.2f), true, false, new Brake(80));
        addWheel(wheel_fr.getWheelNode(), new Vector3f(-0.75f, .25f, 1.2f), true, false, new Brake(80));

        addWheel(wheel_rl.getWheelNode(), new Vector3f(0.75f, .25f, -1.2f), false, false, new Brake(0));
        addWheel(wheel_rr.getWheelNode(), new Vector3f(-0.75f, .25f, -1.2f), false, false, new Brake(0));

        for (int i = 0; i < getNumWheels(); i++) {
            getWheel(i).getSuspension().setRestLength(0.25f);

            getWheel(i).getSuspension().setStiffness(24);
            getWheel(i).getSuspension().setMaxForce(12000);
            getWheel(i).getSuspension().setCompression(0.5f);
            getWheel(i).getSuspension().setDampness(0.65f);
            getWheel(i).setFriction(1.3f);
        }

        // give each wheel a tyre.
        getWheel(0).setTireModel(new Tyre_01());
        getWheel(1).setTireModel(new Tyre_01());
        getWheel(2).setTireModel(new Tyre_01());
        getWheel(3).setTireModel(new Tyre_01());

        getWheel(0).setAccelerationForce(0);
        getWheel(1).setAccelerationForce(0);
        getWheel(2).setAccelerationForce(1);
        getWheel(3).setAccelerationForce(1);

        setHoodCamLocation(new Vector3f(new Vector3f(0, 1, 0.1f)));

        GearBox gearBox = new GearBox(6);
        gearBox.setGear(0, 0, 15);
        gearBox.setGear(1, 15, 30);
        gearBox.setGear(2, 30, 45);
        gearBox.setGear(3, 45, 80);
        gearBox.setGear(4, 80, 140);
        gearBox.setGear(5, 140, 220);

        setGearBox(gearBox);

        Engine engine = new Engine250HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-5.ogg");
        setEngine(engine);

        setHornAudio(assetManager, "Audio/horn-1.ogg");

        build();

    }

}
