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
import jme3utilities.MyAsset;

/**
 * A sample Sky, built around the "Purple Nebula Complex" cubemap.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class PurpleNebulaSky extends Sky {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PurpleNebulaSky.class.getName());
    /**
     * name of the cubemap
     */
    final public static String cubemapName = "purple-nebula-complex";
    /**
     * path to the LightProbe asset
     */
    final public static String lightProbeAssetPath
            = "/Textures/skies/star-maps/purple-nebula-complex/probe.j3o";
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
        ColorRGBA ambientColor = new ColorRGBA(30f, 50f, 25f, 1f);
        getAmbientLight().setColor(ambientColor);
        /*
         * Configure the DirectionalLight that was added by Sky.initialize().
         */
        DirectionalLight directionalLight = getDirectionalLight();
        directionalLight.setColor(ColorRGBA.Black);
        directionalLight.setDirection(Vector3f.UNIT_Y);
        /*
         * Add the LightProbe.
         */
        Node sceneNode = world.getSceneNode();
        sceneNode.addLight(probe);
        /*
         * Configure the shadow renderer that was added by Sky.initialize().
         */
        getShadowRenderer().setShadowIntensity(0f);
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
        loadedCgm
                = MyAsset.createStarMapSphere(assetManager, cubemapName, 100f);
    }
}
