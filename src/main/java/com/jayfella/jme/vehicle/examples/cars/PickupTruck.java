package com.jayfella.jme.vehicle.examples.cars;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.examples.engines.Engine450HP;
import com.jayfella.jme.vehicle.examples.tyres.Tyre_01;
import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * An example Car vehicle.
 */
public class PickupTruck extends Car {

    public PickupTruck(Application app) {
        super(app, "Pickup Truck");

        AssetManager assetManager = app.getAssetManager();

        // the chassis model, aligned in blender so it points "forward".
        // see here for blender alignment: https://i.ibb.co/jrBtz5K/image.png
        Spatial chassis = assetManager.loadModel("Models/Vehicles/Chassis/Pickup2/pickup.j3o");
        Material chassisMaterial = assetManager.loadMaterial("Materials/Vehicles/Pickup.j3m");
        chassis.setMaterial(chassisMaterial);

        //Node chassisNode = new Node("Chassis");
        //chassis.setLocalTranslation(0, 0.5f, 0);
        //chassisNode.setLocalTranslation(0, -1, 0);
        //chassisNode.attachChild(chassis);

        // Set the mass of the chassis. This is the overall weight.
        setChassis(chassis, 1758);

        // the wheel model, aligned in blender so that the left side of the vehicle requires no rotation.
        // see here for blender alignment: https://i.ibb.co/56Bf0jW/image.png
        // if our wheel models have a diameter of 1 wu it's going to make it a LOT easier resizing them.
        Spatial wheel = assetManager.loadModel("Models/Vehicles/Wheel/Wheel_1/wheel.j3o");
        Material wheelMaterial = assetManager.loadMaterial("Materials/Vehicles/Wheel_1.j3m");
        wheel.setMaterial(wheelMaterial);

        // Add each wheel. The order is not important, but lets do it FL, FR, RL, RR.
        // In this vehicle we're just going to clone the wheel above for every wheel.
        // We're NOT going to clone the material, but if you're implementing damage, you might want to.

        Node w_fl_node = new Node("Wheel FL Node");
        Spatial w_fl = wheel.clone(false);
        w_fl.setMaterial(wheelMaterial);
        w_fl.rotate(0, 0, 0);
        w_fl_node.attachChild(w_fl);

        Node w_fr_node = new Node("Wheel FR Node");
        Spatial w_fr = wheel.clone(false);
        w_fr.setMaterial(wheelMaterial);
        w_fr.rotate(0, FastMath.PI, 0);
        w_fr_node.attachChild(w_fr);

        Node w_rl_node = new Node("Wheel RL Node");
        Spatial w_rl = wheel.clone(false);
        w_rl.setMaterial(wheelMaterial);
        w_rl.rotate(0, 0, 0);
        w_rl_node.attachChild(w_rl);

        Node w_rr_node = new Node("Wheel RR Node");
        Spatial w_rr = wheel.clone(false);
        w_rr.setMaterial(wheelMaterial);
        w_rr.rotate(0, FastMath.PI, 0);
        w_rr_node.attachChild(w_rr);

        // set the scale of the wheels.
        w_fr_node.setLocalScale(1.1f);
        w_fl_node.setLocalScale(1.1f);
        w_rr_node.setLocalScale(1.1f);
        w_rl_node.setLocalScale(1.1f);

        // add the wheels, setting the position, whether or not they steer, and a brake with force.
        // if you want rear-wheel steering, you will also want to "flip" the steering.

        addWheel(w_fl_node, new Vector3f(0.8f, .5f, 1.7f), true, false, new Brake(90));
        addWheel(w_fr_node, new Vector3f(-.8f, .5f, 1.7f), true, false, new Brake(90));

        addWheel(w_rl_node, new Vector3f(0.8f, .5f, -1.7f), false, false, new Brake(90));
        addWheel(w_rr_node, new Vector3f(-.8f, .5f, -1.7f), false, false, new Brake(90));

        // configure the suspension.
        // In this car we're setting the same settings for each wheel, but you don't have to.
        for (int i = 0; i < getNumWheels(); i++) {

            // the rest-length or "height" of the suspension.
            getWheel(i).getSuspension().setRestLength(.51f);

            getWheel(i).getVehicleWheel().setMaxSuspensionTravelCm(1000);

            // how much force the suspension can take before it bottoms out.
            // setting this too low will make the wheels sink into the ground.
            getWheel(i).getSuspension().setMaxForce(20000);

            // the stiffness of the suspension.
            // setting this too soft can cause odd behavior.
            getWheel(i).getSuspension().setStiffness(9);

            // how fast the suspension will compress.
            // 1 = slow, 0 = fast.
            getWheel(i).getSuspension().setCompression(0.2f);

            // how quickly the suspension will rebound back to height.
            // 1 = slow, 0 = fast.
            getWheel(i).getSuspension().setDampness(0.3f);
        }

        // give each wheel a tyre.
        getWheel(0).setTireModel(new Tyre_01());
        getWheel(1).setTireModel(new Tyre_01());
        getWheel(2).setTireModel(new Tyre_01());
        getWheel(3).setTireModel(new Tyre_01());

        // the friction of the tyre.
        getWheel(0).setFriction(1f);
        getWheel(1).setFriction(1f);
        getWheel(2).setFriction(1f);
        getWheel(3).setFriction(1f);

        // define how much power each wheel gets. 0 = no power, 1 = full power.
        // the order you added your wheels comes into play here. We added our front wheels first and rear last.
        // this dictates whether you have 4WD, RWD, FWD, etc.. You could do a 60/40 mix or whatever.
        getWheel(0).setAccelerationForce(1);
        getWheel(1).setAccelerationForce(1);
        getWheel(2).setAccelerationForce(1);
        getWheel(3).setAccelerationForce(1);

        // set the "hood" cam of the vehicle. I'm not happy with this. It shouldn't really be here....
        setHoodCamLocation(new Vector3f(0, 1.7f, 1.3f));

        // define a gearbox. Each gear does NOT need to begin where the last one ends.
        // the "end" value of the last gear will dictate the maximum speed this vehicle can go.
        GearBox gearBox = new GearBox(new Gear[] {
                new Gear(0, 19),
                new Gear(15, 48),
                new Gear(35, 112),
                new Gear(100, 192),
                new Gear(180, 254),
        });

        setGearBox(gearBox);

        // define an engine.
        // there are 3 example engines in the com.jayfella.jme.vehicle.examples.engines package.
        // In this implementation we define power, max revs and a power band.
        // See the Engine450HP class for more information.
        Engine engine = new Engine450HP(this);
        engine.setEngineAudio(assetManager, "Audio/engine-1.ogg");
        setEngine(engine);

        // set the horn audio file. beep beep, richie.
        setHornAudio(assetManager, "Audio/horn-1.ogg");

        // this MUST be called last.
        // in the car implementation it initializes the skidmarks and smoke emitters for each wheel.
        build();
    }
}
