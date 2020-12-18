package com.jayfella.jme.vehicle;

import com.jme3.asset.AssetManager;
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

/**
 * An Environment based on "vehicle-playground.j3o", James Khan's Vehicle
 * Playground model.
 */
public class Playground extends Environment {
    // *************************************************************************
    // Environment methods

    /**
     * Determine the preferred intensity for direct light.
     *
     * @return the average color component (&ge;0)
     */
    @Override
    float directLightIntensity() {
        return 1f;
    }

    /**
     * Determine the drop location, which lies directly above the preferred
     * initial location for vehicles.
     *
     * @return a new location vector (in world coordinates)
     */
    @Override
    Vector3f dropLocation() {
        return new Vector3f(0f, 6f, 0f);
    }

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    @Override
    float dropYRotation() {
        return 0f;
    }

    /**
     * Load this Environment from assets.
     * 
     * @return the model's root node (a new instance)
     */
    @Override
    public Node load() {
        assert loadedCgm == null : "The model is already loaded.";

        AssetManager assetManager = Main.getApplication().getAssetManager();
        Material material = new Material(assetManager, Materials.PBR);

        Texture baseColorMap = assetManager.loadTexture(
                "Textures/Ground/Marble/marble_01_diff_2k.png");
        baseColorMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("BaseColorMap", baseColorMap);

        Texture roughnessMap = assetManager.loadTexture(
                "Textures/Ground/Marble/marble_01_rough_2k.png");
        roughnessMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("RoughnessMap", roughnessMap);

        Texture aoMap = assetManager.loadTexture(
                "Textures/Ground/Marble/marble_01_AO_2k.png");
        aoMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("LightMap", aoMap);
        material.setBoolean("LightMapAsAOMap", true);

        //Texture dispMap = assetManager.loadTexture("Textures/Ground/Marble/marble_01_disp_2k.png");
        //dispMap.setWrap(Texture.WrapMode.Repeat);
        //material.setTexture("ParallaxMap", dispMap);
        Texture normalMap = assetManager.loadTexture(
                "Textures/Ground/Marble/marble_01_nor_2k.png");
        normalMap.setWrap(Texture.WrapMode.Repeat);
        material.setTexture("NormalMap", normalMap);
        material.setFloat("NormalType", 1f);

        material.setFloat("Metallic", 0.001f);

        // material.setBoolean("UseFog", true);
        // material.setColor("FogColor", new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        // material.setFloat("ExpSqFog", 0.002f);
        RenderState additional = material.getAdditionalRenderState();
        additional.setFaceCullMode(RenderState.FaceCullMode.Off);

        loadedCgm = (Node) assetManager.loadModel(
                "Models/vehicle-playground/vehicle-playground.j3o");
        loadedCgm.setMaterial(material);

        loadedCgm.breadthFirstTraversal(spatial
                -> spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive));

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
        camera.setLocation(new Vector3f(0f, 3f, -11f));
        camera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);
    }
}
