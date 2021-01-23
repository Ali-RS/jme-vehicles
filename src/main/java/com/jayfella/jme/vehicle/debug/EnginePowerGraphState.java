package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class EnginePowerGraphState extends BaseAppState {
    // *************************************************************************
    // fields

    private Geometry line;

    final private static int height = 100;
    final private static int width = 300;

    final private Node node = new Node("Engine Graph Node");
    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public EnginePowerGraphState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        EnginePowerGraph enginePowerGraph = new EnginePowerGraph(
                application.getAssetManager(), vehicle.getEngine(), width, height);
        node.attachChild(enginePowerGraph);

        line = new Geometry("", new Quad(1, height));
        line.setMaterial(new Material(application.getAssetManager(), Materials.UNSHADED));
        line.getMaterial().setColor("Color", ColorRGBA.Green);
        node.attachChild(line);

        node.setLocalTranslation(0, application.getCamera().getHeight() - height, 0);
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
        Node guiNode = ((SimpleApplication) getApplication()).getGuiNode();
        guiNode.attachChild(node);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        // float revs = vehicle.getEngine().getRevs() * vehicle.getEngine().getMaxRevs();
        float posX = vehicle.getEngine().rpmFraction() * width;
        // float posY = getApplication().getCamera().getHeight() - height;
        line.setLocalTranslation(posX, 0, 2);
    }
}
