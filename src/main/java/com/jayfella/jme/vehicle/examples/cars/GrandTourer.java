package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tyres.Tyre_01;
import com.jayfella.jme.vehicle.examples.wheels.CruiserWheel;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.WheelModel;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;

public class GrandTourer extends Car {

    public GrandTourer(Application app) {
        super(app, "Grand Tourer");

        AssetManager assetManager = app.getAssetManager();

        Spatial chassis = assetManager.loadModel("Models/GT/scene.gltf.j3o");
        chassis.setLocalScale(0.2f);
        setChassis(chassis, 1525);

        WheelModel wheel_fl = new CruiserWheel(assetManager, 0.85f);
        wheel_fl.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_fr = new CruiserWheel(assetManager, 0.85f);

        WheelModel wheel_rl = new CruiserWheel(assetManager, 0.85f);
        wheel_rl.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_rr = new CruiserWheel(assetManager, 0.85f);


        addWheel(wheel_fl.getWheelNode(), new Vector3f(0.85f, .35f, 1.6f), true, false, new Brake(140));
        addWheel(wheel_fr.getWheelNode(), new Vector3f(-0.85f, .35f, 1.6f), true, false, new Brake(140));

        addWheel(wheel_rl.getWheelNode(), new Vector3f(0.85f, .45f, -1.6f), false, false, new Brake(0));
        addWheel(wheel_rr.getWheelNode(), new Vector3f(-0.85f, .45f, -1.6f), false, false, new Brake(0));


        for (int i = 0; i < getNumWheels(); i++) {
            // getWheel(i).getSuspension().setRestLength(0.285f);

            getWheel(i).getSuspension().setStiffness(10);
            getWheel(i).getSuspension().setMaxForce(8000);
            getWheel(i).getSuspension().setCompression(0.33f);
            getWheel(i).getSuspension().setDampness(0.45f);
            getWheel(i).setFriction(1.6f);
        }

        getWheel(0).getSuspension().setRestLength(0.225f);
        getWheel(1).getSuspension().setRestLength(0.225f);
        getWheel(2).getSuspension().setRestLength(0.285f);
        getWheel(3).getSuspension().setRestLength(0.285f);

        // give each wheel a tyre.
        getWheel(0).setTireModel(new Tyre_01());
        getWheel(1).setTireModel(new Tyre_01());
        getWheel(2).setTireModel(new Tyre_01());
        getWheel(3).setTireModel(new Tyre_01());

        getWheel(0).setAccelerationForce(0);
        getWheel(1).setAccelerationForce(0);
        getWheel(2).setAccelerationForce(1);
        getWheel(3).setAccelerationForce(1);

        //getWheel(0).setBrakeForce(80);
        //getWheel(1).setBrakeForce(80);
        //getWheel(2).setBrakeForce(0);
        //getWheel(3).setBrakeForce(0);

        // vehicle.setMaxSpeedMph(50);
        setHoodCamLocation(new Vector3f(new Vector3f(0, 1.5f, 0.5f)));

        GearBox gearBox = new GearBox(5);
        gearBox.setGear(0, 0, 15);
        gearBox.setGear(1, 15, 40);
        gearBox.setGear(2, 40, 75);
        gearBox.setGear(3, 75, 130);
        gearBox.setGear(4, 130, 190);
        // gearBox.setGear(5, 140, 220);

        setGearBox(gearBox);

        Engine engine = new Engine450HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-1.ogg");
        setEngine(engine);

        setHornAudio(assetManager, "Audio/horn-1.ogg");

        build();

    }
}
