package com.jayfella.jme.vehicle;

import com.github.stephengold.jmepower.Loadable;
import com.jayfella.jme.vehicle.lemurdemo.Main;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashSet;
import java.util.Set;
import jme3utilities.math.Vector3i;

/**
 * A game world, such as the Vehicle Playground. Includes the C-G model and
 * collision object, but not lights, post-processors, or sky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class World implements Loadable {
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
            AssetManager assetManager = Main.getApplication().getAssetManager();
            load(assetManager);
        }
        Main.findAppState(ChunkManager.class).setWorld(this);

        Node decalNode = decalManager.getNode();
        parent.attachChild(decalNode);

        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        rigidBody
                = new PhysicsRigidBody(loadedShape, PhysicsBody.massForStatic);
        physicsSpace.add(rigidBody);
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
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    abstract public float dropYRotation();

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
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles.
     *
     * @param storeResult storage for the result (not null)
     */
    abstract public void locateDrop(Vector3f storeResult);

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World. The World need not be loaded.
     */
    abstract public void resetCameraPosition();
    // *************************************************************************
    // new methods exposed

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
}
