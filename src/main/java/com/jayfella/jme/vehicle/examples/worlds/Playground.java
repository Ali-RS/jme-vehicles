package com.jayfella.jme.vehicle.examples.worlds;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.World;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import java.util.logging.Logger;

/**
 * A sample World, build around James Khan's Vehicle Playground model.
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
     * Determine the preferred intensity for direct light.
     *
     * @return the average color component (&ge;0)
     */
    @Override
    public float directLightIntensity() {
        return 1f;
    }

    /**
     * Determine the drop location, which lies directly above the preferred
     * initial location for vehicles.
     *
     * @return a new location vector (in world coordinates)
     */
    @Override
    public Vector3f dropLocation() {
        return new Vector3f(1f, 6f, 0f);
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
        assert loadedCgm == null : "The model is already loaded.";

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Material material = new Material(assetManager, Materials.PBR);

        String prefix = "/Textures/Ground/Marble/marble_01_";
        String assetPath = prefix + "diff_2k.png";
        Texture baseColorMap = assetManager.loadTexture(assetPath);
        baseColorMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("BaseColorMap", baseColorMap);

        assetPath = prefix + "rough_2k.png";
        Texture roughnessMap = assetManager.loadTexture(assetPath);
        roughnessMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("RoughnessMap", roughnessMap);

        assetPath = prefix + "AO_2k.png";
        Texture aoMap = assetManager.loadTexture(assetPath);
        aoMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("LightMap", aoMap);
        material.setBoolean("LightMapAsAOMap", true);

        assetPath = prefix + "nor_2k.png";
        Texture normalMap = assetManager.loadTexture(assetPath);
        normalMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("NormalMap", normalMap);
        material.setFloat("NormalType", 1f);

        material.setFloat("Metallic", 0.001f);

        RenderState additional = material.getAdditionalRenderState();
        additional.setFaceCullMode(RenderState.FaceCullMode.Off);

        assetPath = "/Models/vehicle-playground/vehicle-playground.j3o";
        loadedCgm = (Node) assetManager.loadModel(assetPath);
        loadedCgm.setMaterial(material);

        loadedCgm.breadthFirstTraversal(spatial
                -> spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive));

        assetPath = "/Models/vehicle-playground/shapes/env-shape.j3o";
        CollisionShape shape;
        try {
            shape = (CollisionShape) assetManager.loadAsset(assetPath);
            Vector3f scale = loadedCgm.getWorldScale();
            shape.setScale(scale);
        } catch (AssetNotFoundException exception) {
            shape = CollisionShapeFactory.createMeshShape(loadedCgm);
        }

        RigidBodyControl rigidBodyControl
                = new RigidBodyControl(shape, PhysicsBody.massForStatic);
        loadedCgm.addControl(rigidBodyControl);
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World. The world need not be loaded.
     */
    @Override
    public void resetCameraPosition() {
        Camera camera = Main.getApplication().getCamera();
        camera.setLocation(new Vector3f(-3f, 1.6f, -1.5f));
        camera.lookAt(new Vector3f(1f, 0f, 0f), Vector3f.UNIT_Y);
    }
}
