package com.jayfella.jme.vehicle;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * A 3-D world for vehicles.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface VehicleWorld {
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    public float dropYRotation();

    /**
     * Access the AssetManager.
     *
     * @return the pre-existing instance (not null)
     */
    public AssetManager getAssetManager();

    /**
     * Access the PhysicsSpace.
     *
     * @return the pre-existing instance (not null)
     */
    public PhysicsSpace getPhysicsSpace();

    /**
     * Access the scene-graph node for visualizations.
     *
     * @return the pre-existing instance (not null)
     */
    public Node getSceneNode();

    /**
     * Access the AppStateManager.
     *
     * @return the pre-existing instance (not null)
     */
    public AppStateManager getStateManager();

    /**
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles.
     *
     * @param storeResult storage for the result (not null)
     */
    public void locateDrop(Vector3f storeResult);
}
