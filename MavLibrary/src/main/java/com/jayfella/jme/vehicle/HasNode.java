package com.jayfella.jme.vehicle;

import com.jme3.scene.Node;

/**
 * Interface to the scene-graph representation of a game object.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface HasNode {
    // *************************************************************************
    // new methods exposed

    /**
     * Access the scene-graph subtree that represents this game object.
     *
     * @return the pre-existing instance (not null)
     */
    Node getNode();
}
