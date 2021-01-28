package com.jayfella.jme.vehicle.examples.worlds;

import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import java.util.logging.Logger;

/**
 * A sample World, built around the "mountains512.png" heightmap and terrain
 * textures from jme3-testdata.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Mountains extends World {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Mountains.class.getName());
    // *************************************************************************
    // World methods

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    @Override
    public float dropYRotation() {
        return FastMath.PI;
    }

    /**
     * Load this World from assets.
     */
    @Override
    public void load() {
        assert getCgm() == null : "The model is already loaded.";
        /*
         * Load the heightmap image.
         */
        String assetFolder = "/Textures/Terrain/splat/";
        boolean flipY = false;
        TextureKey textureKey
                = new TextureKey(assetFolder + "mountains512.png", flipY);
        AssetManager assetManager = Main.getApplication().getAssetManager();
        Texture texture = assetManager.loadTexture(textureKey);
        Image image = texture.getImage();
        /*
         * Scale it, smooth it, and convert it to an array.
         */
        float yScale = 0.3f;
        AbstractHeightMap heightmap = new ImageBasedHeightMap(image, yScale);
        heightmap.load();
        int radius = 1;
        heightmap.smooth(0.9f, radius);
        float[] heightArray = heightmap.getHeightMap();
        /*
         * Construct the TerrainQuad.
         */
        String name = "Mountains";
        int patchSize = 65;
        int totalSize = 513;
        TerrainQuad loadedTerrain
                = new TerrainQuad(name, patchSize, totalSize, heightArray);
        loadedTerrain.scale(10f);
        setCgm(loadedTerrain);
        /*
         * Apply a PBR terrain material.
         */
        String materialAssetPath = "/Materials/Vehicles/Mountains.j3m";
        Material material = assetManager.loadMaterial(materialAssetPath);
        loadedTerrain.setMaterial(material);
        /*
         * Generate a CollisionShape.
         */
        CollisionShape shape
                = CollisionShapeFactory.createMeshShape(loadedTerrain);
        setCollisionShape(shape);
    }

    /**
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDrop(Vector3f storeResult) {
        storeResult.set(291f, 20f, 2_060f);
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World. The World need not be loaded.
     */
    @Override
    public void resetCameraPosition() {
        Camera camera = Main.getApplication().getCamera();
        camera.setLocation(new Vector3f(285f, 12f, 2_063f));
        camera.lookAt(new Vector3f(291f, 9f, 2_060f), Vector3f.UNIT_Y);
    }
}
