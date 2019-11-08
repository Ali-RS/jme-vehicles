package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Quad;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;

public class MagicFormulaState extends BaseAppState {

    private final Car vehicle;

    private Node rootNode;
    private Node guiNode;

    // private Node vehicleDataNode;
    private Container vehicleDataContainer;
    private Geometry vehicle2D;
    private Label crosshair;
    private Geometry centerOfGravity;

    // weight per wheel labels
    private Label[] tyreWeightLabels;


    public MagicFormulaState(Car vehicle) {
        this.vehicle = vehicle;
    }

    private Vector3f screenCenter = new Vector3f();

    private void createCenterOfGravityControl() {

        crosshair = new Label("+");
        crosshair.setColor(ColorRGBA.Pink);
        crosshair.setLocalTranslation(screenCenter.add(-crosshair.getPreferredSize().x * 0.5f, crosshair.getPreferredSize().y * 0.5f, 0));

        centerOfGravity = new Geometry("Center of Mass", new Quad(8, 8));
        centerOfGravity.setMaterial(new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
        centerOfGravity.getMaterial().setColor("Color", ColorRGBA.Yellow);
        // guiNode.attachChild(centerOfGravity);

    }

    private void updateCenterOfGravityControl() {

        Vector3f newLoc = new Vector3f();

        float[] rotation = new float[3];
        // vehicle.getVehicleControl().getPhysicsRotation().toAngles(rotation);
        vehicle.getChassis().getWorldRotation().toAngles(rotation);


        // rotation along the X axis. forward and backward rotation.
        // positive = forward, negative = backward.
        float rotX = rotation[0] / FastMath.QUARTER_PI;
        float movementY = rotX * screenCenter.y;

        // rotation along the Z axis.
        float rotY = rotation[2] / FastMath.QUARTER_PI;//(FastMath.QUARTER_PI * 0.5f);
        float movementX = rotY * screenCenter.x;

        newLoc.set(movementX, movementY, 0);

        //System.out.println(Arrays.toString(rotation));

        centerOfGravity.setLocalTranslation(screenCenter.add(newLoc).subtract(2,2, 0));
    }

    private void createTyreWeightLabels() {

        tyreWeightLabels = new Label[vehicle.getNumWheels()];

        for (int i = 0; i < tyreWeightLabels.length; i++) {

            Label label = new Label("wheel");

            // BoundingBox chassisBB = (BoundingBox) vehicle.getChassis().getWorldBound();
            // loc.addLocal(chassisBB.getXExtent() * 0.5f, chassisBB.getZExtent() * 0.5f, 0);

            if (i == 0 || i == 2) {
                label.setTextHAlignment(HAlignment.Right);
            }

            label.setInsets(new Insets3f(10, 10, 10, 10));

            tyreWeightLabels[i] = label;
        }

        vehicleDataContainer.addChild(tyreWeightLabels[0], 0, 0);
        vehicleDataContainer.addChild(tyreWeightLabels[1], 0, 1);
        vehicleDataContainer.addChild(tyreWeightLabels[2], 1, 0);
        vehicleDataContainer.addChild(tyreWeightLabels[3], 1, 1);

        // guiNode.attachChild(vehicleDataContainer);
    }

    // private boolean centerOfGravityEnabled = false;
    // private boolean vehicleDataEnabled = false;

    public boolean isCenterOfGravityEnabled() {

        if (centerOfGravity != null) {
            return centerOfGravity.getParent() != null;
        }

        return false;
    }

    public void setCenterOfGravityEnabled(boolean centerOfGravityEnabled) {
        // this.centerOfGravityEnabled = centerOfGravityEnabled;

        if (centerOfGravityEnabled) {
            guiNode.attachChild(centerOfGravity);
            guiNode.attachChild(crosshair);
        }
        else {
            centerOfGravity.removeFromParent();
            crosshair.removeFromParent();
        }
    }

    public boolean isVehicleDataEnabled() {

        if (vehicleDataContainer != null) {
            return vehicleDataContainer.getParent() != null;
        }

        return false;
    }

    public void setVehicleDataEnabled(boolean vehicleDataEnabled) {
        // this.vehicleDataEnabled = vehicleDataEnabled;

        if (vehicleDataEnabled) {
            ((SimpleApplication)getApplication()).getGuiNode().attachChild(vehicleDataContainer);
        }
        else {
            vehicleDataContainer.removeFromParent();
        }
    }

    @Override
    protected void initialize(Application app) {
        rootNode = ((SimpleApplication)app).getRootNode();
        guiNode = ((SimpleApplication)app).getGuiNode();

        screenCenter.set(app.getCamera().getWidth() * 0.5f, app.getCamera().getHeight() * 0.5f, 1.0f);

        vehicleDataContainer = new Container();
        vehicleDataContainer.setLocalTranslation(20, getApplication().getCamera().getHeight() - 250, 1);
        createCenterOfGravityControl();
        createTyreWeightLabels();


    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

        maxLoad = new float[vehicle.getNumWheels()];

        for (int i = 0; i < maxLoad.length; i++) {
            maxLoad[i] = vehicle.getWheel(i).getFriction();
        }
    }

    @Override
    protected void onDisable() {
        setCenterOfGravityEnabled(false);
        setVehicleDataEnabled(false);
    }

    private Geometry createArrow(Vector3f dir, ColorRGBA color) {
        Arrow arrow = new Arrow(dir);
        Geometry geometry = new Geometry("arrow", arrow);
        geometry.setMaterial(new Material(vehicle.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
        geometry.getMaterial().getAdditionalRenderState().setWireframe(true);
        geometry.getMaterial().getAdditionalRenderState().setLineWidth(4);
        geometry.getMaterial().setColor("Color", color);

        return geometry;
    }

    private Geometry wheelDirArrow;
    private Geometry vehicleDirArrow;
    private Geometry vehicleTravelArrow;

    private float[] maxLoad;

    // LATERAL
    // the slip angle is the angle between the direction in which a wheel is pointing
    // and the direction in which the vehicle is traveling.
    private float calculateLateralSlipAngle(Wheel wheel) {

        Quaternion wheelRot = vehicle.getRotation().mult(
                new Quaternion().fromAngles(new float[]{0, wheel.getSteeringAngle(), 0}));

        Vector3f wheelDir = wheelRot.getRotationColumn(2);

        Vector3f vehicleTravel;

        if (vehicle.getSpeed(Vehicle.SpeedUnit.KMH) < 5) {
            vehicleTravel = vehicle.getVehicleControl().getPhysicsRotation().getRotationColumn(2);
        }
        else {
            vehicleTravel = vehicle.getVehicleControl().getLinearVelocity().normalizeLocal();
            vehicleTravel.setY(0);
        }

        float angle = wheelDir.angleBetween(vehicleTravel);
        // System.out.println(wheel.getVehicleWheel().getWheelSpatial().getName() + ": " + angle * FastMath.RAD_TO_DEG);

        angle = Math.max(0.1f, angle);

        return angle;

    }

    // calculate the amount of weight on the wheel.
    private float calculateWheelLoad(int i) {

        float[] chassisAngles = new float[3];
        vehicle.getChassis().getWorldRotation().toAngles(chassisAngles);

        // negative angle = weight on back wheels.
        // positive angle = weight on front wheels.
        float xAngle = chassisAngles[0];

        // negative angle = left side.
        // positive angle = right side.
        float zAngle = chassisAngles[2];

        // the load starts by sharing it with each wheel. One quarter per wheel.
        float load = vehicle.getVehicleControl().getMass();// * 0.25f;

        // front-back

        // so the angle is in radians. at 90 degrees it should take full load.

        if (i < 2) {
            if (xAngle > 0) {
                load += (vehicle.getVehicleControl().getMass()) * (xAngle / FastMath.QUARTER_PI);
            }
        }
        else {
            if (xAngle < 0) {
                load += (vehicle.getVehicleControl().getMass()) * (-xAngle / FastMath.QUARTER_PI);
            }
        }

        // left-right
        if ((i + 1) % 2 == 0) {
            if (zAngle > 0) {
                // load += (vehicle.getVehicleControl().getMass() * 1.25) * -zAngle;
                load += (vehicle.getVehicleControl().getMass()) * (-zAngle / FastMath.QUARTER_PI);
            }
        }
        else {
            if (zAngle < 0) {
                //load += (vehicle.getVehicleControl().getMass() * 1.25) * zAngle;
                load += (vehicle.getVehicleControl().getMass()) * (zAngle / FastMath.QUARTER_PI);
            }
        }

        return load;
    }

    @Override
    public void update(float tpf) {

        //if (isCenterOfGravityEnabled()) {
            updateCenterOfGravityControl();
        //}

        //if (isVehicleDataEnabled()) {
            for (int i = 0; i < vehicle.getNumWheels(); i++) {

                Wheel wheel = vehicle.getWheel(i);

                // the angle between the dir of the wheel and the dir the vehicle is travelling.
                float lateralSlip = wheel.calculateLateralSlipAngle();

                float load = 10000; // * (wheel.getAccelerationForce() * vehicle.getAccelerationForce());

                wheel.getTireModel().setLoad(load);

                // returns the amount of force in N on the tyre.
                // this model allows max 10,000
                float lateral = wheel.getTireModel().calcLateralTireForce(lateralSlip);

                // the slip angle for this is how much force is being applied to the tyre (acceleration force).

                float longSlip = wheel.calculateLongitudinalSlipAngle();
                float longitudinal = wheel.getTireModel().calcLongtitudeTireForce(longSlip);

                // System.out.println(longitudinal);
                // float friction = lateral / 10000;

                // float friction = wheel.getTireModel().calculateFrictionCircle();
                float friction = 1.0f - ((lateral / 10000) - (longitudinal / 10000));
                wheel.setFriction(friction * 2.0f);
                // wheel.setFriction( lateral / 5000 );

                // String format = "Weight: %.2f\nLat: %.2f\nLong: %.2f\nFriction: %.2f\nSlip: %.2f";
                String format = "Lat: %.2f\nLong: %.2f\nFriction: %.2f\nSlip: %.2f";

                tyreWeightLabels[i].setText(String.format(format,
                        // wheel.getTireModel().getLoad(),
                        lateral / 10000,
                        longitudinal / 10000,
                        wheel.getFriction(),
                        1.0f - wheel.getVehicleWheel().getSkidInfo()
                ));

            }
        //}

    }

}
