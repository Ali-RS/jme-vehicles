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
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
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
    private final Node needleNode = new Node("Tachometer Needle");
    private final Node node;
    /**
     * reusable temporary Quaternion
     */
    private final Quaternion tmpRotation = new Quaternion();
    /**
     * corresponding Vehicle
     */
    private final Vehicle vehicle;
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

        Geometry fixedGeometry = createFixedGeometry(assetManager);
        node.attachChild(fixedGeometry);

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
                -(width * 0.5f),
                -(width * 0.5f) - 7f,
                0f);

        revsLabel = new Label("RPM");
        node.attachChild(revsLabel);
        revsLabel.setColor(labelColor);
        revsLabel.setLocalTranslation(70f, 30f, 1f);

        node.setLocalTranslation(
                app.getCamera().getWidth() - 400f - 40f,
                20f, 0f
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
        numNode.setLocalTranslation(0f, 0f, -1f);

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

    private Geometry createFixedGeometry(AssetManager assetManager) {
        String path = "Textures/Vehicles/Speedometer/tachometer_bg.png";
        Texture backgroundTexture = assetManager.loadTexture(path);
        Image image = backgroundTexture.getImage();
        int height = image.getHeight();
        int width = image.getWidth();

        Geometry backgroundGeom = new Geometry("Tachometer Background",
                new Quad(width, height));

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundGeom.setMaterial(material);
        material.setTexture("ColorMap", backgroundTexture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        int maxRevs = (int) vehicle.getEngine().getMaxRevs();
        Node numbers = buildNumNode(maxRevs, 1000, width / 2f - 20f);

        backgroundGeom.setLocalTranslation(
                -backgroundTexture.getImage().getWidth() / 2f,
                -backgroundTexture.getImage().getHeight() / 2f,
                -1f
        );

        numbers.attachChild(backgroundGeom);

        Texture2D numberTexture = generateImpostor(numbers, width);

        Geometry numbersGeom = new Geometry("Tachometer Numbers",
                new Quad(width, height));

        material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        numbersGeom.setMaterial(material);
        material.setTexture("ColorMap", numberTexture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        return numbersGeom;
    }

    private Texture2D generateImpostor(Node scene, int size) {
        Camera newCam = new Camera(size, size);
        newCam.setFrustumPerspective(45f, 1f, 1f, 2f);
        newCam.setParallelProjection(true);
        setProjectionHeight(newCam, size + 40f);
        newCam.lookAtDirection(new Vector3f(0f, 0f, -1f), Vector3f.UNIT_Y);

        RenderManager renderManager = getApplication().getRenderManager();
        ViewPort vp = renderManager.createPreView("Offscreen View", newCam);
        vp.setClearFlags(true, true, true);
        vp.setBackgroundColor(ColorRGBA.BlackNoAlpha);

        FrameBuffer offBuffer = new FrameBuffer(size, size, 1);

        Texture2D offTex = new Texture2D(size, size, Image.Format.ABGR8);
        offTex.setMinFilter(Texture.MinFilter.Trilinear);
        offTex.setMagFilter(Texture.MagFilter.Bilinear);

        offBuffer.setDepthBuffer(Image.Format.Depth);
        offBuffer.setColorTexture(offTex);

        vp.setOutputFrameBuffer(offBuffer);

        //scene.updateLogicalState(0);
        scene.updateGeometricState();

        vp.attachScene(scene);

        renderManager.removeMainView(vp);

        return offTex;
    }

    private void setProjectionHeight(Camera camera, float factor) {
        float bottom = camera.getFrustumBottom();
        camera.setFrustumBottom(bottom * factor);
        float left = camera.getFrustumLeft();
        camera.setFrustumLeft(left * factor);
        float right = camera.getFrustumRight();
        camera.setFrustumRight(right * factor);
        float top = camera.getFrustumTop();
        camera.setFrustumTop(top * factor);
    }
}
