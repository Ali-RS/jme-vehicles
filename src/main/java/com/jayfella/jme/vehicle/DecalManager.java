package com.jayfella.jme.vehicle;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * AppState to manage decal geometries and "age them out" on a first-in,
 * first-out (FIFO) basis.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DecalManager {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum number of triangles to retain
     */
    final private static int maxTriangles = 9_999;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DecalManager.class.getName());
    // *************************************************************************
    // fields

    /**
     * queue of retained decals, from oldest to newest
     */
    final private Deque<Geometry> fifo = new LinkedList<>();
    /**
     * assign unique names to inactive decals
     */
    private int nextId = 0;
    /**
     * total triangles across all retained decals
     */
    private int totalTriangles = 0;
    /**
     * parent the decals
     */
    final private Node decalNode = new Node("Decal Node");
    // *************************************************************************
    // new methods exposed

    /**
     * Add a decal based on the specified template.
     *
     * @param active the template (not null, not empty, unaffected)
     */
    public void addCloneOf(Geometry active) {
        Geometry decal = (Geometry) active.deepClone();
        addDecal(decal);
    }

    /**
     * Add the specified decal to the queue.
     *
     * @param active the template (not null, not empty, alias created)
     */
    void addDecal(Geometry decal) {
        int triangleCount = decal.getTriangleCount();
        assert triangleCount > 0 : triangleCount;

        decal.setName("decal #" + nextId);
        ++nextId;

        fifo.addLast(decal);
        decalNode.attachChild(decal);
        totalTriangles += triangleCount;
        /*
         * Remove enough old decals to stay at or below the limit.
         */
        while (totalTriangles > maxTriangles) {
            Geometry oldest = fifo.removeFirst();
            oldest.removeFromParent();
            int count = oldest.getTriangleCount();
            totalTriangles -= count;
        }
    }

    /**
     * Access the scene-graph node with all the decals.
     *
     * @return the pre-existing instance (not null)
     */
    Node getNode() {
        return decalNode;
    }
}
