package com.jayfella.jme.vehicle.examples.worlds;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.World;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.util.logging.Logger;

/**
 * A single-chunk sample World, built around James Khan's Vehicle Playground
 * model.
 */
public class Playground extends World {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Playground.class.getName());
    // *************************************************************************
    // World methods

    /**
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles. TODO re-order methods
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDrop(Vector3f storeResult) {
        storeResult.set(1f, 6f, 0f);
    }

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    @Override
    public float dropYRotation() {
        return 0f;
    }

    /**
     * Load this World from assets.
     */
    @Override
    public void load() {
        assert getCgm() == null : "The model is already loaded.";

        String assetFolder = "/Models/vehicle-playground/";
        String assetPath = assetFolder + "vehicle-playground.j3o";
        AssetManager assetManager = Main.getApplication().getAssetManager();
        Node loadedCgm = (Node) assetManager.loadModel(assetPath);
        setCgm(loadedCgm);

        assetPath = "/Materials/Vehicles/marble_01.j3m";
        Material material = assetManager.loadMaterial(assetPath);
        loadedCgm.setMaterial(material);

        LightList lights = loadedCgm.getLocalLightList();
        Light light = lights.get(0);
        loadedCgm.removeLight(light);

        loadedCgm.breadthFirstTraversal(spatial
                -> spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive));

        Platform platform = JmeSystem.getPlatform();
        if (platform == Platform.Windows64) {
            assetPath = assetFolder + "shapes/env-shape-Windows64.j3o";
        } else {
            assetPath = assetFolder + "shapes/env-shape.j3o";
        }

        CollisionShape shape;
        try {
            shape = (CollisionShape) assetManager.loadAsset(assetPath);
        } catch (AssetNotFoundException exception) {
            shape = CollisionShapeFactory.createMeshShape(loadedCgm);
        }
        setCollisionShape(shape);
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World. The World need not be loaded.
     */
    @Override
    public void resetCameraPosition() {
        Camera camera = Main.getApplication().getCamera();
        camera.setLocation(new Vector3f(-3f, 1.6f, -1.5f));
        camera.lookAt(new Vector3f(1f, 0f, 0f), Vector3f.UNIT_Y);
    }
}
