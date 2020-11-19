package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.Car;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.tire.TireGraph;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class TireDataState extends BaseAppState {

    private final Car vehicle;

    int graphWidth = 200;
    int graphHeight = 100;
    private TireGraph[] tireGraphs;

    private Node guiNode;
    private Node node;

    // 3 needles per wheel.
    private Geometry[][] needles;

    public TireDataState(Car vehicle) {

        this.vehicle = vehicle;
        this.tireGraphs = new TireGraph[vehicle.getNumWheels()];
        this.needles = new Geometry[vehicle.getNumWheels()][3];

        this.node = new Node("Tire Data Node");
    }

    private Geometry createNeedle(AssetManager assetManager, ColorRGBA color) {
        Geometry line = new Geometry("Needle", new Quad(1, graphHeight));
        line.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
        line.getMaterial().setColor("Color", color);

        return line;
    }

    private void drawGraph(int i) {
        tireGraphs[i].drawGraph();


    }

    @Override
    protected void initialize(Application app) {

        guiNode = ((SimpleApplication)app).getGuiNode();

        float space = 10;

        int x = 0;
        int y = 0;

        for (int i = 0; i < tireGraphs.length; i++) {

            Node graphNode = new Node("");
            graphNode.setLocalTranslation(x, y, 0);

            x+= graphWidth + space;

            if ((i + 1) % 2 == 0) {
                y -= graphHeight + space;
                x = 0;
            }

            TireGraph tireGraph = new TireGraph(app.getAssetManager(), vehicle.getWheel(i).getTireModel(), graphWidth, graphHeight);
            graphNode.attachChild(tireGraph);

            tireGraph.setBackgroundColor(ColorRGBA.DarkGray);
            tireGraph.setLateralColor(ColorRGBA.Red);
            tireGraph.setLongitudinalColor(ColorRGBA.Yellow);
            tireGraph.setMomentColor(ColorRGBA.Green);

            Geometry lateralNeedle = createNeedle(app.getAssetManager(), tireGraph.getLateralColor());
            Geometry longitudeNeedle = createNeedle(app.getAssetManager(), tireGraph.getLongitudinalColor());
            Geometry momentNeedle = createNeedle(app.getAssetManager(), tireGraph.getMomentColor());

            graphNode.attachChild(lateralNeedle);
            graphNode.attachChild(longitudeNeedle);
            graphNode.attachChild(momentNeedle);

            node.attachChild(graphNode);

            needles[i][0] = lateralNeedle;
            needles[i][1] = longitudeNeedle;
            needles[i][2] = momentNeedle;

            tireGraphs[i] = tireGraph;
        }

        node.setLocalTranslation(space, (graphHeight) + (space * 2), -1);

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {
        guiNode.attachChild(node);
    }

    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    @Override
    public void update(float tpf) {

        for (int i = 0; i < vehicle.getNumWheels(); i++) {

            tireGraphs[i].drawGraph();

            Wheel wheel = vehicle.getWheel(i);

            // float lat = wheel.getTireModel().getLateralValue();
            // lat /= wheel.getTireModel().getMaxLoad();
            // lat *= graphWidth;
            float lat = wheel.calculateLateralSlipAngle();
            lat /= wheel.getMaxSteerAngle();
            lat *= graphWidth;

            float lng = wheel.calculateLongitudinalSlipAngle();
            lng /= FastMath.QUARTER_PI;
            //System.out.println(lng);
            //lng /= 1000;
            lng *= graphWidth;

            float mnt = wheel.getTireModel().getMomentValue();

            needles[i][0].setLocalTranslation(lat, 0, 0);
            needles[i][1].setLocalTranslation(lng, 0, 0);
            needles[i][2].setLocalTranslation(mnt, 0, 0);
        }

    }

}
