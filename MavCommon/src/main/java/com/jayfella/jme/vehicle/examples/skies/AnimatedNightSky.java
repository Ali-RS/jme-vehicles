package com.jayfella.jme.vehicle.examples.skies;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.app.LegacyApplication;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import java.util.logging.Logger;
import jme3utilities.sky.LunarPhase;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.Updater;

/**
 * An example of a nighttime sky using SkyControl.
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
     * manage the sky simulation
     */
    private SkyControl skyControl;
    // *************************************************************************
    // new methods exposed

    /**
     * Create and configure a SkyControl for this example.
     *
     * @param application (not null)
     * @return a new instance
     */
    public static SkyControl createSkyControl(LegacyApplication application) {
        AssetManager assetManager = application.getAssetManager();
        Camera camera = application.getCamera();
        float cloudFlattening = 0.8f;
        boolean bottomDome = true;
        SkyControl result = new SkyControl(assetManager, camera,
                cloudFlattening, StarsOption.Cube, bottomDome);
        result.setCloudiness(0.8f);
        result.setCloudsYOffset(0.4f);
        result.setPhase(LunarPhase.WAXING_GIBBOUS);
        result.setStarMaps("equator16m");

        return result;
    }
    // *************************************************************************
    // Sky methods

    /**
     * Add this Sky to the scene of the specified world.
     *
     * @param world where to add (not null)
     */
    @Override
    public void attachToScene(VehicleWorld world) {
        super.attachToScene(world);
        /*
         * Configure the AmbientLight that was added by Sky.initialize().
         */
        ColorRGBA ambientColor = new ColorRGBA(0.8f, 0.8f, 0.8f, 1f);
        getAmbientLight().setColor(ambientColor);
        /*
         * Configure the DirectionalLight that was added by Sky.initialize().
         */
        Updater updater = skyControl.getUpdater();
        DirectionalLight mainLight = getDirectionalLight();
        updater.setMainLight(mainLight);
        /*
         * Configure the shadow filter that was added by Sky.initialize().
         */
        DirectionalLightShadowRenderer shadowRenderer = getShadowRenderer();
        updater.addShadowRenderer(shadowRenderer);
    }

    /**
     * Load this Sky from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        super.load(assetManager);

        LegacyApplication application = Main.getApplication();
        skyControl = createSkyControl(application);

        Node node = new Node();
        node.addControl(skyControl);
        skyControl.setEnabled(true);

        LightProbe probe
                = (LightProbe) assetManager.loadAsset(lightProbeAssetPath);
        build(node, probe);
    }
}
