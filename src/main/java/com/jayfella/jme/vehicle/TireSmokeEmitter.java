package com.jayfella.jme.vehicle;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import java.util.logging.Logger;

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

    private int wheelCount;
    private Node rootNode;
    private ParticleEmitter[] emitters;
    final private Vehicle vehicle;

    public TireSmokeEmitter(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    private ParticleEmitter createEmitter(AssetManager assetManager) {
        ParticleEmitter result = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);

        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture("Textures/Particles/smoke_line.png"));

        result.setMaterial(mat_red);
        result.setImagesX(15);
        result.setImagesY(1); // 2x2 texture animation
        result.setEndColor(new ColorRGBA(99 / 255f, 68 / 255f, 45 / 255f, 0.4f));   // red
        result.setStartColor(new ColorRGBA(183 / 255f, 130 / 255f, 89 / 255f, 0.05f)); // yellow
        result.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        result.setStartSize(1.0f);
        result.setEndSize(0.0f);
        result.setGravity(0, 0, 0);
        result.setLowLife(0.1f);
        result.setHighLife(2f);
        result.getParticleInfluencer().setVelocityVariation(0.3f);

        return result;
    }

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
        wheelCount = vehicle.getVehicleControl().getNumWheels();

        if (rootNode == null) {
            rootNode = ((SimpleApplication) getApplication()).getRootNode();
        }

        if (emitters == null) {
            emitters = new ParticleEmitter[wheelCount];

            for (int i = 0; i < wheelCount; i++) {
                ParticleEmitter smoke = createEmitter(app.getAssetManager());
                smoke.setLocalTranslation(vehicle.getVehicleControl().getWheel(i).getLocation());
                smoke.setShadowMode(RenderQueue.ShadowMode.Receive);
                emitters[i] = smoke;
            }
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        for (int i = 0; i < emitters.length; i++) {
            emitters[i].removeFromParent();
        }
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        for (int i = 0; i < emitters.length; i++) {
            rootNode.attachChild(emitters[i]);
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
        for (int i = 0; i < wheelCount; i++) {
            VehicleWheel wheel = vehicle.getVehicleControl().getWheel(i);

            ParticleEmitter smoke = emitters[i];
            smoke.setLocalTranslation(wheel.getCollisionLocation());

            float traction = wheel.getSkidInfo();
            if (traction < 0.5f) {
                float scale = 1f - traction;

                smoke.emitParticles((int) (scale * 20));
                // smoke.getParticleInfluencer().setInitialVelocity(vehicle.getVehicleControl().getLinearVelocity().negate().mult(scale * 0.25f));
                smoke.getParticleInfluencer().setInitialVelocity(vehicle.getVehicleControl().getPhysicsRotation().getRotationColumn(2).negate()
                        .mult(scale * (vehicle.getSpeed(SpeedUnit.KPH) / 10)));

            } else {
                // smoke.emitParticles(0);
                smoke.setParticlesPerSec(0);
            }
        }
    }
}
