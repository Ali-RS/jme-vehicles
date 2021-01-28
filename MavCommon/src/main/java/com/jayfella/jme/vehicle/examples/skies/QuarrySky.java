package com.jayfella.jme.vehicle.examples.skies;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.logging.Logger;

/**
 * A sample Sky, built around Sergej Majboroda's "Quarry 03" HDRI.
 */
public class QuarrySky extends Sky {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(QuarrySky.class.getName());
    /**
     * path to the image asset
     */
    final public static String imageAssetPath
            = "/Textures/Sky/quarry_03/equirec_4k.jpg";
    /**
     * path to the LightProbe asset
     */
    final public static String lightProbeAssetPath
            = "/Textures/Sky/quarry_03/probe.j3o";
    // *************************************************************************
    // fields

    /**
     * loaded LightProbe
     */
    private LightProbe probe;
    // *************************************************************************
    // new methods exposed

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
        ColorRGBA ambientColor = new ColorRGBA(1f, 1f, 1f, 1f);
        getAmbientLight().setColor(ambientColor);
        /*
         * Configure the DirectionalLight that was added by Sky.initialize().
         */
        ColorRGBA directColor = new ColorRGBA(1f, 1f, 1f, 1f);
        Vector3f direction = new Vector3f(1f, -0.45f, 0.5f).normalizeLocal();
        DirectionalLight directionalLight = getDirectionalLight();
        directionalLight.setColor(directColor);
        directionalLight.setDirection(direction);
        /*
         * Configure and add the LightProbe.
         */
        probe.setPosition(Vector3f.ZERO);
        probe.getArea().setRadius(9_999f);
        Node sceneNode = world.getSceneNode();
        sceneNode.addLight(probe);
        /*
         * Configure the shadow renderer that was added by Sky.initialize().
         */
        getShadowRenderer().setShadowIntensity(0.3f);
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
     *
     * @param assetManager the AssetManager for loading (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        assert loadedCgm == null : "The model is already loaded.";

        probe = (LightProbe) assetManager.loadAsset(lightProbeAssetPath);
        loadedCgm = createSky(assetManager, imageAssetPath);
    }
}
