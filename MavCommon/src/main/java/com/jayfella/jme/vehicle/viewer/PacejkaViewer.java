package com.jayfella.jme.vehicle.viewer;

import com.jayfella.jme.vehicle.examples.tires.Tire_01;
import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jayfella.jme.vehicle.tire.TireGraph;
import com.jayfella.jme.vehicle.tire.TireSettings;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import java.util.logging.Logger;

public class PacejkaViewer extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PacejkaViewer.class.getName());
    // *************************************************************************
    // fields

    private PacejkaTireModel tireModel;

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

        tireModel = new Tire_01();

        TireGraph tireGraph = new TireGraph(assetManager, tireModel, cam.getWidth(), cam.getHeight());
        tireGraph.setBackgroundColor(ColorRGBA.DarkGray);
        tireGraph.setLineColor(ColorRGBA.LightGray);

        TireSettings.ChangeListener changeListener = new TireSettings.ChangeListener() {
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
