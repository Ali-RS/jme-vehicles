package com.jayfella.jme.vehicle.view;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.math.Vector3f;

/**
 * An interface to determine the location and forward direction of a camera
 * target that's part of a collision object.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface Target {

    /**
     * Determine the forward direction for chase purposes.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in world coordinates (either storeResult or a new
     * instance)
     */
    Vector3f forwardDirection(Vector3f storeResult);

    /**
     * Access the collision object that contains the primary target.
     *
     * @return the pre-existing object (not null)
     */
    PhysicsCollisionObject getTargetPco();

    /**
     * Determine the world location of the primary target.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the world location vector (either storeResult or a new instance)
     */
    Vector3f target(Vector3f storeResult);
}
