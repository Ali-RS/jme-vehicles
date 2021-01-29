package com.jayfella.jme.vehicle;

import com.github.stephengold.jmepower.Loadable;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.HashSet;
import java.util.Set;
import jme3utilities.MyCamera;
import jme3utilities.math.Vector3i;

/**
 * A game world, such as the Vehicle Playground. Includes the C-G model and
 * collision object, but not lights, post-processors, or sky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class World
        implements Loadable, VehicleWorld {
    // *************************************************************************
    // fields

    /**
     * loaded CollisionShape
     */
    private CollisionShape loadedShape;
    /**
     * manage decals
     */
    final private DecalManager decalManager = new DecalManager();
    /**
     * loaded C-G model of a prototypical chunk
     */
    private Node loadedCgm;
    /**
     * collision object
     */
    private PhysicsRigidBody rigidBody;
    // *************************************************************************
    // new methods exposed

    /**
     * Add this World to the specified scene and also to the PhysicsSpace.
     *
     * @param parent where to attach (not null)
     */
    public void attachToScene(Node parent) {
        if (loadedCgm == null) {
            AssetManager assetManager = getAssetManager();
            load(assetManager);
        }
        Main.findAppState(ChunkManager.class).setWorld(this);

        Node decalNode = decalManager.getNode();
        parent.attachChild(decalNode);

        PhysicsSpace physicsSpace = getPhysicsSpace();
        rigidBody
                = new PhysicsRigidBody(loadedShape, PhysicsBody.massForStatic);
        physicsSpace.add(rigidBody);
        /*
         * Set the far clipping plane for this world.
         *
         * The dash camera sits close to the bodywork, so set the near clipping
         * plane accordingly.
         */
        Camera cam = Main.getApplication().getCamera();
        float near = 0.1f;
        float far = farDistance();
        MyCamera.setNearFar(cam, near, far);
    }

    /**
     * Determine the dimensions of each scene chunk, in scene units. For
     * single-chunk worlds, the result is always (1e9,1e9,1e9).
     *
     * @param storeResult storage for the result (not null)
     */
    public void chunkDimensions(Vector3f storeResult) {
        storeResult.set(1e9f, 1e9f, 1e9f);
    }

    /**
     * Create a Node for the identified scene chunk.
     *
     * @param storeResult storage for the result (not null)
     */
    Node createChunk(Vector3i chunkId) {
        boolean cloneMaterials = false;
        Node result = loadedCgm.clone(cloneMaterials);

        return result;
    }

    /**
     * Remove this World from the scene-graph node and PhysicsSpace to which it
     * has been added.
     */
    public void detachFromScene() {
        PhysicsSpace space = (PhysicsSpace) rigidBody.getCollisionSpace();
        space.removeCollisionObject(rigidBody);

        decalManager.getNode().removeFromParent();
        Main.findAppState(ChunkManager.class).setWorld(null);
    }

    /**
     * Determine the distance to the camera's far plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    abstract public float farDistance();

    /**
     * Access the loaded C-G model.
     *
     * @return the pre-existing Node, or null if not yet loaded
     */
    public Node getCgm() {
        return loadedCgm;
    }

    /**
     * Access the loaded CollisionShape.
     *
     * @return the pre-existing instance, or null if not yet loaded
     */
    public CollisionShape getCollisionShape() {
        return loadedShape;
    }

    /**
     * Access the decal manager.
     *
     * @return the pre-existing instance, or null if not yet loaded
     */
    public DecalManager getDecalManager() {
        return decalManager;
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World. The World need not be loaded.
     */
    abstract public void resetCameraPosition();
    // *************************************************************************
    // new protected methods

    /**
     * Access the application's default Camera.
     *
     * @return the pre-existing instance (not null)
     */
    protected Camera getCamera() {
        Camera result = Main.getApplication().getCamera();

        assert result != null;
        return result;
    }

    /**
     * Enumerate all chunks that are near the scene origin. For single-chunk
     * worlds, the result is always (0,0,0).
     *
     * @return a new collection of chunk IDs (not null)
     */
    protected Set<Vector3i> listNearbyChunks() {
        Set<Vector3i> result = new HashSet<>();
        result.add(Vector3i.zero);
        return result;
    }

    /**
     * Alter which C-G model is loaded.
     *
     * @param cgm the desired model
     */
    protected void setCgm(Node cgm) {
        this.loadedCgm = cgm;
    }

    /**
     * Alter which CollisionShape is loaded.
     *
     * @param shape the desired shape
     */
    protected void setCollisionShape(CollisionShape shape) {
        this.loadedShape = shape;
    }
    // *************************************************************************
    // VehicleWorld methods

    /**
     * Access the AssetManager.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public AssetManager getAssetManager() {
        AssetManager result = Main.getApplication().getAssetManager();

        assert result != null;
        return result;
    }

    /**
     * Access the PhysicsSpace.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public PhysicsSpace getPhysicsSpace() {
        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace result = bulletAppState.getPhysicsSpace();

        assert result != null;
        return result;
    }

    /**
     * Access the scene-graph node for visualization.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public Node getSceneNode() {
        Node result = Main.getApplication().getRootNode();

        assert result != null;
        return result;
    }

    /**
     * Access the AppStateManager.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public AppStateManager getStateManager() {
        AppStateManager result = Main.getApplication().getStateManager();
        assert result != null;
        return result;
    }
}
