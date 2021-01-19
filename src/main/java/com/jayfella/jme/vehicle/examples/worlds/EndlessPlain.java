package com.jayfella.jme.vehicle.examples.worlds;

import com.jayfella.jme.vehicle.ChunkId;
import com.jayfella.jme.vehicle.ChunkManager;
import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.World;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.material.Material;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A sample World, built around the "marble_01" material and the
 * PlaneCollisionShape.
 */
public class EndlessPlain extends World {
    // *************************************************************************
    // constants and loggers

    /**
     * X-Z size of each chunk, in world units
     */
    final private static float chunkSize = 100f;
    /**
     * number of axes
     */
    final private static int numAxes = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EndlessPlain.class.getName());
    // *************************************************************************
    // World methods

    /**
     * Determine the dimensions of each scene chunk, in world units.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void chunkDimensions(Vector3f storeResult) {
        storeResult.set(chunkSize, 1e9f, chunkSize);
    }

    /**
     * Determine the preferred intensity for direct light.
     *
     * @return the average color component (&ge;0)
     */
    @Override
    public float directLightIntensity() {
        return 1f;
    }

    /**
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles.
     *
     * @return a new location vector (in world coordinates)
     */
    @Override
    public Vector3f dropLocation() {
        return new Vector3f(0f, 9f, 0f);
    }

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    @Override
    public float dropYRotation() {
        return 0f;
    }

    /**
     * Enumerate all chunks that are near the scene origin according to discrete
     * Chebyshev distance.
     *
     * @return a new collection of IDs (not null)
     */
    @Override
    protected Set<ChunkId> listNearbyChunks() {
        Camera camera = Main.getApplication().getCamera();
        float cameraY = camera.getLocation().y;
        int discreteR = Math.round(1.5f + 4f * cameraY / chunkSize);

        ChunkManager chunkManager = Main.findAppState(ChunkManager.class);
        ChunkId sceneOrigin = chunkManager.originChunk();
        int originX = sceneOrigin.x();
        int originZ = sceneOrigin.z();

        Set<ChunkId> result = new HashSet<>();
        for (int deltaX = -discreteR; deltaX <= discreteR; ++deltaX) {
            int chunkX = originX + deltaX;
            for (int deltaZ = -discreteR; deltaZ <= discreteR; ++deltaZ) {
                int chunkZ = originZ + deltaZ;
                ChunkId chunkId = new ChunkId(chunkX, 0, chunkZ);
                result.add(chunkId);
            }
        }

        return result;
    }

    /**
     * Load this World from assets.
     */
    @Override
    public void load() {
        assert loadedCgm == null : "The model is already loaded.";
        /*
         * Generate texture coordinates and vertex positions
         * for a large square in the X-Z plane.
         */
        float uvDiameter = 5f;
        FloatBuffer uvs = BufferUtils.createFloatBuffer(
                uvDiameter, uvDiameter,
                0f, 0f,
                uvDiameter, 0f,
                uvDiameter, uvDiameter,
                0f, uvDiameter,
                0f, 0f
        );
        float posRadius = chunkSize / 2;
        FloatBuffer positions = BufferUtils.createFloatBuffer(
                +posRadius, 0f, +posRadius,
                -posRadius, 0f, -posRadius,
                -posRadius, 0f, +posRadius,
                +posRadius, 0f, +posRadius,
                +posRadius, 0f, -posRadius,
                -posRadius, 0f, -posRadius
        );
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, numAxes, positions);
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uvs);
        mesh.updateBound();

        loadedCgm = new Node("Endless Plain");
        loadedCgm.setShadowMode(RenderQueue.ShadowMode.Receive);

        Geometry geometry = new Geometry("Plain Chunk", mesh);
        loadedCgm.attachChild(geometry);

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = "/Materials/Vehicles/marble_01.j3m";
        Material material = assetManager.loadMaterial(assetPath);
        loadedCgm.setMaterial(material);

        float planeConstant = 0f;
        Plane plane = new Plane(Vector3f.UNIT_Y, planeConstant);
        CollisionShape shape = new PlaneCollisionShape(plane);
        setCollisionShape(shape);
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World. The World need not be loaded.
     */
    @Override
    public void resetCameraPosition() {
        Camera camera = Main.getApplication().getCamera();
        camera.setLocation(new Vector3f(-3.8f, 2.3f, -2.9f));
        camera.lookAt(new Vector3f(1f, 0f, 0f), Vector3f.UNIT_Y);
    }
}
