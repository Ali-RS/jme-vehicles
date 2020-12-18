package com.jayfella.jme.vehicle;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * A game environment/world, such as the Vehicle Playground. Doesn't include
 * sky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class Environment {
    // *************************************************************************
    // fields

    /**
     * loaded C-G model of the environment
     */
    protected Node loadedCgm;
    // *************************************************************************
    // new methods exposed

    /**
     * Add this loaded environment to the specified scene-graph node and also to
     * the PhysicsSpace. TODO rename attachToScene()
     */
    void add(Node parent) {
        assert loadedCgm != null;

        parent.attachChild(loadedCgm);

        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        RigidBodyControl rigidBodyControl
                = loadedCgm.getControl(RigidBodyControl.class);
        physicsSpace.add(rigidBodyControl);
    }

    /**
     * Determine the preferred intensity for direct light.
     *
     * @return the average color component (&ge;0)
     */
    abstract float directLightIntensity();

    /**
     * Determine the drop location, which lies directly above the preferred
     * initial location for vehicles.
     *
     * @return a new location vector (in world coordinates)
     */
    abstract Vector3f dropLocation();

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    abstract float dropYRotation();

    /**
     * Access the C-G model.
     *
     * @return the pre-existing Node, or null if not yet loaded
     */
    public Node getCgm() {
        return loadedCgm;
    }

    /**
     * Load the Environment from assets.
     *
     * @return the model's root node (a new instance)
     */
    abstract public Node load();

    /**
     * Remove this loaded environment from the scene-graph node and PhysicsSpace
     * to which it has been added. TODO rename detachFromScene()
     */
    void remove() {
        RigidBodyControl rigidBodyControl
                = loadedCgm.getControl(RigidBodyControl.class);
        PhysicsSpace physicsSpace = rigidBodyControl.getPhysicsSpace();
        physicsSpace.remove(rigidBodyControl);

        loadedCgm.removeFromParent();
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this Environment. The environment need not be loaded.
     */
    abstract public void resetCameraPosition();
}
