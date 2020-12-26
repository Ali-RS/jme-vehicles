package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.engine.EnginePowerGraph;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class EnginePowerGraphState extends BaseAppState {

    final private Vehicle vehicle;
    private Geometry line;

    final private Node node = new Node("Engine Graph Node");

    public EnginePowerGraphState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    final private static int width = 300;
    final private static int height = 100;

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        EnginePowerGraph enginePowerGraph = new EnginePowerGraph(app.getAssetManager(), vehicle.getEngine(), width, height);
        node.attachChild(enginePowerGraph);

        line = new Geometry("", new Quad(1, height));
        line.setMaterial(new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
        line.getMaterial().setColor("Color", ColorRGBA.Green);
        node.attachChild(line);

        node.setLocalTranslation(0, app.getCamera().getHeight() - height, 0);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        // float revs = vehicle.getEngine().getRevs() * vehicle.getEngine().getMaxRevs();
        float posX = vehicle.getEngine().getRpmFraction() * width;
        // float posY = getApplication().getCamera().getHeight() - height;
        line.setLocalTranslation(posX, 0, 2);
    }

    private float map(float value, float oldMin, float oldMax, float newMin, float newMax) {
        return (((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin;
    }

    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        Node guiNode = ((SimpleApplication)getApplication()).getGuiNode();
        guiNode.attachChild(node);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }
}
