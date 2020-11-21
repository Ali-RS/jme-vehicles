package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.simsilica.lemur.Label;
import jme3utilities.math.MyMath;

/**
 * Appstate to manage an analog speedometer in the DriverHud.
 */
public class SpeedometerState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * important needle rotations (measured CCW from +X, in radians)
     */
    final private static float thetaMin = -1f; // "pegged" to the right
    final private static float theta0 = FastMath.PI - thetaMin; // speed=0
    // *************************************************************************
    // fields

    private float prevTheta = theta0;
    private Label gearLabel;
    private Label speedLabel;
    private Node guiNode;
    final private Node needleNode = new Node("Speedometer Needle");
    final private Node node;
    /**
     * reusable temporary Quaternion
     */
    final private Quaternion tmpRotation = new Quaternion();

    final private Vehicle.SpeedUnit speedUnit;
    /**
     * corresponding Vehicle
     */
    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled speedometer for the specified Vehicle.
     *
     * @param vehicle the corresponding Vehicle (not null)
     * @param speedUnit the units to display (not null)
     */
    public SpeedometerState(Vehicle vehicle, Vehicle.SpeedUnit speedUnit) {
        this.vehicle = vehicle;
        this.speedUnit = speedUnit;

        node = new Node("Speedometer for " + vehicle.getName());
        node.setQueueBucket(RenderQueue.Bucket.Gui);
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked during initialization once this AppState is attached but
     * before onEnable() is called.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        guiNode = ((SimpleApplication) app).getGuiNode();
        AssetManager assetManager = app.getAssetManager();

        node.attachChild(needleNode);
        needleNode.setLocalTranslation(100f, 100f, 1f);

        Node fixedNode = createFixedNode(assetManager);
        node.attachChild(fixedNode);

        String needlePath = "Textures/Vehicles/Speedometer/speedo_needle_2.png";
        Texture needleTexture = assetManager.loadTexture(needlePath);
        Image image = needleTexture.getImage();
        int width = image.getWidth();
        int height = image.getHeight();
        Quad needleMesh = new Quad(width, height);
        Geometry needleGeometry = new Geometry("Speedometer Needle", needleMesh);
        needleNode.attachChild(needleGeometry);

        Material material
                = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        needleGeometry.setMaterial(material);
        material.setTexture("ColorMap", needleTexture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        needleGeometry.setLocalTranslation(
                -(width / 2f),
                -(width / 2f) - 7f,
                0f);

        speedLabel = new Label(speedUnit.toString());
        node.attachChild(speedLabel);
        speedLabel.setColor(TachometerState.labelColor);
        speedLabel.setLocalTranslation(70f, 30f, 1f);

        node.setLocalTranslation(
                app.getCamera().getWidth() - 200f - 20f,
                20f,
                0f
        );

        gearLabel = new Label("N");
        gearLabel.setColor(TachometerState.labelColor);
        gearLabel.setLocalTranslation(
                100f - (gearLabel.getPreferredSize().x * 0.5f),
                speedLabel.getPreferredSize().y + 45f,
                1f
        );
        node.attachChild(gearLabel);
    }

    /**
     * Callback invoked when this AppState was previously enabled but is now
     * disabled either because setEnabled(false) was called or the state is
     * being cleaned up.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    /**
     * Callback invoked when this AppState becomes fully enabled, ie: is
     * attached and isEnabled() is true or when the setEnabled() status changes
     * after the state is attached.
     */
    @Override
    protected void onEnable() {
        guiNode.attachChild(node);
    }

    /**
     * Called to update this AppState, invoked once per frame when the AppState
     * is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        float speed = vehicle.getSpeed(speedUnit);
        float maxSpeed = vehicle.getGearBox().getMaxSpeed(speedUnit);
        float speedFraction = speed / maxSpeed;
        float theta = MyMath.lerp(speedFraction, theta0, thetaMin);
        /*
         * a slight lag, because a physical needle cannot pivot instantly
         */
        prevTheta = FastMath.interpolateLinear(0.5f, prevTheta, theta);

        prevTheta = FastMath.clamp(prevTheta, thetaMin, theta0);
        tmpRotation.fromAngles(0f, 0f, prevTheta - FastMath.HALF_PI);
        needleNode.setLocalRotation(tmpRotation);
        /*
         * update the Lemur labels, which are mainly for testing
         */
        String unit = speedUnit.toString().toLowerCase();
        String labelText = String.format("%.0f %s", FastMath.abs(speed), unit);
        speedLabel.setText(labelText);
        int gearNum = vehicle.getGearBox().getActiveGearNum();
        labelText = Integer.toString(gearNum + 1);
        gearLabel.setText(labelText);
    }
    // *************************************************************************
    // private methods

    private Node buildNumNode(float maxSpeed, int stepSpeed, float radius) {
        Node numNode = new Node("Speedometer Numbers");

        for (int intSpeed = 0;; intSpeed += stepSpeed) {
            float fraction = intSpeed / maxSpeed;
            float theta = MyMath.lerp(fraction, theta0, thetaMin);
            if (theta < thetaMin) {
                break;
            }
            String text = Integer.toString(intSpeed);
            Label label = new Label(text);
            numNode.attachChild(label);
            label.setColor(ColorRGBA.White);

            Vector3f size = label.getPreferredSize();
            float x = radius * FastMath.cos(theta) - size.x / 2f;
            float y = radius * FastMath.sin(theta) + size.y / 2f;
            label.setLocalTranslation(x, y, 0f);
        }

        return numNode;
    }

    /**
     * Build the Node for the fixed parts of the speedometer, including the
     * background and numbers.
     */
    private Node createFixedNode(AssetManager assetManager) {
        String path = "Textures/Vehicles/Speedometer/speedo_bg_2.png";
        Texture backgroundTexture = assetManager.loadTexture(path);
        Image image = backgroundTexture.getImage();
        int height = image.getHeight();
        int width = image.getWidth();

        Geometry backgroundGeom = new Geometry("Speedometer Background",
                new Quad(width, height));
        backgroundGeom.setLocalTranslation(-width / 2f, -height / 2f, -1f);

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundGeom.setMaterial(material);
        material.setTexture("ColorMap", backgroundTexture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        int maxSpeed = (int) vehicle.getGearBox().getMaxSpeed(speedUnit);
        int stepSpeed = 10 * (1 + maxSpeed / 160);
        Node numNode = buildNumNode(maxSpeed, stepSpeed, width / 2f - 20f);
        numNode.attachChild(backgroundGeom);
        numNode.setLocalTranslation(width / 2f, height / 2f, -1f);

        return numNode;
    }
}
