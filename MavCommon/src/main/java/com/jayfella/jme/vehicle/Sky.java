package com.jayfella.jme.vehicle;

import com.github.stephengold.jmepower.Loadable;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
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
     * modulate the color/intensity of light probes
     */
    private static AmbientLight ambientLight;
    /**
     * main directional light
     */
    private static DirectionalLight directionalLight;
    /**
     * shadow renderer for the main light
     */
    private static DirectionalLightShadowRenderer shadowRenderer;
    /**
     * root of the loaded C-G model
     */
    protected Spatial loadedCgm;
    /**
     * VehicleWorld that contains this Sky, or null if none
     */
    private VehicleWorld world;
    // *************************************************************************
    // new methods exposed

    /**
     * Add this Sky to the scene of the specified world.
     *
     * @param world where to add (not null)
     */
    public void attachToScene(VehicleWorld world) {
        this.world = world;

        if (loadedCgm == null) {
            AssetManager assetManager = world.getAssetManager();
            load(assetManager);
        }

        Node parent = world.getSceneNode();
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
     *
     * @param application (not null)
     */
    public static void initialize(SimpleApplication application) {
        assert ambientLight == null : ambientLight;
        assert directionalLight == null : directionalLight;
        ViewPort viewPort = application.getViewPort();
        assert viewPort.getProcessors().isEmpty();
        Node rootNode = application.getRootNode();
        assert rootNode.getChildren().isEmpty();
        assert rootNode.getLocalLightList().size() == 0;
        assert rootNode.getNumControls() == 0;

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        /*
         * Add an AmbientLight.
         */
        ambientLight = new AmbientLight();
        rootNode.addLight(ambientLight);
        /*
         * Add a DirectionalLight.
         */
        directionalLight = new DirectionalLight();
        rootNode.addLight(directionalLight);
        /*
         * Add shadows.
         */
        AssetManager assetManager = application.getAssetManager();
        shadowRenderer
                = new DirectionalLightShadowRenderer(assetManager, 4_096, 4);
        viewPort.addProcessor(shadowRenderer);
        shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        shadowRenderer.setLight(directionalLight);
        shadowRenderer.setShadowIntensity(0.3f);
        shadowRenderer.setShadowZExtend(256f);
        shadowRenderer.setShadowZFadeLength(128f);
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
     * @param assetManager for loading assets (not null)
     * @param textureAssetPath the path to the texture asset (not null, not
     * empty)
     * @return a new orphan Geometry
     */
    protected static Spatial createSky(AssetManager assetManager,
            String textureAssetPath) {
        /*
         * Load and configure the Texture.
         */
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
         * Construct the BoundingVolume, a very large sphere.
         */
        float boundRadius = 9e6f; // finite, to work around issue #1
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
     * Access the ambient light, used to modulate the color/intensity of light
     * probes.
     *
     * @return the pre-existing instance (not null)
     */
    final protected AmbientLight getAmbientLight() {
        assert ambientLight != null;
        return ambientLight;
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
     * Access the shadow renderer.
     *
     * @return the pre-existing instance (not null)
     */
    final protected DirectionalLightShadowRenderer getShadowRenderer() {
        assert shadowRenderer != null;
        return shadowRenderer;
    }
}
