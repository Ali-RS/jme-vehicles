package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TyreDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.examples.cars.DuneBuggy;
import com.jayfella.jme.vehicle.examples.cars.HatchBack;
import com.jayfella.jme.vehicle.examples.cars.PickupTruck;
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
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusManagerState;
import com.simsilica.lemur.style.BaseStyles;

public class Main extends SimpleApplication {

    public static void main(String... args) {
        Main main = new Main();

        AppSettings appSettings = new AppSettings(true);
        // appSettings.setFrameRate(120);

        appSettings.setResolution(1280, 720);
        appSettings.setTitle("jMonkeyEngine :: Advanced Vehicles");

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

        getStateManager().getState(StatsAppState.class).setDisplayStatView(false);

        setAnistropy();

        inputManager.clearMappings();
        inputManager.clearRawInputListeners();

        // set a nice sky color
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));

        // the "hood-cam" gets close to the bodywork, so set the near-frustum accordingly...
        float aspect = (float)cam.getWidth() / (float)cam.getHeight();
        cam.setFrustumPerspective(60, aspect, 0.01f, 1000);

        // the speedo uses Lemur labels.
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Let there be light
        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(1, -.35f, 0.5f).normalizeLocal(),
                ColorRGBA.White.clone()
        );
        rootNode.addLight(directionalLight);

        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.3f)));

        Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/quarry_03_4k.jpg", SkyFactory.EnvMapType.EquirectMap);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        rootNode.attachChild(sky);

        // initialize physics
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        getStateManager().attach(bulletAppState);

        loadPlayground(bulletAppState.getPhysicsSpace());

        // Choose a vehicle
        // ================
        Car vehicle = new PickupTruck(this);
        //Car vehicle = new HatchBack(this);
        //Car vehicle = new DuneBuggy(this);

        vehicle.showSpeedo(Vehicle.SpeedUnit.MPH);
        vehicle.showTacho();
        vehicle.attachToScene(rootNode, bulletAppState.getPhysicsSpace());

        // raise the vehicle a little so it doesn't spawn in the ground.
        vehicle.getVehicleControl().setPhysicsLocation(new Vector3f(0, 2, 0));

        // add some controls
        BasicVehicleInputState basicVehicleInputState = new BasicVehicleInputState(vehicle);
        getStateManager().attach(basicVehicleInputState);

        // the vehicle debug.
        VehicleEditorState vehicleEditorState = new VehicleEditorState(vehicle);
        getStateManager().attach(vehicleEditorState);

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        addPostProcessing(directionalLight);

        MagicFormulaState magicFormulaState = new MagicFormulaState(vehicle);
        stateManager.attach(magicFormulaState);

        // engine debugger
        EnginePowerGraphState enginePowerGraphState = new EnginePowerGraphState(vehicle);
        stateManager.attach(enginePowerGraphState);

        // tyre debugger
        TyreDataState tyreDataState = new TyreDataState(vehicle);
        stateManager.attach(tyreDataState);

    }

    private void loadPlayground(PhysicsSpace physicsSpace) {

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

        rootNode.attachChild(playground);
    }

    private void addPostProcessing(DirectionalLight directionalLight) {

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 2);
        shadowFilter.setLight(directionalLight);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);
        fpp.addFilter(shadowFilter);

        // SSAOFilter ssaoFilter = new SSAOFilter();
        // fpp.addFilter(ssaoFilter);

        //BloomFilter bloomFilter = new BloomFilter();
        //fpp.addFilter(bloomFilter);

        viewPort.addProcessor(fpp);
    }


}
