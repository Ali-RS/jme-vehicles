package com.jayfella.jme.vehicle;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

/**
 * An Environment based on "race1.j3o", Adi Barda's racetrack model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Racetrack extends Environment {
    // *************************************************************************
    // Environment methods

    /**
     * Determine the preferred intensity for direct light.
     *
     * @return the average color component (&ge;0)
     */
    @Override
    float directLightIntensity() {
        return 2.5f;
    }

    /**
     * Determine the drop location, which lies directly above the preferred
     * initial location for vehicles.
     *
     * @return a new location vector (in world coordinates)
     */
    @Override
    Vector3f dropLocation() {
        return new Vector3f(-92f, 6f, 675f);
    }

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    @Override
    float dropYRotation() {
        return FastMath.PI;
    }

    /**
     * Load this Environment from assets.
     */
    @Override
    Node load() {
        assert loadedCgm == null : "The model is already loaded.";

        AssetManager assetManager = Main.getApplication().getAssetManager();
        loadedCgm = (Node) assetManager.loadModel("Models/race1/race1.j3o");

        CollisionShape shape = CollisionShapeFactory.createMeshShape(loadedCgm);
        RigidBodyControl rigidBodyControl
                = new RigidBodyControl(shape, PhysicsBody.massForStatic);
        loadedCgm.addControl(rigidBodyControl);

        return loadedCgm;
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this Environment. The environment need not be loaded.
     */
    @Override
    public void resetCameraPosition() {
        Camera camera = Main.getApplication().getCamera();
        camera.setLocation(new Vector3f(-92f, 6f, 700f));
        camera.lookAt(new Vector3f(-92f, 0f, 675f), Vector3f.UNIT_Y);
    }
}
