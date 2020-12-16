package com.jayfella.jme.vehicle.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
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
 * until a CountDownLatch reaches zero.
 */
public class LoadingState extends BaseAppState {
    // *************************************************************************
    // fields

    final private CountDownLatch latch;
    private Node node = new Node("Loading Node");
    private Geometry backgroundGeom;
    private Node spinnerNode = new Node("Spinner Node");
    private Label label;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AppState for the specified latch.
     *
     * @param latch the latch to monitor.
     */
    public LoadingState(CountDownLatch latch) {
        this.latch = latch;
    }

    private Node createSpinnerNode(AssetManager assetManager) {

        Texture texture = assetManager.loadTexture("loading.png");

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", texture);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Geometry spinnerGeom = new Geometry("Loading Circle", new Quad(texture.getImage().getWidth(), texture.getImage().getHeight()));
        spinnerGeom.setMaterial(material);

        spinnerGeom.setLocalTranslation(
                -(texture.getImage().getWidth() * 0.5f),
                -(texture.getImage().getHeight() * 0.5f),
                1);

        spinnerNode.attachChild(spinnerGeom);
        spinnerNode.setLocalScale(0.25f);

        // if we put this in the onEnable method we could set its location every time it's enabled just in-case the resolution changes.
        spinnerNode.setLocalTranslation(
                (getApplication().getCamera().getWidth() * 0.5f),
                (getApplication().getCamera().getHeight() * 0.5f),
                1
        );

        return spinnerNode;
    }

    private Geometry createBackgroundGeom(AssetManager assetManager, Camera cam) {

        Geometry backgroundGeom = new Geometry("Background", new Quad(cam.getWidth(), cam.getHeight()));
        backgroundGeom.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        backgroundGeom.getMaterial().setColor("Color", new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        backgroundGeom.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        return backgroundGeom;
    }

    public void setShowBackground(boolean value) {
        if (value) {
            node.attachChild(backgroundGeom);
        }
        else {
            backgroundGeom.removeFromParent();
        }
    }

    public boolean isShowBackground() {
        return backgroundGeom.getParent() != null;
    }

    public void setBackgroundColor(ColorRGBA color) {
        backgroundGeom.getMaterial().setColor("Color", color);
    }

    private Label createLabel() {
        label = new Label("");
        label.setFontSize(18);
        return label;
    }

    public void setText(String text) {
        label.setText(text);
        label.setLocalTranslation(
                getApplication().getCamera().getWidth() - label.getPreferredSize().x - 20,
                label.getPreferredSize().y + 20,
                1
        );
    }

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

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        ((SimpleApplication)getApplication()).getGuiNode().attachChild(node);
    }

    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    private float speedMult = 3.0f;

    @Override
    public void update(float tpf) {
        spinnerNode.rotate(0, 0, -tpf * speedMult);

        long latchCount = latch.getCount();
        if (latchCount == 0L) {
            /*
             * All asynchronous asset loads have completed.
             * Disable this AppState and display the main menu.
             */
            MainMenuState mainMenuState = new MainMenuState();
            getStateManager().attach(mainMenuState);
            setEnabled(false);
        }
    }

}
