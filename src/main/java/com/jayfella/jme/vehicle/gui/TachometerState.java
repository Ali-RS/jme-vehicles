package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.Engine;
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
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.simsilica.lemur.Label;
import jme3utilities.MyAsset;
import jme3utilities.math.MyMath;

/**
 * Appstate to manage an analog tachometer in the DriverHud.
 */
class TachometerState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * important needle rotations (measured CCW from +X, in radians)
     */
    final private static float thetaMin = -1f; // "pegged" to the right
    final private static float theta0 = FastMath.PI - thetaMin; // 0 rpms
    final private static float thetaRedline = 0f;
    /**
     * color for Lemur debug labels
     */
    final public static ColorRGBA labelColor
            = new ColorRGBA(66 / 255f, 244 / 255f, 241 / 255f, 1f);
    /**
     * color for dial markings
     */
    final private static ColorRGBA markingColor = ColorRGBA.White.clone();
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
    TachometerState(Vehicle vehicle) {
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
     * Callback invoked after this AppState is attached but before onEnable().
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

        revsLabel = new Label("RPM x1000");
        node.attachChild(revsLabel);
        revsLabel.setColor(markingColor);
        float labelWidth = revsLabel.getPreferredSize().x;
        revsLabel.setLocalTranslation(100f - labelWidth / 2, 30f, 1f);

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
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
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
        float rpmFraction = engine.getRpmFraction();
        float theta = MyMath.lerp(rpmFraction, theta0, thetaRedline);
        /*
         * a slight lag, to prevent the needle from jiggling
         */
        prevTheta = FastMath.interpolateLinear(0.3f, prevTheta, theta);

        tmpRotation.fromAngles(0f, 0f, prevTheta - FastMath.HALF_PI);
        needleNode.setLocalRotation(tmpRotation);
        /*
         * update the Lemur label, which is mainly for testing
         */
        //float redlineRpm = engine.getMaxRevs();
        //float rpm = rpmFraction * redlineRpm;
        //String labelText = String.format("%.0f rpm", rpm);
        //revsLabel.setText(labelText);
    }
    // *************************************************************************
    // private methods

    private Node buildNumNode(float redlineRpm, int stepRpm, float radius) {
        Node result = new Node("Tachometer Numbers");

        AssetManager assetManager = getApplication().getAssetManager();
        Material markingMaterial
                = MyAsset.createUnshadedMaterial(assetManager, markingColor);

        Vector3f innerOffset = new Vector3f();
        Vector3f outerOffset = new Vector3f();

        for (int intRpm = 0;; intRpm += stepRpm) {
            float rpmFraction = intRpm / redlineRpm;
            float theta = MyMath.lerp(rpmFraction, theta0, thetaRedline);
            if (theta < thetaMin) {
                break;
            }
            String text = Integer.toString(intRpm / 1000);
            Label label = new Label(text);
            result.attachChild(label);
            label.setColor(markingColor);

            float cos = FastMath.cos(theta);
            float sin = FastMath.sin(theta);
            Vector3f size = label.getPreferredSize();
            float x = radius * cos - size.x / 2;
            float y = radius * sin + size.y / 2;
            label.setLocalTranslation(x, y, 0f);
            /*
             * Generate a Mesh for the corresponding radial marking.
             */
            float innerRadius = 60f;
            innerOffset.x = innerRadius * cos;
            innerOffset.y = innerRadius * sin;
            float outerRadius = 68f;
            outerOffset.x = outerRadius * cos;
            outerOffset.y = outerRadius * sin;
            Line radialMesh = new Line(innerOffset, outerOffset);

            Geometry radial = new Geometry("Tachometer Radial", radialMesh);
            result.attachChild(radial);
            radial.setMaterial(markingMaterial);
        }

        return result;
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

        int maxRevs = (int) vehicle.getEngine().getRedlineRpm();
        float numbersRadius = 0.38f * width;
        Node result = buildNumNode(maxRevs, 1000, numbersRadius);
        result.attachChild(backgroundGeom);
        result.setLocalTranslation(width / 2f, height / 2f, -1f);

        return result;
    }
}
