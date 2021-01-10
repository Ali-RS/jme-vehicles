package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
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
    private Node rootNode;
    /**
     * emitters, one for each wheel
     */
    private ParticleEmitter[] emitters;
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
            ParticleEmitter smoke = createEmitter(assetManager);
            emitters[wheelIndex] = smoke;

            Wheel wheel = vehicle.getWheel(wheelIndex);
            Vector3f location = wheel.getVehicleWheel().getLocation();
            smoke.setLocalTranslation(location);
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
        VehicleControl vehicleControl = vehicle.getVehicleControl();
        Quaternion orientation = vehicleControl.getPhysicsRotation();
        Vector3f rearDirection = orientation.getRotationColumn(2).negate();

        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; wheelIndex++) {
            Wheel wheel = vehicle.getWheel(wheelIndex);

            ParticleEmitter smoke = emitters[wheelIndex];
            Vector3f location = wheel.getVehicleWheel().getCollisionLocation();
            smoke.setLocalTranslation(location);

            float skidFraction = wheel.skidFraction();
            if (skidFraction > 0.5f) {
                smoke.emitParticles((int) (skidFraction * 20f));

                float speed = skidFraction * vehicle.getSpeed(SpeedUnit.KPH) / 10f;
                Vector3f velocity = rearDirection.mult(speed);
                smoke.getParticleInfluencer().setInitialVelocity(velocity);

            } else {
                smoke.setParticlesPerSec(0);
            }
        }
    }
    // *************************************************************************
    // private methods

    private ParticleEmitter createEmitter(AssetManager assetManager) {
        int numParticles = 30;
        ParticleEmitter result = new ParticleEmitter("Emitter",
                ParticleMesh.Type.Triangle, numParticles);

        Material mat_red = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        String assetPath = "Textures/Particles/smoke_line.png";
        Texture smokeLine = assetManager.loadTexture(assetPath);
        mat_red.setTexture("Texture", smokeLine);

        ColorRGBA red = new ColorRGBA(99 / 255f, 68 / 255f, 45 / 255f, 0.4f);
        ColorRGBA yellow = new ColorRGBA(183 / 255f, 130 / 255f, 89 / 255f, 0.05f);

        result.setEndColor(red);
        result.setEndSize(0f);
        result.setGravity(0f, 0f, 0f);
        result.setHighLife(2f);
        result.setImagesX(15);
        result.setImagesY(1);
        result.setLowLife(0.1f);
        result.setMaterial(mat_red);
        result.setStartColor(yellow);
        result.setStartSize(1f);

        ParticleInfluencer influencer = result.getParticleInfluencer();
        influencer.setInitialVelocity(new Vector3f(0f, 2f, 0f));
        influencer.setVelocityVariation(0.3f);

        return result;
    }
}
