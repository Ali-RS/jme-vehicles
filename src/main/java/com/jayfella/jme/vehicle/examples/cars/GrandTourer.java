package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tyres.Tyre_01;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;

public class GrandTourer extends Car {

    Node w_fl_node = new Node("Wheel FL Node");
    Node w_fr_node = new Node("Wheel FR Node");
    Node w_rl_node = new Node("Wheel RL Node");
    Node w_rr_node = new Node("Wheel RR Node");

    private class WheelVisitor extends SceneGraphVisitorAdapter {
        public void visit(Node geom) {
            if (geom.getName().equals("lWheelGroup")) {
                w_fl_node.attachChild(geom);
                geom.setLocalTranslation(0, 0, 0);
                geom.center();
            }
            else if (geom.getName().equals("rWheelGroup")) {
                w_fr_node.attachChild(geom);
                geom.setLocalTranslation(0, 0, 0);
                geom.center();
            }
            else if (geom.getName().equals("slick1")) {
                w_rl_node.attachChild(geom);
                geom.setLocalTranslation(0, 0, 0);
                geom.center();
            }
            else if (geom.getName().equals("slick2")) {
                w_rr_node.attachChild(geom);
                geom.setLocalTranslation(0, 0, 0);
                geom.center();
            }
        }
    }


    public GrandTourer(Application app) {
        super(app, "Dune Buggy");

        AssetManager assetManager = app.getAssetManager();

        Spatial chassis = assetManager.loadModel("Models/GT/scene.gltf.j3o");
        chassis.setLocalScale(0.2f);
        chassis.breadthFirstTraversal(new WheelVisitor());
        setChassis(chassis, 1525);

        w_fl_node.setLocalScale(0.2f);
        w_fr_node.setLocalScale(0.2f);
        w_rl_node.setLocalScale(0.2f);
        w_rr_node.setLocalScale(0.2f);


        addWheel(w_fr_node, new Vector3f(-0.85f, .35f, 1.65f), true, false, new Brake(140));
        addWheel(w_fl_node, new Vector3f(0.85f, .35f, 1.65f), true, false, new Brake(140));
        addWheel(w_rr_node, new Vector3f(-0.85f, .45f, -1.6f), false, false, new Brake(0));
        addWheel(w_rl_node, new Vector3f(0.85f, .45f, -1.6f), false, false, new Brake(0));

        for (int i = 0; i < getNumWheels(); i++) {
            getWheel(i).getSuspension().setRestLength(0.05f);

            getWheel(i).getSuspension().setStiffness(32);
            getWheel(i).getSuspension().setMaxForce(12000);
            getWheel(i).getSuspension().setCompression(0.5f);
            getWheel(i).getSuspension().setDampness(0.65f);
            getWheel(i).setFriction(1.6f);
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
