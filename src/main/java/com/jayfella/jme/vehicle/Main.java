package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.gui.LoadingState;
import com.jayfella.jme.vehicle.gui.MainMenuState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.AssetEventListener;
import com.jme3.asset.AssetKey;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickConnectionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.style.BaseStyles;

import java.util.concurrent.CompletableFuture;

public class Main extends SimpleApplication {

    public static void main(String... args) {
        Main main = new Main();

        AppSettings appSettings = new AppSettings(true);
        // appSettings.setFrameRate(120);

        appSettings.setResolution(1280, 720);
        appSettings.setTitle("jMonkeyEngine :: Advanced Vehicles");

        appSettings.setUseJoysticks(true);

        main.setDisplayStatView(false);
        main.setDisplayFps(false);

        main.setSettings(appSettings);
        main.setShowSettings(true);
        main.start();
    }

    private Main() {
        super(new StatsAppState(), new AudioListenerState(), new LoadingState());
    }

    private void setAnistropy() {
        assetManager.addAssetEventListener(new AssetEventListener() {

            private String[] extensions = {
                    "png", "jpg", "gif", "dds"
            };

            @Override
            public void assetLoaded(AssetKey key) { }

            @Override
            public void assetRequested(AssetKey key) {

                for (String ext : extensions) {
                    if (key.getExtension().equalsIgnoreCase(ext)) {
                        TextureKey tkey = (TextureKey) key;
                        tkey.setAnisotropy(16);
                    }
                }

            }

            @Override public void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey) { }
        });
    }

    @Override
    public void simpleInitApp() {

        setAnistropy();
        getInputManager().addJoystickConnectionListener(new JoystickConnectionListener() {
            @Override
            public void onConnected(Joystick joystick) {
                System.out.println("Joystick connected: " + joystick);
            }

            @Override
            public void onDisconnected(Joystick joystick) {
                System.out.println("Joystick disconnected: " + joystick);
            }
        });

        inputManager.clearMappings();
        inputManager.clearRawInputListeners();

        // the "hood-cam" gets close to the bodywork, so set the near-frustum accordingly...
        float aspect = cam.getWidth() / (float) cam.getHeight();
        cam.setFrustumPerspective(60, aspect, 0.1f, 1000);

        // the speedo uses Lemur labels.
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Let there be light
        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(1, -.45f, 0.5f).normalizeLocal(),
                ColorRGBA.White.clone()
        );
        rootNode.addLight(directionalLight);

        // initialize physics
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        getStateManager().attach(bulletAppState);

        // load the sky async.
        CompletableFuture
                .supplyAsync(() -> {
                    Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/quarry_03_4k.jpg", SkyFactory.EnvMapType.EquirectMap);
                    sky.setQueueBucket(RenderQueue.Bucket.Sky);
                    sky.setShadowMode(RenderQueue.ShadowMode.Off);
                    return sky;
                })
                .whenComplete((spatial, ex) -> {
                    enqueue( () -> rootNode.attachChild(spatial) );
                });

        // load the playground async.
        CompletableFuture
                .supplyAsync(() -> {
                    Node node =  (Node) loadPlayground();
                    RigidBodyControl rigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createMeshShape(node), 0);
                    node.addControl(rigidBodyControl);
                    return node;
                })
                .whenComplete((node, ex) -> {
                    enqueue( () -> {
                        rootNode.attachChild(node);
                        RigidBodyControl rigidBodyControl = node.getControl(RigidBodyControl.class);
                        bulletAppState.getPhysicsSpace().add(rigidBodyControl);

                        getStateManager().getState(LoadingState.class).setEnabled(false);
                        getStateManager().attach(new MainMenuState());
                    });
                });

        cam.setLocation(new Vector3f(-200, 50, -200));
        cam.lookAt(new Vector3f(100, 10, 150), Vector3f.UNIT_Y);

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        addPostProcessing(directionalLight);

        // this consumes joystick input. I'll have to investigate why.
        stateManager.getState(FocusNavigationState.class).setEnabled(false);
    }

    private Spatial loadPlayground() {

        Material material = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");

        Texture baseColorMap = assetManager.loadTexture("Textures/Ground/Marble/marble_01_diff_2k.png");
        baseColorMap.setWrap(Texture.WrapMode.Repeat);

        Texture roughnessMap = assetManager.loadTexture("Textures/Ground/Marble/marble_01_rough_2k.png");
        roughnessMap.setWrap(Texture.WrapMode.Repeat);

        Texture aoMap = assetManager.loadTexture("Textures/Ground/Marble/marble_01_AO_2k.png");
        aoMap.setWrap(Texture.WrapMode.Repeat);

        //Texture dispMap = assetManager.loadTexture("Textures/Ground/Marble/marble_01_disp_2k.png");
        //dispMap.setWrap(Texture.WrapMode.Repeat);

        Texture normalMap = assetManager.loadTexture("Textures/Ground/Marble/marble_01_nor_2k.png");
        normalMap.setWrap(Texture.WrapMode.Repeat);

        material.setTexture("BaseColorMap", baseColorMap);
        material.setTexture("RoughnessMap", roughnessMap);
        material.setTexture("LightMap", aoMap);
        material.setBoolean("LightMapAsAOMap", true);
        //material.setTexture("ParallaxMap", dispMap);
        material.setTexture("NormalMap", normalMap);
        material.setFloat("NormalType", 1.0f);


        // material.setColor("BaseColor", ColorRGBA.LightGray);
        // material.setFloat("Roughness", 0.75f);
        material.setFloat("Metallic", 0.001f);

        // material.setBoolean("UseFog", true);
        // material.setColor("FogColor", new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        // material.setFloat("ExpSqFog", 0.002f);

        Spatial playground = assetManager.loadModel("Models/vehicle-playground/vehicle-playground.j3o");
        //Spatial playground = assetManager.loadModel("Models/track/test-track.gltf.j3o");
        playground.setMaterial(material);

        // RigidBodyControl rigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createMeshShape(playground), 0);
        // playground.addControl(rigidBodyControl);
        // physicsSpace.add(rigidBodyControl);

        // playground.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node p = (Node) playground;
        p.breadthFirstTraversal(spatial -> spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive));


        // rootNode.attachChild(playground);
        playground.setName("playground");
        return playground;
    }

    private void addPostProcessing(DirectionalLight directionalLight) {

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 4);
        shadowFilter.setLight(directionalLight);
        shadowFilter.setShadowIntensity(0.3f);
        shadowFilter.setShadowZExtend(256);
        shadowFilter.setShadowZFadeLength(128);
        // shadowFilter.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        fpp.addFilter(shadowFilter);

        SSAOFilter ssaoFilter = new SSAOFilter();
        fpp.addFilter(ssaoFilter);

        // LightScatteringFilter lightScattering = new LightScatteringFilter();
        // lightScattering.setLightPosition(directionalLight.getDirection());
        // lightScattering.setLightDensity(1);
        // lightScattering.setBlurWidth(1.1f);
        // fpp.addFilter(lightScattering);

        // DepthOfFieldFilter dof = new DepthOfFieldFilter();
        // dof.setFocusDistance(0);
        // dof.setFocusRange(384);
        // dof.setEnabled(false);
        // fpp.addFilter(dof);

        // BloomFilter bloomFilter = new BloomFilter();
        // bloomFilter.setExposurePower(55);
        // bloomFilter.setBloomIntensity(1.2f);
        // fpp.addFilter(bloomFilter);

        viewPort.addProcessor(fpp);
    }

}
