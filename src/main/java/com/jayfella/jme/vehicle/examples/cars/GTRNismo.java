package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tyres.Tyre_01;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class GTRNismo extends Car {

    private final float scale = 0.01f;

    public GTRNismo(Application app) {
        super(app, "Dune Buggy");

        AssetManager assetManager = app.getAssetManager();

        Spatial chassis = assetManager.loadModel("Models/gtr_nismo/scene.gltf.j3o");
        chassis.setLocalScale(scale);
        // chassis.breadthFirstTraversal(new WheelVisitor());
        setChassis(chassis, 1525);

        WheelModel wheel_fl = new DarkAlloyWheel(assetManager, 0.75f);
        wheel_fl.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_fr = new DarkAlloyWheel(assetManager, 0.75f);

        WheelModel wheel_rl = new DarkAlloyWheel(assetManager, 0.75f);
        wheel_rl.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_rr = new DarkAlloyWheel(assetManager, 0.75f);

        addWheel(wheel_fl.getWheelNode(), new Vector3f(0.8f, 0.1f, 1.4f), true, false, new Brake(140));
        addWheel(wheel_fr.getWheelNode(), new Vector3f(-0.8f, 0.1f, 1.4f), true, false, new Brake(140));

        addWheel(wheel_rl.getWheelNode(), new Vector3f(0.8f, 0.1f, -1.4f), false, false, new Brake(0));
        addWheel(wheel_rr.getWheelNode(), new Vector3f(-0.8f, 0.1f, -1.4f), false, false, new Brake(0));

        for (int i = 0; i < getNumWheels(); i++) {
            getWheel(i).getSuspension().setRestLength(0.01f);

            getWheel(i).getSuspension().setStiffness(12.5f);
            getWheel(i).getSuspension().setMaxForce(7000);
            getWheel(i).getSuspension().setCompression(0.3f);
            getWheel(i).getSuspension().setDampness(0.4f);
            getWheel(i).setFriction(1.6f);
        }

        // give each wheel a tyre.
        getWheel(0).setTireModel(new Tyre_01());
        getWheel(1).setTireModel(new Tyre_01());
        getWheel(2).setTireModel(new Tyre_01());
        getWheel(3).setTireModel(new Tyre_01());

        getWheel(0).setAccelerationForce(1);
        getWheel(1).setAccelerationForce(1);
        getWheel(2).setAccelerationForce(1);
        getWheel(3).setAccelerationForce(1);

        //getWheel(0).setBrakeForce(80);
        //getWheel(1).setBrakeForce(80);
        //getWheel(2).setBrakeForce(0);
        //getWheel(3).setBrakeForce(0);

        // vehicle.setMaxSpeedMph(50);
        setHoodCamLocation(new Vector3f(new Vector3f(0, 1.5f, 0.5f)));

        GearBox gearBox = new GearBox(5);
        gearBox.setGear(0, 0, 30);
        gearBox.setGear(1, 30, 70);
        gearBox.setGear(2, 70, 130);
        gearBox.setGear(3, 130, 190);
        gearBox.setGear(4, 190, 255);
        // gearBox.setGear(5, 140, 220);

        setGearBox(gearBox);

        Engine engine = new Engine450HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-1.ogg");
        setEngine(engine);

        setHornAudio(assetManager, "Audio/horn-1.ogg");

        build();

    }
}