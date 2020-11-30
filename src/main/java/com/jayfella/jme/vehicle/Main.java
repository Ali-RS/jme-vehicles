package com.jayfella.jme.vehicle;

import com.atr.jme.font.asset.TrueTypeLoader;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.gui.LoadingState;
import com.jayfella.jme.vehicle.gui.MainMenuState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioListenerState;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickConnectionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.style.BaseStyles;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.SignalTracker;
import jme3utilities.mesh.Octasphere;

public class Main extends SimpleApplication {

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Main.class.getName());
    /**
     * drop location for vehicles added to the Playground TODO Environment i/f
     */
    final public static Vector3f dropLocation = new Vector3f(0f, 6f, 0f);
    // *************************************************************************
    // fields

    /**
     * application instance
     */
    private static Main application;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a SimpleApplication without FlyCam or debug keys.
     */
    private Main() {
        super(
                new AudioListenerState(),
                new ConstantVerifierState(),
                new StatsAppState()
        );
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Find the first attached AppState that's an instance of the specified
     * class.
     *
     * @param <T>
     * @param subclass the kind of AppState to search for (not null)
     * @return the pre-existing instance (not null)
     */
    public static <T extends AppState> T findAppState(Class<T> subclass) {
        AppStateManager manager = application.getStateManager();
        T appState = manager.getState(subclass);

        assert appState != null;
        return appState;
    }

    /**
     * Access the application instance from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Main getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Main entry point for the More Advanced Vehicles application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String... args) {
        boolean forceDialog = false;
        /*
         * Process any command-line arguments.
         */
        for (String arg : args) {
            switch (arg) {
                case "-f":
                case "--forceDialog":
                    forceDialog = true;
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }
        SignalTracker.logger.setLevel(Level.WARNING);

        AppSettings appSettings = new AppSettings(true);
        appSettings.setResolution(1280, 720);
        appSettings.setTitle("More Advanced Vehicles");
        appSettings.setUseJoysticks(true);
        appSettings.setVSync(true);

        application = new Main();
        application.setDisplayStatView(false);
        application.setDisplayFps(false);
        application.setSettings(appSettings);
        application.setShowSettings(forceDialog);
        application.start();
    }
    // *************************************************************************
    // SimpleApplication methods

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        renderer.setDefaultAnisotropicFilter(4);

        inputManager.addJoystickConnectionListener(new JoystickConnectionListener() {
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
        /*
         * The dash camera sits close to the bodywork, so set its near clipping
         * plane accordingly.
         */
        float near = 0.1f;
        float far = 1800f;
        MyCamera.setNearFar(cam, near, far);

        // initialize Lemur with the "glass" style
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Let there be light
        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(1, -.45f, 0.5f).normalizeLocal(),
                ColorRGBA.White.clone()
        );
        rootNode.addLight(directionalLight);

        // display a rotating texture to entertain users
        LoadingState loadingState = new LoadingState();
        stateManager.attach(loadingState);

        // initialize physics with debug disabled
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        stateManager.attach(bulletAppState);

        // create the driver's heads-up display (disabled)
        DriverHud driverHud = new DriverHud();
        stateManager.attach(driverHud);

        // enable screenshots
        ScreenshotAppState screenshotAppState
                = new ScreenshotAppState("./", "screen_shot");
        stateManager.attach(screenshotAppState);

        // load the sky asyncronously
        CompletableFuture
                .supplyAsync(() -> {
                    Spatial sky = createSky(assetManager,
                            "Textures/Sky/quarry_03_4k.jpg");
                    sky.setQueueBucket(RenderQueue.Bucket.Sky);
                    sky.setShadowMode(RenderQueue.ShadowMode.Off);
                    return sky;
                })
                .whenComplete((spatial, ex) -> {
                    enqueue(() -> rootNode.attachChild(spatial));
                });

        // load the playground async.
        CompletableFuture
                .supplyAsync(() -> {
                    Node node = (Node) loadPlayground();
                    RigidBodyControl rigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createMeshShape(node), 0);
                    node.addControl(rigidBodyControl);
                    return node;
                })
                .whenComplete((node, ex) -> {
                    enqueue(() -> {
                        rootNode.attachChild(node);
                        RigidBodyControl rigidBodyControl = node.getControl(RigidBodyControl.class);
                        bulletAppState.getPhysicsSpace().add(rigidBodyControl);

                        findAppState(LoadingState.class).setEnabled(false);
                        stateManager.attach(new MainMenuState());
                    });
                });

        cam.setLocation(new Vector3f(-200, 50, -200));
        cam.lookAt(new Vector3f(100, 10, 150), Vector3f.UNIT_Y);

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        addPostProcessing(directionalLight);

        // this consumes joystick input. I'll have to investigate why.
        stateManager.getState(FocusNavigationState.class).setEnabled(false);
    }
    // *************************************************************************
    // private methods

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

    /**
     * Generate a sky Geometry from an Equirectangular texture asset.
     *
     * @param assetManager (not null)
     * @param assetPath the asset path to the texture (not null, not empty)
     * @return a new Geometry
     */
    private Spatial createSky(AssetManager assetManager, String assetPath) {
        boolean flipY = true;
        TextureKey textureKey = new TextureKey(assetPath, flipY);
        Texture texture = assetManager.loadTexture(textureKey);
        texture.setAnisotropicFilter(1);

        Material skyMat = new Material(assetManager, "MatDefs/SkyEquirec.j3md");
        skyMat.setTexture("Texture", texture);
        skyMat.setVector3("NormalScale", new Vector3f(1f, 1f, 1f));

        int numRefineSteps = 1;
        float meshRadius = 10f;
        Octasphere sphereMesh = new Octasphere(numRefineSteps, meshRadius);
        MyMesh.reverseNormals(sphereMesh);
        MyMesh.reverseWinding(sphereMesh);

        Geometry result = new Geometry("Sky", sphereMesh);
        result.setCullHint(Spatial.CullHint.Never);
        result.setMaterial(skyMat);
        result.setQueueBucket(RenderQueue.Bucket.Sky);
        result.setShadowMode(RenderQueue.ShadowMode.Off);

        float boundRadius = Float.POSITIVE_INFINITY;
        BoundingSphere bound = new BoundingSphere(boundRadius, Vector3f.ZERO);
        result.setModelBound(bound);

        return result;
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
        RenderState additional = material.getAdditionalRenderState();
        additional.setFaceCullMode(RenderState.FaceCullMode.Off);

        Spatial playground = assetManager.loadModel("Models/vehicle-playground/vehicle-playground.j3o");
        playground.setMaterial(material);

        Node p = (Node) playground;
        p.breadthFirstTraversal(spatial -> spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive));

        playground.setName("playground");
        return playground;
    }
}
