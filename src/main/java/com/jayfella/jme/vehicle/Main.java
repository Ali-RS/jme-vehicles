package com.jayfella.jme.vehicle;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.AssetEventListener;
import com.jme3.asset.AssetKey;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
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
import com.simsilica.lemur.style.BaseStyles;

public class Main extends SimpleApplication {

    public static void main(String... args) {
        Main main = new Main();

        AppSettings appSettings = new AppSettings(true);
        // appSettings.setFrameRate(120);

        appSettings.setResolution(1280, 720);
        appSettings.setTitle("jMonkeyEngine :: Advanced Vehicles");

        main.setDisplayStatView(false);
        main.setDisplayFps(false);

        main.setSettings(appSettings);
        main.setShowSettings(true);
        main.start();
    }

    private Main() {
        super(new StatsAppState(), new AudioListenerState());
    }

    private void setAnistropy() {
        assetManager.addAssetEventListener(new AssetEventListener() {
            @Override public void assetLoaded(AssetKey key) { }

            public void assetRequested(AssetKey key) {
                if (key.getExtension().equals("png") || key.getExtension().equals("jpg") || key.getExtension().equals("dds")) {
                    TextureKey tkey = (TextureKey) key;
                    tkey.setAnisotropy(16);
                }
            }

            @Override public void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey) { }
        });
    }

    @Override
    public void simpleInitApp() {

        setAnistropy();

        //inputManager.clearMappings();
        //inputManager.clearRawInputListeners();

        // set a nice sky color
        // viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));

        // the "hood-cam" gets close to the bodywork, so set the near-frustum accordingly...
        float aspect = (float)cam.getWidth() / (float)cam.getHeight();
        cam.setFrustumPerspective(60, aspect, 0.01f, 1000);

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

        Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/quarry_03_4k.jpg", SkyFactory.EnvMapType.EquirectMap);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);

        // initialize physics
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        getStateManager().attach(bulletAppState);

        Node playground = (Node) loadPlayground(bulletAppState.getPhysicsSpace());

        CarSelectorState carSelectorState = new CarSelectorState(playground, bulletAppState.getPhysicsSpace());
        stateManager.attach(carSelectorState);

        cam.setLocation(new Vector3f(-200, 50, -200));
        cam.lookAt(new Vector3f(100, 10, 150), Vector3f.UNIT_Y);

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        addPostProcessing(directionalLight);

    }

    private Spatial loadPlayground(PhysicsSpace physicsSpace) {

        // Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Material material = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");

        Texture texture = assetManager.loadTexture("Textures/grid.png");
        texture.setWrap(Texture.WrapMode.Repeat);

        // material.setTexture("DiffuseMap", texture);
        material.setTexture("BaseColorMap", texture);

        material.setColor("BaseColor", ColorRGBA.LightGray);
        material.setFloat("Roughness", 0.75f);
        material.setFloat("Metallic", 0.5f);

        // material.setBoolean("UseFog", true);
        // material.setColor("FogColor", new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        // material.setFloat("ExpSqFog", 0.002f);
        Spatial playground = assetManager.loadModel("Models/vehicle-playground/vehicle-playground.j3o");
        playground.setMaterial(material);

        RigidBodyControl rigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createMeshShape(playground), 0);
        playground.addControl(rigidBodyControl);
        physicsSpace.add(rigidBodyControl);

        // playground.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Node p = (Node) playground;
        p.breadthFirstTraversal(spatial -> spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive));


        rootNode.attachChild(playground);
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
