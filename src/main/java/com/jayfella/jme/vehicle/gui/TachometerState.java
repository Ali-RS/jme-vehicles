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
import com.jme3.renderer.Camera;
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

/**
 * Appstate to manage an analog tachometer in the DriverHud.
 */
public class TachometerState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    private final String revsFormat = "%.0f";
    // *************************************************************************
    // fields

    final private float[] eulerAngles = new float[3];
    private Label revsLabel;
    private Node guiNode;
    private final Node needleNode = new Node("Needle");
    private final Node node;
    private final Quaternion quat = new Quaternion();
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

        this.node = new Node("Tachometer: " + vehicle.getName());
        this.node.setQueueBucket(RenderQueue.Bucket.Gui);
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
        this.guiNode = ((SimpleApplication) app).getGuiNode();

        Geometry fixedGeometry = createFixedGeometry(app.getAssetManager());
        node.attachChild(fixedGeometry);

        Texture needleTexture = app.getAssetManager().loadTexture("Textures/Vehicles/Speedometer/speedo_needle_2.png");
        Geometry needleGeometry = new Geometry("Tachometer Needle",
                new Quad(needleTexture.getImage().getWidth(), needleTexture.getImage().getHeight()));

        needleGeometry.setMaterial(new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
        needleGeometry.getMaterial().setTexture("ColorMap", needleTexture);
        needleGeometry.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        needleGeometry.setLocalTranslation(
                -(needleTexture.getImage().getWidth() * 0.5f),
                -(needleTexture.getImage().getWidth() * 0.5f) - 7,
                0);

        needleNode.setLocalTranslation(100, 100, 1);
        needleNode.attachChild(needleGeometry);

        node.attachChild(needleNode);

        revsLabel = new Label("8888");
        revsLabel.setColor(new ColorRGBA(66 / 255f, 244 / 255f, 241 / 255f, 1.0f));

        revsLabel.setLocalTranslation(
                100 - (revsLabel.getPreferredSize().x * 0.5f),
                revsLabel.getPreferredSize().y + 15,
                1
        );
        node.attachChild(revsLabel);

        node.setLocalTranslation(
                app.getCamera().getWidth() - 400 - 40,
                20, 0
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
        float startStopAngle = 155;

        float rpmFraction = vehicle.getEngine().getRevs();

        float rot = startStopAngle - ((startStopAngle * 2) * rpmFraction);
        rot = FastMath.clamp(rot, -startStopAngle, startStopAngle);
        rot = rot * FastMath.DEG_TO_RAD;

        eulerAngles[2] = FastMath.interpolateLinear(tpf * 5, eulerAngles[2], rot);
        quat.fromAngles(eulerAngles);

        needleNode.setLocalRotation(quat);
        revsLabel.setText(String.format(revsFormat, rpmFraction * vehicle.getEngine().getMaxRevs()));
    }
    // *************************************************************************
    // private methods

    private Node buildRadialNumbers(int max, int step, float radius, float border) {
        int count = (max / step) + 1;

        Node node = new Node("Numbers Node");
        node.setLocalTranslation(radius, radius, 1);

        float reducedRad = radius - border;
        int num = 0;
        int startAngle = 245;
        float angleStep = ((155 * 2f) / count) * FastMath.DEG_TO_RAD;
        float theta = startAngle * FastMath.DEG_TO_RAD;

        for (int i = 0; i <= count; i++) {
            float x = reducedRad * FastMath.cos(theta);
            float y = reducedRad * FastMath.sin(theta);

            Label label = new Label("" + num / 1000);
            label.setColor(ColorRGBA.White);

            label.setLocalTranslation(
                    x - (label.getPreferredSize().x * .5f),
                    y + (label.getPreferredSize().y * .5f),
                    0);

            node.attachChild(label);

            num += step;
            theta -= angleStep;
        }

        node.setLocalTranslation(0, 0, -1);

        return node;
    }

    private Geometry createFixedGeometry(AssetManager assetManager) {
        Texture backgroundTexture = assetManager.loadTexture("Textures/Vehicles/Speedometer/speedo_bg_2.png");

        Geometry backgroundGeom = new Geometry("Tachometer Background",
                new Quad(backgroundTexture.getImage().getWidth(), backgroundTexture.getImage().getHeight()));

        backgroundGeom.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        backgroundGeom.getMaterial().setTexture("ColorMap", backgroundTexture);
        backgroundGeom.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Node numbers = buildRadialNumbers((int) vehicle.getEngine().getMaxRevs(), 1000, backgroundTexture.getImage().getWidth() / 2f, 20);

        backgroundGeom.setLocalTranslation(
                -backgroundTexture.getImage().getWidth() / 2f,
                -backgroundTexture.getImage().getHeight() / 2f,
                -1
        );

        numbers.attachChild(backgroundGeom);

        Texture2D numberTexture = generateImpostor(numbers, backgroundTexture.getImage().getWidth());

        Geometry numbersGeom = new Geometry("Tachometer Numbers",
                new Quad(backgroundTexture.getImage().getWidth(), backgroundTexture.getImage().getHeight()));

        numbersGeom.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        numbersGeom.getMaterial().setTexture("ColorMap", numberTexture);
        numbersGeom.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        return numbersGeom;
    }

    private Texture2D generateImpostor(Node scene, int size) {
        Camera newCam = new Camera(size, size);
        newCam.setFrustumPerspective(45f, 1f, 1f, 2f);
        newCam.setParallelProjection(true);
        setProjectionHeight(newCam, size + 40);
        newCam.lookAtDirection(new Vector3f(0, 0, -1), Vector3f.UNIT_Y);

        ViewPort vp = getApplication().getRenderManager().createPreView("Offscreen View", newCam);
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

        getApplication().getRenderManager().removeMainView(vp);

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
