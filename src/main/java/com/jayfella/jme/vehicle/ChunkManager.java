package com.jayfella.jme.vehicle;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * An AppState to manage a World that's decomposed into equal-sized, rectangular
 * chunks.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ChunkManager extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ChunkManager.class.getName());
    // *************************************************************************
    // fields

    /**
     * chunk currently occupying the scene's origin
     */
    private ChunkId originChunk = ChunkId.zero;
    /**
     * map attached chunks to nodes
     */
    final private Map<ChunkId, Node> idToNode = new HashMap<>();
    /**
     * where to attach chunks
     */
    final Node parent = new Node("Chunks");
    /**
     * dimensions of each chunk (in world units)
     */
    final private Vector3f chunkDimensions = new Vector3f(1e9f, 1e9f, 1e9f);

    private World world;
    // *************************************************************************
    // new methods exposed

    public void setWorld(World world) {
        detachAll();
        this.world = world;
        if (world != null) {
            world.chunkDimensions(chunkDimensions);
        }
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        Node rootNode = Main.getApplication().getRootNode();
        rootNode.attachChild(parent);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        parent.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        Node rootNode = Main.getApplication().getRootNode();
        rootNode.attachChild(parent);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        if (world == null) {
            return;
        }

        Set<ChunkId> nearbySet = world.listNearbyChunks();

        int numAttached = idToNode.size();
        ChunkId[] oldChunks = new ChunkId[numAttached];
        idToNode.keySet().toArray(oldChunks);
        for (ChunkId chunk : oldChunks) {
            if (!nearbySet.contains(chunk)) {
                Node oldNode = idToNode.remove(chunk);
                oldNode.removeFromParent();
            }
        }

        Vector3f tmpOffset = new Vector3f(); // TODO static
        for (ChunkId chunk : nearbySet) {
            if (!idToNode.containsKey(chunk)) {
                Node newNode = world.createChunk(chunk);
                parent.attachChild(newNode);
                idToNode.put(chunk, newNode);
            }

            Node node = idToNode.get(chunk);
            locateCenter(chunk, tmpOffset);
            node.setLocalTranslation(tmpOffset);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Detach all chunks from the scene.
     */
    private void detachAll() {
        for (Node oldNode : idToNode.values()) {
            oldNode.removeFromParent();
        }
        idToNode.clear();
    }

    /**
     * Locate the center of the identified chunk.
     *
     * @param chunk the chunk to locate (not null)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector in scene coordinates (either storeResult or a
     * new instance)
     */
    private Vector3f locateCenter(ChunkId chunk, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;

        result.x = chunk.xDiff(originChunk);
        result.y = chunk.yDiff(originChunk);
        result.z = chunk.zDiff(originChunk);
        result.multLocal(chunkDimensions);

        return result;
    }
}
