package com.jayfella.jme.vehicle.examples.skies;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Sky;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowFilter;
import java.util.logging.Logger;
import jme3utilities.sky.LunarPhase;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.Updater;

/**
 * A sample Sky using SkyControl.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class AnimatedNightSky extends Sky {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AnimatedNightSky.class.getName());
    /**
     * path to the LightProbe asset
     */
    final public static String lightProbeAssetPath
            = "/Textures/skies/moon/probe.j3o";
    // *************************************************************************
    // fields

    /**
     * loaded LightProbe
     */
    private LightProbe probe;
    // *************************************************************************
    // new methods exposed

    /**
     * Add this loaded Sky to the specified scene-graph node.
     *
     * @param parent where to attach (not null)
     */
    @Override
    public void attachToScene(Node parent) {
        super.attachToScene(parent);
        parent.addLight(probe);
    }

    /**
     * Remove this loaded Sky from the scene.
     */
    @Override
    public void detachFromScene() {
        Node parent = loadedCgm.getParent();
        parent.removeLight(probe);

        super.detachFromScene();
    }

    /**
     * Load this Sky from assets.
     */
    @Override
    public void load() {
        assert loadedCgm == null : "The model is already loaded.";

        Main application = Main.getApplication();
        AssetManager assetManager = application.getAssetManager();
        probe = (LightProbe) assetManager.loadAsset(lightProbeAssetPath);

        Camera camera = application.getCamera();
        float cloudFlattening = 0.8f;
        boolean bottomDome = true;
        SkyControl skyControl = new SkyControl(assetManager, camera,
                cloudFlattening, StarsOption.Cube, bottomDome);
        skyControl.setCloudiness(0.8f);
        skyControl.setCloudsYOffset(0.4f);
        skyControl.setPhase(LunarPhase.WAXING_GIBBOUS);
        skyControl.setStarMaps("equator16m");

        Updater updater = skyControl.getUpdater();
        DirectionalLight mainLight = getDirectionalLight();
        updater.setMainLight(mainLight);
        DirectionalLightShadowFilter shadowFilter = getShadowFilter();
        updater.addShadowFilter(shadowFilter);

        loadedCgm = new Node();
        loadedCgm.addControl(skyControl);
        skyControl.setEnabled(true);
    }
}
