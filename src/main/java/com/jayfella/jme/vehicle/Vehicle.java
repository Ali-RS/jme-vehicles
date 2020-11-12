package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.engine.Engine;
import com.jayfella.jme.vehicle.gui.ReturnToMenuClickCommand;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.core.GuiComponent;

/**
 * A vehicle that may contain wheels and other propellants.
 */
public abstract class Vehicle {

    public enum SpeedUnit { KMH, MPH }

    public static final float KMH_TO_MPH = 0.62137f;
    public static final float MPH_TO_KMH = 1.60934f;

    private final Application app;

    private final Node node;
    private VehicleControl vehicleControl;

    private String name;

    private Spatial chassis;
    private Engine engine;
    private GearBox gearBox;

    private Button rtmmButton;
    private SpeedometerState speedo;
    private TachometerState tacho;
    private AutomaticGearboxState gearboxState;
    private VehicleAudioState vehicleAudioState;

    private final Vector3f hoodCamLoc = new Vector3f();

    private AudioNode hornAudio;

    private boolean parkingBrakeApplied;

    public Vehicle(Application app, String name) {
        this.app = app;
        this.name = name;
        this.node = new Node("Vehicle: " + name);
    }

    public Application getApplication() {
        return app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.node.setName("Vehicle: " + name);
    }

    public AudioNode getHornAudio() {
        return hornAudio;
    }

    public void setHornAudio(AssetManager assetManager, String audioFile) {
        this.hornAudio = new AudioNode(assetManager, audioFile, AudioData.DataType.Stream);
        this.hornAudio.setLooping(false);
        this.hornAudio.setPositional(true);
        this.hornAudio.setDirectional(false);
        this.node.attachChild(this.hornAudio);
    }

    public void pressHorn() {
        vehicleAudioState.playHornSound();
    }

    public Spatial getChassis() {
        return chassis;
    }

    public void setChassis(Spatial chassis, float mass) {
        this.chassis = chassis;
        CollisionShape chassisCollisionShape = CollisionShapeFactory.createDynamicMeshShape(chassis);
        this.vehicleControl = new VehicleControl(chassisCollisionShape, mass);
        this.node.addControl(this.vehicleControl);
        node.attachChild(chassis);
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
        this.node.attachChild(this.engine.getEngineAudio());
    }

    public void setEngineStarted(boolean started) {
        if (started) {
            startEngine();
        }
        else {
            stopEngine();
        }
    }

    public void startEngine() {
        if (!engine.isStarted()) {
            engine.setStarted(true);
            vehicleAudioState.playEngineSound();
        }
    }

    public void stopEngine() {
        if (engine.isStarted()) {
            engine.setStarted(false);
            vehicleAudioState.stopEngineSound();
        }
    }

    public GearBox getGearBox() {
        return gearBox;
    }

    public void setGearBox(GearBox gearBox) {
        this.gearBox = gearBox;
    }

    private float accelerationForce;

    /**
     * Accelerate the vehicle with the given power.
     * @param strength a unit value between 0.0 - 1.0. Essentially how "hard" you want to accelerate.
     */
    public void accelerate(float strength) {
        this.accelerationForce = strength;
    }

    public float getAccelerationForce() {
        return this.accelerationForce;
    }

    /**
     * Apply the vehicle brakes at the given strength.
     * @param strength a unit value between 0.0 - 1.0.
     */
    public abstract void brake(float strength);

    public abstract void handbrake(float strength);

    public abstract void steer(float strength);

    public Vector3f getLocation() {
        return node.getLocalTranslation();
    }

    public void setLocation(Vector3f loc) {
        vehicleControl.setPhysicsLocation(loc);
    }

    public Quaternion getRotation() {
        return node.getLocalRotation();
    }

    public void setRotation(Quaternion rotation) {
        vehicleControl.setPhysicsRotation(rotation);
    }

    public Node getNode() {
        return node;
    }

    public VehicleControl getVehicleControl() {
        return vehicleControl;
    }


    public float getSpeed(SpeedUnit speedUnit) {
        switch (speedUnit) {
            case KMH: return this.vehicleControl.getCurrentVehicleSpeedKmHour();
            case MPH: return this.vehicleControl.getCurrentVehicleSpeedKmHour() * KMH_TO_MPH;
            default: return -1;
        }
    }

    /**
     * Show the "Return to Main Menu" button.
     */
    public void showRtmmButton() {
        removeRtmmButton();

        rtmmButton = new Button("Return to Main Menu");
        rtmmButton.setFontSize(16f);
        GuiComponent background = rtmmButton.getBackground();
        ((TbtQuadBackgroundComponent) background).setMargin(10f, 5f);
        rtmmButton.addClickCommands(new ReturnToMenuClickCommand((Car) this));
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        Camera cam = simpleApp.getCamera();
        rtmmButton.setLocalTranslation(
                cam.getWidth() - rtmmButton.getPreferredSize().x - 40f,
                cam.getHeight() - 20f,
                1f
        );
        simpleApp.getGuiNode().attachChild(rtmmButton);
    }

    /**
     * Hide the "Return to Main Menu" button.
     */
    public void removeRtmmButton() {
        if (rtmmButton != null) {
            rtmmButton.removeFromParent();
        }
    }

    public void showSpeedo(SpeedUnit speedUnit) {
        removeSpeedo();

        this.speedo = new SpeedometerState(this, speedUnit);
        app.getStateManager().attach(this.speedo);
    }

    public void removeSpeedo() {
        if (this.speedo != null) {
            app.getStateManager().detach(this.speedo);
            this.speedo = null;
        }
    }

    public void showTacho() {
        removeTacho();

        this.tacho = new TachometerState(this);
        app.getStateManager().attach(this.tacho);
    }

    public void removeTacho() {
        if (this.tacho != null) {
            app.getStateManager().detach(this.tacho);
            this.tacho = null;
        }
    }

    // I feel like camera positions shouln't be part of this...
    public Vector3f getHoodCamLocation() {
        return this.hoodCamLoc;
    }

    protected void setHoodCamLocation(Vector3f loc) {
        this.hoodCamLoc.set(loc);
    }

    public boolean isParkingBrakeApplied() {
        return this.parkingBrakeApplied;
    }

    public void setParkingBrakeApplied(boolean applied) {
        this.parkingBrakeApplied = applied;
    }

    public void attachToScene(Node parent, PhysicsSpace physicsSpace) {
        enable();

        parent.attachChild(node);
        physicsSpace.add(vehicleControl);
    }

    public void detachFromScene() {
        disable();

        node.removeFromParent();
        vehicleControl.getPhysicsSpace().remove(vehicleControl);
    }

    /**
     * Should be called last when all vehicle parts have been built and added.
     */
    protected void build() {
        this.gearboxState = new AutomaticGearboxState(this);
        this.vehicleAudioState = new VehicleAudioState(this);

        app.getStateManager().attach(gearboxState);
        app.getStateManager().attach(vehicleAudioState);
    }

    protected void enable() {
        app.getStateManager().attach(gearboxState);
        app.getStateManager().attach(vehicleAudioState);
    }

    protected void disable() {
        app.getStateManager().detach(gearboxState);
        app.getStateManager().detach(vehicleAudioState);
        removeSpeedo();
    }

    public abstract void applyEngineBraking();
    public abstract void removeEngineBraking();

}
