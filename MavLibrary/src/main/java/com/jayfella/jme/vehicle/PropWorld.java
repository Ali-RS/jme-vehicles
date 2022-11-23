package com.jayfella.jme.vehicle;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import java.util.Collection;

/**
 * A 3-D world for props.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface PropWorld {
    // *************************************************************************
    // new methods exposed

    /**
     * Add the specified Prop to this world.
     *
     * @param newProp (not null, not already added)
     */
    void addProp(Prop newProp);

    /**
     * Access the AssetManager.
     *
     * @return the pre-existing instance (not null)
     */
    AssetManager getAssetManager();

    /**
     * Access the scene-graph node for adding probes and attaching spatials.
     *
     * @return the pre-existing instance (not null)
     */
    Node getParentNode();

    /**
     * Access the PhysicsSpace.
     *
     * @return the pre-existing instance (not null)
     */
    PhysicsSpace getPhysicsSpace();

    /**
     * Access the AppStateManager.
     *
     * @return the pre-existing instance (not null)
     */
    AppStateManager getStateManager();

    /**
     * Enumerate props that have been added to this world and not yet removed.
     *
     * @return a new unmodifiable collection of pre-existing instances (not
     * null)
     */
    Collection<Prop> listProps();

    /**
     * Remove the specified Prop from this World.
     *
     * @param prop (not null, previously added)
     */
    void removeProp(Prop prop);
}
