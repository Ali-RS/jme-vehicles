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
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * An example Car vehicle.
 */
public class PickupTruck extends Car {

    public PickupTruck(Application app) {
        super(app, "Pickup Truck");
    }

    /**
     * Load this Vehicle from assets. TODO re-order methods
     */
    @Override
    public void load() {
        AssetManager assetManager = Main.getApplication().getAssetManager();

        // the chassis model, aligned in blender so it points "forward".
        // see here for blender alignment: https://i.ibb.co/jrBtz5K/image.png
        Spatial chassis = assetManager.loadModel("Models/Vehicles/Chassis/Pickup2/pickup.j3o");
        Material chassisMaterial = assetManager.loadMaterial("Materials/Vehicles/Pickup.j3m");
        chassis.setMaterial(chassisMaterial);

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

        WheelModel wheel_fl = new BasicAlloyWheel(assetManager, 1.1f);

        WheelModel wheel_fr = new BasicAlloyWheel(assetManager, 1.1f);
        wheel_fr.getSpatial().rotate(0, FastMath.PI, 0);

        WheelModel wheel_rl = new BasicAlloyWheel(assetManager, 1.1f);

        WheelModel wheel_rr = new BasicAlloyWheel(assetManager, 1.1f);
        wheel_rr.getSpatial().rotate(0, FastMath.PI, 0);

        // add the wheels, setting the position, whether or not they steer, and a brake with force.
        // if you want rear-wheel steering, you will also want to "flip" the steering.

        addWheel(wheel_fl.getWheelNode(), new Vector3f(0.8f, .5f, 1.7f), true, false, new Brake(90));
        addWheel(wheel_fr.getWheelNode(), new Vector3f(-.8f, .5f, 1.7f), true, false, new Brake(90));

        addWheel(wheel_rl.getWheelNode(), new Vector3f(0.8f, .5f, -1.7f), false, false, new Brake(90));
        addWheel(wheel_rr.getWheelNode(), new Vector3f(-.8f, .5f, -1.7f), false, false, new Brake(90));

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
            getWheel(i).getSuspension().setDamping(0.3f);
        }

        // give each wheel a tire.
        getWheel(0).setTireModel(new Tire_01());
        getWheel(1).setTireModel(new Tire_01());
        getWheel(2).setTireModel(new Tire_01());
        getWheel(3).setTireModel(new Tire_01());

        // the friction of the tire.
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

        super.setHornAudio("Audio/horn-1.ogg");

        // this MUST be called last.
        // in the car implementation it initializes the skidmarks and smoke emitters for each wheel.
        build();
    }
    // *************************************************************************
    // Vehicle methods

    /**
     * Determine the offset of the truck's DashCamera.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    public Vector3f dashCamOffset() {
        return new Vector3f(0f, 1.7f, 1.3f);
    }

    /**
     * Determine the offset of the truck's ChaseCamera target.
     *
     * @return a new offset vector (in scaled shape coordinates)
     */
    @Override
    protected Vector3f targetOffset() {
        return new Vector3f(0f, 0.77f, -2.67f);
    }
}
