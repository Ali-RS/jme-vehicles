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

public class TyreSmokeEmitter extends BaseAppState {

    private Node rootNode;

    private final Vehicle vehicle;

    private int wheelCount;
    private ParticleEmitter[] emitters;

    public TyreSmokeEmitter(Vehicle vehicle) {
        this.vehicle = vehicle;

    }

    private ParticleEmitter createEmitter(AssetManager assetManager) {
        ParticleEmitter smoke = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);

        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture( "Textures/Particles/smoke_line.png"));

        smoke.setMaterial(mat_red);
        smoke.setImagesX(15);
        smoke.setImagesY(1); // 2x2 texture animation
        smoke.setEndColor(  new ColorRGBA(99 / 255f, 68 / 255f, 45 / 255f, 0.4f));   // red
        smoke.setStartColor(new ColorRGBA(183 / 255f, 130 / 255f, 89 / 255f, 0.05f)); // yellow
        smoke.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        smoke.setStartSize(1.0f);
        smoke.setEndSize(0.0f);
        smoke.setGravity(0, 0, 0);
        smoke.setLowLife(0.1f);
        smoke.setHighLife(2f);
        smoke.getParticleInfluencer().setVelocityVariation(0.3f);

        return smoke;
    }

    @Override
    protected void initialize(Application app) {


        this.wheelCount = vehicle.getVehicleControl().getNumWheels();

        if (rootNode == null) {
            rootNode = ((SimpleApplication)getApplication()).getRootNode();
        }

        if (this.emitters == null) {

            this.emitters = new ParticleEmitter[wheelCount];

            for (int i = 0; i < wheelCount; i++) {
                ParticleEmitter smoke = createEmitter(app.getAssetManager());
                smoke.setLocalTranslation(vehicle.getVehicleControl().getWheel(i).getLocation());
                smoke.setShadowMode(RenderQueue.ShadowMode.Receive);
                emitters[i] = smoke;

            }

        }


    }

    @Override protected void cleanup(Application app) { }

    @Override protected void onEnable() {

        for (int i = 0; i < emitters.length; i++) {
            rootNode.attachChild(emitters[i]);
        }
    }

    @Override protected void onDisable() {

        for (int i = 0; i < emitters.length; i++) {
            emitters[i].removeFromParent();
        }

    }



    @Override
    public void update(float tpf) {

        for (int i = 0; i < wheelCount; i++) {

            VehicleWheel wheel = vehicle.getVehicleControl().getWheel(i);

            ParticleEmitter smoke = emitters[i];

            smoke.setLocalTranslation(wheel.getCollisionLocation());

            if (wheel.getSkidInfo() < 0.5) {

                float scale = 1.0f - wheel.getSkidInfo();

                smoke.emitParticles((int) (scale * 20));
                // smoke.getParticleInfluencer().setInitialVelocity(vehicle.getVehicleControl().getLinearVelocity().negate().mult(scale * 0.25f));
                smoke.getParticleInfluencer().setInitialVelocity(vehicle.getVehicleControl().getPhysicsRotation().getRotationColumn(2).negate()
                        .mult(scale * (vehicle.getSpeed(Vehicle.SpeedUnit.KMH) / 10)));

            }
            else {
                // smoke.emitParticles(0);
                smoke.setParticlesPerSec(0);
            }

        }





    }

}
