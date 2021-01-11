package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import java.util.logging.Logger;

/**
 * Visualize a car's tire smoke, with one ParticleEmitter per wheel.
 */
public class TireSmokeEmitter extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(TireSmokeEmitter.class.getName());
    // *************************************************************************
    // fields

    final private Car vehicle;
    /**
     * reusable temporary ColorRGBA
     */
    final private static ColorRGBA tmpColor = new ColorRGBA();
    private Node rootNode;
    /**
     * emitters, one for each wheel
     */
    private ParticleEmitter[] emitters;
    /**
     * reusable temporary Vector3f
     */
    final private static Vector3f tmpLocation = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AppState for the specfied Car.
     */
    public TireSmokeEmitter(Car car) {
        this.vehicle = car;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        int numWheels = vehicle.countWheels();
        emitters = new ParticleEmitter[numWheels];

        rootNode = ((SimpleApplication) getApplication()).getRootNode();
        AssetManager assetManager = app.getAssetManager();

        for (int wheelIndex = 0; wheelIndex < numWheels; wheelIndex++) {
            Wheel wheel = vehicle.getWheel(wheelIndex);
            wheel.tireSmokeColor(tmpColor);
            ParticleEmitter smoke = createEmitter(assetManager, tmpColor);
            emitters[wheelIndex] = smoke;

            wheel.getVehicleWheel().getLocation(tmpLocation);
            smoke.setLocalTranslation(tmpLocation);
            smoke.setShadowMode(RenderQueue.ShadowMode.Receive);
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; wheelIndex++) {
            emitters[wheelIndex].removeFromParent();
        }
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; wheelIndex++) {
            rootNode.attachChild(emitters[wheelIndex]);
        }
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; wheelIndex++) {
            ParticleEmitter emitter = emitters[wheelIndex];

            Wheel wheel = vehicle.getWheel(wheelIndex);
            float skidFraction = wheel.skidFraction();

            float particlesPerSecond;
            if (skidFraction > 0.25f) {
                particlesPerSecond = 100f * (skidFraction - 0.25f);

                wheel.getVehicleWheel().getCollisionLocation(tmpLocation);
                emitter.setLocalTranslation(tmpLocation);

                wheel.tireSmokeColor(tmpColor);
                emitter.setStartColor(tmpColor);

            } else {
                particlesPerSecond = 0f;
            }
            emitter.setParticlesPerSec(particlesPerSecond);
        }
    }
    // *************************************************************************
    // private methods

    private ParticleEmitter createEmitter(AssetManager assetManager,
            ColorRGBA startColor) {
        int numParticles = 250;
        ParticleEmitter result = new ParticleEmitter("Emitter",
                ParticleMesh.Type.Triangle, numParticles);

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        result.setMaterial(material);
        RenderState ars = material.getAdditionalRenderState();
        ars.setBlendMode(RenderState.BlendMode.Alpha);

        String assetPath = "Textures/Particles/smoke_line.png";
        Texture smokeLine = assetManager.loadTexture(assetPath);
        material.setTexture("Texture", smokeLine);
        result.setImagesX(15);
        result.setImagesY(1);

        result.setGravity(0f, -0.2f, 0f); // less dense than the ambient air
        result.setHighLife(3f);
        result.setLowLife(1f);

        result.setStartColor(startColor);
        ColorRGBA clear = new ColorRGBA(1f, 1f, 1f, 0f);
        result.setEndColor(clear);

        result.setStartSize(0f);
        result.setEndSize(3f);

        return result;
    }
}
