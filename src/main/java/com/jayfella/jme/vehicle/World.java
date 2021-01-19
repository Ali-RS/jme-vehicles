package com.jayfella.jme.vehicle;

import com.github.stephengold.jmepower.Loadable;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

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
     * loaded C-G model of the game world
     */
    protected Node loadedCgm;
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
    void attachToScene(Node parent) {
        if (loadedCgm == null) {
            load();
        }
        parent.attachChild(loadedCgm);

        Node decalNode = decalManager.getNode();
        parent.attachChild(decalNode);

        BulletAppState bulletAppState = Main.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        rigidBody
                = new PhysicsRigidBody(loadedShape, PhysicsBody.massForStatic);
        physicsSpace.add(rigidBody);
    }

    /**
     * Remove this World from the scene-graph node and PhysicsSpace to which it
     * has been added.
     */
    void detachFromScene() {
        PhysicsSpace space = (PhysicsSpace) rigidBody.getCollisionSpace();
        space.removeCollisionObject(rigidBody);

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
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles. TODO rename locateDrop()
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

    /**
     * Alter the loaded CollisionShape.
     *
     * @param shape the desired shape
     */
    protected void setCollisionShape(CollisionShape shape) {
        this.loadedShape = shape;
    }
}
