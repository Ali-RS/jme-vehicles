package com.jayfella.jme.vehicle;

import com.github.stephengold.jmepower.Loadable;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * A game world, such as the Vehicle Playground. Includes the collision object,
 * but not lights, post-processing, or sky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class World implements Loadable {
    // *************************************************************************
    // fields

    /**
     * manage decals
     */
    final private DecalManager decalManager = new DecalManager();
    /**
     * loaded C-G model of the game world
     */
    protected Node loadedCgm;
    // *************************************************************************
    // new methods exposed

    /**
     * Add this World to the specified scene and also to the PhysicsSpace.
     *
     * @param parent where to attach (not null)
     */
    void attachToScene(Node parent) {
        if (loadedCgm == null) {
            load();
        }
        parent.attachChild(loadedCgm);

        Node decalNode = decalManager.getNode();
        parent.attachChild(decalNode);

        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        RigidBodyControl rigidBodyControl
                = loadedCgm.getControl(RigidBodyControl.class);
        physicsSpace.add(rigidBodyControl);
    }

    /**
     * Remove this World from the scene-graph node and PhysicsSpace to which it
     * has been added.
     */
    void detachFromScene() {
        RigidBodyControl rigidBodyControl
                = loadedCgm.getControl(RigidBodyControl.class);
        rigidBodyControl.setPhysicsSpace(null);

        decalManager.getNode().removeFromParent();
        loadedCgm.removeFromParent();
    }

    /**
     * Determine the preferred intensity for direct light.
     *
     * @return the average color component (&ge;0)
     */
    abstract public float directLightIntensity();

    /**
     * Determine the drop location, which lies directly above the preferred
     * initial location for vehicles.
     *
     * @return a new location vector (in world coordinates)
     */
    abstract public Vector3f dropLocation();

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    abstract public float dropYRotation();

    /**
     * Access the C-G model.
     *
     * @return the pre-existing Node, or null if not yet loaded
     */
    public Node getCgm() {
        return loadedCgm;
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
     * this World. The world need not be loaded.
     */
    abstract public void resetCameraPosition();
}
