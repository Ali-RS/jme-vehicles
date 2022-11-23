package com.jayfella.jme.vehicle.examples.skies;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * An example Sky, built around Sergej Majboroda's "Quarry 03" HDRI.
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
    // new methods exposed

    /**
     * Add this Sky to the specified world.
     *
     * @param world where to add (not null)
     */
    @Override
    public void addToWorld(VehicleWorld world) {
        super.addToWorld(world);

        // Configure the AmbientLight that was added by Sky.initialize().
        ColorRGBA ambientColor = new ColorRGBA(1f, 1f, 1f, 1f);
        getAmbientLight().setColor(ambientColor);

        // Configure the DirectionalLight that was added by Sky.initialize().
        ColorRGBA directColor = new ColorRGBA(1f, 1f, 1f, 1f);
        Vector3f direction = new Vector3f(1f, -0.45f, 0.5f).normalizeLocal();
        DirectionalLight directionalLight = getDirectionalLight();
        directionalLight.setColor(directColor);
        directionalLight.setDirection(direction);

        // Configure the shadow renderer that was added by Sky.initialize().
        getShadowRenderer().setShadowIntensity(0.3f);
    }

    /**
     * Load this Sky from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        super.load(assetManager);

        LightProbe probe
                = (LightProbe) assetManager.loadAsset(lightProbeAssetPath);
        probe.setPosition(Vector3f.ZERO);
        probe.getArea().setRadius(9_999f);

        Spatial spatial = createSky(assetManager, imageAssetPath);
        build(spatial, probe);
    }
}
