package com.jayfella.jme.vehicle;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.texture.Texture;
import java.util.logging.Logger;
import jme3utilities.MyMesh;
import jme3utilities.mesh.Octasphere;

/**
 * A simulated sky with its associated lights and post-processing.
 */
abstract public class Sky implements Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Sky.class.getName());
    // *************************************************************************
    // fields

    /**
     * main directional light
     */
    private static DirectionalLight directionalLight;
    /**
     * shadow filter for the main light
     */
    private static DirectionalLightShadowFilter shadowFilter;
    /**
     * root of the loaded C-G model
     */
    protected Spatial loadedCgm;
    // *************************************************************************
    // new methods exposed

    /**
     * Add this Sky to the specified scene.
     *
     * @param parent where to attach (not null)
     */
    public void attachToScene(Node parent) {
        if (loadedCgm == null) {
            load();
        }
        parent.attachChild(loadedCgm);
    }

    /**
     * Remove this loaded Sky from the scene.
     */
    public void detachFromScene() {
        loadedCgm.removeFromParent();
    }

    /**
     * Access the C-G model.
     *
     * @return the pre-existing Spatial, or null if not yet loaded
     */
    final public Spatial getCgm() {
        return loadedCgm;
    }

    /**
     * Initialize the static fields. Can only be invoked once.
     */
    static void initialize() {
        assert directionalLight == null : directionalLight;
        Main application = Main.getApplication();
        assert application.getViewPort().getProcessors().isEmpty();
        Node rootNode = application.getRootNode();
        assert rootNode.getChildren().isEmpty();
        assert rootNode.getNumControls() == 0;
        assert rootNode.getLocalLightList().size() == 0;

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        /*
         * Add a DirectionalLight.
         */
        directionalLight = new DirectionalLight();
        rootNode.addLight(directionalLight);
        /*
         * Add a FilterPostProcessor.
         */
        AssetManager assetManager = Main.getApplication().getAssetManager();
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        application.getViewPort().addProcessor(fpp);
        int numSamples = application.getContext().getSettings().getSamples();
        if (numSamples > 0) {
            fpp.setNumSamples(numSamples);
        }
        /*
         * Add a DirectionalLightShadowFilter.
         */
        shadowFilter = new DirectionalLightShadowFilter(assetManager, 4_096, 4);
        fpp.addFilter(shadowFilter);
        shadowFilter.setLight(directionalLight);
        shadowFilter.setShadowIntensity(0.3f);
        shadowFilter.setShadowZExtend(256f);
        shadowFilter.setShadowZFadeLength(128f);
    }
    // *************************************************************************
    // new protected methods

    /**
     * Generate a sky geometry from an equirectangular texture asset. This
     * method has a couple advantages over
     * com.jme3.util.SkyFactory.createSky(AssetManager, Texture, EnvMapType):
     * <ul>
     * <li>It adds fewer triangles to the scene: 32 instead of 160.</li>
     * <li>It uses a custom J3MD to avoid JME issue #1414.</li>
     * </ul>
     *
     * @param textureAssetPath the path to the texture asset (not null, not
     * empty)
     * @return a new orphaned Geometry
     */
    protected static Spatial createSky(String textureAssetPath) {
        /*
         * Load and configure the Texture.
         */
        AssetManager assetManager = Main.getApplication().getAssetManager();
        boolean flipY = true;
        TextureKey textureKey = new TextureKey(textureAssetPath, flipY);
        Texture texture = assetManager.loadTexture(textureKey);
        texture.setAnisotropicFilter(1);
        /*
         * Construct the Material.
         */
        String matDefAssetPath = "/MatDefs/SkyEquirec.j3md";
        Material material = new Material(assetManager, matDefAssetPath);
        material.setTexture("Texture", texture);
        material.setVector3("NormalScale", new Vector3f(1f, 1f, 1f));
        /*
         * Construct the BoundingVolume, an infinite sphere.
         */
        float boundRadius = Float.POSITIVE_INFINITY;
        BoundingVolume boundingSphere
                = new BoundingSphere(boundRadius, Vector3f.ZERO);
        /*
         * Construct the Mesh, an Octasphere with 32 trianges.
         */
        int numRefineSteps = 1;
        float meshRadius = 10f;
        Octasphere sphereMesh = new Octasphere(numRefineSteps, meshRadius);
        MyMesh.reverseNormals(sphereMesh);
        MyMesh.reverseWinding(sphereMesh);
        /*
         * Construct the Geometry.
         */
        Geometry result = new Geometry("Sky Sphere", sphereMesh);
        result.setCullHint(Spatial.CullHint.Never);
        result.setMaterial(material);
        result.setModelBound(boundingSphere);
        result.setQueueBucket(RenderQueue.Bucket.Sky);
        result.setShadowMode(RenderQueue.ShadowMode.Off);

        return result;
    }

    /**
     * Access the main directional light.
     *
     * @return the pre-existing instance (not null)
     */
    final protected DirectionalLight getDirectionalLight() {
        assert directionalLight != null;
        return directionalLight;
    }

    /**
     * Access the shadow filter.
     *
     * @return the pre-existing instance (not null)
     */
    final protected DirectionalLightShadowFilter getShadowFilter() {
        assert shadowFilter != null;
        return shadowFilter;
    }
}
