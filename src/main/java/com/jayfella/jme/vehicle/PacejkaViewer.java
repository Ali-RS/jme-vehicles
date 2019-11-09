package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.examples.tyres.Tyre_01;
import com.jayfella.jme.vehicle.tire.PajeckaTireModel;
import com.jayfella.jme.vehicle.tire.TireEditor;
import com.jayfella.jme.vehicle.tire.TireGraph;
import com.jayfella.jme.vehicle.tire.TyreSettings;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;

public class PacejkaViewer extends SimpleApplication {


    private PajeckaTireModel tireModel;

    public static void main(String... args) {
        PacejkaViewer pacejkaViewer = new PacejkaViewer();

        AppSettings appSettings = new AppSettings(true);
        appSettings.setResolution(1280, 720);
        appSettings.setFrameRate(120);
        appSettings.setTitle("Pacejka Viewer");

        pacejkaViewer.setSettings(appSettings);
        pacejkaViewer.setShowSettings(false);
        pacejkaViewer.setPauseOnLostFocus(false);

        pacejkaViewer.start();

    }

    @Override
    public void simpleInitApp() {

        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        tireModel = new Tyre_01();

        TireGraph tireGraph = new TireGraph(assetManager, tireModel, cam.getWidth(), cam.getHeight());
        tireGraph.setBackgroundColor(ColorRGBA.DarkGray);
        tireGraph.setLineColor(ColorRGBA.LightGray);

        TyreSettings.ChangeListener changeListener = new TyreSettings.ChangeListener() {
            @Override
            public void valueChanged() {
                tireGraph.drawGraph();
            }
        };

        tireModel.setChangeListener(changeListener);
        tireModel.getLongitudinal().setChangeListener(changeListener);
        tireModel.getLateral().setChangeListener(changeListener);
        tireModel.getAlignMoment().setChangeListener(changeListener);


        guiNode.attachChild(tireGraph);

        TireEditor tireEditor = new TireEditor(tireModel);
        tireEditor.setLocalTranslation(cam.getWidth() - tireEditor.getPreferredSize().x, tireEditor.getPreferredSize().y, 1);
        guiNode.attachChild(tireEditor);
    }




}
