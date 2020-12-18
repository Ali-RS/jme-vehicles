package com.jayfella.jme.vehicle.gui;

import com.jayfella.jme.vehicle.Main;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.Label;
import java.util.concurrent.CountDownLatch;

/**
 * A simple loading state to entertain users. It displays a rotating texture
 * until its CountDownLatch reaches zero.
 */
public class LoadingState extends BaseAppState {
    // *************************************************************************
    // fields

    /**
     * keep track of how many assets loads are in progress
     */
    final private CountDownLatch latch;

    private float speedMult = 3.0f;
    private Geometry backgroundGeom;
    private Label label;
    private Node node = new Node("Loading Node");
    private Node spinnerNode = new Node("Spinner Node");
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AppState for the specified latch.
     *
     * @param latch the latch to monitor
     */
    public LoadingState(CountDownLatch latch) {
        this.latch = latch;
    }

    public boolean isShowBackground() {
        return backgroundGeom.getParent() != null;
    }

    public void setBackgroundColor(ColorRGBA color) {
        backgroundGeom.getMaterial().setColor("Color", color);
    }

    public void setShowBackground(boolean value) {
        if (value) {
            node.attachChild(backgroundGeom);
        } else {
            backgroundGeom.removeFromParent();
        }
    }

    public void setText(String text) {
        label.setText(text);

        Camera camera = getApplication().getCamera();
        float height = camera.getHeight();
        float width = camera.getWidth();
        Vector3f labelSize = label.getPreferredSize();
        float x = width / 2 - labelSize.x / 2;
        float y = height / 2 - labelSize.y / 2 + 70f;
        label.setLocalTranslation(x, y, 1f);
    }

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
        spinnerNode = createSpinnerNode(app.getAssetManager());
        backgroundGeom = createBackgroundGeom(app.getAssetManager(), app.getCamera());
        label = createLabel();

        node.attachChild(backgroundGeom);
        node.attachChild(spinnerNode);
        node.attachChild(label);
        node.setQueueBucket(RenderQueue.Bucket.Gui);

        setText("Loading...");
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
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
        ((SimpleApplication) getApplication()).getGuiNode().attachChild(node);
    }

    @Override
    public void update(float tpf) {
        spinnerNode.rotate(0, 0, -tpf * speedMult);

        long latchCount = latch.getCount();
        if (latchCount == 0L) {
            /*
             * All asynchronous asset loads have completed.
             */
            Main.getApplication().attachAllToScene();
            /*
             * Disable this AppState and display the main menu.
             */
            MainMenuState mainMenuState = new MainMenuState();
            getStateManager().attach(mainMenuState);
            setEnabled(false);
        }
    }

    private Geometry createBackgroundGeom(AssetManager assetManager, Camera cam) {
        Geometry result = new Geometry("Background", new Quad(cam.getWidth(), cam.getHeight()));
        result.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        result.getMaterial().setColor("Color", new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        result.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        return result;
    }

    private Label createLabel() {
        label = new Label("");
        label.setFontSize(18);
        return label;
    }

    private Node createSpinnerNode(AssetManager assetManager) {
        Texture texture = assetManager.loadTexture("loading.png");

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", texture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry spinnerGeom = new Geometry("Loading Circle",
                new Quad(texture.getImage().getWidth(), texture.getImage().getHeight()));
        spinnerGeom.setMaterial(material);

        spinnerGeom.setLocalTranslation(
                -(texture.getImage().getWidth() * 0.5f),
                -(texture.getImage().getHeight() * 0.5f),
                1);

        spinnerNode.attachChild(spinnerGeom);
        spinnerNode.setLocalScale(0.25f);
        /*
         * If we put this in the onEnable() method,
         * we could set its location every time.
         * It's here in-case the display size changes.
         */
        spinnerNode.setLocalTranslation(
                (getApplication().getCamera().getWidth() * 0.5f),
                (getApplication().getCamera().getHeight() * 0.5f),
                1
        );

        return spinnerNode;
    }
}
