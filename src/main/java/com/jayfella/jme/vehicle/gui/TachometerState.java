package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.engine.Engine;
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
 * Appstate to manage an analog tachometer in the DriverHud.
 */
public class TachometerState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * important needle rotations (measured CCW from +X, in radians)
     */
    final private static float thetaMin = -1f; // "pegged" to the right
    final private static float theta0 = FastMath.PI - thetaMin; // 0 rpms
    final private static float thetaRedline = 0f;
    /**
     * color for Lemur labels
     */
    final public static ColorRGBA labelColor
            = new ColorRGBA(66 / 255f, 244 / 255f, 241 / 255f, 1f);
    // *************************************************************************
    // fields

    private float prevTheta = theta0;
    private Label revsLabel;
    private Node guiNode;
    final private Node needleNode = new Node("Tachometer Needle");
    final private Node node;
    /**
     * reusable temporary Quaternion
     */
    final private Quaternion tmpRotation = new Quaternion();
    /**
     * corresponding Vehicle
     */
    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled tachometer for the specified Vehicle.
     *
     * @param vehicle the corresponding Vehicle (not null)
     */
    public TachometerState(Vehicle vehicle) {
        this.vehicle = vehicle;

        node = new Node("Tachometer for " + vehicle.getName());
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

        Node fixedNode = buildFixedNode(assetManager);
        node.attachChild(fixedNode);

        String needlePath = "Textures/Vehicles/Speedometer/speedo_needle_2.png";
        Texture needleTexture = assetManager.loadTexture(needlePath);
        Image image = needleTexture.getImage();
        int width = image.getWidth();
        int height = image.getHeight();
        Quad needleMesh = new Quad(width, height);
        Geometry needleGeometry = new Geometry("Tachometer Needle", needleMesh);
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

        revsLabel = new Label("RPM");
        node.attachChild(revsLabel);
        revsLabel.setColor(labelColor);
        revsLabel.setLocalTranslation(70f, 30f, 1f);

        node.setLocalTranslation(
                app.getCamera().getWidth() - 400f - 40f,
                20f,
                0f
        );
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
        Engine engine = vehicle.getEngine();
        float rpmFraction = engine.getRevs();
        float theta = MyMath.lerp(rpmFraction, theta0, thetaRedline);
        /*
         * a slight lag, because a physical needle cannot pivot instantly
         */
        prevTheta = FastMath.interpolateLinear(0.1f, prevTheta, theta);

        tmpRotation.fromAngles(0f, 0f, prevTheta - FastMath.HALF_PI);
        needleNode.setLocalRotation(tmpRotation);
        /*
         * update the Lemur label, which is mainly for testing
         */
        float redlineRpm = engine.getMaxRevs();
        float rpm = rpmFraction * redlineRpm;
        String labelText = String.format("%.0f rpm", rpm);
        revsLabel.setText(labelText);
    }
    // *************************************************************************
    // private methods

    private Node buildNumNode(float redlineRpm, int stepRpm, float radius) {
        Node numNode = new Node("Tachometer Numbers");

        for (int intRpm = 0;; intRpm += stepRpm) {
            float rpmFraction = intRpm / redlineRpm;
            float theta = MyMath.lerp(rpmFraction, theta0, thetaRedline);
            if (theta < thetaMin) {
                break;
            }
            String text = Integer.toString(intRpm / 1000);
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
     * Build the Node for the fixed parts of the tachometer, including the
     * background and numbers.
     */
    private Node buildFixedNode(AssetManager assetManager) {
        String path = "Textures/Vehicles/Speedometer/tachometer_bg.png";
        Texture backgroundTexture = assetManager.loadTexture(path);
        Image image = backgroundTexture.getImage();
        int height = image.getHeight();
        int width = image.getWidth();

        Geometry backgroundGeom = new Geometry("Tachometer Background",
                new Quad(width, height));
        backgroundGeom.setLocalTranslation(-width / 2f, -height / 2f, -1f);

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundGeom.setMaterial(material);
        material.setTexture("ColorMap", backgroundTexture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        int maxRevs = (int) vehicle.getEngine().getMaxRevs();
        Node numNode = buildNumNode(maxRevs, 1000, width / 2f - 20f);
        numNode.attachChild(backgroundGeom);
        numNode.setLocalTranslation(width / 2f, height / 2f, -1f);

        return numNode;
    }
}
