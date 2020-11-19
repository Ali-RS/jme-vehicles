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

        Geometry fixedGeometry = createFixedGeometry(assetManager);
        node.attachChild(fixedGeometry);

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
        numNode.setLocalTranslation(0f, 0f, -1f);

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

    private Geometry createFixedGeometry(AssetManager assetManager) {
        String path = "Textures/Vehicles/Speedometer/speedo_bg_2.png";
        Texture backgroundTexture = assetManager.loadTexture(path);
        Image image = backgroundTexture.getImage();
        int height = image.getHeight();
        int width = image.getWidth();

        Geometry backgroundGeom = new Geometry("Speedometer Background",
                new Quad(width, height));

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundGeom.setMaterial(material);
        material.setTexture("ColorMap", backgroundTexture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        int maxSpeed = (int) vehicle.getGearBox().getMaxSpeed(speedUnit);
        Node numbers = buildNumNode(maxSpeed, 10, width / 2f - 20f);

        backgroundGeom.setLocalTranslation(
                -width / 2f,
                -height / 2f,
                -1f
        );

        numbers.attachChild(backgroundGeom);

        Texture2D numberTexture = generateImpostor(numbers, width);

        Geometry numbersGeom = new Geometry("Speedometer Numbers",
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
